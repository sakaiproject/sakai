/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.tool.producers.fragments;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.content.ContentTypeReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.mailsender.tool.params.UserGroupViewParameters;
import org.sakaiproject.mailsender.tool.params.UsersViewParameters;

@Slf4j
public class UserGroupingProducer implements ViewComponentProducer, ViewParamsReporter,
		ContentTypeReporter
{
	public static final String VIEW_ID = "userGroup";

	private ComposeLogic composeLogic;
	private TargettedMessageList messages;
	private ViewStateHandler viewStateHandler;
	private ConfigLogic configLogic;

	public UserGroupingProducer()
	{
	}

	public UserGroupingProducer(ComposeLogic composeLogic, ViewStateHandler viewStateHandler,
			TargettedMessageList messages)
	{
		this.composeLogic = composeLogic;
		this.viewStateHandler = viewStateHandler;
		this.messages = messages;
	}

	public void setComposeLogic(ComposeLogic composeLogic)
	{
		this.composeLogic = composeLogic;
	}

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	public void setViewStateHandler(ViewStateHandler viewStateHandler)
	{
		this.viewStateHandler = viewStateHandler;
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ComponentProducer
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		try
		{
			UserGroupViewParameters ugParams = (UserGroupViewParameters) viewparams;
			List<EmailRole> emailRoles = null;

			if ("group".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailGroups();
			}
			else if ("section".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailSections();
			}
			else if ("role".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailRoles();
			}
			else
			{
				messages.addMessage(new TargettedMessage("error.unknown.role.type"));
				return;
			}

			if (emailRoles.size() == 0)
			{
				messages.addMessage(new TargettedMessage("no." + ugParams.type + ".found", null,
						TargettedMessage.SEVERITY_INFO));
			}
			else
			{
				int totalEntriesShown = 0;
				ConfigEntry config = configLogic.getConfig();

				for (int i = 0; i < emailRoles.size(); i++)
				{
					EmailRole role = emailRoles.get(i);

					// lookup the number of entries in the role. if none found, disable the checkbox
					// for the role
					int numEntriesInRole = 0;
					switch (role.getType())
					{
						case GROUP:
						case SECTION:
							numEntriesInRole = composeLogic.countUsersByGroup(role.getRoleId());
							break;

						case ROLE:
							numEntriesInRole = composeLogic.countUsersByRole(role.getRoleId());
							break;
					}
					totalEntriesShown += numEntriesInRole;

					// get the data
					String[] rolePlural = new String[] { role.getRolePlural() };

					if (numEntriesInRole > 0 || config.isDisplayEmptyGroups())
					{
						// create a branch for looping
						// The localId needs to be unique as multiple copies of this element will get loaded
						// into the same page if a user switches between roles/groups/sections.
						UIBranchContainer roleBranch = UIBranchContainer.make(tofill,
								"mailsender-usersGroupOption:",
								role.getType().toString().toLowerCase()+ "-"+ Integer.toString(i));

						// build the EL binding
						UIBoundBoolean input = UIBoundBoolean.make(roleBranch,
								"mailsender-usersGroup",
								"emailBean.newEmail." + ugParams.type + "Ids." + role.getRoleId());
						input.decorate(new UIIDStrategyDecorator(input.getFullID() + "-"
								+ ugParams.type));

						if (numEntriesInRole > 0)
						{
							// add an 'onclick' decorator if there are entries
							input.decorate(new UIFreeAttributeDecorator("onclick",
									"RcptSelect.toggleSelectAll(this.id)"));
						}
						else if (config.isDisplayEmptyGroups())
						{
							// disable if there are no entries
							input.decorate(new UIDisabledDecorator());
						}

						// create the toggle area
						createToggleArea(ugParams, role, roleBranch, rolePlural);
					}
				}

				if (!config.isDisplayEmptyGroups() && totalEntriesShown <= 0)
				{
					messages.addMessage(new TargettedMessage("no." + ugParams.type + ".found",
							null, TargettedMessage.SEVERITY_INFO));
				}
			}
		}
		catch (GroupNotDefinedException gnde)
		{
			log.error(gnde.getMessage(), gnde);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { gnde
					.getMessage() }));
		}
		catch (IdUnusedException iue)
		{
			log.error(iue.getMessage(), iue);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { iue
					.getMessage() }));
		}
	}

	private void createToggleArea(UserGroupViewParameters ugParams, EmailRole role,
			UIBranchContainer roleBranch, String[] rolePlural)
	{
		UIMessage msg = UIMessage.make("usersbyrole_all_prefix", rolePlural);

		// create view params for user list links
		UsersViewParameters usersParams = new UsersViewParameters(UsersProducer.VIEW_ID);
		usersParams.type = ugParams.type;
		usersParams.id = role.getRoleId();
		String url = viewStateHandler.getFullURL(usersParams);

		// create the area for user listings
		UIOutput usersArea = UIOutput.make(roleBranch, "mailsender-users");

		// create the select & collapse links
		UILink selectLink = UIInternalLink.make(roleBranch, "mailsender-usersGroupLink-select",
				msg, url);
		UILink collapseLink = UIInternalLink.make(roleBranch, "mailsender-usersGroupLink-collapse",
				msg, "#");

		// create IDs that are unique on the page by including the type of area being accessed.
		String usersAreaId = usersArea.getFullID() + "-" + ugParams.type;
		String selectLinkId = selectLink.getFullID() + "-" + ugParams.type;
		String collapseLinkId = collapseLink.getFullID() + "-" + ugParams.type;

		usersArea.decorate(new UIIDStrategyDecorator(usersAreaId));
		selectLink.decorate(new UIIDStrategyDecorator(selectLinkId));
		collapseLink.decorate(new UIIDStrategyDecorator(collapseLinkId));

		String commonParams = "'" + usersAreaId + "', '" + selectLinkId + "', '" + collapseLinkId
				+ "');return false";

		// 'onclick' and 'id' decorators for the select link
		String command = "RcptSelect.showIndividuals(this, " + commonParams;
		selectLink.decorate(new UIFreeAttributeDecorator("onclick", command));

		// 'onclick' and 'id' decorators for the collapse link
		command = "RcptSelect.hideIndividuals(" + commonParams;
		collapseLink.decorate(new UIFreeAttributeDecorator("onclick", command));
	}

	/**
	 * @see uk.org.ponder.rsf.content.ContentTypeReporter
	 */
	public String getContentType()
	{
		// need to define the content type to keep it from being sent as the default
		return ContentTypeInfoRegistry.HTML_FRAGMENT;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer
	 */
	public String getViewID()
	{
		return VIEW_ID;
	}

	/**
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter
	 */
	public ViewParameters getViewParameters()
	{
		return new UserGroupViewParameters();
	}
}
