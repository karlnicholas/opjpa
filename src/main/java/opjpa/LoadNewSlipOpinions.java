package opjpa;

import javax.persistence.*;

import codesparser.*;

import java.io.*;
import java.util.*;

import load.InterfacesFactory;
import load.LoadHistoricalOpinions;
import memorydb.MemoryDBFacade;
import opinions.facade.*;
import opinions.model.SlipOpinion;
import opinions.parsers.*;

public class LoadNewSlipOpinions {
	
//	private static Logger log = Logger.getLogger(OpJpaTest.class.getName());
	private EntityManagerFactory emf;

	public final static String caseListFile = "html/60days.html";
	public final static String encoding = "UTF-8";
	public final static String xmlcodes = "/xmlcodes"; 
	
//    private final static int levelOfInterest = 2;
//    private final static boolean compressSections = true;
	private final static String DEBUGFILE = "ALL"; // "A140107" or "ALL";

	public static void main(String[] args) throws Exception {
		LoadNewSlipOpinions opJpa = new LoadNewSlipOpinions();
//		opJpa.runUpdateScheduler();
		CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		codesInterface.loadXMLCodes(new File(LoadNewSlipOpinions.class.getResource(xmlcodes).getFile()));
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
		    
	//		CaseParserInterface caseParserInterface = InterfacesFactory.getCaseParserInterface(); 
			CaseParserInterface caseParserInterface = new CATestCases(); 
	
			Reader reader = caseParserInterface.getCaseList();
			List<SlipOpinion> opinions = caseParserInterface.parseCaseList(reader);
			reader.close();
	
			// trim list to available test cases
			Iterator<SlipOpinion> ccit = opinions.iterator();
			while ( ccit.hasNext() ) {
				SlipOpinion slipOpinion = ccit.next();
				if ( DEBUGFILE != null && !DEBUGFILE.equals("ALL") ) {
					if ( !slipOpinion.getFileName().equals(DEBUGFILE)) ccit.remove();
				} else if (DEBUGFILE != null && DEBUGFILE.equals("ALL")) {
					File tFile = new File(CATestCases.casesDir + slipOpinion.getFileName() + ".DOC");
					if ( !tFile.exists() ) ccit.remove();
				} else {
					Date cDate = slipOpinion.getPublishDate();
					if ( cDate.compareTo(cal.getTime()) != 0 ) {
						ccit.remove();
					}
				}
			}
			System.out.println("Cases = " + opinions.size() );
			// Create the CACodes list
		    CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
			
	//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
			CodeTitles[] codeTitles = codesInterface.getCodeTitles();
			CodeCitationParser parser = new CodeCitationParser(codeTitles);
			
			System.out.println("There are " + opinions.size() + " SlipOpinions to process");

			MemoryDBFacade memoryDB = MemoryDBFacade.getInstance();

			for( SlipOpinion slipOpinion: opinions ) {
				System.out.println("Case = " + slipOpinion.getFileName());
				ParserResults parserResults = parser.parseCase(caseParserInterface.getCaseFile(slipOpinion, false), slipOpinion, slipOpinion.getOpinionKey() );
	        	parserResults.persist(slipOpinion, memoryDB);
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
