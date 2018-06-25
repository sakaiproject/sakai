/**
 * $Id$
 * $URL$
 * 
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
package org.sakaiproject.accountvalidator.tool.producers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.accountvalidator.logic.ValidationLogic;
import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.accountvalidator.tool.params.ValidationViewParams;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.springutil.SpringMessageLocator;

@Slf4j
public class ValidationProducer extends BaseValidationProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

	public static final String VIEW_ID = "validate";

	private static final String MAX_PASSWORD_RESET_MINUTES = "accountValidator.maxPasswordResetMinutes";
	private static final int MAX_PASSWORD_RESET_MINUTES_DEFAULT = 60;

	public String getViewID() {
		return VIEW_ID;
	}

	private ValidationLogic validationLogic;
	public void setValidationLogic(ValidationLogic vl) {
		validationLogic = vl;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private AuthzGroupService authzGroupService;
	
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}
	
	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private SpringMessageLocator messageLocator;
	public void setSpringMessageLocator(SpringMessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	

	String portalurl = "http://localhost:8080/portal";
	public void init() {
		log.debug("portalUrl:" + developerHelperService.getPortalURL());
		portalurl = developerHelperService.getPortalURL();
		
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		ValidationAccount va = null;
		if (viewparams instanceof ValidationViewParams) {
			//get the bean
			ValidationViewParams vvp = (ValidationViewParams) viewparams;
			if (vvp.tokenId == null || "".equals(vvp.tokenId)) {
				tml.addMessage(new TargettedMessage("msg.noCode", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
				
				return;
			}
			log.debug("getting token: " + vvp.tokenId);
			va = validationLogic.getVaLidationAcountBytoken(vvp.tokenId);

			if (va == null) {
				Object[] args = new Object[]{ vvp.tokenId};
				tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
				return;
			} else if (ValidationAccount.STATUS_CONFIRMED.equals((va.getStatus()))) {
				Object[] args = new Object[]{ vvp.tokenId};
				tml.addMessage(new TargettedMessage("msg.alreadyValidated", args, TargettedMessage.SEVERITY_ERROR));
				return;
			} else if (validationLogic.isTokenExpired(va)) {
				Object[] args = new Object[]{ vvp.tokenId};
				tml.addMessage(new TargettedMessage("msg.expiredValidation", args, TargettedMessage.SEVERITY_ERROR));
				return;
			}
			else
			{
				/*
				* If we're dealing with password resets, they should go quickly. If it takes longer than
				* accountValidator.maxPasswordResetMinutes, it could be an intruder who stumbled upon the validation
				* token from an intercepted email, and we should stop them.
				* Note that there already exists a quartz job to expire the validation tokens, but using a quartz job
				* means that tokens would only be invalidated when the job runs. So here we check in real-time
				* */
				if (va.getStatus() != null)
				{
					if (va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
					{
						int minutes = serverConfigurationService.getInt(MAX_PASSWORD_RESET_MINUTES, MAX_PASSWORD_RESET_MINUTES_DEFAULT);

						//get the time limit and convert to millis
						long maxMillis = minutes * 60 * 1000;

						//the time when the validation token was sent to the email server
						long sentTime = va.getValidationSent().getTime();

						if (System.currentTimeMillis() - sentTime > maxMillis)
						{
							//it's been too long, so invalidate the token and stop the user
							va.setStatus(ValidationAccount.STATUS_EXPIRED);

							Object[] args = new Object[] {vvp.tokenId};
							tml.addMessage(new TargettedMessage("msg.expiredValidation", args, TargettedMessage.SEVERITY_ERROR));
							return;
						}
					}
				}
			}
		} else {
			//with no VP we need to exit
			tml.addMessage(new TargettedMessage("msg.noCode", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
			return;
		}

		if (!serverConfigurationService.getBoolean("accountValidator.sendLegacyLinks", false))
		{
			//This could be an old link stored in an email. 
			//If there's any way to automatically redirect to the desired page, please implement that instead.
			//TODO: Building the URL is somewhat duplicated in ValidationLogicImpl. It would be good to reduce this duplication
			Integer accountStatus = va.getAccountStatus();
			String statusMessage = "msg.acceptInvitation";
			String page = "newUser";
			if (accountStatus != null)
			{
				if (accountStatus.equals(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET))
				{
					page = "passwordReset";
					statusMessage = "msg.resetPassword";
				}
			}
			String serverUrl = serverConfigurationService.getServerUrl();
			String url = serverUrl + "/accountvalidator/faces/" + page + "?tokenId=" + va.getValidationToken();
			String[] args = new String[]{serverConfigurationService.getString("ui.service", "Sakai")};
			statusMessage = messageLocator.getMessage(statusMessage, args);
			UILink.make(tofill, "redirectLink", statusMessage, url);
			return;
		}

		try {
			User u = userDirectoryService.getUser(EntityReference.getIdFromRef(va.getUserId()));
			
			UIOutput.make(tofill, "email", u.getEmail());

			//user who added this person
			User addedBy = u.getCreatedBy();
			
			//we need some values to fill in
			Object[] args = new Object[]{
					getUIService(),
					addedBy.getDisplayName(),
					addedBy.getEmail()
					
			};
			
			//is this a password reset?
			boolean isReset = false;
			if (va.getAccountStatus() == ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET) {
				isReset = true;
			}
			
            UIMessage.make(tofill, "validate.or", "validate.or", args);
			if (!isReset) {
				UIMessage.make(tofill, "welcome1", "validate.welcome1", args);
				UIMessage.make(tofill, "welcome", "validate.welcome", args);
				UIMessage.make(tofill, "validate.imnew", "validate.imnew", args);
				//merge form
				UIMessage.make(tofill, "validate.alreadyhave",  "validate.alreadyhave", args);
				
			} else {
				UIMessage.make(tofill, "welcome1", "validate.welcome1.reset", args);
				UIMessage.make(tofill, "welcome", "validate.welcome.reset", args);
				UIMessage.make(tofill, "validate.imnew", "validate.oneaccount", args);
				
				//merge form
				UIMessage.make(tofill, "validate.alreadyhave",  "validate.alreadyhave.reset", args);
			}

			//we need to know what sites their a member of:
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
			Iterator<String> git = groups.iterator();
			//UIBranchContainer list = UIBranchContainer.make(tofill, "sites:");
			List existingSites = new ArrayList();
			while (git.hasNext()) {
				String groupRef = git.next();
				String groupId = EntityReference.getIdFromRef(groupRef);
				if(!existingSites.contains(groupId)){
					log.debug("groupId is " + groupId);
					try {
						Site s = siteService.getSite(groupId);
						UIBranchContainer list =  UIBranchContainer.make(tofill, "siteListItem:", groupId);
						UIOutput.make(list, "siteName", s.getTitle());
						existingSites.add(groupId);
					} catch (IdUnusedException e) {
						log.error(e.getMessage(), e);
					}
				}
				
			}
			
			//details form
			UIForm detailsForm = UIForm.make(tofill, "setDetailsForm");
			
			if (isReset) {
				UIMessage.make(detailsForm, "claim", "validate.reset", args);
				UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.new.reset"), "accountValidationLocator.validateAccount");
			} else {
				UIMessage.make(detailsForm, "claim", "validate.claim", args);
				UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.new.account"), "accountValidationLocator.validateAccount");
			}
			
			String otp =  "accountValidationLocator." + va.getValidationToken();
			
			UIOutput.make(detailsForm, "eid", u.getDisplayId());
			UIInput.make(detailsForm, "firstName", otp + ".firstName", u.getFirstName());
			UIInput.make(detailsForm, "surName", otp + ".surname", u.getLastName());
		
			UIBranchContainer row1 = UIBranchContainer.make(detailsForm, "passrow1:");
			UIInput.make(row1, "password1", otp + ".password");
			
			log.debug("account status: " + va.getAccountStatus());	
			
			if (ValidationAccount.ACCOUNT_STATUS_NEW == va.getAccountStatus() || ValidationAccount.ACCOUNT_STATUS_LEGACY_NOPASS == va.getAccountStatus()
					|| ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET == va.getAccountStatus()) {
				log.debug("this is a new account render the second password box");
				UIBranchContainer row2 = UIBranchContainer.make(detailsForm, "passrow2:");
				UIInput.make(row2, "password2", otp + ".password2");
			}
			// If we have some terms get the user to accept them.
			if (!"".equals(serverConfigurationService.getString("account-validator.terms"))) {
				String url = serverConfigurationService.getString("account-validator.terms");
				UIBranchContainer termsRow = UIBranchContainer.make(detailsForm, "termsrow:");

				UIBoundBoolean.make(termsRow, "terms", otp+ ".terms");
				// If someone wants to re-write this to be RSF like great, but this works.
				// Although it doesn't escape the bundle strings.
				String terms = messageLocator.getMessage("terms", new Object[]{
						"<a href='"+ url+ "' target='_new'>"+
						messageLocator.getMessage("terms.link")+
						"</a>"
				});
				UIVerbatim.make(termsRow, "termsLabel", terms);
			}
			
			
			detailsForm.parameters.add(new UIELBinding(otp + ".userId", va.getUserId()));

			//the claim form
			
			UIForm claimForm = UIForm.make(tofill, "claimAccountForm");
			
			if (!isReset) {
				UIMessage.make(claimForm, "validate.loginexisting",  "validate.loginexisting", args);
			} else {
				UIMessage.make(claimForm, "validate.loginexisting",  "validate.loginexisting.reset", args);
			}
			UIInput.make(claimForm, "userName", "claimLocator.new_1.userEid");
			UIInput.make(claimForm, "password", "claimLocator.new_1.password1");
			UICommand.make(claimForm, "submitClaim", UIMessage.make("submit.login"), "claimLocator.claimAccount");
			claimForm.parameters.add(new UIELBinding("claimLocator.new_1.validationToken", va.getValidationToken()));
		} catch (UserNotDefinedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public ViewParameters getViewParameters() {
		return new ValidationViewParams();
	}

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (result.resultingView instanceof ValidationViewParams) {
			log.debug("got some viewparams! with return " + actionReturn);
			if (actionReturn instanceof String) {
				String ret = (String) actionReturn;
				if ("success".equals(ret)) {
					result.resultingView = new RawViewParameters(portalurl);
				}
			}
		}
		
	}
}
