package opjpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import load.LoadHistoricalOpinions;
import parser.ParserInterface;
import statutesca.factory.CAStatutesFactory;

public class LoadOpinions {

	private EntityManagerFactory emf;

	public static void main(String[] args) throws Exception {
		new LoadOpinions().run();
	}
	
	public LoadOpinions() {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	
	private void run() throws Exception {
	
	    ParserInterface parserInterface = CAStatutesFactory.getInstance().getParserInterface(true);
    	LoadHistoricalOpinions load = new LoadHistoricalOpinions(emf, parserInterface);
        try {
        	load.initializeDB();
        } finally {
        	emf.close();
        }
	}
        
}
