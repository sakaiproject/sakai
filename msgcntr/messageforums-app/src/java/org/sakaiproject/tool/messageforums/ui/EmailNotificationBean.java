package org.sakaiproject.tool.messageforums.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.api.app.messageforums.EmailNotification;

 
public class EmailNotificationBean {

	private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationBean.class);
	private EmailNotification emailNotification;

	public EmailNotificationBean(EmailNotification curroption){
		LOG.debug("EmailNotificationBean's constructor, setting emailnotificaiton = " + curroption.getNotificationLevel());
		this.emailNotification = curroption;
	}
	public EmailNotification getEmailNotification() {
		return emailNotification;
	}

	public void setEmailNotification(EmailNotification currEmailOption) {
		this.emailNotification = currEmailOption;
	}

	
}
