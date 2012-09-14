package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;

import org.sakaiproject.site.api.Site;

public class SiteSerialized implements Serializable{
	private String url;
	private String id;
	private String title;
	private String term;
	private String reference;

	public SiteSerialized(Site site, String termProp){
		this.id = site.getId();
		this.url = site.getUrl();
		this.title = site.getTitle();
		Object prop = site.getProperties().get(termProp);
		term = "";
		if(prop != null){
			term = prop.toString();
		}
		this.reference = site.getReference();
	}
	
	public SiteSerialized(String id, String title, String term){
		this.id = id;
		this.title = title;
		this.url = "/portal/site/" + id;
		this.term = term;
		this.reference = "/site/" + id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

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

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
}
