package org.sakaiproject.api.app.messageforums;

/**
 * A user's anonymous ID must remain consistent across a site (so they can be graded consistently). 
 * But the anonymous ID should be different in other sites to prevent any way to deduce identities based on mutual enrollments.
 * So, each row maps siteIds to userIds to anonIds
 * @author bbailla2
 */
public interface AnonymousMapping
{
	public String getSiteId();
	public void setSiteId(String siteId);

	public String getUserId();
	public void setUserId(String userId);

	public String getAnonId();
	public void setAnonId(String anonId);
}
