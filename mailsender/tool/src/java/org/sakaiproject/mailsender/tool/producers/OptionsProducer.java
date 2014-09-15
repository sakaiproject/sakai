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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.sakaiproject.mailsender.tool.beans.ConfigBean;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class OptionsProducer implements ViewComponentProducer, NavigationCaseReporter
{
	public static final String VIEW_ID = "options";

	private NavBarRenderer navBarRenderer;
	private ExternalLogic externalLogic;
	private ConfigLogic configLogic;
	private MessageLocator messageLocator;

	public OptionsProducer()
	{
	}

	public void setNavBarRenderer(NavBarRenderer navBarRenderer)
	{
		this.navBarRenderer = navBarRenderer;
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		UIForm optionsForm = UIForm.make(tofill, "optionsForm");

		// Send Me A Copy
		UIBoundBoolean.make(optionsForm, "sendMeACopy", "configBean.config.sendMeACopy");

		// Append recipient list
		UIBoundBoolean.make(optionsForm, "appendRecipientList", "configBean.config.appendRecipientList");

		// Add to email archive
		if (externalLogic.isEmailArchiveAddedToSite())
		{
			UIOutput.make(optionsForm, "addToArchiveDiv");
			UIBoundBoolean.make(optionsForm, "addToArchive", "configBean.config.addToArchive");
		}

		// Reply-to
		String[] options = { ReplyTo.sender.toString(), ReplyTo.no_reply_to.toString() };
		String[] labels = { "options_replylabel1", "options_replylabel2" };
		UISelect replyToSelect = UISelect.make(optionsForm, "replyToSelect", options, labels,
				"configBean.config.replyTo").setMessageKeys();
		String replyToFullId = replyToSelect.getFullID();
		UISelectChoice.make(optionsForm, "replyToSender", replyToFullId, 0);
		UISelectLabel.make(optionsForm, "replyToSenderLabel", replyToFullId, 0);
		UISelectChoice.make(optionsForm, "replyToNone", replyToFullId, 1);
		UISelectLabel.make(optionsForm, "replyToNoneLabel", replyToFullId, 1);

		// Display invalid emails
		options = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		labels = new String[] { "options_displayinvalidemails_yes",
				"options_displayinvalidemails_no" };
		UISelect invalidEmailsSelect = UISelect.make(optionsForm, "invalidEmailsSelect", options,
				labels, "configBean.config.displayInvalidEmails").setMessageKeys();
		String invalidEmailsFullId = invalidEmailsSelect.getFullID();
		UISelectChoice.make(optionsForm, "invalidEmailsYes", invalidEmailsFullId, 0);
		UISelectLabel.make(optionsForm, "invalidEmailsYesLabel", invalidEmailsFullId, 0);
		UISelectChoice.make(optionsForm, "invalidEmailsNo", invalidEmailsFullId, 1);
		UISelectLabel.make(optionsForm, "invalidEmailsNoLabel", invalidEmailsFullId, 1);

		// Display empty gropus
		options = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		labels = new String[] { "options_displayemptygroups_yes", "options_displayemptygroups_no" };
		UISelect emptyGroupsSelect = UISelect.make(optionsForm, "emptyGroupsSelect", options,
				labels, "configBean.config.displayEmptyGroups").setMessageKeys();
		String emptyGroupsFullId = emptyGroupsSelect.getFullID();
		UISelectChoice.make(optionsForm, "emptyGroupsYes", emptyGroupsFullId, 0);
		UISelectLabel.make(optionsForm, "emptyGroupsYesLabel", emptyGroupsFullId, 0);
		UISelectChoice.make(optionsForm, "emptyGroupsNo", emptyGroupsFullId, 1);
		UISelectLabel.make(optionsForm, "emptyGroupsNoLabel", emptyGroupsFullId, 1);

		// subject prefix
		if (configLogic.allowSubjectPrefixChange())
		{
			// make the div that contains the details for prefix changing
			UIOutput.make(optionsForm, "subjectPrefixDiv");

			// Subject Prefix select
			options = new String[] { SubjectPrefixType.system.toString(),
					SubjectPrefixType.custom.toString() };
			labels = new String[] {
					messageLocator.getMessage("options_prefixsystemdefault", configLogic
							.getDefaultSubjectPrefix()),
					messageLocator.getMessage("options_prefixcustom") };
			UISelect subjectPrefixSelect = UISelect.make(optionsForm, "subjectPrefixSelect",
					options, labels, "configBean.config.subjectPrefixType");
			String subjectPrefixFullId = subjectPrefixSelect.getFullID();
			UISelectChoice.make(optionsForm, "subjectPrefixDefault", subjectPrefixFullId, 0);
			UISelectLabel.make(optionsForm, "subjectPrefixDefaultLabel", subjectPrefixFullId, 0);
			UISelectChoice.make(optionsForm, "subjectPrefixCustom", subjectPrefixFullId, 1);
			UISelectLabel.make(optionsForm, "subjectPrefixCustomLabel", subjectPrefixFullId, 1);

			UIOutput customPrefix = UIOutput.make(optionsForm, "customPrefixDiv");
			UIInput.make(optionsForm, "subjectPrefix", "configBean.config.subjectPrefix");

			// hide the input field if the selection is the default
			if (SubjectPrefixType.system.name().equals(
					configLogic.getConfig().getSubjectPrefixType()))
			{
				customPrefix.decorate(new UIStyleDecorator("followUp"));
			}
		}

		// command buttons
		UICommand.make(optionsForm, "update-button", UIMessage.make("options_update_button"),
				"configBean.saveConfig");
		UICommand.make(optionsForm, "cancel-button", UIMessage.make("options_cancel_button"));
	}

	/**
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List<NavigationCase> reportNavigationCases()
	{
		// All cases [except for errors] return to the compose screen
		List<NavigationCase> cases = new ArrayList<NavigationCase>();
		cases.add(new NavigationCase(new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		cases.add(new NavigationCase(ConfigBean.CONFIG_SAVE_FAILED, new SimpleViewParameters(
				OptionsProducer.VIEW_ID)));
		return cases;
	}
}