package load;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import apimodel.ApiOpinion;
import apimodel.Cluster;
import loadmodel.LoadOpinion;

public class LoadCourtListenerFiles {
	private final Pattern pattern;
	private final CourtListenerCallback courtListenerCallback;
	private final Logger logger;
	int total = 0;

	public LoadCourtListenerFiles(CourtListenerCallback courtListenerCallback) {
		this.courtListenerCallback = courtListenerCallback;
		pattern = Pattern.compile("/");
		logger = Logger.getLogger(LoadCourtListenerFiles.class.getName());
	}

	public void loadFiles(String opinionsFileName, String clustersFileName, int loadOpinionsPerCallback) throws IOException {
		//
		ObjectMapper om = new ObjectMapper();
		Map<Long, LoadOpinion> mapLoadOpinions = new TreeMap<Long, LoadOpinion>();
		TarArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(clustersFileName))));
		TarArchiveEntry entry;
		try {
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if (tarIn.canReadEntryData(entry)) {
					int entrySize = (int) entry.getSize();
					byte[] content = new byte[entrySize];
					int offset = 0;

					while ((offset += tarIn.read(content, offset, (entrySize - offset))) != -1) {
						if (entrySize - offset == 0)
							break;
					}
					// System.out.println("Content:" + new String(content));
					// http://www.courtlistener.com/api/rest/v3/clusters/1361768/
					Cluster cluster = om.readValue(content, Cluster.class);
					if (cluster.getPrecedential_status() != null && cluster.getPrecedential_status().equals("Published")) {
						LoadOpinion loadOpinion = new LoadOpinion(cluster);
//						if (loadOpinion.getCaseName() != null || !loadOpinion.getCaseName().trim().isEmpty()) {
							mapLoadOpinions.put(loadOpinion.getId(), loadOpinion);
//						}
					}
				}
			}
		} finally {
			try {
				tarIn.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, null, e);
			}
		}
		//
		tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(opinionsFileName))));
		try {
			boolean working = true;
			while (working) {
				List<LoadOpinion> clOps = getCases(tarIn, om, mapLoadOpinions, loadOpinionsPerCallback);
//				if ( ++count <= 1 ) {
//					continue;
//				}
				if (clOps.size() == 0) {
					working = false;
					courtListenerCallback.shutdown();
					break;
				}
				courtListenerCallback.callBack(clOps);
// courtListenerCallback.shutdown();
// break;
			}
		} finally {
			try {
				tarIn.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, null, e);
			}
		}
	}

	private List<LoadOpinion> getCases(TarArchiveInputStream tarIn, ObjectMapper om,
			Map<Long, LoadOpinion> mapLoadOpinions, int loadOpinionsPerCallback) throws IOException {
		TarArchiveEntry entry;
		int count = 0;
		List<LoadOpinion> clOps = new ArrayList<LoadOpinion>(loadOpinionsPerCallback);
		while ((entry = tarIn.getNextTarEntry()) != null) {
			if (tarIn.canReadEntryData(entry)) {
/*				
if ( ++total < 38 )
	continue;
*/
				int entrySize = (int) entry.getSize();
				byte[] content = new byte[entrySize];
				int offset = 0;

				while ((offset += tarIn.read(content, offset, (entrySize - offset))) != -1) {
					if (entrySize - offset == 0)
						break;
				}
				// System.out.println("Content:" + new String(content));
				ApiOpinion op = om.readValue(content, ApiOpinion.class);
				Long id = new Long(pattern.split(op.getResource_uri())[7]);
				LoadOpinion loadOpinion = mapLoadOpinions.get(id);
				if (loadOpinion != null) {
					if (op.getHtml_lawbox() != null ) {
						loadOpinion.setHtml_lawbox(op.getHtml_lawbox());
						loadOpinion.setOpinions_cited(op.getOpinions_cited());
						clOps.add(loadOpinion);
					}
					mapLoadOpinions.remove(id);
				}
				/*
				 * if( op.getPrecedentialStatus() == null ) { continue; } if
				 * (op.getPrecedentialStatus().toLowerCase().equals(
				 * "unpublished")) { continue; } if (op.getHtmlLawbox() == null)
				 * { continue; } if
				 * (op.getCitation().getCaseName().trim().length() == 0) {
				 * System.out.print('T'); continue; }
				 */

				if (++count >= loadOpinionsPerCallback)
					break;
/*				
if ( total >= 39 )
	break;
*/					
			}
		}
		return clOps;
	}
}
