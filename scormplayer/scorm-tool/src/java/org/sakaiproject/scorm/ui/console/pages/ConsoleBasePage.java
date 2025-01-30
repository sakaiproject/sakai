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
package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.ui.console.components.BreadcrumbPanel;
import org.sakaiproject.wicket.markup.html.feedback.SakaiFeedbackPanel;
import org.sakaiproject.scorm.ui.upload.UploadPage;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;
import org.sakaiproject.wicket.markup.html.navigation.NavLink;

public class ConsoleBasePage extends SakaiPortletWebPage implements IHeaderContributor {

	private static PackageResourceReference CONSOLE_CSS = new PackageResourceReference(ConsoleBasePage.class, "res/scorm_console.css");

	@SpringBean
	private LearningManagementSystem lms;

	@SpringBean
	public ToolManager toolManager;

	// This JavaScript snippet is used to scroll to the top of a given page after form submission onError(), so that feedback messages are immediately visible to the user
	protected static final String JS_SCROLL_TO_TOP = "window.scrollTo(0,0);";

	// The feedback panel component displays dynamic messages to the user
	protected SakaiFeedbackPanel feedback;
	private BreadcrumbPanel breadcrumbs;

	public NavLink listLink;
	public NavLink uploadLink;

	private boolean isSinglePackageTool;

	public ConsoleBasePage()
	{
		this(null);
	}

	public ConsoleBasePage(PageParameters params)
	{
		final String context = lms.currentContext();
		final boolean canUpload = lms.canUpload(context);
		final boolean canValidate = lms.canValidate(context);

		isSinglePackageTool = isSinglePackageTool();

		WebMarkupContainer navBar = new WebMarkupContainer("navBar")
		{
			@Override
			public boolean isVisible()
			{
				return !isSinglePackageTool;
			}
		};

		ResourceModel listLinkText = new ResourceModel("link.list");
		ResourceModel uploadLinkText = new ResourceModel("link.upload");
		navBar.add(listLink = new NavLink("listLink", PackageListPage.class, (canUpload || canValidate), listLinkText, listLinkText));
		navBar.add(uploadLink = new NavLink("uploadLink", UploadPage.class, canUpload, uploadLinkText, uploadLinkText));

		// add the toolbar container
		add(navBar);

		// page title
		add(newPageTitleLabel(params));

		// feedback panel
		feedback = new SakaiFeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		// breadcrumbs
		add(breadcrumbs = new BreadcrumbPanel("breadcrumbs"));
	}

	public void addBreadcrumb(IModel model, Class<?> pageClass, PageParameters params, boolean isEnabled)
	{
		breadcrumbs.addBreadcrumb(model, pageClass, params, isEnabled);
	}

	protected Label newPageTitleLabel(PageParameters params)
	{
		return new Label("page.title", new ResourceModel("page.title"));
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		breadcrumbs.setVisible(breadcrumbs.getNumberOfCrumbs() > 0);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CONSOLE_CSS));
	}

	protected boolean isSinglePackageTool()
	{
		return toolManager != null && toolManager.getCurrentTool() != null
				&& "sakai.scorm.singlepackage.tool".equals(toolManager.getCurrentTool().getId());
	}
}
