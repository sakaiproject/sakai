/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui.standalone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.component.section.support.IntegrationSupport;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;

public class FrameworkManagerSectionsImpl implements FrameworkManager {
	private static Log log = LogFactory.getLog(FrameworkManagerSectionsImpl.class);

	private IntegrationSupport integrationSupport;
	private GradebookManager gradebookManager;

	public List getAccessibleGradebooks(String userUid) {
		List gradebooks = new ArrayList();
		List siteMemberships = integrationSupport.getAllSiteMemberships(userUid);
		for (Iterator iter = siteMemberships.iterator(); iter.hasNext(); ) {
			ParticipationRecord participationRecord = (ParticipationRecord)iter.next();
			Course course = (Course)participationRecord.getLearningContext();
			String siteContext = course.getSiteContext();
            Gradebook gradebook = null;
            try {
                gradebook = getGradebookManager().getGradebook(siteContext);
                gradebooks.add(gradebook);
            } catch (GradebookNotFoundException gnfe) {
            	if (log.isInfoEnabled()) log.info("no gradebook found for " + siteContext);
            }
        }
		return gradebooks;
	}

	public IntegrationSupport getIntegrationSupport() {
		return integrationSupport;
	}
	public void setIntegrationSupport(IntegrationSupport integrationSupport) {
		this.integrationSupport = integrationSupport;
	}

	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}

}