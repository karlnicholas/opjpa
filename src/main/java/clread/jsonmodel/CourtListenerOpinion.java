package clread.jsonmodel;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "absolute_url", "blocked", "citation", "citation_count",
		"court", "date_blocked", "date_filed", "date_modified", "docket",
		"download_url", "extracted_by_ocr", "html", "html_lawbox",
		"html_with_citations", "id", "judges", "local_path", "nature_of_suit",
		"plain_text", "precedential_status", "resource_uri", "sha1", "source",
		"time_retrieved" })
public class CourtListenerOpinion {

	@JsonProperty("absolute_url")
	private String absoluteUrl;
	@JsonProperty("blocked")
	private Boolean blocked;
	@JsonProperty("citation")
	private Citation citation;
	@JsonProperty("citation_count")
	private Integer citationCount;
	@JsonProperty("court")
	private String court;
	@JsonProperty("date_blocked")
	private Object dateBlocked;
	@JsonProperty("date_filed")
	private String dateFiled;
	@JsonProperty("date_modified")
	private String dateModified;
	@JsonProperty("docket")
	private String docket;
	@JsonProperty("download_url")
	private Object downloadUrl;
	@JsonProperty("extracted_by_ocr")
	private Boolean extractedByOcr;
	@JsonProperty("html")
	private String html;
	@JsonProperty("html_lawbox")
	private String htmlLawbox;
	@JsonProperty("html_with_citations")
	private String htmlWithCitations;
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("judges")
	private String judges;
	@JsonProperty("local_path")
	private Object localPath;
	@JsonProperty("nature_of_suit")
	private String natureOfSuit;
	@JsonProperty("plain_text")
	private String plainText;
	@JsonProperty("precedential_status")
	private String precedentialStatus;
	@JsonProperty("resource_uri")
	private String resourceUri;
	@JsonProperty("sha1")
	private String sha1;
	@JsonProperty("source")
	private String source;
	@JsonProperty("time_retrieved")
	private String timeRetrieved;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The absoluteUrl
	 */
	@JsonProperty("absolute_url")
	public String getAbsoluteUrl() {
		return absoluteUrl;
	}

	/**
	 * 
	 * @param absoluteUrl
	 *            The absolute_url
	 */
	@JsonProperty("absolute_url")
	public void setAbsoluteUrl(String absoluteUrl) {
		this.absoluteUrl = absoluteUrl;
	}

	/**
	 * 
	 * @return The blocked
	 */
	@JsonProperty("blocked")
	public Boolean getBlocked() {
		return blocked;
	}

	/**
	 * 
	 * @param blocked
	 *            The blocked
	 */
	@JsonProperty("blocked")
	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

	/**
	 * 
	 * @return The citation
	 */
	@JsonProperty("citation")
	public Citation getCitation() {
		return citation;
	}

	/**
	 * 
	 * @param citation
	 *            The citation
	 */
	@JsonProperty("citation")
	public void setCitation(Citation citation) {
		this.citation = citation;
	}

	/**
	 * 
	 * @return The citationCount
	 */
	@JsonProperty("citation_count")
	public Integer getCitationCount() {
		return citationCount;
	}

	/**
	 * 
	 * @param citationCount
	 *            The citation_count
	 */
	@JsonProperty("citation_count")
	public void setCitationCount(Integer citationCount) {
		this.citationCount = citationCount;
	}

	/**
	 * 
	 * @return The court
	 */
	@JsonProperty("court")
	public String getCourt() {
		return court;
	}

	/**
	 * 
	 * @param court
	 *            The court
	 */
	@JsonProperty("court")
	public void setCourt(String court) {
		this.court = court;
	}

	/**
	 * 
	 * @return The dateBlocked
	 */
	@JsonProperty("date_blocked")
	public Object getDateBlocked() {
		return dateBlocked;
	}

	/**
	 * 
	 * @param dateBlocked
	 *            The date_blocked
	 */
	@JsonProperty("date_blocked")
	public void setDateBlocked(Object dateBlocked) {
		this.dateBlocked = dateBlocked;
	}

	/**
	 * 
	 * @return The dateFiled
	 */
	@JsonProperty("date_filed")
	public String getDateFiled() {
		return dateFiled;
	}

	/**
	 * 
	 * @param dateFiled
	 *            The date_filed
	 */
	@JsonProperty("date_filed")
	public void setDateFiled(String dateFiled) {
		this.dateFiled = dateFiled;
	}

	/**
	 * 
	 * @return The dateModified
	 */
	@JsonProperty("date_modified")
	public String getDateModified() {
		return dateModified;
	}

	/**
	 * 
	 * @param dateModified
	 *            The date_modified
	 */
	@JsonProperty("date_modified")
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	/**
	 * 
	 * @return The docket
	 */
	@JsonProperty("docket")
	public String getDocket() {
		return docket;
	}

	/**
	 * 
	 * @param docket
	 *            The docket
	 */
	@JsonProperty("docket")
	public void setDocket(String docket) {
		this.docket = docket;
	}

