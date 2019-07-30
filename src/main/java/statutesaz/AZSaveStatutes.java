package statutesaz;

import java.io.BufferedWriter;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import statutes.StatutesRoot;

public class AZSaveStatutes extends AZSiteRip {
//	private Pattern pattern = Pattern.compile("class=([\"'])(?:(?=(\\\\?))\\2.)*?\\1");

	private final Path lawCodesPath;

	/*
	 * Required to run this to create xml files in the resources folder that describe
	 * the code hierarchy
	 */
	public static void main(String... args) throws Exception {
		new AZSaveStatutes().saveStatutes();
	}

	public AZSaveStatutes() {
		lawCodesPath = Paths.get("c:/users/karln/opcastorage/ArizonaStatutes");
	}

	protected void saveStatutes() throws Exception {
		List<Path> filePaths = new ArrayList<>();

		parse(statutesRoot->{
			try {
				filePaths.add(processFile(statutesRoot));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, null);

		Path filePath = Paths.get(lawCodesPath.toString(), "files");
		BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.US_ASCII);
		for (Path file : filePaths) {
			bw.write(file.getFileName().toString());
			bw.newLine();
		}
		bw.close();
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
