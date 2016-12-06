package apimodel;

public class ApiOpinion
{
    private String[] joined_by;
    private String html_lawbox;
    private String extracted_by_ocr;
    private String date_modified;
    private String local_path;
    private String date_created;
    private String sha1;
    private String plain_text;
    private String type;
    private String[] opinions_cited;
    private String author;
    private String html_columbia;
    private String download_url;
    private String resource_uri;
    private String cluster;
    private String html;
    private String absolute_url;
    private String html_with_citations;
    private String per_curiam;
    private String page_count;
    private String author_str;
    public String[] getJoined_by ()
    {
        return joined_by;
    }

    public void setJoined_by (String[] joined_by)
    {
        this.joined_by = joined_by;
    }

    public String getHtml_lawbox ()
    {
        return html_lawbox;
    }

    public void setHtml_lawbox (String html_lawbox)
    {
        this.html_lawbox = html_lawbox;
    }

    public String getExtracted_by_ocr ()
    {
        return extracted_by_ocr;
    }

    public void setExtracted_by_ocr (String extracted_by_ocr)
    {
        this.extracted_by_ocr = extracted_by_ocr;
    }

    public String getDate_modified ()
    {
        return date_modified;
    }

    public void setDate_modified (String date_modified)
    {
        this.date_modified = date_modified;
    }

    public String getLocal_path ()
    {
        return local_path;
    }

    public void setLocal_path (String local_path)
    {
        this.local_path = local_path;
    }

    public String getDate_created ()
    {
        return date_created;
    }

    public void setDate_created (String date_created)
    {
        this.date_created = date_created;
    }

    public String getSha1 ()
    {
        return sha1;
    }

    public void setSha1 (String sha1)
    {
        this.sha1 = sha1;
    }

    public String getPlain_text ()
    {
        return plain_text;
    }

    public void setPlain_text (String plain_text)
    {
        this.plain_text = plain_text;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String[] getOpinions_cited ()
    {
        return opinions_cited;
    }

    public void setOpinions_cited (String[] opinions_cited)
    {
        this.opinions_cited = opinions_cited;
    }

    public String getAuthor ()
    {
        return author;
    }

    public void setAuthor (String author)
    {
        this.author = author;
    }

    public String getHtml_columbia ()
    {
        return html_columbia;
    }

    public void setHtml_columbia (String html_columbia)
    {
        this.html_columbia = html_columbia;
    }

    public String getDownload_url ()
    {
        return download_url;
    }

    public void setDownload_url (String download_url)
    {
        this.download_url = download_url;
    }

    public String getResource_uri ()
    {
        return resource_uri;
    }

    public void setResource_uri (String resource_uri)
    {
        this.resource_uri = resource_uri;
    }

    public String getCluster ()
    {
        return cluster;
    }

    public void setCluster (String cluster)
    {
        this.cluster = cluster;
    }

    public String getHtml ()
    {
        return html;
    }

    public void setHtml (String html)
    {
        this.html = html;
    }

    public String getAbsolute_url ()
    {
        return absolute_url;
    }

    public void setAbsolute_url (String absolute_url)
    {
        this.absolute_url = absolute_url;
    }

    public String getHtml_with_citations ()
    {
        return html_with_citations;
    }

    public void setHtml_with_citations (String html_with_citations)
    {
        this.html_with_citations = html_with_citations;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [joined_by = "+joined_by+", html_lawbox = "+html_lawbox+", extracted_by_ocr = "+extracted_by_ocr+", date_modified = "+date_modified+", local_path = "+local_path+", date_created = "+date_created+", sha1 = "+sha1+", plain_text = "+plain_text+", type = "+type+", opinions_cited = "+opinions_cited+", author = "+author+", html_columbia = "+html_columbia+", download_url = "+download_url+", resource_uri = "+resource_uri+", cluster = "+cluster+", html = "+html+", absolute_url = "+absolute_url+", html_with_citations = "+html_with_citations+"]";
    }

	public String getPer_curiam() {
		return per_curiam;
	}

	public void setPer_curiam(String per_curiam) {
		this.per_curiam = per_curiam;
	}

	public String getPage_count() {
		return page_count;
	}

	public void setPage_count(String page_count) {
		this.page_count = page_count;
	}

	public String getAuthor_str() {
		return author_str;
	}

	public void setAuthor_str(String author_str) {
		this.author_str = author_str;
	}
}
