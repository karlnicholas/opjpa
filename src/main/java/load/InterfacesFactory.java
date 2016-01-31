package load;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import opinions.parsers.CaseParserInterface;
import codesparser.*;

/**
 * Servlet implementation class Load4Web
 */
public class InterfacesFactory { 
	
	private static CodesInterface codesInterface = null;
	private static CaseParserInterface caseParserInterface = null;
	
	public static CodesInterface getCodesInterface() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if ( codesInterface == null ) {
			ResourceBundle rb = ResourceBundle.getBundle("interfaces/interfaces");
			String iface = rb.getString("codesinterface");
			codesInterface = (CodesInterface) Class.forName(iface).newInstance();
		}
		return codesInterface;
	}
	
	public static CodeTitles[] getCodeTitles() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getCodesInterface().getCodeTitles();
	}

	public static LoadInterface getLoadInterface() throws InstantiationException, IllegalAccessException, ClassNotFoundException {		
		ResourceBundle rb = ResourceBundle.getBundle("interfaces/interfaces");
		String iface = rb.getString("loadinterface");
		return (LoadInterface) Class.forName(iface).newInstance();
	}

	public static CaseParserInterface getCaseParserInterface() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if ( caseParserInterface == null ) {
			ResourceBundle rb = ResourceBundle.getBundle("interfaces/interfaces");
			String iface = rb.getString("caseparserinterface");
			caseParserInterface = (CaseParserInterface) Class.forName(iface).newInstance();
		}
		return caseParserInterface;
	}
	/*
	 * Reqired to run this to create xml files in the resources folder that describe the code hierarchy 
	 */
	public static void main(String... args) throws Exception {
		
		LoadInterface loader = getLoadInterface();

		// For gscalifornia
		Path codesDir = Paths.get("c:/users/karl/code");

		Path xmlcodes = Paths.get("c:/users/karl/scsb/opjpa/src/main/resources/xmlcodes");
		
		loader.createXMLCodes(codesDir, xmlcodes );
	}
    
}
