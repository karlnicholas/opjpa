package restclient;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import apimodel.Clusters;
import apimodel.ApiOpinions;
import loadmodel.LoadOpinion;

public class Decode {
	private static final Pattern pattern = Pattern.compile("/");
	public static void main(String... strings) throws Exception {
		new Decode().run();
	}

	private void run() throws JsonProcessingException, IOException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		ObjectMapper om = new ObjectMapper();
		JsonFactory factory = new JsonFactory();
		Map<Long, LoadOpinion> mapLoadOpinions = new TreeMap<Long, LoadOpinion>();

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("cluters.json"))) {
			JsonParser jp = factory.createParser(bis);
			// loop until token equal to "}"
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				if ("results".equals(fieldname)) {
					JsonToken token;
					LoadOpinion loadOpinion = null;
					// current token is "[", move next
					while ((token = jp.nextToken()) != JsonToken.END_ARRAY ) {
						if ( token == JsonToken.START_OBJECT ) {
							loadOpinion = new LoadOpinion();
							continue;
						} else if ( token == JsonToken.END_OBJECT ) {
							mapLoadOpinions.put(loadOpinion.getId(), loadOpinion);
							continue;
						} else if (token == JsonToken.START_ARRAY) {
							continue;
						}
						if ( token != JsonToken.FIELD_NAME ) {
							System.out.print(".");
						}
						String cName = jp.getCurrentName();
						if ( "resource_uri".equals(cName)) {
							loadOpinion.setId(new Long(pattern.split(jp.nextTextValue())[7]));
						} else if ( "date_filed".equals(cName)) {
							String date = jp.nextTextValue();
							loadOpinion.setDateFiled(sdf.parse(date));
						} else if ( "case_name".equals(cName)) {
							loadOpinion.setCaseName(jp.nextTextValue());
						} else if ( "case_name_full".equals(cName)) {
							loadOpinion.setFullCaseName(jp.nextTextValue());
						} else if ( "case_name_short".equals(cName)) {
							loadOpinion.setShortCaseName(jp.nextTextValue());
						} else if ( "state_cite_one".equals(cName)) {
							String cite = jp.nextTextValue().replace(". ", ".");
							if ( cite.contains("Cal.App.") ) {
								loadOpinion.setCitation(cite);
							}
						} else if ( "state_cite_two".equals(cName)) {
							String cite = jp.nextTextValue().replace(". ", ".");
							if ( cite.contains("Cal.App.") ) {
								loadOpinion.setCitation(cite);
							}
						} else if ( "state_cite_three".equals(cName)) {
							String cite = jp.nextTextValue().replace(". ", ".");
							if ( cite.contains("Cal.App.") ) {
								loadOpinion.setCitation(cite);
							}
						} else {
							token = jp.nextToken();
							if ( token == JsonToken.START_ARRAY ) {
								// skip past array
								while ( jp.nextToken() != JsonToken.END_ARRAY);
							}
						}
						// display msg1, msg2, msg3
					}
				}
			}

			Clusters clusters = om.readValue(jp, Clusters.class);
			System.out.println(clusters.getCount());
			bis.close();
		}

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream("opinions.json"))) {
			JsonParser jp = factory.createParser(bis);
			ApiOpinions opinions = om.readValue(jp, ApiOpinions.class);
			System.out.println(opinions.getCount());
			bis.close();
		}

	}
}
