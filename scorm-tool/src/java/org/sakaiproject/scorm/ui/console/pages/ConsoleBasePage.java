/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.scorm.ui.console.components.BreadcrumbPanel;
import org.sakaiproject.scorm.ui.upload.pages.UploadPage;
import org.sakaiproject.scorm.ui.validation.pages.ValidationPage;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;
import org.sakaiproject.wicket.markup.html.link.NavIntraLink;

public class ConsoleBasePage extends SakaiPortletWebPage {

	private static final long serialVersionUID = 1L;
	
	// The feedback panel component displays dynamic messages to the user
	private FeedbackPanel feedback;
	private BreadcrumbPanel breadcrumbs;
	
	public ConsoleBasePage() {
		this(null);
	}
	
	public ConsoleBasePage(PageParameters params) {
		add(newPageTitleLabel(params));
		add(new NavIntraLink("listLink", new ResourceModel("link.list"), PackageListPage.class));
		add(new NavIntraLink("uploadLink", new ResourceModel("link.upload"), UploadPage.class));
		add(new NavIntraLink("validateLink", new ResourceModel("link.validate"), ValidationPage.class));
		add(feedback = new FeedbackPanel("feedback"));
		add(breadcrumbs = new BreadcrumbPanel("breadcrumbs"));
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
	
}
