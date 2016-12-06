package load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import opca.memorydb.CitationStore;
import opca.model.OpinionSummary;
import opca.model.StatuteCitation;
import opca.service.SlipOpinionService;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class LoadHistoricalOpinions {
	private static Logger logger = Logger.getLogger(LoadHistoricalOpinions.class.getName());
	EntityManagerFactory emf;
	private final CitationStore citationStore;
//	OpinionDocumentParser parser;
	
	public LoadHistoricalOpinions(
		EntityManagerFactory emf, 
		ParserInterface parserInterface 
	) {
		this.emf = emf;
    	citationStore = CitationStore.getInstance();
//		parser = new OpinionDocumentParser(parserInterface.getCodeTitles());
	}
	
    public void initializeDB() throws Exception {
    	Date startTime = new Date();
    	//
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);

	    LoadCourtListenerCallback cb1 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file1 = new LoadCourtListenerFiles(cb1);
	    file1.loadFiles("c:/users/karln/downloads/calctapp-opinions.tar.gz", "c:/users/karln/downloads/calctapp-clusters.tar.gz", 1000);

	    LoadCourtListenerCallback cb2 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file2 = new LoadCourtListenerFiles(cb2);
	    file2.loadFiles("c:/users/karln/downloads/cal-opinions.tar.gz", "c:/users/karln/downloads/cal-clusters.tar.gz", 1000);

	    processesOpinions(citationStore); 
    	processesStatutes(citationStore); 
		logger.info("count " + citationStore.getAllOpinions().size() + " : " + (new Date().getTime()-startTime.getTime())/1000);
//    	persistMemory(citationStore);
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
			logger.info("There are " + opMax + " opinions to be saved");
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
				logger.info("divided "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
			}
			//now persist opinions
			working = true;
			opMax = persistOpinions.size();
			total = 0;
			logger.info("There are " + opMax + " opinions to be persisted");
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
					logger.info("persisted "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
			//now merge opinions
			working = true;
			opMax = mergeOpinions.size();
			total = 0;
			logger.info("There are " + opMax + " opinions to be merged");
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
					logger.info("merged "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
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
			logger.info("There are " + stMax + " statutes to be saved");
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
				logger.info("divided "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
			}
			//now persist statutes
			working = true;
			stMax = persistStatutes.size();
			total = 0;
			logger.info("There are " + stMax + " statutes to be persisted");
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
					logger.info("persisted "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
			//now merge statutes
			working = true;
			stMax = mergeStatutes.size();
			total = 0;
			logger.info("There are " + stMax + " statutes to be merged");
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
					logger.info("merged "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
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
			logger.info("Divided "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
			logger.info("Persisted "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
			logger.info("Merged "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
			logger.info("Divided "+count+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
			logger.info("Persisted "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
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
			logger.info("Merged "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    	}
    }

}
