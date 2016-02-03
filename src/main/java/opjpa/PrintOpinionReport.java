package opjpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import codesparser.CodesInterface;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionBase;
import opinions.model.OpinionKey;
import opinions.model.OpinionSummary;
import opinions.model.SlipOpinion;
import opinions.model.StatuteCitation;
import opinions.model.StatuteKey;
import opinions.parsers.ParserResults;
import opinions.view.OpinionView;
import opinions.view.OpinionViewBuilder;
import opinions.view.SectionView;
import opinions.view.StatuteView;
import opinions.view.ViewReference;

public class PrintOpinionReport {

    private class MyPersistenceLookup implements ParserResults.PersistenceLookup {
    	DatabaseFacade databaseFacade;
    	public MyPersistenceLookup(DatabaseFacade databaseFacade) {
    		this.databaseFacade = databaseFacade;
    	}
		@Override
		public StatuteCitation statuteExists(StatuteKey statuteKey) {
			return databaseFacade.findStatute(statuteKey);
		}
		@Override
		public OpinionSummary opinionExists(OpinionKey opinionKey) {
			return databaseFacade.findOpinion(opinionKey);
		}
		@Override
		public List<StatuteCitation> getStatutes(Collection<StatuteKey> statuteKeys) {					
			return databaseFacade.getStatutes(statuteKeys);
		}
		@Override
		public List<OpinionSummary> getOpinions(Collection<OpinionKey> opinionKeys) {
			return databaseFacade.getOpinions(opinionKeys);
		}
    }
    
	public void printOpinionReport(
    		CodesInterface codesInterface,
    		EntityManager em, 
    		OpinionKey opinionKey
	) throws Exception {
// Date startDate = new Date();
        
		DatabaseFacade databaseFacade = new DatabaseFacade(em);
		SlipOpinion slipOpinion = databaseFacade.slipOpinionExists(opinionKey);
		if ( slipOpinion != null ) {
	    	ParserResults parserResults = new ParserResults(slipOpinion, new MyPersistenceLookup(databaseFacade));

	    	OpinionViewBuilder opinionCaseBuilder = new OpinionViewBuilder(codesInterface);
	        //
	        OpinionView opinionCase = opinionCaseBuilder.buildSlipOpinionView(slipOpinion, parserResults, true);
	        opinionCase.trimToLevelOfInterest(2, true);

	    	printBaseOpinionReport(parserResults, slipOpinion, opinionCase);

// System.out.println("TIMING: " + (new Date().getTime()-startDate.getTime()));
	    	return;
		}
        OpinionSummary opinionSummary = databaseFacade.opinionExists(opinionKey);
		if ( opinionSummary != null ) {
	    	ParserResults parserResults = new ParserResults(opinionSummary, new MyPersistenceLookup(databaseFacade));
	        OpinionViewBuilder opinionCaseBuilder = new OpinionViewBuilder(codesInterface);
	        //
	        OpinionView opinionCase = opinionCaseBuilder.buildOpinionSummaryView(opinionSummary, parserResults, true);
	        opinionCase.trimToLevelOfInterest(2, true);

	    	printBaseOpinionReport(parserResults, opinionSummary, opinionCase);

// System.out.println("TIMING: " + (new Date().getTime()-startDate.getTime()));
	    	return;
		}

    }
    

    private void printBaseOpinionReport(
    		ParserResults parserResults, 
    		OpinionBase opinionBase, 
    		OpinionView opinionCase
	) throws Exception {
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
        for ( OpinionKey opinionKey: opinionBase.getOpinionCitations()) {
        	OpinionSummary opinionCited = parserResults.findOpinion(opinionKey);
        	int countRefs = 0;
        	for ( StatuteKey statuteKey: opinionBase.getStatuteCitationKeys() ) {
        		StatuteCitation statuteCited = parserResults.findStatute(statuteKey);
        		countRefs += statuteCited.getRefCount(opinionKey);
//        		System.out.print(":" + statuteCite.getRefCount(opinionKey));
        	}
        	if ( countRefs > 0 || opinionCited.getCountReferringOpinions() > 10) {
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
        	System.out.println(opPrint.opinionCited.getOpinionKey().toString() + ":" + opPrint.opinionCited.getCountReferringOpinions()+":"+opPrint.countRefs);
        }
    }
    
    private List<String> checkPrintTitle(SectionView section, List<String> currentTitle) {
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
	private void printFullTitle(List<String> fullTitle) {
/*		
		String indent = "  ";
		for ( int i=1, j = fullTitle.size(); i<j; ++i) {
			System.out.println(indent + fullTitle.get(i));
			indent = indent + "  ";
		}
*/		
		if ( fullTitle.size() < 2 ) return;
		System.out.print("  ");
		for ( int i=1, j = fullTitle.size(); i<j; ++i) {
			System.out.print(fullTitle.get(i)+", ");
			fullTitle.get(i);
		}
		System.out.println();
	}
    private List<SectionView> sortSubcodes(StatuteView opinionCode) {
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
    private void handleSubcode(List<SectionView> sortedSubcodes, ViewReference reference) {
		handleSections(sortedSubcodes, reference);
    	for ( ViewReference subcode: reference.getSubcodes() ) {
    		handleSubcode(sortedSubcodes, subcode);
    	}
    }
    private void handleSections(List<SectionView> sortedSubcodes, ViewReference reference) {
		for ( SectionView section: reference.getSections() ) {
			sortedSubcodes.add(section); 
		}
    }

}