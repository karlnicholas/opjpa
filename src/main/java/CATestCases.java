

import java.io.*;

import opcalifornia.*;
import opinions.model.courtcase.CourtCase;

public class CATestCases extends CACaseParser {
	
	@Override
	public Reader getCaseList() throws Exception {
        return new BufferedReader( new InputStreamReader( new FileInputStream( OpJpaTest.caseListFile ), OpJpaTest.encoding) );
	}

	@Override
	public InputStream getCaseFile(CourtCase ccase) throws Exception {
		return new BufferedInputStream(new FileInputStream(new File( OpJpaTest.casesDir + ccase.getName() )));
	}

}
