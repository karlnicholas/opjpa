package opjpa;

import java.io.File;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
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
	
	    CodesInterface codesInterface = new CACodes();
        codesInterface.loadXMLCodes(new File(LoadOpinions.class.getResource("/xmlcodes").getFile()));
    	LoadHistoricalOpinions load = new LoadHistoricalOpinions(emf, codesInterface);
        try {
        	load.initializeDB();
        } finally {
        	emf.close();
        }
	}
        
}
