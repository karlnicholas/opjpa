package opjpa;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class TestOpinionInitializer {
	

	public static void main(String... args) {
	    Weld weld = new Weld();
	    WeldContainer container = weld.initialize();
	    TestOpinionInitializer application = container.instance().select(TestOpinionInitializer.class).get();
	    application.run();
	    weld.shutdown();
	}
	
//	@PersistenceUnit(unitName="opjpa")
//	private EntityManagerFactory emf;
//	private EntityManager em;
//	@Produces
//	@RequestScoped
//	private UserTransaction ut;
	
	private void run() {
//		opinionQueries.listPublishDates();
	}
    

	@Produces
	@RequestScoped
	public EntityManager createEntityManager() {
		EntityManager em = Persistence.createEntityManagerFactory("opjpa").createEntityManager();
		return em;
	}

	public void closeEM(@Disposes EntityManager em) {
		em.close();
	}
	
}
