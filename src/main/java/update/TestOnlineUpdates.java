package update;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.internal.EntityManagerImpl;

import opca.parser.OpinionScraperInterface;
import opca.scraper.CACaseScraper;
import opca.service.CAOnlineUpdateService;
import opca.service.OpinionViewCache;
import scraper.TestCACaseScraper;

public class TestOnlineUpdates {
	private EntityManager em;
//	private OpinionViewCache slipOpinionData;

	public static void main(String... args) {
		new TestOnlineUpdates().run();
	}
	
	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
		try {
//			OpinionScraperInterface caseScraper = new CACaseScraper(true);
			OpinionScraperInterface caseScraper = new TestCACaseScraper(false);
//			EntityTransaction tx = em.getTransaction();
			try {
//				tx.begin();
				new CAOnlineUpdateService(em).updateDatabase(caseScraper);
			} catch (Exception ex) {
				ex.printStackTrace();
//				tx.rollback();
			}
//			tx.commit();
			em.close();
		} finally {
			emf.close();
		}
	}
}
