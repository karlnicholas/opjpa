package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.internal.EntityManagerImpl;

import opca.service.OpinionViewCache;

public class TestSlipOpinionData {

	public static void main(String... args) {
		new TestSlipOpinionData().run();
	}

	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		EntityManagerImpl emImpl = (EntityManagerImpl)em;
		HibernateEntityManagerFactory factory = emImpl.getFactory();
		SessionFactory sessionFactory = factory.getSessionFactory();
		StatelessSession statelessSession = sessionFactory.openStatelessSession();
		try {
/*			
			List<Date> dates = statelessSession.getNamedQuery("SlipOpinion.listOpinionDates").list();
			Date endDate = dates.get(0);
			Date startDate = dates.get(dates.size()-1);

			List<SlipOpinion> opinions = statelessSession.getNamedQuery("OpinionViewCache.findByOpinionDateRange").setParameter("startDate", startDate).setParameter("endDate", endDate).list();
			Query fetchStatuteCitations = statelessSession.getNamedQuery("OpinionViewCache.fetchStatuteCitations");
			Query fetchOpinionCitations = statelessSession.getNamedQuery("OpinionViewCache.fetchOpinionCitations");		
			for ( SlipOpinion op: opinions ) {
				op.setStatuteCitations(fetchStatuteCitations.setLong("id", op.getId()).list());
				op.setOpinionCitations(fetchOpinionCitations.setLong("id", op.getId()).list());
			}
			System.out.println(opinions.size());
			SlipOpinion op = opinions.get(0);
					
			List<StatuteCitation> statuteCitations = statelessSession.getNamedQuery("StatuteCitationData.findStatutesForKeys").setParameterList("keys", op.getStatuteCitationKeys()).list();
*/
			OpinionViewCache slipOpinionData = new OpinionViewCache();
			slipOpinionData.setSessionFactory(sessionFactory);
			slipOpinionData.buildCache();
			
		} finally {
			statelessSession.close();
			em.close();
			emf.close();
		}
	}
}
