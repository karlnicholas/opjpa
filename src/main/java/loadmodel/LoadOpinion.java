package loadmodel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import apimodel.Cluster;

public class LoadOpinion {
	private static final Pattern pattern = Pattern.compile("/");
	private Long id;
	private String citation;
	private Date dateFiled;
	private String caseName;
	private String fullCaseName;
	private String shortCaseName;
	//
    private String html_lawbox;
    private String[] opinions_cited;
    //
    private DateFormat dateFormat;
    //
    private String clusterSource;
    public LoadOpinion() {}
    public LoadOpinion(Cluster cluster) {
        // http://www.courtlistener.com/api/rest/v3/clusters/1361768/
    	id = new Long(pattern.split(cluster.getResource_uri())[7]);
    	clusterSource = cluster.getSource();
		dateFiled = cluster.getDate_filed();
		String citeOne = cluster.getState_cite_one().replace(". ", "."); 
		String citeTwo = cluster.getState_cite_two().replace(". ", "."); 
		String citeThree = cluster.getState_cite_three().replace(". ", ".");
		if ( citeOne.contains("Cal.App.") ) {
			citation = citeOne;
    	} else if ( citeTwo.contains("Cal.App.") ) {
			citation = citeTwo;
    	} else if ( citeThree.contains("Cal.App.") ) {
			citation = citeThree;
    	} else if ( citeOne.contains("Cal.") && !citeOne.contains("Rptr") ) {
			citation = citeOne;
    	} else if ( citeTwo.contains("Cal.") && !citeTwo.contains("Rptr") ) {
			citation = citeTwo;
    	} else if ( citeThree.contains("Cal.") && !citeThree.contains("Rptr") ) {
			citation = citeThree;
    	}
/*		
		if ( citation == null ) {
			if ( citeOne != null && !citeOne.trim().isEmpty() ) System.out.println(citeOne);
			if ( citeTwo != null && !citeTwo.trim().isEmpty() ) System.out.println(citeTwo);
			if ( citeThree != null && !citeThree.trim().isEmpty() ) System.out.println(citeThree);
//			System.out.println(++total);
		}
*/
		caseName = cluster.getCase_name();
		fullCaseName = cluster.getCase_name_full();
		shortCaseName = cluster.getCase_name_short();
	}
	//
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCitation() {
		return citation;
	}
	public void setCitation(String citation) {
		this.citation = citation;
	}
	public Date getDateFiled() {
		return dateFiled;
	}
	public void setDateFiled(Date dateFiled) {
		this.dateFiled = dateFiled;
	}
	public String getFullCaseName() {
		return fullCaseName;
	}
	public void setFullCaseName(String fullCaseName) {
		this.fullCaseName = fullCaseName;
	}
	public String getShortCaseName() {
		return shortCaseName;
	}
	public void setShortCaseName(String shortCaseName) {
		this.shortCaseName = shortCaseName;
	}
	public String getHtml_lawbox() {
		return html_lawbox;
	}
	public void setHtml_lawbox(String html_lawbox) {
		this.html_lawbox = html_lawbox;
	}
	public String[] getOpinions_cited() {
		return opinions_cited;
	}
	public void setOpinions_cited(String[] opinions_cited) {
		this.opinions_cited = opinions_cited;
	}
	public String getCaseName() {
		return caseName;
	}
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	public String getClusterSource() {
		return clusterSource;
	}
	@Override
	public String toString() {
		if ( dateFormat == null ) {
			dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		}
		return new StringBuilder("LoadOpinion: ").append(caseName).append(" (").append(citation).append(") ").append(dateFormat.format(dateFiled)).toString();
	}
}
