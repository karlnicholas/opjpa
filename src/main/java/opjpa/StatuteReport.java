package opjpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import codesparser.CodeReference;
import codesparser.CodesInterface;
import codesparser.SectionNumber;
import gscalifornia.factory.CAStatutesFactory;
import opinion.data.SlipOpinionRepository;
import opinion.model.OpinionSummary;
import opinion.model.StatuteCitation;

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
		
			SlipOpinionRepository slipOpinionRepository = new SlipOpinionRepository();
			slipOpinionRepository.setEntityManager(em);
	//      String iface = "code.CACodes";
	//      CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
		    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
	
	//        databaseFacade.initializeDB(codesInterface);
	//        OpinionQueries.getInstance().writeToXML();
	//        OpinionQueries.getInstance().initFromXML();
	
	        System.out.println("statuteTable size = " + slipOpinionRepository.getCount());
	        
			List<StatuteCitation> statutesForCode = slipOpinionRepository.selectForCode("welfare");
	        StatuteCitation maxWelfare = getCodeCitationMaxCaseReferrors(statutesForCode );
	        printCodeCitation(codesInterface, slipOpinionRepository, maxWelfare);
	        
	        printCodeCitation(codesInterface, slipOpinionRepository, slipOpinionRepository.testStatuteByCodeSection("welfare", "200"));
	
	        printCodeCitation(codesInterface, slipOpinionRepository, slipOpinionRepository.testStatuteByCodeSection("family code", "4058"));
	
	        printCodeCitation(codesInterface, slipOpinionRepository, slipOpinionRepository.testStatuteByCodeSection("family code", "300"));
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
                return o1.getReferringOpinionCount().size() - o2.getReferringOpinionCount().size();
            }
        });
        return statutesForCode.get(statutesForCode.size()-1);
	}
	
	private void printCodeCitation(
	    CodesInterface codesInterface,
	    SlipOpinionRepository slipOpinionRepository, 
	    StatuteCitation statuteCitation
	) {
	    if ( statuteCitation == null ) {
	        return; 
	    }
        CodeReference reference = codesInterface.findReference(statuteCitation.getStatuteKey().getCode(), new SectionNumber(-1, statuteCitation.getStatuteKey().getSectionNumber()));
        if ( reference == null ) return;
        System.out.println("Total refereeCount = " + statuteCitation.getReferringOpinionCount().size());
        boolean first = true;
        String indent = new String();
        int printed = 0;
        List<OpinionSummary> foundOpinions = slipOpinionRepository.getOpinions(statuteCitation.getReferringOpinionCount().keySet());
        List<OpinionSummary> referringOpinions = new ArrayList<OpinionSummary>();
        
//        for ( OpinionKey caseCitationKey: statuteCitation.getReferringOpinionCount().keySet() ) {
//        	if ( caseCitationKey.isSlipOpinion() ) continue;
//            OpinionSummary opinionSummary = databaseFacade.findOpinion(caseCitationKey);
      for ( OpinionSummary opinionSummary: foundOpinions ) {
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
        Collections.sort(referringOpinions, new Comparator<OpinionSummary>() {
            @Override
            public int compare(OpinionSummary o1, OpinionSummary o2) {
                if ( o1.getCountReferringOpinions() == o2.getCountReferringOpinions() ) {
                    int o1TimesCaseReferredTo = statuteCitation.getRefCount(o1.getOpinionKey());
                    int o2TimesCaseReferredTo = statuteCitation.getRefCount(o2.getOpinionKey());
                    return o1TimesCaseReferredTo - o2TimesCaseReferredTo; 
                }
                return o1.getCountReferringOpinions() - o2.getCountReferringOpinions();
            }
        });
        for (OpinionSummary opinionSummary: referringOpinions ) {
            int timesCaseReferredTo = statuteCitation.getRefCount(opinionSummary.getOpinionKey());
            System.out.println( indent + opinionSummary.getOpinionKey().toString()+":"+timesCaseReferredTo+":"+opinionSummary.getCountReferringOpinions());
            printed++;
        }
        System.out.println("Printed refereeCount = " + printed);
	}
        
}
