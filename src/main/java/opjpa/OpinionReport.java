package opjpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionKey;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class OpinionReport {

	private EntityManagerFactory emf;
	private EntityManager em;

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
	        ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);

	
	//        OpinionQueries.getInstance().initializeDB(parserInterface);
	//        OpinionQueries.getInstance().writeToXML();
	//        OpinionQueries.getInstance().initFromXML();
	        
//	        OpinionSummary opinion = databaseFacade.findOpinion(new OpinionKey("211 Cal.App.4th 13"));
//        	printOpinionSummaryReport(parserInterface, parserResults, opinion );
	        
	        PrintOpinionReport opinionReport = new PrintOpinionReport();
	        
	        opinionReport.printSlipOpinionReport(parserInterface, em, new OpinionKey("1 Slip.Op 20523050"));
	        opinionReport.printSlipOpinionReport(parserInterface, em, new OpinionKey("1 Slip.Op 70099571"));
	        opinionReport.printSlipOpinionReport(parserInterface, em, new OpinionKey("1 Slip.Op 30160198"));	        
	        opinionReport.printSlipOpinionReport(parserInterface, em, new OpinionKey("1 Slip.Op 100423586"));	        
	        
	        
	//        for ( OpinionSummary op: persistenceFacade.getAllOpinions() ) {
	//            if (op.getStatutesReferredTo().size() > 10 ) System.out.println(op.getName() +":" + op.getStatutesReferredTo().size());
	//        }
    	} finally {
    		em.close();
    		emf.close();
    	}
    }
    
}
