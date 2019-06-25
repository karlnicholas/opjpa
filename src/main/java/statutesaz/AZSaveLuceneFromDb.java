
package statutesaz;

import java.io.IOException;
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

public class AZSaveLuceneFromDb extends AZProcessSiteRip {
	private static final Logger logger = Logger.getLogger(AZSaveLuceneFromDb.class.getName());
	private IndexWriter indexWriter;
	private TaxonomyWriter taxoWriter;
    private FacetsConfig facetsConfig;

    private final Path index;
    private final Path indextaxo;
	
	private int nDocsAdded;
    private int nFacetsAdded;    
    private int position;

    public static void main(String... args) throws SQLException, IOException  {

		new AZSaveLuceneFromDb().loadCode();
	}
    
    
    public AZSaveLuceneFromDb() throws SQLException {
		super();
		// For gsarizona
		index = Paths.get("c:/users/karln/opcastorage/index/");
		indextaxo = Paths.get("c:/users/karln/opcastorage/indextaxo/");
		position = 1;
	}

	/**
     * This part loads Lucene Indexes
     * @throws IOException 
	 * @throws SQLException 
     */
	public void loadCode() throws IOException, SQLException {
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
		List<LawCode> lawCodes = retrieveLawCodes();
		for ( LawCode lawCode: lawCodes) {
			if ( lawCode.getCode().equals("CONS")) {
				continue;
			}
			try {
				parseLawCode(lawCode.getCode(), this::processStatutesLeaf);
			} catch ( Exception ex) {
				System.out.println(lawCode.getCode());
				throw ex;
			}
		}
		taxoWriter.commit();
		indexWriter.commit();

		taxoWriter.close();
		indexWriter.close();

		Date end = new Date();
		logger.info(end.getTime() - start.getTime() + " total milliseconds");
		logger.info("From " + "codes" + " " + nDocsAdded + ": Facets = " + nFacetsAdded);
	}

	/*
	 * I need to save the title, the categorypath, the full path for reference, the text, the part and partnumber
	 * and of course the section and sectionParagraph if it exists
	 */
	private void processStatutesLeaf(LawForCodeSections lawForCodeSections, StatutesLeaf statutesLeaf) {
		for ( LawSection lawSection: lawForCodeSections.getSections() ) {
	
			org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

			String[] facetPath = FacetUtils.fromString(statutesLeaf.getFullFacet());
			String content = "<b>" + lawSection.getSection_num() + "</b>" + lawSection.getContent_xml().replace("<caml:Content xmlns:caml=\"http://lc.ca.gov/legalservices/schemas/caml.1#\">", "").replace("</caml:Content>", "")
					.replace("<span class=\"EmSpace\"/>", "&emsp;").replace("<span class=\"EnSpace\"/>", "&ensp;").replace("<span class=\"ThinSpace\"/>", "&thsp;").replace("<span class=\"NbSpace\"/>", "&nbsp;");
			
			doc.add(new StringField("path", statutesLeaf.getFullFacet(), Field.Store.YES));
			doc.add(new StringField("sectionnumber", lawSection.getSection_num().substring(0, lawSection.getSection_num().length()-1), Field.Store.YES));
			doc.add(new StringField("position", Integer.toString( position++ ), Field.Store.YES));
			doc.add(new TextField("sectiontext", content, Field.Store.YES));
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
		
	}

}
