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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

import apimodel.ApiOpinion;
import apimodel.Cluster;
import codesparser.CodesInterface;
import loadmodel.LoadOpinion;
import opca.memorydb.CitationStore;
import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.StatuteCitation;
import opca.parser.OpinionDocumentParser;
import opca.parser.ScrapedOpinionDocument;
import opca.parser.ParsedOpinionResults;
import opca.service.SlipOpinionService;

public class LoadHistoricalOpinions {
	private static Logger logger = Logger.getLogger(LoadHistoricalOpinions.class.getName());
	private static final Pattern pattern = Pattern.compile("/");
	EntityManagerFactory emf;
	OpinionDocumentParser parser;
	
	public LoadHistoricalOpinions(
		EntityManagerFactory emf, 
		CodesInterface codesInterface 
	) {
		this.emf = emf;
		parser = new OpinionDocumentParser(codesInterface.getCodeTitles());
	}
	
    public void initializeDB() throws Exception {
    	Date startTime = new Date();
    	CitationStore citationStore = CitationStore.getInstance();
    	//
    	readStream(citationStore, "c:/users/karl/downloads/calctapp-opinions.tar.gz", "c:/users/karl/downloads/calctapp-clusters.tar.gz");
    	readStream(citationStore, "c:/users/karl/downloads/cal-opinions.tar.gz", "c:/users/karl/downloads/cal-clusters.tar.gz");
    	processesOpinions(citationStore); 
    	processesStatutes(citationStore); 
		System.out.println("count " + citationStore.getAllOpinions().size() + " : " + (new Date().getTime()-startTime.getTime())/1000);
//    	persistMemory(citationStore);
    }

