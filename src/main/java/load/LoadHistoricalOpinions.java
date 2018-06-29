package load;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import opca.memorydb.CitationStore;
import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionStatuteCitation;
import opca.model.StatuteCitation;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class LoadHistoricalOpinions {
	private static Logger logger = Logger.getLogger(LoadHistoricalOpinions.class.getName());
	private final CitationStore citationStore;
	private final EntityManager em;
	
//	OpinionDocumentParser parser;
	
	public LoadHistoricalOpinions(
		EntityManager em, 
		ParserInterface parserInterface 
	) {
		this.em = em;
    	citationStore = CitationStore.getInstance();
//		parser = new OpinionDocumentParser(parserInterface.getCodeTitles());
	}
	
    public void initializeDB() throws Exception {
    	Date startTime = new Date();
    	//
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);

	    LoadCourtListenerCallback cb1 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file1 = new LoadCourtListenerFiles(cb1);
	    file1.loadFiles("c:/users/karln/downloads/calctapp-opinions.tar.gz", "c:/users/karln/downloads/calctapp-clusters.tar.gz", 1000);

	    LoadCourtListenerCallback cb2 = new LoadCourtListenerCallback(citationStore, parserInterface);
	    LoadCourtListenerFiles file2 = new LoadCourtListenerFiles(cb2);
	    file2.loadFiles("c:/users/karln/downloads/cal-opinions.tar.gz", "c:/users/karln/downloads/cal-clusters.tar.gz", 1000);

		List<OpinionStatuteCitation> opinionStatuteCitations = new ArrayList<>();

    	for(OpinionBase opinion: citationStore.getAllOpinions() ) {
    		if ( opinion.getStatuteCitations() != null ) {
	    		for ( OpinionStatuteCitation statuteCitation: opinion.getStatuteCitations() ) {
	    			opinionStatuteCitations.add(statuteCitation);
	    		}
    		}
			em.persist(opinion);
    	}

    	for(StatuteCitation statute: citationStore.getAllStatutes() ) {
			em.persist(statute);
    	}
    	for(OpinionStatuteCitation opinionStatuteCitation: opinionStatuteCitations ) {
			em.persist(opinionStatuteCitation);
    	}
    	
    }

}
