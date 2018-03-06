/**
 * Copyright (c) 2007-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.emailtemplateservice.tool.handler;

import org.apache.commons.lang3.StringUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

import org.sakaiproject.emailtemplateservice.tool.locators.EmailTemplateLocator;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.SessionManager;

public class ModifyEmailHandler{

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
