package org.sakaiproject.content.copyright;

public class CopyrightItem implements org.sakaiproject.content.copyright.api.CopyrightItem {
	private String type;
	private String text;
	private String licenseUrl;
	
	public CopyrightItem(){}
	public CopyrightItem(String type, String text, String url){
		this.type = type;
		this.text = text;
		this.licenseUrl = url;
	}
	
	public void setType(String s){
		this.type = s;
	}
	public String getType(){
		return this.type;
	}

	public void setText(String s){
		this.text = s;
	}
	public String getText(){
		return this.text;
	}
	
	public void setLicenseUrl(String s){
		this.licenseUrl = s;
	}
	public String getLicenseUrl(){
		return this.licenseUrl;
	}
	
}