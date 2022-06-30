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

package org.sakaiproject.tool.assessment.integration.helper.ifc;
import java.io.Serializable;
import java.util.Map;

import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

/**
 * <p>Description:
 * This is a context implementation helper delegate interface for
 * the GradebookService class.  Using Spring injection via the
 * integrationContext.xml selected by the build process for the implementation.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface GradebookServiceHelper extends Serializable
{
  public void removeExternalAssessment(String gradebookUId,
     String publishedAssessmentId, GradingService g) throws Exception;

  public boolean addToGradebook(PublishedAssessmentData publishedAssessment, Long categoryId,
		  GradingService g) throws Exception;

  public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment,
		  GradingService g) throws Exception;

  public boolean isAssignmentDefined(String assessmentTitle,
		  GradingService g) throws Exception;

  public void updateExternalAssessmentScore(AssessmentGradingData ag,
		  GradingService g) throws Exception;
  
  public void updateExternalAssessmentScores(Long publishedAssessmentId, final Map<String, Double> studentUidsToScores,
		  GradingService g) throws Exception;
  
  public void updateExternalAssessmentComment(Long publishedAssessmentId, String studentUid, String comment, 
		  GradingService g) throws Exception;
  
  public Long getExternalAssessmentCategoryId(String gradebookUId,
		  String publishedAssessmentId, GradingService g);

  public String getAppName();
}
