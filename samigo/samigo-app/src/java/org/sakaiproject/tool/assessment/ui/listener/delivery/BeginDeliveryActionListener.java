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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedFeedback;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.cms.CourseManagementBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SettingsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentThread;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class BeginDeliveryActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(BeginDeliveryActionListener.class);

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
    log.info("****DeliveryBean= "+delivery);
    String actionString = ContextUtil.lookupParam("actionString");
    if (actionString != null) {
      // if actionString is null, likely that action & actionString has been set already, 
      // e.g. take assessment via url, actionString is set by LoginServlet.
      // preview and take assessment is set by the parameter in the jsp pages
      delivery.setActionString(actionString);
    }
    int action = delivery.getActionMode();
    PublishedAssessmentFacade pub = getPublishedAssessmentBasedOnAction(action, delivery);
    
    if (DeliveryBean.REVIEW_ASSESSMENT == action && AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(pub.getStatus())) {
    	// Bug 1547: If this is during review and the assessment is retracted for edit now, 
    	// set the outcome to isRetractedForEdit2 error page.
    	delivery.setAssessmentTitle(pub.getTitle());
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

    // protocol = http://servername:8080/; deliverAudioRecording.jsp needs it
    delivery.setProtocol(ContextUtil.getProtocol());
    
    // set the published assessment
    delivery.setPublishedAssessment(pub);
    
    // populate backing bean from published assessment
    populateBeanFromPub(delivery, pub);

    // add in course management system info
    //CourseManagementBean course = (CourseManagementBean) ContextUtil.lookupBean("course");
    populateBeanFromCourse(pub, delivery);  //, course);

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

    // important: set feedbackOnDate last
    Date currentDate = new Date();
    if (component.getShowDateFeedback() && control.getFeedbackDate()!= null && currentDate.after(control.getFeedbackDate())) {
      delivery.setFeedbackOnDate(true); 
    }
    delivery.setAttachmentList(pubAssessment.getAssessmentAttachmentList());
  }

  /**
   * This takes the course information and puts it in the delivery
   * bean.  Just getting course and instructor for now, could be extended later.
   * @param delivery the delivery bean
   * @param course the course info bean
   */
  private void populateBeanFromCourse(PublishedAssessmentIfc pub, DeliveryBean delivery)
  //    CourseManagementBean course)
  {
    PublishedAssessmentService service = new PublishedAssessmentService();
    String ownerSiteId = service.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
    String ownerSiteName = AgentFacade.getSiteName(ownerSiteId);
    delivery.setCourseName(ownerSiteName);
    delivery.setInstructorName(AgentFacade.getDisplayNameByAgentId(pub.getCreatedBy()));
  }

  /**
   * This grabs the assessment feedback & puts it in the FeedbackComponent
   * @param feedback
   * @param pubAssessment
   */
  private FeedbackComponent populateFeedbackComponent(PublishedAssessmentFacade pubAssessment)
  {
    FeedbackComponent component = new FeedbackComponent();
    AssessmentFeedbackIfc info =  (AssessmentFeedbackIfc) pubAssessment.getAssessmentFeedback();
    if ( info != null) {
      component.setAssessmentFeedback(info);
    }
    return component;
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
    delivery.setDueDate(control.getDueDate());
    
    if (control.getMarkForReview() != null && (Integer.valueOf(1)).equals(control.getMarkForReview())) {
    	delivery.setDisplayMardForReview(true);
    }
    else {
    	delivery.setDisplayMardForReview(false);
    }

    // #1 - set submission remains
    int totalSubmissions = (service.getTotalSubmission(AgentFacade.getAgentString(),
        publishedAssessmentId.toString())).intValue();
    if (!(Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
      // when there are retaks, we always display 1 as number of remaining submission	
      int submissionsRemaining = control.getSubmissionsAllowed().intValue() - totalSubmissions;
      if (submissionsRemaining < 1) {
    	  submissionsRemaining = 1;
      }
      delivery.setSubmissionsRemaining(submissionsRemaining);
    }

    // #2 - check if TOC should be made avaliable
    if (control.getItemNavigation() == null)
      delivery.setNavigation(AssessmentAccessControl.RANDOM_ACCESS.toString());
    else
      delivery.setNavigation(control.getItemNavigation().toString());

    // #3 - if this is a timed assessment, set the time limit in hr, min & sec.
    setTimedAssessment(delivery, pubAssessment);

  }

  private void setTimedAssessment(DeliveryBean delivery, PublishedAssessmentIfc pubAssessment){

    AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();
    // check if we need to time the assessment, i.e.hasTimeassessment="true"
    String hasTimeLimit = pubAssessment.getAssessmentMetaDataByLabel("hasTimeAssessment");
    if (hasTimeLimit!=null && hasTimeLimit.equals("true")){
    	delivery.setHasTimeLimit(true);
    	delivery.setTimerId((new Date()).getTime()+"");

    	try {
    		if (control.getTimeLimit() != null) {
    			delivery.setTimeLimit(delivery.updateTimeLimit(control.getTimeLimit().toString()));
    			int seconds = control.getTimeLimit().intValue();
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
    		}
    	} catch (RuntimeException e)
    	{
    		delivery.setTimeLimit("");
    	}
    }
    else{
        delivery.setHasTimeLimit(false);
        delivery.setTimerId(null);
        delivery.setTimeLimit("0");
      }

  }

  public PublishedAssessmentFacade getPublishedAssessmentBasedOnAction(int action, DeliveryBean delivery){
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    String publishedId = ContextUtil.lookupParam("publishedId");
    String assessmentId = (String)ContextUtil.lookupParam("assessmentId");

    switch (action){
    case 2: // delivery.PREVIEW_ASSESSMENT
        // we would publish to create the publishedAssessment which we would use to populate
        // properties in delivery. However, for previewing, we do not need to keep this 
        // publishedAssessment record in DB at all, so we would delete it from DB right away.
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
          //log.info("****publishedId="+publishedId);
          //log.info("****clone publishedId="+pub.getPublishedAssessmentId());
          RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(publishedId, "preview");
          thread.start();
        } 
        catch (Exception e) {
          log.error(e);
          e.printStackTrace();
        }
        break;

    case 5: //delivery.TAKE_ASSESSMENT_VIA_URL:
        // this is accessed via publishedUrl so pubishedId==null
        pub = delivery.getPublishedAssessment();
        if (pub == null)
          throw new AbortProcessingException(
             "taking: publishedAsessmentId null or blank");
        else
          publishedId = pub.getPublishedAssessmentId().toString();
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

}
