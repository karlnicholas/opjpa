package opjpa;

import javax.persistence.*;

import codesparser.*;
import gscalifornia.factory.CAStatutesFactory;
import opcalifornia.CaseInterfacesService;
import opinion.data.SlipOpinionRepository;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import opinion.model.OpinionSummary;
import opinion.model.SlipOpinion;
import opinion.model.StatuteCitation;
import opinion.parsers.*;

public class OpJpaTest {
	
//	private static Logger log = Logger.getLogger(OpJpaTest.class.getName());
	private EntityManagerFactory emf;
	private EntityManager em;

	public final static String caseListFile = "html/60days.html";
	public final static String encoding = "UTF-8";
	public final static String xmlcodes = "/xmlcodes"; 
	
//    private final static int levelOfInterest = 2;
//    private final static boolean compressSections = true;
	private final static String DEBUGFILE = "ALL"; // "A140107" or "ALL";

	public static void main(String[] args) throws Exception {
		OpJpaTest opJpa = new OpJpaTest();
//		opJpa.runUpdateScheduler();
		// force loading of XML files
		CAStatutesFactory.getInstance().getCodesInterface(true);
		
/*		
		opJpa.testViewModel(
				opJpa.readCasesFromDatabase(), 
				codesInterface, 
				true, 
				2);
		
*/		

//		opJpa.reloadDatabase();

//		opJpa.refreshDownloads();

//		opJpa.loadAndPersistCases();
		
		opJpa.runAllSlipOpinions();

/*		
		opJpa.testViewModel(
				opJpa.loadTestCases(),  
				codesInterface, 
				true, 
				2);

		opJpa.testViewModel(
			opJpa.readCasesFromDatabase(), 
			codesInterface, 
			true, 
			2);

*/
		
	}
	
	public void reloadDatabase() throws Exception {

		CaseParserInterface caseParserInterface = new CATestCases(); 

		Reader reader = caseParserInterface.getCaseList();
		List<SlipOpinion> opinions = caseParserInterface.parseCaseList(reader);
		reader.close();

		System.out.println("Cases = " + opinions.size() );
		// Create the CACodes list
	    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
		
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);

		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		for( SlipOpinion slipOpinion: opinions ) {
			ParserDocument parserDocument = caseParserInterface.getCaseFile(slipOpinion, false);
			parser.parseCase(parserDocument, slipOpinion, slipOpinion.getOpinionKey() );
        	// look for details
        	// after a summaryParagraph is found, don't check any further .. (might have to change)
			
			// look for summary and disposition 
    		parser.checkSlipOpinionDetails(slipOpinion, parserDocument);

			em.persist(slipOpinion);
		}
		
