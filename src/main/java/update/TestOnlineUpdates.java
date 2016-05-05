package update;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import opca.parser.CaseScraperInterface;
import opca.scraper.CACaseScraper;
import opca.service.CAScraperService;
import opjpa.TestCACaseScraper;

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
//			CaseScraperInterface caseScraper = new CACaseScraper(false);
			CaseScraperInterface caseScraper = new TestCACaseScraper(false);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			try {
				new CAScraperService(em).updateDatabase(caseScraper);
			} catch (Exception ex) {
				ex.printStackTrace();
				tx.rollback();
			}
			tx.commit();
			em.close();
		} finally {
			emf.close();
		}
	}
}
