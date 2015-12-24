
import javax.persistence.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import codesparser.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import load.InterfacesFactory;
import opinions.facade.*;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.view.*;
import opinions.parsers.*;

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
		CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		codesInterface.loadXMLCodes(new File(OpJpaTest.class.getResource(xmlcodes).getFile()));
		
		opJpa.testViewModel(
				opJpa.readCasesFromDatabase(), 
				codesInterface, 
				true, 
				2);
		
		
		/*		
		opJpa.reloadDatabase();

		opJpa.refreshDownloads();

		opJpa.loadAndPersistCases();

		opJpa.testViewModel(
				opJpa.loadTestCases(),  
				codesInterface, 
				true, 
				2);
++
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
		List<OpinionSummary> courtCases = caseParserInterface.parseCaseList(reader);
		reader.close();

		System.out.println("Cases = " + courtCases.size() );
		// Create the CACodes list
	    CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);

		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		DatabaseFacade dbFacade = new DatabaseFacade(em);
		
		for( OpinionSummary courtCase: courtCases ) {
			parser.parseCase(dbFacade, caseParserInterface.getCaseFile(courtCase, false), courtCase );
			em.persist(courtCase);
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
		
		CaseParserInterface onlinecaseParser = InterfacesFactory.getCaseParserInterface();
		Reader reader = onlinecaseParser.getCaseList();
		reader = saveCopyOfCaseList(reader);
		List<OpinionSummary> onlineCases = onlinecaseParser.parseCaseList(reader);
		reader.close();
		
		DatabaseFacade dbFacade = new DatabaseFacade(em);
		List<OpinionSummary> databaseCases = dbFacade.listCases();

		// first to deletes
		for ( OpinionSummary opinionSummary: onlineCases ) {
			if ( fileNames.contains(opinionSummary.getName()+".DOC")) fileNames.remove(opinionSummary.getName()+".DOC");
			if ( databaseCases.contains(opinionSummary)) databaseCases.remove(opinionSummary);
		}
		for ( OpinionSummary opinionSummary: databaseCases ) {
			em.remove(opinionSummary);
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
			OpinionSummary opinionSummary = new OpinionSummary(caseName.replace(".DOC", ""), "title", cal.getTime(), cal.getTime(), "CA/4" );
			if ( onlineCases.contains(opinionSummary)) onlineCases.remove(opinionSummary);
		}
		
	    CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);

		//		System.out.println(onlineCases);
		// EntityTransaction tx = em.getTransaction();
		// tx.begin();
		for( OpinionSummary opinionSummary: onlineCases ) {
			parser.parseCase(dbFacade, onlinecaseParser.getCaseFile(opinionSummary, true), opinionSummary );
			em.persist(opinionSummary);
			System.out.println("Downloaded " + opinionSummary.getName() + ".DOC");
		}
		// tx.commit();
		
	}
	
	public void loadAndPersistCases() throws Exception {
		List<OpinionSummary> ccases = loadTestCases();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		for(OpinionSummary opinionSummary: ccases) {
			em.persist(opinionSummary);
		}
		tx.commit();
	}

//	private String[] terms = {"section", "§" , "sections", "§§"};
	public void playParse(CodesInterface codesInterface) throws Exception {
		CodeCitationParser codeCitationParser = new CodeCitationParser(codesInterface.getCodeTitles());
		String sentence = "(welf. & inst. code, §§ 4501; see also welf. & inst. code, § 4434.)";
		Calendar cal = Calendar.getInstance();
		cal.set(1960, Calendar.JUNE, 1);
		OpinionSummary opinionSummary = new OpinionSummary("test", "test", cal.getTime(), cal.getTime(), "S");
        TreeSet<StatuteCitation> codeCitationTree = new TreeSet<StatuteCitation>();
        TreeSet<OpinionSummary> caseCitationTree = new TreeSet<OpinionSummary>();
        
        codeCitationParser.parseSentence(opinionSummary, sentence, codeCitationTree, caseCitationTree);
        System.out.println(codeCitationTree);
	}
	
	public OpJpaTest() throws Exception {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}
	
	
	public void testViewModel(
			List<OpinionSummary> cases, 	
			CodesInterface codesInterface, 
			boolean compressCodeReferences, 
			int levelOfInterest
	) throws Exception {
		List<OpinionCase> viewModelCases = new ArrayList<OpinionCase>();
		OpinionCaseBuilder viewBuilder = new OpinionCaseBuilder(codesInterface); 
		// copy to ParsedCase 
		for( OpinionSummary opinionSummary: cases ) {
			
			System.out.println("Case = " + opinionSummary.getName() + " CaseCitations = " + opinionSummary.getOpinionCitationKeys().size() + " CodeCitations = " + opinionSummary.getStatuteCitationKeys().size());
//			System.out.println("Case = " + opinionSummary.getName() + " CaseCitations = " + opinionSummary.getCaseCitations());

			OpinionCase viewModelCase = viewBuilder.buildParsedCase(opinionSummary, compressCodeReferences);
			viewModelCase.trimToLevelOfInterest(levelOfInterest, false);
			viewModelCases.add(viewModelCase);
		}

		writeReportXML("xml/DocumentReport.xml", viewModelCases);
		
		
	}


	public List<OpinionSummary> loadTestCases() throws Exception {
	    // Test case
	    Calendar cal = GregorianCalendar.getInstance();
	    cal.set(2014, Calendar.JULY, 7, 0, 0, 0 );
	    cal.set(Calendar.MILLISECOND, 0);
	    
//		CaseParserInterface caseParserInterface = InterfacesFactory.getCaseParserInterface(); 
		CaseParserInterface caseParserInterface = new CATestCases(); 

		Reader reader = caseParserInterface.getCaseList();
		List<OpinionSummary> courtCases = caseParserInterface.parseCaseList(reader);
		reader.close();

		// trim list to available test cases
		Iterator<OpinionSummary> ccit = courtCases.iterator();
		while ( ccit.hasNext() ) {
			OpinionSummary opinionSummary = ccit.next();
			if ( DEBUGFILE != null && !DEBUGFILE.equals("ALL") ) {
				if ( !opinionSummary.getName().equals(DEBUGFILE)) ccit.remove();
			} else if (DEBUGFILE != null && DEBUGFILE.equals("ALL")) {
				File tFile = new File(CATestCases.casesDir + opinionSummary.getName() + ".DOC");
				if ( !tFile.exists() ) ccit.remove();
			} else {
				Date cDate = opinionSummary.getPublishDate();
				if ( cDate.compareTo(cal.getTime()) != 0 ) {
					ccit.remove();
				}
			}
		}
		System.out.println("Cases = " + courtCases.size() );
		// Create the CACodes list
	    CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		
//	    QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);
		
		for( OpinionSummary courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());

			parser.parseCase(caseParserInterface.getCaseFile(courtCase, false), courtCase );
		}
		// persist
		return courtCases;
	}

	public static void writeReportXML(String fileName, List<OpinionCase> cases)
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

	    for( OpinionSummary opinionSummary: cases ) {
	        rootElement.appendChild( opinionSummary.createXML(xmlDoc) );
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

	private static Reader saveCopyOfCaseList(Reader reader) throws Exception {
		
	    File file = new File(caseListFile);
	    file.createNewFile();
	    
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream( file ), encoding));
	    CharArrayWriter cWriter = new CharArrayWriter(); 
		char[] cbuf = new char[8192];
		int len;
		while ( (len = reader.read(cbuf, 0, cbuf.length)) != -1 ) {
			writer.write(cbuf, 0, len);
			cWriter.write(cbuf, 0, len);
		}
		reader.close();
		writer.close();
		cWriter.close();
	    return new BufferedReader( new CharArrayReader(cWriter.toCharArray()) );
	}

	public List<OpinionSummary> readCasesFromDatabase() throws Exception {

	    return new DatabaseFacade(em).listCases();
/*		
	    DatabaseFacade databaseFacade = ;
		List<OpinionSummary> courtCases = databaseFacade.listCases();
		for( OpinionSummary courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());
		}
		
		return courtCases;
*/
	}

}
