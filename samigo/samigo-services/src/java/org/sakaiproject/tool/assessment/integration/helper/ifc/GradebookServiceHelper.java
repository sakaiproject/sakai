/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/ifc/GradebookServiceHelper.java $
 * $Id: GradebookServiceHelper.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.integration.helper.ifc;
import java.io.Serializable;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;

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
  public boolean gradebookExists(String gradebookUId, GradebookService g);
  
  public boolean isGradebookExist(String SiteId);

  public void removeExternalAssessment(String gradebookUId,
     String publishedAssessmentId, GradebookService g) throws Exception;

  public boolean addToGradebook(PublishedAssessmentData publishedAssessment,
    GradebookService g) throws Exception;

  public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment,
    GradebookService g) throws Exception;

  public boolean isAssignmentDefined(String assessmentTitle,
    GradebookService g) throws Exception;

  public void updateExternalAssessmentScore(AssessmentGradingIfc ag,
    GradebookService g) throws Exception;
}
