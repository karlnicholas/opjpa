package clread.jsonmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@JsonPropertyOrder({ "case_name", "docket_number", "document_uris",
		"federal_cite_one", "federal_cite_three", "federal_cite_two", "id",
		"lexis_cite", "neutral_cite", "resource_uri", "scotus_early_cite",
		"specialty_cite_one", "state_cite_one", "state_cite_regional",
		"state_cite_three", "state_cite_two", "westlaw_cite" })
public class Citation {

	@JsonProperty("case_name")
	private String caseName;
	@JsonProperty("docket_number")
	private String docketNumber;
	@JsonProperty("document_uris")
	private List<String> documentUris = new ArrayList<String>();
	@JsonProperty("federal_cite_one")
	private Object federalCiteOne;
	@JsonProperty("federal_cite_three")
	private Object federalCiteThree;
	@JsonProperty("federal_cite_two")
	private Object federalCiteTwo;
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("lexis_cite")
	private Object lexisCite;
	@JsonProperty("neutral_cite")
	private Object neutralCite;
	@JsonProperty("resource_uri")
	private String resourceUri;
	@JsonProperty("scotus_early_cite")
	private Object scotusEarlyCite;
	@JsonProperty("specialty_cite_one")
	private Object specialtyCiteOne;
	@JsonProperty("state_cite_one")
	private String stateCiteOne;
	@JsonProperty("state_cite_regional")
	private Object stateCiteRegional;
	@JsonProperty("state_cite_three")
	private Object stateCiteThree;
	@JsonProperty("state_cite_two")
	private String stateCiteTwo;
	@JsonProperty("westlaw_cite")
	private Object westlawCite;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The caseName
	 */
	@JsonProperty("case_name")
	public String getCaseName() {
		return caseName;
	}

	/**
	 * 
	 * @param caseName
	 *            The case_name
	 */
	@JsonProperty("case_name")
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	/**
	 * 
	 * @return The docketNumber
	 */
	@JsonProperty("docket_number")
	public String getDocketNumber() {
		return docketNumber;
	}

	/**
	 * 
	 * @param docketNumber
	 *            The docket_number
	 */
	@JsonProperty("docket_number")
	public void setDocketNumber(String docketNumber) {
		this.docketNumber = docketNumber;
	}

	/**
	 * 
	 * @return The documentUris
	 */
	@JsonProperty("document_uris")
	public List<String> getDocumentUris() {
		return documentUris;
	}

	/**
	 * 
	 * @param documentUris
	 *            The document_uris
	 */
	@JsonProperty("document_uris")
	public void setDocumentUris(List<String> documentUris) {
		this.documentUris = documentUris;
	}

	/**
	 * 
	 * @return The federalCiteOne
	 */
	@JsonProperty("federal_cite_one")
	public Object getFederalCiteOne() {
		return federalCiteOne;
	}

	/**
	 * 
	 * @param federalCiteOne
	 *            The federal_cite_one
	 */
	@JsonProperty("federal_cite_one")
	public void setFederalCiteOne(Object federalCiteOne) {
		this.federalCiteOne = federalCiteOne;
	}

	/**
	 * 
	 * @return The federalCiteThree
	 */
	@JsonProperty("federal_cite_three")
	public Object getFederalCiteThree() {
		return federalCiteThree;
	}

	/**
	 * 
	 * @param federalCiteThree
	 *            The federal_cite_three
	 */
	@JsonProperty("federal_cite_three")
	public void setFederalCiteThree(Object federalCiteThree) {
		this.federalCiteThree = federalCiteThree;
	}

	/**
	 * 
	 * @return The federalCiteTwo
	 */
	@JsonProperty("federal_cite_two")
	public Object getFederalCiteTwo() {
		return federalCiteTwo;
	}

