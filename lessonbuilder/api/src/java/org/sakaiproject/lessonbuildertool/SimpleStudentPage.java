package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public interface SimpleStudentPage {
	public long getId();
	public void setId(long id);
	
	public Date getLastUpdated();
	public void setLastUpdated(Date lastUpdated);
	
	public long getItemId();
	public void setItemId(long itemId);
	
	public long getPageId();
	public void setPageId(long pageId);
	
	public String getTitle();
	public void setTitle(String title);
	
	public String getOwner();
	public void setOwner(String owner);
	
	public boolean getGroupOwned();
	public void setGroupOwned(boolean go);
	
	public Long getCommentsSection();
	public void setCommentsSection(Long commentsSection);
	
	public Date getLastCommentChange();
	public void setLastCommentChange(Date lastCommentChange);
	
	public boolean isDeleted();
	public void setDeleted(Boolean deleted);
	
	public Double getPoints();
	public void setPoints(Double points);
}
