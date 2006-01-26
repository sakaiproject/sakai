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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.util.HashSet;


import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

/**
 * <p>
 * This handles the updating of the Total Score page.
 *  </p>
 * <p>Description: Action Listener Evaluation Updating Total Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TotalScoreUpdateListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(TotalScoreUpdateListener.class);
  private static EvaluationListenerUtil util;
  private static BeanSort bs;
  private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.info("Total Score Update LISTENER.");
    TotalScoresBean bean = (TotalScoresBean) cu.lookupBean("totalScores");
    log.info("Calling saveTotalScores.");
    if (!saveTotalScores(bean))
    {
      throw new RuntimeException("failed to call saveTotalScores.");
    }

  }

  /**
   * Persist the results from the ActionForm in the total page.
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param bean TotalScoresBean bean
   * @return true if successful
   */
  public boolean saveTotalScores(TotalScoresBean bean)
  {
    try
    {
      Collection agents = bean.getAgents();
      Iterator iter = agents.iterator();
      ArrayList grading = new ArrayList();
      while (iter.hasNext())
      {
        AgentResults agentResults = (AgentResults) iter.next();


	if (!agentResults.getAssessmentGradingId().equals(new Long(-1)) ) {
	  // these are students who have submitted for grades.
          // Add up new score
          agentResults.setFinalScore(new Float(
            new Float(agentResults.getTotalAutoScore()).floatValue() +
            new Float(agentResults.getTotalOverrideScore()).floatValue()).toString());

          AssessmentGradingData data = new AssessmentGradingData();
          BeanUtils.copyProperties(data, agentResults);

          data.setTotalAutoScore(new Float(agentResults.getTotalAutoScore()));
          data.setTotalOverrideScore(new Float(agentResults.getTotalOverrideScore()));
          data.setFinalScore(new Float(agentResults.getFinalScore()));
          data.setComments(agentResults.getComments());
          grading.add(data);
        }

	else {

	  boolean noOverrideScore =  false;
          boolean noComment =  false;
	  if ((new Float(0)).equals(new Float(agentResults.getTotalOverrideScore().trim())))
	    noOverrideScore = true;
	  if ("".equals(agentResults.getComments().trim()))
	    noComment = true;


          if (!(noOverrideScore && noComment )) {
            // these are students who have not submitted for grades and instructor made adjustment to their scores

	    // Add up new score
	    agentResults.setFinalScore(new Float(
              new Float(agentResults.getTotalAutoScore()).floatValue() +
              new Float(agentResults.getTotalOverrideScore()).floatValue()).toString());


	    AssessmentGradingData data = new AssessmentGradingData();
            BeanUtils.copyProperties(data, agentResults);

	    String publishedId = bean.getPublishedId();
	    PublishedAssessmentService pubassessmentService = new PublishedAssessmentService();
    	    PublishedAssessmentFacade pubassessment = pubassessmentService.getPublishedAssessment(publishedId);

            data.setAgentId(agentResults.getIdString());
  	    data.setForGrade(new Boolean(true));
   	    data.setItemGradingSet(new HashSet());
    	    data.setPublishedAssessment(pubassessment.getData());
	    // tell hibernate this is a new record
    	    data.setAssessmentGradingId(new Long(0));

	    data.setSubmittedDate(new Date());
            data.setTotalAutoScore(new Float(agentResults.getTotalAutoScore()));
            data.setTotalOverrideScore(new Float(agentResults.getTotalOverrideScore()));
            data.setFinalScore(new Float(agentResults.getFinalScore()));
            data.setComments(agentResults.getComments());
            grading.add(data);
          }
        }


      }

      GradingService delegate = new GradingService();
      delegate.saveTotalScores(grading);
      log.info("Saved total scores.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
