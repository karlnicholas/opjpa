package opjpa;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import client.StatutesRsService;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionStatuteCitation;
import opca.model.SlipOpinion;
import opca.model.SlipProperties;
import opca.model.StatuteCitation;
import opca.parser.ParsedOpinionCitationSet;
import opca.service.SlipOpinionService;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import service.Client;

public class OpinionReport {

	private EntityManagerFactory emf;
	private EntityManager em;
    private PrintOpinionReport printOpinionReport = new PrintOpinionReport();
	private StatutesRsService service;

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
		SlipOpinionService slipOpinionService = new SlipOpinionService(em);
		SlipOpinion slipOpinion = em.createQuery("select so from SlipOpinion so where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult();
		slipOpinion.setOpinionCitations( em.createQuery("select so from SlipOpinion so left join fetch so.opinionCitations oc left join fetch oc.statuteCitations ocsc left join fetch ocsc.statuteCitation ocscsc left join fetch ocscsc.referringOpinions ocscscro left join fetch ocscscro.opinionBase ocscscroob where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult().getOpinionCitations() );
		slipOpinion.setStatuteCitations( em.createQuery("select so from SlipOpinion so left join fetch so.statuteCitations sc left join fetch sc.statuteCitation scsc left join fetch scsc.referringOpinions scscro left join fetch scscro.opinionBase scscroob where so.opinionKey = :key", SlipOpinion.class).setParameter("key", opinionKey).getSingleResult().getStatuteCitations() );
		slipOpinion.setSlipProperties( em.createNamedQuery("SlipProperties.findOne", SlipProperties.class).setParameter("opinion", slipOpinion).getSingleResult() );
//		SlipOpinion slipOpinion = slipOpinionService.slipOpinionExists(opinionKey);
/*		
		SlipOpinion slipOpinion = slipOpinionService.loadOpinion(opinionKey);
		slipOpinion.setSlipProperties( em.createNamedQuery("SlipProperties.findOne", SlipProperties.class).setParameter("opinion", slipOpinion).getSingleResult() );
		
		Map<StatuteCitation, Set<OpinionStatuteCitation>> osCitations = em.createNamedQuery("OpinionStatuteCitation.findByOpinion", OpinionStatuteCitation.class)
				.setParameter("opinion", slipOpinion)
				.getResultList()
				.stream()
				.collect( groupingBy(OpinionStatuteCitation::getStatuteCitation, toSet() ));
		for ( StatuteCitation statuteCitation: slipOpinion.getOnlyStatuteCitations() ) {
			statuteCitation.setReferringOpinions(osCitations.get(statuteCitation));
		}
*/		
		//        StatutesWS statutesWS = new StatutesWSService(new URL("http://localhost:9080/StatutesWS?wsdl")).getStatutesWSPort();
		service = new StatutesRsService(new URL("http://localhost:8080/statutesrs/rs/"));
		Client statutesRs = service.getRsService();
		
		if ( slipOpinion != null ) {
	    	ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion, slipOpinionService.getPersistenceLookup());

	    	OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder();
	        //TODO:FIX FOR STATUTESERVICE
	        OpinionView opinionView = opinionViewBuilder.buildSlipOpinionView(statutesRs, slipOpinion, parserResults);
	        opinionView.trimToLevelOfInterest(2, true);
	        opinionView.combineCommonSections();
	        
//	        Set<OpinionBase> opinionsCited = opinionView.getOpinionCitations();
	        // get statuteCitations for opinionsCited
//			em.createNamedQuery("StatuteCitation.findStatuteCitationsForOpinions", StatuteCitation.class).setParameter("opinions", opinionsCited).getResultList();
	        
	        
			opinionViewBuilder.scoreSlipOpinionOpinions(opinionView, parserResults);
			opinionViewBuilder.scoreSlipOpinionStatutes(opinionView, parserResults);

			printOpinionReport.printBaseOpinionReport(opinionView, parserResults);

// System.out.println("TIMING: " + (new Date().getTime()-startDate.getTime()));
	    	return;
		}
		throw new RuntimeException("SlipOpinion not found for key:" + opinionKey);
/*		
        OpinionSummary opinionSummary = slipOpinionService.opinionExists(opinionKey);
		if ( opinionSummary != null ) {
	    	ParsedOpinionResults parserResults = new ParsedOpinionResults(opinionSummary, slipOpinionService.getPersistenceLookup());
	        OpinionViewBuilder opinionCaseBuilder = new OpinionViewBuilder(parserInterface);
	        //
	        OpinionView opinionCase = opinionCaseBuilder.buildOpinionSummaryView(opinionSummary, parserResults, true);
	        opinionCase.trimToLevelOfInterest(2, true);

	    	printBaseOpinionReport(parserResults, opinionSummary, opinionCase);

// System.out.println("TIMING: " + (new Date().getTime()-startDate.getTime()));
	    	return;
		}
*/
    }
	
}
