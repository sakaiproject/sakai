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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup.RealmLockMode;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
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

    public RemoveAssessmentListener()
    {
    }

    public void processAction(ActionEvent ae) throws AbortProcessingException
    {
        FacesContext context = FacesContext.getCurrentInstance();

        AssessmentService assessmentService = new AssessmentService();
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

        AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
        AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
        AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");

        List assessmentList = author.getAssessments();
        List publishedAssessmentList = author.getPublishedAssessments();

        List<Object> deleteableAssessments = new ArrayList<>();
        List<String> errorAssessments = new ArrayList<>();

        for (Object assessment : author.getAllAssessments()) {
            if (assessment instanceof AssessmentFacade) {
                final String assessmentId = ((AssessmentFacade) assessment).getAssessmentBaseId().toString();
                AssessmentFacade assessmentFacade = assessmentService.getAssessment(assessmentId);

                if (((AssessmentFacade) assessment).isSelected()) {
                    if (!this.isUserAllowedToDeleteAssessment(author, assessmentFacade)) {
                        errorAssessments.add(assessmentFacade.getTitle());
                    } else {
                        deleteableAssessments.add(assessmentFacade);
                    }
                    assessmentList.remove(assessmentFacade);
                }
            }

            if (assessment instanceof PublishedAssessmentFacade) {
                final String assessmentId = ((PublishedAssessmentFacade) assessment).getPublishedAssessmentId().toString();
                PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(assessmentId, true);

                if (((PublishedAssessmentFacade) assessment).isSelected()) {
                    if (this.isUserAllowedToDeletePublishedAssessment(author, publishedAssessmentService, publishedAssessment) && authorizationBean.isUserAllowedToDeleteAssessment(assessmentId, publishedAssessment.getCreatedBy(), true)) { 
                        deleteableAssessments.add(publishedAssessment);
                    } else {
                        errorAssessments.add(publishedAssessment.getTitle());
                    }
                    publishedAssessmentList.remove(assessment);
                }
            }
        }

        if (errorAssessments.isEmpty()) {
            for (Object deleteableAssessment : deleteableAssessments) {
                if (deleteableAssessment instanceof AssessmentFacade) {
                    AssessmentFacade assessmentFacade = (AssessmentFacade) deleteableAssessment;
                    String assessmentId = assessmentFacade.getAssessmentBaseId().toString();
                    assessmentService.removeAssessment(assessmentId);

                    final String siteId = assessmentService.getAssessmentSiteId(assessmentId);
                    EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REMOVE, "assessmentId=" + assessmentId, siteId, true, NotificationService.NOTI_NONE));

                    try {
                        Iterator<SectionFacade> sectionFacadeIterator = assessmentFacade.getSectionSet().iterator();
                        while (sectionFacadeIterator.hasNext()){
                            SectionFacade sectionFacade = sectionFacadeIterator.next();
                            Iterator<ItemFacade> itemFacadeIterator = sectionFacade.getItemFacadeSet().iterator();
                            while (itemFacadeIterator.hasNext()){
                                ItemFacade itemFacade = itemFacadeIterator.next();
                                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UNINDEXITEM, "/sam/" + siteId + "/unindexed, itemId=" + itemFacade.getItemIdString(), true));
                            }
                        }
                    } catch(Exception ex) {
                        //The assessment doesn't exist. No-op in this case.
                    }
                }
                if (deleteableAssessment instanceof PublishedAssessmentFacade) {
                    PublishedAssessmentFacade publishedAssessment = (PublishedAssessmentFacade) deleteableAssessment;
                    String assessmentId = publishedAssessment.getPublishedAssessmentId().toString();

                    log.debug("assessmentId = " + assessmentId);
                    PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
                    //get assessment to see if it has a calendar event

                    pubAssessmentService.removeAssessment(assessmentId, "remove");
                    removeFromGradebook(assessmentId);

                    //Block the groups for deletion if the assessment is released to groups, students can lose submissions if the group is deleted.
                    boolean groupRelease = publishedAssessment.getReleaseToGroups() != null ? !publishedAssessment.getReleaseToGroups().isEmpty() : false;

                    if(groupRelease){
                        try{
                            Map<String,String> selectedGroups = publishedAssessment.getReleaseToGroups();
                            log.debug("Unlocking groups for deletion by the published assessment with id {}.", assessmentId);
                            log.debug("Unlocking for deletion the following groups {}.", selectedGroups);

                            SiteService siteService = (SiteService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.site.api.SiteService");
                            ToolManager toolManager = (ToolManager) SpringBeanLocator.getInstance().getBean("org.sakaiproject.tool.api.ToolManager");

                            Site site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
                            Collection<Group> groups = site.getGroups();

                            for(Group group : groups){
                                if(selectedGroups.keySet().contains(group.getId())){
                                    log.debug("Unlocking the group {} for deletion by the the published assessment with id {}.", group.getTitle(), assessmentId);
                                    group.setLockForReference(assessmentId, RealmLockMode.NONE);
                                }
                            }

                            log.debug("Saving the site after unlocking the groups for deletion.");
                            siteService.save(site);
                        }catch(Exception e){
                            log.error("Fatal error unlocking the groups for deletion {}.", e);
                        }
                    }

                    String calendarDueDateEventId = publishedAssessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID);
                    if (calendarDueDateEventId != null) {
                        calendarService.removeCalendarEvent(AgentFacade.getCurrentSiteId(), calendarDueDateEventId);
                    }
                    EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REMOVE, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + assessmentId, true));
                    Iterator<PublishedSectionData> sectionDataIterator = publishedAssessment.getSectionSet().iterator();
                    while (sectionDataIterator.hasNext()) {
                        PublishedSectionData sectionData = sectionDataIterator.next();
                        Iterator<ItemDataIfc> itemDataIfcIterator = sectionData.getItemSet().iterator();
                        while (itemDataIfcIterator.hasNext()){
                            ItemDataIfc itemDataIfc = itemDataIfcIterator.next();
                            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_UNINDEXITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/unindexed, publishedItemId=" + itemDataIfc.getItemIdString(), true));
                        }
                    }

                    List inactivePublishedAssessmentList = author.getInactivePublishedAssessments();
                    List inactiveList = new ArrayList();
                    for (int i=0; i<inactivePublishedAssessmentList.size();i++) {
                        PublishedAssessmentFacade pa = (PublishedAssessmentFacade) inactivePublishedAssessmentList.get(i);
                        if (!(assessmentId).equals(pa.getPublishedAssessmentId().toString())) {
                            inactiveList.add(pa);
                        }
                    }
                    author.setInactivePublishedAssessments(inactiveList);
                    boolean isAnyAssessmentRetractForEdit = false;
                    Iterator iter = inactiveList.iterator();
                    while (iter.hasNext()) {
                        PublishedAssessmentFacade publishedAssessmentFacade = (PublishedAssessmentFacade) iter.next();
                        if (Integer.valueOf(3).equals(publishedAssessmentFacade.getStatus())) {
                            isAnyAssessmentRetractForEdit = true;
                            break;
                        }
                    }
                    if (isAnyAssessmentRetractForEdit) {
                        author.setIsAnyAssessmentRetractForEdit(true);
                    }
                    else {
                        author.setIsAnyAssessmentRetractForEdit(false);
                    }
                }
            }
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

    private boolean isUserAllowedToDeletePublishedAssessment(AuthorBean author, PublishedAssessmentService publishedAssessmentService, PublishedAssessmentFacade publishedAssessment) {
        AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        if (!authzBean.isUserAllowedToDeleteAssessment(publishedAssessment.getPublishedAssessmentId().toString(), publishedAssessment.getCreatedBy(), true)) {
            author.setOutcome("removeError");
            return false;
        }

        return true;
    }

    private void removeFromGradebook(String assessmentId) {
        GradebookExternalAssessmentService g = null;
        if (integrated)
        {
            g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
            getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
        }
        try {
            log.debug("before gbsHelper.removeGradebook()");
            gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessmentId, g);
        } catch (Exception e1) {
            // Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
            log.info("Exception thrown in updateGB():" + e1.getMessage());
        }
    }

}
