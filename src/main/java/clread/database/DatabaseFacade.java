package clread.database;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
import opinions.model.SlipOpinion;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.model.StatuteCitationKey;
import opinions.parsers.CodeCitationParser;
import opinions.parsers.ParserDocument;
import opinions.parsers.ParserResults;

public class DatabaseFacade extends opinions.facade.DatabaseFacade {
	private EntityManagerFactory emf;
	private EntityManager em;

    
    public DatabaseFacade(){
    	super();
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
		setEntityManager(em);
    }

    private static class SingletonHelper {
        private static final DatabaseFacade INSTANCE = new DatabaseFacade();
    }
    public static DatabaseFacade getInstance(){
        return SingletonHelper.INSTANCE;
    }
    
    public Long getCount() {
        return em.createQuery("select count(*) from StatuteCitation", Long.class).getSingleResult();
    }

    public List<StatuteCitation> selectForCode(String code) {
    	return em.createQuery("select s from StatuteCitation s where :code in s.key.code", StatuteCitation.class)
    			.setParameter("code", code)
    			.getResultList();
    }

    public StatuteCitation findStatuteByCodeSection(String code, String sectionNumber) {
    	return em.createQuery("select from StatuteCitation s where s.key.code = :code and s.key.sectionNumber = :sectionNumber", StatuteCitation.class)
    			.setParameter("code", code)
    			.setParameter("sectionNumber", sectionNumber)
    			.getSingleResult();
    }

	@Override
	public StatuteCitation findStatute(StatuteCitationKey key) {
    	return em.find(StatuteCitation.class, key);
	}

	public StatuteCitation findStatuteByStatute(StatuteCitation statuteCitation) {
    	return em.find(StatuteCitation.class, statuteCitation.getKey());
	}    
	public SlipOpinion findSlipOpinionBySummaryKey(OpinionSummaryKey opinionSummaryKey) {
		return em.createQuery("select o from SlipOpinion o where o.opinionSummaryKey = :key", SlipOpinion.class)
				.setParameter("key", opinionSummaryKey)
				.getSingleResult();
	}

	public List<OpinionSummary> getAllOpinions() {
        return em.createQuery("select from OpinionSummary", OpinionSummary.class).getResultList();
    }

    public void initializeDB( CodesInterface codesInterface) throws Exception {
//        readStream("c:/users/karl/downloads/calctapp.tar.gz", codesInterface);
        readStream("c:/users/karl/downloads/cal.tar.gz", codesInterface);
//        linkAllReferredFrom(statuteFacade);
    }

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
          
//          List<CourtListenerOpinion> clOps = new ArrayList<CourtListenerOpinion>(1000); 
  
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
