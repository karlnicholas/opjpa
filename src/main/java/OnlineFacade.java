

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import opca.model.SlipOpinion;
import opca.parser.CaseScraperInterface;

public class OnlineFacade {
	private static final Logger log = Logger.getLogger(OnlineFacade.class.getName());
	private OpinionQueries opinionQueries;
	
	private CaseScraperInterface caseParser;
	
	public OnlineFacade(
			CaseScraperInterface caseParser//, 
	) {
		this.caseParser = caseParser;
	}
	
	public synchronized void updateDatabase() {
//		OpinionQueries dbFacade = new OpinionQueries(em);
		
		List<SlipOpinion> onlineCases = caseParser.getCaseList();

		if ( onlineCases == null || onlineCases.size() == 0 ) {
			log.info("No cases found online: returning.");
			return;
		}
		List<SlipOpinion> dbCases = opinionQueries.listSlipOpinions();
		List<SlipOpinion> dbCopy = new ArrayList<SlipOpinion>(dbCases);
		log.info("Found " + dbCases.size() + " in the database.");
		
		// Determine old cases
		// remove online cases from dbCopy
		// what's left is no longer in online List
		Iterator<SlipOpinion> dbit = dbCopy.iterator();
		while ( dbit.hasNext() ) {
			SlipOpinion dbCase = dbit.next();
			if ( onlineCases.contains(dbCase) ) {
				dbit.remove();
			}
		}
		if ( dbCopy.size() > 0 ) {
			log.info("Deleting " + dbCopy.size() + " cases." );
			opinionQueries.removeSlipOpinions(dbCopy);
		} else {
			log.info("No cases deleted.");
		}
		
		// Determine new cases
		// remove already persisted cases from onlineList
		for ( SlipOpinion dbCase: dbCases ) {
			int idx = onlineCases.indexOf(dbCase);
			if ( idx >= 0 ) {
				onlineCases.remove(idx);
			}
		}
		if ( onlineCases.size() > 0 ) {
			int errMax = 3;
			while ( onlineCases.size() > 0 ) {
				List<SlipOpinion> tenCases = new ArrayList<SlipOpinion>();
				Iterator<SlipOpinion> onit = onlineCases.iterator();
				int count = 10;
				while ( onit.hasNext() ) {
					tenCases.add(onit.next());
					onit.remove();
					if ( --count <= 0 ) break;
				}
				try{
//TODO:Was this way					downloadCases(tenCases);
					log.info("Persisting " + tenCases.size() + " cases." );
					opinionQueries.mergeAndPersistSlipOpinions(tenCases);
				} catch ( Throwable t ) {
					log.warning(t.getMessage());
					if ( --errMax <= 0 ) break;
				}
			}
		} else {
			log.info("No new cases.");
		}
	}
	
	
	/**
	 * Not thread safe, so synchronized. I don't think anyone would 
	 * be calling it expect internally, but making the point anyway.
	private void downloadCases(List<SlipOpinion> cases) {
		try {
	    	// Create the CACodes list
//			CodeTitles[] codeTitles = codesInterface.getCodeTitles();
//			CodeCitationParser parser = new CodeCitationParser(codeTitles);
			// loop and download each case
			for( SlipOpinion slipOpinion: cases ) {
				log.info("Downloading Case: " + slipOpinion.getFileName());
//				parser.parseCase(caseParserInterface.getCaseFile(courtCase, false), courtCase);
				caseParser.getCaseFile(slipOpinion);
			}
		} catch (Exception e) {
			throw new RuntimeException( e );
		} 
	}
	 */

}
