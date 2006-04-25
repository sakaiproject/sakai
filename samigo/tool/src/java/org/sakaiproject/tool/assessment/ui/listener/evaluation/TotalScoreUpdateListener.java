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
import java.util.HashMap;


import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
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

  private HashMap prepareAssessmentGradingHash(ArrayList assessmentGradingList){
    HashMap map = new HashMap();
    for (int i=0; i<assessmentGradingList.size(); i++){
      AssessmentGradingIfc a = (AssessmentGradingIfc)assessmentGradingList.get(i);
      map.put(a.getAssessmentGradingId(), a);
    }
    return map;
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
      ArrayList assessmentGradingList = bean.getAssessmentGradingList();
      HashMap map = prepareAssessmentGradingHash(assessmentGradingList);
      Collection agents = bean.getAgents();
      Iterator iter = agents.iterator();
      ArrayList grading = new ArrayList();
      while (iter.hasNext())
      {
        AgentResults agentResults = (AgentResults) iter.next();
        float newScore = new Float(agentResults.getTotalAutoScore()).floatValue() +
                     new Float(agentResults.getTotalOverrideScore()).floatValue();

        boolean update = needUpdate(agentResults, map);
        if (update){
          if (!agentResults.getAssessmentGradingId().equals(new Long(-1)) ) {
	    // these are students who have submitted for grades.
            // Add up new score
            agentResults.setFinalScore(newScore+"");
            AssessmentGradingData data = new AssessmentGradingData();
            BeanUtils.copyProperties(data, agentResults);
    	    data.setPublishedAssessmentId(bean.getPublishedAssessment().getPublishedAssessmentId());
            data.setTotalAutoScore(new Float(agentResults.getTotalAutoScore()));
            data.setTotalOverrideScore(new Float(agentResults.getTotalOverrideScore()));
            data.setFinalScore(new Float(agentResults.getFinalScore()));
            data.setIsLate(agentResults.getIsLate());
            data.setComments(agentResults.getComments());
            grading.add(data);
          }
          else {
            // these are students who have not submitted for grades and instructor made adjustment to their scores
            // Add up new score
            agentResults.setFinalScore(newScore+"");
	    AssessmentGradingData data = new AssessmentGradingData();
            BeanUtils.copyProperties(data, agentResults);

            data.setAgentId(agentResults.getIdString());
  	    data.setForGrade(new Boolean(true));
	    data.setStatus(new Integer(1));
            data.setIsLate(new Boolean(false));
   	    data.setItemGradingSet(new HashSet());
    	    data.setPublishedAssessmentId(bean.getPublishedAssessment().getPublishedAssessmentId());
	    // tell hibernate this is a new record
    	    data.setAssessmentGradingId(new Long(0));
            data.setSubmittedDate(null);
            data.setTotalAutoScore(new Float(agentResults.getTotalAutoScore()));
            data.setTotalOverrideScore(new Float(agentResults.getTotalOverrideScore()));
            data.setFinalScore(new Float(agentResults.getFinalScore()));
            data.setComments(agentResults.getComments());
            // note that I am not sure if we should set this people as late or what?
            grading.add(data);
          }
	}
      }

      GradingService delegate = new GradingService();

      delegate.saveTotalScores(grading, bean.getPublishedAssessment());
      log.info("Saved total scores.");
      } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));
       // scores are saved in Samigo, still return true, but display error to user.
       return true;
      }


    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private boolean needUpdate(AgentResults agentResults, HashMap map){
    boolean update = true;
    float newScore = new Float(agentResults.getTotalAutoScore()).floatValue() +
                     new Float(agentResults.getTotalOverrideScore()).floatValue();
    Boolean newIsLate = agentResults.getIsLate(); // if the duedate were postpond, we need to adjust this
    // we will check if there is change of grade. if so, add up new score
    // else skip
    AssessmentGradingIfc old = (AssessmentGradingIfc)map.get(agentResults.getAssessmentGradingId());
    if (old != null){
      float oldScore = old.getFinalScore().floatValue();
      Boolean oldIsLate=old.getIsLate();
      if (oldScore==newScore && oldIsLate.equals(newIsLate)) {
        update = false;
      }
    }
    else{ //students hasn't submitted the assessment  
      boolean noOverrideScore =  false;
      boolean noComment =  false;
      if ((new Float(0)).equals(new Float(agentResults.getTotalOverrideScore().trim())))
        noOverrideScore = true;
      if ("".equals(agentResults.getComments().trim()))
        noComment = true;

      if (noOverrideScore && noComment) 
	update = false;
    }
    return update;
  }

}
