

import java.io.*;

import org.apache.poi.hwpf.HWPFDocument;

import opcalifornia.*;
import opinions.model.OpinionSummary;
import opinions.parsers.ParserDocument;

public class CATestCases extends CACaseParser {
	public final static String casesDir = "cases/";
	
	@Override
	public Reader getCaseList() throws Exception {
        return new BufferedReader( new InputStreamReader( new FileInputStream( OpJpaTest.caseListFile ), OpJpaTest.encoding) );
	}

	@SuppressWarnings("resource")
	@Override
	public ParserDocument getCaseFile(OpinionSummary opinionSummary, boolean debugCopy) throws Exception {
		InputStream inputStream = new FileInputStream(new File( casesDir + opinionSummary.getName() +".DOC" ));
		if ( debugCopy ) {
			inputStream = saveCopyOfCase(casesDir, opinionSummary.getName() + ".DOC", inputStream );
		}
		try {
			return new ParserDocument( new HWPFDocument(inputStream));
		} finally {
			inputStream.close();
		}
	}
	
	private static InputStream saveCopyOfCase(String directory, String fileName, InputStream inputStream ) throws Exception {
		
	    File file = new File(directory + "/" + fileName);
	    file.createNewFile();
	    
	    OutputStream out = new FileOutputStream( file );
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	    try {
	    	byte[] bytes = new byte[8192];
	    	int len;
	    	while ( (len = inputStream.read(bytes, 0, bytes.length)) != -1 ) {
	    		out.write(bytes, 0, len);
	    		baos.write(bytes, 0, len);
	    	}
	    	out.close();
	    	baos.close();
	        return new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));

	    } finally {
	    	inputStream.close();
	    }
	}

	

}
