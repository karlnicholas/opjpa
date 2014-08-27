

import javax.persistence.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.w3c.dom.*;

import codesparser.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.*;
import java.util.Calendar;
import java.util.logging.Logger;

import load.InterfacesFactory;
import opinions.facade.*;
import opinions.model.courtcase.CourtCase;
import opinions.model.opinion.*;
import opinions.parsers.*;

public class OpJpaTest {
	
	private static Logger log = Logger.getLogger(OpJpaTest.class.getName());
	private Scheduler sched;
	private EntityManagerFactory emf;
	private EntityManager em;

	public final static String caseListFile = "html/60days.html";
	public final static String casesDir = "cases/";
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
				opJpa.loadTestCases(),  
				codesInterface, 
				true, 
				2);
/*		
		opJpa.testViewModel(
			opJpa.readCasesFromDatabase(), 
			codesInterface, 
			true, 
			2);
*/			
	}
	
	public OpJpaTest() throws Exception {
//		emf = Persistence.createEntityManagerFactory("opjpa");
//		em = emf.createEntityManager();
	}
	
	
	public void testViewModel(
			List<CourtCase> cases, 	
			CodesInterface codesInterface, 
			boolean compressCodeReferences, 
			int levelOfInterest
	) throws Exception {
		List<OpinionCase> viewModelCases = new ArrayList<OpinionCase>();
		OpinionCaseBuilder viewBuilder = new OpinionCaseBuilder(codesInterface); 
		// copy to ParsedCase 
		for( CourtCase ccase: cases ) {

			OpinionCase viewModelCase = viewBuilder.buildParsedCase(ccase, compressCodeReferences);
			viewModelCase.trimToLevelOfInterest(levelOfInterest);
			viewModelCases.add(viewModelCase);
		}

		writeReportXML("xml/DocumentReport.xml", viewModelCases);
		
		
	}

	private void runUpdateScheduler() throws Exception {

	    // First we must get a reference to a scheduler
	    SchedulerFactory sf = new StdSchedulerFactory();
	    sched = sf.getScheduler();

	    log.info("------- Scheduling Job  -------------------");

	    // define the job and tie it to our HelloJob class
	    JobDetail job = newJob(HelloJob.class).withIdentity("job1", "group1").build();

	    Date startTime = DateBuilder.nextGivenSecondDate(null, 15);
	    
	    // Trigger the job to run on the next round minute
	    SimpleTrigger trigger = newTrigger().withIdentity("trigger3", "group1").startAt(startTime)
		        .withSchedule(simpleSchedule().withIntervalInSeconds(20).withRepeatCount(0)).build();
		  
	    Date ft = sched.scheduleJob(job, trigger);

	    log.info(job.getKey() + " will run at: " + ft + " and repeat: " + trigger.getRepeatCount() + " times, every "
	             + trigger.getRepeatInterval() / 1000 + " seconds");


	    // Start up the scheduler (nothing can actually run until the
	    // scheduler has been started)
	    sched.start();

	    log.info("------- Started Scheduler -----------------");
	    // wait long enough so that the scheduler as an opportunity to
	    // run the job!
	    log.info("------- Waiting 65 seconds... -------------");
	    try {
		      // wait 65 seconds to show job
		      Thread.sleep(65L * 1000L);
		      // executing...
		    } catch (Exception e) {
		      //
		    }
        sched.shutdown();
	}
	

	public static class HelloJob implements Job {

		private static EntityManagerFactory emf;
		
		public HelloJob() {
			emf = Persistence.createEntityManagerFactory("opjpa");
		}

		@Override
	    public void execute(JobExecutionContext context) throws JobExecutionException {
			EntityManager em = emf.createEntityManager();
			try {
				CasesFacade casesFacade = new CasesFacade(
						em, 
						InterfacesFactory.getCaseParserInterface(), 
						InterfacesFactory.getCodesInterface()
					);
				casesFacade.updateDatabase();
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				em.close();
			}
	    }
	}

	public List<CourtCase> loadTestCases() throws Exception {
	    // Test case
	    Calendar cal = GregorianCalendar.getInstance();
	    cal.set(2014, Calendar.JULY, 7, 0, 0, 0 );
	    cal.set(Calendar.MILLISECOND, 0);
	    
//		CaseParserInterface caseParserInterface = InterfacesFactory.getCaseParserInterface(); 
		CaseParserInterface caseParserInterface = new CATestCases(); 

		Reader reader = caseParserInterface.getCaseList();
		List<CourtCase> courtCases = caseParserInterface.parseCaseList(reader);
		reader.close();

		// trim list to available test cases
		Iterator<CourtCase> ccit = courtCases.iterator();
		while ( ccit.hasNext() ) {
			CourtCase ccase = ccit.next();
			if ( DEBUGFILE != null && !DEBUGFILE.equals("ALL") ) {
				if ( !ccase.getName().equals(DEBUGFILE)) ccit.remove();
			} else if (DEBUGFILE != null && DEBUGFILE.equals("ALL")) {
				File tFile = new File(casesDir + ccase.getName() + ".DOC");
				if ( !tFile.exists() ) ccit.remove();
			} else {
				Date cDate = ccase.getPublishDate();
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
		
		for( CourtCase courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());

			InputStream inputStream = caseParserInterface.getCaseFile(courtCase);
//			inputStream = saveCopyOfCase(casesDir, courtCase.getName()+".DOC", inputStream);
			parser.parseCase(courtCase, inputStream);
			inputStream.close();

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

	    for( CourtCase ccase: cases ) {
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
	    	byte[] bytes = new byte[2^13];
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
		char[] cbuf = new char[2^13];
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

	public List<CourtCase> readCasesFromDatabase() throws Exception {
	    Calendar cal = GregorianCalendar.getInstance();
	    cal.set(2014, Calendar.AUGUST, 12 );

	    DatabaseFacade databaseFacade = new DatabaseFacade(em);
		List<CourtCase> courtCases = databaseFacade.findByPublishDate(cal.getTime());
		
		for( CourtCase courtCase: courtCases ) {
			System.out.println("Case = " + courtCase.getName());
		}
		
		return courtCases;
	}

}
