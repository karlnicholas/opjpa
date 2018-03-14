package update;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.service.SlipOpinionService;

public class DeleteAllSlipOpinions {
	private EntityManager em;
	private static final Logger logger = Logger.getLogger(DeleteAllSlipOpinions.class.getName());

	public static void main(String... args) {
		new DeleteAllSlipOpinions().run();
	}
	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
		SlipOpinionService slipOpinionService = new SlipOpinionService(em);
		try {
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				
				List<SlipOpinion> slipOpinions = slipOpinionService.listSlipOpinions();
				logger.info("There are " + slipOpinions.size() + " to be deleted");
				// should still be attached?
//				slipOpinionService.fetchCitations(slipOpinions);
				int size = slipOpinions.size();
				
				for (SlipOpinion deleteOpinion: slipOpinions) {
					if ( --size == 0 ) continue;	// leave one?
					// insure attached, or re-attach
					em.merge(deleteOpinion);
					for( OpinionBase opinionBase: deleteOpinion.getOpinionCitations() ) {
						OpinionBase opSummary = slipOpinionService.findOpinion(opinionBase);
						Set<OpinionBase> referringOpinions = opSummary.getReferringOpinions();
						if ( referringOpinions.remove(deleteOpinion) ) {
							em.merge(opSummary);
						} else {
							logger.warning("deleteOpinion " + deleteOpinion.getOpinionKey() + " not found in " + opinionBase);							
						}
					}
					for ( StatuteCitation statuteCitation: deleteOpinion.getOnlyStatuteCitations() ) {
						StatuteCitation opStatute = slipOpinionService.findStatute(statuteCitation);
						opStatute.removeOpinionStatuteReference(deleteOpinion);
						em.merge(opStatute);
					}
					em.remove(deleteOpinion);
				}
				
				logger.info("size = " + size);
				tx.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
				tx.rollback();
			}
			em.close();
		} finally {
			emf.close();
		}
		
	}

}
