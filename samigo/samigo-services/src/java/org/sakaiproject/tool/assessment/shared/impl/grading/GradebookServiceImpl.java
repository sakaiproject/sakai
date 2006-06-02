/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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


package org.sakaiproject.tool.assessment.shared.impl.grading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.gradebook.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceException;

/**
 * The GradebookServiceAPI describes an interface for gradebook information
 * for published assessments.  Implemented by wrapping GradebookServiceHelper().
 * Right that is a stub implementation, but this is designed to continue to work
 * if it isn't.
 *
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class GradebookServiceImpl implements GradebookServiceAPI
{
  private static Log log = LogFactory.getLog(GradebookServiceImpl.class);

  private static final GradebookServiceHelper helper = new GradebookServiceHelper();


  public boolean isAssignmentDefined(String assessmentTitle)
  {
    try
    {
      return helper.isAssignmentDefined(assessmentTitle);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Add this published assessment to the site.
   * @param publishedAssessment, must be castable to PublishedAssessmentData
   * @return true if added
   */
  public boolean addToGradebook(PublishedAssessmentIfc publishedAssessment)
  {
    try
    {
      // this a little convoluted
      // our internal data representation uses OSIDs which declare an
      // 'any type' data property, but our OOB standard has data that is
      // a PublishedAssessmentData which is the implementation of
      // PublishedAssessmentIfc
      Long id = publishedAssessment.getPublishedAssessmentId();
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      PublishedAssessmentFacade pubFacade =
        pubService.getPublishedAssessment(id.toString());
      PublishedAssessmentIfc data = pubFacade.getData();
      return helper.addToGradebook((PublishedAssessmentData) data);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Remove published assessment.
   * @param siteId the site id
   * @param publishedAssessmentId teh published assessment id
   */
  public void removeExternalAssessment(String siteId, String publishedAssessmentId)
  {
    try
    {
      helper.removeExternalAssessment(siteId, publishedAssessmentId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * @todo fix
   * Update the assessment for the agent's grade.
   * @param ag the assessment grading data
   * @param agentIdString agent id
   */
  public void updateExternalAssessment(AssessmentGradingIfc ag, String agentIdString)
  {
    try
    {
//      GradingService service = new GradingService();
//      AssessmentGradingData data = service.load(ag.getAssessmentGradingId().toString());
//      helper.updateExternalAssessment(data, agentIdString);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Determine if a gradebook exists for the site.
   * @param siteId the site id
   * @return
   */
  public boolean gradebookExists(String siteId)
  {
    try
    {
      return helper.gradebookExists(siteId);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

  /**
   * Update the score in the gradebook.
   * @param ag the assessment grading interface
   */
  public void updateExternalAssessmentScore(AssessmentGradingIfc ag)
  {
    try
    {
      helper.updateExternalAssessmentScore(ag);
    }
    catch (Exception ex)
    {
      throw new GradingServiceException(ex);
    }
  }

}
