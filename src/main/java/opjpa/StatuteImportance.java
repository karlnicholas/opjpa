package opjpa;

import java.io.InputStream;
import java.io.StringWriter;
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
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import opca.mailer.EmailInformation;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.SlipOpinion;
import opca.model.SlipProperties;
import opca.model.User;
import opca.parser.ParsedOpinionCitationSet;
import opca.service.OpinionViewSingleton;
import opca.service.ViewParameters;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import statutes.service.StatutesService;
import statutes.service.client.StatutesServiceClientImpl;

public class StatuteImportance implements AutoCloseable {
	Logger logger = Logger.getLogger(StatuteImportance.class.getName());
	private EntityManagerFactory emf;
	private EntityManager em;

    public static void main(String... args) throws Exception {
    	try ( StatuteImportance statuteImportance = new StatuteImportance() ) {
    		statuteImportance.run();
    	}
    }

	public StatuteImportance() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	private void run() throws Exception {
		OpinionViewSingleton slipOpinionSingleton = new OpinionViewSingleton(em);

		User user = new User();
		user.setEmail("test@test.com");
		user.setFirstName("First");
		user.setLastName("Lastname");
		user.setOptoutKey("optoutkey");
		TransformerFactory tf = TransformerFactory.newInstance();
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
		EmailInformation emailInformation = new EmailInformation(user, opinionCases);
		
		JAXBContext jc = JAXBContext.newInstance(EmailInformation.class);
		JAXBSource source = new JAXBSource(jc, emailInformation);
		// set up XSLT transformation
		InputStream is = getClass().getResourceAsStream("/xsl/opinionreport.xsl");
		StreamSource streamSource = new StreamSource(is);
		StringWriter htmlContent = null;
		try {
			htmlContent = new StringWriter();
			synchronized(this) {
				Transformer t = tf.newTransformer(streamSource);
				// run transformation
				t.transform(source, new StreamResult(htmlContent));
			}
		} catch (TransformerException e) {
			throw new RuntimeException(e); 
		} finally {
			System.out.println("htmlContent: " + htmlContent);
			htmlContent.close();
		}
	}
	
	public List<OpinionView> getOpinionCases() {
			List<OpinionView> opinionViews = new ArrayList<OpinionView>();
			
			StatutesService statutesRs = null;
			try {
				statutesRs = new StatutesServiceClientImpl(new URL("http://localhost:8080/statutesrs/rs/"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			//
			OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder(statutesRs);
			List<SlipOpinion> opinions = findByPublishDateRange();
			List<OpinionBase> opinionOpinionCitations = new ArrayList<>();
			List<Integer> opinionIds = new ArrayList<>();
			int i = 0;
			for ( SlipOpinion slipOpinion: opinions ) {
				opinionIds.add(slipOpinion.getId());
				if ( ++i % 100 == 0 ) {
					opinionOpinionCitations.addAll( 
						em.createNamedQuery("OpinionBase.fetchOpinionCitationsForOpinions", OpinionBase.class).setParameter("opinionIds", opinionIds).getResultList()
					);
					opinionIds.clear();
				}
			}
			opinionOpinionCitations.addAll( 
				em.createNamedQuery("OpinionBase.fetchOpinionCitationsForOpinions", OpinionBase.class).setParameter("opinionIds", opinionIds).getResultList()
			);
			for ( SlipOpinion slipOpinion: opinions ) {
//				slipOpinion.setOpinionCitations( fetchOpinions.setParameter("id", slipOpinion.getId()).getSingleResult().getOpinionCitations() );
				slipOpinion.setOpinionCitations( opinionOpinionCitations.get( opinionOpinionCitations.indexOf(slipOpinion)).getOpinionCitations() );
				ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion);
				OpinionView opinionView = opinionViewBuilder.buildOpinionView(slipOpinion, parserResults);				
				opinionViews.add(opinionView);
			}
			return opinionViews;	
		}

		public List<SlipOpinion> findByPublishDateRange() {
			List<SlipOpinion> opinions = em.createNamedQuery("SlipOpinion.loadOpinionsWithJoins", SlipOpinion.class).getResultList();

			List<SlipProperties> spl = em.createNamedQuery("SlipProperties.findAll", SlipProperties.class).getResultList();
			for ( SlipOpinion slipOpinion: opinions ) {
				slipOpinion.setSlipProperties(spl.get(spl.indexOf(new SlipProperties(slipOpinion))));
			}

			return opinions;
		}
		public SlipOpinion slipOpinionExists(OpinionKey opinionKey) {
			List<SlipOpinion> list = em.createQuery("select o from SlipOpinion o where o.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getResultList();
			if ( list.size() > 0 ) return list.get(0);
			return null;
		}
		public List<SlipOpinion> listSlipOpinions() {
			return em.createQuery("select from SlipOpinion", SlipOpinion.class).getResultList();
		}

		@Override
		public void close() throws Exception {
    		emf.close();
		}
}
