package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import load.LoadHistoricalOpinions;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class LoadOpinions {

	private EntityManagerFactory emf;

	public static void main(String[] args) throws Exception {
		new LoadOpinions().run();
	}
	
	public LoadOpinions() {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	
	private void run() throws Exception {
	
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
        try {
    		EntityManager em = emf.createEntityManager();
    	    EntityTransaction tx = em.getTransaction();
    	    tx.begin();
        	LoadHistoricalOpinions load = new LoadHistoricalOpinions(em, parserInterface);
        	load.initializeDB();
        	tx.commit();
        	em.close();
        } finally {
        	emf.close();
        }
	}
        
}
