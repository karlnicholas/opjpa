package clread.memorydb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
import opinions.model.StatuteCitation;
import opinions.model.StatuteCitationKey;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;
import opinions.parsers.ParserResults.PersistenceInterface;

public class DatabaseFacade implements PersistenceInterface {
    
    protected Database dataBase;

    private DatabaseFacade(){
        dataBase = new Database();
    }
    private static class SingletonHelper {
        private static final DatabaseFacade INSTANCE = new DatabaseFacade();
    }
    public static DatabaseFacade getInstance(){
        return SingletonHelper.INSTANCE;
    }
    
    public int getCount() {
        return dataBase.getStatuteTable().size();
    }

    public List<StatuteCitation> selectForCode(String code) {
        List<StatuteCitation> statutesForCode = Collections.synchronizedList(new ArrayList<StatuteCitation>());
        dataBase.getStatuteTable().parallelStream().filter(new Predicate<StatuteCitation>() {
            @Override
            public boolean test(StatuteCitation codeCitation) {
            	if ( codeCitation.getKey().getCode() == null ) return false;
                return codeCitation.getKey().getCode().contains(code);
            }
        }).forEach(new Consumer<StatuteCitation>() {
            @Override
            public void accept(StatuteCitation codeCitation) {
                statutesForCode.add(codeCitation);
            }
        });
        return statutesForCode;
    }

    public StatuteCitation findStatuteByCodeSection(String code, String sectionNumber) {
        return findStatute(new StatuteCitationKey(code, sectionNumber));
    }

	@Override
	public StatuteCitation findStatute(StatuteCitationKey key) {
		return findStatuteByStatute(new StatuteCitation(key));
	}

	public StatuteCitation findStatuteByStatute(StatuteCitation statuteCitation) {
        StatuteCitation foundCitation = dataBase.getStatuteTable().floor(statuteCitation);
        if ( statuteCitation.equals(foundCitation)) return foundCitation;
        return null;
	}    

	@Override
	public void persistStatute(StatuteCitation statuteCitation) {
		dataBase.getStatuteTable().add(statuteCitation);
	}

	@Override
	public void mergeStatute(StatuteCitation statuteCitation) {
		// merge has already happened in the object itself
		dataBase.getStatuteTable().add(statuteCitation);
	}

	@Override
	public OpinionSummary findOpinion(OpinionSummaryKey key) {
        OpinionSummary tempOpinion = new OpinionSummary(key);
        if ( dataBase.getOpinionTable().contains(tempOpinion))
        	return dataBase.getOpinionTable().floor(tempOpinion);
        else return null;
	}

	@Override
	public void persistOpinion(OpinionSummary opinionSummary) {
		dataBase.getOpinionTable().add(opinionSummary);
	}

	@Override
	public void mergeOpinion(OpinionSummary opinionSummary) {
		dataBase.getOpinionTable().add(opinionSummary);
	}

	public ConcurrentSkipListSet<OpinionSummary> getAllOpinions() {
        return dataBase.getOpinionTable();
    }

    public void writeToXML() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Database.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
//        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(dataBase, new File("c:/users/karl/downloads/Database.xml"));
    }

    public void initializeDB( CodesInterface codesInterface) throws Exception {
//        readStream("c:/users/karl/downloads/calctapp.tar.gz", codesInterface);
        readStream("c:/users/karl/downloads/cal.tar.gz", codesInterface);
//        linkAllReferredFrom(statuteFacade);
    }
    public void initFromXML() throws Exception {
        File file = new File("c:/users/karl/downloads/Database.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Database.class);
      
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        dataBase = (Database) jaxbUnmarshaller.unmarshal(file);

//        addPrimaryReferringOpinions();
//        linkAllReferredFrom(statuteFacade);
      }
/*
    private void addPrimaryReferringOpinions() {
        // need to do this later.
        dataBase.getOpinionTable().parallelStream().forEach(new Consumer<OpinionSummary>() {
            @Override
            public void accept(OpinionSummary opinionSummary) {
                for (StatuteCitation statuteCitation: opinionSummary.getStatuteCitations()) {
                	statuteCitation.incRefCount(opinionSummary.getKey(), 1);
                }
            }
        });
    }
*/    
/*    
      private void linkAllReferredFrom(PersistenceFacade statuteFacade) {
          Object lock = new Object();
          // need to do this later.
          dataBase.getOpinionTable().parallelStream().forEach(new Consumer<OpinionSummary>() {
              @Override
              public void accept(OpinionSummary opinionSummary) {
                  for ( OpinionSummary caseCitation: opinionSummary.getOpinionCitations() ) {
                      if ( dataBase.getOpinionTable().contains(new OpinionSummary(caseCitation.getKey()))) {
                          synchronized(lock) {
                              OpinionSummary referredToOpinion = dataBase.getOpinionTable().floor(new OpinionSummary(caseCitation.getKey()));
                              referredToOpinion.addOpinionSummaryReferredFrom(opinionSummary.getKey());
                          }
                      }
                  }
              }
          });
      }
*/      
    private void readStream(
        String fileName, 
        CodesInterface codesInterface 
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
/*
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
  */                
                      try {
                          processCourtListener(op, parser, codesInterface, lock);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }                      
                  }
/*             
              });
              clOps.clear();
*/          
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
                    op.getCourt()
                );
        	ParserResults parserResults = parser.parseCase(parserDocument, opinionSummary, opinionSummary.getKey());
            synchronized(lock) {
            	parserResults.persist(opinionSummary, this);
        		OpinionSummary existingOpinion = findOpinion(opinionSummary.getOpinionSummaryKey());
                if (  existingOpinion != null ) {
                    existingOpinion.addModifications(opinionSummary, parserResults);
                    existingOpinion.addOpinionSummaryReferredFrom(opinionSummary.getOpinionSummaryKey());
                    mergeOpinion(existingOpinion);
                } else {
                	persistOpinion(opinionSummary);
                }
            }
        }
    }
}
