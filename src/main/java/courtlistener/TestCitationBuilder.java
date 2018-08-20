package courtlistener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import load.CourtListenerCallback;
import load.LoadCourtListenerFiles;
import loadmodel.LoadOpinion;
import opca.memorydb.CitationStore;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.parser.OpinionDocumentParser;
import opca.parser.ParsedOpinionCitationSet;
import opca.parser.ScrapedOpinionDocument;
import statutes.api.IStatutesApi;
import statutesca.statutesapi.CAStatutesApiImpl;

public class TestCitationBuilder {
	public static void main(String...strings) {
		new TestCitationBuilder().run();
	}
	//
	private final CitationStore citationStore;
	private final Object mergeLock;
		
	private TestCitationBuilder() {
    	this.citationStore = CitationStore.getInstance();
		mergeLock = new Object();
	}
	private void run() {
    	try {
    		int newlyLoaded = 0;
    		int notNewlyLoaded = 0;    		
    		int titled = 0;
    		int notTitled= 0;    		
    	    IStatutesApi iStatutesApi = new CAStatutesApiImpl();
    	    iStatutesApi.loadStatutes();
    	    
    	    TestCourtListenerCallback cb1 = new TestCourtListenerCallback(citationStore, iStatutesApi);
    	    LoadCourtListenerFiles files1 = new LoadCourtListenerFiles(cb1);
    	    files1.loadFiles("c:/users/karln/downloads/calctapp-opinions.tar.gz", "c:/users/karln/downloads/calctapp-clusters.tar.gz", 1000);

    	    TestCourtListenerCallback cb2 = new TestCourtListenerCallback(citationStore, iStatutesApi);
    	    LoadCourtListenerFiles files2 = new LoadCourtListenerFiles(cb2);
    	    files2.loadFiles("c:/users/karln/downloads/cal-opinions.tar.gz", "c:/users/karln/downloads/cal-clusters.tar.gz", 1000);
			
    	    for (  OpinionBase op: citationStore.getAllOpinions() ) {
	    		if ( op.isNewlyLoadedOpinion() ) {
	    			newlyLoaded++;
	    		} else {
	    			notNewlyLoaded++;	
	    		}
	    		if ( op.getTitle() == null || op.getTitle().trim().isEmpty()) {
	    			notTitled++;
	    		} else {
	    			titled++;
	    		}
	    	}
	    	
	    	System.out.println("cb1: passed = " + cb1.getTotalPassed() + " looped = " + cb1.getTotalLooped() + " processed = " + cb1.getTotalProcessed() + " added = " + cb1.getTotalAdded() + " merged = " + cb1.getTotalMerged());
	    	System.out.println("cb2: passed = " + cb2.getTotalPassed() + " looped = " + cb2.getTotalLooped() + " processed = " + cb2.getTotalProcessed() + " added = " + cb2.getTotalAdded() + " merged = " + cb2.getTotalMerged());
	    	System.out.println("tot: passed = " + (cb1.getTotalPassed()+cb2.getTotalPassed()) + " looped = " + (cb1.getTotalLooped()+cb2.getTotalLooped()) + " processed = " + (cb1.getTotalProcessed()+cb2.getTotalProcessed()) + " added = " + (cb1.getTotalAdded()+cb2.getTotalAdded()) + " merged = " + (cb1.getTotalMerged()+cb2.getTotalMerged()));
	    	
	    	System.out.println("newlyLoaded = " + newlyLoaded + " notNewlyLoaded = " + notNewlyLoaded);
	    	System.out.println("titled = " + titled + " notTitled = " + notTitled);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class TestCourtListenerCallback implements CourtListenerCallback {
		private IStatutesApi iStatutesApi;
		int totalPassed;
		int totalProcessed;
		int totalAdded;
		int totalMerged;
		int totalLooped;
		private final ExecutorService es; 
		private final List<Callable<Object>> tasks;
		private final List<CitationBuilder> builders;
		
		public TestCourtListenerCallback(CitationStore citationStore, IStatutesApi iStatutesApi) {
			this.iStatutesApi = iStatutesApi;
			es = Executors.newFixedThreadPool(4);
			tasks = new ArrayList<Callable<Object>>();
			builders = new ArrayList<CitationBuilder>();
			totalPassed = 0;
			totalProcessed = 0;
			totalAdded = 0;
			totalMerged = 0;
			totalLooped = 0;
		}

		/* (non-Javadoc)
		 * @see load.CourtListenerCallback#callBack(java.util.List)
		 */
		@Override
		public void callBack(List<LoadOpinion> clOps) {
			CitationBuilder citationBuilder = new CitationBuilder(clOps, iStatutesApi);
			builders.add(citationBuilder);
			tasks.add(Executors.callable(citationBuilder));
			if ( tasks.size() == 4 ) {
				try {
					es.invokeAll(tasks);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					for ( CitationBuilder cb: builders) {
						totalPassed += cb.totalPassed;
						totalProcessed += cb.totalProcessed;
						totalAdded += cb.totalAdded;
						totalMerged += cb.totalMerged;
						totalLooped += cb.totalLooped;
					}
					builders.clear();
					tasks.clear();
				}
			}
			
		}
		@Override
		public void shutdown() {
			try {
				es.invokeAll(tasks);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for ( CitationBuilder cb: builders) {
					totalPassed += cb.totalPassed;
					totalProcessed += cb.totalProcessed;
					totalAdded += cb.totalAdded;
					totalMerged += cb.totalMerged;
					totalLooped += cb.totalLooped;
				}
				builders.clear();
				tasks.clear();
			}
			es.shutdown();
		}
		public int getTotalPassed() {
			return totalPassed;
		}
		public int getTotalProcessed() {
			return totalProcessed;
		}
		public int getTotalAdded() {
			return totalAdded;
		}
		public int getTotalMerged() {
			return totalMerged;
		}
		public int getTotalLooped() {
			return totalLooped;
		}
	}
	
	private class CitationBuilder implements Runnable {
		List<LoadOpinion> clOps;
		private final OpinionDocumentParser parser;
		int totalPassed;
		int totalProcessed;
		int totalAdded;
		int totalMerged;
		int totalLooped;
		CitationStore citationStore = CitationStore.getInstance(); 
		public CitationBuilder(List<LoadOpinion> clOps, IStatutesApi iStatutesApi) {
			parser = new OpinionDocumentParser(iStatutesApi.getStatutesTitles());
			this.clOps = clOps;
			totalPassed = 0;
			totalProcessed = 0;
			totalAdded = 0;
			totalMerged = 0;
			totalLooped = 0;
		}
		@Override
		public void run() {
			totalPassed += clOps.size();
			for (LoadOpinion op : clOps) {
				totalLooped++;

				Document lawBox = Parser.parse(op.getHtml_lawbox(), "");
				Elements ps = lawBox.getElementsByTag("p");
				List<String> paragraphs = new ArrayList<String>();
				List<String> footnotes = new ArrayList<String>();

				for (Element p : ps) {
					String text = p.text();
					if (text.length() == 0)
						continue;
					if (text.charAt(0) == '[' || text.charAt(0) == '(')
						footnotes.add(text);
					else {
						Elements bs = p.getElementsByTag("span");
						for ( Element b: bs) {
							b.remove();
						}
						paragraphs.add(p.text());
					}
				}
				Date dateFiled = op.getDateFiled();
				String name = op.getCitation();
				// if ( name != null && name.contains("Rptr.") ) name =
				// op.getCitation();
				if (name != null) {
					// name = name.toLowerCase().replace(". ",
					// ".").replace("app.", "App.").replace("cal.",
					// "Cal.").replace("supp.", "Supp.");
					OpinionBase opinionBase = new OpinionBase(new OpinionKey(name), op.getCaseName(), dateFiled, "");
					if ( opinionBase.getOpinionKey().toString().equals("57 Cal.App.2d 892") ) {
						System.out.println(opinionBase);
					}
					ScrapedOpinionDocument parserDocument = new ScrapedOpinionDocument(opinionBase);
					parserDocument.setFootnotes( footnotes );
					parserDocument.setParagraphs( paragraphs );
					ParsedOpinionCitationSet parserResults = parser.parseOpinionDocument(parserDocument, opinionBase, citationStore);
					// managed the opinionCitations and statuteCitations
					// referred to
					// add this opinionBase as a referring opinion.
					synchronized ( citationStore ) {
						// when loading big datafile, opinions might already
						// exist if the court has issued a modification
						totalProcessed++;
						OpinionBase existingOpinion;
						existingOpinion = citationStore.opinionExists(opinionBase);
						if (existingOpinion != null) {
							totalMerged++;
							if ( existingOpinion.isNewlyLoadedOpinion() && opinionBase.isNewlyLoadedOpinion() ) {
								existingOpinion.mergeCourtRepublishedOpinion(opinionBase, parserResults, citationStore);
							} else {
								citationStore.mergeParsedDocumentCitations(opinionBase, parserResults);
								existingOpinion.mergePublishedOpinion(opinionBase);
							}
						} else {
							totalAdded++;
							citationStore.mergeParsedDocumentCitations(opinionBase, parserResults);
							citationStore.persistOpinion(opinionBase);
						}
					}
				}
			}
		}
	}
}
