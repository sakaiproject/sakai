/**
 * $Id: MainProducer.java 105078 2012-02-24 23:00:38Z ottenhoff@longsight.com $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-tool/src/java/org/sakaiproject/accountvalidator/tool/producers/MainProducer.java $
 * DeveloperHelperService.java - entity-broker - Apr 13, 2008 5:42:38 PM - azeckoski
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

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Produces newUser.html - builds a form that allows the user to claim an account that has been created for them
 * @author bbailla2
 */
@Slf4j
public class NewUserProducer extends BaseValidationProducer implements ViewComponentProducer, ActionResultInterceptor {

	public static final String VIEW_ID = "newUser";

	public String getViewID() {
		return VIEW_ID;
	}

	public void init()
	{

	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
	{

		Object[] args = new Object[]{serverConfigurationService.getString("ui.service", "Sakai")};
		UIMessage.make(tofill, "welcome1", "validate.welcome1", args);

		ValidationAccount va = getValidationAccount(viewparams, tml);
		if (va == null)
		{
			//handled by getValidationAccount
			return;
		}
		else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_NEW) && !va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_EXISITING )&&!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE))
		{
			//this form is not appropriate
			args = new Object[] {va.getValidationToken()};
			//no such validaiton of the required account status
			tml.addMessage(new TargettedMessage("msg.noSuchValidation", args, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		else if (ValidationAccount.STATUS_CONFIRMED.equals(va.getStatus()))
		{
			args = new Object[] {va.getValidationToken()};
			tml.addMessage(new TargettedMessage("msg.alreadyValidated", args, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		else if (sendLegacyLinksEnabled())
		{
			redirectToLegacyLink(tofill, va);
			return;
		}

		User u = null;
		try
		{
			u = userDirectoryService.getUser(EntityReference.getIdFromRef(va.getUserId()));
		}
		catch (UserNotDefinedException e)
		{

		}

		if (u == null)
		{
			log.error("user ID does not exist for ValidationAccount with tokenId: " + va.getValidationToken());
			tml.addMessage(new TargettedMessage("validate.userNotDefined", new Object[]{getUIService()}, TargettedMessage.SEVERITY_ERROR));
			return;
		}

		//user who added this person
		User addedBy = u.getCreatedBy();

		//we need some values to fill in
		args = new Object[]{
			serverConfigurationService.getString("ui.service", "Sakai"),
			addedBy.getDisplayName(),
			addedBy.getEmail(),
			u.getDisplayId()
		};
		//details form
		UIForm detailsForm = UIForm.make(tofill, "setDetailsForm");
		UIMessage.make(tofill, "username.new", "username.new");
		//Do not display sites and other welcome page information if user is updating userid
		if(va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE)) {
			UIMessage.make(tofill, "account-title", "submit.update");
			UIOutput.make(tofill, "eid", va.getEid());
			UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.update"), "accountValidationLocator.validateAccount");
		}
		else {
			UIMessage.make(tofill, "account-title", "activateAccount.title");
			UIOutput.make(tofill, "eid", u.getDisplayId());
			UIMessage.make(tofill, "wait.1", "validate.wait.newUser.1", args);
			String linkText = messageLocator.getMessage("validate.wait.newUser.2", args);
			String transferMembershipsURL = getViewURL("transferMemberships", va);
			UILink.make(tofill, "wait.2", linkText, transferMembershipsURL);
			UIMessage.make(tofill, "validate.alreadyhave", "validate.alreadyhave", args);
			UICommand.make(detailsForm, "addDetailsSub", UIMessage.make("submit.new.account"), "accountValidationLocator.validateAccount");
			//we need to know which sites they're a member of:
			Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
			Iterator<String> git = groups.iterator();
			List existingSites = new ArrayList();
			while (git.hasNext())
			{
				String groupRef = git.next();
				String groupId = EntityReference.getIdFromRef(groupRef);
				if (!existingSites.contains(groupId))
				{
					log.debug("groupId is " + groupId);
					try
					{
						Site s = siteService.getSite(groupId);
						UIBranchContainer list = UIBranchContainer.make(tofill, "siteListItem:", groupId);
						UIOutput.make(list, "siteName", s.getTitle());
						existingSites.add(groupId);
					}
					catch (IdUnusedException e)
					{
						log.error(e.getMessage(), e);
					}
				}
			}

			if (existingSites.size() >= 2)
			{
				UIMessage.make(tofill, "welcome", "validate.welcome.plural", args);
			}
			else
			{
				UIMessage.make(tofill, "welcome", "validate.welcome.single", args);
			}
		}
		UIMessage.make(tofill, "welcome2", "validate.welcome2", args);

		String otp = "accountValidationLocator." + va.getValidationToken();
		UIBranchContainer firstNameContainer = UIBranchContainer.make(detailsForm, "firstNameContainer:");
		UIMessage.make(firstNameContainer, "lblFirstName", "firstname");
		UIInput.make(detailsForm, "firstName", otp + ".firstName", u.getFirstName());
		UIBranchContainer lastNameContainer = UIBranchContainer.make(detailsForm, "lastNameContainer:");
		UIMessage.make(lastNameContainer, "lblLastName", "lastname");
		UIInput.make(detailsForm, "surName", otp + ".surname", u.getLastName());
		
		// In terms of using UIVerbatim, we would gladly accept an alternate method of doing this, however
		// this seems to be the only way to pass the enabled, disabled value from the server into the JavaScript
		boolean passwordPolicyEnabled = (userDirectoryService.getPasswordPolicy() != null);
		String passPolicyEnabledJavaScript = "VALIDATOR.isPasswordPolicyEnabled = " + Boolean.toString(passwordPolicyEnabled) + ";";
		UIVerbatim.make(tofill, "passwordPolicyEnabled", passPolicyEnabledJavaScript);

		UIBranchContainer row1 = UIBranchContainer.make(detailsForm, "passrow1:");
		UIInput.make(row1, "password1", otp + ".password");

		log.debug("account status: " + va.getAccountStatus());

		if (ValidationAccount.ACCOUNT_STATUS_NEW == va.getAccountStatus())
		{
			log.debug("this is a new account render the second password box");
			UIBranchContainer row2 = UIBranchContainer.make(detailsForm, "passrow2:");
			UIInput.make(row2, "password2", otp + ".password2");
		}
		// If we have some terms, get the user to accept them.
		if (!"".equals(serverConfigurationService.getString("account-validator.terms")))
		{
			String termsURL = serverConfigurationService.getString("account-validator.terms");
			UIBranchContainer termsRow = UIBranchContainer.make(detailsForm, "termsrow:");

			UIBoundBoolean.make(termsRow, "terms", otp + ".terms");
			// If someone wants to re-write this to be RSF like great, but this works.
			// Although it doesn't escape the bundle strings.
			String terms = messageLocator.getMessage("terms", new Object[]
			{
				"<a href='" +  termsURL + "' target='_new'>" + messageLocator.getMessage("terms.link")+"</a>"
			});
			UIVerbatim.make(termsRow, "termsLabel", terms);
		}
	}
}
