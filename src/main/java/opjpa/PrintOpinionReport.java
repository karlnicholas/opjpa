package opjpa;

import opca.parser.ParsedOpinionCitationSet;
import opca.view.CaseView;
import opca.view.OpinionView;
import opca.view.StatuteView;

public class PrintOpinionReport {

    public void printBaseOpinionReport(
		OpinionView opinionView,     		
		ParsedOpinionCitationSet parserResults		
	) throws Exception {
        System.out.println("ApiOpinion: " + opinionView);
        System.out.println("--------- STATUTES -----------");
    	for ( StatuteView statuteView: opinionView.getStatutes() ) {
//	        	System.out.println(statuteView.getStatutesBaseClass().getTitle(false).toUpperCase());
//	        	System.out.println(statuteView.getStatutesBaseClass().getShortTitle().toUpperCase());
//	        	List<SectionView> sortedSectionViews = sortSectionViews(statuteView);
    		System.out.println(String.format("%-10d%-10d%-10d%-80s%-20s", statuteView.getImportance(), statuteView.getScore(), statuteView.getRefCount(), statuteView.getDisplayTitlePath(), statuteView.getDisplaySections()));
        }
        System.out.println("--------- OPINIONS -----------");		
		//what is opinionBase needed for? Is it just extra info, or should it be in opinionView?
        for (  CaseView caseView: opinionView.getCases() ) {
//        	OpinionSummary opinionSummary = parserResults.findOpinion(opinionScoreList.getOpinionKey());
//        	System.out.println(opinionSummary.getOpinionKey().toString() + ":" + opinionSummary.getCountReferringOpinions());
        	System.out.println(String.format("%-10d%-10d%-10d%-40s", caseView.getImportance(), caseView.getScore(), caseView.getCountReferringOpinions(), caseView.getCitation().toString()));
        }
        
    }
}
