

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import statutes.StatutesTitles;
import parser.ParserInterface;
import opca.parser.OpinionScraperInterface;

public class CaseInterfacesService {

	private static final String interfaces = "application";
	private static final String codesinterfaceKey = "statutes.codesinterface";
	private static final String caseparserinterfaceKey = "opinions.caseparserinterface";	

	private ParserInterface parserInterface = null;
	private OpinionScraperInterface caseScraper = null;
	
	public CaseInterfacesService initialize(boolean loadXMLCodes) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, JAXBException, URISyntaxException {
		ResourceBundle rb = ResourceBundle.getBundle(interfaces);
		String iface = rb.getString(codesinterfaceKey);
		parserInterface = (ParserInterface) Class.forName(iface).newInstance();
		if ( loadXMLCodes ) parserInterface.loadStatutes();
		iface = rb.getString(caseparserinterfaceKey);
		caseScraper = (OpinionScraperInterface) Class.forName(iface).newInstance();
		return this;
	}
	
	public ParserInterface getParserInterface() {
		return parserInterface;
	}
	
	public StatutesTitles[] getStatutesTitles() {
		return parserInterface.getStatutesTitles();
	}

	public OpinionScraperInterface getCaseParserInterface() {
		return caseScraper;
	}
    
}
