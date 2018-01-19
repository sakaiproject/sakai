/**
 * $Id$
 * $URL$
 * ModifyEmailProducer.java - evaluation - Feb 29, 2008 6:06:42 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.emailtemplateservice.tool.producers;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.service.external.ExternalLogic;
import org.sakaiproject.emailtemplateservice.tool.locators.EmailTemplateLocator;
import org.sakaiproject.emailtemplateservice.tool.handler.ModifyEmailHandler;
import org.sakaiproject.emailtemplateservice.tool.params.EmailTemplateViewParams;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Page for Modifying Email templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class ModifyEmailProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

	public static final String VIEW_ID = "modify_email";
	public String getViewID() {
		return VIEW_ID;
	}

	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService ets) {
		emailTemplateService = ets;
	}


	private ExternalLogic externalLogic;	
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService uds) {
		this.userDirectoryService = uds;
	}


	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}
	
	private ModifyEmailHandler handler;
	public void setHandler (ModifyEmailHandler handler){
		this.handler = handler;
	}
	
	private String emailTemplateLocator = "emailTemplateLocator.";

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		//is this user admin?
		if (!externalLogic.isSuperUser()) {
			messages.addMessage(new TargettedMessage("tool.notAdmin", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		
		
		// handle the input params for the view
		EmailTemplateViewParams emailViewParams = (EmailTemplateViewParams) viewparams;


		String actionBean = "modifyEmailHandler.";
		EmailTemplate template = null;
		// form the proper OTP path
		boolean newEmailTemplate = true;
		String emailTemplateId = EmailTemplateLocator.NEW_1; // default is new one of the supplied type
		if (emailViewParams.id == null) {
			log.debug("this is a new tamplate");
			template = new EmailTemplate();
		} else {

			emailTemplateId = emailViewParams.id;
			template = emailTemplateService.getEmailTemplateById(Long.valueOf(emailTemplateId));
			newEmailTemplate = false;
		}
		String emailTemplateOTP = actionBean + emailTemplateLocator + emailTemplateId + ".";

		// local variables used in the render logic
		/* not needed?
      String currentUserRef = developerHelperService.getCurrentUserReference();

      boolean userAdmin = developerHelperService.isUserAdmin(currentUserRef);



      if (emailViewParams.evaluationId == null) {
         /*
		 * top links here
		 */
		/*     UIInternalLink.make(tofill, "summary-link", 
               UIMessage.make("summary.page.title"), 
               new SimpleViewParameters(SummaryProducer.VIEW_ID));

         if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                  UIMessage.make("administrate.page.title"),
                  new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         }

         UIInternalLink.make(tofill, "control-emailtemplates-link",
               UIMessage.make("controlemailtemplates.page.title"),
               new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));
      }
		 */

		String headerName = template.getKey();
		if (template.getLocale() != null && !template.getLocale().trim().equals("")) 
			headerName = headerName + " (" + template.getLocale() + ")";

		if (!newEmailTemplate) {
			UIMessage.make(tofill, "modify-template-header", "modifyemail.modify.template.header", 
					new Object[] {headerName});
		} else {
			UIMessage.make(tofill, "modify-template-header", "modifyemail.new.template.header");
		}

		UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

		UIForm form = UIForm.make(tofill, "emailTemplateForm");

		String actionBinding = null;
		actionBinding = actionBean + "saveAll";

		if (template.getId() != null) {
			// bind in the evaluationId
			//form.parameters.add(new UIELBinding(actionBean + "id", template.getId().toString()));
			//actionBinding = actionBean + "saveAndAssignEmailTemplate";
			//form.parameters.add(new UIELBinding(actionBean + "locale", template.getLocale()));
		}

		// add in the close window control
		UIMessage.make(tofill, "closeWindow", "general.close.window.button");
		if (! newEmailTemplate) {
			// add in the reset to default if not a new email template
			UICommand resetCommand = UICommand.make(form, "resetEmailTemplate", UIMessage.make("modifyemail.reset.to.default.link"), 
					actionBean + "resetToDefaultEmailTemplate");
			//resetCommand.addParameter( new UIELBinding(actionBean + "emailTemplateType", emailViewParams.emailType) );
		}
		/*
      } else {
         // not part of an evaluation so use the WBL
         actionBinding = emailTemplateLocator + "saveAll";
         // add in a cancel button
         UIMessage.make(form, "cancel-button", "general.cancel.button");
      }
		 */


		UIInput.make(form, "emailSubject", emailTemplateOTP + "subject",template.getSubject());
		UIInput.make(form, "emailFrom", emailTemplateOTP + "from",template.getFrom());
		UIInput.make(form, "emailKey", emailTemplateOTP + "key",template.getKey());
		UIInput.make(form, "emailLocale", emailTemplateOTP + "locale",template.getLocale());
		UIInput.make(form, "emailMessage", emailTemplateOTP + "message",template.getMessage());
		UIInput.make(form, "emailHtmlMessage", emailTemplateOTP + "htmlMessage",template.getHtmlMessage());
		UIInput.make(form, "csrfToken","#{modifyEmailHandler.csrfToken}",handler.getCsrfToken());
		form.parameters.add(new UIELBinding(emailTemplateOTP + "owner", userDirectoryService.getCurrentUser().getId()));
		UICommand.make(form, "saveEmailTemplate", UIMessage.make("modifyemail.save.changes.link"), actionBinding);

	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
	 */
	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		// handles the navigation cases and passing along data from view to view
		EmailTemplateViewParams evp = (EmailTemplateViewParams) incoming;
		EmailTemplateViewParams outgoing = (EmailTemplateViewParams) evp.copyBase(); // inherit all the incoming data
		if ("success".equals(actionReturn) 
				|| "successAssign".equals(actionReturn) 
				|| "successReset".equals(actionReturn) ) {
			//outgoing.viewID = PreviewEmailProducer.VIEW_ID;
			result.resultingView = outgoing;
		} else if ("failure".equals(actionReturn)) {
			// failure just comes back here
			result.resultingView = outgoing;
		} else {
			// default
			result.resultingView = new SimpleViewParameters(MainViewProducer.VIEW_ID);
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new EmailTemplateViewParams();
	}

}
