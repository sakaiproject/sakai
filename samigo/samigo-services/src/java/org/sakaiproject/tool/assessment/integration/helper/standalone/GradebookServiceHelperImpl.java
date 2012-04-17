/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
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

	public boolean isAssignmentDefined(String assessmentTitle, GradebookExternalAssessmentService g)
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
	public boolean addToGradebook(PublishedAssessmentData publishedAssessment, GradebookExternalAssessmentService g)
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
	public void removeExternalAssessment(String gradebookUId, String publishedAssessmentId, GradebookExternalAssessmentService g) throws Exception
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
	public boolean gradebookExists(String gradebookUId, GradebookExternalAssessmentService g)
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
	public void updateExternalAssessmentScore(AssessmentGradingData ag, GradebookExternalAssessmentService g) throws Exception
	{
	}

	public void updateExternalAssessmentScores(Long publishedAssessmentId, final Map studentUidsToScores,
			GradebookExternalAssessmentService g) throws Exception {
	}
	
	public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment, GradebookExternalAssessmentService g) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

}
