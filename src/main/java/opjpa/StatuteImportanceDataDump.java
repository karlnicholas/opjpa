package opjpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import opca.memorydb.PersistenceLookup;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.SlipOpinion;
import opca.model.SlipProperties;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parser.ParsedOpinionCitationSet;
import opca.service.RestServicesFactory;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import service.Client;

public class StatuteImportanceDataDump implements AutoCloseable {
	private EntityManagerFactory emf;
	private EntityManager em;

    public static void main(String... args) throws Exception {
    	try ( StatuteImportanceDataDump statuteImportance = new StatuteImportanceDataDump() ) {	    	
	    	List<OpinionView> getOpinionCases = statuteImportance.getOpinionCases(true, 2);
/*	    	
	    	for( OpinionView opinionView: getOpinionCases) {
	    		System.out.println("\n=============================");
	    		for( StatuteView statuteView: opinionView.getStatutes() ) {
	        		System.out.println("\t"+statuteView.getImportance()+":"+statuteView.getDisplaySections()+":"+statuteView.getDisplayTitlePath());
	    		}
	    		for( CaseView caseView: opinionView.getCases() ) {
	        		System.out.println("\t"
        				+caseView.getImportance()
        				+":"+caseView.getCitation()
        				+(caseView.getOpinionDate()==null?"":" ("+caseView.getOpinionDate()+")")
        				+(caseView.getTitle()==null?"":" " + caseView.getTitle())
    				);
	    		}
	    	}
*/	    	
    	}
    }

