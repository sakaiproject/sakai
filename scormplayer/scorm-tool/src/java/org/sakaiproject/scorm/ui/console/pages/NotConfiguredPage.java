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

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;

@Slf4j
public class NotConfiguredPage extends SakaiPortletWebPage implements IHeaderContributor
{
	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormContentService")
	ScormContentService contentService;
	
	public NotConfiguredPage()
	{
		// add components
		add(new FeedbackPanel("feedback"));
		add(new Label("page.title", getLocalizer().getString("page.title", this)));

		error(getLocalizer().getString("designated.resource.notfound", this, new Model<>(this)));
	}

	public String getConfigProperty()
	{
		return DisplayDesignatedPackage.CFG_PACKAGE_NAME;
	}
}
