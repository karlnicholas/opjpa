package opjpa;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
import opinions.model.OpinionKey;
import opinions.facade.DatabaseFacade;

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
        
    		DatabaseFacade databaseFacade = new DatabaseFacade(em);
	
	//        String iface = "code.CACodes";
	//        CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
	        CodesInterface codesInterface = new CACodes();
	        codesInterface.loadXMLCodes(new File(StatuteReport.class.getResource("/xmlcodes").getFile()));
	
	//        DatabaseFacade.getInstance().initializeDB(codesInterface);
	//        DatabaseFacade.getInstance().writeToXML();
	//        DatabaseFacade.getInstance().initFromXML();
	        
//	        OpinionSummary opinion = databaseFacade.findOpinion(new OpinionKey("5 Cal.4th 295"));
//        	printOpinionSummaryReport(codesInterface, parserResults, opinion );
	        
	        PrintOpinionReport opinionReport = new PrintOpinionReport();
	        
	        opinionReport.printOpinionReport(codesInterface, em, new OpinionKey("1 Slip.Op 10287300"));
	        
	//        for ( OpinionSummary op: persistenceFacade.getAllOpinions() ) {
	//            if (op.getStatutesReferredTo().size() > 10 ) System.out.println(op.getName() +":" + op.getStatutesReferredTo().size());
	//        }
    	} finally {
    		em.close();
    		emf.close();
    	}
    }
    
}
