package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public interface SimplePageComment {
	
	public long getId();
	public void setId(long id);
	
	public long getItemId();
	public void setItemId(long itemId);
	
	public long getPageId();
	public void setPageId(long pageId);
	
	public Date getTimePosted();
	public void setTimePosted(Date date);
	
	public String getAuthor();
	public void setAuthor(String author);
	
	public String getComment();
	public void setComment(String comment);
	
	public String getUUID();
	public void setUUID(String UUID);
	
	public boolean getHtml();
	public void setHtml(boolean html);
	
	public int compareTo(Object o);
	
	public Double getPoints();
	public void setPoints(Double points);
	
}
