/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.webapi.beans.SiteEntityRestBean;
import org.sakaiproject.webapi.beans.TimeExceptionRestBean;
import org.sakaiproject.webapi.beans.SiteEntityRestBean.SiteEntityType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SiteEntityController extends AbstractSakaiApiController {


    private static final String GROUP_TAKE_ASSESSMENT_PERM = "TAKE_PUBLISHED_ASSESSMENT";
    private static final String SITE_SEGMENT = "/site/";
    private static final String GROUP_SEGMENT = "/group/";

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    private DiscussionForumManager discussionForumManager;

    @Autowired
    @Qualifier("org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    private SimplePageToolDao lessonService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PersistenceService persistenceService;

    private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();


    @GetMapping(value = "/sites/{siteId}/entities/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SiteEntityRestBean>> getSiteAssessments(@PathVariable String siteId) {
        String userId = checkSakaiSession().getUserId();
        checkSite(siteId);

        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();

        List<PublishedAssessmentFacade> publishedAssessments = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(
                userId, "publishedAssessmentId", true, siteId);

        Map<Long, List<ExtendedTime>> assessmentExtendedTimesMap = publishedAssessments.stream()
                .map(assessment -> {
                    PublishedAssessmentData assessmentData = new PublishedAssessmentData();
                    BeanUtils.copyProperties(assessment, assessmentData);

                    return new AbstractMap.SimpleEntry<Long, List<ExtendedTime>>(assessment.getPublishedAssessmentId(),
                        extendedTimeFacade.getEntriesForPub(assessmentData));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<SiteEntityRestBean> assessmentSiteEntities = publishedAssessments.stream()
                .map(assessment -> SiteEntityRestBean.of(assessment,
                        assessmentExtendedTimesMap.get(assessment.getPublishedAssessmentId()).stream()
                                .map(extendedTime -> TimeExceptionRestBean.of(siteId, extendedTime))
                                .collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(assessmentSiteEntities);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/sites/{siteId}/entities/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SiteEntityRestBean>> getSiteResources(@PathVariable String siteId) {
        checkSakaiSession();
        checkSite(siteId);

        String collectionRef = GROUP_SEGMENT + siteId + "/";

        List<ContentEntity> resources;
        try {
            resources = contentHostingService.getAllEntities(collectionRef);
        } catch (IllegalArgumentException e) {
            log.debug("Could not get resources due to {} {}", e.toString(), ExceptionUtils.getStackTrace(e));
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(resources.stream()
                .map(SiteEntityRestBean::of)
                .collect(Collectors.toSet()));
    }

    @PatchMapping(path = "/sites/{siteId}/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<SiteEntityRestBean>> updateSiteEntities(@PathVariable String siteId,
            @RequestBody Set<SiteEntityRestBean> patchEntities) {
        Site site = checkSite(siteId);
        String userId = checkSakaiSession().getUserId();

        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();

        Map<String, Object> toolEntities = new HashMap<>();

        // First iteration - Check if request is valid and keep the retrieved entities
        for (SiteEntityRestBean patchEntity : patchEntities) {
            SiteEntityType entityType = patchEntity.getType();
            Instant entityDueDate = patchEntity.getDueDate();

            if (StringUtils.isBlank(patchEntity.getId()) || entityType == null) {
                log.debug("Id or type not set");
                return ResponseEntity.badRequest().build();
            }

            switch(entityType) {
                case ASSESSMENT:
                    PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(patchEntity.getId(), true);
                    boolean timeExceptionsValid = Optional.ofNullable(patchEntity.getTimeExceptions())
                            .map(Set::stream)
                            .map(timeExceptionStream -> timeExceptionStream
                                .map(TimeExceptionRestBean::isValid)
                                .filter(timeExceptionValid -> !timeExceptionValid)
                                .findAny()
                                .orElse(true))
                            .orElse(true);

                    if (assessment == null) {
                        log.debug("assessment with id {} not found", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    if (!timeExceptionsValid) {
                        log.debug("timeExceptins for assessment with id {} not valid", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    if (!canUpdateAssessment(userId, assessment)) {
                        log.debug("canUpdateAssessment failed", patchEntity.getId());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    toolEntities.put(entityKey(patchEntity), assessment);
                    break;
                case ASSIGNMENT:
                    if(patchEntity.getTimeExceptions() !=  null) {
                        return ResponseEntity.badRequest().build();
                    }

                    Assignment assignment;
                    try {
                        assignment = assignmentService.getAssignment(patchEntity.getId());

                    } catch (IdUnusedException e) {
                        log.debug("assignment with id {} not found", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    } catch (PermissionException e) {
                        log.debug("assignmentService permission check failed", patchEntity.getId());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    if (!canUpdateAssignment(userId, assignment)) {
                        log.debug("canUpdateAssignment failed", patchEntity.getId());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    toolEntities.put(entityKey(patchEntity), assignment);
                    break;
                case RESOURCE:
                case RESOURCE_FOLDER:
                    if (ObjectUtils.anyNotNull(patchEntity.getDueDate(), patchEntity.getTimeExceptions())) {
                        log.debug("Due date and timeExceptions can not be set for resources", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    if (!isDateRestrictionPatchValid(patchEntity)) {
                        log.debug("Date restriction invalid", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    // ContentResourceEdit or ContentCollectionEdit
                    GroupAwareEdit resourceEdit;
                    try {
                        resourceEdit = SiteEntityType.RESOURCE.equals(patchEntity.getType())
                            ? contentHostingService.editResource(patchEntity.getId())
                            : contentHostingService.editCollection(patchEntity.getId());
                    } catch (IdUnusedException | InUseException e) {
                        return ResponseEntity.badRequest().build();
                    } catch (PermissionException e) {
                        log.error("Permission check when editing resource failed {}",
                                ExceptionUtils.getStackTrace(e));
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    } catch (TypeException e) {
                        log.error("Could not get resource edit due to TypeException: {}", ExceptionUtils.getStackTrace(e));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    toolEntities.put(entityKey(patchEntity), resourceEdit);
                    break;
                case FORUM:
                    if (ObjectUtils.anyNotNull(
                            entityDueDate,
                            patchEntity.getTimeExceptions(),
                            patchEntity.getGroupRefs())) {
                        log.debug("dueDate, timeExceptions and groupRefs can not be set for forum entities");
                        return ResponseEntity.badRequest().build();
                    }

                    if (!isDateRestrictionPatchValid(patchEntity)) {
                        log.debug("Date restriction invalid", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    Area forumArea = discussionForumManager.getDiscussionForumArea(siteId);

                    Optional<DiscussionForum> optForum = findForumInArea(forumArea, patchEntity.getId());

                    if (optForum.isEmpty()) {
                        log.debug("Forum with id {} not found", patchEntity.getId());
                        return ResponseEntity.badRequest().build();
                    }

                    toolEntities.put(entityKey(patchEntity), optForum.get());
                    break;
                default:
                    log.debug("Unhandled entity type {}", patchEntity.getType());
                    return ResponseEntity.badRequest().build();
            }
        }

        Set<SiteEntityRestBean> updatedEntities = new HashSet<>();

        // Second iteration - Persist updated entities and map to bean
        for (SiteEntityRestBean patchEntity : patchEntities) {
            Boolean dateRestricted = patchEntity.getDateRestricted();
            switch(patchEntity.getType()) {
                case ASSESSMENT:
                    PublishedAssessmentFacade assessment = (PublishedAssessmentFacade) toolEntities.get(entityKey(patchEntity));
                    PublishedAssessmentData assessmentData = (PublishedAssessmentData) assessment.getData();
                    String assessmentId = assessment.getPublishedAssessmentId().toString();

                    AssessmentAccessControlIfc accessControl = publishedAssessmentService.loadPublishedAccessControl(
                            assessment.getPublishedAssessmentId());

                    Optional.ofNullable(patchEntity.getOpenDate())
                            .ifPresent(openDate -> accessControl.setStartDate(Date.from(openDate)));

                    Optional.ofNullable(patchEntity.getDueDate())
                            .ifPresent(dueDate -> accessControl.setDueDate(Date.from(dueDate)));

                    Optional.ofNullable(patchEntity.getCloseDate())
                            .ifPresent(closeDate -> accessControl.setDueDate(Date.from(closeDate)));

                    Optional.ofNullable(patchEntity.getGroupRefs())
                            .ifPresent(groupRefs -> {
                                AuthzQueriesFacadeAPI authzQueries = persistenceService.getAuthzQueriesFacade();
                                // Remove previous authorizations
                                authzQueries.removeAuthorizationByQualifierAndFunction(assessmentId, GROUP_TAKE_ASSESSMENT_PERM);

                                // Add authorization for each group
                                groupRefs.stream()
                                        .map(groupRef -> groupIdFromRef(groupRef))
                                        .forEach(groupId -> authzQueries.createAuthorization(groupId, GROUP_TAKE_ASSESSMENT_PERM, assessmentId));

                                // Set "release to" to site TITLE (?) or constant for groups
                                accessControl.setReleaseTo(groupRefs.isEmpty() ? site.getTitle() : AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS);
                            });

                    Optional.ofNullable(patchEntity.getTimeExceptions())
                            .ifPresent(timeExceptions -> {
                                List<ExtendedTime> extendedTimes = timeExceptions.stream()
                                        .map(TimeExceptionRestBean::toExtendedTime)
                                        .peek(timeException -> timeException.setPubAssessment(assessmentData))
                                        .collect(Collectors.toList());

                                extendedTimeFacade.saveEntriesPub(assessmentData, extendedTimes);;
                            });

                    publishedAssessmentService.saveOrUpdatePublishedAccessControl(accessControl);

                    PublishedAssessmentFacade updatedAssessment = publishedAssessmentService.getPublishedAssessment(patchEntity.getId(), true);
                    updatedEntities.add(SiteEntityRestBean.of(updatedAssessment, timeExceptionSet(updatedAssessment)));
                    break;
                case ASSIGNMENT:
                    Assignment assignment = (Assignment) toolEntities.get(entityKey(patchEntity));

                    Optional.ofNullable(patchEntity.getOpenDate())
                            .ifPresent(openDate -> assignment.setOpenDate(openDate));

                    Optional.ofNullable(patchEntity.getDueDate())
                            .ifPresent(dueDate -> assignment.setDueDate(dueDate));

                    Optional.ofNullable(patchEntity.getCloseDate())
                            .ifPresent(closeDate -> assignment.setCloseDate(closeDate));

                    Optional.ofNullable(patchEntity.getGroupRefs())
                            .ifPresent(groupRefs -> {
                                Set<String> assignmentGroups = assignment.getGroups();

                                // Remove old groups
                                assignmentGroups.clear();

                                if (groupRefs.isEmpty()) {
                                    // Assign assignment to entire site
                                    assignment.setTypeOfAccess(Assignment.Access.SITE);
                                } else {
                                    // Assign assignment to groups
                                    assignment.setTypeOfAccess(Assignment.Access.GROUP);
                                    // Add new groups
                                    assignmentGroups.addAll(groupRefs);
                                }
                            });

                    Assignment updatedAssignment;
                    try {
                        assignmentService.updateAssignment(assignment);
                        updatedAssignment = assignmentService.getAssignment(assignment.getId());
                    } catch (PermissionException | IdUnusedException e) {
                        // Permission already evaluated before and assignmentId should be safe
                        log.error("Pervious permission check was not sufficient: {}",
                                ExceptionUtils.getStackTrace(e));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    updatedEntities.add(SiteEntityRestBean.of(updatedAssignment));
                    break;
                case RESOURCE:
                case RESOURCE_FOLDER:
                    GroupAwareEdit resourceEdit = (GroupAwareEdit) toolEntities.get(entityKey(patchEntity));
                    Optional<Set<String>> optGroupRefs = Optional.ofNullable(patchEntity.getGroupRefs());
                    Instant resourceOpenDate = patchEntity.getOpenDate();
                    Instant resourceCloseDate = patchEntity.getCloseDate();
                    boolean updateAvailability = dateRestricted != null;

                    if (dateRestricted !=null && dateRestricted) {
                        resourceEdit.setAvailabilityInstant(false, resourceOpenDate, resourceCloseDate);
                    } else {
                        resourceEdit.setAvailabilityInstant(false, null, null);
                    }

                    if (optGroupRefs.isPresent()) {
                        try {
                            Set<String> groupRefs = optGroupRefs.get();
                            if (groupRefs.isEmpty()) {
                                if (AccessMode.GROUPED.equals(resourceEdit.getAccess())) {
                                    resourceEdit.clearGroupAccess();
                                }
                            } else {
                                resourceEdit.setGroupAccess(Set.copyOf(groupRefs));
                            }
                        } catch (PermissionException e) {
                            log.error("Pervious permission check was not sufficient: {}",
                                    ExceptionUtils.getStackTrace(e));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        } catch (InconsistentException e) {
                            log.error("InconsistentException setting group access to resource: {}",
                                    ExceptionUtils.getStackTrace(e));
                        }
                    }

                    if (updateAvailability || optGroupRefs.isPresent()) {
                        try {
                            if (SiteEntityType.RESOURCE.equals(patchEntity.getType())) {
                                contentHostingService.commitResource((ContentResourceEdit) resourceEdit);
                            } else {
                                contentHostingService.commitCollection((ContentCollectionEdit) resourceEdit);
                            }
                        } catch (ServerOverloadException | OverQuotaException e) {
                            log.error("Could not commit resource edit due to {}: {}", e.toString(),
                                    ExceptionUtils.getStackTrace(e));
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                    }

                    ContentEntity updatedResource;
                    try {
                        if (SiteEntityType.RESOURCE.equals(patchEntity.getType())) {
                            updatedResource = contentHostingService.getResource(patchEntity.getId());
                        } else {
                            updatedResource = contentHostingService.getCollection(patchEntity.getId());
                        }
                    } catch (PermissionException | IdUnusedException | TypeException e) {
                        // This should be safe at this point
                        log.error("Pervious check was not sufficient: {} {}", e.toString(),
                                ExceptionUtils.getStackTrace(e));
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    updatedEntities.add(SiteEntityRestBean.of(updatedResource));
                    break;
                case FORUM:
                    DiscussionForum forum = (DiscussionForum) toolEntities.get(entityKey(patchEntity));

                    if (dateRestricted != null && dateRestricted) {
                        forum.setOpenDate(Date.from(patchEntity.getOpenDate()));
                        forum.setCloseDate(Date.from(patchEntity.getCloseDate()));
                        forum.setAvailabilityRestricted(true);
                        forum.setAvailability(patchEntity.getOpenDate().isBefore(Instant.now())
                                && patchEntity.getCloseDate().isAfter(Instant.now()));
                    } else {
                        forum.setOpenDate(null);
                        forum.setCloseDate(null);
                        forum.setAvailabilityRestricted(false);
                        forum.setAvailability(false);
                    }

                    DiscussionForum updatedForum = discussionForumManager.saveForum(site.getId(), forum);
                    updatedEntities.add(SiteEntityRestBean.of(updatedForum));
                    break;
                default:
                    break;
            }
        }

        return ResponseEntity.ok(updatedEntities.stream()
                .sorted(SiteEntityRestBean.comparator())
                .collect(Collectors.toSet()));
    }

    private boolean canUpdateAssessment(String userId, PublishedAssessmentFacade assessment) {
        String siteRef = SITE_SEGMENT + assessment.getOwnerSiteId();

        return securityService.unlock(userId, SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, siteRef);
    }

    private boolean canUpdateAssignment(String userId, Assignment assignment) {
        String siteRef = SITE_SEGMENT + AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getContext();

        return securityService.unlock(userId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, siteRef)
                && securityService.unlock(userId, AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, siteRef);
    }

    private String entityKey(SiteEntityRestBean siteEntity) {
        return siteEntity.getType().toString() + ":" + siteEntity.getId();
    }

    private String groupIdFromRef(String groupRef) {
        return StringUtils.substringAfterLast(groupRef, "/");
    }

    private Set<TimeExceptionRestBean> timeExceptionSet(PublishedAssessmentFacade assessment) {
        ExtendedTimeFacade extendedTimeFacade = persistenceService.getExtendedTimeFacade();
        String siteId = assessment.getOwnerSiteId();

        PublishedAssessmentData assessmentData = new PublishedAssessmentData();
        BeanUtils.copyProperties(assessment, assessmentData);

        return extendedTimeFacade.getEntriesForPub(assessmentData).stream()
                .map(extendedTime -> TimeExceptionRestBean.of(siteId, extendedTime))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Optional<DiscussionForum> findForumInArea(Area forumArea, String forumId) {
        if (forumArea == null) {
            return Optional.empty();
        }

        Set<DiscussionForum> forums = (Set<DiscussionForum>) forumArea.getOpenForumsSet();

        return forums.stream()
                .filter(forum -> Long.valueOf(forumId).equals(forum.getId()))
                .findAny();
    }

    private boolean isDateRestrictionPatchValid(SiteEntityRestBean patchEntity) {
        Boolean dateRestricted = patchEntity.getDateRestricted();
        Instant openDate = patchEntity.getOpenDate();
        Instant closeDate = patchEntity.getCloseDate();

        if (dateRestricted != null && dateRestricted) {
            if (openDate != null && closeDate != null) {
                log.debug("Open and close date present for dateRestriction=true -> valid");
                return true;
            } else {
                log.debug("Open and close date need to be set for dateRestriction=true -> invalid");
                return false;
            }
        } else {
            if (openDate == null && closeDate == null) {
                if (dateRestricted == null) {
                    log.debug("No date restriction change to handle -> valid");
                } else {
                    log.debug("Date restriction can be cleared -> valid");
                }
                return true;
            } else {
                log.debug("Can not set open or close date without dateRestriction=true -> invalid");
                return false;
            }
        }
    }
}