	/**
	 * 
	 * @return The downloadUrl
	 */
	@JsonProperty("download_url")
	public Object getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * 
	 * @param downloadUrl
	 *            The download_url
	 */
	@JsonProperty("download_url")
	public void setDownloadUrl(Object downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	/**
	 * 
	 * @return The extractedByOcr
	 */
	@JsonProperty("extracted_by_ocr")
	public Boolean getExtractedByOcr() {
		return extractedByOcr;
	}

	/**
	 * 
	 * @param extractedByOcr
	 *            The extracted_by_ocr
	 */
	@JsonProperty("extracted_by_ocr")
	public void setExtractedByOcr(Boolean extractedByOcr) {
		this.extractedByOcr = extractedByOcr;
	}

	/**
	 * 
	 * @return The html
	 */
	@JsonProperty("html")
	public String getHtml() {
		return html;
	}

	/**
	 * 
	 * @param html
	 *            The html
	 */
	@JsonProperty("html")
	public void setHtml(String html) {
		this.html = html;
	}

	/**
	 * 
	 * @return The htmlLawbox
	 */
	@JsonProperty("html_lawbox")
	public String getHtmlLawbox() {
		return htmlLawbox;
	}

	/**
	 * 
	 * @param htmlLawbox
	 *            The html_lawbox
	 */
	@JsonProperty("html_lawbox")
	public void setHtmlLawbox(String htmlLawbox) {
		this.htmlLawbox = htmlLawbox;
	}

	/**
	 * 
	 * @return The htmlWithCitations
	 */
	@JsonProperty("html_with_citations")
	public String getHtmlWithCitations() {
		return htmlWithCitations;
	}

	/**
	 * 
	 * @param htmlWithCitations
	 *            The html_with_citations
	 */
	@JsonProperty("html_with_citations")
	public void setHtmlWithCitations(String htmlWithCitations) {
		this.htmlWithCitations = htmlWithCitations;
	}

	/**
	 * 
	 * @return The id
	 */
	@JsonProperty("id")
	public Integer getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            The id
	 */
	@JsonProperty("id")
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 
	 * @return The judges
	 */
	@JsonProperty("judges")
	public String getJudges() {
		return judges;
	}

	/**
	 * 
	 * @param judges
	 *            The judges
	 */
	@JsonProperty("judges")
	public void setJudges(String judges) {
		this.judges = judges;
	}

	/**
	 * 
	 * @return The localPath
	 */
	@JsonProperty("local_path")
	public Object getLocalPath() {
		return localPath;
	}

	/**
	 * 
	 * @param localPath
	 *            The local_path
	 */
	@JsonProperty("local_path")
	public void setLocalPath(Object localPath) {
		this.localPath = localPath;
	}

	/**
	 * 
	 * @return The natureOfSuit
	 */
	@JsonProperty("nature_of_suit")
	public String getNatureOfSuit() {
		return natureOfSuit;
	}

	/**
	 * 
	 * @param natureOfSuit
	 *            The nature_of_suit
	 */
	@JsonProperty("nature_of_suit")
	public void setNatureOfSuit(String natureOfSuit) {
		this.natureOfSuit = natureOfSuit;
	}

	/**
	 * 
	 * @return The plainText
	 */
	@JsonProperty("plain_text")
	public String getPlainText() {
		return plainText;
	}

	/**
	 * 
	 * @param plainText
	 *            The plain_text
	 */
	@JsonProperty("plain_text")
	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	/**
	 * 
	 * @return The precedentialStatus
	 */
	@JsonProperty("precedential_status")
	public String getPrecedentialStatus() {
		return precedentialStatus;
	}

	/**
	 * 
	 * @param precedentialStatus
	 *            The precedential_status
	 */
	@JsonProperty("precedential_status")
	public void setPrecedentialStatus(String precedentialStatus) {
		this.precedentialStatus = precedentialStatus;
	}

	/**
	 * 
	 * @return The resourceUri
	 */
	@JsonProperty("resource_uri")
	public String getResourceUri() {
		return resourceUri;
	}

	/**
	 * 
	 * @param resourceUri
	 *            The resource_uri
	 */
	@JsonProperty("resource_uri")
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	/**
	 * 
	 * @return The sha1
	 */
	@JsonProperty("sha1")
	public String getSha1() {
		return sha1;
	}

	/**
	 * 
	 * @param sha1
	 *            The sha1
	 */
	@JsonProperty("sha1")
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	/**
	 * 
	 * @return The source
	 */
	@JsonProperty("source")
	public String getSource() {
		return source;
	}

	/**
	 * 
	 * @param source
	 *            The source
	 */
	@JsonProperty("source")
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * 
	 * @return The timeRetrieved
	 */
	@JsonProperty("time_retrieved")
	public String getTimeRetrieved() {
		return timeRetrieved;
	}

	/**
	 * 
	 * @param timeRetrieved
	 *            The time_retrieved
	 */
	@JsonProperty("time_retrieved")
	public void setTimeRetrieved(String timeRetrieved) {
		this.timeRetrieved = timeRetrieved;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
