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
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import opca.view.StatuteCaseScore;
import opca.view.StatuteCaseScoreList;
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
	        
	        printSlipOpinionReport(em, new OpinionKey("1 Slip.Op 30156316"));
	        
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
	        
			List<StatuteCaseScoreList> statuteCaseScores = new ArrayList<StatuteCaseScoreList>();
			// make a sortd list of statuteKey's that opinionView refers to
			List<StatuteKey> opinionStatuteKeys = new ArrayList<StatuteKey>(opinionView.getStatuteCitations());
			// need a collection StatutueCitations.
			
			TypedQuery<StatuteCitation> query = em.createNamedQuery("StatuteCitationData.findStatutesForKeys", StatuteCitation.class);
			List<StatuteCitation> statuteCitations = query.setParameter("keys", opinionStatuteKeys).getResultList();
			Collections.sort(statuteCitations);
			
			Comparator comparator = new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					StatuteCitation statuteCitation = (StatuteCitation)o1;
					StatuteKey statuteKey = (StatuteKey)o2;
					return statuteCitation.getStatuteKey().compareTo(statuteKey);
				}
			};

	        for ( OpinionKey citedOpinionKey: slipOpinion.getOpinionCitations()) {
	        	OpinionSummary opinionCited = parserResults.findOpinion(citedOpinionKey);
	        	for ( StatuteKey statuteKey: opinionCited.getStatuteCitations() ) {
	        		int foundPosition = Collections.binarySearch(statuteCitations, statuteKey, comparator);
	        		if ( foundPosition >= 0 ) {
	        			// search scoreMatrix for opinionStatuteCitation
	        			StatuteCaseScoreList statuteCaseScoreList = null;
	        			Iterator<StatuteCaseScoreList> scsIt = statuteCaseScores.iterator(); 
	        			while( scsIt.hasNext() ) {
	        				statuteCaseScoreList = scsIt.next(); 
	        				if (statuteCaseScoreList.getSlipOpinionStatute().equals(statuteKey)) {
	        					for (StatuteCaseScore statuteCaseScore: statuteCaseScoreList.getStatuteCaseScoreList()) {
	        						if ( statuteCaseScore.getOpinionKey().equals(citedOpinionKey)) {
	        							// problems?
	        							throw new RuntimeException("Dont think so??");
	        						}
	        					}
	        					break;
	        				}
	        				statuteCaseScoreList = null;
	        			}
						StatuteCitation statuteCitation = statuteCitations.get(foundPosition);
	        			if ( statuteCaseScoreList == null ) {
	        				statuteCaseScoreList = new StatuteCaseScoreList();
	        				statuteCaseScoreList.setSlipOpinionStatute(statuteKey);
	        				statuteCaseScoreList.setSlipOpinionReferCount(statuteCitation.getRefCount(opinionKey));
	        				statuteCaseScores.add(statuteCaseScoreList);
	        			}
						//TODO will all entries be unqiue?
						StatuteCaseScore statuteCaseScore = new StatuteCaseScore();
						statuteCaseScore.setOpinionKey(citedOpinionKey);
						statuteCaseScore.setOpinionReferCount(statuteCitation.getRefCount(citedOpinionKey));
						statuteCaseScoreList.getStatuteCaseScoreList().add(statuteCaseScore);
	        		}
	        	}
	        }
			
	        printOpinionReport.printBaseOpinionReport(parserResults, opinionView);

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
