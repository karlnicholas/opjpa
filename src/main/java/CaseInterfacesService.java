

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import statutes.StatutesTitles;
import statutes.api.IStatutesApi;
import opca.parser.OpinionScraperInterface;

public class CaseInterfacesService {

	private static final String interfaces = "application";
	private static final String codesinterfaceKey = "statutes.codesinterface";
	private static final String caseparserinterfaceKey = "opinions.caseparserinterface";	

	private IStatutesApi iStatutesApi = null;
	private OpinionScraperInterface caseScraper = null;
	
	public CaseInterfacesService initialize(boolean loadXMLCodes) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, JAXBException, URISyntaxException {
		ResourceBundle rb = ResourceBundle.getBundle(interfaces);
		String iface = rb.getString(codesinterfaceKey);
		iStatutesApi = (IStatutesApi) Class.forName(iface).newInstance();
		iStatutesApi.loadStatutes();
		iface = rb.getString(caseparserinterfaceKey);
		caseScraper = (OpinionScraperInterface) Class.forName(iface).newInstance();
		return this;
	}
	
	public IStatutesApi getParserInterface() {
		return iStatutesApi;
	}
	
	public StatutesTitles[] getStatutesTitles() {
		return iStatutesApi.getStatutesTitles();
	}

	public OpinionScraperInterface getCaseParserInterface() {
		return caseScraper;
	}
    
}
