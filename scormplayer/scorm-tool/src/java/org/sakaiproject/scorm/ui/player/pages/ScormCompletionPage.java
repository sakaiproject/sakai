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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.service.api.LearningManagementSystem;

public class ScormCompletionPage extends NotificationPage
{
	private static final String CLOSE_ON_LOAD = "setTimeout('window.close()', 5000);";

	private static final long serialVersionUID = 1L;

	@SpringBean
	LearningManagementSystem lms;

	public ScormCompletionPage()
	{
		this(new PageParameters());
	}

	public ScormCompletionPage(PageParameters pageParams)
	{
		super();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript(CLOSE_ON_LOAD));
	}
}
