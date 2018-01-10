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

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.tool.params.UsersViewParameters;
import org.sakaiproject.user.api.User;

@Slf4j
public class UsersProducer implements ViewComponentProducer, ViewParamsReporter
{
	public static final String VIEW_ID = "users";

	private ComposeLogic composeLogic;
	private TargettedMessageList messages;

	public UsersProducer()
	{
	}

	public void setComposeLogic(ComposeLogic composeLogic)
	{
		this.composeLogic = composeLogic;
	}

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public String getViewID()
	{
		return VIEW_ID;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(UIContainer, ViewParameters,
	 *      ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		try
		{
			// cast the view params
			UsersViewParameters viewParams = (UsersViewParameters) viewparams;

			// get the members based on type and id
			List<User> users = null;
			if (viewParams.id != null && viewParams.id.trim().length() != 0)
			{
				if ("section".equals(viewParams.type) || "group".equals(viewParams.type))
				{
					users = composeLogic.getUsersByGroup(viewParams.id);
				}
				else
				{
					users = composeLogic.getUsersByRole(viewParams.id);
				}
			}

			// get the members that match the requested role
			if (users == null || users.size() == 0)
			{
				messages.addMessage(new TargettedMessage(
						"no." + viewParams.type + ".members.found", null,
						TargettedMessage.SEVERITY_INFO));
			}
			else
			{
				int i = 0;
				for (User user : users)
				{
					// populate the page with the members found
					UIBranchContainer cell = UIBranchContainer.make(tofill, "mailsender-userCol:",
							viewParams.id + "-" + Integer.toString(i));
					String displayName = user.getLastName() + ", " + user.getFirstName() + " ("
							+ user.getDisplayId() + ")";
					String path = PathUtil.buildPath(new String []{
							"emailBean","newEmail","userIds",user.getId()});
					UIBoundBoolean input = UIBoundBoolean.make(cell, "mailsender-user", path);
					UIVerbatim label = UIVerbatim.make(cell, "mailsender-userLabel",
							displayName);
					
					if (user.getEmail() != null && user.getEmail().trim().length() != 0)
					{
						// add onclick
						input.decorate(new UIFreeAttributeDecorator("onclick",
								"RcptSelect.toggleIndividual(this.id)"));
					}
					else
					{
						// disable the checkbox
						input.decorate(new UIDisabledDecorator());

						// add a class to the label to show disabled
						label.decorate(new UIStyleDecorator("invalid-user"));
					}
					i++;
				}
			}
		}
		catch (IdUnusedException e)
		{
			log.error(e.getMessage(), e);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { e
					.getMessage() }, TargettedMessage.SEVERITY_ERROR));
		}
	}

	/**
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters()
	{
		return new UsersViewParameters();
	}
}
