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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
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
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class PublishAssessmentListener
    implements ActionListener {

  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();
  private static final Lock repeatedPublishLock = new ReentrantLock();
  private static boolean repeatedPublish = false;

  private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
  private ResourceLoader rl= new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
  
  public PublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
	  repeatedPublishLock.lock();
	  try {

  		//FacesContext context = FacesContext.getCurrentInstance();
  		if (ae == null) {
  			repeatedPublish = false;
  			return;
  		}
  		else {
  			UIComponent eventSource = (UIComponent) ae.getSource();
  			ValueBinding vb = eventSource.getValueBinding("value");
  			if (vb == null) {
  				repeatedPublish = false;
  				return;
  			}
  			else {
  				String buttonValue = (String) vb.getExpressionString(); 
  				if(buttonValue.endsWith(".button_unique_save_and_publish}"))
  				{
  					repeatedPublish = false;
  					return;
  				}
  			}
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

  			// Tell AuthorBean that we just published an assessment
  			// This will allow us to jump directly to published assessments tab
  			author.setJustPublishedAnAssessment(true);

  			//update any random draw questions from pool since they could have changed
  			int success = assessmentService.updateAllRandomPoolQuestions(assessment, true);
  			if(success == assessmentService.UPDATE_SUCCESS){

  				//grab new updated assessment
  				assessment = assessmentService.getAssessment(assessment.getAssessmentId().toString());	

  				publish(assessment, assessmentSettings);

  				GradingService gradingService = new GradingService();
  				PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
  				AuthorActionListener authorActionListener = new AuthorActionListener();
  				authorActionListener.prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);

  				repeatedPublish = true;
  			}else{
  				repeatedPublish = false;

  				FacesContext context = FacesContext.getCurrentInstance();
  				if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){  		    		
  					String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
  					context.addMessage(null,new FacesMessage(err));
  				}else{
  					String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
  					context.addMessage(null,new FacesMessage(err));
  				}

  				return;
  			}
  		}
	  } finally{
		  repeatedPublishLock.unlock();

	  }
  }

  private void publish(AssessmentFacade assessment,
                       AssessmentSettingsBean assessmentSettings) {
	PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;

    try {
       assessment.addAssessmentMetaData("ALIAS", assessmentSettings.getAlias());
       pub = publishedAssessmentService.publishAssessment(assessment);
       PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
       boolean sendNotification = publishRepublishNotification.getSendNotification();
       String subject = publishRepublishNotification.getNotificationSubject();
       String notificationMessage = getNotificationMessage(publishRepublishNotification, assessmentSettings.getTitle(), assessmentSettings.getReleaseTo(), assessmentSettings.getStartDateString(), assessmentSettings.getPublishedUrl(),
          		assessmentSettings.getReleaseToGroupsAsString(), assessmentSettings.getDueDateString(), assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(), 
          		assessmentSettings.getUnlimitedSubmissions(), assessmentSettings.getSubmissionsAllowed(), assessmentSettings.getScoringType(), assessmentSettings.getFeedbackDelivery(), assessmentSettings.getFeedbackDateString());
       
       if (sendNotification) {
    	   sendNotification(pub, publishedAssessmentService, subject, notificationMessage, 
    			   assessmentSettings.getReleaseTo());
       }

       ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
       extendedTimeFacade.copyEntriesToPub(pub.getData(), assessmentSettings.getExtendedTimes());

       EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_PUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));

		Iterator<PublishedSectionData> sectionDataIterator = pub.getSectionSet().iterator();
		while (sectionDataIterator.hasNext()){
			PublishedSectionData sectionData = sectionDataIterator.next();
			Iterator<ItemDataIfc> itemDataIfcIterator = sectionData.getItemSet().iterator();
			while (itemDataIfcIterator.hasNext()){
				ItemDataIfc itemDataIfc = itemDataIfcIterator.next();
				EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/publish, publishedItemId=" + itemDataIfc.getItemIdString(), true));
			}
		}

		//update Calendar Events
       boolean addDueDateToCalendar = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishAssessmentForm:calendarDueDate") != null;
       calendarService.updateAllCalendarEvents(pub, assessmentSettings.getReleaseTo(), assessmentSettings.getGroupsAuthorized(), rl.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);
    } catch (AssignmentHasIllegalPointsException gbe) {
       // Right now gradebook can only accept assessements with totalPoints > 0 
       // this  might change later
        log.warn(gbe.getMessage(), gbe);
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_min_points");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(gbe);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_error");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(e);
    }

    // Add ALIAS if it doesn't exist
    if ("".equals(assessment.getAssessmentMetaDataByLabel("ALIAS"))) {
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
    
    if (!assessmentService.assessmentTitleIsUnique(assessmentId, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentName), false)){
      error=true;
      String nameUnique_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
      FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(nameUnique_err));
    }

    //#b - check if gradebook exist, if so, if assessment title already exists in GB
    GradebookExternalAssessmentService g = null;
    if (integrated){
      g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
           getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
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
  
  public void sendNotification(PublishedAssessmentFacade pub, PublishedAssessmentService service, String subject, String message,
		  String releaseTo) {
	  TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
	  
	  AgentFacade instructor = new AgentFacade();
	  InternetAddress fromIA = null;
	  try {
		  fromIA = new InternetAddress(instructor.getEmail(), instructor.getDisplayName());
	  } catch (UnsupportedEncodingException e) {
		  log.warn("UnsupportedEncodingException encountered when constructing instructor's email.");
	  }

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

	  ArrayList<InternetAddress> toIAList = new ArrayList<>();
	  try {
		  toIAList.add(new InternetAddress(instructor.getEmail())); // send one copy to instructor
	  } catch (AddressException e) {
		  log.warn("AddressException encountered when constructing instructor's email.");
	  }
	  Iterator iter = useridMap.keySet().iterator();

	  while (iter.hasNext()) {
		  String userUid = (String) iter.next();
		  agent = new AgentFacade(userUid);
		  InternetAddress ia = null;
		  try {
			  ia = new InternetAddress(agent.getEmail()); 
		  } catch (AddressException e) {
			  log.warn("AddressException encountered when constructing toIAList email. userUid = " + userUid);
		  }
		  if (ia != null) {
			  toIAList.add(ia);
		  }
	  }
	  
	  InternetAddress[] toIA = new InternetAddress[toIAList.size()];
	  int count = 0;
	  Iterator iter2 = toIAList.iterator();
	  while (iter2.hasNext()) {
		  toIA[count++] = (InternetAddress) iter2.next();
	  }


	  String noReplyEmaillAddress =  ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName());
      InternetAddress[] noReply = new InternetAddress[1];
      try {
    	  noReply[0] = new InternetAddress(noReplyEmaillAddress);
      } catch (AddressException e) {
              log.warn("AddressException encountered when constructing no_reply@serverName email.");
      }
	  
	  List<String> headers = new  ArrayList<String>();
	  headers.add("Content-Type: text/html");
	  EmailService.sendMail(fromIA, toIA, subject, message, noReply, noReply, headers);
  }
  
  public String getNotificationMessage(PublishRepublishNotificationBean publishRepublishNotification, String title, String releaseTo, String startDateString, String publishedURL, String releaseToGroupsAsString, String dueDateString, Integer timedHours, Integer timedMinutes, String unlimitedSubmissions, String submissionsAllowed, String scoringType, String feedbackDelivery, String feedbackDateString){
	  String siteTitle = publishRepublishNotification.getSiteTitle();
	  if(siteTitle == null || "".equals(siteTitle)){
		  try {
			  Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			  siteTitle = site.getTitle();
			  publishRepublishNotification.setSiteTitle(siteTitle);
		  } catch (IdUnusedException iue) {
			  log.warn(iue.getMessage());
		  }
	  }
	  String newline = "<br />\n";
	  String bold_open = "<b>";
	  String bold_close = "</b>";
	  StringBuilder message = new StringBuilder();

	  String prePopulateText = publishRepublishNotification.getPrePopulateText();
	  if (prePopulateText != null && !prePopulateText.trim().equals("") && 
		  (!prePopulateText.trim().equals(rl.getString("pre_populate_text_publish")) && 
		   !prePopulateText.trim().equals(rl.getString("pre_populate_text_republish")) && 
		   !prePopulateText.trim().equals(rl.getString("pre_populate_text_regrade_republish")))) {
		  message.append(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(prePopulateText));
		  message.append(newline);
		  message.append(newline);
	  }

	  message.append("\"");
	  message.append(bold_open);
	  message.append(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(title));
	  message.append(bold_close);
	  message.append("\"");
	  message.append(" ");
	  
	  publishedURL = "<a target=\"_blank\" href=\"" + publishedURL + "\">" + publishedURL + "</a>";
	  if ("Anonymous Users".equals(releaseTo)) {
		  message.append(MessageFormat.format(rl.getString("available_anonymously_at"), startDateString, publishedURL));
	  }
	  else if (AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS.equals(releaseTo)) {
		  message.append(MessageFormat.format(rl.getString("available_group_at"), startDateString, releaseToGroupsAsString, publishedURL));
	  }
	  else {
		  message.append(MessageFormat.format(rl.getString("available_class_at"), startDateString, publishedURL));
	  }
	  
	  if (dueDateString != null && !dueDateString.trim().equals("")) {
		  message.append(newline);
		  message.append(newline);
		  message.append(MessageFormat.format(rl.getString("it_is_due"), dueDateString));
	  }
	  
	  message.append(newline);
	  message.append(newline);

	  // Time limited
	  if (timedHours > 0 || timedMinutes > 0) {
		  message.append(rl.getString("the_time_limit_is"));
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
		  message.append(rl.getString("submit_when_time_is_up"));
	  }
	  else {
		  message.append(rl.getString("there_is_no_time_limit"));
	  }

	  message.append(" ");
	  
	  // Number of submissions
	  if ("1".equals(unlimitedSubmissions)) {
		  message.append(rl.getString("student_submit_unlimited_times"));
	  }
	  else {
		  message.append(MessageFormat.format(rl.getString("student_submit_certain_time"), submissionsAllowed));
	  }

	  // Scoring type
	  message.append(" ");
	  if ("1".equals(scoringType)) {
		  message.append(rl.getString("record_highest"));
	  }
	  else if ("4".equals(scoringType)) {
		message.append(rl.getString("record_average"));
	  }
	  else {
		  message.append(rl.getString("record_last"));
	  }
	  
	  message.append(newline);
	  message.append(newline);

	  // Feedback
	  if ("1".equals(feedbackDelivery)) {
		  message.append(rl.getString("receive_immediate"));
	  }
	  else if ("4".equals(feedbackDelivery)) {
		  message.append(rl.getString("receive_feedback_on_submission"));
	  }
	  else if ("3".equals(feedbackDelivery)) {
		  message.append(rl.getString("receive_no_feedback"));
	  }
	  else {
		  message.append(MessageFormat.format(rl.getString("feedback_available_on"), feedbackDateString));
	  }
	  message.append(newline);
	  message.append(newline);
	  
	  StringBuffer siteTitleSb = new StringBuffer();
	  siteTitleSb.append(" \"");
	  siteTitleSb.append(siteTitle);
	  siteTitleSb.append("\" ");
	  StringBuffer portalUrlSb = new StringBuffer();
	  portalUrlSb.append(" <a href=\"");
	  portalUrlSb.append(ServerConfigurationService.getPortalUrl());
	  portalUrlSb.append("\" target=\"_blank\">");
	  portalUrlSb.append(ServerConfigurationService.getPortalUrl());
	  portalUrlSb.append("</a>");
	  message.append(MessageFormat.format(rl.getString("notification_content"), siteTitleSb.toString(), portalUrlSb.toString()));
	  
	  message.append(newline);
	  message.append(newline);
	  
	  return message.toString();
  }
}
