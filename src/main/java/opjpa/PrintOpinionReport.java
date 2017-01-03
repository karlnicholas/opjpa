package opjpa;

import java.util.List;

import opca.model.OpinionSummary;
import opca.parser.ParsedOpinionCitationSet;
import opca.view.OpinionScoreList;
import opca.view.OpinionView;
import opca.view.StatuteView;

public class PrintOpinionReport {

    public void printBaseOpinionReport(
		OpinionView opinionView,     		
		ParsedOpinionCitationSet parserResults, 
		List<OpinionScoreList> scoreSlipOpinionOpinions		
	) throws Exception {
        System.out.println("ApiOpinion: " + opinionView);
        System.out.println("--------- STATUTES -----------");
    	for ( StatuteView statuteView: opinionView.getStatutes() ) {
//	        	System.out.println(statuteView.getStatutesBaseClass().getTitle(false).toUpperCase());
//	        	System.out.println(statuteView.getStatutesBaseClass().getShortTitle().toUpperCase());
//	        	List<SectionView> sortedSectionViews = sortSectionViews(statuteView);
    		System.out.println(String.format("%-10s%-80s%-20s", statuteView.getRefCount(), statuteView.getDisplayTitlePath(), statuteView.getDisplaySections()));
        }
        System.out.println("--------- OPINIONS -----------");		
		//what is opinionBase needed for? Is it just extra info, or should it be in opinionView?
        for (  OpinionScoreList opinionScoreList: scoreSlipOpinionOpinions	) {
        	OpinionSummary opinionSummary = parserResults.findOpinion(opinionScoreList.getOpinionKey());
        	System.out.println(opinionSummary.getOpinionKey().toString() + ":" + opinionSummary.getCountReferringOpinions());
        }
        
    }
}
