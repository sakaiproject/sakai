/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>
 * This handles the updating of the Question Score page.
 *  </p>
 * <p>Description: Action Listener Evaluation Updating Question Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class QuestionScoreUpdateListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(QuestionScoreUpdateListener.class);
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
    log.debug("Question Score Update LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean) cu.lookupBean("questionScores");
    TotalScoresBean tbean = (TotalScoresBean) cu.lookupBean("totalScores");
    log.debug("Calling saveQuestionScores.");
    tbean.setAssessmentGradingHash(tbean.getPublishedAssessment().getPublishedAssessmentId());
    try{
      if (!saveQuestionScores(bean, tbean))
      {
        throw new RuntimeException("failed to call saveQuestionScores.");
      }
    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));

    }

  }

  /**
   * Persist the results from the ActionForm in the question page.
   * @param bean QuestionScoresBean bean
   * @return true if successful
   */
  public boolean saveQuestionScores(QuestionScoresBean bean, TotalScoresBean tbean)
  {
    try
    {
      GradingService delegate = new GradingService();
      String publishedId = cu.lookupParam("publishedId");
      String itemId = cu.lookupParam("itemId");
      String which = cu.lookupParam("allSubmissions");
      if (which == null)
        which = "false";
      Collection agents = bean.getAgents();
      ArrayList items = new ArrayList();
      Iterator iter = agents.iterator();
      while (iter.hasNext())
      {
        // each agent has a list of modified itemGrading
        AgentResults ar = (AgentResults) iter.next();
        // Get the itemgradingdata list for this result
        ArrayList datas = (ArrayList) bean.getScoresByItem().get
          (ar.getAssessmentGradingId() + ":" + itemId);
        if (datas == null)
          datas = new ArrayList();
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext()){
          Object obj = iter2.next();
          //log.info("Data = " + obj);
          ItemGradingData data = (ItemGradingData) obj;

          // check if there is differnce in score, if so, update. Otherwise, do nothing
          float newAutoScore = (new Float(ar.getTotalAutoScore())).floatValue() / (float) datas.size();
          String newComments = ar.getComments();
          if (newComments!=null) newComments.trim();

          float oldAutoScore = 0;
          if (data.getAutoScore() !=null)
            oldAutoScore=data.getAutoScore().floatValue();
          String oldComments = data.getComments();

          if (oldComments!=null) oldComments.trim();
          if (newAutoScore != oldAutoScore || !newComments.equals(oldComments)){
	    data.setAutoScore(new Float(newAutoScore));
            data.setComments(ar.getComments());
            delegate.updateItemScore(data, newAutoScore-oldAutoScore, tbean.getPublishedAssessment());
	  }
        }
      }

    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
