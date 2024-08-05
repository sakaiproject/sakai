/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.player.pages;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.scorm.ui.player.behaviors.ActivityAjaxEventBehavior;
import org.sakaiproject.scorm.ui.player.behaviors.CloseWindowBehavior;
import org.sakaiproject.scorm.ui.player.components.ButtonForm;
import org.sakaiproject.scorm.ui.player.components.LaunchPanel;
import org.sakaiproject.scorm.ui.player.components.LazyLaunchPanel;

public class ScormPlayerPage extends BaseToolPage
{
	private static final long serialVersionUID = 1L;

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService scormContentService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormSequencingService")
	ScormSequencingService scormSequencingService;

	// Components
	private LazyLaunchPanel lazyLaunchPanel;
	private ActivityAjaxEventBehavior closeWindowBehavior;
	@Getter private ButtonForm buttonForm;

	public ScormPlayerPage()
	{
		this(new PageParameters());
	}

	public ScormPlayerPage(final PageParameters pageParams)
	{
		super();

		long contentPackageId = pageParams.get("contentPackageId").toLong();
		int userNavRequest = pageParams.get("navRequest").toInt(-1);

		ContentPackage contentPackage = scormContentService.getContentPackage(contentPackageId);
		final SessionBean sessionBean = scormSequencingService.newSessionBean(contentPackage);
		sessionBean.setCompletionUrl(getCompletionUrl());

		WebMarkupContainer container = new WebMarkupContainer("scormButtonPanel");
		if (!contentPackage.isShowNavBar())
		{
			container.setVisible(false);
		}

		buttonForm = new ButtonForm("buttonForm", sessionBean, this);
		container.add(buttonForm);
		add(container);
		add(lazyLaunchPanel = new LazyLaunchPanel("actionPanel", sessionBean, userNavRequest, this));

		closeWindowBehavior = new CloseWindowBehavior(sessionBean, lms.canUseRelativeUrls());
		add(closeWindowBehavior);
	}

	private String getCompletionUrl()
	{
		HttpServletRequest servletRequest = (HttpServletRequest) getRequest().getContainerRequest();
		String toolUrl = servletRequest.getContextPath();
		Class<? extends Page> pageClass = PackageListPage.class;

		if (lms.canLaunchNewWindow())
		{
			pageClass = ScormCompletionPage.class;
		}

		String completionUrl = getRequestCycle().urlFor(pageClass, null).toString();
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/").append(completionUrl);

		return url.toString();
	}

	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target)
	{
		buttonForm.synchronizeState(sessionBean, target);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forScript("window.beforeunload = function() { " + closeWindowBehavior.getCall() + " }", null));
	}

	public LaunchPanel getLaunchPanel()
	{
		return lazyLaunchPanel.getLaunchPanel();
	}
}
