

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.*;

import opca.model.SlipOpinion;
import opca.parser.*;

public class OnlineFacade {
	private static final Logger log = Logger.getLogger(OnlineFacade.class.getName());
	@Inject
	private OpinionQueries opinionQueries;
	
//	private EntityManager em;
	private CaseParserInterface caseParserInterface;
//	private CodesInterface codesInterface;
	
	public OnlineFacade(
			EntityManager em, 
			CaseParserInterface caseParserInterface //, 
//			CodesInterface codesInterface
	) {
//		this.em = em;
		this.caseParserInterface = caseParserInterface;
//		this.codesInterface = codesInterface;
	}
	
	public synchronized void updateDatabase() {
//		OpinionQueries dbFacade = new OpinionQueries(em);
		
		List<SlipOpinion> onlineCases = listOnlineCases();
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
					downloadCases(tenCases);
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
	
	private List<SlipOpinion> listOnlineCases() {
		Reader reader = null;
		try {
	    	reader = caseParserInterface.getCaseList();
	    	List<SlipOpinion> courtCases = caseParserInterface.parseCaseList(reader);
	    	reader.close();
	    	return courtCases;
		} catch ( Exception e) {
			throw new RuntimeException(e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * Not thread safe, so synchronized. I don't think anyone would 
	 * be calling it expect internally, but making the point anyway.
	 */
	private void downloadCases(List<SlipOpinion> cases) {
		try {
	    	// Create the CACodes list
//			CodeTitles[] codeTitles = codesInterface.getCodeTitles();
//			CodeCitationParser parser = new CodeCitationParser(codeTitles);
			// loop and download each case
			for( SlipOpinion slipOpinion: cases ) {
				log.info("Downloading Case: " + slipOpinion.getFileName());
//				parser.parseCase(caseParserInterface.getCaseFile(courtCase, false), courtCase);
				caseParserInterface.getCaseFile(slipOpinion, false);
			}
		} catch (Exception e) {
			throw new RuntimeException( e );
		} 
	}

}
