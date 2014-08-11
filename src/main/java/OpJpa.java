

import java.io.*;
import java.util.*;

import javax.persistence.*;

import load.InterfacesFactory;
import opinions.facade.DatabaseFacade;
import opinions.model.CourtCase;
import opinions.parsers.*;

import codesparser.*;

public class OpJpa {
	
	private EntityManagerFactory emf;
	private EntityManager em;

	public final static String caseListFile = "html/60days.html";
	public final static String casesDir = "cases/";
	public final static String encoding = "UTF-8";
	
//    private final static int levelOfInterest = 2;
//    private final static boolean compressSections = true;
//    private static String DEBUGFILE = "C067636.DOC";
//	private final static String DEBUGFILE = null;

	public static void main(String[] args) throws Exception {
		OpJpa opJpa = new OpJpa();
		opJpa.readCases();
	}
	
	public OpJpa() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}

	public void readCases() throws Exception {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2014, Calendar.AUGUST, 8 );

        DatabaseFacade opDatabase = new DatabaseFacade(em);
		List<CourtCase> courtCases = opDatabase.findByPublishDate(cal.getTime());
		
		for( CourtCase courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());
			System.out.println("Disposition = " + courtCase.getDisposition());
			System.out.println("Summary = " + courtCase.getSummary());
		}
	}
	public void loadCases() throws Exception {

        // Test case
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2014, Calendar.AUGUST, 8 );
        
//    	CaseParserInterface caseParserInterface = InterfacesFactory.getCaseParserInterface(); 
    	CaseParserInterface caseParserInterface = new CACasesFile(); 

    	Reader reader = caseParserInterface.getCaseList();
    	List<CourtCase> courtCases = caseParserInterface.parseCaseList(reader, cal.getTime());
    	reader.close();
		
    	// Create the CACodes list
        CodesInterface codesInterface = InterfacesFactory.getCodesInterface();
		
//        QueueUtility queue = new QueueUtility(compressSections);  // true is compress references within individual titles
		CodeTitles[] codeTitles = codesInterface.getCodeTitles();
		CodeCitationParser parser = new CodeCitationParser(codeTitles);
		
		for( CourtCase courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());
			
			InputStream inputStream = caseParserInterface.getCaseFile(courtCase);
			parser.parseCase(courtCase, inputStream);
			inputStream.close();

		}
		
		// persist
		
		DatabaseFacade opDatabase = new DatabaseFacade(em);
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		opDatabase.persistCases(courtCases);
		tx.commit();
		System.out.println("Transaction committed.");
		
/*
		File xmlCodes = new File(OpJpa.class.getResource("/xmlcodes").getFile()); 
        codesInterface.loadXMLCodes( xmlCodes );
*/        
	}

	/*

	List<ViewModelCase> ccases = new ArrayList<ViewModelCase>();
	ViewModelCaseBuilder pCaseBuilder = new ViewModelCaseBuilder(codesInterface); 
	// copy to ParsedCase 
	for( Case ccase: cases ) {

		ViewModelCase codesCase = pCaseBuilder.buildParsedCase(ccase, compressSections);
		codesCase.trimToLevelOfInterest(levelOfInterest);
		ccases.add(codesCase);
	}

	writeReportXML("xml/DocumentReport.xml", ccases);
	public static void writeReportXML(String fileName, List<ViewModelCase> cases)
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

        for( Case ccase: cases ) {
            rootElement.appendChild( ccase.createXML(xmlDoc) );
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

	private static InputStream saveCopyOfCase(String directory, String fileName, InputStream inputStream ) throws Exception {
    	
        File file = new File(directory + "/" + fileName);
        file.createNewFile();
        
        OutputStream out = new FileOutputStream( file );
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        try {
        	byte[] bytes = new byte[2^18];
        	int len;
        	while ( (len = inputStream.read(bytes, 0, bytes.length)) != -1 ) {
        		out.write(bytes, 0, len);
        		baos.write(bytes, 0, len);
        	}
        	out.close();
        	baos.close();
	        return new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));

        } finally {
        	inputStream.close();
        }
    }

    private static Reader saveCopyOfCaseList(Reader reader) throws Exception {
    	
        File file = new File(caseListFile);
        file.createNewFile();
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream( file ), encoding));
        CharArrayWriter cWriter = new CharArrayWriter(); 
    	char[] cbuf = new char[2^18];
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
*/
}
