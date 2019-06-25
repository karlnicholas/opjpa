package statutesaz;

public class LawSection {
	private String section_num;
	private String content_xml;
	public String getSection_num() {
		return section_num;
	}
	public LawSection(String section_num, String content_xml) {
		this.section_num = section_num;
		this.content_xml = content_xml;
	}
	public void setSection_num(String section_num) {
		this.section_num = section_num;
	}
	public String getContent_xml() {
		return content_xml;
	}
	public void setContent_xml(String content_xml) {
		this.content_xml = content_xml;
	}
	@Override
	public String toString() {
		return "LawSection [section_num=" + section_num + "]";
	}
}
