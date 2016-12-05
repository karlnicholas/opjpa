package update;

import java.io.IOException;
import java.nio.file.Paths;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.google.common.io.Files;

import opca.parser.OpinionScraperInterface;
import opca.scraper.CACaseScraper;
import opca.service.CAOnlineUpdates;
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
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				new CAOnlineUpdates(em).updateDatabase(caseScraper);
				tx.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
				tx.rollback();
			}
			em.close();
		} finally {
//			emf.close();
		}
		try {
			Files.copy(Paths.get("c:/users/karln/opca/opjpa/html/60days2.html").toFile(), Paths.get("c:/users/karln/opca/opjpa/html/60days.html").toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		em = emf.createEntityManager();
		try {
//			OpinionScraperInterface caseScraper = new CACaseScraper(true);
			OpinionScraperInterface caseScraper = new TestCACaseScraper(false);
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				new CAOnlineUpdates(em).updateDatabase(caseScraper);
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