		tx.commit();

	}
	
	public void refreshDownloads() throws Exception {

		DirectoryStream<Path> files = Files.newDirectoryStream( Paths.get(CATestCases.casesDir) );
		List<String> fileNames = new ArrayList<String>();
		Iterator<Path> fit = files.iterator();
		while (fit.hasNext() ) {
			fileNames.add(fit.next().toFile().getName());
		}
		List<String> fileNamesCopy = new ArrayList<String>(fileNames); 
		
		CaseInterfacesService casesInterface = new CaseInterfacesService();
		casesInterface.initialize(false);

		CaseParserInterface onlinecaseParser = casesInterface.getCaseParserInterface();
		Reader reader = onlinecaseParser.getCaseList();
//		reader = saveCopyOfCaseList(reader);
		List<SlipOpinion> onlineCases = onlinecaseParser.parseCaseList(reader);
		reader.close();
		
		SlipOpinionRepository slipOpinionRepository = new SlipOpinionRepository();
		slipOpinionRepository.setEntityManager(em);
		List<SlipOpinion> databaseCases = slipOpinionRepository.listSlipOpinions();

		// first to deletes
		for ( SlipOpinion slipOpinion: onlineCases ) {
			if ( fileNames.contains(slipOpinion.getFileName()+".DOC")) fileNames.remove(slipOpinion.getFileName()+".DOC");
			if ( databaseCases.contains(slipOpinion)) databaseCases.remove(slipOpinion);
		}
		for ( SlipOpinion slipOpinion: databaseCases ) {
			em.remove(slipOpinion);
		}
		for ( String caseName: fileNames ) {
			Path path = Paths.get(CATestCases.casesDir, caseName);
			if ( Files.exists(path) ) Files.delete(path);
		}
		// download and save remaining cases
		// which are ...
		Calendar cal = Calendar.getInstance();
		cal.set(1960, Calendar.JUNE, 1);
		for ( String caseName: fileNamesCopy ) {
			SlipOpinion slipOpinion = new SlipOpinion(caseName.replace(".DOC", ""), "title", cal.getTime(), "CA/4" );
			if ( onlineCases.contains(slipOpinion)) onlineCases.remove(slipOpinion);
		}
		
	    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);

		//		System.out.println(onlineCases);
		// EntityTransaction tx = em.getTransaction();
		// tx.begin();
		
		for( SlipOpinion slipOpinion: onlineCases ) {
			ParserDocument parserDoc = onlinecaseParser.getCaseFile(slipOpinion, true);
			ParserResults parserResults = parser.parseCase(parserDoc, slipOpinion, slipOpinion.getOpinionKey() );
        	parserResults.persist(slipOpinion, slipOpinionRepository.getPersistenceInterface());
//			em.persist(slipOpinion);
			System.out.println("Downloaded " + slipOpinion.getFileName() + ".DOC");
		}
		// tx.commit();
		
	}
	
