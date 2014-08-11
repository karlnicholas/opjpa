

import java.io.*;

import opcalifornia.*;
import opinions.model.CourtCase;

public class CACasesFile extends CACaseParser {
	
	@Override
	public Reader getCaseList() throws Exception {
        return new BufferedReader( new InputStreamReader( new FileInputStream( OpJpa.caseListFile ), OpJpa.encoding) );
	}

	@Override
	public InputStream getCaseFile(CourtCase ccase) throws Exception {
		return new BufferedInputStream(new FileInputStream(new File( OpJpa.casesDir + ccase.getName() )));
	}

}
