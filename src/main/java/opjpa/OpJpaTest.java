package opjpa;

import javax.persistence.*;

import opca.model.SlipOpinion;
import opca.parser.*;
import opca.scraper.CACaseScraper;
import opca.service.SlipOpinionService;
import parser.ParserInterface;
import scraper.TestCACaseScraper;
import statutes.StatutesTitles;
import statutesca.factory.CAStatutesFactory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OpJpaTest {
	
//	private static Logger log = Logger.getLogger(OpJpaTest.class.getName());
	private EntityManagerFactory emf;
	private EntityManager em;

	public final static String encoding = "UTF-8";
	public final static String xmlcodes = "/xmlcodes"; 
	
//    private final static int levelOfInterest = 2;
//    private final static boolean compressSections = true;
	private final static String DEBUGFILE = "ALL"; // "A140107" or "ALL";

	public static void main(String[] args) throws Exception {
		OpJpaTest opJpa = new OpJpaTest();
//		opJpa.runUpdateScheduler();
		// force loading of XML files
//		CAStatutesFactory.getInstance().getParserInterface(true);
		
/*		
		opJpa.testViewModel(
				opJpa.readCasesFromDatabase(), 
				parserInterface, 
				true, 
				2);
		
*/		

//		opJpa.reloadDatabase();

		opJpa.refreshDownloads();	// good for download all new slipOpinions

//		opJpa.loadAndPersistCases();
		
//		opJpa.runAllSlipOpinions();

/*		
		opJpa.testViewModel(
				opJpa.loadTestCases(),  
				parserInterface, 
				true, 
				2);

		opJpa.testViewModel(
			opJpa.readCasesFromDatabase(), 
			parserInterface, 
			true, 
			2);

*/
		
	}
	
	public void reloadDatabase() throws Exception {

		OpinionScraperInterface caseParser = new TestCACaseScraper(false); 

		List<SlipOpinion> opinions = caseParser.getCaseList();

		System.out.println("Cases = " + opinions.size() );
		// Create the CACodes list
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
		
		StatutesTitles[] statutesTitles = parserInterface.getStatutesTitles();
		OpinionDocumentParser parser = new OpinionDocumentParser(statutesTitles);

		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		List<ScrapedOpinionDocument> parserDocuments = caseParser.scrapeOpinionFiles(opinions);
		for( ScrapedOpinionDocument parserDocument: parserDocuments ) {
			parser.parseOpinionDocument(parserDocument, parserDocument.getOpinionBase(), parserDocument.getOpinionBase().getOpinionKey() );
        	// look for details
        	// after a summaryParagraph is found, don't check any further .. (might have to change)
			
			// look for summary and disposition 
    		parser.parseSlipOpinionDetails((SlipOpinion) parserDocument.getOpinionBase(), parserDocument);

			em.persist((SlipOpinion) parserDocument.getOpinionBase());
		}
		
		tx.commit();

	}
	
	public void refreshDownloads() throws Exception {

		DirectoryStream<Path> files = Files.newDirectoryStream( Paths.get(TestCACaseScraper.casesDir) );
		List<String> fileNames = new ArrayList<String>();
		Iterator<Path> fit = files.iterator();
		while (fit.hasNext() ) {
			fileNames.add(fit.next().toFile().getName());
		}
		List<String> fileNamesCopy = new ArrayList<String>(fileNames); 
		
		OpinionScraperInterface caseScaper = new CACaseScraper(true); 
		List<SlipOpinion> onlineCases = caseScaper.getCaseList();
		
		SlipOpinionService slipOpinionService = new SlipOpinionService();
		slipOpinionService.setEntityManager(em);
		List<SlipOpinion> databaseCases = slipOpinionService.listSlipOpinions();

		// first to deletes
		for ( SlipOpinion slipOpinion: onlineCases ) {
			if ( fileNames.contains(slipOpinion.getFileName()+".DOC")) fileNames.remove(slipOpinion.getFileName()+".DOC");
			if ( databaseCases.contains(slipOpinion)) databaseCases.remove(slipOpinion);
		}
		for ( SlipOpinion slipOpinion: databaseCases ) {
			em.remove(slipOpinion);
		}
		for ( String caseName: fileNames ) {
			Path path = Paths.get(TestCACaseScraper.casesDir, caseName);
			if ( Files.exists(path) ) Files.delete(path);
		}
		// download and save remaining cases
		// which are ...
		Calendar cal = Calendar.getInstance();
		cal.set(1960, Calendar.JUNE, 1);
		for ( String caseName: fileNamesCopy ) {
			String fileExtension = ".DOCX";
			int loc = caseName.indexOf(fileExtension); 
			if ( loc == -1 ) {
				fileExtension = ".DOC";
				loc = caseName.indexOf(fileExtension); 
			}
			SlipOpinion slipOpinion = new SlipOpinion(caseName.replace(fileExtension, ""), fileExtension, "title", cal.getTime(), "CA/4" );
			if ( onlineCases.contains(slipOpinion)) onlineCases.remove(slipOpinion);
		}
		
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
		StatutesTitles[] statutesTitles = parserInterface.getStatutesTitles();
		OpinionDocumentParser parser = new OpinionDocumentParser(statutesTitles);

		//		System.out.println(onlineCases);
		// EntityTransaction tx = em.getTransaction();
		// tx.begin();
		
		List<ScrapedOpinionDocument> parserDocs = caseScaper.scrapeOpinionFiles(onlineCases);
		for( ScrapedOpinionDocument parserDoc: parserDocs ) {
			ParsedOpinionCitationSet parserResults = parser.parseOpinionDocument(parserDoc, parserDoc.getOpinionBase(), parserDoc.getOpinionBase().getOpinionKey() );
//        	parserResults.mergeParsedDocumentCitationsToMemoryDB(slipOpinionService.getPersistenceInterface(), parserDoc.opinionBase);
//			em.persist(slipOpinion);
			System.out.println("Downloaded " + ((SlipOpinion)parserDoc.getOpinionBase()).getFileName() + ".DOC");
			throw new RuntimeException("this was changed");
		}
		// tx.commit();
		
	}
	
	
	/*	
//	private String[] terms = {"section", "§" , "sections", "§§"};
	public void playParse(ParserInterface parserInterface) throws Exception {
		OpinionDocumentParser codeCitationParser = new OpinionDocumentParser(parserInterface.getCodeTitles());
		String sentence = "(welf. & inst. code, §§ 4501; see also welf. & inst. code, § 4434.)";
		Calendar cal = Calendar.getInstance();
		cal.set(1960, Calendar.JUNE, 1);
		SlipOpinion slipOpinion = new SlipOpinion("test", "test", cal.getTime(), "S");
        TreeSet<StatuteCitation> codeCitationTree = new TreeSet<StatuteCitation>();
        TreeSet<OpinionSummary> caseCitationTree = new TreeSet<OpinionSummary>();
        
        codeCitationParser.parseSentence(slipOpinion.getOpinionKey(), sentence, codeCitationTree, caseCitationTree, null);
        System.out.println(codeCitationTree);
	}
*/	
	public OpJpaTest() throws Exception {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	
	public void testViewModel(
			List<SlipOpinion> cases, 	
			ParserInterface parserInterface, 
			boolean compressCodeReferences, 
			int levelOfInterest
	) throws Exception {
//		List<OpinionView> viewModelCases = new ArrayList<OpinionView>();
//		OpinionViewBuilder viewBuilder = new OpinionViewBuilder(parserInterface);
//		OpinionQueries dbFacade = new OpinionQueries(em);
		// copy to ParsedCase 
		for( SlipOpinion slipOpinion: cases ) {
			
			System.out.println(
				"Case = " + slipOpinion.getFileName() 
				+ " CaseCitations = " + slipOpinion.getOpinionCitations().size() 
				+ " CaseReferrees = " + slipOpinion.getCountReferringOpinions()
				+ " CodeCitations = " + slipOpinion.getStatuteCitations().size()
			);
//			System.out.println("Case = " + slipOpinion.getName() + " CaseCitations = " + slipOpinion.getCaseCitations());
//			ParsedOpinionResults parserResults = new ParsedOpinionResults(slipOpinion, dbFacade);
//			OpinionView viewModelCase = viewBuilder.buildOpinionView(slipOpinion, parserResults, compressCodeReferences);
//			viewModelCase.trimToLevelOfInterest(levelOfInterest, false);
//			viewModelCases.add(viewModelCase);
//	        OpinionReport.printOpinionReport(parserInterface, parserResults, slipOpinion );
			
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
		    
	//		OpinionScraperInterface caseScraper = InterfacesFactory.getCaseParserInterface(); 
			OpinionScraperInterface caseParser = new TestCACaseScraper(false); 
			List<SlipOpinion> onlineCases = caseParser.getCaseList();
	
			// trim list to available test cases
			Iterator<SlipOpinion> ccit = onlineCases.iterator();
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
			System.out.println("Cases = " + onlineCases.size() );
			// Create the CACodes list
		    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
			
	//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
			StatutesTitles[] statutesTitles = parserInterface.getStatutesTitles();
			OpinionDocumentParser parser = new OpinionDocumentParser(statutesTitles);
			
//			OpinionQueries dbFacade = new OpinionQueries(em);
			SlipOpinionService slipOpinionService = new SlipOpinionService();
			slipOpinionService.setEntityManager(em);
			
	//		SlipOpinionDao slipOpinionDao = new SlipOpinionDao(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			
			List<ScrapedOpinionDocument> parserDocuments = caseParser.scrapeOpinionFiles(onlineCases);
			for( ScrapedOpinionDocument parserDocument: parserDocuments ) {
//				if ( slipOpinion.getFileName().contains("143650") ) {
					ParsedOpinionCitationSet parserResults = parser.parseOpinionDocument(parserDocument, parserDocument.getOpinionBase(), parserDocument.getOpinionBase().getOpinionKey() );
//		        	parserResults.mergeParsedDocumentCitationsToMemoryDB(slipOpinionService.getPersistenceInterface(), parserDocument.opinionBase);
		        	em.persist((SlipOpinion)parserDocument.getOpinionBase());
					throw new RuntimeException("this was changed");
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
		    
	//		OpinionScraperInterface caseScraper = InterfacesFactory.getCaseParserInterface(); 
			OpinionScraperInterface caseParser = new TestCACaseScraper(false); 
			List<SlipOpinion> onlineCases = caseParser.getCaseList();

			// trim list to available test cases
			Iterator<SlipOpinion> ccit = onlineCases.iterator();
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

			System.out.println("Cases = " + onlineCases.size() );
			Date startTime = new Date();
			
			// Create the CACodes list
//		    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
			
	//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
			// CodeTitles[] statutesTitles = parserInterface.getCodeTitles();
			// OpinionDocumentParser parser = new OpinionDocumentParser(statutesTitles);
			
			PrintOpinionReport opinionReport = new PrintOpinionReport();
			
			for( SlipOpinion slipOpinion: onlineCases ) {
				if ( slipOpinion.getFileName().equals("C071776") ) continue;
				if ( slipOpinion.getFileName().equals("B264460") ) continue;
				if ( slipOpinion.getFileName().equals("D066715") ) continue;
				if ( slipOpinion.getFileName().equals("A142502") ) continue;
				if ( slipOpinion.getFileName().equals("A143043N") ) continue;
				if ( slipOpinion.getFileName().equals("A143043M") ) continue;
				if ( slipOpinion.getFileName().equals("A142485") ) continue;
								
//				System.out.println("Case = " + slipOpinion.getFileName());
				opinionReport.printSlipOpinionReport(em, slipOpinion.getOpinionKey());
//				if ( slipOpinion.getFileName().contains("143650") ) {
//					ParsedOpinionResults parserResults = parser.parseCase(caseScraper.getCaseFile(slipOpinion, false), slipOpinion, slipOpinion.getOpinionKey() );
//				}
			}
			// persist
			System.out.println("Processed " + onlineCases.size() + " cases in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds.");
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
