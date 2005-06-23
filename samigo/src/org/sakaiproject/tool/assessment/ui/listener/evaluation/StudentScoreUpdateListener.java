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
import java.util.Date;
import java.util.Iterator;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>
 * This handles the updating of the Student Score page.
 *  </p>
 * <p>Description: Action Listener Evaluation Updating Student Score page</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Rachel Gollub
 * @version $Id$
 */

public class StudentScoreUpdateListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(StudentScoreUpdateListener.class);
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
      //System.out.println("Student Score Update LISTENER.");
    StudentScoresBean bean = (StudentScoresBean) cu.lookupBean("studentScores");
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    log.info("Calling saveStudentScores.");
    if (!saveStudentScores(bean, delivery))
    {
      throw new RuntimeException("failed to call saveStudentScores.");
    }

  }

  /**
   * Persist the results from the ActionForm in the student page.
   * @param bean StudentScoresBean bean
   * @return true if successful
   */
  public boolean saveStudentScores(StudentScoresBean bean,
                                   DeliveryBean delivery)
  {
    ArrayList list = new ArrayList();
    AssessmentGradingData adata = null;
    try
    {
      ArrayList parts = delivery.getPageContents().getPartsContents();
      Iterator iter = parts.iterator();
      while (iter.hasNext())
      {
        ArrayList items = ((SectionContentsBean) iter.next()).getItemContents();
        Iterator iter2 = items.iterator();
        while (iter2.hasNext())
        {
          ItemContentsBean question = (ItemContentsBean) iter2.next();
          ArrayList gradingarray = question.getItemGradingDataArray();
          //System.out.println("Gradingarray length = " + gradingarray.size());
          // Create a new one if we need it.
          if (gradingarray.isEmpty() && (question.getPoints() > 0  ||
              (question.getGradingComment() != null &&
               !question.getGradingComment().trim().equals("")) ))
          {
            question.setReview(false); // This creates an itemgradingdata
            gradingarray = question.getItemGradingDataArray();
          }
          //System.out.println("Gradingarray length2 = " + gradingarray.size());
          Iterator iter3 = gradingarray.iterator();
          while (iter3.hasNext())
          {
            ItemGradingData data = (ItemGradingData) iter3.next();
            if (data.getAgentId() == null)
            { // it's a new data, fill it in
              data.setSubmittedDate(new Date());
              data.setAgentId(bean.getStudentId());
            }
            data.setAutoScore(new Float
              (new Float(question.getPoints()).floatValue()
               / (float) gradingarray.size()));
            data.setComments(question.getGradingComment());
            //System.out.println("set points = " + question.getPoints() + ", comments to " + question.getGradingComment());
            list.add(data);

            if (adata == null)
              adata = (AssessmentGradingData) data.getAssessmentGrading();
          }
        }
      }

      if (adata == null)
        return true; // Nothing to save.

      adata.setComments(bean.getComments());
      //System.out.println("Got total comments: " + adata.getComments());

      // Some of the itemgradingdatas may be new.
      iter = list.iterator();
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        data.setAssessmentGrading(adata);
      }
      GradingService delegate = new GradingService();
      delegate.saveItemScores(list);

      log.info("Saved student scores.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

}
