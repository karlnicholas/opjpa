

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import codesparser.*;
import opca.parser.CaseScraperInterface;

public class CaseInterfacesService {

	private static final String interfaces = "application";
	private static final String codesinterfaceKey = "codesparser.codesinterface";
	private static final String caseparserinterfaceKey = "opinions.caseparserinterface";	

	private CodesInterface codesInterface = null;
	private CaseScraperInterface caseScraper = null;
	
	public CaseInterfacesService initialize(boolean loadXMLCodes) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, JAXBException, URISyntaxException {
		ResourceBundle rb = ResourceBundle.getBundle(interfaces);
		String iface = rb.getString(codesinterfaceKey);
		codesInterface = (CodesInterface) Class.forName(iface).newInstance();
		if ( loadXMLCodes ) codesInterface.loadStatutes();
		iface = rb.getString(caseparserinterfaceKey);
		caseScraper = (CaseScraperInterface) Class.forName(iface).newInstance();
		return this;
	}
	
	public CodesInterface getCodesInterface() {
		return codesInterface;
	}
	
	public CodeTitles[] getCodeTitles() {
		return codesInterface.getCodeTitles();
	}

	public CaseScraperInterface getCaseParserInterface() {
		return caseScraper;
	}
    
}
