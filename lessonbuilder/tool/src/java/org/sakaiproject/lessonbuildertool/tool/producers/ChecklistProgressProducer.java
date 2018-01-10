/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016 University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.producers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class ChecklistProgressProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
    private SimplePageBean simplePageBean;
    private ShowPageProducer showPageProducer;
    private SimplePageToolDao simplePageToolDao;

    private AuthzGroupService authzGroupService;
    private UserDirectoryService userDirectoryService;

    public MessageLocator messageLocator;
    public LocaleGetter localeGetter;

    public static final String VIEW_ID = "ChecklistProgress";

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        GeneralViewParameters gparams = (GeneralViewParameters) viewparams;

        if (gparams.getSendingPage() != -1) {
            // will fail if page not in this site
            // security then depends upon making sure that we only deal with this page
            try {
                simplePageBean.updatePageObject(gparams.getSendingPage());
            } catch (Exception e) {
                log.error("Checklist permission exception {}, {}", e.getMessage(), e);
                return;
            }
        }

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        SimplePage page = simplePageBean.getCurrentPage();
        String siteId = simplePageBean.getCurrentSiteId();
        List<SimpleChecklistItem> checklistItems = new ArrayList<SimpleChecklistItem>();
        String groups = null;

        Long itemId = gparams.getItemId();

        if (itemId != null && itemId != -1) {
            SimplePageItem i = simplePageBean.findItem(itemId);
            groups = i.getGroups();
            checklistItems = simplePageToolDao.findChecklistItems(i);
        }

        if (simplePageBean.canEditPage()) {
            simplePageBean.setItemId(itemId);

            UIOutput.make(tofill, "title-label", messageLocator.getMessage("simplepage.viewing-checklistProgress"));
            UIOutput.make(tofill, "checklist-title", simplePageBean.getName());

            UIOutput.make(tofill, "checklist-name", simplePageBean.getName());
            UIOutput.make(tofill, "checklist-description", simplePageBean.getDescription());

            List<SimplePageItem> allChecklists = simplePageToolDao.findAllChecklistsInSite(siteId);
            if (allChecklists != null) {
                allChecklists.sort( (SimplePageItem o1, SimplePageItem o2) -> o1.getName().compareToIgnoreCase(o2.getName()) );
                for (SimplePageItem checklist : allChecklists) {
                    // Don't include the current checklist
                    if(checklist.getId() != simplePageBean.getItemId()) {
                        UIBranchContainer otherChecklistItems = UIBranchContainer.make(tofill, "other-checklists:");
                        GeneralViewParameters gvp = new GeneralViewParameters();
                        gvp.setSendingPage(gparams.getSendingPage());
                        gvp.setItemId(checklist.getId());
                        gvp.viewID = ChecklistProgressProducer.VIEW_ID;
                        UIInternalLink.make(otherChecklistItems, "edit-checklistProgress", checklist.getName(), gvp).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.view-checklist").replace("{}", checklist.getName())));
                        UIOutput.make(otherChecklistItems, "otherChecklistName", checklist.getName());
                    }
                }
            }

            // Make the table container
            UIBranchContainer container = UIBranchContainer.make(tofill, "checklistContainer:");
            UIBranchContainer tableContainer = UIBranchContainer.make(container, "checklistTable:");

            // Make the header row for the progress table
            UIBranchContainer headerRow = UIBranchContainer.make(tableContainer, "itemHeader:");

            // The first column is filled with student names this is the header.
            UIOutput.make(headerRow, "student-th");
            UIOutput.make(headerRow, "studentheader-text", messageLocator.getMessage("simplepage.studentheader"));

            int itemNum = 1;
            // Need to make a column/header for every checklist item
            for (SimpleChecklistItem checklistItem : checklistItems) {
                // Add item to checklist item list
                UIBranchContainer checklistItemList = UIBranchContainer.make(tofill, "checklistItemList:");
                UIOutput.make(checklistItemList, "checklistitem-text", checklistItem.getName());

                // Add item to progress table
                UIBranchContainer checklistItemHeader = UIBranchContainer.make(headerRow, "checklistItem-th:");
                UIOutput.make(checklistItemHeader, "checklistitemheader-num", String.valueOf(itemNum));

                itemNum++;
            }

            // Build the "progress" part of the checklist progress table
            try {
                AuthzGroup membership;
                Set<Member> memberSet = new HashSet<Member>();

                if (groups != null && !"".equals(groups)) {
                    // Released to groups
                    List<String> groupIds = Arrays.asList(groups.split(","));
                    if (!groupIds.isEmpty()) {
                        for (String groupId : groupIds) {
                            membership = authzGroupService.getAuthzGroup("/site/" + siteId + "/group/" + groupId);
                            memberSet.addAll(membership.getMembers());
                        }
                    } else {
                        // Problem getting the groups, just show the whole site list
                        membership = authzGroupService.getAuthzGroup("/site/" + siteId);
                        memberSet.addAll(membership.getMembers());
                    }
                } else {
                    // Released to the whole site
                    membership = authzGroupService.getAuthzGroup("/site/" + siteId);
                    memberSet.addAll(membership.getMembers());
                }

                // Get the site maintainer role so we don't show those users in the list
                membership = authzGroupService.getAuthzGroup("/site/" + siteId);
                String maintainRole = membership.getMaintainRole();

                for (Member member : memberSet) {

                    // Only want to include non-maintainers in the progress table
                    if (!maintainRole.equals(member.getRole().getId())) {
                        User student = null;
                        try {
                            student = userDirectoryService.getUser(member.getUserId());

                            // Make an item row
                            UIBranchContainer progressRow = UIBranchContainer.make(tableContainer, "item:");

                            // Make the student name column
                            UIOutput.make(progressRow, "student-td");
                            UIOutput.make(progressRow, "studentname-text", student.getSortName());

                            // Check off items in list where completed
                            for (SimpleChecklistItem checklistItem : checklistItems) {
                                UIBranchContainer checklistItemCol = UIBranchContainer.make(progressRow, "checklistItem-td:");

                                boolean isDone = simplePageToolDao.isChecklistItemChecked(itemId, checklistItem.getId(), member.getUserId());

                                if (isDone) {
                                    checklistItemCol.decorate(new UIStyleDecorator("checkedOff"));
                                    UIOutput.make(checklistItemCol, "checkedOff");
                                    // Hidden span with the word "done" to assist with sorting.
                                    UIOutput.make(checklistItemCol, "sortHelper", "done").decorate(new UIStyleDecorator("noDisplay"));
                                }
                            }
                        } catch (UserNotDefinedException e) {
                            log.error("Unable to get user " + e);
                            continue;
                        }
                    }
                }
            } catch (GroupNotDefinedException e) {
                log.error("Unable to get site or group membership " + e);
            }

            UIInternalLink.make(tofill, "back-button", messageLocator.getMessage("simplepage.back"), new SimpleViewParameters(ShowPageProducer.VIEW_ID)).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.back")));
            UIInternalLink.make(tofill, "back-button2", messageLocator.getMessage("simplepage.back"), new SimpleViewParameters(ShowPageProducer.VIEW_ID)).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.back")));


        } else {
            UIBranchContainer error = UIBranchContainer.make(tofill, "error");
            UIOutput.make(error, "message", messageLocator.getMessage("simplepage.not_available"));
        }
    }

    public void setShowPageProducer(ShowPageProducer showPageProducer) {
        this.showPageProducer = showPageProducer;
    }

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }

    public void setSimplePageToolDao(SimplePageToolDao s) {
        simplePageToolDao = s;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

        return togo;
    }
}
