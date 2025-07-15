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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.security.KeyStore.Entry;
import java.time.Instant;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.business.entity.SebConfig;
import org.sakaiproject.tool.assessment.business.entity.SebConfig.ConfigMode;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.SecureDeliverySeb;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.assessment.util.TimeLimitValidator;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class SavePublishedSettingsListener
implements ActionListener
{
	private static final GradebookServiceHelper gbsHelper =
		IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
		IntegrationContextFactory.getInstance().isIntegrated();
	private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	private final SamigoAvailableNotificationService samigoAvailableNotificationService = ComponentManager.get(SamigoAvailableNotificationService.class);
	private EventTrackingService eventTrackingService;

	public SavePublishedSettingsListener() {
		eventTrackingService = ComponentManager.get(EventTrackingService.class);	
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

		eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SETTING_EDIT, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + assessmentId, true));

		boolean error = checkPublishedSettings(assessmentService, assessmentSettings, context, retractNow);

		List<SelectItem> existingGradebook = assessmentSettings.getExistingGradebook();
		ToolSession currentToolSession = SessionManager.getCurrentToolSession();

		for (SelectItem item : existingGradebook) {
			if (!item.getLabel().contains(assessmentSettings.getTitle()) &&
				item.getLabel().split("\\(").length > 1 &&
				assessmentSettings.getGradebookName().equals(item.getValue()) &&
				currentToolSession.getAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED") == null) {
					error = true;
					String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "addtogradebook.previouslyAssoc");
					currentToolSession.setAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED", Boolean.TRUE);
					context.addMessage(null,new FacesMessage(err));
			}
		}

		if (error){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}

		//userNotification
		ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
		List<ExtendedTime> oldExtendedTimes = extendedTimeFacade.getEntriesForPub(assessment.getData());
		Date oldStartDate  = assessment.getAssessmentAccessControl().getStartDate();

		boolean isTitleChanged = isTitleChanged(assessmentSettings, assessment);
		boolean isScoringTypeChanged = isScoringTypeChanged(assessmentSettings, assessment);

		PublishedAssessmentFacade originalAssessment = (PublishedAssessmentFacade) SerializationUtils.clone(assessment);

		SaveAssessmentSettings saveAssessmentSettings = new SaveAssessmentSettings();
		setPublishedSettings(assessmentSettings, assessment, retractNow, saveAssessmentSettings);

		boolean gbError = checkScore(assessmentSettings, assessment, context);
		if (gbError){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}

		boolean gbUpdated = updateGB(assessmentSettings, assessment, originalAssessment, isTitleChanged, isScoringTypeChanged, context);
		if (!gbUpdated){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}

		currentToolSession.removeAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED");

		extendedTimeFacade.saveEntriesPub(assessmentService.getBasicInfoOfPublishedAssessment(assessmentId.toString()), assessmentSettings.getExtendedTimes());

		assessment.setLastModifiedBy(AgentFacade.getAgentString());
		assessment.setLastModifiedDate(new Date());
		assessmentService.saveAssessment(assessment); 

		// jj. save assessment first, then deal with ip
	    assessmentService.saveAssessment(assessment);
	    assessmentService.deleteAllSecuredIP(assessment);
	    // k. set ipAddresses
	    Set ipSet = new HashSet();
	    String ipAddresses = assessmentSettings.getIpAddresses().replace(" ", "");
	    if (ipAddresses == null)
	      ipAddresses = "";
	    
	    String[] ip = ipAddresses.split("\\n");
        for( String ip1 : ip )
        {
            if( ip1 != null && !ip1.equals( "\r" ) )
            {
                ipSet.add( new PublishedSecuredIPAddress( assessment.getData(), null, ip1 ) );
            }
        }
	    assessment.setSecuredIPAddressSet(ipSet);
	    
	    // k. secure delivery settings
	    SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.MODULE_KEY, assessmentSettings.getSecureDeliveryModule() );
	    String encryptedPassword = secureDeliveryService.encryptPassword( assessmentSettings.getSecureDeliveryModule(), assessmentSettings.getSecureDeliveryModuleExitPassword() );
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.EXITPWD_KEY, encryptedPassword);

        if (SecureDeliverySeb.MODULE_NAME.equals(assessmentSettings.getSecureDeliveryModule())) {
            ConfigMode sebConfigMode = ConfigMode.valueOf(assessmentSettings.getSebConfigMode());
            switch(sebConfigMode) {
                case UPLOAD:
                case CLIENT:
                    assessment.updateAssessmentMetaData(SebConfig.CONFIG_KEY, assessmentSettings.getSebConfigKey());
                    assessment.updateAssessmentMetaData(SebConfig.EXAM_KEYS, assessmentSettings.getSebExamKeys());
                    break;
                case MANUAL:
                    break;
                default:
                    log.error("Unhandled value of seb config mode [{}]", sebConfigMode);
                    break;
            }
        }

	    // kk. remove the existing title decoration (if any) and then add the new one (if any)	    
	    String titleDecoration = assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.TITLE_DECORATION );
	    String newTitle = StringUtils.isNotEmpty(titleDecoration) && !"NONE".equals(titleDecoration)
	            ? StringUtils.replace(assessment.getTitle(), " " + titleDecoration, "")
	            : assessment.getTitle();
	    titleDecoration = secureDeliveryService.getTitleDecoration( assessmentSettings.getSecureDeliveryModule(), new ResourceLoader().getLocale() );
	    if (titleDecoration != null && !titleDecoration.trim().equals("")) {
	    	newTitle = newTitle + " " + titleDecoration;
	    }
	    assessment.setTitle( newTitle );
	    assessment.updateAssessmentMetaData(SecureDeliveryServiceAPI.TITLE_DECORATION, titleDecoration );

	    // Save Instructor Notification value
	    try
	    {
	        assessment.setInstructorNotification(Integer.valueOf(assessmentSettings.getInstructorNotification()));
	    }
	    catch( NullPointerException | NumberFormatException ex )
	    {
	        log.warn(ex.getMessage(), ex);
	        assessment.setInstructorNotification( SamigoConstants.NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT );
	    }

		postUserNotification(assessmentSettings, assessment ,  oldExtendedTimes,  oldStartDate);

	    // l. FINALLY: save the assessment
	    assessmentService.saveAssessment(assessment);
	    
		saveAssessmentSettings.updateAttachment(assessment.getAssessmentAttachmentList(), assessmentSettings.getAttachmentList(),(AssessmentIfc)assessment.getData(), false);
		eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SETTING_EDIT, "siteId=" + AgentFacade.getCurrentSiteId() + ", pubAssessmentId=" + assessmentSettings.getAssessmentId(), true));
	    
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		if ("editAssessment".equals(author.getFromPage())) {
			// If go back to edit assessment page, need to refresh the title
			AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
			assessmentBean.setTitle(assessmentSettings.getTitle());
		}
		else {
			resetPublishedAssessmentsList(author, authorization, assessmentService);
		}
		assessmentSettings.setOutcome(author.getFromPage());
		
		//update calendar event dates:
		//need to add the calendar even back on the calendar if there already exists one (user opted to have it added to calendar)
		boolean addDueDateToCalendar = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID) != null;
		PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
		PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
		String notificationMessage = publishAssessmentListener.getNotificationMessage(publishRepublishNotification, assessmentSettings.getTitle(), assessmentSettings.getReleaseTo(), 
				assessmentSettings.getStartDateString(), assessmentSettings.getPublishedUrl(), assessmentSettings.getDueDateString(), assessmentSettings.getTimedHours(),
				assessmentSettings.getTimedMinutes(), assessmentSettings.getUnlimitedSubmissions(), assessmentSettings.getSubmissionsAllowed(), assessmentSettings.getScoringType(),
				assessmentSettings.getFeedbackDelivery(), assessmentSettings.getFeedbackDateString(), assessmentSettings.getFeedbackEndDateString(), assessmentSettings.getFeedbackScoreThreshold(),
				assessmentSettings.getAutoSubmit(), assessmentSettings.getLateHandling(), assessmentSettings.getRetractDateString());
		calendarService.updateAllCalendarEvents(assessment, assessmentSettings.getReleaseTo(), assessmentSettings.getGroupsAuthorized(), rb.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);
		// Update scheduled assessment available notification
		samigoAvailableNotificationService.rescheduleAssessmentAvailableNotification(String.valueOf(assessmentId));
	}


	private void postUserNotification(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, List<ExtendedTime> oldExtendedTimes, Date oldStartDate) {

		if (!Objects.equals(assessment.getStatus(), AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS)) {
			Date newStartDate = assessmentSettings.getStartDate();
			boolean flag = false;

			if (!newStartDate.equals(oldStartDate)) {
				eventTrackingService.cancelDelays("siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), SamigoConstants.EVENT_ASSESSMENT_AVAILABLE);
				if (newStartDate.toInstant().isAfter(Instant.now())) {
					eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessmentSettings.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), newStartDate.toInstant());
					//delete existing alerts
					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_DELETE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
				} else {
					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
				}
				flag = true;
			}
			eventTrackingService.cancelDelays("siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),SamigoConstants.EVENT_ASSESSMENT_AVAILABLE);
			if (assessmentSettings.getExtendedTimesSize() != oldExtendedTimes.size()) {
				if (!flag) {
					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
					flag = true;
				}
                for (ExtendedTime newExTime : assessmentSettings.getExtendedTimes()) {
                    if (newExTime.getStartDate().toInstant().isAfter(Instant.now())) {
                        eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessmentSettings.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), newExTime.getStartDate().toInstant());
                    }
                }
			} else {
				ListIterator<ExtendedTime> oldtimes = oldExtendedTimes.listIterator();
				ListIterator<ExtendedTime> newtimes = assessmentSettings.getExtendedTimes().listIterator();
				while (oldtimes.hasNext()) {
					ExtendedTime oldExTime = (ExtendedTime) oldtimes.next();
					while (newtimes.hasNext()) {
						ExtendedTime newExTime = (ExtendedTime) newtimes.next();
						if (!newExTime.equals(oldExTime)) {
							if (!flag) {
								eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
								flag = true;
							}
							if (newExTime.getStartDate().toInstant().isAfter(Instant.now())) {
								eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessmentSettings.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), newExTime.getStartDate().toInstant());
							}
						}
					}
				}
			}
		}
	}

	public boolean checkPublishedSettings(PublishedAssessmentService assessmentService, PublishedAssessmentSettingsBean assessmentSettings, FacesContext context, boolean retractNow) {
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
			assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentName.trim());
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
		final Date dueDate = assessmentSettings.getDueDate();
		final Date retractDate = assessmentSettings.getRetractDate();
		final boolean isAcceptingLateSubmissions = assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling());
		boolean isRetractEarlierThanAvaliable = false;

	    if ((dueDate != null && startDate != null && dueDate.before(startDate)) ||
	    	(dueDate != null && startDate == null && dueDate.before(new Date()))) {
	    	String dateError1 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","due_earlier_than_avaliable");
	    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError1, null));
	    	error=true;
	    	assessmentSettings.setStartDate(new Date());
	    }
	    if (isAcceptingLateSubmissions) {
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

        // if due date is null we cannot have late submissions
        if (dueDate == null && isAcceptingLateSubmissions && retractDate != null) {
            String noDueDate = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","due_null_with_retract_date");
            context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, noDueDate, null));
            error=true;
            
        }

		// if using a time limit, ensure open window is greater than or equal to time limit
		boolean hasTimer = TimeLimitValidator.hasTimer(assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes());
		if(hasTimer) {
			Date due = assessmentSettings.getRetractDate() != null && isAcceptingLateSubmissions ? assessmentSettings.getRetractDate() : assessmentSettings.getDueDate();
			boolean availableLongerThanTimer = TimeLimitValidator.availableLongerThanTimer(startDate, due, assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(),
																							"org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "open_window_less_than_time_limit", context);
			if(!availableLongerThanTimer) {
				error = true;
			}
		}

		// If auto-submit is enabled, make sure there is either a due date or late acceptance date set (depending on the late handling setting)
		if (assessmentSettings.getAutoSubmit()) {
			boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", true);

			// If late submissions not allowed, set late submission date to due date
			if (assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) &&
					dueDate != null && autoSubmitEnabled) {
				assessmentSettings.setRetractDate(dueDate);
			}

			// If late submissions not allowed and due date is null, throw error
			if (assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) &&
					dueDate == null && autoSubmitEnabled) {
				String dateError4 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "due_required_with_auto_submit");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, dateError4, null));
				error = true;
			}

			// If late submissions are allowed and late submission date is null, throw error
			if (assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) &&
					retractDate == null && autoSubmitEnabled) {
				String dateError4 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "retract_required_with_auto_submit");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, dateError4, null));
				error = true;
			}
		}

	    // if auto-submit and late-submissions are disabled Set retract date to null
	    if ( !assessmentSettings.getAutoSubmit() && retractDate != null && 
	        assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling())){
	        assessmentSettings.setRetractDate(null);
	    }
	    	    
		// if timed assessment, does it has value for time
		Object time = assessmentSettings.getValueMap().get("hasTimeAssessment");
		boolean isTime = false;
		try
		{
			if (time != null) {
				if (time instanceof String) {
					String timeStr = time.toString();
					if ("true".equals(timeStr)) {
						isTime = true;
					} else if ("false".equals(timeStr)) {
						isTime = false;
					}
				} else {
					isTime = ( (Boolean) time).booleanValue();
				}
			}
		}
		catch (Exception ex)
		{
			// keep default
			log.warn("Expecting Boolean or String true/false for hasTimeAssessment, got: " + time + ", exception: " + ex.getMessage());
		}
		if(isTime && (assessmentSettings.getTimeLimit())==0){
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
			if (StringUtils.isBlank(assessmentSettings.getFeedbackDateString())) {
				error=true;
				String  date_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
				context.addMessage(null,new FacesMessage(date_err));
			}
			else {
				if(StringUtils.isNotBlank(assessmentSettings.getFeedbackEndDateString()) && assessmentSettings.getFeedbackDate().after(assessmentSettings.getFeedbackEndDate())){
					String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_ranges");
					context.addMessage(null,new FacesMessage(feedbackDateErr));
					error=true;
				}
			}

			if(!assessmentSettings.getIsValidFeedbackDate()){
				String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_date");
				context.addMessage(null,new FacesMessage(feedbackDateErr));
				error=true;
			}

			boolean scoreThresholdEnabled = assessmentSettings.getFeedbackScoreThresholdEnabled();
			//Check if the value is empty
			boolean scoreThresholdError = StringUtils.isBlank(assessmentSettings.getFeedbackScoreThreshold());
			//If the threshold value is not empty, check if is a valid percentage
			if (!scoreThresholdError) {
				String submittedScoreThreshold = StringUtils.replace(assessmentSettings.getFeedbackScoreThreshold(), ",", ".");
				try {
					Double doubleInput = new Double(submittedScoreThreshold);
					if(doubleInput.compareTo(new Double("0.0")) == -1 || doubleInput.compareTo(new Double("100.0")) == 1){
						throw new Exception();
					}
				} catch(Exception ex) {
					scoreThresholdError = true;
				}
			}
			//If the threshold is enabled and is not valid, display an error.
			if(scoreThresholdEnabled && scoreThresholdError){
				error = true;
				String str_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","feedback_score_threshold_required");
				context.addMessage(null,new FacesMessage(str_err));
			}
		}

		org.sakaiproject.grading.api.GradingService gradingService =
			(org.sakaiproject.grading.api.GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");

		boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());
		boolean isReleaseToSelectedGroups = assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);

		if (isGradebookGroupEnabled && !isReleaseToSelectedGroups) {
			error = true;

			String categoriesInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.release_to.error");
			context.addMessage(null,new FacesMessage(categoriesInGroups));
		}

		String[] groupsAuthorized =  assessmentSettings.getGroupsAuthorized();

		List<String> groupList = Arrays.asList(groupsAuthorized);

		String siteId = assessmentSettings.getCurrentSiteId();
		String defaultToGradebook = assessmentSettings.getToDefaultGradebook();

		if (defaultToGradebook != null && isGradebookGroupEnabled) {
			if (defaultToGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
				String categorySelected = assessmentSettings.getCategorySelected();

				if (categorySelected != null && !categorySelected.isBlank() && !categorySelected.equals("-1")) {
					List<String> selectedCategories = Arrays.asList(categorySelected.split(","));

					boolean areCategoriesInGroups =
						gradingService.checkMultiSelectorList(siteId, groupList != null ? groupList : new ArrayList<>(), selectedCategories, true);

					if (!areCategoriesInGroups) {
						error = true;
						String categoriesInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.categories.error");

						context.addMessage(null,new FacesMessage(categoriesInGroups));
					}
				}
			} else if (defaultToGradebook.equals(EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString())) {
				String gradebookName = assessmentSettings.getGradebookName();
				List<String> gradebookList = Arrays.asList(gradebookName.split(","));

				boolean areItemsInGroups =
					gradingService.checkMultiSelectorList(siteId, groupList != null ? groupList : new ArrayList<>(), gradebookList, false);

				if (!areItemsInGroups) {
					error = true;
					String itemsInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.items.error");

					context.addMessage(null,new FacesMessage(itemsInGroups));
				}
			}
		}

		return error;
	}

	// Check if title has been changed. If yes, update it.
	private boolean isTitleChanged(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment) {
		if (assessment.getTitle() != null && assessmentSettings.getTitle() != null) {
			String assessmentTitle = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getTitle().trim());
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

		Integer lateHandling = assessmentSettings.getLateHandling() != null ? new Integer(assessmentSettings.getLateHandling()) : -1;
		if (lateHandling > 0) {
			control.setLateHandling(lateHandling);
		}

		if (retractNow && lateHandling.equals(AssessmentAccessControl.ACCEPT_LATE_SUBMISSION)) {
			control.setRetractDate(new Date());
			if (assessmentSettings.getDueDate() != null && assessmentSettings.getDueDate().after(new Date())) {
				control.setDueDate(new Date());
			}
		}
		else if (retractNow) {
			assessmentSettings.setLateHandling(AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString());
			control.setDueDate(new Date());
			control.setRetractDate(new Date());
		}
		else if ("".equals(assessmentSettings.getRetractDateString())) {
			control.setRetractDate(null);
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
		if (StringUtils.isNotBlank(assessmentSettings.getDisplayScoreDuringAssessments())) {
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

		control.setHonorPledge(assessmentSettings.isHonorPledge());

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

		if (assessmentSettings.getInstructorNotification() != null){
			try
			{
				control.setInstructorNotification(new Integer(assessmentSettings.getInstructorNotification()));
			}
			catch( NumberFormatException ex )
			{
				log.warn(ex.getMessage(), ex);
				assessment.setInstructorNotification( SamigoConstants.NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT );
			}
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
	    // g. set password
	    control.setPassword(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(StringUtils.trim(assessmentSettings.getPassword())));
	    // h. set finalPageUrl
	    String finalPageUrl = "";
	    if (assessmentSettings.getFinalPageUrl() != null) {
	    	finalPageUrl = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getFinalPageUrl().trim());
	    	if (finalPageUrl.length() != 0 && !finalPageUrl.toLowerCase().startsWith("http")) {
	    		finalPageUrl = "http://" + finalPageUrl;
	    	}
	    }
	    control.setFinalPageUrl(finalPageUrl);

		// set Feedback
		AssessmentFeedbackIfc feedback = assessment.getAssessmentFeedback();
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
		control.setFeedbackEndDate(assessmentSettings.getFeedbackEndDate());
		//Set the value if the checkbox is selected, wipe the value otherwise.
		String feedbackScoreThreshold = StringUtils.replace(assessmentSettings.getFeedbackScoreThreshold(), ",", ".");
		control.setFeedbackScoreThreshold(assessmentSettings.getFeedbackScoreThresholdEnabled() ? new Double(feedbackScoreThreshold) : null);
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
				feedback.setShowCorrection(false);
		    }
		    else {
		    		feedback.setShowQuestionText(assessmentSettings.getShowQuestionText());
		    		feedback.setShowStudentResponse(assessmentSettings.getShowStudentResponse());
		    		feedback.setShowCorrectResponse(assessmentSettings.getShowCorrectResponse());
		    		feedback.setShowStudentScore(assessmentSettings.getShowStudentScore());
		    		feedback.setShowStudentQuestionScore(assessmentSettings.getShowStudentQuestionScore());
		    		feedback.setShowQuestionLevelFeedback(assessmentSettings.getShowQuestionLevelFeedback());
		    		feedback.setShowSelectionLevelFeedback(assessmentSettings.getShowSelectionLevelFeedback());
		    		feedback.setShowGraderComments(assessmentSettings.getShowGraderComments());
		    		feedback.setShowStatistics(assessmentSettings.getShowStatistics());
		    		feedback.setShowCorrection(assessmentSettings.getShowCorrectResponse() ? assessmentSettings.getShowCorrection() : false);
		    }
		assessment.setAssessmentFeedback(feedback);

		// set Grading
		EvaluationModelIfc evaluation =  assessment.getEvaluationModel();
		if (evaluation == null){
			evaluation = new EvaluationModel();
			evaluation.setAssessmentBase(assessment.getData());
		}
		if (assessmentSettings.getAnonymousGrading()) {
			evaluation.setAnonymousGrading(EvaluationModelIfc.ANONYMOUS_GRADING);
		}
		else {
			evaluation.setAnonymousGrading(EvaluationModelIfc.NON_ANONYMOUS_GRADING);
			evaluation.setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
		}

		// If there is value set for toDefaultGradebook, we reset it
		// Otherwise, do nothing
		if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(assessmentSettings.getToDefaultGradebook())) {
			evaluation.setToGradeBook(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString());
		} else if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(assessmentSettings.getToDefaultGradebook())) {
			evaluation.setToGradeBook(EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString());
		} else {
			evaluation.setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
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
		HashMap<String, String> h = assessmentSettings.getValueMap();
		saveAssessmentSettings.updateMetaWithValueMap(assessment, h);

		org.sakaiproject.grading.api.GradingService gradingService =
			(org.sakaiproject.grading.api.GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");

		boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());

		// Add category unless unassigned (-1) is selected or defaulted. CategoryId comes
		// from the web page as a string representation of a the long cat id.
		if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
			assessment.updateAssessmentToGradebookNameMetaData(assessmentSettings.getGradebookName());
	
			if (isGradebookGroupEnabled) {
			  assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.CATEGORY_LIST, "-1");
			} else {
			  assessment.setCategoryId(null);
			}
		} else if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
		  if (isGradebookGroupEnabled) {
			assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.CATEGORY_LIST, assessmentSettings.getCategorySelected());
		  } else {
			// Add category unless unassigned (-1) is selected or defaulted. CategoryId comes
			// from the web page as a string representation of a the long cat id.
			if (!StringUtils.equals(assessmentSettings.getCategorySelected(), "-1") && !StringUtils.isEmpty(assessmentSettings.getCategorySelected())) {
			  List<String> categoryList = Arrays.asList(assessmentSettings.getCategorySelected().split(","));
			  assessment.getData().setCategoryId(Long.parseLong((categoryList.get(0))));
			  assessment.setCategoryId(Long.parseLong((categoryList.get(0))));
			} else {
				assessment.getData().setCategoryId(null);
				assessment.setCategoryId(null);
			}
		  }
	
		  assessment.updateAssessmentToGradebookNameMetaData("");
		}

		ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
		extendedTimeFacade.saveEntriesPub(assessment.getData(), assessmentSettings.getExtendedTimes());

		// i. set Graphics
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGCOLOR, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getBgColor()));
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGIMAGE, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getBgImage()));

	    // j. set objectives,rubrics,keywords,tracking
		assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.KEYWORDS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getKeywords()));
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.OBJECTIVES, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getObjectives()));
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.RUBRICS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getRubrics()));
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.TRACK_QUESTIONS, Boolean.toString(assessmentSettings.getTrackQuestions()));
	}

	public boolean checkScore(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, FacesContext context) {
		// check if the score is > 0, Gradebook doesn't allow assessments with total
		// point = 0.
		boolean gbError = false;

		if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(assessmentSettings.getToDefaultGradebook())) {
			if (assessment.getTotalScore() <= 0) {
				String gb_err = (String) ContextUtil.getLocalizedString(
						"org.sakaiproject.tool.assessment.bundle.AuthorMessages","gradebook_exception_min_points");
				context.addMessage(null, new FacesMessage(gb_err));
				gbError = true;
			}
		}
		return gbError;
	}

    public boolean updateGB(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, PublishedAssessmentFacade originalAssessment,
		boolean isTitleChanged, boolean isScoringTypeChanged, FacesContext context) {

        //#3 - add or remove external assessment to gradebook
        // a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
        // b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
        //    exception are indication that the assessment is already in the Gradebook or there is nothing
        //    to remove.
        org.sakaiproject.grading.api.GradingService gradingServiceApi = null;
        if (integrated) {
            gradingServiceApi = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.grading.api.GradingService");
        }

        PublishedEvaluationModel evaluation = (PublishedEvaluationModel) assessment.getEvaluationModel();

        if (evaluation == null){
            evaluation = new PublishedEvaluationModel();
            evaluation.setAssessmentBase(assessment.getData());
        }

        evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());

        // If the assessment is retracted for edit, we don't sync with gradebook (only until it is republished)
        if (AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS.equals(assessment.getStatus())) {
            return true;
        }

		String toGradebook = evaluation.getToGradeBook();
		boolean isGradebookGroupEnabled = gradingServiceApi.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());

        if (toGradebook != null &&
                !toGradebook.equals(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString())) {
            boolean existingItemHasBeenRemoved = false;

			if (EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(toGradebook)) {
				/* SINCE THIS IS CASE 3, THE ITEMS ASSOCIATED WITH THIS EXAM ARE GENERATED IN THE GRADEBOOK
					SO WE MUST ALWAYS DELETE ANY ITEM THAT IS ASSOCIATED WITH THE PUBLISHED EXAM ID
					THIS IS BECAUSE ITEMS CAN BE CREATED IN EXAMS, BUT ONLY IF WE ARE IN CASE 2, SO
					THESE ITEMS FROM CASE 2 WILL BE DELETED */
				try {
					gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), gradingServiceApi);
					// This variable is only true then the assessment has been removed!
					existingItemHasBeenRemoved = true;
				} catch (Exception e1) {
					// Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
					log.info("Exception thrown in updateGB():" + e1.getMessage());
				}

				if (existingItemHasBeenRemoved) {
					String gradebookItemString = assessment.getAssessmentToGradebookNameMetaData();
					List<String> gradebookItemList = new ArrayList<>();

					if (gradebookItemString != null && !gradebookItemString.isBlank()) {
						gradebookItemList = Arrays.asList(gradebookItemString.split(","));
					} else {
						String gbNotExistsError = "No se pueden crear exámenes sin ningún item asociado";
						context.addMessage(null, new FacesMessage(gbNotExistsError));
						return false;
					}

					if (gradebookItemList.contains(assessment.getPublishedAssessmentId().toString())) {
						String gbNotExistsError = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "gbNotExistsError");
						context.addMessage(null, new FacesMessage(gbNotExistsError));
						return false;
					}
				}

				gbsHelper.manageScoresToNewGradebook(new GradingService(), gradingServiceApi, assessment, evaluation);
			} else if (EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
				Object categorySelected = null;
				Object oldCategorySelected = null;

				if (isGradebookGroupEnabled) {
					categorySelected = assessment.getAssessmentMetaDataMap().get(AssessmentMetaDataIfc.CATEGORY_LIST);
					oldCategorySelected = originalAssessment.getAssessmentMetaDataMap().get(AssessmentMetaDataIfc.CATEGORY_LIST);
				} else {
					categorySelected = assessment.getCategoryId();
					oldCategorySelected = originalAssessment.getData() != null ? originalAssessment.getData().getCategoryId() : "";
				}

				String categoryString = categorySelected != null ? categorySelected.toString() : "-1";
				categoryString = !categoryString.equals("-1") ? categoryString : "";

				String oldCategoryString = oldCategorySelected != null ? oldCategorySelected.toString() : "-1";
				oldCategoryString = !oldCategoryString.equals("-1") ? oldCategoryString : "";

				Map<String, String> oldGradebookCategoryMap = new HashMap<>();
				Map<String, String> newGradebookCategoryMap = new HashMap<>();

				Map<String, String> updateGradebookCategoryMap = new HashMap<>();
				Map<String, String> createGradebookCategoryMap = new HashMap<>();

				if (isGradebookGroupEnabled) {
					/* FIRST, WE WILL NEED TO CREATE TWO MAPS, ONE FOR THE OLD ONES AND ANOTHER FOR THE NEW ONES,
						WHICH CONTAIN THE CATEGORY IDS AND THE GRADEBOOK UID. THIS IS BECAUSE WE WILL
						LATER NEED TO CHECK IF THE CATEGORY IN EACH GRADEBOOK ASSOCIATED WITH THE EXAM HAS BEEN CHANGED */
					oldGradebookCategoryMap = gradingServiceApi.buildCategoryGradebookMap(Arrays.asList(assessmentSettings.getGroupsAuthorized()), oldCategoryString, AgentFacade.getCurrentSiteId());
					newGradebookCategoryMap = gradingServiceApi.buildCategoryGradebookMap(Arrays.asList(assessmentSettings.getGroupsAuthorized()), categoryString, AgentFacade.getCurrentSiteId());

					for (Map.Entry<String, String> entry : newGradebookCategoryMap.entrySet()) {
						String oldCategoryId = oldGradebookCategoryMap.get(entry.getKey());

						boolean isExternalAssignmentDefined = gradingServiceApi.isExternalAssignmentDefined(entry.getKey(),
							assessment.getPublishedAssessmentId().toString());

						/* IF: HERE WE WILL NEED TO CHECK IF THE ITEM EXISTS IN THE GRADEBOOK AND, IF THE CATEGORY HAS CHANGED,
								WE WILL PUT IT IN THE MAP OF ITEMS THAT NEED TO BE UPDATED
							ELSE: HERE WE WILL NEED TO CHECK IF THE ITEM EXISTS IN THE GRADEBOOK, IF NOT, WE WILL PUT IT
								IN THE MAP OF ITEMS THAT NEED TO BE CREATED */
						if (isExternalAssignmentDefined && !entry.getValue().equals(oldCategoryId)) {
							updateGradebookCategoryMap.put(entry.getKey(), entry.getValue());
						} else if (!isExternalAssignmentDefined) {
							createGradebookCategoryMap.put(entry.getKey(), entry.getValue());
						}
					}
				} else {
					// IN THIS CASE, SINCE IT'S NOT A MULTI-GRADEBOOK, WE ONLY NEED THE PREVIOUS CATEGORY AND THE NEW ONE
					Long newCategoryId = assessment.getCategoryId() != null ? assessment.getCategoryId() : -1L ;
					boolean isExternalAssignmentDefined = gradingServiceApi.isExternalAssignmentDefined(
						AgentFacade.getCurrentSiteId(),
						assessment.getPublishedAssessmentId().toString());

					if (isExternalAssignmentDefined && !newCategoryId.toString().equals(oldCategoryString)) {
						updateGradebookCategoryMap.put(AgentFacade.getCurrentSiteId(), newCategoryId.toString());
					} else if (!isExternalAssignmentDefined) {
						createGradebookCategoryMap.put(AgentFacade.getCurrentSiteId(), newCategoryId.toString());
					}
				}

				if (createGradebookCategoryMap != null && createGradebookCategoryMap.size() >= 1) {
					for (Map.Entry<String, String> entry : createGradebookCategoryMap.entrySet()) {
						try {
							PublishedAssessmentData data = (PublishedAssessmentData) assessment.getData();
							Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
							String ref = SamigoReferenceReckoner.reckoner().site(site.getId()).subtype("p").id(assessment.getPublishedAssessmentId().toString()).reckon().getReference();
							data.setReference(ref);

							gbsHelper.addToGradebook(entry.getKey(), data, !entry.getValue().equals("-1") ? Long.parseLong(entry.getValue()) : null, gradingServiceApi);
						} catch(Exception e){
							log.warn("oh well, must have been added already:"+e.getMessage());
						}
					}

					gbsHelper.manageScoresToNewGradebook(new GradingService(), gradingServiceApi, assessment, evaluation);
				}

				/* WE WILL NEED TO UPDATE THE ITEMS FROM CASE 2 IF THE TITLE, SCORE, OR ANY OF THE CATEGORIES
					HAVE CHANGED (IN CASE OF MULTI GRADEBOOK) */
				if (isTitleChanged || isScoringTypeChanged || updateGradebookCategoryMap.size() >= 1) {
					List<String> gradebookUidList = new ArrayList<>();

					if (isGradebookGroupEnabled) {
						gradebookUidList = assessmentSettings.getGroupsAuthorized() != null ?
                    		Arrays.asList(assessmentSettings.getGroupsAuthorized()) : new ArrayList<>();
					} else {
						gradebookUidList.add(AgentFacade.getCurrentSiteId());
					}

					try {
						gbsHelper.updateGradebook(assessment, isGradebookGroupEnabled, gradebookUidList, updateGradebookCategoryMap, gradingServiceApi);
					} catch (Exception e) {
						String gbConflict_error = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
						context.addMessage(null, new FacesMessage(gbConflict_error));
						evaluation.setToGradeBook("0");
						log.warn("Exception thrown in updateGB():" + e.getMessage());
						return false;
					}
				}
			}
        } else {
            try {
                gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), gradingServiceApi);
            } catch(Exception e){
                log.warn("No external assessment to remove: {}", e.getMessage());
            }
        }

        return true;
    }

	public void resetPublishedAssessmentsList(AuthorBean author, AuthorizationBean authorization,
			PublishedAssessmentService assessmentService) {
		AuthorActionListener authorActionListener = new AuthorActionListener();
		GradingService gradingService = new GradingService();
		List publishedAssessmentList = assessmentService.getBasicInfoOfAllPublishedAssessments2(
				  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
		authorActionListener.prepareAllPublishedAssessmentsList(author, authorization, gradingService, publishedAssessmentList);
	}
}