    private void readStream(
		CitationStore citationStore, 
		String opinionsFileName, 
		String clustersFileName
    ) throws Exception {
    	int processors = Runtime.getRuntime().availableProcessors();
    	int number = 1000;
    	int total = 0;
		//
    	ObjectMapper om = new ObjectMapper();
		Map<Long, LoadOpinion> mapLoadOpinions = new TreeMap<Long, LoadOpinion>();

    	TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(clustersFileName))));
    	TarArchiveEntry entry;
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
//	            System.out.println("Content:" +  new String(content));
	            // http://www.courtlistener.com/api/rest/v3/clusters/1361768/
	            Cluster cluster = om.readValue(content, Cluster.class);
	            if ( cluster.getPrecedential_status() != null && cluster.getPrecedential_status().equals("Published") ) {
		            LoadOpinion loadOpinion = new LoadOpinion(cluster);
		            if( loadOpinion.getCaseName() != null || !loadOpinion.getCaseName().trim().isEmpty() ) {
			            mapLoadOpinions.put(loadOpinion.getId(), loadOpinion);
		            }
	            }
	        }
        }
		tarIn.close();
		//
    	tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(opinionsFileName))));
    	ExecutorService es = Executors.newFixedThreadPool(processors);
    	List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		try {
    	  boolean working = true;
    	  while ( working ) {
    		  tasks.clear();
        	  for ( int i=0; i < processors; ++i ) {
        		  List<LoadOpinion> clOps = getCases(tarIn, om, mapLoadOpinions, number);
        		  if ( clOps.size() == 0 ) {
        			  working = false;
        			  break;
        		  }
        		  total = total + clOps.size();
        		  tasks.add(Executors.callable(new BuildMemoryDB(clOps, citationStore)));
        	  }
			  es.invokeAll(tasks);
			  System.out.println("computed "+processors+"x"+number+" : Total = " + total);
	      }
      } finally {
          es.shutdown();
          tarIn.close();
      }
    }
    
    public void processesOpinions(
		CitationStore citationStore 
    ) throws Exception {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
		try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			OpinionSummary[] opArray = new OpinionSummary[citationStore.getAllOpinions().size()];    	
			citationStore.getAllOpinions().toArray(opArray);
			List<OpinionSummary> opinions = Arrays.asList(opArray);
			List<OpinionSummary> persistOpinions = Collections.synchronizedList(new ArrayList<OpinionSummary>());
			List<OpinionSummary> mergeOpinions = Collections.synchronizedList(new ArrayList<OpinionSummary>());
			int opMax = opinions.size();
			// first decide which ones are persist and which are merge
			boolean working = true;
			System.out.println("There are " + opMax + " opinions to be saved");
			while ( working ) {
				tasks.clear();
				for ( int i=0; i < processors; ++i ) {
					int from = total>opMax?opMax-1:total;
					int to = (total+number)>opMax?opMax:(total+number);
					List<OpinionSummary> clSts = opinions.subList(from, to);
					if ( clSts.size() == 0 ) {
						working = false;
						break;
					}
					total = total + clSts.size();
					tasks.add(Executors.callable(new DivideOpinionsFromMemory(clSts, persistOpinions, mergeOpinions)));
				}
				es.invokeAll(tasks);
				System.out.println("divided "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
			}
			//now persist opinions
			working = true;
			opMax = persistOpinions.size();
			total = 0;
			System.out.println("There are " + opMax + " opinions to be persisted");
			if ( opMax >= 0 ) {
				while ( working ) {
					tasks.clear();
					for ( int i=0; i < processors; ++i ) {
						int from = total>opMax?opMax-1:total;
						int to = (total+number)>opMax?opMax:(total+number);
						List<OpinionSummary> clSts = persistOpinions.subList(from, to);
						if ( clSts.size() == 0 ) {
							working = false;
							break;
						}
						total = total + clSts.size();
						tasks.add(Executors.callable(new PersistOpinionsFromMemory(clSts)));
					}
					es.invokeAll(tasks);
					System.out.println("persisted "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
			//now merge opinions
			working = true;
			opMax = mergeOpinions.size();
			total = 0;
			System.out.println("There are " + opMax + " opinions to be merged");
			if ( opMax >= 0 ) {
				while ( working ) {
					tasks.clear();
					for ( int i=0; i < processors; ++i ) {
						int from = total>opMax?opMax-1:total;
						int to = (total+number)>opMax?opMax:(total+number);
						List<OpinionSummary> clSts = mergeOpinions.subList(from, to);
						if ( clSts.size() == 0 ) {
							working = false;
							break;
						}
						total = total + clSts.size();
						tasks.add(Executors.callable(new MergeOpinionsFromMemory(clSts)));
					}
					es.invokeAll(tasks);
					System.out.println("merged "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
		} finally {
			es.shutdown();
		}
    }

    public void processesStatutes(
		CitationStore citationStore 
    ) throws Exception {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
		try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			StatuteCitation[] stArray = new StatuteCitation[citationStore.getAllStatutes().size()];    	
			citationStore.getAllStatutes().toArray(stArray);
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
	
    class DivideOpinionsFromMemory implements Runnable {
    	List<OpinionSummary> opinions;
    	List<OpinionSummary> persistOpinions;
    	List<OpinionSummary> mergeOpinions;
    	public DivideOpinionsFromMemory(
			List<OpinionSummary> opinions, 
	    	List<OpinionSummary> persistOpinions, 
	    	List<OpinionSummary> mergeOpinions
		) {
    		this.opinions = opinions;
    		this.persistOpinions = persistOpinions; 
    		this.mergeOpinions= mergeOpinions; 
    	}
    	
    	@Override
    	public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	SlipOpinionService slipOpinionService = new SlipOpinionService();
	    	slipOpinionService.setEntityManager(em);
	    	Date startTime = new Date();
	    	for(OpinionSummary opinion: opinions ) {
// This causes a NPE !?!?	    		
//	    		opinion.checkCountReferringOpinions();
	    		// this checks the database .. so, it won't be true unless this is a published modification 
	    		OpinionSummary existingOpinion = slipOpinionService.opinionExists(opinion.getOpinionKey());
				if ( existingOpinion == null ) {
					persistOpinions.add(opinion);
				} else {
					// first time through, this never happens ... all duplicates have been merged
//					existingOpinion.mergeCourtModifications(opinion, slipOpinionService.getPersistenceLookup());
					//opinion referred to itself?
					// Was there a problem with an opinion referring to itself?
					// what about the entire collection of referred from?
					// in other words, it's a modification already in memory DB
					// and now its another modification during save?
//                    existingOpinion.addOpinionSummaryReferredFrom(opinion.getOpinionKey());
					mergeOpinions.add(existingOpinion);
					throw new RuntimeException("Merge on DivideOpinionsFromMemory");					
				}
	    	}
	    	em.close();
			System.out.println("Divided "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	Date startTime = new Date();
	    	for(OpinionSummary opinion: opinions ) {
				em.persist(opinion);
	    	}
			tx.commit();
	    	em.close();
			System.out.println("Persisted "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
	    }
	}
	  
    class MergeOpinionsFromMemory implements Runnable {
    	List<OpinionSummary> opinions;
    	public MergeOpinionsFromMemory(List<OpinionSummary> opinions) {
    		this.opinions = opinions;
    	}
    	
    	@Override
    	public void run() {
	    	EntityManager em = emf.createEntityManager();
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	Date startTime = new Date();
	    	for(OpinionSummary opinion: opinions ) {
				em.merge(opinion);
	    	}
	    	tx.commit();
	    	em.close();
			System.out.println("Merged "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
	    	SlipOpinionService slipOpinionService = new SlipOpinionService();
	    	slipOpinionService.setEntityManager(em);
	    	int count = statutes.size();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
	    		StatuteCitation existingStatute = slipOpinionService.statuteExists(statute.getStatuteKey());
				if ( existingStatute == null ) {
					persistStatutes.add(statute);
				} else {
// need to do addModifications again in case it already exists in the database
// should never happen in first load
//					existingStatute.addModificationsFromTempStatute(statute);
					mergeStatutes.add(existingStatute);
					throw new RuntimeException("Merge on DivideStatutesFromMemory");					
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
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
				em.persist(statute);
	    	}
	    	tx.commit();
	    	em.close();
			System.out.println("Persisted "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
				em.merge(statute);
	    	}
	    	tx.commit();
	    	em.close();
			System.out.println("Merged "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    	}
    }

    private List<LoadOpinion> getCases(
		TarArchiveInputStream tarIn, 
		ObjectMapper om, 
		Map<Long, LoadOpinion> mapLoadOpinions, 
		int number
	) throws Exception {
    	TarArchiveEntry entry;
    	int count = 0;
        List<LoadOpinion> clOps = new ArrayList<LoadOpinion>(number); 
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
//	            System.out.println("Content:" +  new String(content));
	            ApiOpinion op = om.readValue(content, ApiOpinion.class);
	            
	            Long id = new Long(pattern.split(op.getResource_uri())[7]);
	            LoadOpinion loadOpinion = mapLoadOpinions.get(id);
	            if ( loadOpinion != null  ) {
	            	if ( op.getHtml_lawbox() != null ) {
		            	loadOpinion.setHtml_lawbox(op.getHtml_lawbox());
		            	loadOpinion.setOpinions_cited(op.getOpinions_cited());
	    	            clOps.add(loadOpinion);
	            	}
    	            mapLoadOpinions.remove(id);
	            }
/*	            
	            if( op.getPrecedentialStatus() == null ) {
	            	continue;
	            }
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
*/	            
	
	            if ( ++count >= number ) break;
	        }
        }
        return clOps;
    }
    
    /**
     * Create new OpinionSummaries from LoadOpinion types, add to citationStore.
     * Merging?
     * @author karl
     *
     */
    class BuildMemoryDB implements Runnable {
		List<LoadOpinion> clOps;  
    	CitationStore persistence;
    	DateFormat clFormat;
    	
		public BuildMemoryDB(    	
			List<LoadOpinion> clOps,  
	    	CitationStore persistence
		)  {
			this.clOps = clOps;
			this.persistence = persistence;
			clFormat = new SimpleDateFormat("YYYY-mm-dd");
		}
    	@Override
		public void run() {
			try {
		    	for ( LoadOpinion op: clOps ) {
			        
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
			            else
			                paragraphs.add(text);
			        }
			        Date dateFiled = op.getDateFiled();
			        String name = op.getCitation();
//			        if ( name != null && name.contains("Rptr.") ) name = op.getCitation();
			        if ( name != null ) {
//			            name = name.toLowerCase().replace(". ", ".").replace("app.", "App.").replace("cal.", "Cal.").replace("supp.", "Supp.");
			            OpinionSummary opinionSummary = new OpinionSummary(
			                    new OpinionKey(name),
			                    op.getCaseName(),
			                    dateFiled, 
			                    ""
			                );
				        ScrapedOpinionDocument parserDocument = new ScrapedOpinionDocument(opinionSummary);
				        parserDocument.footnotes = footnotes; 
				        parserDocument.paragraphs = paragraphs; 
			        	ParsedOpinionResults parserResults = parser.parseOpinionDocument(parserDocument, opinionSummary, opinionSummary.getOpinionKey());
			        	synchronized(persistence) {
			        		// managed the opinionCitations and statuteCitations referred to
			        		// add this opinionSummary as a referring opinion.
			        		persistence.mergeParsedDocumentCitations(opinionSummary, parserResults);
			            	// when loading big datafile, opinions might already exist if the court has issued a modification
			        		OpinionSummary existingOpinion = persistence.opinionExists(opinionSummary.getOpinionKey());
			                if (  existingOpinion != null ) {
			                	// this can only be a modification?
			                	// does the source already handle modifications? Let's hope not.
			                	// so, what about referring opinions?
			                    existingOpinion.mergePublishedOpinion(opinionSummary);
			                    // opinion referred to itself?
//			                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionKey());
			                    // in case of citationStore, merge does same thing as persist (as does EntityManager)
// don't need this, it's already there		persistence.mergeOpinion(existingOpinion);
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
