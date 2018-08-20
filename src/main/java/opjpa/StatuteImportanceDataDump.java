package opjpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.SlipOpinion;
import opca.model.SlipProperties;
import opca.parser.ParsedOpinionCitationSet;
import opca.view.CaseView;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import opca.view.SectionView;
import statutes.service.StatutesService;
import statutes.service.client.StatutesServiceClientImpl;

public class StatuteImportanceDataDump implements AutoCloseable {
	private EntityManagerFactory emf;
	private EntityManager em;

    public static void main(String... args) throws Exception {
    	try ( StatuteImportanceDataDump statuteImportance = new StatuteImportanceDataDump() ) {
    		statuteImportance.run();
    	}
    }

	public StatuteImportanceDataDump() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	private void run() throws Exception {
    	for( OpinionView opinionView: getOpinionCases() ) {
    		System.out.println("\n=============================");
    		for( SectionView sectionView: opinionView.getSectionViews() ) {
//        		System.out.println("\t"+statuteView.getImportance()+":"+statuteView.getDisplaySections()+":"+statuteView.getDisplayTitlePath());
        		System.out.println("\t"+sectionView.getImportance()+":"+sectionView.getDisplaySections()+":"+sectionView.getDisplayTitlePath());
//        		System.out.println("\t"+sectionView.getImportance()+":"+sectionView.getTitle());
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
	}
	
	public List<OpinionView> getOpinionCases() {
			List<OpinionView> opinionViews = new ArrayList<OpinionView>();
			StatutesService statutesRs = null;
			try {
				statutesRs = new StatutesServiceClientImpl(new URL("http://localhost:8080/statutesrs/rs/"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			//
			OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder(statutesRs);
			List<SlipOpinion> opinions = findByPublishDateRange();
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
				ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion);
				OpinionView opinionView = opinionViewBuilder.buildOpinionView(slipOpinion, parserResults);				
				opinionViews.add(opinionView);
			}
			return opinionViews;	
		}

		public List<SlipOpinion> findByPublishDateRange() {
			List<SlipOpinion> opinions = em.createNamedQuery("SlipOpinion.loadOpinionsWithJoins", SlipOpinion.class).getResultList();

			List<SlipProperties> spl = em.createNamedQuery("SlipProperties.findAll", SlipProperties.class).getResultList();
			for ( SlipOpinion slipOpinion: opinions ) {
				slipOpinion.setSlipProperties(spl.get(spl.indexOf(new SlipProperties(slipOpinion))));
			}

			return opinions;
		}
		public SlipOpinion slipOpinionExists(OpinionKey opinionKey) {
			List<SlipOpinion> list = em.createQuery("select o from SlipOpinion o where o.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getResultList();
			if ( list.size() > 0 ) return list.get(0);
			return null;
		}
		public List<SlipOpinion> listSlipOpinions() {
			return em.createQuery("select from SlipOpinion", SlipOpinion.class).getResultList();
		}

		@Override
		public void close() throws Exception {
    		emf.close();
		}
}
