package scraper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import opca.model.SlipOpinion;
import opca.parser.ScrapedOpinionDocument;
import opca.scraper.*;

public class TestCACaseScraper extends CACaseScraper {

	public TestCACaseScraper(boolean debugFiles) {
		super(debugFiles);
	}
	
	@Override
	public List<SlipOpinion> getCaseList() {
		try {
			return parseCaseList(new FileInputStream( CACaseScraper.caseListFile ));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<ScrapedOpinionDocument> scrapeOpinionFiles(List<SlipOpinion> opinions) {
		List<ScrapedOpinionDocument> documents = new ArrayList<ScrapedOpinionDocument>();
		CAParseScrapedDocument parseScrapedDocument = new CAParseScrapedDocument();		
		for (SlipOpinion slipOpinion: opinions ) {
			try ( InputStream inputStream = Files.newInputStream( Paths.get(casesDir + slipOpinion.getFileName() + slipOpinion.getFileExtension())) ) {
				documents.add( parseScrapedDocument.parseScrapedDocument(slipOpinion, inputStream) );
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}
}
