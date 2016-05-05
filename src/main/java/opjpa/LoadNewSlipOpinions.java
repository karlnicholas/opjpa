package opjpa;

import javax.persistence.*;

import codesparser.*;
import gscalifornia.factory.CAStatutesFactory;

import java.io.*;
import java.util.*;

import load.LoadHistoricalOpinions;
import opca.memorydb.MemoryPersistance;
import opca.model.SlipOpinion;
import opca.parser.*;

public class LoadNewSlipOpinions {
	
//	private static Logger log = Logger.getLogger(OpJpaTest.class.getName());
	private EntityManagerFactory emf;

	public final static String xmlcodes = "/xmlcodes"; 
	
//    private final static int levelOfInterest = 2;
//    private final static boolean compressSections = true;
	private final static String DEBUGFILE = "ALL"; // "A140107" or "ALL";

	public static void main(String[] args) throws Exception {
		LoadNewSlipOpinions opJpa = new LoadNewSlipOpinions();
//		opJpa.runUpdateScheduler();
		CAStatutesFactory.getInstance().getCodesInterface(true);
		opJpa.loadAndPersistCases();

	}

	public LoadNewSlipOpinions() throws Exception {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	

	public void loadAndPersistCases() throws Exception {
	    // Test case
		try {
		    Calendar cal = GregorianCalendar.getInstance();
		    cal.set(2014, Calendar.JULY, 7, 0, 0, 0 );
		    cal.set(Calendar.MILLISECOND, 0);
		    
//			CaseScraperInterface caseScraper = new CACaseScraper(); // InterfacesFactory.getCaseParserInterface(); 
			CaseScraperInterface caseScraper = new TestCACaseScraper(false); 
	
			List<SlipOpinion> opinions = caseScraper.getCaseList();
	
			// trim list to available test cases
			Iterator<SlipOpinion> ccit = opinions.iterator();
			while ( ccit.hasNext() ) {
				SlipOpinion slipOpinion = ccit.next();
				if ( DEBUGFILE != null && !DEBUGFILE.equals("ALL") ) {
					if ( !slipOpinion.getFileName().equals(DEBUGFILE)) ccit.remove();
				} else if (DEBUGFILE != null && DEBUGFILE.equals("ALL")) {
					File tFile = new File(TestCACaseScraper.casesDir + slipOpinion.getFileName() + ".DOC");
					if ( !tFile.exists() ) ccit.remove();
				} else {
					Date cDate = slipOpinion.getOpinionDate();
					if ( cDate.compareTo(cal.getTime()) != 0 ) {
						ccit.remove();
					}
				}
			}
			System.out.println("Cases = " + opinions.size() );
			// Create the CACodes list
		    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
			
	//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
			CodeTitles[] codeTitles = codesInterface.getCodeTitles();
			CodeCitationParser parser = new CodeCitationParser(codeTitles);
			
			System.out.println("There are " + opinions.size() + " SlipOpinions to process");

			MemoryPersistance memoryDB = MemoryPersistance.getInstance();

			List<ParserDocument> parserDocuments = caseScraper.getCaseFiles(opinions);
			for( ParserDocument parserDocument: parserDocuments ) {
				ParserResults parserResults = parser.parseCase(parserDocument, parserDocument.opinionBase, parserDocument.opinionBase.getOpinionKey() );
	    		parser.checkSlipOpinionDetails((SlipOpinion) parserDocument.opinionBase, parserDocument);

	        	parserResults.persist(parserDocument.opinionBase, memoryDB);
			}
			
			LoadHistoricalOpinions loadOpinions = new LoadHistoricalOpinions(emf, codesInterface);
			loadOpinions.processesOpinions(memoryDB);
			loadOpinions.processesStatutes(memoryDB);

			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			for( SlipOpinion slipOpinion: opinions ) {
				em.persist(slipOpinion);
			}
			tx.commit();
			em.close();

			// persist
		} finally {
			emf.close();
		}
	}

}
