package update;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKeyEntity;
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
					for( OpinionKey key: deleteOpinion.getOpinionCitations() ) {
						OpinionSummary opSummary = slipOpinionService.findOpinion(key);
						Set<OpinionKey> referringOpinions = opSummary.getReferringOpinions();
						if ( referringOpinions.remove(deleteOpinion.getOpinionKey()) ) {
							em.merge(opSummary);
						} else {
							logger.warning("deleteOpinion " + deleteOpinion.getOpinionKey() + " not found in " + key);							
						}
					}
					for ( StatuteKeyEntity key: deleteOpinion.getStatuteCitations() ) {
						StatuteCitation opStatute = slipOpinionService.findStatute(key);
						Map<OpinionKey, Integer> mapReferringOpinionCount = opStatute.getReferringOpinionCount();
						OpinionKey opKey = deleteOpinion.getOpinionKey();
						if ( !mapReferringOpinionCount.containsKey(opKey) ) throw new RuntimeException("Cannot delete referring opinion: " + opKey + " " + opStatute);
						mapReferringOpinionCount.remove(opKey);
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
