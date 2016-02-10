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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SavePublishedSettingsListener
implements ActionListener
{
	private static Log log = LogFactory.getLog(SavePublishedSettingsListener.class);
	private static final GradebookServiceHelper gbsHelper =
		IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
		IntegrationContextFactory.getInstance().isIntegrated();
	private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
	private ResourceLoader rb= new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	
	private static String EXTENDED_TIME_KEY = "extendedTime";
	
	public SavePublishedSettingsListener()
	{
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException
	{
		FacesContext context = FacesContext.getCurrentInstance();
		PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean(
				"publishedSettings");
		// #1 - set Assessment
		Long assessmentId = assessmentSettings.getAssessmentId();
		log.debug("**** save assessment assessmentId ="+assessmentId.toString());
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(
				assessmentId.toString());

		boolean retractNow = false;
		String id = ae.getComponent().getId();
		// Check if the action is clicking the the "Retract" button on Assessment Retract Confirmation button
		if ("retract".equals(id)) {
			retractNow = true;
		}

		EventTrackingService.post(EventTrackingService.newEvent("sam.pubsetting.edit", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + assessmentId, true));
		boolean error = checkPublishedSettings(assessmentService, assessmentSettings, context);
		
		if (error){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}
		boolean isTitleChanged = isTitleChanged(assessmentSettings, assessment);
		boolean isScoringTypeChanged = isScoringTypeChanged(assessmentSettings, assessment);
		SaveAssessmentSettings saveAssessmentSettings = new SaveAssessmentSettings();
		setPublishedSettings(assessmentSettings, assessment, retractNow, saveAssessmentSettings);
		
		boolean gbError = checkScore(assessmentSettings, assessment, context);
		if (gbError){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}

		boolean gbUpdated = updateGB(assessmentSettings, assessment, isTitleChanged, isScoringTypeChanged, context);
		if (!gbUpdated){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}
		
		assessment.setLastModifiedBy(AgentFacade.getAgentString());
		assessment.setLastModifiedDate(new Date());
		assessmentService.saveAssessment(assessment); 
		
		// jj. save assessment first, then deal with ip
	    assessmentService.saveAssessment(assessment);
	    assessmentService.deleteAllSecuredIP(assessment);
	    // k. set ipAddresses
	    HashSet ipSet = new HashSet();
	    String ipAddresses = assessmentSettings.getIpAddresses();
	    if (ipAddresses == null)
	      ipAddresses = "";
	    
	    String[] ip = ipAddresses.split("\\n");
	    for (int j=0; j<ip.length;j++){
	      if (ip[j]!=null && !ip[j].equals("\r")) {
	    	  
	        ipSet.add(new PublishedSecuredIPAddress(assessment.getData(),null,ip[j]));
	      }
	    }
	    assessment.setSecuredIPAddressSet(ipSet);
	    
	    // k. secure delivery settings
	    SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.MODULE_KEY, assessmentSettings.getSecureDeliveryModule() );
	    String encryptedPassword = secureDeliveryService.encryptPassword( assessmentSettings.getSecureDeliveryModule(), assessmentSettings.getSecureDeliveryModuleExitPassword() );
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.EXITPWD_KEY, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, encryptedPassword ));
	    
	    // kk. remove the existing title decoration (if any) and then add the new one (if any)	    
	    String titleDecoration = assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.TITLE_DECORATION );
	    String newTitle;
	    if ( titleDecoration != null )
	    	newTitle = assessment.getTitle().replace( titleDecoration, "");
	    else
	    	newTitle = assessment.getTitle();
	    // getTitleDecoration() returns "" if null or NONE module is passed
	    titleDecoration = secureDeliveryService.getTitleDecoration( assessmentSettings.getSecureDeliveryModule(), new ResourceLoader().getLocale() );
	    if (titleDecoration != null && !titleDecoration.trim().equals("")) {
	    	newTitle = newTitle + " " + titleDecoration;
	    }
	    assessment.setTitle( newTitle );
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.TITLE_DECORATION, titleDecoration );

	    // Save Instructor Notification value
	    assessment.setInstructorNotification(Integer.valueOf(assessmentSettings.getInstructorNotification()));
	    
	    // l. FINALLY: save the assessment
	    assessmentService.saveAssessment(assessment);
	    
		saveAssessmentSettings.updateAttachment(assessment.getAssessmentAttachmentList(), assessmentSettings.getAttachmentList(),(AssessmentIfc)assessment.getData(), false);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubSetting.edit", "siteId=" + AgentFacade.getCurrentSiteId() + ", pubAssessmentId=" + assessmentSettings.getAssessmentId(), true));
	    
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		if ("editAssessment".equals(author.getFromPage())) {
			// If go back to edit assessment page, need to refresh the title
			AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
			assessmentBean.setTitle(assessmentSettings.getTitle());
		}
		else {
			resetPublishedAssessmentsList(author, assessmentService);
		}
		assessmentSettings.setOutcome(author.getFromPage());
		
		//update calendar event dates:
	    //need to add the calendar even back on the calendar if there already exists one (user opted to have it added to calendar)
	    boolean addDueDateToCalendar = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID) != null;
	    PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
	    PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
	    String notificationMessage = publishAssessmentListener.getNotificationMessage(publishRepublishNotification, assessmentSettings.getTitle(), assessmentSettings.getReleaseTo(), assessmentSettings.getStartDateString(), assessmentSettings.getPublishedUrl(),
				assessmentSettings.getReleaseToGroupsAsString(), assessmentSettings.getDueDateString(), assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(), 
				assessmentSettings.getUnlimitedSubmissions(), assessmentSettings.getSubmissionsAllowed(), assessmentSettings.getScoringType(), assessmentSettings.getFeedbackDelivery(), assessmentSettings.getFeedbackDateString());
	    calendarService.updateAllCalendarEvents(assessment, assessmentSettings.getReleaseTo(), assessmentSettings.getGroupsAuthorized(), rb.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);
	}

	public boolean checkPublishedSettings(PublishedAssessmentService assessmentService, PublishedAssessmentSettingsBean assessmentSettings, FacesContext context) {
		boolean error = false;
		// Title
		String assessmentName = assessmentSettings.getTitle();
		// check if name is empty
		if(assessmentName == null || (assessmentName.trim()).equals("")){
			String nameEmpty_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
			context.addMessage(null, new FacesMessage(nameEmpty_err));
			error=true;
		}
		else {
			assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentName.trim());
			// check if name is unique 
			if(!assessmentService.publishedAssessmentTitleIsUnique(assessmentSettings.getAssessmentId().toString(), assessmentName)){
				String nameUnique_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
				context.addMessage(null, new FacesMessage(nameUnique_err));
				error=true;
			}
		}

		// check if start date is valid
		if(!assessmentSettings.getIsValidStartDate()){
			String startDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_start_date");
			context.addMessage(null, new FacesMessage(startDateErr));
			error=true;
		}

		// check if due date is valid
		if(!assessmentSettings.getIsValidDueDate()){
			String dueDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_due_date");
			context.addMessage(null,new FacesMessage(dueDateErr));
			error=true;
		}
		// check if late submission date is valid
		if(!assessmentSettings.getIsValidRetractDate()){
			String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_retrack_date");
			context.addMessage(null,new FacesMessage(retractDateErr));
			error=true;
		}
		
		Date startDate = assessmentSettings.getStartDate();
	    Date dueDate = assessmentSettings.getDueDate();
	    Date retractDate = assessmentSettings.getRetractDate();
	    boolean isRetractEarlierThanAvaliable = false;
	    if ((dueDate != null && startDate != null && dueDate.before(startDate)) ||
	    	(dueDate != null && startDate == null && dueDate.before(new Date()))) {
	    	String dateError1 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","due_earlier_than_avaliable");
	    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError1, null));
	    	error=true;
	    	assessmentSettings.setStartDate(new Date());
	    }
	    if(assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling())){
		    if ((retractDate != null && startDate != null && retractDate.before(startDate)) ||
		    	(retractDate != null && startDate == null && retractDate.before(new Date()))) {
		    	String dateError2 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_earlier_than_avaliable");
		    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError2, null));
		    	error=true;
		    	isRetractEarlierThanAvaliable = true;
		    	assessmentSettings.setStartDate(new Date());
		    }
		    if (!isRetractEarlierThanAvaliable && (retractDate != null && dueDate != null && retractDate.before(dueDate))) {
		    	String dateError3 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_earlier_than_due");
		    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError3, null));
		    	error=true;
		    }
	    }

	    // SAM-1088
	    // if late submissions not allowed and late submission date is null, set late submission date to due date
	    if (assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.equals(assessmentSettings.getLateHandling()) &&
	    		retractDate == null && dueDate != null && assessmentSettings.getAutoSubmit()) {
	    	boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", false);
	    	if (autoSubmitEnabled) {
	    		assessmentSettings.setRetractDate(dueDate);
	    	}
	    }

	    // if auto-submit is enabled, make sure late submission date is set
	    if (assessmentSettings.getAutoSubmit() && retractDate == null) {
	    	boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", false);
	    	if (autoSubmitEnabled) {
	    		String dateError4 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_required_with_auto_submit");
	    		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError4, null));
	    		error=true;
	    	}
	    }
	    	    
		// if timed assessment, does it has value for time
		Object time = assessmentSettings.getValueMap().get("hasTimeAssessment");
		boolean isTime = false;
		try
		{
			if (time != null)
			{
				isTime = ((Boolean) time).booleanValue();
			}
		}
		catch (Exception ex)
		{
			// keep default
			log.warn("Expecting Boolean hasTimeAssessment, got: " + time + ", exception: " + ex);
		}
		if(isTime && (assessmentSettings.getTimeLimit().intValue())==0){
			String time_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "timeSelect_error");
			context.addMessage(null, new FacesMessage(time_err));
			error = true;
		}

		// check submissions
		String unlimitedSubmissions = assessmentSettings.getUnlimitedSubmissions();
		if (unlimitedSubmissions != null && unlimitedSubmissions.equals(AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString())) {
			try {
				String submissionsAllowed = assessmentSettings.getSubmissionsAllowed().trim();
				int submissionAllowed = Integer.parseInt(submissionsAllowed);
				if (submissionAllowed < 1) {
					throw new RuntimeException();
				}
			}
			catch (RuntimeException e){
				error=true;
				String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","submissions_allowed_error");
				context.addMessage(null,new FacesMessage(submission_err));
			}
		}

		String scoringType=assessmentSettings.getScoringType();
		if ((scoringType).equals(EvaluationModelIfc.AVERAGE_SCORE.toString()) && "0".equals(assessmentSettings.getUnlimitedSubmissions())) {
			try {
				String submissionsAllowed = assessmentSettings.getSubmissionsAllowed().trim();
				int submissionAllowed = Integer.parseInt(submissionsAllowed);
				if (submissionAllowed < 2) {
					throw new RuntimeException();
				}
			}
			catch (RuntimeException e){
				error=true;
				String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","averag_grading_single_submission");
				context.addMessage(null,new FacesMessage(submission_err));
			}
		}

		// check feedback - if at specific time then time should be defined.
		if((assessmentSettings.getFeedbackDelivery()).equals("2")) {
			if (assessmentSettings.getFeedbackDateString()==null || assessmentSettings.getFeedbackDateString().equals("")) {
				error=true;
				String  date_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
				context.addMessage(null,new FacesMessage(date_err));
			}
			else if(!assessmentSettings.getIsValidFeedbackDate()){
				String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_date");
				context.addMessage(null,new FacesMessage(feedbackDateErr));
				error=true;
			}
		}
		
		// check secure delivery exit password
		SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
		if ( secureDeliveryService.isSecureDeliveryAvaliable() ) {
			
			String moduleId = assessmentSettings.getSecureDeliveryModule();
			if ( ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
			
				String exitPassword = assessmentSettings.getSecureDeliveryModuleExitPassword(); 
				if ( exitPassword != null && exitPassword.length() > 0 ) {
					
					for ( int i = 0; i < exitPassword.length(); i++ ) {
						
						char c = exitPassword.charAt(i);
						if ( ! (( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) || ( c >= '0' && c <= '9' )) ) {
							error = true;
							String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","exit_password_error");
							context.addMessage(null,new FacesMessage(submission_err));
							break;
						}
					}					
				}
			}			
		}

		return error;
	}
	
	// Check if title has been changed. If yes, update it.
	private boolean isTitleChanged(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment) {
		if (assessment.getTitle() != null && assessmentSettings.getTitle() != null) {
			String assessmentTitle = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getTitle().trim());
				if (!assessment.getTitle().trim().equals(assessmentTitle)) {
					assessment.setTitle(assessmentTitle);
					return true;
				}
		}
		return false;
	}
	
	// Check if scoring type has been changed. 
	private boolean isScoringTypeChanged(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment) {
		if (assessment.getEvaluationModel() != null && assessment.getEvaluationModel().getScoringType() != null && assessmentSettings.getScoringType() != null) {
			Integer oldScoringType = assessment.getEvaluationModel().getScoringType();
			String newScoringType = assessmentSettings.getScoringType().trim();
			if (newScoringType.equals(oldScoringType.toString())) {
				return false;
			}
		}
		return true;
	}
	
	private void setPublishedSettings(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, boolean retractNow, SaveAssessmentSettings saveAssessmentSettings) {
		// Title is set in isTitleChanged()
		assessment.setDescription(assessmentSettings.getDescription());
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.AUTHORS, assessmentSettings.getAuthors());
	    
		PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
		if (control == null){
			control = new PublishedAccessControl();
			// need to fix accessControl so it can take AssessmentFacade later
			control.setAssessmentBase(assessment.getData());
		}
		// set startDate, dueDate, retractDate 
		control.setStartDate(assessmentSettings.getStartDate());
		control.setDueDate(assessmentSettings.getDueDate());
		if (retractNow)
		{
			control.setRetractDate(new Date());
		}
		else {
			control.setRetractDate(assessmentSettings.getRetractDate());
		}

		
		// set Assessment Orgainzation
		if (assessmentSettings.getItemNavigation()!=null ) {
			String nav = assessmentSettings.getItemNavigation();
			if ("1".equals(nav)) {
				assessmentSettings.setAssessmentFormat("1");
			}
			control.setItemNavigation(Integer.valueOf(nav));
		}
		if (assessmentSettings.getAssessmentFormat() != null ) {
			control.setAssessmentFormat(new Integer(assessmentSettings.getAssessmentFormat()));
		}	    
		if (assessmentSettings.getItemNumbering() != null) {
			control.setItemNumbering(new Integer(assessmentSettings.getItemNumbering()));
		}
		if (assessmentSettings.getDisplayScoreDuringAssessments() != null) {
			control.setDisplayScoreDuringAssessments(new Integer(assessmentSettings.getDisplayScoreDuringAssessments()));
		}
		
		// set Timed Assessment
		control.setTimeLimit(assessmentSettings.getTimeLimit());
		if (assessmentSettings.getTimedAssessment()) {
			control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
		}
		else {
			control.setTimedAssessment(AssessmentAccessControl.DO_NOT_TIMED_ASSESSMENT);
		}
		
		if (assessmentSettings.getIsMarkForReview())
	        control.setMarkForReview(AssessmentAccessControl.MARK_FOR_REVIEW);
	    else {
	    	control.setMarkForReview(AssessmentAccessControl.NOT_MARK_FOR_REVIEW);
	    }

		// set Submissions
		if (assessmentSettings.getUnlimitedSubmissions()!=null){
			if (!assessmentSettings.getUnlimitedSubmissions().
					equals(AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString())) {
				control.setUnlimitedSubmissions(Boolean.FALSE);
				if (assessmentSettings.getSubmissionsAllowed() != null)
					control.setSubmissionsAllowed(new Integer(assessmentSettings.
							getSubmissionsAllowed()));
				else
					control.setSubmissionsAllowed(Integer.valueOf("1"));
			}
			else {
				control.setUnlimitedSubmissions(Boolean.TRUE);
				control.setSubmissionsAllowed(null);
			}
		}

		if (assessmentSettings.getLateHandling()!=null){
			control.setLateHandling(new Integer(assessmentSettings.
					getLateHandling()));
		}
		if (assessmentSettings.getSubmissionsSaved()!=null){
			control.setSubmissionsSaved(new Integer(assessmentSettings.getSubmissionsSaved()));
		}
		
		if (assessmentSettings.getAutoSubmit())
	        control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
	    else {
	    	control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);
	    }
		assessment.setAssessmentAccessControl(control);

		// e. set Submission Messages
	    control.setSubmissionMessage(assessmentSettings.getSubmissionMessage());
	    // f. set username
	    control.setUsername(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, StringUtils.trim(assessmentSettings.getUsername())));
	    // g. set password
	    control.setPassword(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, StringUtils.trim(assessmentSettings.getPassword())));
	    // h. set finalPageUrl
	    String finalPageUrl = "";
	    if (assessmentSettings.getFinalPageUrl() != null) {
	    	finalPageUrl = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getFinalPageUrl().trim());
	    	if (finalPageUrl.length() != 0 && !finalPageUrl.toLowerCase().startsWith("http")) {
	    		finalPageUrl = "http://" + finalPageUrl;
	    	}
	    }
	    control.setFinalPageUrl(finalPageUrl);

		// set Feedback
		AssessmentFeedbackIfc feedback = (AssessmentFeedbackIfc) assessment.getAssessmentFeedback();
		if (feedback == null){
			feedback = new AssessmentFeedback();
			// need to fix feeback so it can take AssessmentFacade later
			feedback.setAssessmentBase(assessment.getData());
		}
		// Feedback authoring
		if (StringUtils.isNotBlank(assessmentSettings.getFeedbackAuthoring()))
			feedback.setFeedbackAuthoring(new Integer(assessmentSettings.getFeedbackAuthoring()));
		// Feedback delivery
		if (StringUtils.isNotBlank(assessmentSettings.getFeedbackDelivery()))
			feedback.setFeedbackDelivery(new Integer(assessmentSettings.getFeedbackDelivery()));
		if (StringUtils.isNotBlank(assessmentSettings.getFeedbackComponentOption()))
		    feedback.setFeedbackComponentOption(new Integer(assessmentSettings.getFeedbackComponentOption()));

		control.setFeedbackDate(assessmentSettings.getFeedbackDate());
		// Feedback Components Students Can See
		// if 'No feedback' (it corresponds to value 3) is selected, 
		// all components are unchecked
		 if (feedback.getFeedbackDelivery().equals(new Integer("3")))
		    {
		    	feedback.setShowQuestionText(false);
				feedback.setShowStudentResponse(false);
				feedback.setShowCorrectResponse(false);
				feedback.setShowStudentScore(false);
				feedback.setShowStudentQuestionScore(false);
				feedback.setShowQuestionLevelFeedback(false);
				feedback.setShowSelectionLevelFeedback(false);
				feedback.setShowGraderComments(false);
				feedback.setShowStatistics(false);
		    }
		    else {
		    		feedback.setShowQuestionText(Boolean.valueOf(assessmentSettings.getShowQuestionText()));
		    		feedback.setShowStudentResponse(Boolean.valueOf(assessmentSettings.getShowStudentResponse()));
		    		feedback.setShowCorrectResponse(Boolean.valueOf(assessmentSettings.getShowCorrectResponse()));
		    		feedback.setShowStudentScore(Boolean.valueOf(assessmentSettings.getShowStudentScore()));
		    		feedback.setShowStudentQuestionScore(Boolean.valueOf(assessmentSettings.getShowStudentQuestionScore()));
		    		feedback.setShowQuestionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowQuestionLevelFeedback()));
		    		feedback.setShowSelectionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowSelectionLevelFeedback()));
		    		feedback.setShowGraderComments(Boolean.valueOf(assessmentSettings.getShowGraderComments()));
		    		feedback.setShowStatistics(Boolean.valueOf(assessmentSettings.getShowStatistics()));
		    }
		assessment.setAssessmentFeedback(feedback);

		// set Grading
		EvaluationModelIfc evaluation = (EvaluationModelIfc) assessment.getEvaluationModel();
		if (evaluation == null){
			evaluation = new EvaluationModel();
			evaluation.setAssessmentBase(assessment.getData());
		}
		if (assessmentSettings.getAnonymousGrading()) {
			evaluation.setAnonymousGrading(Integer.valueOf(1));
		}
		else {
			evaluation.setAnonymousGrading(Integer.valueOf(2));
		}
	    
		// If there is value set for toDefaultGradebook, we reset it
		// Otherwise, do nothing
		if (assessmentSettings.getToDefaultGradebook()) {
			evaluation.setToGradeBook("1");
		}
		else {
			evaluation.setToGradeBook("2");
		}

		if (assessmentSettings.getScoringType() != null) {
			evaluation.setScoringType(new Integer(assessmentSettings.getScoringType()));
		}
		assessment.setEvaluationModel(evaluation);

		// update ValueMap: it contains value for thh checkboxes in
		// publishedSettings.jsp for: hasAvailableDate, hasDueDate,
		// hasRetractDate, hasAnonymous, hasAuthenticatedUser, hasIpAddress,
		// hasUsernamePassword, hasTimeAssessment,hasAutoSubmit, hasPartMetaData, 
		// hasQuestionMetaData
		HashMap h = assessmentSettings.getValueMap();
		h = addExtendedTimeValuesToMetaData(assessment, assessmentSettings);
		saveAssessmentSettings.updateMetaWithValueMap(assessment, h);
		
		// i. set Graphics
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGCOLOR, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getBgColor()));
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGIMAGE, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getBgImage()));

	    // j. set objectives,rubrics,keywords
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.KEYWORDS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getKeywords()));
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.OBJECTIVES, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getObjectives()));
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.RUBRICS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getRubrics()));
	}

	public boolean checkScore(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, FacesContext context) {
		// check if the score is > 0, Gradebook doesn't allow assessments with total
		// point = 0.
		boolean gbError = false;

		if (assessmentSettings.getToDefaultGradebook()) {
			if (assessment.getTotalScore().doubleValue() <= 0) {
				String gb_err = (String) ContextUtil.getLocalizedString(
						"org.sakaiproject.tool.assessment.bundle.AuthorMessages","gradebook_exception_min_points");
				context.addMessage(null, new FacesMessage(gb_err));
				gbError = true;
			}
		}
		return gbError;
	}

	public boolean updateGB(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, boolean isTitleChanged, boolean isScoringTypeChanged, FacesContext context) {
		//#3 - add or remove external assessment to gradebook
		// a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
		// b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
		//    exception are indication that the assessment is already in the Gradebook or there is nothing
		//    to remove.
		GradebookExternalAssessmentService g = null;
		if (integrated)
		{
			g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
			getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
		}

		if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)){ // => something to do
			PublishedEvaluationModel evaluation = (PublishedEvaluationModel)assessment.getEvaluationModel();
			//Integer scoringType = EvaluationModelIfc.HIGHEST_SCORE;
			if (evaluation == null){
				evaluation = new PublishedEvaluationModel();
				evaluation.setAssessmentBase(assessment.getData());
			}
			
			String assessmentName = "";
			boolean gbItemExists = false;
			try{
				assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(log, assessmentSettings.getTitle().trim());
				gbItemExists = gbsHelper.isAssignmentDefined(assessmentName, g);
				if (assessmentSettings.getToDefaultGradebook() && gbItemExists && isTitleChanged){
					String gbConflict_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
					context.addMessage(null,new FacesMessage(gbConflict_error));
					return false;
				}
			}
			catch(Exception e){
				log.warn("external assessment in GB has the same title:"+e.getMessage());
			}
			
			if (assessmentSettings.getToDefaultGradebook()) {
				evaluation.setToGradeBook("1");
			}
			else {
				evaluation.setToGradeBook("2");
			}

			// If the assessment is retracted for edit, we don't sync with gradebook (only until it is republished)
			if(AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS.equals(assessment.getStatus())) {
				return true;
			}
			Integer scoringType = evaluation.getScoringType();
			if (evaluation.getToGradeBook()!=null && 
					evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
				Long categoryId = null;
				if (isTitleChanged || isScoringTypeChanged) {
					// Because GB use title instead of id, we remove and re-add to GB if title changes.
					try {
						log.debug("before gbsHelper.removeGradebook()");
						categoryId = gbsHelper.getExternalAssessmentCategoryId(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), g);
						gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), g);
					} catch (Exception e1) {
						// Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
						log.info("Exception thrown in updateGB():" + e1.getMessage());
					}
				}
				
				if(gbItemExists && !(isTitleChanged || isScoringTypeChanged)){
					try {
						gbsHelper.updateGradebook(assessment, g);
					} catch (Exception e) {
						log.warn("Exception thrown in updateGB():" + e.getMessage());
					}
				}
				else{
					try{
						log.debug("before gbsHelper.addToGradebook()");
						gbsHelper.addToGradebook((PublishedAssessmentData)assessment.getData(), categoryId, g);

						// any score to copy over? get all the assessmentGradingData and copy over
						GradingService gradingService = new GradingService();

						// need to decide what to tell gradebook
						List list = null;

						if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
							list = gradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
						}
						else {
							list = gradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
						}

						//ArrayList list = gradingService.getAllSubmissions(assessment.getPublishedAssessmentId().toString());
						log.debug("list size =" + list.size()	);
						for (int i=0; i<list.size();i++){
							try {
								AssessmentGradingData ag = (AssessmentGradingData)list.get(i);
								log.debug("ag.scores " + ag.getTotalAutoScore());
								// Send the average score if average was selected for multiple submissions
								if (scoringType.equals(EvaluationModelIfc.AVERAGE_SCORE)) {							
									// status = 5: there is no submission but grader update something in the score page
									if(ag.getStatus() ==5) {
										ag.setFinalScore(ag.getFinalScore());
									} else {
										Double averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
										getAverageSubmittedAssessmentGrading(Long.valueOf(assessment.getPublishedAssessmentId()), ag.getAgentId());
										ag.setFinalScore(averageScore);
									}
								}
								gbsHelper.updateExternalAssessmentScore(ag, g);
							}
							catch (Exception e) {
								log.warn("Exception occues in " + i + "th record. Message:" + e.getMessage());
							}
						}
					}
					catch(Exception e){
						log.warn("oh well, must have been added already:"+e.getMessage());
					}
				}
			}
			else{ //remove
				try{
					gbsHelper.removeExternalAssessment(
							GradebookFacade.getGradebookUId(),
							assessment.getPublishedAssessmentId().toString(), g);
				}
				catch(Exception e){
					log.warn("*** oh well, looks like there is nothing to remove:"+e.getMessage());
				}
			}
		}
		return true;
	}

	public void resetPublishedAssessmentsList(AuthorBean author,
			PublishedAssessmentService assessmentService) {
		AuthorActionListener authorActionListener = new AuthorActionListener();
		GradingService gradingService = new GradingService();
		ArrayList publishedAssessmentList = assessmentService.getBasicInfoOfAllPublishedAssessments2(
				  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
		authorActionListener.prepareAllPublishedAssessmentsList(author, gradingService, publishedAssessmentList);
	}

	/**
	 * This will clear out the old extended time values and update them with new
	 * ones.
	 * 
	 * @param assessment
	 * @param assessmentSettings
	 * @return
	 */
	private HashMap addExtendedTimeValuesToMetaData(PublishedAssessmentFacade assessment,
			PublishedAssessmentSettingsBean assessmentSettings) {

		String[] allExtendedTimeEntries = assessmentSettings.getExtendedTimes().split("\\^");
		HashMap<String, String> metaDataMap = assessment.getAssessmentMetaDataMap();
		String metaKey = "";

		// clear out the old extended Time values
		int itemNum = 1;
		String extendedTimeData = assessment.getAssessmentMetaDataByLabel(EXTENDED_TIME_KEY + itemNum);
		while ((extendedTimeData != null) && (!extendedTimeData.equals(""))) {
			metaKey = EXTENDED_TIME_KEY + itemNum;
			metaDataMap.put(metaKey, ""); // set to empty string TODO: actually
											// delete it.
			extendedTimeData = assessment.getAssessmentMetaDataByLabel(EXTENDED_TIME_KEY + itemNum);
			itemNum++;
		}

		for (itemNum = 0; itemNum < allExtendedTimeEntries.length; itemNum++) {
			String extendedTimeEntry = allExtendedTimeEntries[itemNum];
			metaKey = "extendedTime" + (itemNum + 1);

			// Add in the new extended time values
			metaDataMap.put(metaKey, extendedTimeEntry);
		}

		return metaDataMap;
	}
}


