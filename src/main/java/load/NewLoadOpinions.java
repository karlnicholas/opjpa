package load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import clread.jsonmodel.CourtListenerOpinion;
import clread.memorydb.MemoryDBFacade;
import codesparser.CodesInterface;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionKey;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;

public class NewLoadOpinions {
	EntityManagerFactory emf;
	CodeCitationParser parser;
	
	public NewLoadOpinions(
		EntityManagerFactory emf, 
		CodesInterface codesInterface 
	) {
		this.emf = emf;
		parser = new CodeCitationParser(codesInterface.getCodeTitles());
	}
	
    public void initializeDB() throws Exception {
    	Date startTime = new Date();
    	MemoryDBFacade memoryDB = MemoryDBFacade.getInstance();
    	readStream(memoryDB, "c:/users/karl/downloads/calctapp.tar.gz");
    	readStream(memoryDB, "c:/users/karl/downloads/cal.tar.gz");
    	processesStatutes(memoryDB); 
    	processesOpinions(memoryDB); 
		System.out.println("count " + memoryDB.getAllOpinions().size() + " : " + (new Date().getTime()-startTime.getTime())/1000);
//    	persistMemory(memoryDB);
    }

    private void readStream(
		MemoryDBFacade memoryDB, 
		String fileName
    ) throws Exception {
    	int processors = Runtime.getRuntime().availableProcessors();
    	int number = 1000;
    	int total = 0;
    	ObjectMapper om = new ObjectMapper();
    	TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(fileName))));
    	ExecutorService es = Executors.newFixedThreadPool(processors);
    	List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		try {
    	  boolean working = true;
    	  while ( working ) {
    		  tasks.clear();
        	  for ( int i=0; i < processors; ++i ) {
        		  List<CourtListenerOpinion> clOps = getCases(tarIn, om, number);
        		  if ( clOps.size() == 0 ) {
        			  working = false;
        			  break;
        		  }
        		  total = total + clOps.size();
        		  tasks.add(Executors.callable(new BuildMemoryDB(clOps, memoryDB)));
        	  }
			  es.invokeAll(tasks);
			  System.out.println("computed "+processors+"x"+number+" : Total = " + total);
	      }
      } finally {
          es.shutdown();
          tarIn.close();
      }
    }
    
    private void processesOpinions(
		MemoryDBFacade memoryDB 
    ) throws Exception {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
    	try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			OpinionSummary[] opArray = new OpinionSummary[memoryDB.getAllOpinions().size()];    	
			memoryDB.getAllOpinions().toArray(opArray);
			List<OpinionSummary> opinions = Arrays.asList(opArray);
			int opMax = opinions.size()-1;
			boolean working = true;
			while ( working ) {
				tasks.clear();
				for ( int i=0; i < processors; ++i ) {
					int from = total>opMax?opMax:total;
					int to = (total+number)>opMax?opMax:(total+number);
					List<OpinionSummary> clOps = opinions.subList(from, to);
					if ( clOps.size() == 0 ) {
						working = false;
						break;
					}
					total = total + clOps.size();
					tasks.add(Executors.callable(new PersistOpinionsFromMemory(clOps)));
				}
				es.invokeAll(tasks);
				System.out.println("saved "+processors+"x"+number+" : Total = " + total);
			}
    	} finally {
    		es.shutdown();
    	}
    }

    private void processesStatutes(
		MemoryDBFacade memoryDB 
    ) throws Exception {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
		try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			StatuteCitation[] stArray = new StatuteCitation[memoryDB.getAllStatutes().size()];    	
			memoryDB.getAllStatutes().toArray(stArray);
			List<StatuteCitation> statutes = Arrays.asList(stArray);
			List<StatuteCitation> persistStatutes = Collections.synchronizedList(new ArrayList<StatuteCitation>());
			List<StatuteCitation> mergeStatutes = Collections.synchronizedList(new ArrayList<StatuteCitation>());
			int stMax = statutes.size();
			// first decide which ones are persist and which are merge
			boolean working = true;
			System.out.println("There are " + stMax + " statutes to be saved");
			while ( working ) {
				tasks.clear();
				for ( int i=0; i < processors; ++i ) {
					int from = total>stMax?stMax-1:total;
					int to = (total+number)>stMax?stMax:(total+number);
					List<StatuteCitation> clSts = statutes.subList(from, to);
					if ( clSts.size() == 0 ) {
						working = false;
						break;
					}
					total = total + clSts.size();
					tasks.add(Executors.callable(new DivideStatutesFromMemory(clSts, persistStatutes, mergeStatutes)));
				}
				es.invokeAll(tasks);
				System.out.println("divided "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
			}
			//now persist statutes
			working = true;
			stMax = persistStatutes.size();
			total = 0;
			System.out.println("There are " + stMax + " statutes to be persisted");
			if ( stMax >= 0 ) {
				while ( working ) {
					tasks.clear();
					for ( int i=0; i < processors; ++i ) {
						int from = total>stMax?stMax-1:total;
						int to = (total+number)>stMax?stMax:(total+number);
						List<StatuteCitation> clSts = persistStatutes.subList(from, to);
						if ( clSts.size() == 0 ) {
							working = false;
							break;
						}
						total = total + clSts.size();
						tasks.add(Executors.callable(new PersistStatutesFromMemory(clSts)));
					}
					es.invokeAll(tasks);
					System.out.println("persisted "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
			//now merge statutes
			working = true;
			stMax = mergeStatutes.size();
			total = 0;
			System.out.println("There are " + stMax + " statutes to be merged");
			if ( stMax >= 0 ) {
				while ( working ) {
					tasks.clear();
					for ( int i=0; i < processors; ++i ) {
						int from = total>stMax?stMax-1:total;
						int to = (total+number)>stMax?stMax:(total+number);
						List<StatuteCitation> clSts = mergeStatutes.subList(from, to);
						if ( clSts.size() == 0 ) {
							working = false;
							break;
						}
						total = total + clSts.size();
						tasks.add(Executors.callable(new MergeStatutesFromMemory(clSts)));
					}
					es.invokeAll(tasks);
					System.out.println("merged "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
		} finally {
			es.shutdown();
		}
    }
	
	class PersistOpinionsFromMemory implements Runnable {
		List<OpinionSummary> opinions;
	    public PersistOpinionsFromMemory(List<OpinionSummary> opinions) {
	    	this.opinions = opinions;
	    }
		@Override
		public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	DatabaseFacade database = new DatabaseFacade(em);
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	int count = opinions.size();
	    	Date startTime = new Date();
	    	for(OpinionSummary opinion: opinions ) {
				database.persistOpinion(opinion);
	    	}
			tx.commit();
	    	em.close();
			System.out.println("Persisted "+count+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
	    }
	}
	  
    class DivideStatutesFromMemory implements Runnable {
    	List<StatuteCitation> statutes;
    	List<StatuteCitation> persistStatutes;
    	List<StatuteCitation> mergeStatutes;
    	public DivideStatutesFromMemory(
			List<StatuteCitation> statutes, 
	    	List<StatuteCitation> persistStatutes, 
	    	List<StatuteCitation> mergeStatutes
		) {
    		this.statutes = statutes;
    		this.persistStatutes = persistStatutes; 
    		this.mergeStatutes = mergeStatutes; 
    	}
    	
    	@Override
    	public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	DatabaseFacade database = new DatabaseFacade(em);
	    	int count = statutes.size();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
	    		StatuteCitation existingStatute = database.statuteExists(statute.getStatuteKey());
				if ( existingStatute == null ) {
					persistStatutes.add(statute);
				} else {
					existingStatute.addModifications(statute);
					mergeStatutes.add(existingStatute);
				}
	    	}
	    	em.close();
			System.out.println("Divided "+count+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    	}
    }

    class PersistStatutesFromMemory implements Runnable {
    	List<StatuteCitation> statutes;
    	public PersistStatutesFromMemory(List<StatuteCitation> statutes) {
    		this.statutes = statutes;
    	}
    	
    	@Override
    	public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	DatabaseFacade database = new DatabaseFacade(em);
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	int count = statutes.size();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
				database.persistStatute(statute);
	    	}
	    	tx.commit();
	    	em.close();
			System.out.println("Persisted "+count+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    	}
    }

    class MergeStatutesFromMemory implements Runnable {
    	List<StatuteCitation> statutes;
    	public MergeStatutesFromMemory(List<StatuteCitation> statutes) {
    		this.statutes = statutes;
    	}
    	
    	@Override
    	public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	DatabaseFacade database = new DatabaseFacade(em);
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	int count = statutes.size();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
				database.mergeStatute(statute);
	    	}
	    	tx.commit();
	    	em.close();
			System.out.println("Persisted "+count+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    	}
    }

    private List<CourtListenerOpinion> getCases(
		TarArchiveInputStream tarIn, 
		ObjectMapper om, 
		int number
	) throws Exception {
    	TarArchiveEntry entry;
    	int count = 0;
        List<CourtListenerOpinion> clOps = new ArrayList<CourtListenerOpinion>(250); 
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
	            CourtListenerOpinion op = om.readValue(content, CourtListenerOpinion.class);
	            if (op.getPrecedentialStatus().toLowerCase().equals("unpublished")) {
	                continue;
	            }
	            if (op.getHtmlLawbox() == null) {
	                continue;
	            }
	            if (op.getCitation().getCaseName().trim().length() == 0) {
	            	System.out.print('T');
	                continue;
	            }
	
	            clOps.add(op);
	            if ( ++count >= number ) break;
	        }
        }
        return clOps;
    }
    
    class BuildMemoryDB implements Runnable {
		List<CourtListenerOpinion> clOps;  
    	MemoryDBFacade persistence;
    	DateFormat clFormat;
    	
		public BuildMemoryDB(    	
			List<CourtListenerOpinion> clOps,  
	    	MemoryDBFacade persistence
		)  {
			this.clOps = clOps;
			this.persistence = persistence;
			clFormat = new SimpleDateFormat("YYYY-mm-dd");
		}
    	@Override
		public void run() {
			try {
		    	for ( CourtListenerOpinion op: clOps ) {
			        
			        Document lawBox = Parser.parse(op.getHtmlLawbox(), "");
			        Elements ps = lawBox.getElementsByTag("p");
			
			        ParserDocument parserDocument = new ParserDocument();
			        for (Element p : ps) {
			            String text = p.text();
			            if (text.length() == 0)
			                continue;
			            if (text.charAt(0) == '[' || text.charAt(0) == '(')
			                parserDocument.footnotes.add(text);
			            else
			                parserDocument.paragraphs.add(text);
			        }
			        Date dateFiled = clFormat.parse(op.getDateFiled());
			        String name = op.getCitation().getStateCiteOne();
			        if ( name != null && name.contains("Rptr.") ) name = op.getCitation().getStateCiteTwo();
			        if ( name != null ) {
			            name = name.toLowerCase().replace(". ", ".").replace("app.", "App.").replace("cal.", "Cal.").replace("supp.", "Supp.");
			            OpinionSummary opinionSummary = new OpinionSummary(
			                    new OpinionKey(name),
			                    op.getCitation().getCaseName(),
			                    dateFiled, 
			                    dateFiled, 
			                    ""
			                );
			        	ParserResults parserResults = parser.parseCase(parserDocument, opinionSummary, opinionSummary.getOpinionKey());
			        	synchronized(persistence) {
			            	parserResults.persist(opinionSummary, persistence);
			        		OpinionSummary existingOpinion = persistence.opinionExists(opinionSummary.getOpinionKey());
			                if (  existingOpinion != null ) {
			                    existingOpinion.addModifications(opinionSummary, persistence);
			                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionKey());
			                    persistence.mergeOpinion(existingOpinion);
			                } else {
			                	persistence.persistOpinion(opinionSummary);
			                }
			        	}
			        }
		        }
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
		    	clOps.clear();
			}
		}
    }

}
