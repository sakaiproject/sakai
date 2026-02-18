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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Locale;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.context.annotation.DeferredImportSelector.Group.Entry;
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

	private SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);
	private SessionManager sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
	private SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
	private PreferencesService preferencesService = (PreferencesService) ComponentManager.get(PreferencesService.class);

	private Site getCurrentSite(String id) {
		Site site = null;
		try {
			site = siteService.getSite(id);
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
    public void removeExternalAssessment(String gradebookUId, String publishedAssessmentId, GradingService g)
        throws Exception {
        g.removeExternalAssignment(null, publishedAssessmentId, getAppName());
    }

  public boolean isAssignmentDefined(String assessmentTitle,
		  GradingService g)
  {
    String gradebookUId = GradebookFacade.getGradebookUId();
    return g.isAssignmentDefined(gradebookUId, gradebookUId, assessmentTitle);
  }
  
  public String getAppName()
  {
      return "sakai.samigo";
  }

  /**
   * Add a published assessment to gradebook.
   * @param publishedAssessment the published assessment
   * @param g  the Gradebook Service
   * @return false: cannot add to gradebook
   * @throws java.lang.Exception
   */
  public boolean addToGradebook(String gradebookUId, PublishedAssessmentData publishedAssessment, Long categoryId,
		  GradingService g) throws
    Exception
  {
    boolean added = false;
    String siteId = GradebookFacade.getGradebookUId();
    if (gradebookUId == null) {
      return false;
    }

    String title = StringEscapeUtils.unescapeHtml4(publishedAssessment.getTitle());
    if (!g.isAssignmentDefined(gradebookUId, siteId, title)) {
      g.addExternalAssessment(gradebookUId, siteId,
              publishedAssessment.getPublishedAssessmentId().toString(),
              null,
              title,
              publishedAssessment.getTotalScore(),
              publishedAssessment.getAssessmentAccessControl().getDueDate(),
              getAppName(), // Use the app name from sakai
              null,
              false,
              categoryId,
              publishedAssessment.getReference());
      added = true;
    }
    return added;
  }

  public void buildItemToGradebook(PublishedAssessmentData publishedAssessment, List<String> selectedGroups, GradingService g)
    throws Exception {
    boolean isGradebookGroupEnabled = g.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());

    if (isGradebookGroupEnabled) {
      Object categoryListMetaData = publishedAssessment.getAssessmentMetaDataMap().get(AssessmentMetaDataIfc.CATEGORY_LIST);
      String categoryString = categoryListMetaData != null ? (String) categoryListMetaData : "-1";
      categoryString = !categoryString.equals("-1") ? categoryString : "";

      Map<String, String> gradebookCategoryMap = g.buildCategoryGradebookMap(selectedGroups, categoryString, AgentFacade.getCurrentSiteId());
      for (Map.Entry<String, String> entry : gradebookCategoryMap.entrySet()) {
        boolean isExternalAssignmentDefined = g.isExternalAssignmentDefined(entry.getKey(), publishedAssessment.getPublishedAssessmentId().toString());

        if (!isExternalAssignmentDefined) {
          addToGradebook(entry.getKey(), publishedAssessment, !entry.getValue().equals("-1") ? Long.parseLong(entry.getValue()) : null, g);
        }
      }
    } else {
      Long categoryId = publishedAssessment.getCategoryId();
      boolean isExternalAssignmentDefined = g.isExternalAssignmentDefined(AgentFacade.getCurrentSiteId(),
        publishedAssessment.getPublishedAssessmentId().toString());

      if (!isExternalAssignmentDefined) {
        addToGradebook(GradebookFacade.getGradebookUId(), publishedAssessment, categoryId, g);
      }
    }
}

  /**
   * Update a gradebook.
   * @param publishedAssessment the published assessment
   * @param g  the Gradebook Service
   * @return false: cannot update the gradebook
   * @throws java.lang.Exception
   */
  public boolean updateGradebook(PublishedAssessmentIfc publishedAssessment, boolean isGradebookGroupEnabled,
		  List<String> gradebookList, Map<String, String> gradebookCategoryMap, GradingService g) throws Exception
  {
    log.debug("updateGradebook start");

    for (String gradebookUid : gradebookList) {
      String category = gradebookCategoryMap != null ? gradebookCategoryMap.get(gradebookUid) : null;

      log.debug("before g.updateExternalAssessment()");
      g.updateExternalAssessment(gradebookUid,
                publishedAssessment.getPublishedAssessmentId().toString(),
                null,
                null,
                publishedAssessment.getTitle(),
                category != null ? Long.parseLong(category) : null,
                publishedAssessment.getTotalScore(),
                publishedAssessment.getAssessmentAccessControl().getDueDate(),
                null);
    }

    return true;
  }

  /**
   * Update the grading of the assessment.
   * @param ag the assessment grading.
   * @param g  the Gradebook Service
   * @throws java.lang.Exception
   */
  public void updateExternalAssessmentScore(AssessmentGradingData ag, GradingService g) throws Exception {
    updateExternalAssessmentScore(ag, g, null);
  }

  /**
   * Update the grading of the assessment.
   * @param ag the assessment grading.
   * @param g  the Gradebook Service
   * @param assignmentId the Id of the gradebook assignment.
   * @throws java.lang.Exception
   */
  public void updateExternalAssessmentScore(AssessmentGradingData ag, GradingService g, Long assignmentId) throws Exception {
    boolean testErrorHandling = false;
    PublishedAssessmentService pubService = new PublishedAssessmentService();

    String siteId = GradebookFacade.getGradebookUId();

    if (siteId == null) {
        PublishedAssessmentFacade pub = pubService.getPublishedAssessment(ag.getPublishedAssessmentId().toString());
        // Get the siteId from the assessment, as it also works in an no-site context
        siteId = pub.getOwnerSiteId();
    }

    boolean isGradebookGroupEnabled = g.isGradebookGroupEnabled(siteId);

    String gradebookUId = siteId;

    if (!isGradebookGroupEnabled) {
      gradebookUId = pubService.getPublishedAssessmentOwner(ag.getPublishedAssessmentId());

      if (gradebookUId == null) {
        return;
      }
    } else {
      PublishedAssessmentFacade pAF = pubService.getPublishedAssessment(ag.getPublishedAssessmentId().toString());

      if (pAF != null) {
        if (assignmentId == null) {
          List<String> userGradebookList = g.getGradebookInstancesForUser(siteId, ag.getAgentId());
          Map<String, String> releaseToGroupsMap = pAF.getReleaseToGroups();

          for (String userGradebook : userGradebookList) {
            if (releaseToGroupsMap.containsKey(userGradebook)) {
              gradebookUId = userGradebook;
              break;
            }
          }
        } else {
          String foundGradebookUid = g.getGradebookUidByAssignmentById(siteId, assignmentId);

          if (foundGradebookUid != null && !StringUtils.isBlank(foundGradebookUid)) {
            gradebookUId = foundGradebookUid;
          }
        }
      }
    }

    //Will pass to null value when last submission is deleted
    String points = null;
    if(ag.getFinalScore() != null) {
        //SAM-1562 We need to round the double score and covert to a double -DH
        double fScore = Precision.round(ag.getFinalScore(), 2);
        Double score = Double.valueOf(fScore).doubleValue();
        points = getFormattedScore(score, siteId);
        log.debug("rounded:  " + ag.getFinalScore() + " to: " + score.toString() );
    }

    if (assignmentId == null) {
      g.updateExternalAssessmentScore(gradebookUId, siteId, ag.getPublishedAssessmentId().toString(), ag.getAgentId(), points);
    } else {
        // This is the student grading it's own submission, we need to grant permissions to the student temporarily.
        SecurityAdvisor securityAdvisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return "gradebook.gradeAll".equals(function) ? SecurityAdvice.ALLOWED : SecurityAdvice.PASS;
            }
        };

        try {
            securityService.pushAdvisor(securityAdvisor);
            g.setAssignmentScoreString(gradebookUId, siteId, assignmentId, ag.getAgentId(), points, null, null);
        } catch (Exception e) {
            log.error("Error while grading submission {} for agent {}", assignmentId, ag.getAgentId());
        } finally {
            securityService.popAdvisor(securityAdvisor);
        }

    }
    if (testErrorHandling){
      throw new Exception("Encountered an error in update ExternalAssessmentScore.");
    }
  }
  
  public void updateExternalAssessmentComment(AssessmentGradingData ag, String studentUid, String comment,
          GradingService g) throws Exception {
	  boolean testErrorHandling=false;
	  PublishedAssessmentService pubService = new PublishedAssessmentService();
	  Long publishedAssessmentId = ag.getPublishedAssessmentId();
	  String gradebookUId = pubService.getPublishedAssessmentOwner(publishedAssessmentId);

	  if (gradebookUId == null) {
		  return;
	  }

	  String siteId = GradebookFacade.getGradebookUId();

	  if (siteId == null) {
		  PublishedAssessmentFacade pub = pubService.getPublishedAssessment(ag.getPublishedAssessmentId().toString());
		  // Get the siteId from the assessment, as it also works in an no-site context
		  siteId = pub.getOwnerSiteId();
	  }

	  if (g.isGradebookGroupEnabled(siteId)) {
		  PublishedAssessmentFacade pAF = pubService.getPublishedAssessment(publishedAssessmentId.toString());
		  if (pAF != null) {
			  List<String> userGradebookList = g.getGradebookInstancesForUser(siteId, ag.getAgentId());
			  Map<String, String> releaseToGroupsMap = pAF.getReleaseToGroups();
			  for (String userGradebook : userGradebookList) {
				  if (releaseToGroupsMap.containsKey(userGradebook)) {
					  gradebookUId = userGradebook;
					  break;
				  }
			  }
		  }
	  }

	  g.updateExternalAssessmentComment(gradebookUId, siteId, publishedAssessmentId.toString(), studentUid, comment);

	  if (testErrorHandling){
          throw new Exception("Encountered an error in update ExternalAssessmentComment.");
	  }
  }


	public Long getExternalAssessmentCategoryId(String gradebookUId,
        String publishedAssessmentId, GradingService g) {
        try {
            return g.getExternalAssessmentCategoryId(gradebookUId, publishedAssessmentId);
        }
        catch (AssessmentNotFoundException e) {
            log.info("No category defined for publishedAssessmentId={} in gradebookUid={}", publishedAssessmentId, gradebookUId);
        }
		return null;
	}

  private String getFormattedScore(Double score, String siteId) {
    String currentLocaleStr = null;
    String userId = AgentFacade.getEid();

    try {
      Site site = siteService.getSite(siteId);
      ResourceProperties siteProperties = site.getProperties();
      currentLocaleStr = (String) siteProperties.get("locale_string");

    } catch (IdUnusedException ex) {
      log.warn("Unable to retrieve site properties for siteId {} : {}", siteId, ex.toString());
    }

    if (currentLocaleStr == null && userId != null) {
      currentLocaleStr = preferencesService.getLocale(userId).toString();
    }

    if (currentLocaleStr == null) {
      currentLocaleStr = Locale.ENGLISH.toString();
    }

    String[] localeParts = new String[]{"", ""};
    List localePartsList = Arrays.asList(currentLocaleStr.split("_"));
    for (int i = 0; i < localePartsList.size(); i++) {
      localeParts[i] = localePartsList.get(i).toString();
    }

    NumberFormat nf = NumberFormat.getInstance(new Locale(localeParts[0], localeParts[1]));
    return nf.format(score);
  }

  public List<String> getGradebookList(boolean isGradebookGroupEnabled, String[] groupsAuthorized) {
    List<String> gradebookList = new ArrayList<>();

    if (isGradebookGroupEnabled && groupsAuthorized != null) {
      gradebookList = Arrays.asList(groupsAuthorized);
    } else {
      gradebookList.add(GradebookFacade.getGradebookUId());
    }

    return gradebookList;
  }

  public boolean isGradebookGroupEnabled(org.sakaiproject.grading.api.GradingService gradingService) {
    return gradingService.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());
  }

  public void manageScoresToNewGradebook(org.sakaiproject.tool.assessment.services.GradingService samigoGradingService,
    GradingService gradingService, PublishedAssessmentFacade assessment, PublishedEvaluationModel evaluation) {

    Integer scoringType = evaluation.getScoringType();

			// need to decide what to tell gradebook
			List<AssessmentGradingData> gradingDataList;

			if (EvaluationModelIfc.HIGHEST_SCORE.equals(scoringType)) {
				gradingDataList = samigoGradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
			}
			else {
				gradingDataList = samigoGradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
			}

			log.debug("list size = {}", gradingDataList.size());
			for (AssessmentGradingData ag : gradingDataList) {
				try {
					log.debug("ag.scores " + ag.getTotalAutoScore());
					// Send the average score if average was selected for multiple submissions
					if (scoringType.equals(EvaluationModelIfc.AVERAGE_SCORE)) {
						// status = 5: there is no submission but grader update something in the score page
						if(ag.getStatus() ==5) {
							ag.setFinalScore(ag.getFinalScore());
						} else {
							Double averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
							getAverageSubmittedAssessmentGrading(assessment.getPublishedAssessmentId(), ag.getAgentId());
							ag.setFinalScore(averageScore);
						}
					}

          if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
            updateExternalAssessmentScore(ag, gradingService);
            updateExternalAssessmentComment(ag, ag.getAgentId() , ag.getComments(), gradingService);
          }

          if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
            String gradebookItemIdString = assessment.getAssessmentToGradebookNameMetaData();

						if (isGradebookGroupEnabled(gradingService)) {
							if (gradebookItemIdString != null && !StringUtils.isBlank(gradebookItemIdString)) {
                Long gradebookItemId = gradingService.getMatchingUserGradebookItemId(AgentFacade.getCurrentSiteId(),
                  ag.getAgentId(), gradebookItemIdString);

                if (gradebookItemId != null) {
                  updateExternalAssessmentScore(ag, gradingService, gradebookItemId);
                }
							}
						} else {
							updateExternalAssessmentScore(ag, gradingService, Long.valueOf(gradebookItemIdString));
						}
					}
				}
				catch (Exception e) {
					log.warn("Exception occues in " + ag + "th record. Message:" + e.getMessage());
				}
			}
  }
}
