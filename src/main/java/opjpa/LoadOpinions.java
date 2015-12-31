package opjpa;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
import load.LoadHistoricalOpinions;
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
		try {
			DatabaseFacade databaseFacade = new DatabaseFacade(em);
	
	//      String iface = "code.CACodes";
	//      CodesInterface codesInterface = (CodesInterface) Class.forName(iface).newInstance();
		    CodesInterface codesInterface = new CACodes();
	        codesInterface.loadXMLCodes(new File(LoadOpinions.class.getResource("/xmlcodes").getFile()));
	
	        LoadHistoricalOpinions load = new LoadHistoricalOpinions(databaseFacade, codesInterface);
	        load.initializeDB(em);
	//        DatabaseFacade.getInstance().writeToXML();
		} finally {
			emf.close();
		}

	}
        
}
