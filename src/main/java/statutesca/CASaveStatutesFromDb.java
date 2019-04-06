package statutesca;

import java.io.BufferedWriter;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import javax.xml.bind.JAXBException;

import statutes.SectionNumber;
import statutes.StatuteRange;
import statutes.StatutesLeaf;
import statutes.StatutesRoot;

public class CASaveStatutesFromDb extends CAProcessDb {
	private final Path lawCodesPath;
	private int position;
	private Set<String> classes;
//	private Pattern pattern = Pattern.compile("class=([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

	/*
	 * Reqired to run this to create xml files in the resources folder that describe
	 * the code hierarchy
	 */
	public static void main(String... args) throws Exception {
		new CASaveStatutesFromDb().run();
	}

	public CASaveStatutesFromDb() throws SQLException, JAXBException {
		super();
		lawCodesPath = Paths.get("c:/users/karln/opcastorage/CaliforniaStatutes");
		position = 1;
		classes = new HashSet<>();
	}

	protected void run() throws Exception {
		List<Path> filePaths = new ArrayList<>();
		List<LawCode> lawCodes = retrieveLawCodes();
		for (LawCode lawCode : lawCodes) {
			if (lawCode.getCode().equals("CONS")) {
				continue;
			}
			try {
				StatutesRoot statutesRoot = parseLawCode(lawCode.getCode(), this::processStatutesLeaf);
				filePaths.add(processFile(statutesRoot));
			} catch (Exception ex) {
				System.out.println(lawCode.getCode());
				throw ex;
			}
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

	private void processStatutesLeaf(LawForCodeSections lawForCodeSections, StatutesLeaf statutesLeaf) {
		ArrayList<SectionNumber> sectionNumbers = statutesLeaf.getSectionNumbers();
		for (LawSection lawSection : lawForCodeSections.getSections()) {
			if ( lawSection.getSection_num() != null ) {
				sectionNumbers.add(new SectionNumber(position++, 
					lawSection.getSection_num().substring(0, lawSection.getSection_num().length()-1)));
			}
/*			
			Matcher matcher = pattern.matcher(lawSection.getContent_xml());
			while(matcher.find()) {
				String content = lawSection.getContent_xml();
				int s = matcher.start();
				while ( content.charAt(s) != '<' ) {
					s--;
					if ( s == 0 ) {
						break;
					}
				}
				classes.add(lawSection.getContent_xml().substring(s, matcher.end()));
	        }
*/	        
		}
		if ( sectionNumbers.size() > 0 ) {
			statutesLeaf.setStatuteRange(new StatuteRange(
				sectionNumbers.get(0), 
				sectionNumbers.get(sectionNumbers.size()-1)
			));
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
