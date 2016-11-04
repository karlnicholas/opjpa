package opjpa;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.service.OpinionViewCache;

public class TestSlipOpinion {

	Logger logger = Logger.getLogger(TestSlipOpinion.class.getName());

	public static void main(String... args) {
		new TestSlipOpinion().run();
	}

	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		OpinionViewCache slipOpinionData = new OpinionViewCache(em, logger);
		slipOpinionData.buildCache();
		System.out.println(slipOpinionData.getAllOpinionCases().size());
		em.close();
		emf.close();
	}
}
