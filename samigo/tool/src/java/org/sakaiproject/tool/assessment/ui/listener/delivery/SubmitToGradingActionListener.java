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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
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
      log.debug("SubmitToGradingActionListener.processAction() ");

      // get managed bean
      DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");            

      if ((cu.lookupParam("showfeedbacknow") != null 
           && "true".equals(cu.lookupParam("showfeedbacknow")) 
           || delivery.getActionMode()==delivery.PREVIEW_ASSESSMENT))
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

      AssessmentGradingData adata = submitToGradingService(publishedAssessment, delivery);

      // set url & confirmation after saving the record for grade
      if (adata !=null && delivery.getForGrade())
        setConfirmation(adata, publishedAssessment, delivery);

      if (isForGrade(adata) && !isUnlimited(publishedAssessment))
      {
        delivery.setSubmissionsRemaining(
            delivery.getSubmissionsRemaining() - 1);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isForGrade(AssessmentGradingData aData)
  {
    if (aData !=null) 
      return (Boolean.TRUE).equals(aData.getForGrade());
    else
      return false;
  }

  private boolean isUnlimited(PublishedAssessmentFacade publishedAssessment)
  {
    return (Boolean.TRUE).equals(publishedAssessment.getAssessmentAccessControl().getUnlimitedSubmissions());
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
    if (publishedAssessment.getAssessmentAccessControl()!=null){
      setFinalPage(publishedAssessment, delivery);
      setSubmissionMessage(publishedAssessment, delivery);
    }
    setConfirmationId(adata, publishedAssessment, delivery);
  }

  /**
   * Set confirmationId which is AssessmentGradingId-TimeStamp.
   * @param adata
   * @param publishedAssessment
   * @param delivery
   */
  private void setConfirmationId(AssessmentGradingData adata,
                                 PublishedAssessmentFacade publishedAssessment,
                                 DeliveryBean delivery)
  {
    delivery.setConfirmation(adata.getAssessmentGradingId()+"-"+
        publishedAssessment.getPublishedAssessmentId()+"-"+
        adata.getAgentId()+"-"+adata.getSubmittedDate().toString());
  }

  /**
   * Set the submission message.
   * @param publishedAssessment
   * @param delivery
   */
  private void setSubmissionMessage(PublishedAssessmentFacade
                                    publishedAssessment, DeliveryBean delivery)
  {
    String submissionMessage = publishedAssessment.getAssessmentAccessControl().
        getSubmissionMessage();
    if (submissionMessage != null)
      delivery.setSubmissionMessage(submissionMessage);
  }

  /**
   * Set finalPage url in delivery bean.
   * @param publishedAssessment
   * @param delivery
   */
  private void setFinalPage(PublishedAssessmentFacade publishedAssessment,
                            DeliveryBean delivery)
  {
    String url = publishedAssessment.getAssessmentAccessControl().
        getFinalPageUrl();
    if (url != null)
        url = url.trim();
    delivery.setUrl(url);
  }

  /**
   * Invoke submission and return the grading data
   * @param publishedAssessment
   * @param delivery
   * @return
   */
  private synchronized AssessmentGradingData submitToGradingService(
    PublishedAssessmentFacade publishedAssessment,
    DeliveryBean delivery)
  {
    log.debug("****1a. inside submitToGradingService ");
    String submissionId = "";
    HashSet itemData = new HashSet();
    // daisyf decoding: get page contents contains SectionContentsBean, a wrapper for SectionDataIfc
    Iterator iter = delivery.getPageContents().getPartsContents().iterator();
    log.debug("****1b. inside submitToGradingService, iter= "+iter);
    while (iter.hasNext())
    {
      // daisyf decoding:
      // looks like it is going through questions in each part.
      // for each question, it look up all the answer ever saved
      SectionContentsBean part = (SectionContentsBean) iter.next();
      log.debug("****1c. inside submitToGradingService, part "+part);
      Iterator iter2 = part.getItemContents().iterator();
      while (iter2.hasNext())
      {
        ItemContentsBean item = (ItemContentsBean) iter2.next();
        log.debug("****1d. inside submitToGradingService, item= "+item);
        ArrayList grading = item.getItemGradingDataArray();
        if (grading.isEmpty())
        {
          log.info("No item grading data.");
        }
        else
        { // found at least one valid existing answer, i.e. itemGradingData
          // then it loops through them and gather up the one that has valid
          // answers, i.e. not null
          Iterator iter3 = grading.iterator();
          while (iter3.hasNext())
          {
            ItemGradingData data = (ItemGradingData) iter3.next();
            // for FIB and MC/TF/Matching question, don't add the data if no item is selected
            log.debug("****1e. inside submitToGradingService, olddata= "+data);
            if (data.getPublishedAnswer() != null ||
                data.getAnswerText() != null)
            itemData.add(data);
          }
        }
      }
    }

    AssessmentGradingData adata = null;
    if (delivery.getAssessmentGrading() != null)
      adata = delivery.getAssessmentGrading();
    log.debug("****** 1f. submitToGradingService, adata= "+adata);

    GradingService service = new GradingService();
    if (adata == null)
    {
      adata = makeNewAssessmentGrading(publishedAssessment, delivery, itemData);
      delivery.setAssessmentGrading(adata);
      log.debug("****** 1g. submitToGradingService, itemData.size()= "+itemData.size());
    }
    else
    {
      log.debug("****** 1h. submitToGradingService, old adata= "+adata);
      ArrayList adds = new ArrayList();
      ArrayList removes = new ArrayList();
      // itemData contains all valid saved answers, itemGradingData
      integrateItemGradingDatas(itemData, adata, adds, removes);

      // Add and remove separately so we don't get concurrent modification
      if (adata.getItemGradingSet()!=null){
        adata.getItemGradingSet().removeAll(removes);
        adata.getItemGradingSet().addAll(adds);
      }
      adata.setForGrade(new Boolean(delivery.getForGrade()));
    }

    service.storeGrades(adata);

    delivery.setSubmissionId(submissionId);
    delivery.setSubmissionTicket(submissionId);// is this the same thing? hmmmm
    delivery.setSubmissionDate(new Date());
    delivery.setSubmitted(true);

    return adata;
  }

  /**
   * Make a new AssessmentGradingData object for delivery
   * @param publishedAssessment the PublishedAssessmentFacade
   * @param delivery the DeliveryBean
   * @param itemData the item data
   * @return
   */
  private AssessmentGradingData makeNewAssessmentGrading(
    PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery,
    HashSet itemData)
  {
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");            
    AssessmentGradingData adata = new AssessmentGradingData();
    adata.setAgentId(person.getId());
    adata.setForGrade(new Boolean(delivery.getForGrade()));
    adata.setItemGradingSet(itemData);
    adata.setPublishedAssessment(publishedAssessment.getData());
    return adata;
  }

  /**
   * figure out what new item grading data needs to be added, removed
   * @param itemData
   * @param adata
   * @param adds the data that needs to be added
   * @param removes the data that needs to be removed
   */

  private void integrateItemGradingDatas(HashSet itemData,
                                         AssessmentGradingData adata,
                                         ArrayList adds, ArrayList removes)
  {
    // daisyf's question: why not just persist the currently submitted answer by 
    // updating the existing one?
    // why do you need to "replace" it by deleting and adding it again?
    Iterator i1 = itemData.iterator();
    while (i1.hasNext())
    {
      ItemGradingData data = (ItemGradingData) i1.next();
      if (!adata.getItemGradingSet().contains(data)) // wouldn't this always true? wouldn't they always have diff address even if the two objects contains the same properties value?
      {
        log.debug("****** cc. data is new");
        Iterator iter2 = adata.getItemGradingSet().iterator();
        boolean added = false;
        if (data.getItemGradingId() != null)
        {
          log.debug("****** ccc. data not saved to DB yet");
          while (iter2.hasNext())
          {
            ItemGradingData olddata = (ItemGradingData) iter2.next();
            if (data.getItemGradingId().equals(olddata.getItemGradingId()))
            {
              log.debug("****** d. integrate"+data.getAssessmentGrading()+":"+data.getItemGradingId()+":"+data.getAnswerText());
              log.debug("****** e. integrate"+olddata.getAssessmentGrading()+":"+olddata.getItemGradingId()+":"+olddata.getAnswerText());
              data.setAssessmentGrading(adata);
              removes.add(olddata);
              adds.add(data);
              added = true;
            }
          }
        } //end if  (data.getItemGradingId() != null)
        if (!added) // add last one?
          adds.add(data);
      } //end if (!adata.getItemGradingSet().contains(data))
    }
  }

}
