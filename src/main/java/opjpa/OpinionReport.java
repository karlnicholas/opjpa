package opjpa;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parser.ParsedOpinionCitationSet;
import opca.service.SlipOpinionService;
import opca.view.OpinionScoreList;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import opca.view.StatuteScore;
import opca.view.StatuteScoreList;
import statutesws.StatutesWS;
import statutesws.StatutesWSService;

public class OpinionReport {

	private EntityManagerFactory emf;
	private EntityManager em;
    PrintOpinionReport printOpinionReport = new PrintOpinionReport();
    

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
    			        
//    		printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 20541592"));
    		printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 60140282"));
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
        
		SlipOpinionService slipOpinionService = new SlipOpinionService();
		slipOpinionService.setEntityManager(em);
		SlipOpinion slipOpinion = slipOpinionService.slipOpinionExists(opinionKey);
        StatutesWS statutesWS = new StatutesWSService(new URL("http://localhost:9080/StatutesWS?wsdl")).getStatutesWSPort();
		
		if ( slipOpinion != null ) {
	    	ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion, slipOpinionService.getPersistenceLookup());

	    	OpinionViewBuilder opinionViewBuilder = new OpinionViewBuilder();
	        //TODO:FIX FOR STATUTESERVICE
	        OpinionView opinionView = opinionViewBuilder.buildSlipOpinionView(statutesWS, slipOpinion, parserResults);
	        opinionView.trimToLevelOfInterest(2, true);
	        opinionView.combineCommonSections();
	        
//	    	List<StatuteCaseScoreList> slipOpinionStatutesScore = scoreSlipOpinionStatutes(opinionView, parserResults);
			List<StatuteKey> opinionStatuteKeys = new ArrayList<StatuteKey>(opinionView.getStatuteCitations());
			TypedQuery<StatuteCitation> query = em.createNamedQuery("StatuteCitationData.findStatutesForKeys", StatuteCitation.class);
			List<StatuteCitation> statuteCitations = query.setParameter("keys", opinionStatuteKeys).getResultList();
			Collections.sort(statuteCitations);
	        
			opinionViewBuilder.scoreSlipOpinionOpinions(opinionView, parserResults, statuteCitations);

			opinionViewBuilder.scoreSlipOpinionStatutes(opinionView, parserResults, statuteCitations);

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