	/**
	 * 
	 * @param federalCiteTwo
	 *            The federal_cite_two
	 */
	@JsonProperty("federal_cite_two")
	public void setFederalCiteTwo(Object federalCiteTwo) {
		this.federalCiteTwo = federalCiteTwo;
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
	 * @return The lexisCite
	 */
	@JsonProperty("lexis_cite")
	public Object getLexisCite() {
		return lexisCite;
	}

	/**
	 * 
	 * @param lexisCite
	 *            The lexis_cite
	 */
	@JsonProperty("lexis_cite")
	public void setLexisCite(Object lexisCite) {
		this.lexisCite = lexisCite;
	}

	/**
	 * 
	 * @return The neutralCite
	 */
	@JsonProperty("neutral_cite")
	public Object getNeutralCite() {
		return neutralCite;
	}

	/**
	 * 
	 * @param neutralCite
	 *            The neutral_cite
	 */
	@JsonProperty("neutral_cite")
	public void setNeutralCite(Object neutralCite) {
		this.neutralCite = neutralCite;
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
	 * @return The scotusEarlyCite
	 */
	@JsonProperty("scotus_early_cite")
	public Object getScotusEarlyCite() {
		return scotusEarlyCite;
	}

	/**
	 * 
	 * @param scotusEarlyCite
	 *            The scotus_early_cite
	 */
	@JsonProperty("scotus_early_cite")
	public void setScotusEarlyCite(Object scotusEarlyCite) {
		this.scotusEarlyCite = scotusEarlyCite;
	}

	/**
	 * 
	 * @return The specialtyCiteOne
	 */
	@JsonProperty("specialty_cite_one")
	public Object getSpecialtyCiteOne() {
		return specialtyCiteOne;
	}

	/**
	 * 
	 * @param specialtyCiteOne
	 *            The specialty_cite_one
	 */
	@JsonProperty("specialty_cite_one")
	public void setSpecialtyCiteOne(Object specialtyCiteOne) {
		this.specialtyCiteOne = specialtyCiteOne;
	}

	/**
	 * 
	 * @return The stateCiteOne
	 */
	@JsonProperty("state_cite_one")
	public String getStateCiteOne() {
		return stateCiteOne;
	}

	/**
	 * 
	 * @param stateCiteOne
	 *            The state_cite_one
	 */
	@JsonProperty("state_cite_one")
	public void setStateCiteOne(String stateCiteOne) {
		this.stateCiteOne = stateCiteOne;
	}

	/**
	 * 
	 * @return The stateCiteRegional
	 */
	@JsonProperty("state_cite_regional")
	public Object getStateCiteRegional() {
		return stateCiteRegional;
	}

	/**
	 * 
	 * @param stateCiteRegional
	 *            The state_cite_regional
	 */
	@JsonProperty("state_cite_regional")
	public void setStateCiteRegional(Object stateCiteRegional) {
		this.stateCiteRegional = stateCiteRegional;
	}

	/**
	 * 
	 * @return The stateCiteThree
	 */
	@JsonProperty("state_cite_three")
	public Object getStateCiteThree() {
		return stateCiteThree;
	}

	/**
	 * 
	 * @param stateCiteThree
	 *            The state_cite_three
	 */
	@JsonProperty("state_cite_three")
	public void setStateCiteThree(Object stateCiteThree) {
		this.stateCiteThree = stateCiteThree;
	}

	/**
	 * 
	 * @return The stateCiteTwo
	 */
	@JsonProperty("state_cite_two")
	public String getStateCiteTwo() {
		return stateCiteTwo;
	}

	/**
	 * 
	 * @param stateCiteTwo
	 *            The state_cite_two
	 */
	@JsonProperty("state_cite_two")
	public void setStateCiteTwo(String stateCiteTwo) {
		this.stateCiteTwo = stateCiteTwo;
	}

	/**
	 * 
	 * @return The westlawCite
	 */
	@JsonProperty("westlaw_cite")
	public Object getWestlawCite() {
		return westlawCite;
	}

	/**
	 * 
	 * @param westlawCite
	 *            The westlaw_cite
	 */
	@JsonProperty("westlaw_cite")
	public void setWestlawCite(Object westlawCite) {
		this.westlawCite = westlawCite;
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
