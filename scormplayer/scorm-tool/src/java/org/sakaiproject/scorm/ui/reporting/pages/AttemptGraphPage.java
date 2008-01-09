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
package org.sakaiproject.scorm.ui.reporting.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.ui.player.pages.BaseToolPage;
import org.sakaiproject.scorm.ui.reporting.components.CMIDataGraph;

public class AttemptGraphPage extends BaseToolPage {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	transient ScormResultService resultService;
	
	public AttemptGraphPage(PageParameters params) {
		long contentPackageId = params.getLong("contentPackageId");
		String learnerId = params.getString("learnerId");
		String activityId = params.getString("activityId");
		long attemptNumber = params.getLong("attemptNumber");
		
		Attempt attempt = resultService.getAttempt(contentPackageId, learnerId, attemptNumber);
		
		//add(new AttemptGraph("attemptGraph", attempt, activityId));
	}
	
}
