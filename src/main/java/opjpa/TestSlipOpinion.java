package opjpa;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import opca.mailer.EmailInformation;
import opca.model.User;
import opca.service.OpinionViewSingleton;
import opca.service.ViewParameters;
import opca.view.OpinionView;

public class TestSlipOpinion {

	Logger logger = Logger.getLogger(TestSlipOpinion.class.getName());

	public static void main(String... args) throws Exception {
		new TestSlipOpinion().run();
	}

	private void run() throws Exception {
		EntityManagerFactory emf = null;
		try  {
			emf = Persistence.createEntityManagerFactory("opjpa");
			EntityManager em = emf.createEntityManager();
			OpinionViewSingleton slipOpinionSingleton = new OpinionViewSingleton(em);
	//		slipOpinionData.buildCache();
	
	        Calendar calNow = Calendar.getInstance();
	        Calendar calLastWeek = Calendar.getInstance();
	        int year = calLastWeek.get(Calendar.YEAR);
	        int dayOfYear = calLastWeek.get(Calendar.DAY_OF_YEAR);
	        dayOfYear = dayOfYear - 7;
	        if ( dayOfYear < 1 ) {
	            year = year - 1;
	            dayOfYear = 365 + dayOfYear;
	        }
	        calLastWeek.set(Calendar.YEAR, year);
	        calLastWeek.set(Calendar.DAY_OF_YEAR, dayOfYear);
	
	        List<OpinionView> opinionCases = slipOpinionSingleton.getOpinionCases(
	        		new ViewParameters(calLastWeek.getTime(), calNow.getTime())
	    		);
	        User user = new User();
	        user.setEmail("test@test.com");
	        EmailInformation emailInformation = new EmailInformation(user, opinionCases);
			JAXBContext jc = JAXBContext.newInstance(EmailInformation.class);
		    Marshaller marshaller = jc.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		    marshaller.marshal(emailInformation, System.out);
	        
			em.close();
		} finally {
			if ( emf != null )
				emf.close();
		}
	}
}
