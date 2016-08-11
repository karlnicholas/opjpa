package opjpa;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

import codesparser.Section;
import opca.dto.OpinionViews;
import opca.service.OpinionViewCache;
import opca.view.OpinionView;
import opca.view.SectionView;
import opca.view.StatuteView;
import opca.view.SubcodeView;

public class SlipOpinionJson {

	public static void main(String... args) {
		new SlipOpinionJson().run();
	}

	private void run() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("opjpa");
		EntityManager em = emf.createEntityManager();
		try {
			OpinionViewCache slipOpinionData = new OpinionViewCache();
			slipOpinionData.setEntityManager(em);
			slipOpinionData.buildCache();
			em.close();

			List<Date[]> dates = slipOpinionData.getReportDates();
			Date[] date = dates.get(1);
			OpinionViews opinions = copyCasesForViewinfo( slipOpinionData, date[0], date[1] );
			
			JAXBContext jc;
			try {

		        jc = JAXBContext.newInstance(
		        		OpinionViews.class, OpinionView.class, StatuteView.class, SubcodeView.class, SectionView.class, Section.class
	        		);
			       
		        Configuration config = new Configuration();
		        MappedNamespaceConvention con = new MappedNamespaceConvention(config);
		        Writer writer = new OutputStreamWriter(System.out);
		        XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, writer);
		 
		        Marshaller marshaller = jc.createMarshaller();
		        marshaller.marshal(opinions, xmlStreamWriter);

/*		        
		        Marshaller marshaller = jc.createMarshaller(); 
		        // Set the Marshaller media type to JSON or XML 
		        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		        // Set it to true if you need to include the JSON root element in the JSON output 
//	        	marshaller.setProperty(Marshaller.JSON_INCLUDE_ROOT, true); 
		        // Set it to true if you need the JSON output to formatted 
		        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
		        // Marshal the employee object to JSON and print the output to console 
		        marshaller.marshal(opinions, System.out);
*/		         
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} finally {
			emf.close();
		}
	}
	private OpinionViews copyCasesForViewinfo(OpinionViewCache slipOpinionData, Date sd, Date ed) {
		List<OpinionView> opinionViews = new ArrayList<OpinionView>();
		for (OpinionView opinionView: slipOpinionData.getAllOpinionCases() ) {
			if ( 
				opinionView.getOpinionDate().compareTo(sd) >= 0  
				&& opinionView.getOpinionDate().compareTo(ed) <= 0
			) {
				opinionViews.add(opinionView);
			}
		}
		OpinionViews opinionViewList = new OpinionViews(opinionViews);
		return opinionViewList;
	}

}
