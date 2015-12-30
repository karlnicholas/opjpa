package opjpa;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodeReference;
import codesparser.CodesInterface;
import codesparser.SectionNumber;
import opinions.model.OpinionSummaryKey;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.parsers.ParserResults.PersistenceInterface;

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
		
		DatabaseFacade databaseFacade = new DatabaseFacade(em);

//      String iface = "code.CACodes";
//      CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
	    CodesInterface codesInterface = new CACodes();
        codesInterface.loadXMLCodes(new File(StatuteReport.class.getResource("/xmlcodes").getFile()));

//        databaseFacade.initializeDB(codesInterface);
//        DatabaseFacade.getInstance().writeToXML();
//        DatabaseFacade.getInstance().initFromXML();

        System.out.println("statuteTable size = " + databaseFacade.getCount());
        
		List<StatuteCitation> statutesForCode = databaseFacade.selectForCode("welfare");
        StatuteCitation maxWelfare = getCodeCitationMaxCaseReferrors(statutesForCode );
        printCodeCitation(codesInterface, databaseFacade, maxWelfare);
        
        printCodeCitation(codesInterface, databaseFacade, databaseFacade.findStatuteByCodeSection("welfare", "200"));

        printCodeCitation(codesInterface, databaseFacade, databaseFacade.findStatuteByCodeSection("family code", "4058"));

        printCodeCitation(codesInterface, databaseFacade, databaseFacade.findStatuteByCodeSection("family code", "300"));
        
	}
	
	public static StatuteCitation getCodeCitationMaxCaseReferrors(
		List<StatuteCitation> statutesForCode
	) {
        if ( statutesForCode.size() == 0 ) return null;
        Collections.sort(statutesForCode, new Comparator<StatuteCitation>() {
            @Override
            public int compare(StatuteCitation o1, StatuteCitation o2) {
                return o1.getReferringCaseMap().size() - o2.getReferringCaseMap().size();
            }
        });
        return statutesForCode.get(statutesForCode.size()-1);
	}
	
	public static void printCodeCitation(
	    CodesInterface codesInterface,
	    PersistenceInterface persistence, 
	    StatuteCitation statuteCitation
	) {
	    if ( statuteCitation == null ) {
	        return; 
	    }
        CodeReference reference = codesInterface.findReference(statuteCitation.getKey().getCode(), new SectionNumber(-1, statuteCitation.getKey().getSectionNumber()));
        if ( reference == null ) return;
        System.out.println("Total refereeCount = " + statuteCitation.getReferringCaseMap().size());
        boolean first = true;
        String indent = new String();
        int printed = 0;
        List<OpinionSummary> referringOpinions = new ArrayList<OpinionSummary>();
        for ( OpinionSummaryKey caseCitationKey: statuteCitation.getReferringCaseMap().keySet() ) {
            OpinionSummary opinionSummary = persistence.findOpinion(caseCitationKey);
            if ( opinionSummary == null ) continue;
            // don't print anything with less than 3 referees
            if ( opinionSummary.getCountOpinionsReferredFrom() < 3 ) continue;
            if ( first ) {
                String[] titles = reference.getFullTitle(":").split("[:]");
                for ( String title: titles ) {
                    System.out.println(indent + title);
                    indent = indent + "  ";
                }
                System.out.println(indent+"§"+statuteCitation.getKey().getSectionNumber());
                indent = indent + "  ";
                first = false;
            }
            referringOpinions.add(opinionSummary);
        }
        Collections.sort(referringOpinions, new Comparator<OpinionSummary>() {
            @Override
            public int compare(OpinionSummary o1, OpinionSummary o2) {
                if ( o1.getCountOpinionsReferredFrom() == o2.getCountOpinionsReferredFrom() ) {
                    int o1TimesCaseReferredTo = statuteCitation.getRefCount(o1.getKey());
                    int o2TimesCaseReferredTo = statuteCitation.getRefCount(o2.getKey());
                    return o1TimesCaseReferredTo - o2TimesCaseReferredTo; 
                }
                return o1.getCountOpinionsReferredFrom() - o2.getCountOpinionsReferredFrom();
            }
        });
        for (OpinionSummary opinionSummary: referringOpinions ) {
            int timesCaseReferredTo = statuteCitation.getRefCount(opinionSummary.getKey());
            System.out.println( indent + opinionSummary.getKey().toString()+":"+timesCaseReferredTo+":"+opinionSummary.getCountOpinionsReferredFrom());
            printed++;
        }
        System.out.println("Printed refereeCount = " + printed);
	}
        
}
