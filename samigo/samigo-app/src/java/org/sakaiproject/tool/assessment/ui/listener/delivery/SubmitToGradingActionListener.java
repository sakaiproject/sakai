/**********************************************************************************
 * $URL$
 * $Id$
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



package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.File;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
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
      else{
        publishedAssessment =
          publishedAssessmentService.getPublishedAssessment(delivery.getAssessmentId());
        delivery.setPublishedAssessment(publishedAssessment);
      }

      AssessmentGradingData adata = submitToGradingService(publishedAssessment, delivery);
      // set AssessmentGrading in delivery
      delivery.setAssessmentGrading(adata);

      // set url & confirmation after saving the record for grade
      if (adata !=null && delivery.getForGrade())
        setConfirmation(adata, publishedAssessment, delivery);

      if (isForGrade(adata) && !isUnlimited(publishedAssessment))
      {
        delivery.setSubmissionsRemaining(
            delivery.getSubmissionsRemaining() - 1);
      }

    } catch (GradebookServiceException ge) {
       ge.printStackTrace();
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));
       return;

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
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
        log.debug("****** before prepareItemGradingPerItem");
        prepareItemGradingPerItem(delivery, item, adds, removes);
        log.debug("****** after prepareItemGradingPerItem");
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
    }

    GradingService service = new GradingService();
    log.debug("**adata="+adata);
    if (adata == null) { // <--- this shouldn't happened 'cos it should have been created by BeginDelivery
      adata = makeNewAssessmentGrading(publishedAssessment, delivery, itemGradingHash);
      delivery.setAssessmentGrading(adata);
    }
    else {
      // 1. add all the new itemgrading for MC/Survey and discard any
      // itemgrading for MC/Survey
      // 2. add any modified SAQ/TF/FIB/Matching/MCMR
  
      HashMap fibMap = getFIBMap(publishedAssessment);
      HashMap mcmrMap = getMCMRMap(publishedAssessment);
      Set itemGradingSet = adata.getItemGradingSet();
      log.debug("*** 2a. before removal & addition "+(new Date()));
      if (itemGradingSet!=null){
        log.debug("*** 2aa. removing old itemGrading "+(new Date()));
        itemGradingSet.removeAll(removes);
        service.deleteAll(removes);
        // refresh itemGradingSet & assessmentGrading after removal 
        log.debug("*** 2ab. reload itemGradingSet "+(new Date()));
        itemGradingSet = service.getItemGradingSet(adata.getAssessmentGradingId().toString());
        log.debug("*** 2ac. load assessmentGarding "+(new Date()));
        adata = service.load(adata.getAssessmentGradingId().toString());

        Iterator iter = adds.iterator();
        while (iter.hasNext()){
          ((ItemGradingIfc)iter.next()).setAssessmentGradingId(adata.getAssessmentGradingId());
	}
        // make update to old item and insert new item
        // and we will only update item that has been changed
        log.debug("*** 2ad. set assessmentGrading with new/updated itemGrading "+(new Date()));
        HashSet updateItemGradingSet = getUpdateItemGradingSet(itemGradingSet, adds, fibMap, mcmrMap, adata);
        adata.setItemGradingSet(updateItemGradingSet);
      }
    }

    adata.setIsLate(isLate(publishedAssessment));
    adata.setForGrade(new Boolean(delivery.getForGrade()));
    log.debug("*** 2b. before storingGrades, did all the removes and adds "+(new Date()));
    service.saveOrUpdateAssessmentGrading(adata); 

    log.debug("*** 3. before storingGrades, did all the removes and adds "+(new Date()));
    // 3. let's build three HashMap with (publishedItemId, publishedItem), 
    // (publishedItemTextId, publishedItem), (publishedAnswerId, publishedItem) to help with
    // storing grades
    HashMap publishedItemHash = delivery.getPublishedItemHash();
    HashMap publishedItemTextHash = delivery.getPublishedItemTextHash();
    HashMap publishedAnswerHash = delivery.getPublishedAnswerHash();
    service.storeGrades(adata, publishedAssessment, publishedItemHash, 
                        publishedItemTextHash, publishedAnswerHash);
    log.debug("*** 4. after storingGrades, did all the removes and adds "+(new Date()));
    return adata;
  }

  private HashMap getFIBMap(PublishedAssessmentIfc publishedAssessment){
    PublishedAssessmentService s = new PublishedAssessmentService();
    return s.prepareFIBItemHash(publishedAssessment);
  }

  private HashMap getMCMRMap(PublishedAssessmentIfc publishedAssessment){
    PublishedAssessmentService s = new PublishedAssessmentService();
    return s.prepareMCMRItemHash(publishedAssessment);
  }

  private HashSet getUpdateItemGradingSet(Set oldItemGradingSet, Set newItemGradingSet, 
                                          HashMap fibMap, HashMap mcmrMap,
                                          AssessmentGradingData adata){
    HashSet updateItemGradingSet = new HashSet();
    HashSet h = new HashSet();
    Iterator iter = oldItemGradingSet.iterator();
    HashMap map = new HashMap();
    while (iter.hasNext()){ // create a map with old itemGrading
      ItemGradingIfc item = (ItemGradingIfc) iter.next();
      map.put(item.getItemGradingId(), item);
    }

    // go through new itemGrading
    Iterator iter1 = newItemGradingSet.iterator();
    while (iter1.hasNext()){
      ItemGradingIfc newItem = (ItemGradingIfc) iter1.next();
      ItemGradingIfc oldItem = (ItemGradingIfc)map.get(newItem.getItemGradingId());
      if (oldItem != null){ 
        // itemGrading exists and value has been change, then need update
        Boolean oldReview = oldItem.getReview();
        Boolean newReview = newItem.getReview();
        Long oldAnswerId = oldItem.getPublishedAnswerId(); 
        Long newAnswerId = newItem.getPublishedAnswerId(); 
        String oldRationale = oldItem.getRationale();
        String newRationale = newItem.getRationale();
        String oldAnswerText = oldItem.getAnswerText();
        String newAnswerText = newItem.getAnswerText();
        if ((oldReview!=null && !oldReview.equals(newReview))
            || (oldAnswerId!=null && !oldAnswerId.equals(newAnswerId))
            || (newAnswerId!=null && !newAnswerId.equals(oldAnswerId))
            || (oldRationale!=null && !oldRationale.equals(newRationale))
            || (newRationale!=null && !newRationale.equals(newRationale))
            || (oldAnswerText!=null && !oldAnswerText.equals(newAnswerText))
            || (newAnswerText!=null && !newAnswerText.equals(newAnswerText))
            || fibMap.get(oldItem.getPublishedItemId())!=null
            || mcmrMap.get(oldItem.getPublishedItemId())!=null){
          oldItem.setReview(newItem.getReview());
          oldItem.setPublishedAnswerId(newItem.getPublishedAnswerId());
          oldItem.setRationale(newItem.getRationale());
          oldItem.setAnswerText(newItem.getAnswerText());
          oldItem.setSubmittedDate(new Date());
          oldItem.setAutoScore(newItem.getAutoScore());
          oldItem.setOverrideScore(newItem.getOverrideScore());
          updateItemGradingSet.add(oldItem);
          //log.debug("**** SubmitToGrading: need update "+oldItem.getItemGradingId());
	}
      }
      else {  // itemGrading from new set doesn't exist, add to set in this case
        //log.debug("**** SubmitToGrading: need add new item");
        log.debug("** who r u?"+AgentFacade.getAgentString());
        newItem.setAgentId(adata.getAgentId());
        updateItemGradingSet.add(newItem);
      }
    }
    return updateItemGradingSet;
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
    adata.setPublishedAssessmentId(publishedAssessment.getPublishedAssessmentId());
    adata.setForGrade(new Boolean(delivery.getForGrade()));
    adata.setItemGradingSet(itemGradingHash);
    adata.setAttemptDate(new Date());
    adata.setIsLate(Boolean.FALSE);
    adata.setStatus(new Integer(0));
    adata.setTotalOverrideScore(new Float(0));
    adata.setTimeElapsed(new Integer("0"));
    return adata;
  }

  /* This is specific to JSF - question for each type is layout differently in JSF and the
   * answers submitted are being collected differently too.
   * e.g. for each MC/Survey/MCMR, an itemgrading is associated with each choice.
   *      whereas there is only one itemgrading per each question for SAQ/TF/Audio, and one for
   *      ecah blank in FIB.
   * To understand the logic in this method, it is best to study jsf/delivery/item/deliver*.jsp
   */
  private void prepareItemGradingPerItem(DeliveryBean delivery, ItemContentsBean item, HashSet adds, HashSet removes){
    ArrayList grading = item.getItemGradingDataArray();
    int typeId = item.getItemData().getTypeId().intValue();

    // 1. add all the new itemgrading for MC/Survey and discard any
    // itemgrading for MC/Survey
    // 2. add any modified SAQ/TF/FIB/Matching/MCMR/Audio
    switch (typeId){
    case 1: // MC
    case 3: // Survey
            boolean answerModified = false;
            for (int m=0;m<grading.size();m++){
              ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
              if (itemgrading.getItemGradingId()==null 
                  || itemgrading.getItemGradingId().intValue()<=0){ // => new answer
                if (itemgrading.getPublishedAnswerId()!=null){ //null=> skipping this question
                  answerModified = true;
                  break;
                }
              }
            }
            if (answerModified){
              for (int m=0;m<grading.size();m++){
                ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
                if (itemgrading.getItemGradingId()!=null && itemgrading.getItemGradingId().intValue()>0){
                  // remove all old answer for MC & Surevy
                  removes.add(itemgrading);
                }
                else{
                  // add new answer
                  if (itemgrading.getPublishedAnswerId()!=null || itemgrading.getAnswerText()!=null){ 
                    //null=> skipping this question
                    itemgrading.setAgentId(AgentFacade.getAgentString());
                    itemgrading.setSubmittedDate(new Date());
                    // the rest of the info is collected by ItemContentsBean via JSF form 
                    adds.add(itemgrading);
		  }
                }
              }
	    }
            break;
    case 4: // T/F
    case 5: // SAQ
    case 6: // File Upload
    case 7: // Audio
    case 8: // FIB
    case 9: // Matching
	    for (int m=0;m<grading.size();m++){
              ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
              itemgrading.setAgentId(AgentFacade.getAgentString());
              itemgrading.setSubmittedDate(new Date());
	    }
	    for (int m=0;m<grading.size();m++){
              ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
              if (itemgrading.getItemGradingId()!=null && itemgrading.getItemGradingId().intValue()>0){
                adds.addAll(grading);
                break;
	      }
              else if (itemgrading.getPublishedAnswerId()!=null || itemgrading.getAnswerText()!=null){ 
                //null=> skipping this question
                adds.addAll(grading);
                break;
	      }
	    }
            break;   
    case 2: // MCMR
	    for (int m=0;m<grading.size();m++){
              ItemGradingData itemgrading = (ItemGradingData)grading.get(m);
              if (itemgrading.getItemGradingId()!=null && itemgrading.getItemGradingId().intValue()>0){
                // old answer, check which one to keep, not keeping null answer 
                if (itemgrading.getPublishedAnswerId()!=null){
                  itemgrading.setAgentId(AgentFacade.getAgentString());
                  itemgrading.setSubmittedDate(new Date());
                  adds.add(itemgrading);
		}
		else{
                  removes.add(itemgrading);
		}
	      }
              else if (itemgrading.getPublishedAnswerId()!=null){ // new addition
                // not accepting any new answer with null for MCMR
                itemgrading.setAgentId(AgentFacade.getAgentString());
                itemgrading.setSubmittedDate(new Date());
                adds.add(itemgrading);
	      }
	    }
            break;   
	    /*
    case 7: // Audio
            // audio is uploaded by UploadAudioMediaServlet to 
            // {repositoryPath}/jsf/upload_tmp/assessmentId/questionId/agentId
            // 1. check if saveToDB=true, if so move audio to DB
            // 2. add an itemGrading record for audio question.   
            ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
	    ServletContext context = (ServletContext) extContext.getContext();
            String repositoryPath = (String)context.getAttribute("FILEUPLOAD_REPOSITORY_PATH");
            Long questionId = item.getItemData().getItemId();
            String mediaLocation = repositoryPath+ContextUtil.lookupParam("mediaLocation_"+questionId.toString());           
            //log.debug("**** mediaLocation="+mediaLocation);
            try{
              File file = new File(mediaLocation); 
              //log.debug("**** file exists="+file.exists());
              if (file.exists())
                delivery.addMediaToItemGrading(mediaLocation);
	    }
            catch(Exception e){
              
              log.debug("audio question not answered:"+e.getMessage());
              e.printStackTrace();
	    }
            break;
	    */
    }
  }

  private Boolean isLate(PublishedAssessmentIfc pub){
    AssessmentAccessControlIfc a = pub.getAssessmentAccessControl();
    if (a.getDueDate()!=null && a.getDueDate().before(new Date()))
      return Boolean.TRUE;
    else
      return Boolean.FALSE;
  }

}
