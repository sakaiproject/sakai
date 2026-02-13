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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

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

  public boolean addToGradebook(String gradebookUId, PublishedAssessmentData publishedAssessment, Long categoryId,
		  GradingService g) throws Exception;

  public void buildItemToGradebook(PublishedAssessmentData publishedAssessment,
    List<String> selectedGroups, GradingService g) throws Exception;

  public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment, boolean isGradebookGroupEnabled,
    List<String> gradebookList, Map<String, String> gradebookCategoryMap, GradingService g) throws Exception;

  public boolean isAssignmentDefined(String assessmentTitle, GradingService g);

  public void updateExternalAssessmentScore(AssessmentGradingData ag,
		  GradingService g) throws Exception;
  
  public void updateExternalAssessmentComment(AssessmentGradingData ag, String studentUid, String comment, 
		  GradingService g) throws Exception;
  
  public Long getExternalAssessmentCategoryId(String gradebookUId,
		  String publishedAssessmentId, GradingService g);

  public String getAppName();

  public void updateExternalAssessmentScore(AssessmentGradingData ag, GradingService g, Long assignmentId) throws Exception;
  
  public List<String> getGradebookList(boolean isGradebookGroupEnabled, String[] groupsAuthorized);

  public boolean isGradebookGroupEnabled(org.sakaiproject.grading.api.GradingService gradingService);

  public void manageScoresToNewGradebook(org.sakaiproject.tool.assessment.services.GradingService samigoGradingService,
    GradingService gradingService, PublishedAssessmentFacade assessment, PublishedEvaluationModel evaluation);

}
