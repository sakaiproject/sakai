package org.sakaiproject.sitemanage.api;

import org.sakaiproject.user.api.User;

public interface UserNotificationProvider {

	/**
	 * Send an email to newly added user informing password
	 * 
	 * @param newnonOfficialAccount
	 * @param emailId
	 * @param userName
	 * @param siteTitle
	 */
	public void notifyNewUserEmail(User user, String newUserPassword, String siteTitle);

	/**
	 * send email notification to added participant
	 */
	public void notifyAddedParticipant(boolean newNonOfficialAccount, User user, String siteTitle); 
	
}
