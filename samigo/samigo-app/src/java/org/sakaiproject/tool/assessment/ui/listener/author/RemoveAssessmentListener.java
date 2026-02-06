/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
import java.util.Collection;
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

import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class RemoveAssessmentListener implements ActionListener
{
    private static final GradebookServiceHelper gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
    private static final boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();
    private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
    private SamigoAvailableNotificationService samigoAvailableNotificationService = ComponentManager.get(SamigoAvailableNotificationService.class);
    private TaskService taskService;
    
    public RemoveAssessmentListener()
    {
        taskService = ComponentManager.get(TaskService.class);
    }

    public void processAction(ActionEvent ae) throws AbortProcessingException
    {
        FacesContext context = FacesContext.getCurrentInstance();

        AssessmentService assessmentService = new AssessmentService();
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

        AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
        AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");

        @SuppressWarnings("unchecked")
        List<AssessmentFacade> assessmentList = (List<AssessmentFacade>) author.getAssessments();
        @SuppressWarnings("unchecked")
        List<PublishedAssessmentFacade> publishedAssessmentList = (List<PublishedAssessmentFacade>) author.getPublishedAssessments();

        List<AssessmentFacade> deleteableAssessments = new ArrayList<>();
        List<String> deleteablePublishedAssessmentIds = new ArrayList<>();
        Map<String, Set<String>> releaseGroupIdsByPublishedAssessmentId = new HashMap<>();
        Map<String, String> calendarDueDateEventIdByPublishedAssessmentId = new HashMap<>();
        List<String> errorAssessments = new ArrayList<>();

        for (Object assessment : author.getAllAssessments()) {
            if (assessment instanceof AssessmentFacade) {
                final String assessmentId = ((AssessmentFacade) assessment).getAssessmentBaseId().toString();

                if (((AssessmentFacade) assessment).isSelected()) {
                    AssessmentFacade assessmentFacade = assessmentService.getBasicInfoOfAnAssessment(assessmentId);
                    if (assessmentFacade == null) {
                        continue;
                    }

                    if (!this.isUserAllowedToDeleteAssessment(author, assessmentFacade)) {
                        errorAssessments.add(assessmentFacade.getTitle());
                    } else {
                        deleteableAssessments.add(assessmentFacade);
                    }
                }
            }

            if (assessment instanceof PublishedAssessmentFacade) {
                final String assessmentId = ((PublishedAssessmentFacade) assessment).getPublishedAssessmentId().toString();

                if (((PublishedAssessmentFacade) assessment).isSelected()) {
                    PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getSettingsOfPublishedAssessment(assessmentId);
                    if (publishedAssessment == null) {
                        continue;
                    }

                    if (this.isUserAllowedToDeletePublishedAssessment(author, publishedAssessment)) {
                        Map<String, String> selectedGroups = ((PublishedAssessmentFacade) assessment).getReleaseToGroups();
                        String calendarDueDateEventId = publishedAssessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID);
                        deleteablePublishedAssessmentIds.add(assessmentId);
                        if (selectedGroups != null && !selectedGroups.isEmpty()) {
                            releaseGroupIdsByPublishedAssessmentId.put(assessmentId, new HashSet<>(selectedGroups.keySet()));
                        }
                        if (calendarDueDateEventId != null) {
                            calendarDueDateEventIdByPublishedAssessmentId.put(assessmentId, calendarDueDateEventId);
                        }
                    } else {
                        errorAssessments.add(publishedAssessment.getTitle());
                    }
                }
            }
        }

        if (errorAssessments.isEmpty()) {
            Set<String> removedAssessmentIds = new HashSet<>();
            Set<String> removedPublishedAssessmentIds = new HashSet<>();
            Map<String, Set<String>> groupIdsByAssessmentId = new HashMap<>();

            for (AssessmentFacade assessmentFacade : deleteableAssessments) {
                String assessmentId = assessmentFacade.getAssessmentBaseId().toString();
                assessmentService.removeAssessment(assessmentId);
                removedAssessmentIds.add(assessmentId);

                final String siteId = assessmentService.getAssessmentSiteId(assessmentId);
                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REMOVE, "assessmentId=" + assessmentId, siteId, true, NotificationService.NOTI_NONE));
                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UNINDEXITEM, "/sam/" + siteId + "/unindexed, assessmentId=" + assessmentId, true));
            }

            final String siteId = AgentFacade.getCurrentSiteId();
            PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
            for (String assessmentId : deleteablePublishedAssessmentIds) {
                log.debug("assessmentId = {}", assessmentId);

                pubAssessmentService.removeAssessment(assessmentId, "remove");
                removeFromGradebook(assessmentId);

                collectGroupUnlocks(assessmentId, releaseGroupIdsByPublishedAssessmentId.get(assessmentId), groupIdsByAssessmentId);

                String calendarDueDateEventId = calendarDueDateEventIdByPublishedAssessmentId.get(assessmentId);
                if (calendarDueDateEventId != null) {
                    calendarService.removeCalendarEvent(siteId, calendarDueDateEventId);
                }

                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REMOVE, "siteId=" + siteId + ", publishedAssessmentId=" + assessmentId, true));
                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_UNINDEXITEM, "/sam/" + siteId + "/unindexed, publishedAssessmentId=" + assessmentId, true));

                // Delete task
                String reference = AssessmentEntityProducer.REFERENCE_ROOT + "/" + siteId + "/" + assessmentId;
                taskService.removeTaskByReference(reference);

                samigoAvailableNotificationService.removeScheduledAssessmentNotification(assessmentId); // remove the existing scheduled notification for this published assessment if it exists
                removedPublishedAssessmentIds.add(assessmentId);
            }

            unlockGroupsForDeletion(groupIdsByAssessmentId);
            updateInactivePublishedAssessments(author, removedPublishedAssessmentIds);
            assessmentList.removeIf(assessmentFacade -> removedAssessmentIds.contains(assessmentFacade.getAssessmentBaseId().toString()));
            publishedAssessmentList.removeIf(publishedAssessmentFacade -> removedPublishedAssessmentIds.contains(publishedAssessmentFacade.getPublishedAssessmentId().toString()));

            List allAssessments = new ArrayList<>();
            if (authorizationBean.getEditAnyAssessment() || authorizationBean.getEditOwnAssessment()) {
                allAssessments.addAll(assessmentList);
            }
            if (authorizationBean.getGradeAnyAssessment() || authorizationBean.getGradeOwnAssessment()) {
                allAssessments.addAll(publishedAssessmentList);
            }
            author.setOutcome("author");
            author.setAssessments(assessmentList);
            author.setPublishedAssessments(publishedAssessmentList);
            author.setAllAssessments(allAssessments);
        } else {
            String err = (String) ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_delete_other_members_assessment_error");
            context.addMessage(null,new FacesMessage(err));
            for (String errorAssessment : errorAssessments) {
                context.addMessage(null,new FacesMessage("- " + errorAssessment));
            }
        }
    }

    private boolean isUserAllowedToDeleteAssessment(AuthorBean author, AssessmentFacade assessment) {
        AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");

        if (!authzBean.isUserAllowedToDeleteAssessment(assessment.getAssessmentBaseId().toString(), assessment.getCreatedBy(), false)) {
            author.setOutcome("removeError");
            return false;
        }
        return true;
    }

    private boolean isUserAllowedToDeletePublishedAssessment(AuthorBean author, PublishedAssessmentFacade publishedAssessment) {
        AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        if (!authzBean.isUserAllowedToDeleteAssessment(publishedAssessment.getPublishedAssessmentId().toString(), publishedAssessment.getCreatedBy(), true)) {
            author.setOutcome("removeError");
            return false;
        }

        return true;
    }

    private void removeFromGradebook(String assessmentId) {
        org.sakaiproject.grading.api.GradingService g = null;
        if (integrated)
        {
            g = (org.sakaiproject.grading.api.GradingService) SpringBeanLocator.getInstance().
            getBean("org.sakaiproject.grading.api.GradingService");
        }
        try {
            log.debug("before gbsHelper.removeGradebook()");
            gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessmentId, g);
        } catch (Exception e1) {
            // Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
            log.info("Exception thrown in updateGB():" + e1.getMessage());
        }
    }

    private void collectGroupUnlocks(String assessmentId, Set<String> selectedGroupIds, Map<String, Set<String>> groupIdsByAssessmentId) {
        if (selectedGroupIds == null || selectedGroupIds.isEmpty()) {
            return;
        }

        for (String groupId : selectedGroupIds) {
            Set<String> assessmentIds = groupIdsByAssessmentId.get(groupId);
            if (assessmentIds == null) {
                assessmentIds = new HashSet<>();
                groupIdsByAssessmentId.put(groupId, assessmentIds);
            }
            assessmentIds.add(assessmentId);
        }
    }

    private void unlockGroupsForDeletion(Map<String, Set<String>> groupIdsByAssessmentId) {
        if (groupIdsByAssessmentId.isEmpty()) {
            return;
        }

        try {
            SiteService siteService = (SiteService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.site.api.SiteService");
            ToolManager toolManager = (ToolManager) SpringBeanLocator.getInstance().getBean("org.sakaiproject.tool.api.ToolManager");

            Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
            Collection<Group> groups = site.getGroups();
            Map<String, Group> groupsById = new HashMap<>();
            for (Group group : groups) {
                groupsById.put(group.getId(), group);
            }

            for (Map.Entry<String, Set<String>> unlockEntry : groupIdsByAssessmentId.entrySet()) {
                Group group = groupsById.get(unlockEntry.getKey());
                if (group == null) {
                    continue;
                }

                for (String assessmentId : unlockEntry.getValue()) {
                    group.setLockForReference(assessmentId, RealmLockMode.NONE);
                }
            }

            siteService.save(site);
        } catch (Exception e) {
            log.error("Fatal error unlocking groups for deletion.", e);
        }
    }

    private void updateInactivePublishedAssessments(AuthorBean author, Set<String> removedPublishedAssessmentIds) {
        if (removedPublishedAssessmentIds.isEmpty()) {
            return;
        }

        List inactivePublishedAssessmentList = author.getInactivePublishedAssessments();
        List inactiveList = new ArrayList();
        boolean isAnyAssessmentRetractForEdit = false;

        for (int i = 0; i < inactivePublishedAssessmentList.size(); i++) {
            PublishedAssessmentFacade pa = (PublishedAssessmentFacade) inactivePublishedAssessmentList.get(i);
            if (!removedPublishedAssessmentIds.contains(pa.getPublishedAssessmentId().toString())) {
                inactiveList.add(pa);
                if (Integer.valueOf(3).equals(pa.getStatus())) {
                    isAnyAssessmentRetractForEdit = true;
                }
            }
        }

        author.setInactivePublishedAssessments(inactiveList);
        author.setIsAnyAssessmentRetractForEdit(isAnyAssessmentRetractForEdit);
    }

}
