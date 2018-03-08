package opjpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import opca.model.OpinionBase;
import opca.model.OpinionStatuteCitation;
import opca.model.StatuteCitation;
import opca.service.SlipOpinionService;
import parser.ParserInterface;
import statutes.SectionNumber;
import statutes.StatutesBaseClass;
import statutesca.factory.CAStatutesFactory;

public class StatuteReport {

	private EntityManagerFactory emf;
	private EntityManager em;

	public static void main(String[] args) throws Exception {
		new StatuteReport().run();
	}

	public StatuteReport() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}

	private void run() throws Exception {
		
		try {
		
			SlipOpinionService slipOpinionService = new SlipOpinionService(em);
	//      String iface = "code.CACodes";
	//      ParserInterface parserInterface = (ParserInterface) Class.forName(iface).newInstance();
		    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
	
	//        databaseFacade.initializeDB(parserInterface);
	//        OpinionQueries.getInstance().writeToXML();
	//        OpinionQueries.getInstance().initFromXML();
	
	        System.out.println("statuteTable size = " + slipOpinionService.getCount());
	        
			List<StatuteCitation> statutesForCode = slipOpinionService.selectForTitle("welfare");
	        StatuteCitation maxWelfare = getCodeCitationMaxCaseReferrors(statutesForCode );
	        printCodeCitation(parserInterface, slipOpinionService, maxWelfare);
	        
	        printCodeCitation(parserInterface, slipOpinionService, slipOpinionService.testStatuteByTitleSection("welfare", "200"));
	
	        printCodeCitation(parserInterface, slipOpinionService, slipOpinionService.testStatuteByTitleSection("family code", "4058"));
	
	        printCodeCitation(parserInterface, slipOpinionService, slipOpinionService.testStatuteByTitleSection("family code", "300"));
		} finally {
			em.close();
			emf.close();
		}
	}
	
	private StatuteCitation getCodeCitationMaxCaseReferrors(
		List<StatuteCitation> statutesForCode
	) {
        if ( statutesForCode.size() == 0 ) return null;
        Collections.sort(statutesForCode, new Comparator<StatuteCitation>() {
            @Override
            public int compare(StatuteCitation o1, StatuteCitation o2) {
                return o1.getReferringOpinions().size() - o2.getReferringOpinions().size();
            }
        });
        return statutesForCode.get(statutesForCode.size()-1);
	}
	
	private void printCodeCitation(
	    ParserInterface parserInterface,
	    SlipOpinionService slipOpinionService, 
	    StatuteCitation statuteCitation
	) {
	    if ( statuteCitation == null ) {
	        return; 
	    }
        StatutesBaseClass reference = parserInterface.findReference(statuteCitation.getStatuteKey().getTitle(), new SectionNumber(-1, statuteCitation.getStatuteKey().getSectionNumber()));
        if ( reference == null ) return;
        System.out.println("Total refereeCount = " + statuteCitation.getReferringOpinions().size());
        boolean first = true;
        String indent = new String();
        int printed = 0;
        Collection<OpinionStatuteCitation> referredOpinions = statuteCitation.getReferringOpinions();
        List<OpinionBase> opinions = new ArrayList<>(referredOpinions.size());
        for ( OpinionStatuteCitation statuteOpinionCitation: referredOpinions ) {
        	opinions.add( statuteOpinionCitation.getOpinionBase() );
        }
        List<OpinionBase> foundOpinions = slipOpinionService.getOpinions(opinions);
        List<OpinionBase> referringOpinions = new ArrayList<>();
        
//        for ( OpinionKey caseCitationKey: statuteCitation.getReferringOpinionCount().keySet() ) {
//        	if ( caseCitationKey.isSlipOpinion() ) continue;
//            OpinionSummary opinionSummary = databaseFacade.findOpinion(caseCitationKey);
      for ( OpinionBase opinionSummary: foundOpinions ) {
            // don't print anything with less than 3 referees
            if ( opinionSummary.getCountReferringOpinions() < 3 ) continue;
            if ( first ) {
                String[] titles = reference.getFullTitle(":").split("[:]");
                for ( String title: titles ) {
                    System.out.println(indent + title);
                    indent = indent + "  ";
                }
                System.out.println(indent+"§"+statuteCitation.getStatuteKey().getSectionNumber());
                indent = indent + "  ";
                first = false;
            }
            referringOpinions.add(opinionSummary);
        }
        Collections.sort(referringOpinions, new Comparator<OpinionBase>() {
            @Override
            public int compare(OpinionBase o1, OpinionBase o2) {
            	long v1 = (o1.getCountReferringOpinions() * o1.getCountReferringOpinions()) * statuteCitation.getOpinionStatuteReference(o1).getCountReferences();
            	long v2 = (o2.getCountReferringOpinions() * o1.getCountReferringOpinions()) * statuteCitation.getOpinionStatuteReference(o2).getCountReferences();
            	return (int)(v1 - v2);
/*            	
                if ( o1.getCountReferringOpinions() == o2.getCountReferringOpinions() ) {
                    int o1TimesCaseReferredTo = statuteCitation.getRefCount(o1.getOpinionKey());
                    int o2TimesCaseReferredTo = statuteCitation.getRefCount(o2.getOpinionKey());
                    return o1TimesCaseReferredTo - o2TimesCaseReferredTo; 
                }
                return o1.getCountReferringOpinions() - o2.getCountReferringOpinions();
*/                
            }
        });
        for (OpinionBase opinionSummary: referringOpinions ) {
            int timesCaseReferredTo = statuteCitation.getOpinionStatuteReference(opinionSummary).getCountReferences();
            System.out.println( indent + opinionSummary.getOpinionKey().toString()+":"+timesCaseReferredTo+":"+opinionSummary.getCountReferringOpinions()+":"+timesCaseReferredTo*opinionSummary.getCountReferringOpinions());
            printed++;
        }
        System.out.println("Printed refereeCount = " + printed);
	}
        
}
