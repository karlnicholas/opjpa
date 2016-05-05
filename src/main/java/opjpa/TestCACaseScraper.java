package opjpa;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;

import opca.model.SlipOpinion;
import opca.parser.ParserDocument;
import opca.scraper.*;

public class TestCACaseScraper extends CACaseScraper {

	public TestCACaseScraper(boolean debugFiles) {
		super(debugFiles);
	}
	
	@Override
	public List<SlipOpinion> getCaseList() {
		try {
			return parseCaseList(new BufferedReader( new InputStreamReader( new FileInputStream( CACaseScraper.caseListFile ), "UTF-8") ));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ParserDocument> getCaseFiles(List<SlipOpinion> opinions) {
		List<ParserDocument> documents = new ArrayList<ParserDocument>();
		for (SlipOpinion slipOpinion: opinions ) {
			try ( InputStream inputStream = Files.newInputStream( Paths.get(casesDir + slipOpinion.getFileName() +".DOC" )) ) {
				documents.add( new ParserDocument( slipOpinion, new HWPFDocument(inputStream)) );
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}
}
