package org.sakaiproject.content.copyright.api;

public interface CopyrightItem {
	
	public void setType(String s);
	public String getType();

	public void setText(String s);
	public String getText();

	public void setLicenseUrl(String s);
	public String getLicenseUrl();
	
}