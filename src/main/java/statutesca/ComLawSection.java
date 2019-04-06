package statutesca;

import java.util.Date;

public class ComLawSection {
	private String id;
	private String title;
	private String part;
	private String division;
	private String chapter;
	private String article;
	private String law_code;
	private String section_num;
	private String op_statues;
	private String op_chapter;
	private String op_section;
	private Date effective_date;
	private String law_section_version_id;
	private String history;
	private String content_xml;
	private String active_flg;
	private String trans_uid;
	private Date trans_update;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
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
	public String getLaw_code() {
		return law_code;
	}
	public void setLaw_code(String law_code) {
		this.law_code = law_code;
	}
	public String getSection_num() {
		return section_num;
	}
	public void setSection_num(String section_num) {
		this.section_num = section_num;
	}
	public String getOp_statues() {
		return op_statues;
	}
	public void setOp_statues(String op_statues) {
		this.op_statues = op_statues;
	}
	public String getOp_chapter() {
		return op_chapter;
	}
	public void setOp_chapter(String op_chapter) {
		this.op_chapter = op_chapter;
	}
	public String getOp_section() {
		return op_section;
	}
	public void setOp_section(String op_section) {
		this.op_section = op_section;
	}
	public Date getEffective_date() {
		return effective_date;
	}
	public void setEffective_date(Date effective_date) {
		this.effective_date = effective_date;
	}
	public String getLaw_section_version_id() {
		return law_section_version_id;
	}
	public void setLaw_section_version_id(String law_section_version_id) {
		this.law_section_version_id = law_section_version_id;
	}
	public String getHistory() {
		return history;
	}
	public void setHistory(String history) {
		this.history = history;
	}
	public String getContent_xml() {
		return content_xml;
	}
	public void setContent_xml(String content_xml) {
		this.content_xml = content_xml;
	}
	public String getActive_flg() {
		return active_flg;
	}
	public void setActive_flg(String active_flg) {
		this.active_flg = active_flg;
	}
	public String getTrans_uid() {
		return trans_uid;
	}
	public void setTrans_uid(String trans_uid) {
		this.trans_uid = trans_uid;
	}
	public Date getTrans_update() {
		return trans_update;
	}
	public void setTrans_update(Date trans_update) {
		this.trans_update = trans_update;
	}
	@Override
	public String toString() {
		return "LawSection [title=" + title + ", part=" + part + ", division=" + division + ", chapter=" + chapter
				+ ", article=" + article + "]";
	}
}
