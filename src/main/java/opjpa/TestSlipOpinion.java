package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.service.OpinionViewCache;

public class TestSlipOpinion {

	public static void main(String... args) {
		new TestSlipOpinion().run();
	}

	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		OpinionViewCache slipOpinionData = new OpinionViewCache();
		slipOpinionData.setEntityManager(em);
		slipOpinionData.buildCache();
		System.out.println(slipOpinionData.getAllOpinionCases().size());
		em.close();
		emf.close();
	}
}
