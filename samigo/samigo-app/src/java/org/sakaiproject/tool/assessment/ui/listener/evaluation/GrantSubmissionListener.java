/**********************************************************************************
 * 
 * 
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>
 * This handles the deletion of student submission on the Score page.
 *  </p>
 * <p>Description: Action Listener for deletion of student's submission on the Score page</p>
 * <p>Organization: Sakai Project</p>
 * @author Texas State University
 */
@Slf4j
public class GrantSubmissionListener
  implements ActionListener
{
  private final EventTrackingService eventTrackingService = ComponentManager.get( EventTrackingService.class );

  /**
   * Increases submissions remaining by 1.
   * Gives students an additional attempt.
   * 
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("GrantSubmission LISTENER.");

    TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");

	String gradingIdParam = ContextUtil.lookupParam("gradingData");
	String publishedAssessmentId = ContextUtil.lookupParam("publishedId");

	deleteSubmission(totalScores, gradingIdParam, publishedAssessmentId, new GradingService(), eventTrackingService);
  }

  /**
   * the single-submission soft delete, shared with the batch
   * Delete Selected action: flips the grading row's status, prunes the
   * page beans, renotifies the gradebook and posts the delete event.
   */
  static void deleteSubmission(TotalScoresBean totalScores, String gradingIdParam, String publishedAssessmentId,
      GradingService gradingService, EventTrackingService eventTrackingService) {

    String deletedStudentId = null;
    Long gradingId = Long.valueOf(gradingIdParam);

    AssessmentGradingData ag = (AssessmentGradingData) gradingService.load(gradingIdParam);
    gradingService.removeAssessmentGradingData(ag); // This will just flip the STATUS column, no hard deletes

    Collection agentList = totalScores.getAgents();
    for(Iterator i = agentList.iterator(); i.hasNext();) {
    	AgentResults a = (AgentResults)i.next();
    	if (a.getAssessmentGradingId().equals(gradingId)) {
    		deletedStudentId = a.getAgentId();
    		i.remove();
    	}
    }

    // drop the deleted row from the page's grading list without
    // clearing it (the old code cleared the whole list, which corrupted the
    // gradebook notification for every later student when this helper is
    // looped for a bulk delete). notifyDeleteToGradebook re-queries the DB for
    // the student's remaining submissions and only uses the record we pass to
    // resolve the assessment id and, when the student has no submissions left,
    // to null the gradebook — so pass exactly this student's just-deleted row
    // with its score cleared. Behavior is identical for a single-row delete.
    List gradingList = totalScores.getAssessmentGradingList();
    if (gradingList != null) {
        for (Iterator i = gradingList.iterator(); i.hasNext();) {
            if (((AssessmentGradingData) i.next()).getAssessmentGradingId().equals(gradingId)) {
                i.remove();
            }
        }
        totalScores.setAssessmentGradingList(gradingList);
    }
    totalScores.setAgents(agentList);

    ag.setFinalScore(null);
    List notifyList = new ArrayList();
    notifyList.add(ag);
    gradingService.notifyDeleteToGradebook(notifyList, totalScores.getPublishedAssessment(), deletedStudentId);

    // Now post an event so support teams know who deleted the submission
    eventTrackingService.post(
    		eventTrackingService.newEvent(
    				SamigoConstants.EVENT_SUBMISSION_DELETE, 
    				"siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId + ", agentId=" + deletedStudentId + ", assessmentGradingID=" + gradingId,
    				AgentFacade.getCurrentSiteId(), 
    				true, 
    				NotificationService.NOTI_NONE
    		)
    );
  }
}
