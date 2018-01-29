/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/tags/sakai-10.4/samigo-services/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/GradebookServiceHelperImpl.java $
 * $Id: GradebookServiceHelperImpl.java 127473 2013-07-21 00:04:12Z nbotimer@unicon.net $
 ***********************************************************************************
 *
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
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

  /**
   * Does a gradebook exist?
   * @param gradebookUId the gradebook id
   * @param g  the Gradebook Service
   * @return true if the given gradebook exists
   */
  public boolean gradebookExists(String gradebookUId, GradebookExternalAssessmentService g)
  {
    log.debug("GradebookService = " + g);
    if (gradebookUId == null)
    {
      return false;
    }
    return g.isGradebookDefined(gradebookUId);
  }
  
	/**
	 *  Does a gradebook exist?
	 * @param siteId  the site id
	 * @return true if the given gradebook exists
	 */
	public boolean isGradebookExist(String siteId)
	{
		Site currentSite = getCurrentSite(siteId);
		if (currentSite == null) {
			return false;
		}
		SitePage page = null;
		String toolId = null;
		try {
			// get page
			List pageList = currentSite.getPages();
			for (int i = 0; i < pageList.size(); i++) {
				page = (SitePage) pageList.get(i);
				List pageToolList = page.getTools();
				try {
					toolId = ((ToolConfiguration) pageToolList.get(0)).getTool().getId();
				} catch (Exception ee) {
					log.warn(siteId + " contains a page (" + page.getTitle() + ") without a valid tool registration");
				}
				if (toolId != null && toolId.equalsIgnoreCase("sakai.gradebook.tool")) {
					return true;
				} else if (toolId != null && toolId.equalsIgnoreCase("sakai.gradebook.gwt.rpc")) {
					return true;
				} else if (toolId != null && toolId.equalsIgnoreCase("sakai.gradebookng")) {
					return true;
				}

			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		return false;
	}

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
public void removeExternalAssessment(String gradebookUId,
   String publishedAssessmentId, GradebookExternalAssessmentService g) throws Exception
  {
    if (g.isGradebookDefined(gradebookUId))
    {
      g.removeExternalAssessment(gradebookUId, publishedAssessmentId);
    }
  }

  public boolean isAssignmentDefined(String assessmentTitle,
		  GradebookExternalAssessmentService g) throws Exception
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
		  GradebookExternalAssessmentService g) throws
    Exception
  {
    boolean added = false;
    String gradebookUId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null)
    {
      return false;
    }

    if (g.isGradebookDefined(gradebookUId))
    {
      String title = StringEscapeUtils.unescapeHtml(publishedAssessment.getTitle());
      if(!g.isAssignmentDefined(gradebookUId, title))
      {
        g.addExternalAssessment(gradebookUId,
                              publishedAssessment.getPublishedAssessmentId().
                              toString(), null,
                              title,
                              publishedAssessment.getTotalScore().doubleValue(),
                              publishedAssessment.getAssessmentAccessControl().
                              getDueDate(),
                              getAppName(),	// Use the app name from sakai
                              false,
                              categoryId); 
        added = true;
      }
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
		  GradebookExternalAssessmentService g) throws Exception
  {
    log.debug("updateGradebook start");
    String gradebookUId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null)
    {
      return false;
    }

    log.debug("before g.isAssignmentDefined()");
	g.updateExternalAssessment(gradebookUId,
				publishedAssessment.getPublishedAssessmentId().
				toString(), null,
				publishedAssessment.getTitle(),
				publishedAssessment.getTotalScore().doubleValue(),
				publishedAssessment.getAssessmentAccessControl().
				getDueDate());
    return true;
  }

  /**
   * Update the grading of the assessment.
   * @param ag the assessment grading.
   * @param g  the Gradebook Service
   * @throws java.lang.Exception
   */
  public void updateExternalAssessmentScore(AssessmentGradingData ag,
		  GradebookExternalAssessmentService g) throws
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
		  GradebookExternalAssessmentService g) throws Exception {
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
          GradebookExternalAssessmentService g) throws Exception {
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
			String publishedAssessmentId, GradebookExternalAssessmentService g) {
		Long categoryId = null;
		if (g.isGradebookDefined(gradebookUId)) 
		{
			categoryId = g.getExternalAssessmentCategoryId(gradebookUId, publishedAssessmentId);
		}
		return categoryId;
	}

}
