package update;

import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import opca.parser.OpinionScraperInterface;
import opca.service.CAOnlineUpdates;
import scraper.TestCACaseScraper;
import statutes.service.StatutesService;
import statutes.service.client.StatutesServiceClientImpl;

public class TestOnlineUpdates {
	private EntityManager em;
//	private OpinionViewCache slipOpinionData;

	public static void main(String... args) {
		new TestOnlineUpdates().run();
	}
	
	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
		StatutesService statutesService;
		try {
			statutesService = new StatutesServiceClientImpl(new URL("http://localhost:8080/statutesrs/rs/"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		try {
//			OpinionScraperInterface caseScraper = new CACaseScraper(true);
			OpinionScraperInterface caseScraper = new TestCACaseScraper(false);
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				new CAOnlineUpdates(em).updateDatabase(caseScraper, statutesService);
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
