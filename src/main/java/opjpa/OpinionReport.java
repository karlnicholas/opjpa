package opjpa;

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionKey;
import opca.model.SlipOpinion;
import opca.model.SlipProperties;
import opca.parser.ParsedOpinionCitationSet;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import statutes.service.StatutesService;
import statutes.service.client.StatutesServiceClientImpl;

public class OpinionReport {

	private EntityManagerFactory emf;
	private EntityManager em;
    private PrintOpinionReport printOpinionReport = new PrintOpinionReport();

    public static void main(String... args) throws Exception {
        new OpinionReport().run();
    }

	public OpinionReport() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}

    private void run() throws Exception {
    	
    	try {
	
	//        String iface = "code.CACodes";
	//        ParserInterface parserInterface = (ParserInterface) Class.forName(iface).newInstance();
//	        ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);

	
	//        OpinionQueries.getInstance().initializeDB(parserInterface);
	//        OpinionQueries.getInstance().writeToXML();
	//        OpinionQueries.getInstance().initFromXML();
	        
//	        OpinionSummary opinion = databaseFacade.findOpinion(new OpinionKey("211 Cal.App.4th 13"));
//        	printOpinionSummaryReport(parserInterface, parserResults, opinion );

    		
//    		printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 20536622"));
//    		System.out.println( OpinionKey.printKey(281474986991962L) );
    		printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 10281306"));
//    		printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 60140282"));
//	        printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 30156316"));
	        
//	        opinionReport.printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 70099571"));
//	        opinionReport.printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 30160198"));	        
//	        opinionReport.printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 100423586"));	        
	        
	        
	//        for ( OpinionSummary op: persistenceFacade.getAllOpinions() ) {
	//            if (op.getStatutesReferredTo().size() > 10 ) System.out.println(op.getName() +":" + op.getStatutesReferredTo().size());
	//        }
    	} finally {
    		em.close();
    		emf.close();
    	}
    }
    
	public void printSlipOpinionReport(
    		EntityManager em, 
    		OpinionKey opinionKey
	) throws Exception {
// Date startDate = new Date();
        
		// select along specific joins so that the result stays hierarchically oriented.
		// in other words join -> join -> join -> join along the same path
		// don't try to mix joins
		SlipOpinion slipOpinion = em.createQuery("select so from SlipOpinion so where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult();
		slipOpinion.setOpinionCitations( em.createQuery("select so from SlipOpinion so left join fetch so.opinionCitations oc left join fetch oc.statuteCitations ocsc left join fetch ocsc.statuteCitation ocscsc left join fetch ocscsc.referringOpinions ocscscro left join fetch ocscscro.opinionBase ocscscroob where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult().getOpinionCitations() );
		slipOpinion.setStatuteCitations( em.createQuery("select so from SlipOpinion so left join fetch so.statuteCitations sc left join fetch sc.statuteCitation scsc left join fetch scsc.referringOpinions scscro left join fetch scscro.opinionBase scscroob where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult().getStatuteCitations() );
		slipOpinion.setSlipProperties( em.createQuery("select p from SlipProperties p where p.slipOpinion = :opinion", SlipProperties.class).setParameter("opinion", slipOpinion).getSingleResult() );

		StatutesService statutesRs = new StatutesServiceClientImpl(new URL("http://localhost:8080/statutesrs/rs/"));
		
    	ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion);

    	OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder(statutesRs);
        //TODO:FIX FOR STATUTESERVICE
        OpinionView opinionView = opinionViewBuilder.buildOpinionView(slipOpinion, parserResults);

		printOpinionReport.printBaseOpinionReport(opinionView, parserResults);

// System.out.println("TIMING: " + (new Date().getTime()-startDate.getTime()));
    	return;
    }
	
}
