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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.List;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SettingsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentThread;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class BeginDeliveryActionListener implements ActionListener
{

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("BeginDeliveryActionListener.processAction() ");

    // get managed bean and set its action accordingly
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    log.debug("****DeliveryBean= "+delivery);
    String actionString = ContextUtil.lookupParam("actionString");
    String publishedId = ContextUtil.lookupParam("publishedId");
    String assessmentId = (String)ContextUtil.lookupParam("assessmentId");

    if (StringUtils.isNotBlank(actionString)) {
      // if actionString is null, likely that action & actionString has been set already, 
      // e.g. take assessment via url, actionString is set by LoginServlet.
      // preview and take assessment is set by the parameter in the jsp pages
      delivery.setActionString(actionString);
    }
    
    delivery.setDisplayFormat();
    
    if ("previewAssessment".equals(delivery.getActionString()) || "editAssessment".equals(actionString)) {
    	String isFromPrint = ContextUtil.lookupParam("isFromPrint");
        if (StringUtils.isNotBlank(isFromPrint)) {
    		delivery.setIsFromPrint(Boolean.parseBoolean(isFromPrint));
    	}
    }
    else {
    	delivery.setIsFromPrint(false);
    }
    
    int action = delivery.getActionMode();
    PublishedAssessmentFacade pub = getPublishedAssessmentBasedOnAction(action, delivery, assessmentId, publishedId);

    AssessmentAccessControlIfc control = pub.getAssessmentAccessControl();
    boolean releaseToAnonymous = control.getReleaseTo() != null && control.getReleaseTo().indexOf("Anonymous Users")> -1;

    if(pub == null){
    	delivery.setOutcome("poolUpdateError");
    	throw new AbortProcessingException("pub is null");
    }

    // Does the user have permission to take this action on this assessment in this site?
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (DeliveryBean.PREVIEW_ASSESSMENT == action) {
      if (StringUtils.isBlank(publishedId)) {
        if (!authzBean.isUserAllowedToEditAssessment(assessmentId, pub.getCreatedBy(), false)) {
          throw new IllegalArgumentException("User does not have permission to preview assessment id " + assessmentId);
        }
      }
      else {
        if (!authzBean.isUserAllowedToEditAssessment(publishedId, pub.getCreatedBy(), true)) {
          throw new IllegalArgumentException("User does not have permission to preview assessment id " + publishedId);
        }
      }
    }
    else if (DeliveryBean.REVIEW_ASSESSMENT == action || DeliveryBean.TAKE_ASSESSMENT == action) {
      if (!releaseToAnonymous && !authzBean.isUserAllowedToTakeAssessment(pub.getPublishedAssessmentId().toString())) {
        throw new IllegalArgumentException("User does not have permission to view assessment id " + pub.getPublishedAssessmentId());
      }
    }

    // Bug 1547: If this is during review and the assessment is retracted for edit now, 
	// set the outcome to isRetractedForEdit2 error page.
    if (DeliveryBean.REVIEW_ASSESSMENT == action && AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(pub.getStatus())) {
    	delivery.setAssessmentTitle(pub.getTitle());
        delivery.setHonorPledge(control.getHonorPledge());
    	delivery.setOutcome("isRetractedForEdit2");
    	return;
    }
    
    // reset DeliveryBean before begin
    ResetDeliveryListener reset = new ResetDeliveryListener();
    reset.processAction(null);

    // reset timer before begin
    /*
    delivery.setTimeElapse("0");
    delivery.setTimeElapseAfterFileUpload(null);
    delivery.setLastTimer(0);
    delivery.setTimeLimit("0");
    */
    delivery.setBeginAssessment(true);
    delivery.setTimeStamp((new Date()).getTime());
    delivery.setRedrawAnchorName("");
    
    // protocol = http://servername:8080/; deliverAudioRecording.jsp needs it
    delivery.setProtocol(ContextUtil.getProtocol());

    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext external = context.getExternalContext();
    String paramValue = ((Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX")).toString();
    Long sizeMax = null;
    float sizeMax_float = 0f;
    if (paramValue != null) {
    	sizeMax = Long.parseLong(paramValue);
    	sizeMax_float = sizeMax.floatValue()/1024;
    }
    delivery.setFileUploadSizeMax(Math.round(sizeMax_float));
    delivery.setPublishedAssessment(pub);
    
    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);
  }

  private PublishedAssessmentFacade lookupPublishedAssessment(String id,
    PublishedAssessmentService publishedAssessmentService
    )
  {
    PublishedAssessmentFacade pub;
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    pub = assessmentService.getPublishedAssessment(id);
    if (pub.getAssessmentFeedback()==null)
    {
      pub.setAssessmentFeedback(new PublishedFeedback());
    }
    return pub;
  }

  /**
   * This takes the published assessment information and puts it in the delivery
   * bean.  This is primarily the information that needs to be set up for the
   * begin assessment page.  Additional properties will be set when the student
   * elects to begin taking assessment.
   * @param delivery
   * @param pubAssessment
   */
  public void populateBeanFromPub(DeliveryBean delivery,
    PublishedAssessmentFacade pubAssessment)
  {
    AssessmentAccessControlIfc control = (AssessmentAccessControlIfc)pubAssessment.getAssessmentAccessControl();
    
    // populate deliveryBean, settingsBean and feedbackComponent .
    // deliveryBean contains settingsBean & feedbackComponent)
    populateDelivery(delivery, pubAssessment);

    SettingsDeliveryBean settings = populateSettings(pubAssessment);
    delivery.setSettings(settings);

    // feedback
    FeedbackComponent component = populateFeedbackComponent(pubAssessment);
    delivery.setFeedbackComponent(component);

    // feedback component option
    if (pubAssessment.getFeedbackComponentOption() != null) {
    	delivery.setFeedbackComponentOption(pubAssessment.getFeedbackComponentOption().toString());
    }
    else {
    	delivery.setFeedbackComponentOption("1");
    }
    
    // important: set feedbackOnDate last
    Date currentDate = new Date();
    if (component.getShowDateFeedback() && control.getFeedbackDate()!= null && currentDate.after(control.getFeedbackDate())) {
      delivery.setFeedbackOnDate(true); 
    }
    
    EvaluationModelIfc eval = (EvaluationModelIfc) pubAssessment.getEvaluationModel();
    delivery.setScoringType(eval.getScoringType());
    
    delivery.setAttachmentList(pubAssessment.getAssessmentAttachmentList());
  }

  /**
   * This grabs the assessment feedback & puts it in the FeedbackComponent
   * @param feedback
   * @param pubAssessment
   */
  private FeedbackComponent populateFeedbackComponent(PublishedAssessmentFacade pubAssessment)
  {
    FeedbackComponent component = new FeedbackComponent();
    AssessmentFeedbackIfc info =  pubAssessment.getAssessmentFeedback();
    if ( info != null) {
      component.setAssessmentFeedback(info);
    }
    return component;
  }

  public void populateSubmissionsRemaining(PublishedAssessmentService service, PublishedAssessmentIfc pubAssessment, DeliveryBean delivery) {
      AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();

      int totalSubmissions = service.getTotalSubmission(AgentFacade.getAgentString(), pubAssessment.getPublishedAssessmentId().toString());
      delivery.setTotalSubmissions(totalSubmissions);

      if (!(Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
        // when re-takes are allowed always display 1 as number of remaining submission
        int submissionsRemaining = control.getSubmissionsAllowed() - totalSubmissions;
        if (submissionsRemaining < 1) {
            submissionsRemaining = 1;
        }
        delivery.setSubmissionsRemaining(submissionsRemaining);
      }
  }
 
  /**
   * This grabs the assessment and its AssessmentAccessControlIfc &
   * puts it in the SettingsDeliveryBean.
   * @param settings
   * @param pubAssessment
   */
  private SettingsDeliveryBean populateSettings(PublishedAssessmentIfc pubAssessment)
  {
    // #1 - poplulate control properties such as dueDate, feedbackDate, autoSubmit, autoSave
    //      max. no. of attempt, display format, username & password - mostly info 
    //      on the BeginAssessment page. And deliveryBean contains settingsBean
    SettingsDeliveryBean settings = new SettingsDeliveryBean();
    settings.setAssessmentAccessControl(pubAssessment);
    return settings;
  }

  /**
   * Massage control settings into settings bean
   * @param settings target SettingsDeliveryBean
   * @param control the AssessmentAccessControlIfc
   */
  private void populateDelivery(DeliveryBean delivery, PublishedAssessmentIfc pubAssessment){

    Long publishedAssessmentId = pubAssessment.getPublishedAssessmentId();
    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    PublishedAssessmentService service = new PublishedAssessmentService();

    // #0 - global information
    delivery.setAssessmentId((pubAssessment.getPublishedAssessmentId()).toString());
    delivery.setAssessmentTitle(pubAssessment.getTitle());
    String instructorMessage = pubAssessment.getDescription();
    delivery.setInstructorMessage(instructorMessage);

    String ownerSiteId = service.getPublishedAssessmentOwner(pubAssessment.getPublishedAssessmentId());
    String ownerSiteName = AgentFacade.getSiteName(ownerSiteId);
    delivery.setCourseName(ownerSiteName);

    // for now instructor is the creator 'cos sakai don't have instructor role in 1.5
    delivery.setCreatorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setInstructorName(AgentFacade.getDisplayNameByAgentId(pubAssessment.getCreatedBy()));
    delivery.setSubmitted(false);
    delivery.setGraded(false);
    delivery.setPartIndex(0);
    delivery.setQuestionIndex(0);
    delivery.setBeginTime(null);
    delivery.setFeedbackOnDate(false);
		ExtendedTimeDeliveryService extTimeService = new ExtendedTimeDeliveryService(delivery.getPublishedAssessment());
		PublishedAssessmentFacade paFacade = delivery.getPublishedAssessment();

		if (extTimeService.hasExtendedTime()) {
			delivery.setDueDate(extTimeService.getDueDate());
			delivery.setRetractDate(extTimeService.getRetractDate());
			if (extTimeService.getTimeLimit() > 0)
				paFacade.setTimeLimit(extTimeService.getTimeLimit());
		} else {
			delivery.setDueDate(control.getDueDate());
			delivery.setRetractDate(control.getRetractDate());
		}
    if (control.getMarkForReview() != null && (Integer.valueOf(1)).equals(control.getMarkForReview())) {
    	delivery.setDisplayMardForReview(true);
    }
    else {
    	delivery.setDisplayMardForReview(false);
    }
    if (control.getHonorPledge() != null) delivery.setHonorPledge(control.getHonorPledge());

    // #1 - set submission remains
    populateSubmissionsRemaining(service, pubAssessment, delivery);

    // #2 - check if TOC should be made avaliable
    if (control.getItemNavigation() == null)
      delivery.setNavigation(AssessmentAccessControl.RANDOM_ACCESS.toString());
    else
      delivery.setNavigation(control.getItemNavigation().toString());

    
    GradingService gradingService = new GradingService ();
    List unSubmittedAssessmentGradingList = gradingService.getUnSubmittedAssessmentGradingDataList(publishedAssessmentId, AgentFacade.getAgentString());
    if (unSubmittedAssessmentGradingList.size() != 0){
    	delivery.setFirstTimeTaking(false);
    	AssessmentGradingData unSubmittedAssessmentGrading = (AssessmentGradingData) unSubmittedAssessmentGradingList.get(0);
    	setTimedAssessment(delivery, pubAssessment, extTimeService, unSubmittedAssessmentGrading);
    }  
    else {
    	delivery.setFirstTimeTaking(true);
    	setTimedAssessment(delivery, pubAssessment, extTimeService, null);
    }

    // #3 - if this is a timed assessment, set the time limit in hr, min & sec.
    delivery.setDeadline();
  }

  private void setTimedAssessment(DeliveryBean delivery, PublishedAssessmentIfc pubAssessment, ExtendedTimeDeliveryService extTimeService, AssessmentGradingData unSubmittedAssessmentGrading){

    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    // check if we need to time the assessment, i.e.hasTimeassessment="true"
    String hasTimeLimit = pubAssessment.getAssessmentMetaDataByLabel("hasTimeAssessment");

    //Override time limit settings if there's values in extended time
    if (extTimeService.hasExtendedTime()) {
    	if (extTimeService.getTimeLimit() > 0) {
    		control.setTimeLimit(extTimeService.getTimeLimit());
    		hasTimeLimit = "true";
    	}
    	else {
    		hasTimeLimit = "false";
    	}
    }
    
    if (hasTimeLimit!=null && hasTimeLimit.equals("true") && control.getTimeLimit() != null){

    	delivery.setHasTimeLimit(true);
    	delivery.setTimerId((new Date()).getTime()+"");

    	if (unSubmittedAssessmentGrading == null || unSubmittedAssessmentGrading.getAttemptDate() == null) {
    		try {
    			if (control.getTimeLimit() != null) {
    				Integer timeLimit = control.getTimeLimit();
    				if(timeLimit < 1) delivery.setHasTimeLimit(false); //TODO: figure out why I have to do this
    					delivery.setTimeLimit(delivery.updateTimeLimit(timeLimit.toString()));
    				int seconds = timeLimit;
    				int hour = 0;
    				int minute = 0;
    				if (seconds>=3600) {
    					hour = Math.abs(seconds/3600);
    					minute =Math.abs((seconds-hour*3600)/60);
    				}
    				else {
    					minute = Math.abs(seconds/60);
    				}
    				delivery.setTimeLimit_hour(hour);
    				delivery.setTimeLimit_minute(minute);
    				delivery.setTimeExpired(false);
    				StringBuilder sb = new StringBuilder();
    				ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
    				if (hour == 0) {
    					if (minute == 1) {
    						sb.append(minute).append(" ").append(rl.getString("time_limit_minute"));
    					}
    					else if (minute > 1) {
    						sb.append(minute).append(" ").append(rl.getString("time_limit_minutes"));
    					}
    				}
    				else if (hour == 1) {
    					if (minute == 0) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hour"));
    					}
    					else if (minute == 1) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hour")).append(" ").append(minute).append(" ").append(rl.getString("time_limit_minute"));
    					}
    					else if (minute > 1) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hour")).append(" ").append(minute).append(" ").append(rl.getString("time_limit_minutes"));
    					}
    				}
    				else if (hour > 1) {
    					if (minute == 0) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hours"));
    					}
    					else if (minute == 1) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hours")).append(" ").append(minute).append(" ").append(rl.getString("time_limit_minute"));
    					}
    					else if (minute > 1) {
    						sb.append(hour).append(" ").append(rl.getString("time_limit_hours")).append(" ").append(minute).append(" ").append(rl.getString("time_limit_minutes"));
    					}
    				}

    				delivery.setTimeLimitString(sb.toString());
    			}

    		} catch (RuntimeException e)
    		{
    			delivery.setTimeLimit("");
    		}
    	}
    	else {
    		if (extTimeService.hasExtendedTime() && extTimeService.getTimeLimit() > 0) {
    			control.setTimeLimit(extTimeService.getTimeLimit());
    		}
    		Date attemptDate = unSubmittedAssessmentGrading.getAttemptDate();
    		long timeLimitInSetting = control.getTimeLimit();
    		Long now = new Date().getTime();
    		Long start = attemptDate.getTime();
    		if((now - start) > (timeLimitInSetting*1000)) {
    			// check if the queue is ahead and already submitted it
    			TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    			TimedAssessmentGradingModel timedAG = queue.
    					get(unSubmittedAssessmentGrading.getAssessmentGradingId());
    			// if it was submitted (race condition) while checking, unblock it - sam will synch soon
    			if(timedAG != null && !timedAG.getSubmittedForGrade()) {
    				delivery.setTimeExpired(true);
    			}
    		}
    		delivery.setBeginTime(attemptDate);
    		String timeBeforeDueRetract = delivery.getTimeBeforeDueRetract(control.getTimeLimit() == null ? "0" : String.valueOf(control.getTimeLimit()));
    		delivery.setTimeLimit(timeBeforeDueRetract);
    		long adjustedTimedAssesmentDueDateLong  = attemptDate.getTime() + Long.parseLong(timeBeforeDueRetract) * 1000;
    		delivery.setAdjustedTimedAssesmentDueDate(new Date(adjustedTimedAssesmentDueDateLong));
    	}
    }
    else{
        delivery.setHasTimeLimit(false);
        delivery.setTimerId(null);
        delivery.setTimeLimit("0");
      }
  }

  private PublishedAssessmentFacade getPublishedAssessmentBasedOnAction(int action, DeliveryBean delivery, String assessmentId, String publishedId){
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;

    if (StringUtils.isBlank(assessmentId)) {
        assessmentId = delivery.getAssessmentId();
    }

    switch (action){
    case 2: // delivery.PREVIEW_ASSESSMENT
    	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    	if (author.getIsEditPendingAssessmentFlow()) {
    		// we would publish to create the publishedAssessment which we would use to populate
    		// properties in delivery. However, for previewing, we do not need to keep this 
    		// publishedAssessment record in DB at all, so we would delete it from DB right away.

    		int success = updateQuestionPoolQuestions(assessmentId, assessmentService);
    		if(success == AssessmentService.UPDATE_SUCCESS){
    			AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
    			try {
    				PublishedAssessmentFacade tempPub = publishedAssessmentService.publishPreviewAssessment(
    						assessment);
    				publishedId = tempPub.getPublishedAssessmentId().toString();
    				// clone pub from tempPub, clone is not in anyway bound to the DB session
    				pub = tempPub.clonePublishedAssessment();
    				//get list of resources attached to the published Assessment
    				List resourceIdList = assessmentService.getAssessmentResourceIdList(pub);
    				PersonBean personBean = (PersonBean) ContextUtil.lookupBean("person");
    				personBean.setResourceIdListInPreview(resourceIdList);
    				RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(publishedId, "preview");
    				thread.start();
    			} 
    			catch (Exception e) {
    				log.error(e.getMessage(), e);
    			}
    		}else{
    			FacesContext context = FacesContext.getCurrentInstance();
    			if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){
    				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
    				context.addMessage(null,new FacesMessage(err));
    			}else{
    				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
    				context.addMessage(null,new FacesMessage(err));
    			}
    			return null;
    		}

    	}
    	else {
    		pub = publishedAssessmentService.getPublishedAssessment(assessmentId);
    	}
        break;

    case 5: //delivery.TAKE_ASSESSMENT_VIA_URL:
        // this is accessed via publishedUrl so pubishedId==null
        pub = delivery.getPublishedAssessment();
        if (pub == null)
          throw new AbortProcessingException(
             "taking: publishedAssessmentId null or blank");
        //else
          //publishedId = pub.getPublishedAssessmentId().toString();
        break;

    case 1: //delivery.TAKE_ASSESSMENT
    case 3: //delivery.REVIEW_ASSESSMENT
        pub = lookupPublishedAssessment(publishedId, publishedAssessmentService);
        break;

    default: 
        break;
    }   
    return pub;
  }

  private int updateQuestionPoolQuestions(String assessmentId, AssessmentService assessmentService){
	  int success = assessmentService.updateAllRandomPoolQuestions(assessmentService.getAssessment(assessmentId));
	  if(success == AssessmentService.UPDATE_SUCCESS){
		  String fromEditStr = ContextUtil.lookupParam("fromEdit");
		  if(fromEditStr != null && "true".equals(fromEditStr)){
			  //since this is coming from the edit page, we need to update the bean information so it doesn't have stale data
			  AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
			  if(assessmentBean != null && assessmentBean.getSections() != null){
				  for(int i = 0; i < assessmentBean.getSections().size(); i++){
					  SectionContentsBean sectionBean = assessmentBean.getSections().get(i);
					  if((sectionBean.getSectionAuthorTypeString() != null)
							  && (sectionBean.getSectionAuthorTypeString().equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
									  .toString()))){
						  //this has been updated so we need to reset it
						  assessmentBean.getSections().set(i, new SectionContentsBean(assessmentService.getSection(sectionBean.getSectionId())));
					  }
				  }
			  }
		  }
	  }
	  return success;
  }

}
