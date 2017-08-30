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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.ui.Icon;
import org.sakaiproject.scorm.ui.console.components.BreadcrumbPanel;
import org.sakaiproject.scorm.ui.console.components.SakaiFeedbackPanel;
import org.sakaiproject.scorm.ui.upload.pages.UploadPage;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;
import org.sakaiproject.wicket.markup.html.link.NavIntraLink;


public class ConsoleBasePage extends SakaiPortletWebPage implements IHeaderContributor {

	private static ResourceReference CONSOLE_CSS = new CompressedResourceReference(ConsoleBasePage.class, "res/scorm_console.css");
	private static ResourceReference LIST_ICON = new ResourceReference(ConsoleBasePage.class, "res/table.png");
	private static ResourceReference UPLOAD_ICON = new ResourceReference(ConsoleBasePage.class, "res/table_add.png");
	private static ResourceReference VALIDATE_ICON = new ResourceReference(ConsoleBasePage.class, "res/table_link.png");

    private static final String SAK_PROP_ENABLE_MENU_BUTTON_ICONS = "scorm.menuButton.icons";
    @SpringBean( name = "org.sakaiproject.component.api.ServerConfigurationService" )
    ServerConfigurationService serverConfigurationService;
	
	// The feedback panel component displays dynamic messages to the user
	protected FeedbackPanel feedback;
	private BreadcrumbPanel breadcrumbs;

	public NavIntraLink listLink;
	public NavIntraLink uploadLink;
	//public NavIntraLink validateLink;

	@SpringBean
	private LearningManagementSystem lms;
	@SpringBean
	public ToolManager toolManager;

	
	public ConsoleBasePage() {
		this(null);
	}
	
	public ConsoleBasePage(PageParameters params) {
		
		final String context = lms.currentContext();
		final boolean canUpload = lms.canUpload(context);
		final boolean canValidate = lms.canValidate(context);
		
		WebMarkupContainer wmc = new MaydayWebMarkupContainer("toolbar-administration");
		if (isSinglePackageTool()) {
	        wmc.setVisible(false);
		}

		listLink = new NavIntraLink("listLink", new ResourceModel("link.list"), PackageListPage.class);
		uploadLink = new NavIntraLink("uploadLink", new ResourceModel("link.upload"), UploadPage.class);
		//validateLink = new NavIntraLink("validateLink", new ResourceModel("link.validate"), ValidationPage.class);

		listLink.setVisible(canUpload || canValidate);
		uploadLink.setVisible(canUpload);

		// SCO-107 - hide the validate link (interface is currently unimplemented)
		//validateLink.setVisible(canValidate);
		//validateLink.setVisibilityAllowed(false);

		Icon listIcon = new Icon("listIcon", LIST_ICON);
		Icon uploadIcon = new Icon("uploadIcon", UPLOAD_ICON);
		//Icon validateIcon = new Icon("validateIcon", VALIDATE_ICON);

		// SCO-109 - conditionally show the icons in the menu bar buttons
		boolean enableMenuBarIcons = serverConfigurationService.getBoolean( SAK_PROP_ENABLE_MENU_BUTTON_ICONS, true );
		if( enableMenuBarIcons )
		{
			listIcon.setVisible(canUpload || canValidate);
			uploadIcon.setVisible(canUpload);

			// SCO-107 hide the validate link (interface is currently unimplemented)
			//validateIcon.setVisible(canValidate);
			//validateIcon.setVisibilityAllowed(false);
		}
		else
		{
			listIcon.setVisibilityAllowed( false );
			uploadIcon.setVisibilityAllowed( false );
			//validateIcon.setVisibilityAllowed( false );
		}

		wmc.add(listIcon);
		wmc.add(uploadIcon);
		//wmc.add(validateIcon);

		wmc.add( listLink );
		wmc.add( uploadLink );
		//wmc.add( validateLink );

		// add the toolbar container
		add(wmc);

		add(newPageTitleLabel(params));
		add(feedback = new SakaiFeedbackPanel("feedback"));
		add(breadcrumbs = new BreadcrumbPanel("breadcrumbs"));
		
		Icon pageIcon = new Icon("pageIcon", getPageIconReference());
		pageIcon.setVisible(getPageIconReference() != null);
		add(pageIcon);
	}
	
	public void addBreadcrumb(IModel model, Class<?> pageClass, PageParameters params, boolean isEnabled) {
		breadcrumbs.addBreadcrumb(model, pageClass, params, isEnabled);
	}
	
	protected Label newPageTitleLabel(PageParameters params) {
		return new Label("page.title", new ResourceModel("page.title"));
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		// If a feedback message exists, then make the feedback panel visible, otherwise, hide it.
		feedback.setVisible(hasFeedbackMessage());
		breadcrumbs.setVisible(breadcrumbs.getNumberOfCrumbs() > 0);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderCSSReference(CONSOLE_CSS);
	}
	
	protected ResourceReference getPageIconReference() {
		return null;
	}
	
	protected boolean isSinglePackageTool() {
		return toolManager != null && 
				toolManager.getCurrentTool() != null && 
				"sakai.scorm.singlepackage.tool".equals(toolManager.getCurrentTool().getId());
	}

	/**
	 * Helper to disable a link. Add the Sakai class 'current'.
	 * @param link
	 */
	protected void disableLink(final NavIntraLink link) {
		link.add(new AttributeAppender("class", new Model<>("current"), " "));
		link.setEnabled(false);
	}
}
