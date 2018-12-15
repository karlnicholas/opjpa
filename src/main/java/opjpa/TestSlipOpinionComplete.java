package opjpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import statutes.service.StatutesService;
import statutes.service.client.StatutesServiceClientImpl;

public class TestSlipOpinionComplete {

	Logger logger = Logger.getLogger(TestSlipOpinionComplete.class.getName());

	public static void main(String... args) throws Exception {
		new TestSlipOpinionComplete().run();
	}

	private void run() throws Exception {
		EntityManagerFactory emf = null;
		try  {
			emf = Persistence.createEntityManagerFactory("opjpa");
			EntityManager em = emf.createEntityManager();
			OpinionViewSingleton slipOpinionSingleton = new OpinionViewSingleton(em);
	//		slipOpinionData.buildCache();
			StatutesService statutesService;
			try {
				statutesService = new StatutesServiceClientImpl(new URL("http://localhost:8080/statutesrs/rs/"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
	
	        slipOpinionSingleton.checkStatus();
	        
			em.close();
		} finally {
			if ( emf != null )
				emf.close();
		}
	}
}
