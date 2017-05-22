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
package org.sakaiproject.mailsender.tool.producers;

import org.sakaiproject.mailsender.logic.ExternalLogic;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class NavBarRenderer
{
	private MessageLocator messageLocator;
	private ExternalLogic extLogic;

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}

	public void setExternalLogic(ExternalLogic extLogic)
	{
		this.extLogic = extLogic;
	}

	public void makeNavBar(UIContainer tofill, String divID, String currentViewID)
	{
		UIJointContainer joint = new UIJointContainer(tofill, divID, "navigation:");

		// if on the compose page, make the compose text static and the options
		// text a link
		UIBranchContainer cell = UIBranchContainer.make(joint, "navigation-cell:", "1");
		UIComponent comp = null;
		if (currentViewID.equals(ComposeProducer.VIEW_ID))
		{
			comp = UIMessage.make(cell, "item-text", "compose_toolbar");
		}
		else
		{
			comp = UIInternalLink.make(cell, "item-link", UIMessage.make("compose_toolbar"),
					new SimpleViewParameters(ComposeProducer.VIEW_ID));
		}
		comp.decorate(new UIIDStrategyDecorator("navCompose"));

		if (extLogic.isUserAllowedInLocation(extLogic.getCurrentUserId(), ExternalLogic.PERM_ADMIN,
				extLogic.getCurrentLocationId()))
		{

			// options link
			cell = UIBranchContainer.make(joint, "navigation-cell:", "2");
			if (currentViewID.equals(OptionsProducer.VIEW_ID))
			{
				comp = UIMessage.make(cell, "item-text", "options_toolbar");
			}
			else
			{
				comp = UIInternalLink.make(cell, "item-link", UIMessage.make("options_toolbar"),
						new SimpleViewParameters(OptionsProducer.VIEW_ID));
				String msg = messageLocator.getMessage("navigate.lose.data");
				UIFreeAttributeDecorator decorator = new UIFreeAttributeDecorator("onclick",
						"return Dirty.check('" + msg + "')");
				comp.decorate(decorator);
			}
			comp.decorate(new UIIDStrategyDecorator("navConfig"));

			// permissions link

			cell = UIBranchContainer.make(joint, "navigation-cell:", "3");
			if (currentViewID.equals(PermissionsProducer.VIEW_ID))
			{
				comp = UIMessage.make(cell, "item-text", "mailsender.navbar.permissions");
			}
			else
			{
				comp = UIInternalLink.make(cell, "item-link", UIMessage
						.make("mailsender.navbar.permissions"), new SimpleViewParameters(
						PermissionsProducer.VIEW_ID));
				String msg = messageLocator.getMessage("navigate.lose.data");
				UIFreeAttributeDecorator decorator = new UIFreeAttributeDecorator("onclick",
						"return Dirty.check('" + msg + "')");
				comp.decorate(decorator);
			}
		}
	}
}
