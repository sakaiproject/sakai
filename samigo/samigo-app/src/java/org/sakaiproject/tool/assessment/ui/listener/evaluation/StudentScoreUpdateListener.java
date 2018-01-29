/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;
import org.sakaiproject.tool.assessment.util.TextFormat;

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
@Slf4j
public class StudentScoreUpdateListener
  implements ActionListener
{
  private final EventTrackingService eventTrackingService= ComponentManager.get( EventTrackingService.class );

  private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("Student Score Update LISTENER.");
    StudentScoresBean bean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
    TotalScoresBean tbean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    tbean.setAssessmentGradingHash(tbean.getPublishedAssessment().getPublishedAssessmentId());
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    log.debug("Calling saveStudentScores.");
    try {
      if (!saveStudentScores(bean, tbean, delivery))
      {
        throw new RuntimeException("failed to call saveStudentScores.");
      }
    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));

    }

  }

  /**
   * Persist the results from the ActionForm in the student page.
   * @param bean StudentScoresBean bean
   * @return true if successful
   */
    public boolean saveStudentScores(StudentScoresBean bean, TotalScoresBean tbean,
                                   DeliveryBean delivery)
  {
    GradingService delegate = new GradingService();
    HashSet itemGradingSet = new HashSet();
    AssessmentGradingData adata = null;
    try
    {
      List parts = delivery.getPageContents().getPartsContents();
      Iterator iter = parts.iterator();
      boolean updateFlag = false;
      while (iter.hasNext())
      {
        List items = ((SectionContentsBean) iter.next()).getItemContents();
        Iterator iter2 = items.iterator();
        while (iter2.hasNext())
        {
          ItemContentsBean question = (ItemContentsBean) iter2.next();
          List<ItemGradingData> gradingarray = question.getItemGradingDataArray();
          log.debug("****1. pub questionId = " + question.getItemData().getItemId());
          log.debug("****2. Gradingarray length = " + gradingarray.size());
          // Create a new one if we need it.
          if (gradingarray.isEmpty() && (question.getExactPoints() > 0  ||
              (question.getGradingComment() != null &&
               !question.getGradingComment().trim().equals("")) ))
          {
            // this is another mystery, no idea why review is involved here - daiyf
            question.setReview(false); // This creates an itemgradingdata
            gradingarray = question.getItemGradingDataArray();
          }

          int fibFinNumCorrect  = 0;
          if (question.getItemData().getTypeId().equals(Long.valueOf(8)) || question.getItemData().getTypeId().equals(Long.valueOf(11))) {
        	  Iterator itemGradingIter = gradingarray.iterator();
        	  while (itemGradingIter.hasNext()){
        		  Object obj = itemGradingIter.next();
        		  ItemGradingData data = (ItemGradingData) obj;
        		  if (Boolean.TRUE.equals(data.getIsCorrect())) {
        			  fibFinNumCorrect++;
        		  }
        	  }
          }
          
          log.debug("****3a Gradingarray length2 = " + gradingarray.size());
          log.debug("****3b set points = " + question.getExactPoints() + ", comments to " + question.getGradingComment());
          for (ItemGradingData data : gradingarray)
          {
            if (adata == null && data.getAssessmentGradingId() != null){
              adata = delegate.load(data.getAssessmentGradingId().toString());
            }
            if (data.getAgentId() == null)
            { // this is a skipped question, set submittedDate=null
              data.setSubmittedDate(null);
              data.setAgentId(bean.getStudentId());
            }
            
            double newAutoScore = 0;            
            if ((question.getItemData().getTypeId().equals(Long.valueOf(8)) || question.getItemData().getTypeId().equals(Long.valueOf(11))) && fibFinNumCorrect != 0) {
            	if (Boolean.TRUE.equals(data.getIsCorrect())) {
            		newAutoScore = (question.getExactPoints() / (double) fibFinNumCorrect);
            	}
            }
            else {
          	  newAutoScore = (question.getExactPoints() / (double) gradingarray.size());
            }
            double oldAutoScore = 0;
            if (data.getAutoScore() !=null) {
              oldAutoScore=data.getAutoScore();
            }
            String newComments = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(question.getGradingComment());
            if (newComments != null) {
      		  newComments = newComments.trim();
            }
            else {
              newComments = "";
            }
            String oldComments = data.getComments();
            if (oldComments != null) { 	
          	  oldComments = oldComments.trim();
            }
            else {
              oldComments = "";
            }
            
            // if newAutoScore != oldAutoScore then updateScore = true
            boolean updateScore = !(Precision.equalsIncludingNaN(newAutoScore, oldAutoScore, 0.0001));
            boolean updateComments = !newComments.equals(oldComments);
            StringBuilder logString = new StringBuilder();
            logString.append("gradedBy=");
            logString.append(AgentFacade.getAgentString());
            logString.append(", itemGradingId=");
            logString.append(data.getItemGradingId());
            
            if (updateScore) {
              data.setAutoScore(newAutoScore);
              logString.append(", newAutoScore=");
              logString.append(newAutoScore);
              logString.append(", oldAutoScore=");
              logString.append(oldAutoScore);
            }
            if (updateComments) {
              data.setComments(newComments);
              logString.append(", newComments=");
              logString.append(newComments);
              logString.append(", oldComments=");
              logString.append(oldComments);
            }
            if (updateScore || updateComments) {
              updateFlag = true;	
              data.setGradedBy(AgentFacade.getAgentString());
              data.setGradedDate(new Date());
              String targetString = "siteId=" + AgentFacade.getCurrentSiteId() + ", " + logString.toString();
              String safeString = targetString.length() > 255 ? targetString.substring(0, 255) : targetString;
              eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE, safeString, true));
              log.debug("****4 itemGradingId="+data.getItemGradingId());
              log.debug("****5 set points = " + data.getAutoScore() + ", comments to " + data.getComments());
            }
		data.setAnswerText(data.getAnswerText());
            itemGradingSet.add(data);
          }
        }
        if (adata==null){
          // this is for cases when studnet submitted an assessment but skipped all teh questions
          // when we won't be able to get teh assessmentGrading based on itemGrdaing ('cos there is none).
          String assessmentGradingId = ContextUtil.lookupParam("gradingData");
          adata = delegate.load(assessmentGradingId);
        }
        adata.setItemGradingSet(itemGradingSet);
      }

      if (adata == null)
        return true; // Nothing to save.

      String newComments = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getComments());
      if (newComments != null) {
    	  newComments = newComments.trim();
      }
      else {
    	  newComments = "";
      }
      String oldComments = adata.getComments();
      if (oldComments != null) { 	
    	  oldComments = oldComments.trim();
      }
      else {
    	  oldComments = "";
      }

      StringBuilder logString = new StringBuilder();
      logString.append("gradedBy=");
      logString.append(AgentFacade.getAgentString());
      logString.append(", assessmentGradingId=");
      logString.append(adata.getAssessmentGradingId());
      if (!newComments.equals(oldComments)) {
    	  updateFlag = true;
    	  adata.setComments(newComments);
    	  adata.setGradedBy(AgentFacade.getAgentString());
    	  adata.setGradedDate(new Date());
          logString.append(", newComments=");
          logString.append(newComments);
          logString.append(", oldComments=");
          logString.append(oldComments);
      }

      if (updateFlag) {
    	  delegate.updateAssessmentGradingScore(adata, tbean.getPublishedAssessment(), newComments, oldComments);
    	  eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE, logString.toString(), AgentFacade.getCurrentSiteId(), true, NotificationService.NOTI_OPTIONAL, SamigoLRSStatements.getStatementForStudentScoreUpdate(adata, tbean.getPublishedAssessment())));
      }
      log.debug("Saved student scores.");
      
      updateAttachment(delivery);

    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));

    }
    catch (Exception e)
    {
      log.warn("Error saving scores", e);
      return false;
    }
    return true;
  }

    public void updateAttachment(DeliveryBean delivery){
    	List parts = delivery.getPageContents().getPartsContents();
    	Iterator iter = parts.iterator();
    	List attachmentList = new ArrayList();
    	while (iter.hasNext())
    	{
    		List items = ((SectionContentsBean) iter.next()).getItemContents();
    		Iterator iter2 = items.iterator();
    		while (iter2.hasNext())
    		{
    			ItemContentsBean question = (ItemContentsBean) iter2.next();
    			List<ItemGradingData> gradingarray = question.getItemGradingDataArray();
    			log.debug("Gradingarray length2 = " + gradingarray.size());
    			Iterator<ItemGradingData> iter3 = gradingarray.iterator();
    			while (iter3.hasNext()) {
    				ItemGradingData itemGradingData = iter3.next();
    				List oldList = itemGradingData.getItemGradingAttachmentList();
    				List newList = question.getItemGradingAttachmentList();
    				if ((oldList == null || oldList.isEmpty() ) && (newList == null || newList.isEmpty())) {
    					continue;
    				}
    				
    				Map map = getAttachmentIdHash(oldList);
    				for (int i=0; i<newList.size(); i++){
    					ItemGradingAttachment itemGradingAttachment = (ItemGradingAttachment) newList.get(i);
    					if (map.get(itemGradingAttachment.getAttachmentId()) != null){
    						// exist already, remove it from map
    						map.remove(itemGradingAttachment.getAttachmentId());
    					}
    					else{
    						// new attachments
    						itemGradingAttachment.setItemGrading(itemGradingData);
    						attachmentList.add(itemGradingAttachment);
    					}
    				}      
    				// save new ones
    				GradingService gradingService = new GradingService();
    				if (attachmentList.size() > 0) {
    					gradingService.saveOrUpdateAttachments(attachmentList);
    					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE, 
    							"siteId=" + AgentFacade.getCurrentSiteId() + ", Adding " + attachmentList.size() + " attachments for itemGradingData id = " + itemGradingData.getItemGradingId(), 
    							true));
    				}
    				
    				// remove old ones
    				Set set = map.keySet();
    				Iterator iter4 = set.iterator();
    				while (iter4.hasNext()){
    					Long attachmentId = (Long)iter4.next();
    					gradingService.removeItemGradingAttachment(attachmentId.toString());
    					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE, 
    							"siteId=" + AgentFacade.getCurrentSiteId() + ", Removing attachmentId = " + attachmentId, true));
    				}
    			}
    		}
    	}
    }

    private Map getAttachmentIdHash(List list){
    	Map map = new HashMap();
    	for (int i=0; i<list.size(); i++){
    		ItemGradingAttachment a = (ItemGradingAttachment)list.get(i);
    		map.put(a.getAttachmentId(), a);
    	}
    	return map;
    }
}
