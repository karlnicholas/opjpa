package opjpa;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
import opinions.model.OpinionSummaryKey;
import opinions.model.SlipOpinion;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionBase;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.model.StatuteCitationKey;
import opinions.parsers.ParserResults;
import opinions.view.OpinionView;
import opinions.view.OpinionViewBuilder;
import opinions.view.StatuteView;
import opinions.view.ViewReference;
import opinions.view.SectionView;

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
        
		DatabaseFacade databaseFacade = new DatabaseFacade(em);

//        String iface = "code.CACodes";
//        CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
        CodesInterface codesInterface = new CACodes();
        codesInterface.loadXMLCodes(new File(StatuteReport.class.getResource("/xmlcodes").getFile()));

//        DatabaseFacade.getInstance().initializeDB(codesInterface);
//        DatabaseFacade.getInstance().writeToXML();
//        DatabaseFacade.getInstance().initFromXML();

        SlipOpinion opinion = databaseFacade.findSlipOpinionBySummaryKey(new OpinionSummaryKey("1 Slip.Op 10287300"));
        
        class MyPersistenceLookup implements ParserResults.PersistenceLookup {
        	DatabaseFacade databaseFacade;
        	public MyPersistenceLookup(DatabaseFacade databaseFacade) {
        		this.databaseFacade = databaseFacade;
        	}
			@Override
			public StatuteCitation findStatute(StatuteCitationKey statuteKey) {
				return databaseFacade.findStatute(statuteKey);
			}
			@Override
			public OpinionSummary findOpinion(OpinionSummaryKey opinionKey) {
				return databaseFacade.findOpinion(opinionKey);
			}
        }
        
        ParserResults parserResults = new ParserResults(opinion, new MyPersistenceLookup(databaseFacade));
        printOpinionReport(codesInterface, parserResults, opinion );
        
//        for ( OpinionSummary op: persistenceFacade.getAllOpinions() ) {
//            if (op.getStatutesReferredTo().size() > 10 ) System.out.println(op.getName() +":" + op.getStatutesReferredTo().size());
//        }
    }
    
    public static void printOpinionReport(
    		CodesInterface codesInterface, 
    		ParserResults parserResults, 
    		OpinionBase opinionBase
	) throws Exception {
        if ( opinionBase == null ) return;
        OpinionViewBuilder opinionCaseBuilder = new OpinionViewBuilder(codesInterface);
        //
        OpinionView opinionCase = opinionCaseBuilder.buildOpinionView(opinionBase, parserResults, true);
        opinionCase.trimToLevelOfInterest(2, true);
        System.out.println("Opinion: " + opinionCase);
        System.out.println("--------- STATUTES -----------");
        for ( StatuteView opinionCode: opinionCase.getCodes() ) {
        	System.out.println(opinionCode.getCodeReference().getTitle(false).toUpperCase());
        	List<SectionView> sorted = sortSubcodes(opinionCode);
        	handleSubcode(sorted, opinionCode);
        	List<String> currentTitle = null;
        	for ( SectionView section: sorted ) {
        		currentTitle = checkPrintTitle(section, currentTitle);
        		String indent = "  ";
        		for ( int i=1, j=currentTitle.size(); i<j; ++i ) {
        			indent = indent + "  ";
        		}
        		System.out.println(String.format("%s%-5d %-15s %s", indent, section.getRefCount(), section.getDisplaySectionNumber().replace("§ ", " § ").replace("§ §", "§§"), section.getCodeReference().getTitle(true)));
        	}
        }
        System.out.println("--------- OPINIONS -----------");
		class OpinionSummaryPrint {
			int countRefs;
			OpinionSummary opinionCited;
			public OpinionSummaryPrint(OpinionSummary opinionCited, int countRefs) {
        		this.opinionCited = opinionCited;
        		this.countRefs = countRefs;
			}
        }
        List<OpinionSummaryPrint> opinionsCited = new ArrayList<OpinionSummaryPrint>();
        for ( OpinionSummaryKey opinionKey: opinionBase.getOpinionCitationKeys()) {
        	OpinionSummary opinionCited = parserResults.findOpinion(opinionKey);
        	int countRefs = 0;
        	for ( StatuteCitationKey statuteKey: opinionBase.getStatuteCitationKeys() ) {
        		StatuteCitation statuteCite = parserResults.findStatute(statuteKey);
        		countRefs += statuteCite.getRefCount(opinionKey);
//        		System.out.print(":" + statuteCite.getRefCount(opinionKey));
        	}
        	if ( countRefs > 0 || opinionCited.getCountOpinionsReferredFrom() > 10) {
        		opinionsCited.add(new OpinionSummaryPrint(opinionCited, countRefs));
        	}
        }
        Collections.sort(opinionsCited, new Comparator<OpinionSummaryPrint>() {
			@Override
			public int compare(OpinionSummaryPrint o1, OpinionSummaryPrint o2) {
				return o1.countRefs - o2.countRefs; 
			}
        });
        for ( OpinionSummaryPrint opPrint: opinionsCited) {
        	System.out.println(opPrint.opinionCited.getKey().toString() + ":" + opPrint.opinionCited.getCountOpinionsReferredFrom()+":"+opPrint.countRefs);
        }
    }
    
    private static List<String> checkPrintTitle(SectionView section, List<String> currentTitle) {
		ArrayList<String> fullTitle = new ArrayList<String>(Arrays.asList(section.getCodeReference().getFullTitle(":").split("[:]")));
		fullTitle.remove(fullTitle.size()-1);
		if ( currentTitle == null || fullTitle.size() != currentTitle.size() ) {
			printFullTitle(fullTitle);
			return fullTitle;
		}
		int idx = 0;
		for(String title: fullTitle) {
			if ( !title.equals(currentTitle.get(idx++))) {
				printFullTitle(fullTitle);
				break;
			}
		}
    	return fullTitle;
    }
	private static void printFullTitle(List<String> fullTitle) {
		String indent = "  ";
		for ( int i=1, j = fullTitle.size(); i<j; ++i) {
			System.out.println(indent + fullTitle.get(i));
			indent = indent + "  ";
		}
	}
    private static List<SectionView> sortSubcodes(StatuteView opinionCode) {
    	List<SectionView> sortedSections = new ArrayList<SectionView>();
    	sortedSections.sort(new Comparator<SectionView>() {
			@Override
			public int compare(SectionView o1, SectionView o2) {
				return o1.getCodeReference().getSection().getSectionNumbers().get(0).getPosition()
						- o2.getCodeReference().getSection().getSectionNumbers().get(0).getPosition();
			}
    		
    	});
    	return sortedSections;
    }
    private static void handleSubcode(List<SectionView> sortedSubcodes, ViewReference reference) {
		handleSections(sortedSubcodes, reference);
    	for ( ViewReference subcode: reference.getSubcodes() ) {
    		handleSubcode(sortedSubcodes, subcode);
    	}
    }
    private static void handleSections(List<SectionView> sortedSubcodes, ViewReference reference) {
		for ( SectionView section: reference.getSections() ) {
			sortedSubcodes.add(section); 
		}
    }
    
}
