package opjpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parser.ParsedOpinionResults;
import opca.service.SlipOpinionService;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import opca.view.SectionView;
import opca.view.StatuteView;
import opca.view.ViewReference;
import parser.ParserInterface;

public class PrintOpinionReport {

	public void printSlipOpinionReport(
    		ParserInterface parserInterface,
    		EntityManager em, 
    		OpinionKey opinionKey
	) throws Exception {
// Date startDate = new Date();
        
		SlipOpinionService slipOpinionService = new SlipOpinionService();
		slipOpinionService.setEntityManager(em);
		SlipOpinion slipOpinion = slipOpinionService.slipOpinionExists(opinionKey);
		if ( slipOpinion != null ) {
	    	ParsedOpinionResults parserResults = new ParsedOpinionResults(slipOpinion, slipOpinionService.getPersistenceLookup());

	    	OpinionViewBuilder opinionCaseBuilder = new OpinionViewBuilder();
	        //TODO:FIX FOR STATUTESERVICE
	        OpinionView opinionCase = opinionCaseBuilder.buildSlipOpinionView(slipOpinion, parserResults);
	        opinionCase.trimToLevelOfInterest(2, true);

	    	printBaseOpinionReport(parserResults, slipOpinion, opinionCase);

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
    

    private void printBaseOpinionReport(
    		ParsedOpinionResults parserResults, 
    		OpinionBase opinionBase, 
    		OpinionView opinionCase
	) throws Exception {
        System.out.println("ApiOpinion: " + opinionCase);
        System.out.println("--------- STATUTES -----------");
        for ( StatuteView opinionCode: opinionCase.getStatutes() ) {
        	System.out.println(opinionCode.getStatutesBaseClass().getTitle(false).toUpperCase());
        	List<SectionView> sorted = sortSubcodes(opinionCode);
        	handleSubcode(sorted, opinionCode);
        	List<String> currentTitle = null;
        	for ( SectionView section: sorted ) {
        		currentTitle = checkPrintTitle(section, currentTitle);
        		String indent = "  ";
        		for ( int i=1, j=currentTitle.size(); i<j; ++i ) {
        			indent = indent + "  ";
        		}
        		System.out.println(String.format("%s%-5d %-15s %s", indent, section.getRefCount(), section.getDisplaySectionNumber().replace("§ ", " § ").replace("§ §", "§§"), section.getStatutesBaseClass().getTitle(true)));
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
        	for ( StatuteKey statuteKey: opinionBase.getStatuteCitations() ) {
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
		ArrayList<String> fullTitle = new ArrayList<String>(Arrays.asList(section.getStatutesBaseClass().getFullTitle(":").split("[:]")));
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
				return o1.getStatutesBaseClass().getStatutesLeaf().getSectionNumbers().get(0).getPosition()
						- o2.getStatutesBaseClass().getStatutesLeaf().getSectionNumbers().get(0).getPosition();
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
