/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.integration.helper.integrated;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.assessment.
  PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.assessment.data.dao.assessment.
  PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.spring.SpringBeanLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.
  PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.
  GradebookServiceHelper;

/**
 *
 * <p>Description:
 * This is an integrated context implementation helper delegate class for
 * the GradebookService class.
 * "Integrated" means that Samigo (Tests and Quizzes)
 * is running within the context of the Sakai portal and authentication
 * mechanisms, and therefore makes calls on Sakai for things it needs.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class GradebookServiceHelperImpl implements GradebookServiceHelper
{
  private static Log log = LogFactory.getLog(GradebookServiceHelperImpl.class);

  /**
   * Does a gradebook exist?
   * @param gradebookUId the gradebook id
   * @param g  the Gradebook Service
   * @return true if the given gradebook exists
   */
  public boolean gradebookExists(String gradebookUId, GradebookService g)
  {
    log.debug("GradebookService = " + g);
    if (gradebookUId == null)
    {
      return false;
    }
    return g.gradebookExists(gradebookUId);
  }

  /**
   * Remove a published assessment from the gradebook.
   * @param gradebookUId the gradebook id
   * @param g  the Gradebook Service
   * @param publishedAssessmentId the id of the published assessment
   * @throws java.lang.Exception
   */
public void removeExternalAssessment(String gradebookUId,
   String publishedAssessmentId, GradebookService g) throws Exception
  {
    if (g.gradebookExists(gradebookUId))
    {
      g.removeExternalAssessment(gradebookUId, publishedAssessmentId);
    }
  }

  /**
   * Add a published assessment to gradebook.
   * @param publishedAssessment the published assessment
   * @param g  the Gradebook Service
   * @return false: cannot add to gradebook
   * @throws java.lang.Exception
   */
  public boolean addToGradebook(PublishedAssessmentData publishedAssessment,
                                GradebookService g) throws
    Exception
  {
    log.info("total point(s) is/are =" +
              publishedAssessment.getTotalScore().longValue());
    log.info("gradebookId =" + GradebookFacade.getGradebookUId());
    boolean added = false;
    log.info("GradebookService instance=" + g);
    String gradebookUId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null)
    {
      return false;
    }

    log.info("inside addToGradebook, gradebook exists? " +
              g.gradebookExists(gradebookUId));
    if (g.gradebookExists(gradebookUId))
    {

      // Tool name code added by Josh Holtzman
      Tool tool = ToolManager.getTool("sakai.samigo");
      String appName = null;

      if (tool == null)
      {
        log.warn(
          "could not get tool named sakai.samigo, " +
          "so we're going to assume we're called 'Tests & Quizzes'");
        appName = "Tests & Quizzes";
      }
      else
      {
        appName = tool.getTitle();
      }

      if(!g.isAssignmentDefined(gradebookUId, publishedAssessment.getTitle()))
      {
        g.addExternalAssessment(gradebookUId,
                              publishedAssessment.getPublishedAssessmentId().
                              toString(), null,
                              publishedAssessment.getTitle(),
                              publishedAssessment.getTotalScore().doubleValue(),
                              publishedAssessment.getAssessmentAccessControl().
                              getDueDate(),
                              appName); // Use the app name from sakai
        added = true;
      }
    }
    return added;
  }

  /**
   * Update the grading of the assessment.
   * @param ag the assessment grading.
   * @param g  the Gradebook Service
   * @throws java.lang.Exception
   */
  public void updateExternalAssessmentScore(AssessmentGradingIfc ag,
   GradebookService g) throws
    Exception
  {
    log.info("GradebookService instance=" + g);
    PublishedAssessmentService publishedAssessmentService = new
      PublishedAssessmentService();
    String gradebookUId =
      publishedAssessmentService.getPublishedAssessmentOwner(
        ag.getPublishedAssessment().getPublishedAssessmentId());
    if (gradebookUId == null)
    {
      return;
    }
    g.updateExternalAssessmentScore(gradebookUId,
      ag.getPublishedAssessment().getPublishedAssessmentId().toString(),
      ag.getAgentId(), new Double(ag.getFinalScore().doubleValue()));
  }

}