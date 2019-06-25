package statutesaz;

import java.util.ArrayList;
import java.util.List;

public class LawForCodeSections {
	private String division;
	private String title;
	private String part;
	private String chapter;
	private String article;
	private String heading;
	private String active_flg;
	private Integer node_level;
	private Integer node_position;
	private String node_treepath;
	private String contains_law_sections;
	private List<LawSection> sections;
	public LawForCodeSections() {
		this.node_treepath = "";
	}
	public LawForCodeSections(LawForCode lawForCode) {
		this.division = lawForCode.getDivision();
		this.title = lawForCode.getTitle();
		this.part = lawForCode.getPart();
		this.chapter = lawForCode.getChapter();
		this.article = lawForCode.getArticle();
		this.heading = lawForCode.getHeading();
		this.active_flg = lawForCode.getActive_flg();
		this.node_level = lawForCode.getNode_level();
		this.node_position = lawForCode.getNode_position();
		this.node_treepath = lawForCode.getNode_treepath();
		this.contains_law_sections = lawForCode.getContains_law_sections();
		this.sections = new ArrayList<>();
		this.sections.add(new LawSection(lawForCode.getSection_num(), lawForCode.getContent_xml()));
	}
	public LawForCodeSections reducer(LawForCodeSections lawForCodeSections) {
		if ( this.node_treepath.equals(lawForCodeSections.getNode_treepath())) {
			this.sections.addAll(lawForCodeSections.getSections());
		}
		return this;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPart() {
		return part;
	}
	public void setPart(String part) {
		this.part = part;
	}
	public String getChapter() {
		return chapter;
	}
	public void setChapter(String chapter) {
		this.chapter = chapter;
	}
	public String getArticle() {
		return article;
	}
	public void setArticle(String article) {
		this.article = article;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public String getActive_flg() {
		return active_flg;
	}
	public void setActive_flg(String active_flg) {
		this.active_flg = active_flg;
	}
	public Integer getNode_level() {
		return node_level;
	}
	public void setNode_level(Integer node_level) {
		this.node_level = node_level;
	}
	public Integer getNode_position() {
		return node_position;
	}
	public void setNode_position(Integer node_position) {
		this.node_position = node_position;
	}
	public String getNode_treepath() {
		return node_treepath;
	}
	public void setNode_treepath(String node_treepath) {
		this.node_treepath = node_treepath;
	}
	public String getContains_law_sections() {
		return contains_law_sections;
	}
	public void setContains_law_sections(String contains_law_sections) {
		this.contains_law_sections = contains_law_sections;
	}
	public List<LawSection> getSections() {
		return sections;
	}
	public void setSections(List<LawSection> sections) {
		this.sections = sections;
	}
}
