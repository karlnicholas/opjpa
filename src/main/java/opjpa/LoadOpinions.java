package opjpa;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
import load.LoadHistoricalOpinions;
import load.LoadOpinionsThreaded;
import opinions.facade.DatabaseFacade;

public class LoadOpinions {

	private EntityManagerFactory emf;
	private EntityManager em;

	public static void main(String[] args) throws Exception {
		new LoadOpinions().run();
	}
	
	public LoadOpinions() {
		emf = Persistence.createEntityManagerFactory("opjpa");
		em = emf.createEntityManager();
	}

	private void run() throws Exception {
	
//      String iface = "code.CACodes";
//      CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
	    CodesInterface codesInterface = new CACodes();
        codesInterface.loadXMLCodes(new File(LoadOpinions.class.getResource("/xmlcodes").getFile()));
        DatabaseFacade facade = new DatabaseFacade(em);
        LoadHistoricalOpinions load = new LoadHistoricalOpinions(facade, codesInterface);
        try {
        	load.initializeDB(em);
        } finally {
        	emf.close();
        }
	}
        
}
