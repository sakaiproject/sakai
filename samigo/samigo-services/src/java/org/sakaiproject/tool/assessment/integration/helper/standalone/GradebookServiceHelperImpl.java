/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/GradebookServiceHelperImpl.java $
 * $Id: GradebookServiceHelperImpl.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Sakai Foundation
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;

/**
 * <p>
 * Description: This is a stub standalone context implementation helper delegate class for the GradebookService class.<br />
 * The helper methods are stubs because in standalone there isn't gradebook integration.<br />
 * "Standalone" means that Samigo (Tests and Quizzes) is running without the context of the Sakai portal and authentication mechanisms, and therefore we use stub methods.
 * </p>
 * <p>
 * Note: To customize behavior you can add your own helper class to the Spring injection via the integrationContext.xml for your context.<br />
 * The particular integrationContext.xml to be used is selected by the build process.
 * </p>
 * 
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class GradebookServiceHelperImpl implements GradebookServiceHelper
{
	private static Log log = LogFactory.getLog(GradebookServiceHelperImpl.class);

	public boolean isAssignmentDefined(String assessmentTitle, GradebookService g)
	{
		return false;
	}

	/**
	 * STUB. NO-OP.
	 * 
	 * @param publishedAssessment
	 *        the published assessment
	 * @param g
	 *        the Gradebook Service
	 * @return false: cannot add to gradebook
	 * @throws java.lang.Exception
	 */
	public boolean addToGradebook(PublishedAssessmentData publishedAssessment, GradebookService g)
	{
		return false;
	}

	/**
	 * STUB. NO-OP.
	 * 
	 * @param gradebookUId
	 *        the gradebook id
	 * @param publishedAssessmentId
	 *        the id of the published assessment
	 * @param g
	 *        the Gradebook Service
	 * @throws java.lang.Exception
	 */
	public void removeExternalAssessment(String gradebookUId, String publishedAssessmentId, GradebookService g) throws Exception
	{
	}

	/**
	 * Always returns false, because standalone.
	 * 
	 * @param gradebookUId
	 *        the gradebook id
	 * @param g
	 *        the Gradebook Service
	 * @return false, no gradebook integration
	 */
	public boolean gradebookExists(String gradebookUId, GradebookService g)
	{
		return false;
	}
	
	/**
	 * Always returns false, because standalone.
	 * 
	 * @param siteId
	 *        the site id
	 * @return false, no gradebook integration
	 */
	public boolean isGradebookExist(String siteId)
	{
		return false;
	}

	/**
	 * STUB. NO-OP.
	 * 
	 * @param ag
	 *        the assessment grading
	 * @param g
	 *        the Gradebook Service
	 * @throws java.lang.Exception
	 */
	public void updateExternalAssessmentScore(AssessmentGradingIfc ag, GradebookService g) throws Exception
	{
	}

	public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment, GradebookService g) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

}
