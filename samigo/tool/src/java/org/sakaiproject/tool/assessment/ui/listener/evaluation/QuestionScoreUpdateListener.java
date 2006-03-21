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
    log.info("Question Score Update LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean) cu.lookupBean("questionScores");
    TotalScoresBean tbean = (TotalScoresBean) cu.lookupBean("totalScores");
    log.info("Calling saveQuestionScores.");
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
	System.out.println("****QuestionScoreUpadte, agent="+ar);
	System.out.println("****QuestionScoreUpadte, itemId="+itemId);
        // Get the itemgradingdata list for this result
        ArrayList datas = (ArrayList) bean.getScoresByItem().get
          (ar.getAssessmentGradingId() + ":" + itemId);
        if (datas == null)
          datas = new ArrayList();
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext()){
          Object obj = iter2.next();
          log.info("Data = " + obj);
          ItemGradingData data = (ItemGradingData) obj;

          // check if there is differnce in score, if so, update. Otherwise, do nothing
          float newAutoScore = (new Float(ar.getTotalAutoScore())).floatValue() / (float) datas.size();
          String newComments = ar.getComments();
          if (newComments!=null) newComments.trim();

          float oldAutoScore = data.getAutoScore().floatValue();
          String oldComments = data.getComments();

          if (oldComments!=null) oldComments.trim();
          if (newAutoScore != oldAutoScore || !newComments.equals(oldComments)){
	    data.setAutoScore(new Float(newAutoScore));
            data.setComments(ar.getComments());
            delegate.updateItemScore(data, newAutoScore-oldAutoScore, tbean.getPublishedAssessment());
	  }
        }
      }

      log.info("Saved question scores.");
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
