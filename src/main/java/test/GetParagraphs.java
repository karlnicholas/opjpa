package test;

import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.omg.CORBA.portable.InputStream;

public class GetParagraphs {
	public static void main(String... args) throws IOException {
		new GetParagraphs().run();
	}
	private void run() throws IOException {
		InputStream stream = null;
	    HWPFDocument document = new HWPFDocument(stream);
	    Range range = document.getRange();
	
	    StyleSheet stylesheet = document.getStyleSheet();
	
	    for (int i = 0; i < range.numParagraphs(); i++) {
	        Paragraph paragraph = range.getParagraph(i);
	
            String text = paragraph.text();
	    }
	}

}
