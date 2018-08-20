package load;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import loadmodel.LoadOpinion;
import opca.memorydb.CitationStore;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.parser.OpinionDocumentParser;
import opca.parser.ParsedOpinionCitationSet;
import opca.parser.ScrapedOpinionDocument;
import statutes.api.IStatutesApi;

public class LoadCourtListenerCallback implements CourtListenerCallback {
	private final Logger logger;
	private final CitationStore citationStore;
	private final IStatutesApi iStatutesApi;
	private final int processors;
	private final List<Callable<Object>> tasks;
	private final ExecutorService es;
	

	public LoadCourtListenerCallback(CitationStore citationStore, IStatutesApi iStatutesApi) {
		this.citationStore = citationStore;
		this.iStatutesApi = iStatutesApi;
		logger = Logger.getLogger(LoadCourtListenerCallback.class.getName());
		processors = Runtime.getRuntime().availableProcessors();
		es = Executors.newFixedThreadPool(processors);
		tasks = new ArrayList<Callable<Object>>();
	}

	/* (non-Javadoc)
	 * @see load.CourtListenerCallback#callBack(java.util.List)
	 */
	@Override
	public void callBack(List<LoadOpinion> clOps) {
		tasks.add(Executors.callable(new BuildCitationStore(clOps, citationStore, iStatutesApi)));
		if ( tasks.size() >= processors ) {
			try {
				es.invokeAll(tasks);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Callback tasks interruted", e);
			} finally {
				tasks.clear();
			}
		}
//		new BuildCitationStore(clOps, citationStore, parserInterface).run();
	}

	@Override
	public void shutdown() {
		try {
			es.invokeAll(tasks);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Callback tasks interruted", e);
		} finally {
			tasks.clear();
		}
		es.shutdown();
		
	}

	/**
	 * Create new OpinionSummaries from LoadOpinion types, add to citationStore.
	 * Merging?
	 * 
	 * @author karl
	 *
	 */
	private class BuildCitationStore implements Runnable {
		List<LoadOpinion> clOps;
		CitationStore citationStore;
		private final OpinionDocumentParser parser;

		public BuildCitationStore(List<LoadOpinion> clOps, CitationStore persistence, IStatutesApi iStatutesApi) {
			this.clOps = clOps;
			this.citationStore = persistence;
			parser = new OpinionDocumentParser(iStatutesApi.getStatutesTitles());
		}

		@Override
		public void run() {
			for (LoadOpinion op : clOps) {

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
				String name = op.getCitation();
				// if ( name != null && name.contains("Rptr.") ) name =
				// op.getCitation();
				if (name != null) {
					// name = name.toLowerCase().replace(". ",
					// ".").replace("app.", "App.").replace("cal.",
					// "Cal.").replace("supp.", "Supp.");
					OpinionBase opinionBase = new OpinionBase(new OpinionKey(name), op.getCaseName(), op.getDateFiled(), "");
					//
		        	OpinionBase existingOpinion = citationStore.findOpinionByOpinion(opinionBase);
		            if ( existingOpinion != null ) {
		            	opinionBase = existingOpinion;
		            }
					//
					ScrapedOpinionDocument parserDocument = new ScrapedOpinionDocument(opinionBase);
					parserDocument.setFootnotes( footnotes );
					parserDocument.setParagraphs( paragraphs );

		    		ParsedOpinionCitationSet parserResults = parser.parseOpinionDocument(parserDocument, opinionBase, citationStore);
					synchronized ( citationStore ) {
						citationStore.mergeParsedDocumentCitations(opinionBase, parserResults);
						citationStore.persistOpinion(opinionBase);
//						System.out.println( opinionSummary.fullPrint() );
					}
				}
			}
		}
	}
}
