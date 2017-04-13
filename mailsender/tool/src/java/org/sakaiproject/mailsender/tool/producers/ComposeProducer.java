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

import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.tool.beans.EmailBean;
import org.sakaiproject.mailsender.tool.params.UserGroupViewParameters;
import org.sakaiproject.mailsender.tool.producers.fragments.UserGroupingProducer;
import org.sakaiproject.user.api.User;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIColumnsDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIRowsDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

public class ComposeProducer implements ViewComponentProducer, NavigationCaseReporter, DefaultView
{
	public static final String VIEW_ID = "compose";

	// Spring injected beans
	private ExternalLogic externalLogic;
	private NavBarRenderer navBarRenderer;
	private TextInputEvolver richTextEvolver;
	private ViewStateHandler viewStateHandler;
	private FrameAdjustingProducer frameAdjustingProducer;

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

	public void setNavBarRenderer(NavBarRenderer navBarRenderer)
	{
		this.navBarRenderer = navBarRenderer;
	}

	public void setRichTextEvolver(TextInputEvolver richTextEvolver)
	{
		this.richTextEvolver = richTextEvolver;
	}

	public void setViewStateHandler(ViewStateHandler viewStateHandler)
	{
		this.viewStateHandler = viewStateHandler;
	}

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void setFrameAdjustingProducer(FrameAdjustingProducer frameAdjustingProducer)
	{
		this.frameAdjustingProducer = frameAdjustingProducer;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		String emailBean = "emailBean.newEmail";
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		// build out the form elements and labels
		UIForm mainForm = UIForm.make(tofill, "mainForm");

		// get the user then name & email
		User curUser = externalLogic.getCurrentUser();

		String fromEmail = "";
		String fromDisplay = "";
		if (curUser != null)
		{
			fromEmail = curUser.getEmail();
			fromDisplay = curUser.getDisplayName();
		}
		String from = fromDisplay + " <" + fromEmail + ">";
		UIOutput.make(tofill, "from", from);

		// create the 'select all' checkbox
		UIBoundBoolean input = UIBoundBoolean.make(mainForm,
				"mailsender-rcpt-all", "emailBean.newEmail.allIds");
		input.decorate(new UIFreeAttributeDecorator("onclick",
				"RcptSelect.toggleSelectAll()"));

		// create the select by role link
		UIMessage msg = UIMessage.make("select_rcpts_by_role");
		UserGroupViewParameters viewParams = new UserGroupViewParameters(
				UserGroupingProducer.VIEW_ID);
		viewParams.type = "role";
		String url = viewStateHandler.getFullURL(viewParams);
		UILink link = UILink.make(tofill, "mailsender-rcpt-link-roles", msg, url);
		UIFreeAttributeDecorator rolesDecorator = new UIFreeAttributeDecorator("onclick",
				"RcptSelect.showResults(this, 'mailsender-roles', true); return false;");
		link.decorate(rolesDecorator);

		// create the select by section link
		msg = UIMessage.make("select_rcpts_by_section");
		viewParams = new UserGroupViewParameters(UserGroupingProducer.VIEW_ID);
		viewParams.type = "section";
		url = viewStateHandler.getFullURL(viewParams);
		link = UILink.make(tofill, "mailsender-rcpt-link-sections", msg, url);
		UIFreeAttributeDecorator sectionsDecorator = new UIFreeAttributeDecorator("onclick",
				"RcptSelect.showResults(this, 'mailsender-sections', true); return false;");
		link.decorate(sectionsDecorator);

		// create the select by group link
		msg = UIMessage.make("select_rcpts_by_group");
		viewParams = new UserGroupViewParameters(UserGroupingProducer.VIEW_ID);
		viewParams.type = "group";
		url = viewStateHandler.getFullURL(viewParams);
		link = UILink.make(tofill, "mailsender-rcpt-link-groups", msg, url);
		UIFreeAttributeDecorator groupsDecorator = new UIFreeAttributeDecorator("onclick",
				"RcptSelect.showResults(this, 'mailsender-groups', true); return false;");
		link.decorate(groupsDecorator);

		// create the 'other recipients' field
		UIInput.make(mainForm, "otherRecipients", emailBean + ".otherRecipients");

		// create the subject field
		UIInput.make(mainForm, "subject", emailBean + ".subject");

		// create the content editor
		UIInput content = UIInput.make(mainForm, "content-div:", emailBean + ".content");
		richTextEvolver.evolveTextInput(content);

		// We have to make the textarea 30 rows so that when we resize the iframe we get a size that will be roughly
		// the same as when the WYSIWYG editor has loaded. This is all because we don't resize the iframe after
		// the WYSIWYG has loaded, SAK-23692 may be better in future.
		// This can't be done before the rich text evolver has run.
		content.decorate(new UIRowsDecorator(30));

		// create 'send me a copy' checkbox
		UIBoundBoolean.make(mainForm, "sendMeCopy", emailBean + ".config.sendMeACopy");
		UIBoundBoolean.make(mainForm, "appendRecipientList", emailBean + ".config.appendRecipientList");

		if (externalLogic.isEmailArchiveAddedToSite())
		{
			UIOutput.make(mainForm, "addToArchiveDiv");
			UIBoundBoolean.make(mainForm, "addToArchive", emailBean + ".config.addToArchive");
		}

		Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
		// not sure why we can't just use the token as the value in UIInput, but if we do that,
		// nothing is submitted
		if (sessionToken != null) {
		    UIInput.make(mainForm, "csrf", emailBean + ".csrf");
		    UIOutput.make(mainForm, "csrfvalue", sessionToken.toString());
		}

		// create buttons for form
		UICommand.make(mainForm, "send-button", UIMessage.make("send_mail_button"),
				"emailBean.sendEmail");
		UICommand.make(mainForm, "cancel-button", UIMessage.make("cancel_mail_button"));

		frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
	}

	/**
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List<NavigationCase> reportNavigationCases()
	{
		List<NavigationCase> cases = new ArrayList<NavigationCase>();
		cases.add(new NavigationCase(new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		cases.add(new NavigationCase(EmailBean.EMAIL_SENT, new SimpleViewParameters(
				ResultsProducer.VIEW_ID)));
		return cases;
	}
}