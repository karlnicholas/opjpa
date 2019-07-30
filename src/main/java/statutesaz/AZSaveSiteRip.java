
package statutesaz;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import gsearch.util.FacetUtils;
import statutes.StatutesLeaf;
import statutes.StatutesRoot;

public class AZSaveSiteRip extends AZSiteRip {
	private static final Logger logger = Logger.getLogger(AZSaveSiteRip.class.getName());
	private IndexWriter indexWriter;
	private TaxonomyWriter taxoWriter;
    private FacetsConfig facetsConfig;

    private final Path index;
    private final Path indextaxo;
	private final Path lawCodesPath;
	
	private int nDocsAdded;
    private int nFacetsAdded;    
    private int position;

    public static void main(String... args) throws Exception {
		new AZSaveSiteRip().loadCode();
	}
    
    
    public AZSaveSiteRip() {
		super();
		// For gsarizona
		lawCodesPath = Paths.get("c:/users/karln/opcastorage/ArizonaStatutes");
		index = Paths.get("c:/users/karln/opcastorage/azlucene/index/");
		indextaxo = Paths.get("c:/users/karln/opcastorage/azlucene/indextaxo/");
		position = 1;
	}

	/**
     * This part loads Lucene Indexes
     * @throws IOException 
	 * @throws SQLException 
     */
	public void loadCode() throws Exception {
		Date start = new Date();
		logger.info("Indexing to directory 'index'...");

		Directory indexDir = FSDirectory.open(index);
		Directory taxoDir = FSDirectory.open(indextaxo);

		Analyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);
	    // create and open an index writer
	    indexWriter = new IndexWriter(indexDir, iwc);
	    // create and open a taxonomy writer
	    taxoWriter = new DirectoryTaxonomyWriter(taxoDir, OpenMode.CREATE);
	    
	    facetsConfig = new FacetsConfig();
	    facetsConfig.setHierarchical(FacetsConfig.DEFAULT_INDEX_FIELD_NAME, true);
	    nDocsAdded = 0;
	    nFacetsAdded = 0;
	    
		List<Path> filePaths = new ArrayList<>();

		parse(statutesRoot->{
			try {
				filePaths.add(processFile(statutesRoot));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, this::processStatutesLeaf);

		taxoWriter.commit();
		indexWriter.commit();

		taxoWriter.close();
		indexWriter.close();

		Path filePath = Paths.get(lawCodesPath.toString(), "files");
		BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.US_ASCII);
		for (Path file : filePaths) {
			bw.write(file.getFileName().toString());
			bw.newLine();
		}
		bw.close();

		Date end = new Date();
		logger.info(end.getTime() - start.getTime() + " total milliseconds");
		logger.info("From " + "codes" + " " + nDocsAdded + ": Facets = " + nFacetsAdded);
	}

	/*
	 * I need to save the title, the categorypath, the full path for reference, the text, the part and partnumber
	 * and of course the section and sectionParagraph if it exists
	 */
	private void processStatutesLeaf(SectionContent sectionContent, StatutesLeaf statutesLeaf) {
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

		String[] facetPath = FacetUtils.fromString(statutesLeaf.getFullFacet());
		
		doc.add(new StringField("path", statutesLeaf.getFullFacet(), Field.Store.YES));
		doc.add(new StringField("sectionnumber", sectionContent.getSectionNumber(), Field.Store.YES));
		doc.add(new StringField("position", Integer.toString( position++ ), Field.Store.YES));
		doc.add(new TextField("sectiontext", sectionContent.getSectionContent(), Field.Store.YES));
		// invoke the category document builder for adding categories to the document and,
		// as required, to the taxonomy index 
		FacetField facetField = new FacetField( 
				FacetsConfig.DEFAULT_INDEX_FIELD_NAME, 
				facetPath 
			);

		doc.add( facetField );
		
		// finally add the document to the index
		try {
			indexWriter.addDocument(facetsConfig.build(taxoWriter, doc));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		nDocsAdded++;
		nFacetsAdded += facetPath.length;
	}
	
	Path processFile(StatutesRoot statutesRoot) throws Exception {	
		Path outputFile = Paths.get(lawCodesPath.toString(), "\\", statutesRoot.getTitle(false) + ".ser");
		OutputStream os = Files.newOutputStream(outputFile);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(statutesRoot);
		oos.close();
		return outputFile;
	}

}
