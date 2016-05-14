package opjpa;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;

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
		for (SlipOpinion slipOpinion: opinions ) {
			try ( InputStream inputStream = Files.newInputStream( Paths.get(casesDir + slipOpinion.getFileName() +".DOC" )) ) {
				documents.add( new ScrapedOpinionDocument( slipOpinion, new HWPFDocument(inputStream)) );
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}
}
