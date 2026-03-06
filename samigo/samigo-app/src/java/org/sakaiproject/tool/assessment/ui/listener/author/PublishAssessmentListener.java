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

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.AssignmentHasIllegalPointsException;
import org.sakaiproject.grading.api.InvalidCategoryException;
import org.sakaiproject.grading.api.InvalidGradeItemNameException;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PreDeliveryPhase;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.web.client.HttpClientErrorException;

import lombok.extern.slf4j.Slf4j;

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
  private static final ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");

  private RubricsService rubricsService;
  private TaskService taskService;
  private SamigoAvailableNotificationService samigoAvailableNotificationService;
  private EventTrackingService eventTrackingService;

  public PublishAssessmentListener() {
    rubricsService = ComponentManager.get(RubricsService.class);
    taskService = ComponentManager.get(TaskService.class);
	samigoAvailableNotificationService = ComponentManager.get(SamigoAvailableNotificationService.class);
	eventTrackingService = ComponentManager.get(EventTrackingService.class);
  }

  @Override
  public void processAction(ActionEvent ae) throws AbortProcessingException {

      repeatedPublishLock.lock();
      boolean bulkPublish = false;

      try {
          // If instructor goes straight to publish from the main authoring page, the ae will be null and the instructor needs to do one more step before publishing
          if (ae == null) {
              repeatedPublish = false;
              return;
          }

          UIComponent eventSource = (UIComponent) ae.getSource();
          ValueBinding vb = eventSource.getValueBinding("value");

          // We are coming from a different listener and being thrown over here. This helps determine where we are coming from
          // See ActionSelectListener
          String origin = (String) eventSource.getAttributes().get("origin");

          // This is the bulk publish option: let it through
          if ("publish_selected".equals(origin)) {
              repeatedPublish = false;
              bulkPublish = true;
          }
          else if (vb == null) {
              repeatedPublish = false;
              return;
          }
          else {
              String buttonValue = vb.getExpressionString();
              if (buttonValue.endsWith(".button_unique_save_and_publish}")) {
                  repeatedPublish = false;
                  return;
              }
          }

          if (!repeatedPublish) {
              AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
              AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
              AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
              AssessmentService assessmentService = new AssessmentService();

              if (!bulkPublish && assessmentSettings != null && assessmentSettings.getAssessmentId() != null) {

                // This is a single publishing operation
                AssessmentFacade singleAssessment = assessmentService.getAssessment(
                    assessmentSettings.getAssessmentId().toString());
                singleAssessment = assessmentService.ensureUniquePublishedTitleForPublish(singleAssessment);

                publishOne(author, singleAssessment, assessmentSettings, assessmentService, authorization, repeatedPublish);

                GradingService gradingService = new GradingService();
                PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
                AuthorActionListener authorActionListener = new AuthorActionListener();
                authorActionListener.prepareAssessmentsList(author, authorization, assessmentService, gradingService, publishedAssessmentService);
                repeatedPublish = true;
                return;
              }

            // Assume this is a bulk publishing operation
            List assessmentList = author.getAllAssessments();
            for (Object assessment : assessmentList) {
                if (assessment instanceof AssessmentFacade) {
                    final String assessmentId = ((AssessmentFacade) assessment).getAssessmentBaseId().toString();
                    AssessmentFacade assessmentFacade = assessmentService.getAssessment(assessmentId);

                    if (((AssessmentFacade) assessment).isSelected()) {
                        assessmentList.remove(assessmentFacade);
                        assessmentFacade = assessmentService.ensureUniquePublishedTitleForPublish(assessmentFacade);
                        assessmentSettings.setAssessment(assessmentFacade);
                        publishOne(author, assessmentFacade, assessmentSettings, assessmentService, authorization, repeatedPublish);
                    }
                }
            }

            GradingService gradingService = new GradingService();
            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            AuthorActionListener authorActionListener = new AuthorActionListener();
            authorActionListener.prepareAssessmentsList(author, authorization, assessmentService, gradingService, publishedAssessmentService);

			repeatedPublish = true;
		}
	  } finally {
		  repeatedPublishLock.unlock();
	  }
  }

  private void publishOne(AuthorBean author, AssessmentFacade assessment, AssessmentSettingsBean assessmentSettings, AssessmentService assessmentService, AuthorizationBean authorization, boolean repeatedPublish) {

    // 0. sorry need double checking assesmentTitle and everything
    if (checkTitle(assessment)) return;

    // Tell AuthorBean that we just published an assessment
    // This will allow us to jump directly to published assessments tab
    author.setJustPublishedAnAssessment(true);

    //update any random draw questions from pool since they could have changed
    int success = assessmentService.updateAllRandomPoolQuestions(assessment, true);
    if (success == AssessmentService.UPDATE_SUCCESS) {

        //grab new updated assessment
        assessment = assessmentService.getAssessment(assessment.getAssessmentId().toString());
        publish(assessment, assessmentSettings);
    } else {

        FacesContext context = FacesContext.getCurrentInstance();
        if (success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE) {
            String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
            context.addMessage(null, new FacesMessage(err));
        } else {
            String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
            context.addMessage(null, new FacesMessage(err));
        }
    }
  }

  private void publish(AssessmentFacade assessment, AssessmentSettingsBean assessmentSettings) {
	PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    boolean sendEmailNotification = false;

    try {
      pub = publishedAssessmentService.publishAssessment(assessment);

      //Lock the groups for deletion if the assessment is released to groups, students can lose submissions if the group is deleted.
      boolean groupRelease = AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS.equals(assessmentSettings.getReleaseTo());

      if (groupRelease) {
        try{
            String publishedAssessmentId = String.valueOf(pub.getPublishedAssessmentId());
            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId, true);
            Map<String, String> selectedGroups = publishedAssessment.getReleaseToGroups();

            log.debug("Locking groups for deletion by the published assessment with id {}.", publishedAssessmentId);
            log.debug("Locking for deletion the following groups {}.", selectedGroups);

            Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
            Collection<Group> groups = site.getGroups();

            for(Group group : groups){
                if(selectedGroups.containsKey(group.getId())){
                    log.debug("Locking the group {} for deletion by the the published assessment with id {}.", group.getTitle(), publishedAssessmentId);
                    group.setLockForReference(publishedAssessmentId, RealmLockMode.DELETE);
                }
            }

            log.debug("Saving the site after locking the groups for deletion.");
            SiteService.save(site);
        }catch(Exception e){
            log.error("Fatal error locking the groups for deletion.", e);
        }
      }

      // The notification message will be used by the calendar event
      PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
      sendEmailNotification = publishRepublishNotification.isSendNotification();
      String notificationMessage = getNotificationMessage(publishRepublishNotification, assessment.getTitle(), assessmentSettings.getReleaseTo(),
                                                            assessmentSettings.getStartDateInClientTimezoneString(), assessmentSettings.getPublishedUrl(),
                                                            assessmentSettings.getDueDateInClientTimezoneString(), assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(),
                                                            assessmentSettings.getUnlimitedSubmissions(), assessmentSettings.getSubmissionsAllowed(), assessmentSettings.getScoringType(),
                                                            assessmentSettings.getFeedbackDelivery(), assessmentSettings.getFeedbackDateInClientTimezoneString(),
                                                            assessmentSettings.getFeedbackEndDateInClientTimezoneString(), assessmentSettings.getFeedbackScoreThreshold(),
                                                            assessmentSettings.getAutoSubmit(), assessmentSettings.getLateHandling(), assessmentSettings.getRetractDateString());

      ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
      extendedTimeFacade.copyEntriesToPub(pub.getData(), assessmentSettings.getExtendedTimes());

      eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_PUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));

		/*
		 *   UserNotification: check if event should be fired immediately or/and must be delayed --> subsequent events are handled by TestsAndQuizzesUserNotificationHandler
		 */
		List<ExtendedTime> extendedTimes = assessmentSettings.getExtendedTimes();
		Instant instant = pub.getStartDate().toInstant();
		if (instant.isBefore(Instant.now())) {
			eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));
		} else {
			Instant earliestDelayInstant = instant;
			if (assessmentSettings.getExtendedTimesSize() != 0) {
				ListIterator<ExtendedTime> it = extendedTimes.listIterator();
				boolean postEvent = false;
				while (it.hasNext()) {
					ExtendedTime exTime = it.next();
					Instant startInstant = exTime.getStartDate().toInstant();
					if (startInstant.isBefore(Instant.now()) && !postEvent) {
						postEvent = true;
					} else if (startInstant.isBefore(instant)) {
						earliestDelayInstant = startInstant;
					}
				}
				if (postEvent) {
					eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));
				}
			}
			eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true), earliestDelayInstant);
		}

      for (Object sectionObj : pub.getSectionSet()){
        PublishedSectionData sectionData = (PublishedSectionData) sectionObj;
        for (Object itemObj : sectionData.getItemSet()){
          PublishedItemData itemData = (PublishedItemData) itemObj;
			eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/publish, publishedItemId=" + itemData.getItemIdString(), true));

          try {
            Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_SAMIGO, assessmentSettings.getAssessmentId().toString() + "." + itemData.getOriginalItemId().toString());
            if (rubricAssociation.isPresent()) {
              Map<String, String> params = rubricAssociation.get().getFormattedAssociation();
              if ("2".equals(params.get(RubricsConstants.RBCS_ASSOCIATE))) {
                params.put(RubricsConstants.RBCS_LIST, "0");
              }
              rubricsService.saveRubricAssociation(RubricsConstants.RBCS_TOOL_SAMIGO, RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + pub.getPublishedAssessmentId().toString() + "." + itemData.getItemIdString(), params, AgentFacade.getCurrentSiteId());
            }
          } catch(HttpClientErrorException hcee) {
            log.debug("Current user doesn't have permission to get a rubric: {}", hcee.getMessage());
          }
        }
      }

      // update Calendar Events
      boolean addDueDateToCalendar = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishAssessmentForm:calendarDueDate") != null;
      calendarService.updateAllCalendarEvents(pub, assessmentSettings.getReleaseTo(), assessmentSettings.getGroupsAuthorized(), rl.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);

      // Create task
      AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
      String reference = AssessmentEntityProducer.REFERENCE_ROOT + "/" + AgentFacade.getCurrentSiteId() + "/" + pub.getPublishedAssessmentId();
      Task task = new Task();
      task.setSiteId(AgentFacade.getCurrentSiteId());
      task.setReference(reference);
      task.setSystem(true);
      task.setDescription(pub.getTitle());
      task.setDue((pub.getDueDate() == null ? null : pub.getDueDate().toInstant()));
      SelectItem[] usersMap = assessmentSettings.getUsersInSite();
      Set<String> users = new HashSet<>();
      for(SelectItem item : usersMap) {
        String userId = (String)item.getValue(); 
        if (StringUtils.isNotBlank(userId)) {
          users.add(userId);
        }
      }
      taskService.createTask(task, users, Priorities.HIGH);
        
    } catch (AssignmentHasIllegalPointsException gbe) {
       // Right now gradebook can only accept assessements with totalPoints > 0 
       // this  might change later
        log.warn(gbe.getMessage(), gbe);
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_min_points");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(gbe);
    } catch (InvalidGradeItemNameException gbe) {
        log.warn(gbe.getMessage(), gbe);
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_title_invalid");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(gbe);
    } catch (InvalidCategoryException gbe) {
		log.warn("Incorrect gradebook category settings detected when attempting publish assessment: {}", gbe.toString());
		String err = (String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
				"gradebook_exception_category_invalid");
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
    String settingsAlias = assessmentSettings.getAlias();
    if (StringUtils.isBlank(pub.getData().getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS))) {
      String aliasToUse;
      if (StringUtils.isNotBlank(settingsAlias)) {
        aliasToUse = settingsAlias;
      } else {
        log.warn("Alias was not set before publishing assessment {}; generating fallback UUID", assessment.getAssessmentId());
        // Generate a new unique alias
        aliasToUse = UUID.randomUUID().toString();
      }
      PublishedMetaData meta = new PublishedMetaData(pub.getData(), AssessmentMetaDataIfc.ALIAS, aliasToUse);
      publishedAssessmentService.saveOrUpdateMetaData(meta);

      // Refresh the published assessment to ensure metadata is current
      pub = publishedAssessmentService.getPublishedAssessment(pub.getPublishedAssessmentId().toString());
    }

    // Execute ASSESSMENT_PUBLISH pre-delivery phase for secure delivery module if available
    SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
    PublishedAssessmentIfc publishedAssessment = pub.getData();

    if (secureDeliveryService.isSecureDeliveryAvaliable()) {
        String moduleId = publishedAssessment.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.MODULE_KEY);

        if (moduleId != null && !SecureDeliveryServiceAPI.NONE_ID.equals(moduleId)) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

            PhaseStatus executionResult = secureDeliveryService.executePreDeliveryPhase(moduleId, PreDeliveryPhase.ASSESSMENT_PUBLISH,
                    assessment, publishedAssessment, request);

            log.debug("Pre-delivery phase {} executed for module [{}] with result [{}]", PreDeliveryPhase.ASSESSMENT_PUBLISH, moduleId, executionResult);

            if (!PhaseStatus.SUCCESS.equals(executionResult)) {
                String errorMessage = MessageFormat.format(
                        ContextUtil.getLocalizedString(SamigoConstants.AUTHOR_BUNDLE, "secure_delivery_exception_publish"),
                        new Object[]{ secureDeliveryService.getModuleName(moduleId, ContextUtil.getLocale()) });
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(errorMessage));
                throw new AbortProcessingException("Pre-delivery phase " + PreDeliveryPhase.ASSESSMENT_PUBLISH
                        + " failed for module [" + moduleId + "] when trying to publish assessment");
            }
        }
    }

    // Now that everything is updated schedule an open notification email
    if (sendEmailNotification) samigoAvailableNotificationService.scheduleAssessmentAvailableNotification(String.valueOf(pub.getPublishedAssessmentId()));
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
    org.sakaiproject.grading.api.GradingService g = null;
    if (integrated){
      g = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().
           getBean("org.sakaiproject.grading.api.GradingService");
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
        log.warn("external assessment in GB has the same title:{}", e.getMessage());
    }
    return error;
  }

    public String getNotificationMessage(PublishRepublishNotificationBean publishRepublishNotification, String title, String releaseTo, String startDateString, String publishedURL, String dueDateString,
										Integer timedHours, Integer timedMinutes, String unlimitedSubmissions, String submissionsAllowed, String scoringType, String feedbackDelivery,
										String feedbackDateString, String feedbackEndDateString, String feedbackScoreThreshold, boolean autoSubmitEnabled, String lateHandling,
										String retractDateString) {
	  String siteTitle = publishRepublishNotification.getSiteTitle();
	  if(siteTitle == null || siteTitle.isEmpty()){
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
		  message.append(MessageFormat.format(rl.getString("available_group_at_email"), startDateString, publishedURL));
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
		if(StringUtils.isNotBlank(feedbackScoreThreshold)){
			//Score threshold is set
			if(StringUtils.isNotBlank(feedbackEndDateString)){
				//Ranged availability
				message.append(MessageFormat.format(rl.getString("feedback_available_ranges_threshold"), feedbackScoreThreshold, feedbackDateString, feedbackEndDateString));
			} else{
				//Not ranged availability
				message.append(MessageFormat.format(rl.getString("feedback_available_on_threshold"), feedbackScoreThreshold, feedbackDateString));
			}
		} else{
			//Score threshold is not set
			if(StringUtils.isNotBlank(feedbackEndDateString)){
				//Ranged availability
				message.append(MessageFormat.format(rl.getString("feedback_available_ranges"), feedbackDateString, feedbackEndDateString));
			} else{
				//Not ranged availability
				message.append(MessageFormat.format(rl.getString("feedback_available_on"), feedbackDateString));
			}
		}
	  }
	  message.append(newline);
	  message.append(newline);

	  // Autosubmit
	  if (autoSubmitEnabled) {
		  String label, date;
		  if ("1".equals(lateHandling)) {
			  label = rl.getString("header_extendedTime_retract_date");
			  date = retractDateString;
		  } else {
			  label = rl.getString("header_extendedTime_due_date");
			  date = dueDateString;
		  }

		  message.append(MessageFormat.format(rl.getString("autosubmit_info"), label, date));
		  message.append(" ").append( MessageFormat.format(rl.getString("autosubmit_info_extended_time"), label));

		  message.append(newline);
		  message.append(newline);
	  }

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
