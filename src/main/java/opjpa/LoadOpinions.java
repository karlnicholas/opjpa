package opjpa;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import code.CACodes;
import codesparser.CodesInterface;
import load.NewLoadOpinions;

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
        NewLoadOpinions load = new NewLoadOpinions(emf, codesInterface);
        try {
//        	load.initializeDB(em);
//        	load.readStream("c:/users/karl/downloads/calctapp.tar.gz");
        	load.readStream("c:/users/karl/downloads/cal.tar.gz");
        	
        } finally {
        	emf.close();
        }
	}
        
}
