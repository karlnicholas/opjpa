package load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import clread.jsonmodel.CourtListenerOpinion;
import codesparser.CodesInterface;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionKey;
import opinions.model.OpinionSummary;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;
import opinions.parsers.ParserResults.PersistenceInterface;

public class LoadOpinionsThreaded {
	EntityManagerFactory emf;
	CodeCitationParser parser;
	Object lock;
    
    public void initializeDB(EntityManagerFactory emf, CodesInterface codesInterface) throws Exception {
    	this.emf = emf;
        parser = new CodeCitationParser(codesInterface.getCodeTitles());
        lock = new Object();
        readStream("c:/users/karl/downloads/calctapp.tar.gz");
        readStream("c:/users/karl/downloads/cal.tar.gz");
    }

    private void readStream(
        String fileName 
    ) throws Exception {
      ObjectMapper om = new ObjectMapper();

      TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(
              new FileInputStream(fileName))));        
      List<Thread> threads = new ArrayList<Thread>();
      int tnum = 1;

      try {

          while(true) {
        	  if ( threads.size() < 4 ) {
        		  List<CourtListenerOpinion> clOps = get250cases(tarIn, om);
        		  if ( clOps.size() == 0  ) break;
        		  Thread thread = new ProcessCourtListener(clOps, tnum++);
        		  thread.start();
        		  threads.add(thread);
        	  }
    		  while ( threads.size() >= 4 ) {
    			  Thread.sleep(10000);
            	  Iterator<Thread> thIter = threads.iterator(); 
            	  while (thIter.hasNext() ) {
            		  Thread thread = thIter.next();
            		  if(thread.getState()==Thread.State.TERMINATED){ 
            			  thIter.remove();
            			  System.out.println("thread removed: " + threads.size());
            		  }
            	  }
              } 
          }

      } finally {
          tarIn.close();
      }
    }
    private List<CourtListenerOpinion> get250cases(
		TarArchiveInputStream tarIn, 
		ObjectMapper om
	) throws Exception {
    	TarArchiveEntry entry;
    	int count = 0;
        List<CourtListenerOpinion> clOps = new ArrayList<CourtListenerOpinion>(250); 
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
	            CourtListenerOpinion op = om.readValue(content, CourtListenerOpinion.class);
	            if (op.getPrecedentialStatus().toLowerCase().equals("unpublished"))
	                continue;
	            if (op.getHtmlLawbox() == null)
	                continue;
	
	            clOps.add(op);
	            if ( ++count >= 250 ) break;
	        }
        }
        return clOps;
    }
    
    private class ProcessCourtListener extends Thread {
        private List<CourtListenerOpinion> clOps;
        private int tnum;
    	
	    public ProcessCourtListener (
	            List<CourtListenerOpinion> clOps, 
	            int tnum
	    ) {
	    	this.clOps = clOps;
	    	this.tnum = tnum;
	    }
	    
	    @Override
	    public void run() {
	    	System.out.println("Thread started: " + tnum + ":" + clOps.size());
	    	EntityManager em = emf.createEntityManager();
	    	PersistenceInterface persistence = new DatabaseFacade(em);
	    	EntityTransaction tx = em.getTransaction();
	    	tx.begin();
	    	int count = 0;
	    	System.out.println("Transaction started: " + tnum);
	        DateFormat clFormat = new SimpleDateFormat("YYYY-mm-dd");
	    	try {
		    	System.out.println("Looping started: "+tnum);
		    	for ( CourtListenerOpinion op: clOps ) {
		    		if ( (++count % 50) == 0 ) System.out.println("+"+count+":"+tnum);
			        
			        Document lawBox = Parser.parse(op.getHtmlLawbox(), "");
			        Elements ps = lawBox.getElementsByTag("p");
			
			        ParserDocument parserDocument = new ParserDocument();
			        for (Element p : ps) {
			            String text = p.text();
			            if (text.length() == 0)
			                continue;
			            if (text.charAt(0) == '[' || text.charAt(0) == '(')
			                parserDocument.footnotes.add(text);
			            else
			                parserDocument.paragraphs.add(text);
			        }
			        Date dateFiled = clFormat.parse(op.getDateFiled());
			        String name = op.getCitation().getStateCiteOne();
			        if ( name != null && name.contains("Rptr.") ) name = op.getCitation().getStateCiteTwo();
			        if ( name != null ) {
			            name = name.toLowerCase().replace(". ", ".").replace("app.", "App.").replace("cal.", "Cal.").replace("supp.", "Supp.");
			            OpinionSummary opinionSummary = new OpinionSummary(
			                    new OpinionKey(name),
			                    op.getCitation().getCaseName(),
			                    dateFiled, 
			                    dateFiled, 
			                    ""
			                );
			        	ParserResults parserResults = parser.parseCase(parserDocument, opinionSummary, opinionSummary.getOpinionKey());
//			            synchronized (lock) {
			            	parserResults.persist(opinionSummary, persistence);
			        		OpinionSummary existingOpinion = persistence.opinionExists(opinionSummary.getOpinionKey());
			                if (  existingOpinion != null ) {
			                    existingOpinion.addModifications(opinionSummary, parserResults);
			                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionKey());
			                    persistence.mergeOpinion(existingOpinion);
			                } else {
			                	persistence.persistOpinion(opinionSummary);
			                }
//			            }
			        }
		        }
	    	} catch ( Exception e) {
	    		e.printStackTrace();
	    	}
	    	System.out.println("commiting: "+tnum);
	    	tx.commit();
	    	em.close();
	    	System.out.println("250 processed: "+tnum);
	    	clOps.clear();
	    }
    }

}
