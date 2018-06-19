package opjpa;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionBase;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.parser.ParsedOpinionCitationSet;

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

	
	//        OpinionQueries.getInstance().initializeDB(parserInterface);
	//        OpinionQueries.getInstance().writeToXML();
	//        OpinionQueries.getInstance().initFromXML();
	        
//	        OpinionSummary opinion = databaseFacade.findOpinion(new OpinionKey("211 Cal.App.4th 13"));
//        	printOpinionSummaryReport(parserInterface, parserResults, opinion );
	        
			class OpinionSummaryPrint {
				int countRefs;
				OpinionBase opinionCited;
				public OpinionSummaryPrint(OpinionBase opinionCited, int countRefs) {
	        		this.opinionCited = opinionCited;
	        		this.countRefs = countRefs;
				}
	        }
	        List<OpinionSummaryPrint> opinionsCited = new ArrayList<OpinionSummaryPrint>();

	        List<SlipOpinion> ops = em.createQuery("select op from SlipOpinion op", SlipOpinion.class ).getResultList();
	        
	        for ( SlipOpinion slipOpinion: ops ) {
		    	ParsedOpinionCitationSet parserResults = new ParsedOpinionCitationSet(slipOpinion);
	            for ( OpinionBase opinionBase: slipOpinion.getOpinionCitations()) {
	            	OpinionBase opinionCited = parserResults.findOpinion(opinionBase.getOpinionKey());
	            	int countRefs = 0;
	            	for ( StatuteCitation statuteCitation: slipOpinion.getOnlyStatuteCitations() ) {
	            		StatuteCitation statuteCited = parserResults.findStatute(statuteCitation);
	            		countRefs += statuteCited.getOpinionStatuteReference(opinionBase).getCountReferences();
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
