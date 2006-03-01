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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
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
    HashSet itemGradingHash = new HashSet();
    // daisyf decoding: get page contents contains SectionContentsBean, a wrapper for SectionDataIfc
    Iterator iter = delivery.getPageContents().getPartsContents().iterator();
    log.debug("****1b. inside submitToGradingService, iter= "+iter);
    HashSet adds = new HashSet();
    HashSet removes = new HashSet();

    // we go through all the answer collected from JSF form per each publsihedItem and
    // work out which answer is an new addition and in cases like MC/MCMR/Survey, we will
    // discard any existing one and just save teh new one. For other question type, we
    // simply modify the publishedText or publishedAnswer of teh existing ones.
    while (iter.hasNext()){
      SectionContentsBean part = (SectionContentsBean) iter.next();
      log.debug("****1c. inside submitToGradingService, part "+part);
      Iterator iter2 = part.getItemContents().iterator();
      while (iter2.hasNext()){ // go through each item from form
        ItemContentsBean item = (ItemContentsBean) iter2.next();
        prepareItemGradingPerItem(item, adds, removes);
      }
    }
    AssessmentGradingData adata = persistAssessmentGrading(delivery, itemGradingHash, 
                                  publishedAssessment, adds, removes);
    delivery.setSubmissionId(submissionId);
    delivery.setSubmissionTicket(submissionId);// is this the same thing? hmmmm
    delivery.setSubmissionDate(new Date());
    delivery.setSubmitted(true);
    return adata;
  }

  private AssessmentGradingData persistAssessmentGrading(DeliveryBean delivery, HashSet itemGradingHash,
                                                         PublishedAssessmentFacade publishedAssessment,
                                                         HashSet adds, HashSet removes){
    AssessmentGradingData adata = null;
    if (delivery.getAssessmentGrading() != null){
      adata = delivery.getAssessmentGrading();
  
      if (delivery.getAssessmentGrading().getItemGradingSet() != null)
        System.out.println("SubmitToGradingActionListener before = "+adata.getItemGradingSet().size());
    }

    GradingService service = new GradingService();
    if (adata == null) {
      adata = makeNewAssessmentGrading(publishedAssessment, delivery, itemGradingHash);
      delivery.setAssessmentGrading(adata);
    }
    else {
      // 1. add all the new itemgrading for MC/Survey and discard any
      // itemgrading for MC/Survey
      // 2. add any modified SAQ/TF/FIB/Matching/MCMR

      Set itemGradingSet = adata.getItemGradingSet();
      if (itemGradingSet!=null){
        itemGradingSet.removeAll(removes);
        service.deleteAll(removes);
        // refresh itemGradingSet after removal 
        itemGradingSet = service.getItemGradingSet(adata.getAssessmentGradingId().toString());

        Iterator iter = adds.iterator();
        while (iter.hasNext()){
          ((ItemGradingIfc)iter.next()).setAssessmentGrading(adata);
	}
        adata.setItemGradingSet(updateItemGradingSet(itemGradingSet, adds));
      }
    }
    System.out.println("SubmitToGradingActionListener after = "+adata.getItemGradingSet().size());
    adata.setForGrade(new Boolean(delivery.getForGrade()));
    service.storeGrades(adata, publishedAssessment);
    return adata;
  }

  private Set updateItemGradingSet(Set oldItemGradingSet, Set newItemGradingSet){
    HashSet h = new HashSet();
    Iterator iter = oldItemGradingSet.iterator();
    HashMap map = new HashMap();
    while (iter.hasNext()){
      ItemGradingIfc item = (ItemGradingIfc) iter.next();
      map.put(item.getItemGradingId(), item);
    }

    Iterator iter1 = newItemGradingSet.iterator();
    while (iter1.hasNext()){
      ItemGradingIfc newItem = (ItemGradingIfc) iter1.next();
      ItemGradingIfc oldItem = (ItemGradingIfc)map.get(newItem.getItemGradingId());
      if (oldItem != null){ // itemGrading exist, replace with new in this case
        oldItem.setPublishedAnswerId(newItem.getPublishedAnswerId());
        oldItem.setRationale(newItem.getRationale());
        oldItem.setAnswerText(newItem.getAnswerText());
        oldItem.setSubmittedDate(new Date());
        oldItem.setAutoScore(newItem.getAutoScore());
        oldItem.setOverrideScore(newItem.getOverrideScore());
      }
      else {  // itemGrading from new set doesn't exist, add to set in this case
        oldItemGradingSet.add(newItem);
      }
    }
    return oldItemGradingSet;
  }

  /**
   * Make a new AssessmentGradingData object for delivery
   * @param publishedAssessment the PublishedAssessmentFacade
   * @param delivery the DeliveryBean
   * @param itemGradingHash the item data
   * @return
   */
  private AssessmentGradingData makeNewAssessmentGrading(
    PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery,
    HashSet itemGradingHash)
  {
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");            
    AssessmentGradingData adata = new AssessmentGradingData();
    adata.setAgentId(person.getId());
    adata.setForGrade(new Boolean(delivery.getForGrade()));
    adata.setItemGradingSet(itemGradingHash);
    adata.setPublishedAssessment(publishedAssessment.getData());
    return adata;
  }

  public void prepareItemGradingPerItem(ItemContentsBean item, HashSet adds, HashSet removes){
    ArrayList grading = item.getItemGradingDataArray();
    int typeId = item.getItemData().getTypeId().intValue();

    // 1. add all the new itemgrading for MC/Survey and discard any
    // itemgrading for MC/Survey
    // 2. add any modified SAQ/TF/FIB/Matching/MCMR
    switch (typeId){
    case 1: // MC
    case 3: // Survey
            boolean answerModified = false;
            for (int m=0;m<grading.size();m++){
              ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
              if (itemgrading.getItemGradingId()==null 
                  || itemgrading.getItemGradingId().intValue()<=0){
                answerModified = true;
                break;
              }
            }
            if (answerModified){
              for (int m=0;m<grading.size();m++){
                ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
                if (itemgrading.getItemGradingId()!=null && itemgrading.getItemGradingId().intValue()>0){
                  log.info("****** dd. remove itemGrading="+itemgrading.getItemGradingId());
                  removes.add(itemgrading);
                }
                else{
                  // add new answer
                  log.info("****** dd. add itemGrading, grading.getAssessmentGrading()="+itemgrading.getAssessmentGrading());
                  itemgrading.setAgentId(AgentFacade.getAgentString());
                  itemgrading.setSubmittedDate(new Date());
                  // the rest of the info is collected by ItemContentsBean via JSF form 
                  adds.add(itemgrading);
                }
              }
	    }
            break;
    case 2: // MCMR
    case 4: // T/F
    case 5: // SAQ
    case 6: // File Upload
    case 7: // Audio
    case 8: // FIB
    case 9: // Matching
            log.info("****** dd. typeId, size="+typeId);
            log.info("****** dd. add all grading, size="+grading.size());
            adds.addAll(grading);
            break;   
    }
  }

}
