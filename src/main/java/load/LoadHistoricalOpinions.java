package load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

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
import opinions.model.OpinionSummaryKey;
import opinions.model.OpinionSummary;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;
import opinions.parsers.ParserResults.PersistenceInterface;

public class LoadHistoricalOpinions {
	
	private PersistenceInterface persistence;
	private CodesInterface codesInterface;
    
    public LoadHistoricalOpinions(
		PersistenceInterface persistence, 
		 CodesInterface codesInterface
	){
    	this.persistence = persistence;
    	this.codesInterface = codesInterface;
    }

    public void initializeDB() throws Exception {
        readStream("c:/users/karl/downloads/calctapp.tar.gz");
        readStream("c:/users/karl/downloads/cal.tar.gz");
    }

    private void readStream(
        String fileName 
    ) throws Exception {
      ObjectMapper om = new ObjectMapper();
      Object lock = new Object();
      CodeCitationParser parser = new CodeCitationParser(codesInterface.getCodeTitles());

      TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(
              new FileInputStream(fileName))));        
      try {

          TarArchiveEntry entry;
          
          List<CourtListenerOpinion> clOps = new ArrayList<CourtListenerOpinion>(1000); 
  
          while ((entry = tarIn.getNextTarEntry()) != null) {
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
                  if ( clOps.size() == 1000 ) {
                      clOps.parallelStream().forEach(new Consumer<CourtListenerOpinion>() {
                          @Override
                          public void accept(CourtListenerOpinion op) {
                              try {                              
                                  processCourtListener(op, parser, codesInterface, lock);
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          }
                      });
                      // remove processed cases from the list
                      clOps.clear();
                  }
              }
          }
          if ( clOps.size() > 0 ) {
              clOps.parallelStream().forEach(new Consumer<CourtListenerOpinion>() {
                  @Override
                  public void accept(CourtListenerOpinion op) {

                      try {
                          processCourtListener(op, parser, codesInterface, lock);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }                      
                  }
             
              });
              clOps.clear();
          }
      } finally {
          tarIn.close();
      }
    }
    
    private void processCourtListener(
            CourtListenerOpinion op, 
            CodeCitationParser parser,
            CodesInterface codesInterface,
            Object lock
    ) throws Exception {
        DateFormat clFormat = new SimpleDateFormat("YYYY-mm-dd");
        
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
                    new OpinionSummaryKey(name),
                    op.getCitation().getCaseName(),
                    dateFiled, 
                    ""
                );
        	ParserResults parserResults = parser.parseCase(parserDocument, opinionSummary, opinionSummary.getKey());
            synchronized(lock) {
            	parserResults.persist(opinionSummary, persistence);
        		OpinionSummary existingOpinion = persistence.findOpinion(opinionSummary.getOpinionSummaryKey());
                if (  existingOpinion != null ) {
                    existingOpinion.addModifications(opinionSummary, parserResults);
                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionSummaryKey());
                    persistence.mergeOpinion(existingOpinion);
                } else {
                	persistence.persistOpinion(opinionSummary);
                }
            }
        }
    }

}
