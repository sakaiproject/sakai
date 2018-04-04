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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.site.api.Site;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Produces transferMemberships.html - builds a form that allows the user to transfer their memberships to another account provided that they can authenticate with that account
 * @author bbailla2
 */
@Slf4j
public class TransferMembershipsProducer extends BaseValidationProducer implements ViewComponentProducer, ActionResultInterceptor {

	public static final String VIEW_ID = "transferMemberships";

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void init()
	{

	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) 
	{
		Object [] args = new Object[]{serverConfigurationService.getString("ui.service", "Sakai")};
		UIMessage.make(tofill, "welcome1", "validate.welcome1", args);

		ValidationAccount va = getValidationAccount(viewparams, tml);
		if (va == null)
		{
			//handled by getValidationAccount
			return;
		}
		else if (!va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_NEW) && !va.getAccountStatus().equals(ValidationAccount.ACCOUNT_STATUS_EXISITING))
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

		//produce a link to switch to the new user view
		String activationURL = getViewURL("newUser", va);
		String linkText = messageLocator.getMessage("validate.wait.transfer.2", new Object[]{u.getDisplayId()});

		//we need to know which sites they're a member of:
		Set<String> groups = authzGroupService.getAuthzGroupsIsAllowed(EntityReference.getIdFromRef(va.getUserId()), "site.visit", null);
		Iterator<String> git= groups.iterator();
		List<String> existingSites = new ArrayList<>();
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

		String welcomeMessage = existingSites.size() == 1 ? "validate.welcome.single" : "validate.welcome.plural";
		UIMessage.make(tofill, "welcome", welcomeMessage, args);
		UIMessage.make(tofill, "welcome2.1", "validate.wait.transfer.1", args);
		UILink.make(tofill, "welcome2.2", linkText, activationURL);
		UIMessage.make(tofill, "transferInstructions", "validate.loginexisting.transfer", args);
		UIMessage.make(tofill, "validate.alreadyhave", "validate.alreadyhave", args);

		addResetPassLink(tofill, va);

		Object[] displayIdArgs = new Object[]{u.getDisplayId()};
		UIForm claimForm = UIForm.make(tofill, "claimAccountForm");
		UIMessage.make(claimForm, "validate.loginexisting", "validate.loginexisting.accountReserved", displayIdArgs);
		UIInput.make(claimForm, "userName", "claimLocator.new_1.userEid");
		UIInput.make(claimForm, "password", "claimLocator.new_1.password1");
		UICommand.make(claimForm, "transferMemberships", UIMessage.make("validate.loginexisting.transferMemberships"), "claimLocator.claimAccount");
		claimForm.parameters.add(new UIELBinding("claimLocator.new_1.validationToken", va.getValidationToken()));
	}
}
