package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import load.LoadHistoricalOpinions;
import opca.model.OpinionBase;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class TestJPA {

	private EntityManagerFactory emf;

	public static void main(String[] args) throws Exception {
		new TestJPA().run();
	}
	
	public TestJPA() {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	
	private void run() throws Exception {
        try {
    		EntityManager em = emf.createEntityManager();
    		em.createNamedQuery("OpinionBase.findByOpinionKey", OpinionBase.class);
        	em.close();
        } finally {
        	emf.close();
        }
	}
        
}
