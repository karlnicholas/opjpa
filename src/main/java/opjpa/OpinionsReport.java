package opjpa;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parser.ParsedOpinionResults;
import opca.service.SlipOpinionService;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class OpinionsReport {

	private EntityManagerFactory emf;
	private EntityManager em;

    public static void main(String... args) throws Exception {
        new OpinionsReport().run();
    }

	public OpinionsReport() {
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
			SlipOpinionService slipOpinionService = new SlipOpinionService();
			slipOpinionService.setEntityManager(em);
			class OpinionSummaryPrint {
				int countRefs;
				OpinionSummary opinionCited;
				public OpinionSummaryPrint(OpinionSummary opinionCited, int countRefs) {
	        		this.opinionCited = opinionCited;
	        		this.countRefs = countRefs;
				}
	        }
	        List<OpinionSummaryPrint> opinionsCited = new ArrayList<OpinionSummaryPrint>();

	        List<SlipOpinion> ops = em.createQuery("select op from SlipOpinion op", SlipOpinion.class ).getResultList();
	        
	        for ( SlipOpinion slipOpinion: ops ) {
		    	ParsedOpinionResults parserResults = new ParsedOpinionResults(slipOpinion, slipOpinionService.getPersistenceLookup());
	            for ( OpinionKey opinionKey: slipOpinion.getOpinionCitations()) {
	            	OpinionSummary opinionCited = parserResults.findOpinion(opinionKey);
	            	int countRefs = 0;
	            	for ( StatuteKey statuteKey: slipOpinion.getStatuteCitations() ) {
	            		StatuteCitation statuteCited = parserResults.findStatute(statuteKey);
	            		countRefs += statuteCited.getRefCount(opinionKey);
//	            		System.out.print(":" + statuteCite.getRefCount(opinionKey));
	            	}
	            	if ( countRefs > 0 || opinionCited.getCountReferringOpinions() > 10) {
	            		opinionsCited.add(new OpinionSummaryPrint(opinionCited, countRefs));
	            	}
	            }
	        }
	        System.out.println( opinionsCited.size() );
	        try ( BufferedWriter writer = Files.newBufferedWriter(Paths.get("opinionweights.txt"))) {
		        for ( OpinionSummaryPrint opPrint: opinionsCited) {
		        	writer.write(opPrint.opinionCited.getOpinionKey().toString() + ":" + opPrint.opinionCited.getCountReferringOpinions()+":"+opPrint.countRefs);
		        	writer.newLine();
		        }
		        writer.close();
	        }
//	        opinionReport.printSlipOpinionReport(parserInterface, em, new OpinionKey("1 Slip.Op 100423586"));	        	        
	        
	//        for ( OpinionSummary op: persistenceFacade.getAllOpinions() ) {
	//            if (op.getStatutesReferredTo().size() > 10 ) System.out.println(op.getName() +":" + op.getStatutesReferredTo().size());
	//        }
    	} finally {
    		em.close();
    		emf.close();
    	}
    }
    
}