	public StatuteImportanceDataDump() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	public List<OpinionView> getOpinionCases(
			boolean compressCodeReferences, 
			int levelOfInterest
		) {
			List<OpinionView> opinionViews = new ArrayList<OpinionView>();
	        Client statutesRs = new RestServicesFactory().connectStatutesRsService();
			//
			OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder(statutesRs);
			List<SlipOpinion> opinions = findByPublishDateRange();
			MyPersistenceLookup pl = new MyPersistenceLookup(this);
			List<OpinionBase> opinionOpinionCitations = new ArrayList<>();
			List<Integer> opinionIds = new ArrayList<>();
			int i = 0;
			for ( SlipOpinion slipOpinion: opinions ) {
				opinionIds.add(slipOpinion.getId());
				if ( ++i % 100 == 0 ) {
					opinionOpinionCitations.addAll( 
						em.createNamedQuery("OpinionBase.fetchOpinionCitationsForOpinions", OpinionBase.class).setParameter("opinionIds", opinionIds).getResultList()
					);
					opinionIds.clear();
				}
			}
			opinionOpinionCitations.addAll( 
				em.createNamedQuery("OpinionBase.fetchOpinionCitationsForOpinions", OpinionBase.class).setParameter("opinionIds", opinionIds).getResultList()
			);
			for ( SlipOpinion slipOpinion: opinions ) {
//				slipOpinion.setOpinionCitations( fetchOpinions.setParameter("id", slipOpinion.getId()).getSingleResult().getOpinionCitations() );
				slipOpinion.setOpinionCitations( opinionOpinionCitations.get( opinionOpinionCitations.indexOf(slipOpinion)).getOpinionCitations() );
				ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion, pl);
				OpinionView opinionView = opinionViewBuilder.buildOpinionView(slipOpinion, parserResults);
				opinionView.combineCommonSections();
				opinionView.trimToLevelOfInterest(levelOfInterest, true);
				opinionView.scoreCitations(opinionViewBuilder);
				
				opinionViews.add(opinionView);
			}
			return opinionViews;	
		}

		// OpinionBase
		public OpinionBase opinionExists(OpinionBase opinion) {
			List<OpinionBase> list = em.createNamedQuery("OpinionBase.findByOpinionKey", OpinionBase.class).setParameter("key", opinion.getOpinionKey()).getResultList();
			if ( list.size() > 0 ) return list.get(0);
			return null;
		}

		public List<OpinionBase> getOpinions(Collection<OpinionBase> opinions) {
			if ( opinions.size() == 0 ) return new ArrayList<OpinionBase>();
			List<OpinionKey> keys = new ArrayList<>();
			for(OpinionBase opinion: opinions) {
				keys.add(opinion.getOpinionKey());
			}
			return em.createNamedQuery("OpinionBase.findOpinionsForKeys", OpinionBase.class).setParameter("keys", keys).getResultList();
		}

		// StatuteCitation
		public StatuteCitation statuteExists(StatuteCitation statuteCitation) {
			List<StatuteCitation> list = em.createNamedQuery("StatuteCitation.findByStatuteKey", StatuteCitation.class)
				.setParameter("statuteKey", statuteCitation.getStatuteKey())
				.getResultList();
			if ( list.size() > 0 ) return list.get(0);
			return null;
		}

		public List<StatuteCitation> getStatutes(Collection<StatuteCitation> statuteCitations) {
			if ( statuteCitations.size() == 0 ) return new ArrayList<StatuteCitation>();
			List<StatuteKey> keys = new ArrayList<>();
			for(StatuteCitation statuteCitation: statuteCitations) {
				keys.add(statuteCitation.getStatuteKey());
			}
			return em.createNamedQuery("StatuteCitationData.findStatutesForKeys", StatuteCitation.class).setParameter("keys", keys).getResultList();
		}

		public List<SlipOpinion> findByPublishDateRange() {
			List<SlipOpinion> opinions = em.createNamedQuery("SlipOpinion.loadOpinionsWithJoins", SlipOpinion.class).getResultList();

			List<SlipProperties> spl = em.createNamedQuery("SlipProperties.findAll", SlipProperties.class).getResultList();
			for ( SlipOpinion slipOpinion: opinions ) {
				slipOpinion.setSlipProperties(spl.get(spl.indexOf(new SlipProperties(slipOpinion))));
			}

			return opinions;
		}
		public List<Date> listPublishDates() {
			return em.createNamedQuery("SlipOpinion.listOpinionDates", Date.class).getResultList();
		}

		public SlipOpinion slipOpinionExists(OpinionKey opinionKey) {
			List<SlipOpinion> list = em.createQuery("select o from SlipOpinion o where o.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getResultList();
			if ( list.size() > 0 ) return list.get(0);
			return null;
		}
		public StatuteCitation findStatute(StatuteKey key) {
			return (StatuteCitation) em.createNamedQuery("StatuteCitation.findByCodeSection").setParameter("code", key.getTitle()).setParameter("sectionNumber", key.getSectionNumber()).getResultList().get(0);
		}
		public OpinionBase findOpinion(OpinionKey key) {
			return (OpinionBase) em.createNamedQuery("OpinionBase.findByOpinionKey").setParameter("key", key).getResultList().get(0);
		}
		public List<SlipOpinion> listSlipOpinions() {
			return em.createQuery("select from SlipOpinion", SlipOpinion.class).getResultList();
		}

		class MyPersistenceLookup implements PersistenceLookup {
			protected StatuteImportanceDataDump slipOpinionRepository;
			public MyPersistenceLookup(StatuteImportanceDataDump slipOpinionRepository) {
				this.slipOpinionRepository = slipOpinionRepository;
			}
			@Override
			public StatuteCitation statuteExists(StatuteCitation statuteCitation) {			
				return slipOpinionRepository.statuteExists(statuteCitation);
			}

			@Override
			public List<StatuteCitation> getStatutes(Collection<StatuteCitation> statuteCitations) {
				return slipOpinionRepository.getStatutes(statuteCitations);
			}

			@Override
			public OpinionBase opinionExists(OpinionBase opinionBase) {
				return slipOpinionRepository.opinionExists(opinionBase);
			}

			@Override
			public List<OpinionBase> getOpinions(Collection<OpinionBase> opinions) {
				return slipOpinionRepository.getOpinions(opinions);
			}	
		}
		
		public PersistenceLookup getPersistenceLookup() {
			return new MyPersistenceLookup(this); 
		}

		@Override
		public void close() throws Exception {
    		emf.close();
		}
}
