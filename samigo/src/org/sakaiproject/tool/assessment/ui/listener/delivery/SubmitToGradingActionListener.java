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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class SubmitToGradingActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(SubmitToGradingActionListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    try {
      log.info("ReviewActionListener.processAction() ");

      // get managed bean
      DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");

      if ((cu.lookupParam("showfeedbacknow") != null &&
	   "true".equals(cu.lookupParam("showfeedbacknow")) || "true".equals(delivery.getPreviewAssessment())))
        delivery.setForGrade(false);

      // get service
      PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();

      // get assessment
      PublishedAssessmentFacade publishedAssessment = null;
      if (delivery.getPublishedAssessment() != null)
        publishedAssessment = delivery.getPublishedAssessment();
      else
        publishedAssessment =
          publishedAssessmentService.getPublishedAssessment(delivery.getAssessmentId());

        AssessmentGradingData adata = submitToGradingService( publishedAssessment,delivery);

      // set url & confirmation after saving the record for grade
      if (adata !=null && delivery.getForGrade())
        setConfirmation(adata, publishedAssessment, delivery);

      // decrement submission remaining by 1 if it is submitting for grade and there is a
      // limit on submission
      if ((Boolean.TRUE).equals(adata.getForGrade()) &&
           !(Boolean.TRUE).equals(publishedAssessment.getAssessmentAccessControl().getUnlimitedSubmissions()))
       delivery.setSubmissionsRemaining(delivery.getSubmissionsRemaining() - 1);


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method set the url & confirmation string for submitted.jsp.
   * The confirmation string = assessmentGradingId-publishedAssessmentId-agentId-submitteddate
   * @param adata
   * @param publishedAssessment
   * @param delivery
   */
  private void setConfirmation(AssessmentGradingData adata,
                               PublishedAssessmentFacade publishedAssessment,
                               DeliveryBean delivery){
    // 1a. set finalPage url in delivery bean
    if (publishedAssessment.getAssessmentAccessControl()!=null){
      String url = publishedAssessment.getAssessmentAccessControl().
          getFinalPageUrl();
      if (url != null)
          url = url.trim();
      delivery.setUrl(url);
      // 1b. set submission message
      String submissionMessage = publishedAssessment.getAssessmentAccessControl().
          getSubmissionMessage();
      if (submissionMessage != null)
        delivery.setSubmissionMessage(submissionMessage);
    }
    // 2. set confirmationId which is AssessmentGradingId-TimeStamp
    delivery.setConfirmation(adata.getAssessmentGradingId()+"-"+
        publishedAssessment.getPublishedAssessmentId()+"-"+
        adata.getAgentId()+"-"+adata.getSubmittedDate().toString());
  }

  /**
   * Invoke submission and
   * @param publishedAssessment
   * @param delivery
   */
  private synchronized AssessmentGradingData submitToGradingService(
    PublishedAssessmentFacade publishedAssessment,
    DeliveryBean delivery)
  {
    String submissionId = "";
    HashSet itemData = new HashSet();
    AssessmentGradingData adata = delivery.getAssessmentGrading();
    Iterator iter = delivery.getPageContents().getPartsContents().iterator();
    while (iter.hasNext())
    {
      SectionContentsBean part = (SectionContentsBean) iter.next();
      Iterator iter2 = part.getItemContents().iterator();
      while (iter2.hasNext())
      {
        ItemContentsBean item = (ItemContentsBean) iter2.next();
        ArrayList grading = item.getItemGradingDataArray();
        if (grading.isEmpty())
        {
          log.info("No item grading data.");
        }
        else
        {
          Iterator iter3 = grading.iterator();
          while (iter3.hasNext())
          {
            ItemGradingData data = (ItemGradingData) iter3.next();

            // Don't add the data if no item is selected
            if (data.getPublishedAnswer() != null ||
                data.getAnswerText() != null)
            itemData.add(data);

            // If there's an existing assessmentgradingdata, use it
            if (adata == null && data.getAssessmentGrading() != null)
              adata = (AssessmentGradingData) data.getAssessmentGrading();
          }
        }
      }
    }

    if (adata == null && delivery.getAssessmentGrading() != null)
      adata = delivery.getAssessmentGrading();

    GradingService service = new GradingService();
    if (adata == null)
    {
      adata = new AssessmentGradingData();
      adata.setAgentId(AgentFacade.getAgentString());
      adata.setForGrade(new Boolean(delivery.getForGrade()));
      adata.setItemGradingSet(itemData);
      adata.setPublishedAssessment(publishedAssessment.getData());
    }
    else
    {
      // The assessment grading set is out of date, so we need to
      // integrate all the new itemgradingdatas into it.
      ArrayList adds = new ArrayList();
      ArrayList removes = new ArrayList();
      Iterator i1 = itemData.iterator();

      while (i1.hasNext())
      {
        ItemGradingData data = (ItemGradingData) i1.next();
        if (!adata.getItemGradingSet().contains(data))
        {
          Iterator iter2 = adata.getItemGradingSet().iterator();
          boolean added = false;
          if (data.getItemGradingId() != null)
          {
            while (iter2.hasNext())
            {
              ItemGradingData olddata = (ItemGradingData) iter2.next();
              if (data.getItemGradingId().equals(olddata.getItemGradingId()))
              {
                data.setAssessmentGrading(adata);
                removes.add(olddata);
                adds.add(data);
                added = true;
              }
            }
          }
          if (!added)
            adds.add(data);
        }
      }

      // Add and remove separately so we don't get concurrent modification
      adata.getItemGradingSet().removeAll(removes);
      adata.getItemGradingSet().addAll(adds);
      adata.setForGrade(new Boolean(delivery.getForGrade()));
    }

    log.info("Before time elapsed " + adata.getTimeElapsed());
    // Set time elapsed if this is a timed test
    if (adata.getTimeElapsed() == null)
    {
      adata.setTimeElapsed(new Integer(0));
    }
    // Don't divide by 10 again if we were at the TOC
    if (delivery.getTimeElapse() != null &&
        !delivery.getTimeElapse().equals("0") && // Don't save resets.
        !delivery.getTimeElapse().equals(adata.getTimeElapsed().toString()))
    {
      adata.setTimeElapsed(new Integer(
        new Integer(delivery.getTimeElapse()).intValue() / 10));
    }

    log.info("Set time elapsed " + adata.getTimeElapsed());
    // Store the date you started this attempt.
    adata.setAttemptDate(delivery.getBeginTime());

    service.storeGrades(adata);


    // Please don't put this back in.  It shouldn't be here -- the
    // submissions remaining are now handled by the main page.  This
    // is just causing assessments to be impossible to submit for
    // no reason. If you're going to do this, you need to make sure
    // you don't do this for every save, only for a submit (where
    // forGrade = true), and that you don't throw an exception
    // *after* someone's submitted, for any reason -- if they get
    // to the point where they can submit, it's too late to throw
    // an exception and erase all their work. -- rmg
    //processSubmissionsRemaining(delivery);
    delivery.setSubmissionId(submissionId);
    delivery.setSubmissionTicket(submissionId);// is this the same thing? hmmmm
    delivery.setSubmissionDate(new Date());
    delivery.setSubmitted(true);

    return adata;
  }

  /**
   * wraps a check on validity and if OK updates DeliveryBean decremented
   * @param delivery DeliveryBean
   */
  private void processSubmissionsRemaining(DeliveryBean delivery)
  {
    int subRemain = delivery.getSubmissionsRemaining();
    if (subRemain<1){
    throw new IllegalArgumentException("Cannot submit when number of submissions is " +
      subRemain + ".");
    }
    subRemain--;
    delivery.setSubmissionsRemaining(subRemain);
  }
}
