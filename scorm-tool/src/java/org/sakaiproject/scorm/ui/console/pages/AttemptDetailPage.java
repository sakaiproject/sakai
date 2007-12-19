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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.console.components.CMIFieldGroupPanel;

public class AttemptDetailPage extends ConsoleBasePage {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormResultService resultService;
	@SpringBean
	ScormContentService contentService;
	
	public AttemptDetailPage(PageParameters pageParams) {
		String learnerName = pageParams.getString("learnerName");
		String learnerId = pageParams.getString("learnerId");
		long attemptNumber = pageParams.getLong("attemptNumber");
		long id = pageParams.getLong("id");
		
		String[] fields = {"cmi.completion_status", "cmi.score.scaled", "cmi.success_status" };
		
		Attempt attempt = resultService.getAttempt(id);
		CMIFieldGroup fieldGroup = resultService.getAttemptResults(attempt);
		
		ContentPackage contentPackage = contentService.getContentPackage(attempt.getContentPackageId());
		
		add(new Label("content.package.name", contentPackage.getTitle()));
		add(new Label("learner.name", learnerName));
		
		add(new CMIFieldGroupPanel("fieldGroupPanel", fieldGroup));
	}

	
}
