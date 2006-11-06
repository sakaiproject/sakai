package org.sakaiproject.content.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface ResourceToolActionController 
{
	public String getContent();
	
	public OutputStream getContentStream();
	
	public String getContentType();
	
	public String getPropertyValue(String name);
	
	public void setRevisedContent(String content);
	
	public void setRevisedContentStream(InputStream istream);
	
	public void setRevisedContentType(String type);
	
	public void setRevisedResourceProperty(String name, String value);
	
	public void stopHelper();
}
