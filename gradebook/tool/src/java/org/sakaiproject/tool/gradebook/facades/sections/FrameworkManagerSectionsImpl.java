/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sections;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.LearningContext;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.component.section.support.IntegrationSupport;

import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.standalone.FrameworkManager;

public class FrameworkManagerSectionsImpl extends AbstractSectionsImpl implements FrameworkManager {
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