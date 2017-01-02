package opjpa;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.parser.ParsedOpinionCitationSet;
import opca.service.SlipOpinionService;
import opca.view.OpinionView;
import opca.view.OpinionViewBuilder;
import opca.view.SectionView;
import opca.view.StatuteCaseScore;
import opca.view.StatuteView;
import opca.view.ViewReference;
import statutes.StatutesBaseClass;
import statutes.StatutesRoot;
import statutesws.StatutesWSService;
import statutesws.StatutesWS;

public class PrintOpinionReport {

    public void printBaseOpinionReport(
    		ParsedOpinionCitationSet parserResults, 
    		OpinionView opinionView
	) throws Exception {
        System.out.println("ApiOpinion: " + opinionView);
        System.out.println("--------- STATUTES -----------");
        Map<StatutesRoot, List<StatuteView>> combinedStatutes = opinionView.gatherStatutes();
        for ( StatutesRoot key: combinedStatutes.keySet()) {
        	List<StatuteView> statuteViews =  combinedStatutes.get(key);
        	for ( StatuteView statuteView: statuteViews ) {
//	        	System.out.println(statuteView.getStatutesBaseClass().getTitle(false).toUpperCase());
//	        	System.out.println(statuteView.getStatutesBaseClass().getShortTitle().toUpperCase());
//	        	List<SectionView> sortedSectionViews = sortSectionViews(statuteView);
	        	List<SectionView> sortedSectionViews = new ArrayList<SectionView>();
	        	getAllSectionViews(sortedSectionViews, statuteView);
	        	for ( SectionView section: sortedSectionViews ) {
		        	List<String> shortTitles = section.getShortTitles();
		        	String shortTitle = shortTitles.toString().replace("[", "").replace("]", "");
	        		String indent = "  ";
	        		for ( int i=1, j=shortTitles.size(); i<j; ++i ) {
	        			indent = indent + "  ";
	        		}
//	        		System.out.println(String.format("%s%-5d %-15s %s", indent, statuteView.getRefCount(), statuteView.getDisplaySectionNumber(), section.getStatutesBaseClass().getTitle(true)));
	        		System.out.println(String.format("%-5d %s, %s %-15s", statuteView.getRefCount(), shortTitle, section.getStatutesBaseClass().getTitle(true), ("§§ " + section.getStatutesBaseClass().getStatuteRange().toString()) ));
	        	}
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
		
		//what is opinionBase needed for? Is it just extra info, or should it be in opinionView?
        List<OpinionSummaryPrint> opinionsCited = new ArrayList<OpinionSummaryPrint>();
        for ( OpinionKey opinionKey: opinionView.getOpinionCitations()) {
        	OpinionSummary opinionCited = parserResults.findOpinion(opinionKey);
/*        	
        	for ( StatuteKey statuteKey: opinionCited.getStatuteCitations() ) {
        		int foundPosition = Collections.binarySearch(opinionStatuteKeys, statuteKey);
        		if ( foundPosition >= 0 ) {
        			// search scoreMatrix for opinionStatuteCitation
        			for ( List<StatuteCaseScore> statuteCaseScoreList: statuteCaseScores ) {
        				if (statuteCaseScoreList.get(0).getSlipOpinionStatute().equals(opinionStatuteKeys.get(foundPosition))) {
        					//TODO will all entries be unqiue?
        					StatuteCaseScore statuteCaseScore = new StatuteCaseScore();
        					statuteCaseScore.setSlipOpinionStatute(opinionStatuteKeys.get(foundPosition));
        					statuteCaseScore.add(new )
        				}
        			}
        		}
        	}
*/        	
/*        	
            // 
            int countRefs = 0;
        	// List<StatuteView> statuteViews
        	for ( StatuteKey statuteKey: opinionView.getStatuteCitations() ) {
//         		StatuteCitation statuteCited = parserResults.findStatute(statuteKey);
        		StatuteView existingStatute = null;
                List<StatuteView> statuteViews = combinedStatutes.get(statuteKey.getCode());
            	for ( StatuteView statuteView: statuteViews ) {
            		if ( statuteView.getSectionNumber().equals(statuteKey.getSectionNumber())) {
            			existingStatute = statuteView;
            			break;
            		}
            	}
            	if ( existingStatute != null ) {
            		opinionView.updateStatuteCaseScore(existingStatute, opinionKey);
            	}
//        		countRefs += statuteCited.getRefCount(opinionKey);
//        		System.out.print(":" + statuteCite.getRefCount(opinionKey));
        	}
*/        	
/*
        	for ( StatuteKey statuteKey: opinionBase.getStatuteCitations() ) {
        		StatuteCitation statuteCited = parserResults.findStatute(statuteKey);
        		countRefs += statuteCited.getRefCount(opinionKey);
//        		System.out.print(":" + statuteCite.getRefCount(opinionKey));
        	}
        	if ( countRefs > 0 || opinionCited.getCountReferringOpinions() > 10) {
        		opinionsCited.add(new OpinionSummaryPrint(opinionCited, countRefs));
        	}
        	if ( countRefs > 0 || opinionCited.getCountReferringOpinions() > 10) {
        		opinionsCited.add(new OpinionSummaryPrint(opinionCited, opinionCited.getCountReferringOpinions()));
        	}
*/        	
    		opinionsCited.add(new OpinionSummaryPrint(opinionCited, opinionCited.getCountReferringOpinions()));
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
// work on score
    	// List<StatuteView> statuteViews =  combinedStatutes.get(key);
        // List<OpinionSummaryPrint> opinionsCited = new ArrayList<OpinionSummaryPrint>();

        
    }

/*
    private List<String> checkPrintTitle(SectionView section, List<String> currentTitle) {
    	ArrayList<StatutesBaseClass> baseStatutes = new ArrayList<StatutesBaseClass>(); 
		section.getStatutesBaseClass().getParents(baseStatutes);
		List<String> shortTitles = new ArrayList<String>();
		Collections.reverse(baseStatutes);
		for ( StatutesBaseClass baseStatute: baseStatutes ) {
			System.out.print(baseStatute.getShortTitle() + ":");
			shortTitles.add(baseStatute.getShortTitle());
		}
    	return shortTitles;
    }
*/    
	private void printTitle(List<String> shortTitle) {
/*		
		String indent = "  ";
		for ( int i=1, j = fullTitle.size(); i<j; ++i) {
			System.out.println(indent + fullTitle.get(i));
			indent = indent + "  ";
		}
*/		
		if ( shortTitle.size() < 2 ) return;
		System.out.print("  ");
		for ( int i=1, j = shortTitle.size(); i<j; ++i) {
			System.out.print(shortTitle.get(i)+", ");
			shortTitle.get(i);
		}
		System.out.println();
	}
    private List<String> checkPrintTitleFull(SectionView section, List<String> currentTitle) {
		ArrayList<String> fullTitle = new ArrayList<String>(Arrays.asList(section.getStatutesBaseClass().getFullTitle(":").split("[:]")));
		fullTitle.remove(fullTitle.size()-1);
		if ( currentTitle == null || fullTitle.size() != currentTitle.size() ) {
			printTitle(fullTitle);
			return fullTitle;
		}
		int idx = 0;
		for(String title: fullTitle) {
			if ( !title.equals(currentTitle.get(idx++))) {
				printTitle(fullTitle);
				break;
			}
		}
    	return fullTitle;
    }
	private void printFullTitleFull(List<String> fullTitle) {
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
    private void getAllSectionViews(List<SectionView> sortedSectionViews, ViewReference viewReference) {
		handleSections(sortedSectionViews, viewReference);
    	for ( ViewReference subcode: viewReference.getSubcodes() ) {
    		getAllSectionViews(sortedSectionViews, subcode);
    	}
    }
    private void handleSections(List<SectionView> sortedSectionViews, ViewReference viewReference) {
		for ( SectionView sectionView: viewReference.getSections() ) {
			sortedSectionViews.add(sectionView); 
		}
    }

}
