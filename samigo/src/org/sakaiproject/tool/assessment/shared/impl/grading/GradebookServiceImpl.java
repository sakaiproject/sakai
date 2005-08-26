/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/gradebook/GradebookServiceHelper.java $
   * $Id: GradebookServiceHelper.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.assessment.shared.impl.grading;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.services.gradebook.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;

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
  private static final GradebookServiceHelper helper = new GradebookServiceHelper();
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
