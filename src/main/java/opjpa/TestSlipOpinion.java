package opjpa;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opinion.model.SlipOpinion;

public class TestSlipOpinion {

	public static void main(String... args) {
		new TestSlipOpinion().run();
	}
	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		Calendar startDate = Calendar.getInstance();
		startDate.set(2015, 7, 1, 0, 0, 0);
		Calendar endDate = Calendar.getInstance();
		endDate.set(2016, 2, 1, 0, 0, 0);
		List<SlipOpinion>  list = 
				em.createNamedQuery("SlipOpinion.findByOpinionDateRange", SlipOpinion.class).setParameter("startDate", startDate.getTime()).setParameter("endDate", endDate.getTime()).getResultList();
		System.out.println(list.size());
		for( SlipOpinion op: list ) {
//			System.out.println(op);
//			System.out.println("    " + op.getStatuteCitationKeys());
		}
		emf.close();
	}
}
