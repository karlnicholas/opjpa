package opjpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import codesparser.CodesInterface;
import gscalifornia.factory.CAStatutesFactory;
import load.LoadHistoricalOpinions;

public class LoadOpinions {

	private EntityManagerFactory emf;

	public static void main(String[] args) throws Exception {
		new LoadOpinions().run();
	}
	
	public LoadOpinions() {
		emf = Persistence.createEntityManagerFactory("opjpa");
	}
	
	private void run() throws Exception {
	
	    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
    	LoadHistoricalOpinions load = new LoadHistoricalOpinions(emf, codesInterface);
        try {
        	load.initializeDB();
        } finally {
        	emf.close();
        }
	}
        
}
