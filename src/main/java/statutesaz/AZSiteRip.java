package statutesaz;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import statutes.StatuteRange;
import statutes.StatutesLeaf;
import statutes.StatutesNode;
import statutes.StatutesRoot;
import statutes.api.IStatutesApi;
import statutesaz.statutesapi.AZStatutesApiImpl;

public class AZSiteRip {
	private static final String TITLE = "Title";
	private static final String CHAPTER = "Chapter";
	private static final String ARTICLE = "Article";
	
	protected void parse( Consumer<StatutesRoot> statutesRootConsumer, BiConsumer<SectionContent, StatutesLeaf> statutesLeafConsumer) throws Exception {
		IStatutesApi iStatutesApi = new AZStatutesApiImpl();
		for ( int i=1; i <= 49; ++i ) {
			// hard-coded skips for empty titles.
			if ( i == 2 || i == 24)
				continue;
			System.out.println(TITLE + " " + i);
			// a delay factor so as to not freakout the website server/admin.
			Thread.sleep(1000);
			String lawCode = TITLE.toLowerCase() + i;
			StatutesRoot statutesRoot = new StatutesRoot(
					lawCode, 
					iStatutesApi.getTitle(lawCode), 
					iStatutesApi.getShortTitle(lawCode), 
					lawCode + "-0");

			int p = 1;
			for ( Element e: Jsoup.parse(new URL("https://www.azleg.gov/arsDetail/?title="+i), 10000).select("div[class=accordion]") ) {
				try {
					processChapter(e, p++, statutesRoot, statutesLeafConsumer);
				} catch (Exception ex) {
					System.out.println(lawCode);
					throw ex;
				}
			}
			if ( statutesRootConsumer != null ) {
				statutesRootConsumer.accept(statutesRoot);
			}
		}
		
		
	}

	protected void processChapter(Element e, int position, StatutesRoot statutesRoot, BiConsumer<SectionContent, StatutesLeaf> statutesLeafConsumer) {
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
			if ( statutesLeafConsumer != null ) {
				Element aSelect = a.select("a[class=stat]").first();
				String hrefFull = aSelect.attr("href");
				String docName = "docName=";
				String href = hrefFull.substring(hrefFull.indexOf(docName)+docName.length());
				Elements ps;
				try {
					Thread.sleep(125);
					ps = Jsoup.parse(new URL(href), 10000).select("body p");
					StringBuilder sb = new StringBuilder();
					for( Element p: ps ) {
						sb.append(p.toString());
					}
					statutesLeafConsumer.accept(new SectionContent(sb.toString(), partNumber), statutesLeaf);
				} catch (IOException | InterruptedException e1) {
					System.out.println( "Exception: " + a.toString() );
				}
			}
			statutesNode.addReference(statutesLeaf);
		}

	}
	
}
