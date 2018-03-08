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

import opca.memorydb.CitationStore;
import opca.model.OpinionBase;
import opca.model.OpinionStatuteCitation;
import opca.model.OpinionSummary;
import opca.model.StatuteCitation;
import opca.service.SlipOpinionService;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class LoadHistoricalOpinions {
	private static Logger logger = Logger.getLogger(LoadHistoricalOpinions.class.getName());
	private final CitationStore citationStore;
	private final SlipOpinionService slipOpinionService;
	private final EntityManager em;
	
//	OpinionDocumentParser parser;
	
	public LoadHistoricalOpinions(
		EntityManager em, 
		ParserInterface parserInterface 
	) {
		this.em = em;
    	citationStore = CitationStore.getInstance();
    	slipOpinionService = new SlipOpinionService(em);
//		parser = new OpinionDocumentParser(parserInterface.getCodeTitles());
	}
	
    public void initializeDB() throws Exception {
    	Date startTime = new Date();
    	//
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);

	    LoadCourtListenerCallback cb1 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file1 = new LoadCourtListenerFiles(cb1);
	    file1.loadFiles("c:/users/karln/downloads/calctapp-opinions.tar.gz", "c:/users/karln/downloads/calctapp-clusters.tar.gz", 1000);
/*
	    LoadCourtListenerCallback cb2 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file2 = new LoadCourtListenerFiles(cb2);
	    file2.loadFiles("c:/users/karln/downloads/cal-opinions.tar.gz", "c:/users/karln/downloads/cal-clusters.tar.gz", 1000);
*/
/*
	    Iterator<OpinionBase> oit = citationStore.getAllOpinions().iterator();
	    OpinionBase opinion1 = oit.next();
    	em.persist(opinion1);

    	OpinionBase opinion2 = oit.next();
    	em.persist(opinion2);

    	Iterator<StatuteCitation> sit = citationStore.getAllStatutes().iterator();
    	StatuteCitation statute = sit.next();
    	em.persist(statute);
    	
    	Iterator<OpinionStatuteCitation> oscit1 = opinion1.getStatuteCitations().iterator();
    	OpinionStatuteCitation statuteCitation1 = oscit1.next();
    	em.persist(statuteCitation1);
	    
    	Iterator<OpinionStatuteCitation> oscit2 = opinion2.getStatuteCitations().iterator();
    	OpinionStatuteCitation statuteCitation2 = oscit2.next();
    	em.persist(statuteCitation2);
*/
		List<OpinionStatuteCitation> persistOpinionStatuteCitations = Collections.synchronizedList(new ArrayList<>());

		processOpinions(citationStore, persistOpinionStatuteCitations); 
    	processStatutes(citationStore);
    	processOpinionStatuteCitations(persistOpinionStatuteCitations);
    	
		logger.info("count " + citationStore.getAllOpinions().size() + " : " + (new Date().getTime()-startTime.getTime())/1000);
//    	persistMemory(citationStore);
    }

	public void processOpinions(
		CitationStore citationStore, 
		List<OpinionStatuteCitation> persistOpinionStatuteCitations 
    ) throws Exception {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
		try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			OpinionBase[] opArray = new OpinionBase[citationStore.getAllOpinions().size()];    	
			citationStore.getAllOpinions().toArray(opArray);
			List<OpinionBase> opinions = Arrays.asList(opArray);
			List<OpinionBase> persistOpinions = Collections.synchronizedList(new ArrayList<>());
			List<OpinionBase> mergeOpinions = Collections.synchronizedList(new ArrayList<>());
			int opMax = opinions.size();
			// first decide which ones are persist and which are merge
			boolean working = true;
			logger.info("There are " + opMax + " opinions to be saved");
			while ( working ) {
				tasks.clear();
				for ( int i=0; i < processors; ++i ) {
					int from = total>opMax?opMax-1:total;
					int to = (total+number)>opMax?opMax:(total+number);
					List<OpinionBase> clSts = opinions.subList(from, to);
					if ( clSts.size() == 0 ) {
						working = false;
						break;
					}
					total = total + clSts.size();
					tasks.add(Executors.callable(new DivideOpinionsFromMemory(clSts, persistOpinions, mergeOpinions, persistOpinionStatuteCitations)));
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
						List<OpinionBase> clSts = persistOpinions.subList(from, to);
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
						List<OpinionBase> clSts = mergeOpinions.subList(from, to);
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
		} catch ( Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			es.shutdown();
		}
    }

    public void processStatutes(
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
		} catch ( Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			es.shutdown();
		}
    }
	
    class DivideOpinionsFromMemory implements Runnable {
    	List<OpinionBase> opinions;
    	List<OpinionBase> persistOpinions;
    	List<OpinionBase> mergeOpinions;
		List<OpinionStatuteCitation> persistOpinionStatuteCitations;

    	public DivideOpinionsFromMemory(
			List<OpinionBase> opinions, 
	    	List<OpinionBase> persistOpinions, 
	    	List<OpinionBase> mergeOpinions, 
	    	List<OpinionStatuteCitation> persistOpinionStatuteCitations
		) {
    		this.opinions = opinions;
    		this.persistOpinions = persistOpinions; 
    		this.mergeOpinions= mergeOpinions;
    		this.persistOpinionStatuteCitations = persistOpinionStatuteCitations;
    	}
    	
    	@Override
    	public void run() {
    		try {
//	    	EntityManager em = emf.createEntityManager();
//	    	SlipOpinionService slipOpinionService = new SlipOpinionService();
//	    	slipOpinionService.setEntityManager(em);
	    	Date startTime = new Date();
	    	for(OpinionBase opinion: opinions ) {
	    		if ( opinion.getStatuteCitations() != null ) {
		    		for ( OpinionStatuteCitation statuteCitation: opinion.getStatuteCitations() ) {
//		    			if ( !persistOpinionStatuteCitations.contains(statuteCitation)) {
		    				persistOpinionStatuteCitations.add(statuteCitation);
//		    			} else {
//		    				throw new IllegalStateException("Duplicate OpinionStatuteCitation: " + statuteCitation);
//		    			}
		    		}
	    		}
// This causes a NPE !?!?	    		
//	    		opinion.checkCountReferringOpinions();
	    		// this checks the database .. so, it won't be true unless this is a published modification 	    		
	    		OpinionSummary existingOpinion = slipOpinionService.opinionExists(opinion);
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
//	    	em.close();
			logger.info("Divided "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    		} catch ( Exception ex ) {
    			ex.printStackTrace();
    			throw ex;
    		}
    	}
    }

	class PersistOpinionsFromMemory implements Runnable {
		List<OpinionBase> opinions;
	    public PersistOpinionsFromMemory(List<OpinionBase> opinions) {
	    	this.opinions = opinions;
	    }
		@Override
		public void run() {
			try {
//	    	EntityManager em = emf.createEntityManager();
//	    	EntityTransaction tx = em.getTransaction();
//	    	tx.begin();
	    	Date startTime = new Date();
	    	for(OpinionBase opinion: opinions ) {
	    		synchronized(em) {
	    			try {
	    				em.persist(opinion);
	    			} catch ( Exception ex ) {
	    				logger.severe(ex.toString());
	    				throw ex;
	    			}
	    		}
	    	}
//			tx.commit();
//	    	em.close();
			logger.info("Persisted "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
			} catch ( Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
	    }
	}
	  
    class MergeOpinionsFromMemory implements Runnable {
    	List<OpinionBase> opinions;
    	public MergeOpinionsFromMemory(List<OpinionBase> opinions) {
    		this.opinions = opinions;
    	}
    	
    	@Override
    	public void run() {
    		try {
//	    	EntityManager em = emf.createEntityManager();
//	    	EntityTransaction tx = em.getTransaction();
//	    	tx.begin();
	    	Date startTime = new Date();
	    	for(OpinionBase opinion: opinions ) {
	    		synchronized(em) {
	    			em.merge(opinion);
	    		}
	    	}
//	    	tx.commit();
//	    	em.close();
			logger.info("Merged "+opinions.size()+" opinions in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			throw ex;
    		}
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
    		try {
//	    	EntityManager em = emf.createEntityManager();
//	    	slipOpinionService.setEntityManager(em);
	    	int count = statutes.size();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
	    		StatuteCitation existingStatute = slipOpinionService.statuteExists(statute);
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
//	    	em.close();
			logger.info("Divided "+count+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			throw ex;
    		}
    	}
    }

    class PersistStatutesFromMemory implements Runnable {
    	List<StatuteCitation> statutes;
    	public PersistStatutesFromMemory(List<StatuteCitation> statutes) {
    		this.statutes = statutes;
    	}
    	
    	@Override
    	public void run() {
    		try {
//	    	EntityManager em = emf.createEntityManager();
//	    	EntityTransaction tx = em.getTransaction();
//	    	tx.begin();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
	    		synchronized(em) {
	    			em.persist(statute);
	    		}
	    	}
//	    	tx.commit();
//	    	em.close();
			logger.info("Persisted "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			throw ex;
    		}
    	}
    }

    class MergeStatutesFromMemory implements Runnable {
    	List<StatuteCitation> statutes;
    	public MergeStatutesFromMemory(List<StatuteCitation> statutes) {
    		this.statutes = statutes;
    	}
    	
    	@Override
    	public void run() {
    		try {
//	    	EntityManager em = emf.createEntityManager();
//	    	EntityTransaction tx = em.getTransaction();
//	    	tx.begin();
	    	Date startTime = new Date();
	    	for(StatuteCitation statute: statutes ) {
	    		synchronized(em) {
	    			em.merge(statute);
	    		}
	    	}
//	    	tx.commit();
//	    	em.close();
			logger.info("Merged "+statutes.size()+" statutes in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			throw ex;
    		}
    	}
    }

    public void processOpinionStatuteCitations(List<OpinionStatuteCitation> persistOpinionStatuteCitations) 
		throws Exception 
    {
		int processors = Runtime.getRuntime().availableProcessors();
		int number = 1000;
		int total = 0;
		ExecutorService es = Executors.newFixedThreadPool(processors);
		try {
			List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			//now persist opinionStatuteCitations
			boolean working = true;
			int opMax = persistOpinionStatuteCitations.size();
			total = 0;
			logger.info("There are " + opMax + " opinionStatuteCitations to be persisted");
			if ( opMax >= 0 ) {
				while ( working ) {
					tasks.clear();
					for ( int i=0; i < processors; ++i ) {
						int from = total>opMax?opMax-1:total;
						int to = (total+number)>opMax?opMax:(total+number);
						List<OpinionStatuteCitation> clSts = persistOpinionStatuteCitations.subList(from, to);
						if ( clSts.size() == 0 ) {
							working = false;
							break;
						}
						total = total + clSts.size();
						tasks.add(Executors.callable(new PersistOpinionStatuteCitations(clSts)));
					}
					es.invokeAll(tasks);
					logger.info("persisted "+processors+"x"+number+" in " + tasks.size() + " tasks : Total = " + total);
				}
			}
			
		} catch ( Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			es.shutdown();
		}
	}
	class PersistOpinionStatuteCitations implements Runnable {
		List<OpinionStatuteCitation> opinionStatuteCitations;
	    public PersistOpinionStatuteCitations(List<OpinionStatuteCitation> opinionStatuteCitations) {
	    	this.opinionStatuteCitations = opinionStatuteCitations;
	    }
		@Override
		public void run() {
			try {
//	    	EntityManager em = emf.createEntityManager();
//	    	EntityTransaction tx = em.getTransaction();
//	    	tx.begin();
	    	Date startTime = new Date();
	    	for(OpinionStatuteCitation opinionStatuteCitation: opinionStatuteCitations ) {
	    		synchronized(em) {
	    			try {
	    				em.persist(opinionStatuteCitation);
	    			} catch ( Exception ex ) {
	    				logger.severe(ex.toString());
	    				throw ex;
	    			}
	    		}
	    	}
//			tx.commit();
//	    	em.close();
			logger.info("Persisted "+ opinionStatuteCitations.size()+" opinionStatuteCitation in "+((new Date().getTime()-startTime.getTime())/1000) + " seconds");
			} catch ( Exception ex) {
				ex.printStackTrace();
			}
	    }
	}

}
