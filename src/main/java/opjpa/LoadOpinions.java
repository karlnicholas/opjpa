package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import load.LoadHistoricalOpinions;
import statutes.api.IStatutesApi;
import statutesca.statutesapi.CAStatutesApiImpl;

public class LoadOpinions {

	private EntityManagerFactory emf;

	public static void main(String[] args) throws Exception {
		new LoadOpinions().run();
	}
	
	public LoadOpinions() {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	
	private void run() throws Exception {
	    IStatutesApi iStatutesApi = new CAStatutesApiImpl();
	    
        try {
    		EntityManager em = emf.createEntityManager();
    	    EntityTransaction tx = em.getTransaction();
    	    tx.begin();
        	LoadHistoricalOpinions load = new LoadHistoricalOpinions(em, iStatutesApi);
        	load.initializeDB();
        	tx.commit();
        	em.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
        	emf.close();
        }
	}
        
}
