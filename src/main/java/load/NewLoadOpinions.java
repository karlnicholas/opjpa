package load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import clread.jsonmodel.CourtListenerOpinion;
import clread.memorydb.MemoryDBFacade;
import codesparser.CodesInterface;
import opinions.facade.DatabaseFacade;
import opinions.model.OpinionKey;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;

public class NewLoadOpinions {
	EntityManagerFactory emf;
	CodeCitationParser parser;
	
	public NewLoadOpinions(
		EntityManagerFactory emf, 
		CodesInterface codesInterface 
	) {
		this.emf = emf;
		parser = new CodeCitationParser(codesInterface.getCodeTitles());
	}
    
    public void readStream(
        String fileName 
    ) throws Exception {
      ObjectMapper om = new ObjectMapper();

      TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(
              new FileInputStream(fileName))));
      try {
    	  List<CourtListenerOpinion> clOps;
    	  int count = 0;
		  Date startTime = new Date();
    	  while ( true ) {
    		  clOps = get250cases(tarIn, om);
    		  int read = clOps.size();
    		  count = count + read;
    		  if ( read == 0 ) break;
			  MemoryDBFacade memoryDB = buildMemoryDB(clOps);
			  persistMemory(memoryDB);
    		  Date endTime = new Date();
			  System.out.println("count " + count + " : " + (endTime.getTime()-startTime.getTime())/1000);
	      }
      } finally {
          tarIn.close();
      }
    }
    
    private void persistMemory(MemoryDBFacade memoryDB) {
    	EntityManager em = emf.createEntityManager();
    	em.getTransaction().begin();
    	DatabaseFacade database = new DatabaseFacade(em);
    	Set<OpinionSummary> opinions = memoryDB.getAllOpinions();
    	for(OpinionSummary opinion: opinions ) {
    		OpinionSummary existingOpinion = database.opinionExists(opinion.getOpinionKey());
    		if ( existingOpinion != null ) {
    			existingOpinion.addModifications(opinion, memoryDB);
                existingOpinion.addOpinionSummaryReferredFrom(opinion.getOpinionKey());
    			database.mergeOpinion(existingOpinion);
    		} else {
    			database.persistOpinion(opinion);
    		}
    	}
    	Set<StatuteCitation> statutes = memoryDB.getAllStatutes();
    	for(StatuteCitation statute: statutes ) {
    		StatuteCitation existingStatute = database.statuteExists(statute.getStatuteKey());
    		if ( existingStatute != null ) {
    			existingStatute.addModifications(statute);
    			database.mergeStatute(existingStatute);
    		} else {
    			database.persistStatute(statute);
    		}
    	}
    	em.getTransaction().commit();
    	em.close();
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
    
    private MemoryDBFacade buildMemoryDB(List<CourtListenerOpinion> clOps ) throws Exception {
    	
    	MemoryDBFacade persistence = MemoryDBFacade.getInstance();
    	persistence.clearDB();

    	DateFormat clFormat = new SimpleDateFormat("YYYY-mm-dd");
    	for ( CourtListenerOpinion op: clOps ) {
	        
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
            	parserResults.persist(opinionSummary, persistence);
        		OpinionSummary existingOpinion = persistence.opinionExists(opinionSummary.getOpinionKey());
                if (  existingOpinion != null ) {
                    existingOpinion.addModifications(opinionSummary, persistence);
                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionKey());
                    persistence.mergeOpinion(existingOpinion);
                } else {
                	persistence.persistOpinion(opinionSummary);
                }
	        }
        }
    	clOps.clear();
    	return persistence;
    }

}
