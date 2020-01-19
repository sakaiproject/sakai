/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class AssignmentContentProducer implements EntityContentProducer {

    private AssignmentService assignmentService;
    private SearchIndexBuilder searchIndexBuilder;
    private ServerConfigurationService serverConfigurationService;
    private SearchService searchService;
    private SiteService siteService;
    private TransactionTemplate transactionTemplate;

    private List<String> addingEvents = new ArrayList<>();
    private List<String> removingEvents = new ArrayList<>();

    public void init() {

        if ("true".equals(serverConfigurationService.getString("search.enable", "false"))) {
            addingEvents.add(AssignmentConstants.EVENT_ADD_ASSIGNMENT);
            addingEvents.add(AssignmentConstants.EVENT_ADD_ASSIGNMENT_CONTENT);
            addingEvents.add(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT);
            addingEvents.add(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_TITLE);
            removingEvents.add(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT);
            removingEvents.add(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT_CONTENT);
            addingEvents.forEach(e -> searchService.registerFunction(e));
            removingEvents.forEach(e -> searchService.registerFunction(e));
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    public boolean isContentFromReader(String reference) {
        return false;
    }

    public Reader getContentReader(String reference) {
        return null;
    }

    private Optional<Assignment> getAssignment(AssignmentReferenceReckoner.AssignmentReference ref) {

        String id = ref.getId();
        try {
            return Optional.of(assignmentService.getAssignment(id));
        } catch (IdUnusedException idue) {
            log.error("No assignment for id {}", id);
        } catch (PermissionException pe) {
            log.error("Search indexing user did not have permission to get assignment with id {}", id);
        }
        return Optional.empty();
    }

    public String getContent(String ref) {

        Optional<Assignment> opAssignment = getAssignment(AssignmentReferenceReckoner.reckoner().reference(ref).reckon());
        if (opAssignment.isPresent()) {
            StringBuilder sb = new StringBuilder();
            SearchUtils.appendCleanString(opAssignment.get().getTitle(), sb);
            sb.append(" ");
            SearchUtils.appendCleanString(opAssignment.get().getInstructions(), sb);
            sb.append(" ");
            return sb.toString();
        } else {
            return "";
        }
    }

    public String getTitle(String ref) {

        Optional<Assignment> opAssignment = getAssignment(AssignmentReferenceReckoner.reckoner().reference(ref).reckon());
        return (opAssignment.isPresent()) ? opAssignment.get().getTitle() : "";
    }

    public String getUrl(String ref) {

        return transactionTemplate.execute(new TransactionCallback<String>() {

            @Override
            public String doInTransaction(TransactionStatus status) {

                AssignmentReferenceReckoner.AssignmentReference r = AssignmentReferenceReckoner.reckoner().reference(ref).reckon();

                try {
                    Site site = siteService.getSite(r.getContext());
                    ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId="
                            + ref
                            + "&panel=Main&sakai_action=doView_assignment";
                } catch (Exception e) {
                    log.error("Failed to get deep link for context {} and assignment {}. Returning empty string.", r.getContext(), r.getId(), e);
                }
                return "";
            }
        });
    }

    public boolean matches(String ref) {
        return ref.startsWith("/assignment");
    }

    public Integer getAction(Event event) {

        String evt = event.getEvent();

        if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
        if (removingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;

        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    public boolean matches(Event event) {

        String evt = event.getEvent();
        return addingEvents.contains(evt) || removingEvents.contains(evt);
    }

    public String getTool() {
        return "assignments";
    }

    public String getSiteId(String ref) {
        return AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getContext();
    }

    public Iterator<String> getSiteContentIterator(String context) {

        return assignmentService.getAssignmentsForContext(context).stream().map(a -> {
                return AssignmentReferenceReckoner.reckoner().id(a.getId()).context(context).reckon().toString();
            }).collect(Collectors.toList()).iterator();
    }

    public boolean isForIndex(String ref) {
        return ref.startsWith("/assignment");
    }

    public boolean canRead(String ref) {
        return assignmentService.permissionCheck(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, ref, null);
        //return getAssignment(AssignmentReferenceReckoner.reckoner().reference(ref).reckon()).isPresent();
    }

    public Map<String, ?> getCustomProperties(String ref) {
        return null;
    }

    public String getCustomRDF(String ref) {
        return null;
    }

    public String getId(String ref) {
        return AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getId();
    }

    public String getType(String ref) {
        return "assignment";
    }

    public String getSubType(String ref) {
        return null;
    }

    public String getContainer(String ref) {
        return AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getContainer();
    }
}

