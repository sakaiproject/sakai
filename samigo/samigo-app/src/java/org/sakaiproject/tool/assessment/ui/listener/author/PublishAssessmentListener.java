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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoEmailService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PublishAssessmentListener
    implements ActionListener {


  private static Log log = LogFactory.getLog(PublishAssessmentListener.class);

  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();
  private static final Lock repeatedPublishLock = new ReentrantLock();
  private static boolean repeatedPublish = false;

  public PublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
	  repeatedPublishLock.lock();
	  try {

  		//FacesContext context = FacesContext.getCurrentInstance();
  		
  		UIComponent eventSource = (UIComponent) ae.getSource();
  		ValueBinding vb = eventSource.getValueBinding("value");
  		String buttonValue = (String) vb.getExpressionString(); 
  		if(buttonValue.endsWith(".button_unique_save_and_publish}"))
  		{
  			repeatedPublish = false;
  			return;
  		}
  		
  		if(!repeatedPublish)
  		{
  			//Map reqMap = context.getExternalContext().getRequestMap();
  			//Map requestParams = context.getExternalContext().getRequestParameterMap();
  			AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
  			"author");
  			
  			AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
  			
  			AssessmentService assessmentService = new AssessmentService();
  			
  			AssessmentFacade assessment = assessmentService.getAssessment(
  					assessmentSettings.getAssessmentId().toString());
  			
  			// 0. sorry need double checking assesmentTitle and everything
  			boolean error = checkTitle(assessment);
  			if (error){
  				return;
  			}
  			
  			
  			publish(assessment, assessmentSettings);

  			GradingService gradingService = new GradingService();
  			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
  			AuthorActionListener authorActionListener = new AuthorActionListener();
  			authorActionListener.prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
  			
  			repeatedPublish = true;
  		}
		} finally{
			repeatedPublishLock.unlock();

		}
  }

  private void publish(AssessmentFacade assessment,
                       AssessmentSettingsBean assessmentSettings) {
	PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    String releaseTo = null;
    try {
       pub = publishedAssessmentService.publishAssessment(assessment);
       releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
       PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
       boolean sendNotification = publishRepublishNotification.getSendNotification();

       if (sendNotification) {
    	   sendNotification(pub, publishedAssessmentService, publishRepublishNotification,
    			   assessmentSettings.getReleaseTo(), assessmentSettings.getTitle(), 
    			   assessmentSettings.getStartDateString(), assessmentSettings.getDueDateString(), assessmentSettings.getRetractDateString(),
    			   assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(), assessmentSettings.getUnlimitedSubmissions(),
    			   assessmentSettings.getSubmissionsAllowed(), assessmentSettings.getFeedbackDelivery(), assessmentSettings.getFeedbackDateString());
       }
       EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.publish", "assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));
    } catch (AssignmentHasIllegalPointsException gbe) {
       // Right now gradebook can only accept assessements with totalPoints > 0 
       // this  might change later
       log.warn(gbe);
        gbe.printStackTrace();
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_min_points");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(gbe);
    } catch (Exception e) {
        log.warn(e);
        e.printStackTrace();
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_error");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(e);
    }

    // let's check if we need a publishedUrl
    if (releaseTo != null) {
      // generate an alias to the pub assessment
      String alias = assessmentSettings.getAlias();
      PublishedMetaData meta = new PublishedMetaData(pub.getData(),
          AssessmentMetaDataIfc.ALIAS, alias);
      publishedAssessmentService.saveOrUpdateMetaData(meta);
    }
  }

  private boolean checkTitle(AssessmentFacade assessment){
    boolean error=false;
    String assessmentName = assessment.getTitle();
    AssessmentService assessmentService = new AssessmentService();
    String assessmentId = assessment.getAssessmentBaseId().toString();

    //#a - look for error: check if core assessment title is unique
    if (assessmentName!=null &&(assessmentName.trim()).equals("")){
      String publish_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","publish_error_message");
      FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
      error=true;
    }
    if (!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
      error=true;
      String nameUnique_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
      FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(nameUnique_err));
    }

    //#b - check if gradebook exist, if so, if assessment title already exists in GB
    GradebookService g = null;
    if (integrated){
      g = (GradebookService) SpringBeanLocator.getInstance().
           getBean("org.sakaiproject.service.gradebook.GradebookService");
    }
    String toGradebook = assessment.getEvaluationModel().getToGradeBook();
    try{
      if (toGradebook!=null && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString()) &&
          gbsHelper.isAssignmentDefined(assessmentName, g)){
        error=true;
        String gbConflict_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
        FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(gbConflict_error));
      }
    }
    catch(Exception e){
      log.warn("external assessment in GB has the same title:"+e.getMessage());
    }
    return error;
  }
  
  public void sendNotification(PublishedAssessmentFacade pub, PublishedAssessmentService service, PublishRepublishNotificationBean publishRepublishNotification,
		  String releaseTo, String title, String startDateString, String dueDateString, String retractDateString, 
		  Integer timedHours, Integer timedMinutes, String unlimitedSubmissions, String submissionsAllowed,
		  String feedbackDelivery, String feedbackDateString) {
	  TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
	  ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");

	  ArrayList<String> toEmailAddressList = new ArrayList<String>();
	  AgentFacade instructor = new AgentFacade();
	  //emailBean.setFromEmailAddress(instructor.getEmail());
	  toEmailAddressList.add(instructor.getEmail()); // send one copy to instructor

	  boolean groupRelease = AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS.equals(releaseTo);
	  if (groupRelease) {
		  totalScoresBean.setSelectedSectionFilterValue(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE);
	  }
	  else {
		  totalScoresBean.setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
	  }

	  totalScoresBean.setPublishedId(pub.getPublishedAssessmentId().toString());
	  Map useridMap= totalScoresBean.getUserIdMap(TotalScoresBean.CALLED_FROM_NOTIFICATION_LISTENER); 
	  AgentFacade agent = null;
	  Iterator iter = useridMap.keySet().iterator();
	  while (iter.hasNext()) {
		  String userUid = (String) iter.next();
		  agent = new AgentFacade(userUid);
		  toEmailAddressList.add(agent.getEmail());
	  }

	  String subject = publishRepublishNotification.getNotificationSubject();
	  String siteTitle = publishRepublishNotification.getSiteTitle();
	  String newline = "<br />\n";
	  StringBuilder message = new StringBuilder(rl.getString("notification_content_1"));
	  message.append(" \"");
	  message.append(siteTitle);
	  message.append("\" ");
	  message.append(rl.getString("notification_content_2"));
	  message.append(" (<a href=\"");
	  message.append(ServerConfigurationService.getPortalUrl());
	  message.append("\">");
	  message.append(ServerConfigurationService.getPortalUrl());
	  message.append("</a>)");
	  message.append(newline);
	  message.append(newline);

	  String prePopulateText = publishRepublishNotification.getPrePopulateText();
	  if (prePopulateText != null && !prePopulateText.trim().equals("") && 
		  (!prePopulateText.trim().equals(rl.getString("pre_populate_text_publish")) && 
		   !prePopulateText.trim().equals(rl.getString("pre_populate_text_republish")) && 
		   !prePopulateText.trim().equals(rl.getString("pre_populate_text_regrade_republish")))) {
		  message.append(prePopulateText);
		  message.append(newline);
		  message.append(newline);
	  }

	  message.append(title);
	  message.append(newline);
	  message.append(newline);

	  if (startDateString != null && !startDateString.trim().equals("")) {
		  message.append(rl.getString("available"));
		  message.append(" ");
		  message.append(startDateString);
	  }
	  else {
		  message.append(rl.getString("available_immediately"));
	  }
	  message.append(newline);
	  message.append(newline);

	  if (dueDateString != null && !dueDateString.trim().equals("")) {
		  message.append(rl.getString("due"));
		  message.append(" ");
		  message.append(dueDateString);
		  message.append(newline);
		  message.append(newline);
	  }

	  if (retractDateString != null && !retractDateString.trim().equals("")) {
		  message.append(rl.getString("retract"));
		  message.append(" ");
		  message.append(retractDateString);

		  message.append(newline);
		  message.append(newline);
	  }

	  if (timedHours > 0 || timedMinutes > 0) {
		  message.append(rl.getString("time_assessment_1"));
		  message.append(" ");
		  message.append(timedHours);
		  message.append(" ");
		  message.append(rl.getString("hours"));
		  if (timedMinutes > 0) {
			  message.append(", ");
			  message.append(timedMinutes);
			  message.append(" ");
			  message.append(rl.getString("minutes"));
		  }
		  message.append(". ");
		  message.append(rl.getString("time_assessment_2"));

		  message.append(newline);
		  message.append(newline);
	  }
	  else {
		  message.append(rl.getString("no_time_limit"));

		  message.append(newline);
		  message.append(newline);
	  }

	  if ("1".equals(unlimitedSubmissions)) {
		  message.append(rl.getString("unlimited_submission_allowed"));
	  }
	  else {
		  message.append(submissionsAllowed);
		  message.append(" ");
		  message.append(rl.getString("submission_allowed"));
	  }
	  message.append(newline);
	  message.append(newline);

	  if ("1".equals(feedbackDelivery)) {
		  message.append(rl.getString("immediate_feedback"));
	  }
	  else if ("4".equals(feedbackDelivery)) {
		  message.append(rl.getString("feedback_on_submission"));
	  }
	  else if ("3".equals(feedbackDelivery)) {
		  message.append(rl.getString("no_feedback_short"));
	  }
	  else {
		  message.append(rl.getString("feedback_available_on"));
		  message.append(" ");
		  message.append(feedbackDateString);
	  }
	  message.append(newline);

	  SamigoEmailService emailService = new SamigoEmailService(instructor.getEmail(), toEmailAddressList, "no", subject.toString(), message.toString());
	  emailService.sendMail();
  }
}
