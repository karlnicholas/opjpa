package statutesaz;

public class SectionContent {
	private String sectionContent;
	private String sectionNumber;
	public SectionContent(String sectionContent, String sectionNumber) {
		this.sectionContent = sectionContent;
		this.sectionNumber = sectionNumber;
	}
	public String getSectionContent() {
		return sectionContent;
	}
	public void setSectionContent(String sectionContent) {
		this.sectionContent = sectionContent;
	}
	public String getSectionNumber() {
		return sectionNumber;
	}
	public void setSectionNumber(String sectionNumber) {
		this.sectionNumber = sectionNumber;
	}

}