//	private String[] terms = {"section", "§" , "sections", "§§"};
	public void playParse(CodesInterface codesInterface) throws Exception {
		CodeCitationParser codeCitationParser = new CodeCitationParser(codesInterface.getCodeTitles());
		String sentence = "(welf. & inst. code, §§ 4501; see also welf. & inst. code, § 4434.)";
		Calendar cal = Calendar.getInstance();
		cal.set(1960, Calendar.JUNE, 1);
		SlipOpinion slipOpinion = new SlipOpinion("test", "test", cal.getTime(), "S");
        TreeSet<StatuteCitation> codeCitationTree = new TreeSet<StatuteCitation>();
        TreeSet<OpinionSummary> caseCitationTree = new TreeSet<OpinionSummary>();
        
        codeCitationParser.parseSentence(slipOpinion.getOpinionKey(), sentence, codeCitationTree, caseCitationTree, null);
        System.out.println(codeCitationTree);
	}
	
	public OpJpaTest() throws Exception {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	
	public void testViewModel(
			List<SlipOpinion> cases, 	
			CodesInterface codesInterface, 
			boolean compressCodeReferences, 
			int levelOfInterest
	) throws Exception {
//		List<OpinionView> viewModelCases = new ArrayList<OpinionView>();
//		OpinionViewBuilder viewBuilder = new OpinionViewBuilder(codesInterface);
//		OpinionQueries dbFacade = new OpinionQueries(em);
		// copy to ParsedCase 
		for( SlipOpinion slipOpinion: cases ) {
			
			System.out.println(
				"Case = " + slipOpinion.getFileName() 
				+ " CaseCitations = " + slipOpinion.getOpinionCitations().size() 
				+ " CaseReferrees = " + slipOpinion.getCountReferringOpinions()
				+ " CodeCitations = " + slipOpinion.getStatuteCitationKeys().size()
			);
//			System.out.println("Case = " + slipOpinion.getName() + " CaseCitations = " + slipOpinion.getCaseCitations());
//			ParserResults parserResults = new ParserResults(slipOpinion, dbFacade);
//			OpinionView viewModelCase = viewBuilder.buildOpinionView(slipOpinion, parserResults, compressCodeReferences);
//			viewModelCase.trimToLevelOfInterest(levelOfInterest, false);
//			viewModelCases.add(viewModelCase);
//	        OpinionReport.printOpinionReport(codesInterface, parserResults, slipOpinion );
			
		}

/*		
        JAXBContext jaxbContext = JAXBContext.newInstance(OpinionView.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(viewModelCases, new File("xml/ReportDocument.xml"));
*/        
		
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
			
//			OpinionQueries dbFacade = new OpinionQueries(em);
			SlipOpinionRepository slipOpinionRepository = new SlipOpinionRepository();
			slipOpinionRepository.setEntityManager(em);
			
	//		SlipOpinionDao slipOpinionDao = new SlipOpinionDao(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			for( SlipOpinion slipOpinion: opinions ) {
				System.out.println("Case = " + slipOpinion.getFileName());
//				if ( slipOpinion.getFileName().contains("143650") ) {
					ParserResults parserResults = parser.parseCase(caseParserInterface.getCaseFile(slipOpinion, false), slipOpinion, slipOpinion.getOpinionKey() );
		        	parserResults.persist(slipOpinion, slipOpinionRepository.getPersistenceInterface());
		        	em.persist(slipOpinion);
//				}
	/*        	
	        	SlipOpinion existingOpinion = slipOpinionDao.find(slipOpinion.getOpinionSummaryKey());
	            if (  existingOpinion != null ) {
	            	// i guess it should never get here.
	                existingOpinion.addModifications(slipOpinion, parserResults);
	                existingOpinion.addOpinionSummaryReferredFrom(slipOpinion.getOpinionSummaryKey());
	                slipOpinionDao.merge(existingOpinion);
	            } else {
	            	slipOpinionDao.persist(slipOpinion);
	            }
	*/            
			}
			tx.commit();
			// persist
		} finally {
			emf.close();
		}
	}

	public void runAllSlipOpinions() throws Exception {
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
					Date cDate = slipOpinion.getOpinionDate();
					if ( cDate.compareTo(cal.getTime()) != 0 ) {
						ccit.remove();
					}
				}
			}

			System.out.println("Cases = " + opinions.size() );
			Date startTime = new Date();
			
			// Create the CACodes list
		    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
			
	//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
			// CodeTitles[] codeTitles = codesInterface.getCodeTitles();
			// CodeCitationParser parser = new CodeCitationParser(codeTitles);
			
			PrintOpinionReport opinionReport = new PrintOpinionReport();
			
			for( SlipOpinion slipOpinion: opinions ) {
				if ( slipOpinion.getFileName().equals("C071776") ) continue;
				if ( slipOpinion.getFileName().equals("B264460") ) continue;
				if ( slipOpinion.getFileName().equals("D066715") ) continue;
				if ( slipOpinion.getFileName().equals("A142502") ) continue;
				if ( slipOpinion.getFileName().equals("A143043N") ) continue;
				if ( slipOpinion.getFileName().equals("A143043M") ) continue;
				if ( slipOpinion.getFileName().equals("A142485") ) continue;
								
//				System.out.println("Case = " + slipOpinion.getFileName());
				opinionReport.printOpinionReport(codesInterface, em, slipOpinion.getOpinionKey());
//				if ( slipOpinion.getFileName().contains("143650") ) {
//					ParserResults parserResults = parser.parseCase(caseParserInterface.getCaseFile(slipOpinion, false), slipOpinion, slipOpinion.getOpinionKey() );
//				}
			}
			// persist
			System.out.println("Processed " + opinions.size() + " cases in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds.");
		} finally {
			em.close();
			emf.close();
		}
	}
/*
	public static void writeReportXML(String fileName, List<OpinionView> cases)
	        throws ParserConfigurationException,
	        TransformerException,
	        IOException
	{
	    Element rootElement;

	    File file = new File( fileName );
	    // root elements
	    Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	    rootElement = xmlDoc.createElement("cases");
	    xmlDoc.appendChild(rootElement);

	    for( SlipOpinion slipOpinion: cases ) {
	        rootElement.appendChild( slipOpinion.createXML(xmlDoc) );
	    }

	    // write the content into xml file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(xmlDoc);
	    StreamResult result = new StreamResult( new FileOutputStream( file ) );

	    // Output to console for testing
	    // StreamResult result = new StreamResult(System.out);
	    transformer.transform(source, result);

	    result.getOutputStream().close();
	}
*/

}
