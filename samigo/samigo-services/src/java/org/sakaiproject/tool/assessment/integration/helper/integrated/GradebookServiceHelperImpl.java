/**********************************************************************************
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.cover.ToolManager;

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
@Slf4j
public class GradebookServiceHelperImpl implements GradebookServiceHelper
{
	private Site getCurrentSite(String id) {
		Site site = null;
		try {
			site = SiteService.getSite(id);
		} catch (IdUnusedException e) {
			log.error(e.getMessage());
		}
		return site;
	}
	
   /**
    * Remove a published assessment from the gradebook.
    * @param gradebookUId the gradebook id
    * @param g  the Gradebook Service
    * @param publishedAssessmentId the id of the published assessment
    * @throws java.lang.Exception
    */
    public void removeExternalAssessment(String gradebookUId, String publishedAssessmentId, org.sakaiproject.grading.api.GradingService g)
        throws Exception {

        g.removeExternalAssignment(gradebookUId, publishedAssessmentId);
    }

  public boolean isAssignmentDefined(String assessmentTitle,
		  org.sakaiproject.grading.api.GradingService g) throws Exception
  {
    String gradebookUId = GradebookFacade.getGradebookUId();
    return g.isAssignmentDefined(gradebookUId, assessmentTitle);
  }
  
  public String getAppName()
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
      return appName;
  }

  /**
   * Add a published assessment to gradebook.
   * @param publishedAssessment the published assessment
   * @param g  the Gradebook Service
   * @return false: cannot add to gradebook
   * @throws java.lang.Exception
   */
  public boolean addToGradebook(PublishedAssessmentData publishedAssessment, Long categoryId, 
		  org.sakaiproject.grading.api.GradingService g) throws
    Exception
  {
    boolean added = false;
    String gradebookUId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null)
    {
      return false;
    }

    String title = StringEscapeUtils.unescapeHtml4(publishedAssessment.getTitle());
    if(!g.isAssignmentDefined(gradebookUId, title))
    {
      g.addExternalAssessment(gradebookUId,
              publishedAssessment.getPublishedAssessmentId().toString(),
              null,
              title,
              publishedAssessment.getTotalScore(),
              publishedAssessment.getAssessmentAccessControl().getDueDate(),
              getAppName(), // Use the app name from sakai
              null,
              false,
              categoryId);
      added = true;
    }
    return added;
  }

  /**
   * Update a gradebook.
   * @param publishedAssessment the published assessment
   * @param g  the Gradebook Service
   * @return false: cannot update the gradebook
   * @throws java.lang.Exception
   */
  public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment,
		  org.sakaiproject.grading.api.GradingService g) throws Exception
  {
    log.debug("updateGradebook start");
    String gradebookUId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null)
    {
      return false;
    }

    log.debug("before g.isAssignmentDefined()");
	g.updateExternalAssessment(gradebookUId,
            publishedAssessment.getPublishedAssessmentId().toString(),
            null,
            null,
            publishedAssessment.getTitle(),
            publishedAssessment.getTotalScore(),
            publishedAssessment.getAssessmentAccessControl().getDueDate());
    return true;
  }

  /**
   * Update the grading of the assessment.
   * @param ag the assessment grading.
   * @param g  the Gradebook Service
   * @throws java.lang.Exception
   */
  public void updateExternalAssessmentScore(AssessmentGradingData ag,
		  org.sakaiproject.grading.api.GradingService g) throws
    Exception
  {
    boolean testErrorHandling=false;
    PublishedAssessmentService pubService = new PublishedAssessmentService();
    GradingService gradingService = new GradingService();

    //Following line seems just for the need of getting publishedAssessmentId
    //use ag.getPublishedAssessmentId() instead of pub.getPublishedAssessmentId() to
    //get publishedAssessmentId 
    //comment out following 3 lines since it returns null for not submitted students which we 
    //need not save to assessmentgrading table but will need to notify gradebook only  
    
   /* PublishedAssessmentIfc pub = (PublishedAssessmentIfc) gradingService.getPublishedAssessmentByAssessmentGradingId(ag.getAssessmentGradingId().toString());

    String gradebookUId = pubService.getPublishedAssessmentOwner(
           pub.getPublishedAssessmentId());*/
    String gradebookUId = pubService.getPublishedAssessmentOwner(
            ag.getPublishedAssessmentId());
    if (gradebookUId == null)
    {
      return;
    }
    
    //Will pass to null value when last submission is deleted
    String points = null;
    if(ag.getFinalScore() != null) {
        //SAM-1562 We need to round the double score and covert to a double -DH
        double fScore = Precision.round(ag.getFinalScore(), 2);
        Double score = Double.valueOf(fScore).doubleValue();
        points = score.toString();
        log.info("rounded:  " + ag.getFinalScore() + " to: " + score.toString() );
    }
    g.updateExternalAssessmentScore(gradebookUId,
      ag.getPublishedAssessmentId().toString(),
      ag.getAgentId(),  points);
    if (testErrorHandling){
      throw new Exception("Encountered an error in update ExternalAssessmentScore.");
    }
  }
  
  public void updateExternalAssessmentScores(Long publishedAssessmentId, final Map<String, Double> studentUidsToScores,
		  org.sakaiproject.grading.api.GradingService g) throws Exception {
	  boolean testErrorHandling=false;
	  PublishedAssessmentService pubService = new PublishedAssessmentService();
	  String gradebookUId = pubService.getPublishedAssessmentOwner(publishedAssessmentId);
	  if (gradebookUId == null) {
		  return;
	  }
	  g.updateExternalAssessmentScores(gradebookUId,
			  publishedAssessmentId.toString(),
			  studentUidsToScores);

	  if (testErrorHandling){
		  throw new Exception("Encountered an error in update ExternalAssessmentScore.");
	  }
  }
  
  public void updateExternalAssessmentComment(Long publishedAssessmentId, String studentUid, String comment,
          org.sakaiproject.grading.api.GradingService g) throws Exception {
	  boolean testErrorHandling=false;
	  PublishedAssessmentService pubService = new PublishedAssessmentService();
	  String gradebookUId = pubService.getPublishedAssessmentOwner(publishedAssessmentId);
	  if (gradebookUId == null) {
		  return;
	  }	
	  g.updateExternalAssessmentComment(gradebookUId, publishedAssessmentId.toString(), studentUid, comment);

	  if (testErrorHandling){
          throw new Exception("Encountered an error in update ExternalAssessmentComment.");
	  }
  }


	public Long getExternalAssessmentCategoryId(String gradebookUId,
        String publishedAssessmentId, org.sakaiproject.grading.api.GradingService g) {
        try {
            return g.getExternalAssessmentCategoryId(gradebookUId, publishedAssessmentId);
        }
        catch (AssessmentNotFoundException e) {
            log.info("No category defined for publishedAssessmentId={} in gradebookUid={}", publishedAssessmentId, gradebookUId);
        }
		return null;
	}

}
