/**********************************************************************************
* $HeadURL$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
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
    log.info("Calling saveQuestionScores.");
    if (!saveQuestionScores(bean))
    {
      throw new RuntimeException("failed to call saveQuestionScores.");
    }

  }

  /**
   * Persist the results from the ActionForm in the question page.
   * @param bean QuestionScoresBean bean
   * @return true if successful
   */
  public boolean saveQuestionScores(QuestionScoresBean bean)
  {
    try
    {
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
        AgentResults ar = (AgentResults) iter.next();

        // Get the itemgradingdata list for this result
        ArrayList datas = (ArrayList) bean.getScoresByItem().get
          (ar.getAssessmentGradingId() + ":" + itemId);
        if (datas == null)
          datas = new ArrayList();
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          Object obj = iter2.next();
          System.out.println("Data = " + obj);
          ItemGradingData data = (ItemGradingData) obj;
          data.setAutoScore(new Float
           (new Float(ar.getTotalAutoScore()).floatValue() /
            (float) datas.size()));
          data.setComments(ar.getComments());
          items.add(data);
        }
      }

      GradingService delegate = new GradingService();
      delegate.saveItemScores(items);

      log.info("Saved question scores.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
