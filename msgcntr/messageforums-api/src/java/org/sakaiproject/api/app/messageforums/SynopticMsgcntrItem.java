package org.sakaiproject.api.app.messageforums;

import java.util.Date;

public interface SynopticMsgcntrItem {
	
	/**
	 * resets new messages count to 0
	 */
	public void resetNewMessagesCount();
	
	/**
	 * resets new forum count to 0
	 */
	public void resetNewForumCount();
	
	/**
	 * adds 1 to new messages count
	 */
	public void incrementNewMessagesCount();
	
	/**
	 * adds 1 to new forum count
	 */
	public void incrementNewForumCount();
	
	/**
	 * subs 1 to new messages count
	 */
	public void decrementNewMessagesCount();
	
	/**
	 * subs 1 to new forum count
	 */
	public void decrementNewForumCount();
	
	/**
	 * sets the messages last visit date to current time
	 */
	public void setMessagesLastVisitToCurrentDt();
	
	/**
	 * sets the forum last visit date to current time
	 */
	public void setForumLastVisitToCurrentDt();
	
	public String getSiteId();
	
	public void setSiteId(String siteId);
	
	public String getSiteTitle();
	
	public void setSiteTitle(String siteTitle);
	
	public int getNewMessagesCount();
	
	public void setNewMessagesCount(int newMessagesCount);
	
	public Date getMessagesLastVisit();
	
	public void setMessagesLastVisit(Date messagesLastVisit);
	
	public int getNewForumCount();
	
	public void setNewForumCount(int newForumCount);
	
	public Date getForumLastVisit();
	
	public void setForumLastVisit(Date forumLastVisit);
	
	public String getUserId();
	
	public void setUserId(String userId);
	
	public Long getId();

	public void setId(Long id);
	
	/**
	 * Used for optimistic locking
	 * @return
	 */
	public Integer getVersion();

	public void setVersion(Integer version);
	
	public boolean isHideItem();

	public void setHideItem(boolean hideItem);
		
}
