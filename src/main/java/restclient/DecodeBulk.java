package restclient;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import apimodel.Cluster;
import loadmodel.LoadOpinion;

public class DecodeBulk {
	public static void main(String... strings) throws Exception {
		new DecodeBulk().run();
	}

	private void run() throws JsonProcessingException, IOException, ParseException {
		System.out.println("M="+Runtime.getRuntime().freeMemory());
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		ObjectMapper om = new ObjectMapper();
//		JsonFactory factory = new JsonFactory();
		Map<Long, LoadOpinion> mapLoadOpinions = new TreeMap<Long, LoadOpinion>();

    	TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream("c:/users/karln/downloads/cal-clusters.tar.gz"))));
    	TarArchiveEntry entry;
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
//	            System.out.println("Content:" +  new String(content));
	            // http://www.courtlistener.com/api/rest/v3/clusters/1361768/
	            Cluster cluster = om.readValue(content, Cluster.class);
	            if ( cluster.getPrecedential_status() != null && cluster.getPrecedential_status().equals("Published") ) {
		            LoadOpinion loadOpinion = new LoadOpinion(cluster);
		            mapLoadOpinions.put(loadOpinion.getId(), loadOpinion);
	            }
	        }
        }
		tarIn.close();
		System.out.println(mapLoadOpinions.size());
/*		
		int count = 0;
		System.out.println("M="+Runtime.getRuntime().freeMemory());
    	tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream("c:/users/karl/downloads/calctapp-opinions.tar.gz"))));
        while ( (entry = tarIn.getNextTarEntry()) != null ) {
	        if (tarIn.canReadEntryData(entry)) {
	            int entrySize = (int) entry.getSize();
	            byte[] content = new byte[entrySize];
	            int offset = 0;
	
	            while ((offset += tarIn.read(content, offset, (entrySize - offset) )) != -1) {
	                if (entrySize - offset == 0)
	                    break;
	            }
//	            System.out.println("Content:" +  new String(content));
	            // http://www.courtlistener.com/api/rest/v3/clusters/1361768/
	            ApiOpinion opinion = om.readValue(content, ApiOpinion.class);
	            Long id = new Long(pattern.split(opinion.getResource_uri())[7]);
	            LoadOpinion loadOpinion = mapLoadOpinions.get(id);
	            if ( loadOpinion != null && opinion.getHtml_lawbox() != null ) {
	            	loadOpinion.setHtml_lawbox(opinion.getHtml_lawbox());
	            	loadOpinion.setOpinions_cited(opinion.getOpinions_cited());
	            	if ( count++ %1000 == 0 ) {
	            		System.out.println(count);
		        		System.out.println("M="+Runtime.getRuntime().freeMemory());
	            	}
	            }
	        }
        }
		tarIn.close();
		System.out.println(count);
*/
	}
}
