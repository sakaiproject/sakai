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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.math3.util.Precision;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;
import org.sakaiproject.tool.assessment.util.TextFormat;

/**
 * <p>
 * This handles the updating of the Total Score page.
 *  </p>
 * <p>Description: Action Listener Evaluation Updating Total Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class TotalScoreUpdateListener
  implements ActionListener
{
  private final EventTrackingService eventTrackingService= ComponentManager.get( EventTrackingService.class );


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("Total Score Update LISTENER.");
    TotalScoresBean bean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    if ("4".equals(bean.getAllSubmissions()) && ae != null && ae.getComponent() != null && "applyScoreButton".equals(ae.getComponent().getId()))
    {
        // We're looking at average scores and we're applying a score to participants with no submission
        log.debug("Calling saveTotalScoresAverage.");
        if (!saveTotalScoresAverage(bean))
        {
            throw new RuntimeException("failed to call saveTotalScoresAverage.");
        }
    }
    else
    {
        log.debug("Calling saveTotalScores.");
        if (!saveTotalScores(bean))
        {
            throw new RuntimeException("failed to call saveTotalScores.");
        }
     }
 

  }

  private Map prepareAssessmentGradingHash(List assessmentGradingList){
    Map map = new HashMap();
    for (int i=0; i<assessmentGradingList.size(); i++){
      AssessmentGradingData a = (AssessmentGradingData)assessmentGradingList.get(i);
      map.put(a.getAssessmentGradingId(), a);
    }
    return map;
  }

  /**
   * Persist the results from the ActionForm in the total page.
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param bean TotalScoresBean bean
   * @return true if successful
   */
  public boolean saveTotalScores(TotalScoresBean bean)
  {

      List assessmentGradingList = bean.getAssessmentGradingList();
      Map map = prepareAssessmentGradingHash(assessmentGradingList);
      Collection agents = bean.getAgents();
      Iterator iter = agents.iterator();
      List <AssessmentGradingData> grading = new ArrayList();
      boolean hasNumberFormatException = false;
      StringBuffer idList = new StringBuffer(" ");
  	  String err = "";
  	  boolean isAnonymousGrading = false;

  	  String applyToUngraded = bean.getApplyToUngraded().trim();
  	  if(applyToUngraded != null && !"".equals(applyToUngraded)){
  		  try{
  			  Double.valueOf(applyToUngraded).doubleValue();
  			  List allAgents = bean.getAllAgentsDirect();
  			  iter = allAgents.iterator();
  			  while(iter.hasNext()){
  				  AgentResults agentResults = (AgentResults) iter.next();
  				  if (agentResults.getAssessmentGradingId().equals(Long.valueOf(-1)) || agentResults.getSubmittedDate() == null) {
  					  agentResults.setTotalOverrideScore(applyToUngraded+"");
  				  }
  			  }
  			  iter = allAgents.iterator();
  			  bean.setApplyToUngraded("");
  		  }catch (Exception e) {
  			  FacesContext context = FacesContext.getCurrentInstance();
  			  String err2 = (String) ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_user_id_apply");
  			  context.addMessage(null,  new FacesMessage(err2));
  			  return true;
  		  }
  		  bean.setApplyToUngraded("");
  	  }


  	  if (bean.getPublishedAssessment() != null 
  			  && bean.getPublishedAssessment().getEvaluationModel() != null
  			  && bean.getPublishedAssessment().getEvaluationModel().getAnonymousGrading() != null
  			  && bean.getPublishedAssessment().getEvaluationModel().getAnonymousGrading().equals(EvaluationModelIfc.ANONYMOUS_GRADING)) {
  		  isAnonymousGrading = true;
  		  err = (String) ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_submission_id");
  	  }
  	  else {
  		  err = (String) ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_user_id");
  	  }
  	  List badAdjList = new ArrayList();
  	  
  	  while (iter.hasNext())
  	  {
  		  AgentResults agentResults = (AgentResults) iter.next();
  		  StringBuilder newScoreString = new StringBuilder();
  		  boolean update = false;
  		  try {
  			  update = needUpdate(agentResults, map, newScoreString, bean);     
  		  }
  		  catch (NumberFormatException e) {
  			  hasNumberFormatException = true;
  			  update = false;

  			  if (isAnonymousGrading) {
  				  badAdjList.add(agentResults.getAssessmentGradingId());
  			  }
  			  else {
  				  badAdjList.add(agentResults.getAgentEid());
  			  }
  		  }
  		  
        if (update){
        	log.debug("update is true");
        	Double newScore = new Double(0d);
        	AssessmentGradingData data = new AssessmentGradingData();
        	try {
        		if (!agentResults.getAssessmentGradingId().equals(Long.valueOf(-1)) ) {
        			// these are students who have submitted for grades.
        			// Add up new score
        			newScore = Double.valueOf(newScoreString.toString());
        			agentResults.setFinalScore(newScore+"");
        			BeanUtils.copyProperties(data, agentResults);
        			data.setPublishedAssessmentId(bean.getPublishedAssessment().getPublishedAssessmentId());
        			if ("-".equals(agentResults.getTotalAutoScore())) {
        				data.setTotalAutoScore(Double.valueOf(0d));
        			}
        			else {
        				data.setTotalAutoScore(Double.valueOf(agentResults.getTotalAutoScore()));
        			}
        			data.setTotalOverrideScore(Double.valueOf(agentResults.getTotalOverrideScore()));
        			data.setFinalScore(Double.valueOf(agentResults.getFinalScore()));
        			data.setIsLate(agentResults.getIsLate());
        			data.setComments(agentResults.getComments());
        			data.setGradedBy(AgentFacade.getAgentString());
        			data.setGradedDate(new Date());
        			grading.add(data);
        		}
        		else {
        			// these are students who have not submitted for grades and instructor made adjustment to their scores
        			// Add up new score
        			newScore = Double.valueOf(newScoreString.toString());
        			agentResults.setFinalScore(newScore+"");

        			BeanUtils.copyProperties(data, agentResults);
        			data.setAgentId(agentResults.getIdString());
        			data.setForGrade(Boolean.FALSE);
        			//data.setStatus(Integer.valueOf(1));
        			data.setIsLate(Boolean.FALSE);
        			data.setItemGradingSet(new HashSet());
        			data.setPublishedAssessmentId(bean.getPublishedAssessment().getPublishedAssessmentId());
        			// tell hibernate this is a new record
        			data.setAssessmentGradingId(Long.valueOf(0));
        			data.setSubmittedDate(null);
        			data.setTotalAutoScore(Double.valueOf(0d));
        			data.setTotalOverrideScore(Double.valueOf(agentResults.getTotalOverrideScore()));
        			data.setFinalScore(Double.valueOf(agentResults.getFinalScore()));
        			data.setComments(agentResults.getComments());
        			data.setGradedBy(AgentFacade.getAgentString());
        			data.setGradedDate(new Date());
        			// note that I am not sure if we should set this people as late or what?
        			grading.add(data);
        		}

        	}
        	catch (IllegalAccessException e) {
        		log.error("IllegalAccessException: " + e);
        		return false;
        	} catch (InvocationTargetException e) {
        		log.error("InvocationTargetException: " + e);
        		return false;
        	}
        }
      }

      if (hasNumberFormatException) {
    	  if (bean.getPublishedAssessment() != null 
    			  && bean.getPublishedAssessment().getEvaluationModel() != null
    			  && bean.getPublishedAssessment().getEvaluationModel().getAnonymousGrading() != null
    			  && bean.getPublishedAssessment().getEvaluationModel().getAnonymousGrading().equals(EvaluationModelIfc.ANONYMOUS_GRADING)) {
    		  err = (String) ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_submission_id");
    	  }
    	  else {
    		  err = (String) ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_user_id");
    	  }
    	  for (int i = 0; i < badAdjList.size(); i++) {
    		  idList.append(badAdjList.get(i));
    		  if (i != badAdjList.size() - 1) {
    			  idList.append(", ");
    		  }
    	  }
    	  idList.append(".");

    	  FacesContext context = FacesContext.getCurrentInstance();
    	  context.addMessage(null, new FacesMessage(err + idList.toString()));
      }
      
      GradingService delegate = new GradingService();
      try {
    	  PublishedAssessmentData publishedAssessment = bean.getPublishedAssessment();
    	  delegate.saveTotalScores(grading, publishedAssessment);
    	  StringBuffer logString = new StringBuffer();
    	  logString.append("gradedBy=");
          logString.append(AgentFacade.getAgentString());
    	  logString.append(", publishedAssessmentId=");
    	  logString.append(publishedAssessment.getPublishedAssessmentId());
    	  //Log details for each event
    	  for (AssessmentGradingData data: grading) { 
    		  eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE, "siteId=" + AgentFacade.getCurrentSiteId() + ", " + logString.toString(), AgentFacade.getCurrentSiteId(), true,  NotificationService.NOTI_OPTIONAL, SamigoLRSStatements.getStatementForTotalScoreUpdate(data, publishedAssessment)));
    	  }
    	  log.debug("Saved total scores.");
      } catch (GradebookServiceException ge) {
    	  FacesContext context = FacesContext.getCurrentInstance();
    	  String error=(String)ContextUtil.getLocalizedString(SamigoConstants.AUTHOR_BUNDLE, "gradebook_exception_error");
    	  context.addMessage(null, new FacesMessage(error));
    	  // scores are saved in Samigo, still return true, but display error to user.
    	  return true;
      }

      return true;
  }

  /**
    * "Apply This Score" is the only action in the 'Average Submission' view of the total page.
    * Ensure each user who hasn't yet submitted has one AssessmentGradingData object with the 'applyToUngraded' score
    * @param bean TotalScoresBean bean
    * @return true if successful or if the error message has been prepared within this method
    */
    private boolean saveTotalScoresAverage(TotalScoresBean bean)
    {
       // Get the grade to apply and ensure it is numeric
       Double ungradedScore;
       try
       {
		   String applyToUngraded = bean.getApplyToUngraded().trim();
           ungradedScore = Double.valueOf(applyToUngraded);
       }
       catch (Exception e)
       {
           FacesContext context = FacesContext.getCurrentInstance();
           String err2 = ContextUtil.getLocalizedString(SamigoConstants.EVAL_BUNDLE, "number_format_error_user_id_apply");
           context.addMessage(null, new FacesMessage(err2));
           return true;
       }

       // clear for future use
       bean.setApplyToUngraded("");

       // Find grade records in the DB that represent "No Submission"

       // Get all AssessmentGradingData objects from the db. Students with multiple submissions have multiple records, some students have none
       GradingService gradingService = new GradingService();
       List<AssessmentGradingData> agl = bean.getAssessmentGradingList();
       List<AssessmentGradingData> toUpdate = new ArrayList<>();                        // Grades that will need to be persisted
       HashMap<String, AssessmentGradingData> usersToGradingData = new HashMap<>();     // Maps users to their "No Submission" AssignmentGradeData records

       // Update all the "No Submission" objects' scores
       for (AssessmentGradingData agd : agl)
       {
           if (AssessmentGradingData.NO_SUBMISSION.equals(agd.getStatus()))
           {
               agd.setTotalOverrideScore(ungradedScore);
               agd.setFinalScore(ungradedScore);
               toUpdate.add(agd);
           }

           // Populate the map regardless of the status. This will be used to find users who have no records
           usersToGradingData.put(agd.getAgentId(), agd);
       }

       // Now create AssessmentGradingData objects for users who don't yet have one and are currently presented in bean
       List<AgentResults> agents = bean.getAllAgentsDirect();
       try
       {
           for (AgentResults ar : agents)
           {
               AssessmentGradingData agd = usersToGradingData.get(ar.getIdString());
               if (agd == null)
               {
                   // User does not have a grade record, so create one
                   AssessmentGradingData data = new AssessmentGradingData();
                   BeanUtils.copyProperties(data, ar);  // copy bean properties over
                   data.setAssessmentGradingId(null);   // copyProperties assigns a -1 to the gradingId (pk). Clear it with null so hibernate knows it's new
                   data.setAgentId(ar.getIdString());   // agentId was "N/A" in ar, this was copied to data. The actual agentId is in ar.getIdString(), so copy this over
                   data.setPublishedAssessmentId(bean.getPublishedAssessment().getPublishedAssessmentId());

                   if ("-".equals(ar.getTotalAutoScore()))
                   {
                       data.setTotalAutoScore(0d);
                   }
                   else
                   {
                       data.setTotalAutoScore(Double.valueOf(ar.getTotalAutoScore()));
                   }

                   data.setTotalOverrideScore(ungradedScore);
                   data.setFinalScore(ungradedScore);
                   data.setIsLate(ar.getIsLate());
                   data.setComments(ar.getComments());
                   data.setGradedBy(AgentFacade.getAgentString());
                   data.setGradedDate(new Date());
                   toUpdate.add(data);
                   usersToGradingData.put(ar.getIdString(), data);
               }
           }
       }
       catch (IllegalAccessException | InvocationTargetException e)
       {
           log.error("Error creating AssessmentGradingData: "+ e);
           return false;
       }

       // Persist the scores
       try
       {
           gradingService.saveTotalScores(toUpdate, bean.getPublishedAssessment());
           StringBuilder logString = new StringBuilder();
           logString.append("gradedBy=");
           logString.append(AgentFacade.getAgentString());
           logString.append(", publishedAssessmentId=");
           logString.append(bean.getPublishedAssessment().getPublishedAssessmentId());
           eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE,
                   "siteId=" + AgentFacade.getCurrentSiteId() + ", " + logString.toString(), true));
           log.debug("Saved total scores (average).");
       }
       catch (GradebookServiceException ge)
       {
           FacesContext context = FacesContext.getCurrentInstance();
           String error = ContextUtil.getLocalizedString(SamigoConstants.AUTHOR_BUNDLE, "gradebook_exception_error");
           context.addMessage(null, new FacesMessage(error));
       }

       // update bean for the presentation
       for (AgentResults ar : agents)
       {
           // Only change the presentation of scores for users who have in fact been updated
           String userId = ar.getIdString();
           AssessmentGradingData agd = usersToGradingData.get(userId);
           if (agd != null && toUpdate.contains(agd))
           {
               ar.setTotalOverrideScore(agd.getTotalOverrideScore() + "");
               ar.setFinalScore(agd.getFinalScore() + "");
           }
       }

       return true;
    }

  private boolean needUpdate(AgentResults agentResults, Map map, StringBuilder newScoreString, TotalScoresBean bean) throws NumberFormatException{
    boolean update = true;
    String newComments = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(agentResults.getComments());
    agentResults.setComments(newComments);
    log.debug("newComments = " + newComments);

    double totalAutoScore = 0; 
    if (agentResults.getTotalAutoScore()!=null && !("").equals(agentResults.getTotalAutoScore())){
      try{
        totalAutoScore = Double.valueOf(agentResults.getTotalAutoScore()).doubleValue();
      }
      catch (NumberFormatException e){
        totalAutoScore = 0;
      }
    }

    double totalOverrideScore = 0; 
    Boolean newIsLate = agentResults.getIsLate(); // if the duedate were postpond, we need to adjust this
    // we will check if there is change of grade. if so, add up new score
    // else skip
    AssessmentGradingData old = (AssessmentGradingData)map.get(agentResults.getAssessmentGradingId());
    if (old != null){
        if (agentResults.getTotalOverrideScore()!=null && !("").equals(agentResults.getTotalOverrideScore())){
        	try{
        		totalOverrideScore = Double.valueOf(agentResults.getTotalOverrideScore()).doubleValue();
        	}
        	catch (NumberFormatException e){
        		log.warn("Adj has wrong input type" + e);
        		throw e;
        	}
        }

      double newScore = totalAutoScore + totalOverrideScore;
      newScoreString.append(Double.valueOf(newScore));
	  double oldScore = 0;
      if (old.getFinalScore()!=null){
        oldScore = old.getFinalScore().doubleValue();
      }
      Boolean oldIsLate=old.getIsLate();
      
      String oldComments = old.getComments();
      log.debug("***oldScore = " + oldScore);
      log.debug("***newScore = " + newScore);
      log.debug("***oldIsLate = " + oldIsLate);
      log.debug("***newIsLate = " + newIsLate);
      log.debug("***oldComments = " + oldComments);
      log.debug("***newComments = " + newComments);
      if (Precision.equalsIncludingNaN(oldScore, newScore, 0.0001) && newIsLate.equals(oldIsLate) && 
    		  ((newComments!=null && newComments.equals(oldComments)) 
        		   || (newComments==null && oldComments==null)
        		   // following condition will happen when there is no comments (null) and user clicks on SubmissionId.
        		   // getComments() in AgentResults calls Validator.check(comments, "") so the null comment gets set to ""
        		   // there is nothing updated. update flag should be false
        		   || ((newComments!=null && newComments.equals("")) && oldComments==null)) 
        		   ) {
        update = false;
      }
      boolean attachUpdated = updateAttachment(old, agentResults);
      bean.setIsAnyAssessmentGradingAttachmentListModified(attachUpdated);
    }
    else{ // no assessmentGradingData exists
    	boolean noOverrideScore =  false;
    	boolean noComment =  false;
    	String score = agentResults.getTotalOverrideScore();
    	if (score != null) {
    		if (!("").equals(score.trim()) && !("-").equals(score.trim())) {
    			try{
    				totalOverrideScore = Double.valueOf(agentResults.getTotalOverrideScore()).doubleValue();
    				noOverrideScore = false;
    			}
    			catch (NumberFormatException e){
    				log.warn("Adj has wrong input type" + e);
    				throw e;
    			}
    		}
    		else {
    			noOverrideScore = true;
    			totalAutoScore = 0;
    		}
    	}
    	else {
    		noOverrideScore = true;
    		totalAutoScore = 0;
    	}
		double newScore = totalAutoScore + totalOverrideScore;
		newScoreString.append(Double.valueOf(newScore));
	    
    	if ("".equals(agentResults.getComments().trim()))
    		noComment = true;

    	if (noOverrideScore && noComment) 
    		update = false;
    }
        
    return update;
  }

  private boolean updateAttachment(AssessmentGradingData assessmentGradingData, AgentResults agentResults) {
	  List<AssessmentGradingAttachment> oldList = assessmentGradingData.getAssessmentGradingAttachmentList();
	  List<AssessmentGradingAttachment> newList = agentResults.getAssessmentGradingAttachmentList();
	  if ((oldList == null || oldList.size() == 0 ) && (newList == null || newList.size() == 0)) return false;
	  List<AssessmentGradingAttachment> attachmentList = new ArrayList<>();
	  Map<Long, AssessmentGradingAttachment> map = getAttachmentIdHash(oldList);
	  for (int i=0; i<newList.size(); i++){
		  AssessmentGradingAttachment assessmentGradingAttachment = (AssessmentGradingAttachment) newList.get(i);
		  if (map.get(assessmentGradingAttachment.getAttachmentId()) != null){
			  // exist already, remove it from map
			  map.remove(assessmentGradingAttachment.getAttachmentId());
		  }
		  else{
			  // new attachments
			  assessmentGradingAttachment.setAssessmentGrading(assessmentGradingData);
			  assessmentGradingAttachment.setAttachmentType(AttachmentIfc.ASSESSMENTGRADING_ATTACHMENT);
			  attachmentList.add(assessmentGradingAttachment);
		  }
	  }      
	  // save new ones
	  GradingService gradingService = new GradingService();
	  if (attachmentList.size() > 0) {
			gradingService.saveOrUpdateAttachments(attachmentList);
			eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE, 
					"siteId=" + AgentFacade.getCurrentSiteId() + ", Adding " + attachmentList.size() + " attachments for itemGradingData id = " + assessmentGradingData.getAssessmentGradingId(), 
					true));
		}

	  // remove old ones
	  Set set = map.keySet();
	  Iterator iter = set.iterator();
	  while (iter.hasNext()){
		  Long attachmentId = (Long)iter.next();
		  gradingService.removeAssessmentGradingAttachment(attachmentId.toString());
		  eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TOTAL_SCORE_UPDATE, 
				  "siteId=" + AgentFacade.getCurrentSiteId() + ", Removing attachmentId = " + attachmentId, true));
	  }
	  return true;
  	}
  	
  	private Map<Long, AssessmentGradingAttachment> getAttachmentIdHash(List<AssessmentGradingAttachment> list){
	    Map<Long, AssessmentGradingAttachment> map = new HashMap<>();
	    for (int i=0; i<list.size(); i++){
	    	AssessmentGradingAttachment a = list.get(i);
	      map.put(a.getAttachmentId(), a);
	    }
	    return map;
	}

}
