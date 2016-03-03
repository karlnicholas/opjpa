package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.internal.EntityManagerImpl;

import opca.service.OpinionViewCache;

public class TestSlipOpinion {

	public static void main(String... args) {
		new TestSlipOpinion().run();
	}

	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		EntityManagerImpl emImpl = (EntityManagerImpl)em;
		HibernateEntityManagerFactory factory = emImpl.getFactory();
		SessionFactory sessionFactory = factory.getSessionFactory();
		OpinionViewCache slipOpinionData = new OpinionViewCache();
		slipOpinionData.setSessionFactory(sessionFactory);
		slipOpinionData.buildCache();
		System.out.println(slipOpinionData.getAllOpinionCases().size());
		em.close();
		emf.close();
	}
}
