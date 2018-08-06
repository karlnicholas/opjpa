package update;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import opca.model.StatuteCitation;

public class DeleteAllSlipOpinions {
	private EntityManager em;
//	private static final Logger logger = Logger.getLogger(DeleteAllSlipOpinions.class.getName());

	public static void main(String... args) {
		new DeleteAllSlipOpinions().run();
	}
	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
		try {
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
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

	public StatuteCitation findStatute(StatuteCitation statuteCitation) {
		return em.createNamedQuery("StatuteCitation.findByStatuteKey", StatuteCitation.class).setParameter("statuteKey", statuteCitation.getStatuteKey()).getSingleResult();
	}

}
