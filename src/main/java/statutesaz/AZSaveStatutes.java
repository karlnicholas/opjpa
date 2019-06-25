package statutesaz;

import java.io.BufferedWriter;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import javax.xml.bind.JAXBException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import statutes.StatuteRange;
import statutes.StatutesLeaf;
import statutes.StatutesNode;
import statutes.StatutesRoot;
import statutes.api.IStatutesApi;
import statutesaz.statutesapi.AZStatutesApiImpl;

public class AZSaveStatutes {
	private static final String TITLE = "Title";
	private static final String CHAPTER = "Chapter";
	private static final String ARTICLE = "Article";
	private final Path lawCodesPath;
	private Set<String> classes;
//	private Pattern pattern = Pattern.compile("class=([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

	/*
	 * Reqired to run this to create xml files in the resources folder that describe
	 * the code hierarchy
	 */
	public static void main(String... args) throws Exception {
		new AZSaveStatutes().run();
	}

	public AZSaveStatutes() throws SQLException, JAXBException {
		lawCodesPath = Paths.get("c:/users/karln/opcastorage/ArizonaStatutes");
		classes = new HashSet<>();
	}

	protected void run() throws Exception {
		IStatutesApi iStatutesApi = new AZStatutesApiImpl();
		List<Path> filePaths = new ArrayList<>();

		for ( int i=1; i <= 49; ++i ) {
			if ( i == 2 || i == 24)
				continue;
			System.out.println(TITLE + " " + i);
			Thread.sleep(3000);
			String lawCode = TITLE.toLowerCase() + i;
			StatutesRoot statutesRoot = new StatutesRoot(
					lawCode, 
					iStatutesApi.getTitle(lawCode), 
					iStatutesApi.getShortTitle(lawCode), 
					lawCode + "-0");

			int p = 1;
			for ( Element e: Jsoup.parse(new URL("https://www.azleg.gov/arsDetail/?title="+i), 10000).select("div[class=accordion]") ) {
				try {
					processChapter(e, p++, statutesRoot);
				} catch (Exception ex) {
					System.out.println(lawCode);
					throw ex;
				}
			}
			filePaths.add(processFile(statutesRoot));
		}

		Path filePath = Paths.get(lawCodesPath.toString(), "files");
		BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.US_ASCII);
		for (Path file : filePaths) {
			bw.write(file.getFileName().toString());
			bw.newLine();
		}
		bw.close();
		classes.forEach(System.out::println);
	
	}
	
	private void processChapter(Element e, int position, StatutesRoot statutesRoot) {
		String chapterNum = e.select("h5 a").text();
		String part = CHAPTER;
		String partNumber = chapterNum.toLowerCase().replace("chapter", "").trim();
		String heading = e.select("h5 div").text();
		String fullFacet = statutesRoot.getFullFacet() + "/" + statutesRoot.getLawCode() + "-" + 1 + "-" + position;

		StatutesNode statutesNode = new StatutesNode(
				statutesRoot, 
				fullFacet, 
				part, 
				partNumber, 
				heading, 
				1
			);
		statutesRoot.addReference(statutesNode);

		int posA = 1;
		for ( Element a: e.select("div[class=article] ul") ) {
			part = ARTICLE;
			partNumber = a.select("li[class=colleft] a").text().trim();
			heading = a.select("li[class=colright]").text();
			fullFacet = statutesNode.getFullFacet() + "/" + statutesRoot.getLawCode() + "-" + 2 + "-" + posA++;

			StatutesLeaf statutesLeaf = new StatutesLeaf(
					statutesNode, 
					fullFacet, 
					part,  
					partNumber, 
					heading, 
					2, 
					new StatuteRange()
				);
//			leafConsumer.accept(lawForCode, statutesLeaf);
			statutesNode.addReference(statutesLeaf);
		}

	}
	
	Path processFile(StatutesRoot statutesRoot) throws Exception {

		Path outputFile = Paths.get(lawCodesPath.toString(), "\\", statutesRoot.getTitle(false) + ".ser");
		OutputStream os = Files.newOutputStream(outputFile);
		ObjectOutputStream oos = new ObjectOutputStream(os);

		oos.writeObject(statutesRoot);

		oos.close();

		return outputFile;
		// System.out.println(c.getTitle());
	}

}
