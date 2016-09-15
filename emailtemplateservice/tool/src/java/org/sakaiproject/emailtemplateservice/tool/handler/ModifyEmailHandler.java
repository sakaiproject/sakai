package org.sakaiproject.emailtemplateservice.tool.handler;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.emailtemplateservice.tool.locators.EmailTemplateLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class ModifyEmailHandler{
	
	private static Logger log = LoggerFactory.getLogger(ModifyEmailHandler.class);
	
	
	private SessionManager sessionManager = null;
	
	public void setSessionManager (SessionManager sessionManager){
		this.sessionManager = sessionManager;
	}
	
	private EmailTemplateLocator emailTemplateLocator;
	public void setEmailTemplateLocator (EmailTemplateLocator emailTemplateLocator){
		this.emailTemplateLocator = emailTemplateLocator;
	}

	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}
	
	public String csrfToken=null;
	public String getCsrfToken() {
		Object sessionAttr = sessionManager.getCurrentSession().getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
		return (sessionAttr!=null)?sessionAttr.toString():"";
	}
	
	public void setCsrfToken (String csrfToken){
		this.csrfToken = csrfToken;
	}
	
	public EmailTemplateLocator getEmailTemplateLocator (){
		return emailTemplateLocator;
	}
	
	public String saveAll (){
		if (validCsrfToken()){
			return emailTemplateLocator.saveAll();
		}
	
		messages.addMessage(new TargettedMessage("java.badcsrftoken", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
		return "failure";
	}
	
	
	public boolean validCsrfToken() {
		   Object sessionAttr = sessionManager.getCurrentSession().getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
		   return (sessionAttr != null && this.csrfToken != null && StringUtils.equals(this.csrfToken,sessionAttr.toString()));
	}
	
}
