package courtlistener;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import codesparser.CodesInterface;
import gscalifornia.factory.CAStatutesFactory;
import load.CourtListenerCallback;
import load.LoadCourtListenerCallback;
import load.LoadCourtListenerFiles;
import loadmodel.LoadOpinion;
import opca.memorydb.CitationStore;
import opca.model.OpinionSummary;

public class TestCourtListenerFiles {
	public static void main(String...strings) {
		new TestCourtListenerFiles().run();
	}
	//
	private final CitationStore citationStore;
	private int totalAccepted;
	private int totalCited;
	private Set<String> citations;
	
	private TestCourtListenerFiles() {
    	this.citationStore = CitationStore.getInstance();
    	citations = new TreeSet<String>();
		
	}
	private void run() {
    	try {
    		
    		int newlyLoaded = 0;
    		int notNewlyLoaded = 0;    		
    		int titled = 0;
    		int notTitled= 0;    		
    	    CodesInterface codesInterface = CAStatutesFactory.getInstance().getCodesInterface(true);
    	    LoadCourtListenerCallback cb1 = new LoadCourtListenerCallback(citationStore, codesInterface);
    	    LoadCourtListenerFiles file1 = new LoadCourtListenerFiles(cb1);
    	    file1.loadFiles("c:/users/karl/downloads/calctapp-opinions.tar.gz", "c:/users/karl/downloads/calctapp-clusters.tar.gz", 1000);

    	    LoadCourtListenerCallback cb2 = new LoadCourtListenerCallback(citationStore, codesInterface);
    	    LoadCourtListenerFiles file2 = new LoadCourtListenerFiles(cb2);
    	    file2.loadFiles("c:/users/karl/downloads/cal-opinions.tar.gz", "c:/users/karl/downloads/cal-clusters.tar.gz", 1000);

    	    for (  OpinionSummary op: citationStore.getAllOpinions() ) {
	    		if ( op.isNewlyLoadedOpinion() ) {
	    			newlyLoaded++;
	    		} else {
	    			notNewlyLoaded++;	
	    		}
	    		if ( op.getTitle() == null || op.getTitle().trim().isEmpty()) {
	    			notTitled++;
	    		} else {
	    			titled++;
	    		}
	    	}
	    	System.out.println("newlyLoaded = " + newlyLoaded + " notNewlyLoaded = " + notNewlyLoaded);
	    	System.out.println("titled = " + titled + " notTitled = " + notTitled);
	    	System.out.println("citations = " + citationStore.getAllOpinions().size());

	    	try ( BufferedWriter writer = Files.newBufferedWriter(Paths.get("not-titled.txt"))) {
	    		citationStore.getAllOpinions().forEach( op->{
		    		try {
			    		if ( !op.isNewlyLoadedOpinion() ) {
							writer.write(op.getOpinionKey().toString());
							writer.write(" Cited By: ");
							writer.write(op.getReferringOpinions().toString());
							writer.newLine();
			    		}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		});
	    	    writer.close();
	    	}
//	    	System.out.println("total1 = " + total1 + " total2 = " + total2);

/*	    	
    		totalAccepted = 0;
    		totalCited = 0;
    		LoadCourtListenerFiles file1 = new LoadCourtListenerFiles(new TestCourtListenerCallback());
    		file1.loadFiles("c:/users/karl/downloads/calctapp-opinions.tar.gz", "c:/users/karl/downloads/calctapp-clusters.tar.gz", 1000);
    		LoadCourtListenerFiles file2 = new LoadCourtListenerFiles(new TestCourtListenerCallback());
    		file2.loadFiles("c:/users/karl/downloads/cal-opinions.tar.gz", "c:/users/karl/downloads/cal-clusters.tar.gz", 1000);
	    	
	    	System.out.println("file1: total = " + file1.getTotal() + " totalRead = " + file1.getTotalRead() + " totalLoaded = " + file1.getTotalLoaded() + " totalLawbox = " + file1.getTotalLawbox() + " accepted = " + file1.getAccepted() + " ClusterCount = " + file1.getClusterCount() + " mapLeft = " + file1.getMapLeft() + " nullCount = " + file1.getNullCount());
	    	System.out.println("file2: total = " + file2.getTotal() + " totalRead = " + file2.getTotalRead() + " totalLoaded = " + file2.getTotalLoaded() + " totalLawbox = " + file2.getTotalLawbox() + " accepted = " + file2.getAccepted() + " ClusterCount = " + file2.getClusterCount() + " mapLeft = " + file2.getMapLeft() + " nullCount = " + file2.getNullCount());
	    	System.out.println("total: total = " + (file1.getTotal()+file2.getTotal()) + " totalLoaded = " + (file1.getTotalLoaded()+file2.getTotalLoaded()) + " totalLawbox = " + (file1.getTotalLawbox()+file2.getTotalLawbox()) + " accepted = " + (file1.getAccepted()+file2.getAccepted()) + " ClusterCount = " + (file1.getClusterCount()+file2.getClusterCount()) + " mapLeft = " + (file1.getMapLeft()+file2.getMapLeft()));
	    	System.out.println("totalAccepted = " + totalAccepted + " totalCited = " + totalCited);
	    	System.out.println("citations = " + citations.size());
	    	
	    	System.out.println( LoadCourtListenerFiles.sourceCount );
*/	    	

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private class TestCourtListenerCallback implements CourtListenerCallback {

		@Override
		public void callBack(List<LoadOpinion> clOps) {
			totalAccepted += clOps.size();

			clOps.forEach(op->{ 
				if ( op.getCitation() != null) {
					totalCited++;
					citations.add(op.getCitation());
				}
			});

		}

		@Override
		public void shutdown() {
			// TODO Auto-generated method stub
			
		}			
	}

}
