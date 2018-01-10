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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;
import org.sakaiproject.tool.assessment.util.TextFormat;

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

@Slf4j
 public class QuestionScoreUpdateListener
  implements ActionListener
{
  private final EventTrackingService eventTrackingService= ComponentManager.get( EventTrackingService.class );

  //private static EvaluationListenerUtil util;
  //private static BeanSort bs;
  //private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("Question Score Update LISTENER.");
    QuestionScoresBean bean = (QuestionScoresBean) ContextUtil.lookupBean("questionScores");
    TotalScoresBean tbean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    log.debug("Calling saveQuestionScores.");
    
    Long publishedId = Long.valueOf(ContextUtil.lookupParam("publishedId"));
    Long publishedIdFromBean = tbean.getPublishedAssessment().getPublishedAssessmentId();
    if (publishedId != null && publishedIdFromBean != null && !publishedId.equals(publishedIdFromBean)) {
    	throw new IllegalArgumentException("Published id has changed from " + publishedIdFromBean + " to " + publishedId);
    }
    
    tbean.setAssessmentGradingHash(publishedIdFromBean);
    try{
      if (!saveQuestionScores(bean, tbean))
      {
        throw new RuntimeException("failed to call saveQuestionScores.");
      }
    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
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
      //String publishedId = ContextUtil.lookupParam("publishedId");
      String itemId = ContextUtil.lookupParam("itemId");
      String which = ContextUtil.lookupParam("allSubmissions");
      if (which == null)
        which = "false";
      Collection agents = bean.getAgents();
      //ArrayList items = new ArrayList();
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
        
        int fibFinNumCorrect  = 0;
        if (bean.getTypeId().equals("8") || bean.getTypeId().equals("11")) {        
        	Iterator iter1 = datas.iterator();
        	while (iter1.hasNext()){
        		Object obj = iter1.next();
        		ItemGradingData data = (ItemGradingData) obj;
        		if (data.getIsCorrect() != null && data.getIsCorrect().booleanValue()) {
        			fibFinNumCorrect++;
        		}
        	}
        }
        
        boolean hasUpdateAttachment = false;
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext()){
          Object obj = iter2.next();
          ItemGradingData data = (ItemGradingData) obj;

          // check if there is differnce in score, if so, update. Otherwise, do nothing
          double newAutoScore = 0;
          if ((bean.getTypeId().equals("8") || bean.getTypeId().equals("11")) && fibFinNumCorrect != 0) {
        	  if (Boolean.TRUE.equals(data.getIsCorrect())) {
        		  newAutoScore = (Double.valueOf(ar.getTotalAutoScore())).doubleValue() / (double) fibFinNumCorrect;
        	  }
          }
          else {
        	  newAutoScore = (Double.valueOf(ar.getTotalAutoScore())).doubleValue() / (double) datas.size();
          }
          String newComments = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(ar.getComments());
          ar.setComments(newComments);
          if (newComments!=null) {
        	  newComments = newComments.trim();
          }
          else {
        	  newComments = "";
          }

          double oldAutoScore = 0;
          if (data.getAutoScore() !=null)
            oldAutoScore=data.getAutoScore().doubleValue();
          
          String oldComments = data.getComments();
          if (oldComments!=null) {
        	  oldComments = oldComments.trim();
          }
          else {
        	  oldComments = "";
          }
                    
          StringBuffer logString = new StringBuffer();
          logString.append("gradedBy=");
          logString.append(AgentFacade.getAgentString());
          logString.append(", itemGradingId=");
          logString.append(data.getItemGradingId());
          
          // if newAutoScore != oldAutoScore
          if (!(Precision.equalsIncludingNaN(newAutoScore, oldAutoScore, 0.0001))) {
        	data.setAutoScore(Double.valueOf(newAutoScore));
        	logString.append(", newAutoScore=");
            logString.append(newAutoScore);
            logString.append(", oldAutoScore=");
            logString.append(oldAutoScore);
          }
          if (!newComments.equals(oldComments)) {
            data.setComments(ar.getComments());
            logString.append(", newComments=");
            logString.append(newComments);
            logString.append(", oldComments=");
            logString.append(oldComments);
          }
          
          // if newAutoScore != oldAutoScore or newComments != oldComments
          if (!(Precision.equalsIncludingNaN(newAutoScore, oldAutoScore, 0.0001)) || !newComments.equals(oldComments)){
            data.setGradedBy(AgentFacade.getAgentString());
            data.setGradedDate(new Date());
            String targetString = "siteId=" + AgentFacade.getCurrentSiteId() + ", " + logString.toString();
            String safeString = targetString.length() > 255 ? targetString.substring(0, 255) : targetString;
            delegate.updateItemScore(data, newAutoScore-oldAutoScore, tbean.getPublishedAssessment());
            //Need this again for the total score, it might be better if updateItemScore returned this object
            AssessmentGradingData adata = delegate.load(data.getAssessmentGradingId().toString(),false);
            eventTrackingService.post(eventTrackingService.newEvent(
            		SamigoConstants.EVENT_ASSESSMENT_QUESTION_SCORE_UPDATE,safeString, AgentFacade.getCurrentSiteId(), 
            		true, NotificationService.NOTI_OPTIONAL, SamigoLRSStatements.getStatementForQuestionScoreUpdate(adata, tbean.getPublishedAssessment(), newAutoScore, oldAutoScore)));
          }
          
          if (!hasUpdateAttachment) {
        	  hasUpdateAttachment = true;
        	  updateAttachment(data, ar, bean);
          }
        }
      }

    } catch (GradebookServiceException ge) {
       FacesContext context = FacesContext.getCurrentInstance();
       String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_error");
       context.addMessage(null, new FacesMessage(err));

    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void updateAttachment(ItemGradingData itemGradingData, AgentResults agentResults, QuestionScoresBean bean){
	  List oldList = itemGradingData.getItemGradingAttachmentList();
	  List newList = agentResults.getItemGradingAttachmentList();
	  if ((oldList == null || oldList.size() == 0 ) && (newList == null || newList.size() == 0)) return;
	  List attachmentList = new ArrayList();
	  HashMap map = getAttachmentIdHash(oldList);
	  for (int i=0; i<newList.size(); i++){
		  ItemGradingAttachment itemGradingAttachment = (ItemGradingAttachment) newList.get(i);
		  if (map.get(itemGradingAttachment.getAttachmentId()) != null){
			  // exist already, remove it from map
			  map.remove(itemGradingAttachment.getAttachmentId());
		  }
		  else{
			  // new attachments
			  itemGradingAttachment.setItemGrading(itemGradingData);
			  itemGradingAttachment.setAttachmentType(AttachmentIfc.ITEMGRADING_ATTACHMENT);
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
	  Iterator iter = set.iterator();
	  while (iter.hasNext()){
		  Long attachmentId = (Long)iter.next();
		  gradingService.removeItemGradingAttachment(attachmentId.toString());
		  eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_STUDENT_SCORE_UPDATE, 
				  "siteId=" + AgentFacade.getCurrentSiteId() + ", Removing attachmentId = " + attachmentId, true));
	  }
	  bean.setIsAnyItemGradingAttachmentListModified(true);
  }

  private HashMap getAttachmentIdHash(List list){
    HashMap map = new HashMap();
    for (int i=0; i<list.size(); i++){
    	ItemGradingAttachment a = (ItemGradingAttachment)list.get(i);
      map.put(a.getAttachmentId(), a);
    }
    return map;
  }
}
