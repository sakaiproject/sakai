/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.impl;

import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentEntity;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.ContentReviewResult;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.impl.sort.AnonymousSubmissionComparator;
import org.sakaiproject.assignment.impl.sort.AssignmentSubmissionComparator;
import org.sakaiproject.assignment.impl.sort.UserComparator;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Result;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService, EntityTransferrer, EntityTransferrerRefMigrator, ApplicationContextAware {

    @Setter private AnnouncementService announcementService;
    @Setter private ApplicationContext applicationContext;
    @Setter private AssignmentActivityProducer assignmentActivityProducer;
    @Setter private ObjectFactory<AssignmentEntity> assignmentEntityFactory;
    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentSupplementItemService assignmentSupplementItemService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private CalendarService calendarService;
    @Setter private CandidateDetailProvider candidateDetailProvider;
    @Setter private ContentHostingService contentHostingService;
    @Setter private ContentReviewService contentReviewService;
    @Setter private DigestService digestService;
    @Setter private EmailService emailService;
    @Setter private EmailUtil emailUtil;
    @Setter private EntityManager entityManager;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private FormattedText formattedText;
    @Setter private FunctionManager functionManager;
    @Setter private GradebookExternalAssessmentService gradebookExternalAssessmentService;
    @Setter private GradebookService gradebookService;
    @Setter private GradeSheetExporter gradeSheetExporter;
    @Setter private LearningResourceStoreService learningResourceStoreService;
    @Setter private LinkMigrationHelper linkMigrationHelper;
    @Setter private TransactionTemplate transactionTemplate;
    @Setter private ResourceLoader resourceLoader;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private TaggingManager taggingManager;
    @Setter private ToolManager toolManager;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private UserTimeService userTimeService;

    private boolean allowSubmitByInstructor;

    public void init() {
        allowSubmitByInstructor = serverConfigurationService.getBoolean("assignments.instructor.submit.for.student", true);
        if (!allowSubmitByInstructor) {
            log.info("Instructor submission of assignments is disabled - add assignments.instructor.submit.for.student=true to sakai config to enable");
        } else {
            log.info("Instructor submission of assignments is enabled");
        }

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        // register functions
        functionManager.registerFunction(SECURE_ALL_GROUPS);
        functionManager.registerFunction(SECURE_ADD_ASSIGNMENT);
        functionManager.registerFunction(SECURE_ADD_ASSIGNMENT_SUBMISSION);
        functionManager.registerFunction(SECURE_REMOVE_ASSIGNMENT);
        functionManager.registerFunction(SECURE_ACCESS_ASSIGNMENT);
        functionManager.registerFunction(SECURE_UPDATE_ASSIGNMENT);
        functionManager.registerFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION);
        functionManager.registerFunction(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS);
        functionManager.registerFunction(SECURE_SHARE_DRAFTS);

        // this is needed to avoid a circular dependency, notice we set the AssignmentService proxy and not this
        assignmentSupplementItemService.setAssignmentService(applicationContext.getBean(AssignmentService.class));
    }

    @Override
    public String getLabel() {
        return "assignment";
    }

    @Override
    public String getToolTitle() {
        Tool tool = toolManager.getTool(AssignmentServiceConstants.ASSIGNMENT_TOOL_ID);
        String toolTitle = null;

        if (tool == null)
            toolTitle = "Assignments";
        else
            toolTitle = tool.getTitle();

        return toolTitle;
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        String message = "archiving " + getLabel() + " context " + Entity.SEPARATOR + siteId + Entity.SEPARATOR + SiteService.MAIN_CONTAINER + ".\n";
        log.debug(message);

        // start with an element with our very own (service) name
        Element element = doc.createElement(AssignmentService.class.getName());
        stack.peek().appendChild(element);
        stack.push(element);

        Collection<Assignment> assignments = getAssignmentsForContext(siteId);
        for (Assignment assignment : assignments) {
            String xml = assignmentRepository.toXML(assignment);

            try {
                InputSource in = new InputSource(new StringReader(xml));
                Document assignmentDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                Element assignmentElement = assignmentDocument.getDocumentElement();
                Node assignmentNode = doc.importNode(assignmentElement, true);
                element.appendChild(assignmentNode);
            } catch (Exception e) {
                log.warn("could not append assignment {} to archive, {}", assignment.getId(), e.getMessage());
            }
        }

        stack.pop();

        return message;
    }

    @Override
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport) {
        return null;
    }

    @Override
    public boolean parseEntityReference(String stringReference, Reference reference) {
        if (StringUtils.startsWith(stringReference, REFERENCE_ROOT)) {
            AssignmentReferenceReckoner.AssignmentReference reckoner = AssignmentReferenceReckoner.reckoner().reference(stringReference).reckon();
            reference.set(SAKAI_ASSIGNMENT, reckoner.getSubtype(), reckoner.getId(), reckoner.getContainer(), reckoner.getContext());
            return true;
        }
        return false;
    }

    @Override
    public String getEntityDescription(Reference reference) {
        String description = "Assignment: " + reference.getReference();

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    Assignment a = getAssignment(reference.getReference());
                    description = "Assignment: " + a.getId() + " (" + a.getContext() + ")";
                    break;
                case REF_TYPE_SUBMISSION:
                    AssignmentSubmission s = getSubmission(reference.getReference());
                    description = "AssignmentSubmission: " + s.getId() + " (" + s.getAssignment().getContext() + ")";
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the entity description for ref = {}", reference.getReference(), e);
        }

        return description;
    }

    @Override
    public ResourceProperties getEntityResourceProperties(Reference reference) {
        ResourceProperties properties = null;

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    Assignment a = getAssignment(reference.getReference());
                    properties = new BaseResourceProperties(a.getProperties());
                    break;
                case REF_TYPE_SUBMISSION:
                    AssignmentSubmission s = getSubmission(reference.getReference());
                    properties = new BaseResourceProperties(s.getProperties());
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the entity properties for ref = {}", reference.getReference(), e);
        }

        return properties;
    }

    @Override
    public Entity getEntity(Reference reference) {
        Objects.requireNonNull(reference);
        Entity entity = null;
        switch (reference.getSubType()) {
            case REF_TYPE_CONTENT:
            case REF_TYPE_ASSIGNMENT:
                entity = createAssignmentEntity(reference.getId());
                break;
            case REF_TYPE_SUBMISSION:
                // TODO assignment submission entities
                log.warn("Submission Entity not implemented open a JIRA, reference: {}", reference.getReference());
                break;
            default:
                log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
        }

        return entity;
    }

    @Override
    public Entity createAssignmentEntity(String assignmentId) {
        AssignmentEntity entity = assignmentEntityFactory.getObject();
        entity.initEntity(assignmentId);
        return entity;
    }

    @Override
    public Entity createAssignmentEntity(Assignment assignment) {
        AssignmentEntity entity = assignmentEntityFactory.getObject();
        entity.initEntity(assignment);
        return entity;
    }

    @Override
    public String getEntityUrl(Reference reference) {
        return getEntity(reference).getUrl();
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference reference, String userId) {
        Collection<String> references = new ArrayList<>();

        // for AssignmentService assignments:
        // if access set to SITE, use the assignment and site authzGroups.
        // if access set to GROUP, use the assignment, and the groups, but not the site authzGroups.
        // if the user has SECURE_ALL_GROUPS in the context, ignore GROUP access and treat as if SITE

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    references.add(reference.getReference());

                    boolean grouped = false;
                    Collection groups = null;

                    // check SECURE_ALL_GROUPS - if not, check if the assignment has groups or not
                    // TODO: the last param needs to be a ContextService.getRef(ref.getContext())... or a ref.getContextAuthzGroup() -ggolden
                    if ((userId == null) || ((!securityService.isSuperUser(userId)) && (!securityService.unlock(userId, SECURE_ALL_GROUPS, siteService.siteReference(reference.getContext()))))) {
                        // get the channel to get the message to get group information
                        // TODO: check for efficiency, cache and thread local caching usage -ggolden
                        if (reference.getId() != null) {
                            Assignment a = getAssignment(reference);
                            if (a != null) {
                                grouped = Assignment.Access.GROUP == a.getTypeOfAccess();
                                groups = a.getGroups();
                            }
                        }
                    }

                    if (grouped) {
                        // groups
                        references.addAll(groups);
                    } else {
                        // not grouped
                        reference.addSiteContextAuthzGroup(references);
                    }
                    break;
                case REF_TYPE_SUBMISSION:
                    // for submission, use site security setting
                    references.add(reference.getReference());
                    reference.addSiteContextAuthzGroup(references);
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get the Entity's authz groups with ref = {}", reference.getReference(), e);
        }

        return references;
    }

    @Override
    public HttpAccess getHttpAccess() {
        return (req, res, ref, copyrightAcceptedRefs) -> {
            if (sessionManager.getCurrentSessionUserId() == null) {
                log.warn("Only logged in users can access assignment downloads");
            } else {
                // determine the type of download to create using the reference that was requested
                AssignmentReferenceReckoner.AssignmentReference refReckoner = AssignmentReferenceReckoner.reckoner().reference(ref.getReference()).reckon();
                if (REFERENCE_ROOT.equals("/" + refReckoner.getType())) {
                    // don't process any references that are not of type assignment
                    switch (refReckoner.getSubtype()) {
                        case REF_TYPE_CONTENT:
                        case REF_TYPE_ASSIGNMENT:
                            String date = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(userTimeService.getLocalTimeZone().toZoneId()).format(ZonedDateTime.now());
                            String queryString = req.getQueryString();
                            if (StringUtils.isNotBlank(refReckoner.getId())) {
                                // if subtype is assignment then were downloading all submissions for an assignment
                                try {
                                    Assignment a = getAssignment(refReckoner.getId());
                                    String filename = a.getTitle() + "_" + date;
                                    res.setContentType("application/zip");
                                    res.setHeader("Content-Disposition", "attachment; filename = \"" + filename + ".zip\"");

                                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                        @Override
                                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                                            try (OutputStream out = res.getOutputStream()) {
                                                getSubmissionsZip(out, ref.getReference(), queryString);
                                            } catch (Exception e) {
                                                log.warn("Could not stream the submissions for reference: {}", ref.getReference(), e);
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    log.warn("Could not find assignment for ref = {}", ref.getReference(), e);
                                }
                            } else {
                                String filename = "bulk_download_" + date;
                                // if subtype is assignment and there is no assignmentId then were downloading grades
                                res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                                res.setHeader("Content-Disposition", "attachment; filename = \"export_grades_" + filename + ".xlsx\"");

                                try (OutputStream out = res.getOutputStream()) {
                                    gradeSheetExporter.getGradesSpreadsheet(out, ref.getReference(), queryString);
                                } catch (Exception e) {
                                    log.warn("Could not stream the grades for reference: {}", ref.getReference(), e);
                                }
                            }
                            break;
                        case REF_TYPE_SUBMISSION:
                        default:
                            log.warn("Assignments download unhandled download type for reference: {}", ref.getReference());
                    }
                }
            }
        };
    }

    @Override
    public boolean allowReceiveSubmissionNotification(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString, null)) return true;
        return false;
    }

    @Override
    public List<User> allowReceiveSubmissionNotificationUsers(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return securityService.unlockUsers(SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS, resourceString);
    }

    @Override
    public boolean allowAddSiteAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return permissionCheck(SECURE_ADD_ASSIGNMENT, resourceString, null);
    }

    @Override
    public boolean allowAllGroups(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ALL_GROUPS, resourceString, null)) return true;
        return false;
    }

    @Override
    public boolean allowGetAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return permissionCheck(SECURE_ACCESS_ASSIGNMENT, resourceString, null);
    }

    @Override
    public Collection<Group> getGroupsAllowAddAssignment(String context) {
        return getGroupsAllowFunction(SECURE_ADD_ASSIGNMENT, context, null);
    }

    @Override
    public Collection<Group> getGroupsAllowUpdateAssignment(String context) {
        return getGroupsAllowFunction(SECURE_UPDATE_ASSIGNMENT, context, null);
    }

    @Override
    public Collection<Group> getGroupsAllowGradeAssignment(String assignmentReference) {
        AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon();
        if (allowGradeSubmission(assignmentReference)) {
            Collection<Group> groupsAllowed = getGroupsAllowFunction(SECURE_GRADE_ASSIGNMENT_SUBMISSION, referenceReckoner.getContext(), null);
            Assignment assignment = assignmentRepository.findAssignment(referenceReckoner.getId());
            if (assignment != null) {
                switch (assignment.getTypeOfAccess()) {
                    case SITE: // return all groups for site access
                        return groupsAllowed;
                    case GROUP: // return only matching groups for group access
                        Set<String> assignmentGroups = assignment.getGroups();
                        return groupsAllowed.stream().filter(g -> assignmentGroups.contains(g.getReference())).collect(Collectors.toSet());
                }
            }
        }
        return Collections.emptySet();
    }

    @Override
    public boolean allowUpdateAssignmentInContext(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_UPDATE_ASSIGNMENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which updates are allowed
        return (!getGroupsAllowUpdateAssignment(context).isEmpty());
    }

    @Override
    public boolean allowUpdateAssignment(String assignmentReference) {
        AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon();
        return allowUpdateAssignmentInContext(referenceReckoner.getContext());
    }

    @Override
    public boolean allowRemoveAssignment(String assignmentReference) {
        return permissionCheck(SECURE_REMOVE_ASSIGNMENT, assignmentReference, null);
    }

    @Override
    public Collection<Group> getGroupsAllowRemoveAssignment(String context) {
        return getGroupsAllowFunction(SECURE_REMOVE_ASSIGNMENT, context, null);
    }

    @Override
    public boolean allowAddAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ADD_ASSIGNMENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which adds are allowed
        return (!getGroupsAllowAddAssignment(context).isEmpty());
    }

    @Override
    public boolean allowRemoveAssignmentInContext(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_REMOVE_ASSIGNMENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which remove is allowed
        return (!getGroupsAllowRemoveAssignment(context).isEmpty());
    }

    @Override
    public boolean allowAddSubmission(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        return permissionCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, null);
    }

    @Override
    public boolean allowAddSubmissionCheckGroups(Assignment assignment) {
        return permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignment);
    }

    @Override
    public List<User> allowAddSubmissionUsers(String assignmentReference) {
        return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
    }

    @Override
    public List<User> allowGradeAssignmentUsers(String assignmentReference) {
        List<User> users = securityService.unlockUsers(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            Assignment a = getAssignment(assignmentId);
            if (a.getTypeOfAccess() == Assignment.Access.GROUP) {
                // for grouped assignment, need to include those users that with "all.groups" and "grade assignment" permissions on the site level
                try {
                    AuthzGroup group = authzGroupService.getAuthzGroup(siteService.siteReference(a.getContext()));
                    // get the roles which are allowed for submission but not for all_site control
                    Set<String> rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
                    Set<String> rolesAllowGradeAssignment = group.getRolesIsAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION);
                    // save all the roles with both "all.groups" and "grade assignment" permissions
                    if (rolesAllowAllSite != null)
                        rolesAllowAllSite.retainAll(rolesAllowGradeAssignment);
                    if (rolesAllowAllSite != null && rolesAllowAllSite.size() > 0) {
                        for (String aRolesAllowAllSite : rolesAllowAllSite) {
                            Set<String> userIds = group.getUsersHasRole(aRolesAllowAllSite);
                            if (userIds != null) {
                                for (String userId : userIds) {
                                    try {
                                        User u = userDirectoryService.getUser(userId);
                                        if (!users.contains(u)) {
                                            users.add(u);
                                        }
                                    } catch (Exception ee) {
                                        log.warn("problem with getting user = {}, {}", userId, ee.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (GroupNotDefinedException gnde) {
                    log.warn("Cannot get authz group for site = {}, {}", a.getContext(), gnde.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch assignment with assignmentId = {}", assignmentId, e);
        }

        return users;
    }

    @Override
    public List<String> allowAddAnySubmissionUsers(String context) {
        List<String> rv = new ArrayList<>();

        try {
            AuthzGroup group = authzGroupService.getAuthzGroup(context);

            // get the roles which are allowed for submission but not for all_site control
            Set<String> rolesAllowSubmission = group.getRolesIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);
            Set<String> rolesAllowAllSite = group.getRolesIsAllowed(SECURE_ALL_GROUPS);
            rolesAllowSubmission.removeAll(rolesAllowAllSite);

            for (String role : rolesAllowSubmission) {
                rv.addAll(group.getUsersHasRole(role));
            }
        } catch (Exception e) {
            log.warn("Could not get authz group where context = {}", context);
        }

        return rv;
    }

    @Override
    public List<User> allowAddAssignmentUsers(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString);
    }

    @Override
    public boolean allowGetSubmission(String submissionReference) {
        if (permissionCheck(SECURE_ACCESS_ASSIGNMENT_SUBMISSION, submissionReference, null)) return true;
        return permissionCheck(SECURE_ACCESS_ASSIGNMENT, submissionReference, null);
    }

    @Override
    public boolean allowUpdateSubmission(String submissionReference) {
        if (permissionCheck(SECURE_UPDATE_ASSIGNMENT_SUBMISSION, submissionReference, null)) return true;
        return permissionCheck(SECURE_UPDATE_ASSIGNMENT, submissionReference, null);
    }

    @Override
    public boolean allowRemoveSubmission(String submissionReference) {
        return permissionCheck(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, submissionReference, null);
    }

    @Override
    public boolean allowReviewService(Site site) {
        return serverConfigurationService.getBoolean("assignment.useContentReview", false) && contentReviewService != null && contentReviewService.isSiteAcceptable(site);
    }

    @Override
    public boolean allowGradeSubmission(String assignmentReference) {
        return permissionCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference, null);
    }

    @Override
    @Transactional
    public Assignment addAssignment(String context) throws PermissionException {
        // security check
        if (!allowAddAssignment(context)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null);
        }

        Assignment assignment = new Assignment();
        assignment.setContext(context);
        assignment.setAuthor(sessionManager.getCurrentSessionUserId());
        assignment.setPosition(0);
        assignmentRepository.newAssignment(assignment);

        log.debug("Created new assignment {}", assignment.getId());

        // String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        // AssignmentAction#post_save_assignment contains the logic for adding a new assignment
        // the event should come at the end of that logic, eventually that logic should be moved here
        // eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, reference, true));

        return assignment;
    }

    @Override
    @Transactional
    public Assignment mergeAssignment(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        // TODO need to write a test for this
        // this may also need to handle submission serialization?
        Assignment assignmentFromXml = assignmentRepository.fromXML(el.toString());

        return addAssignment(assignmentFromXml.getContext());
    }

    @Override
    @Transactional
    public Assignment addDuplicateAssignment(String context, String assignmentId) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException {
        Assignment assignment = null;

        if (StringUtils.isNoneBlank(context, assignmentId)) {
            if (!assignmentRepository.existsAssignment(assignmentId)) {
                throw new IdUnusedException(assignmentId);
            } else {
                if (!allowAddAssignment(context)) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null);
                }

                log.debug("duplicating assignment with ref = {}", assignmentId);

                Assignment existingAssignment = getAssignment(assignmentId);

                assignment = new Assignment();
                assignment.setContext(context);
                assignment.setAuthor(sessionManager.getCurrentSessionUserId());
                assignment.setTitle(existingAssignment.getTitle() + " - " + resourceLoader.getString("assignment.copy"));
                assignment.setInstructions(existingAssignment.getInstructions());
                assignment.setHonorPledge(existingAssignment.getHonorPledge());
                assignment.setSection(existingAssignment.getSection());
                assignment.setOpenDate(existingAssignment.getOpenDate());
                assignment.setDueDate(existingAssignment.getDueDate());
                assignment.setDropDeadDate(existingAssignment.getDropDeadDate());
                assignment.setCloseDate(existingAssignment.getCloseDate());
                assignment.setHideDueDate(existingAssignment.getHideDueDate());
                assignment.setDraft(true);
                assignment.setPosition(existingAssignment.getPosition());
                assignment.setIsGroup(existingAssignment.getIsGroup());
                assignment.setAllowPeerAssessment(existingAssignment.getAllowPeerAssessment());
                if (!existingAssignment.getGroups().isEmpty()) {
                	assignment.setGroups(new HashSet<>(existingAssignment.getGroups()));
                	assignment.setTypeOfAccess(Assignment.Access.GROUP);
                }

                // peer properties
                assignment.setPeerAssessmentInstructions(existingAssignment.getPeerAssessmentInstructions());
                assignment.setPeerAssessmentAnonEval(existingAssignment.getPeerAssessmentAnonEval());
                assignment.setPeerAssessmentNumberReviews(existingAssignment.getPeerAssessmentNumberReviews());
                assignment.setPeerAssessmentPeriodDate(existingAssignment.getPeerAssessmentPeriodDate());
                assignment.setPeerAssessmentStudentReview(existingAssignment.getPeerAssessmentStudentReview());

                assignment.setTypeOfSubmission(existingAssignment.getTypeOfSubmission());
                assignment.setTypeOfGrade(existingAssignment.getTypeOfGrade());
                assignment.setMaxGradePoint(existingAssignment.getMaxGradePoint());
                assignment.setScaleFactor(existingAssignment.getScaleFactor());
                assignment.setIndividuallyGraded(existingAssignment.getIndividuallyGraded());
                assignment.setReleaseGrades(existingAssignment.getReleaseGrades());
                assignment.setAllowAttachments(existingAssignment.getAllowAttachments());
                // for ContentReview service
                assignment.setContentReview(existingAssignment.getContentReview());

                //duplicating attachments
                Set<String> tempAttach = existingAssignment.getAttachments();
                if (tempAttach != null && !tempAttach.isEmpty()){
                    for (String attachId : tempAttach){
                        Reference tempRef = entityManager.newReference(attachId);
                        if (tempRef != null){
                            String tempRefId = tempRef.getId();
                            String tempRefCollectionId = contentHostingService.getContainingCollectionId(tempRefId);
                            try {
                                // get the original attachment display name
                                ResourceProperties p = contentHostingService.getProperties(tempRefId);
                                String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
                                // add another attachment instance
                                String newItemId = contentHostingService.copyIntoFolder(tempRefId, tempRefCollectionId);
                                ContentResourceEdit copy = contentHostingService.editResource(newItemId);
                                // with the same display name
                                ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
                                pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
                                contentHostingService.commitResource(copy, NotificationService.NOTI_NONE);
                                Reference newRef = entityManager.newReference(copy.getReference());
                                assignment.getAttachments().add(newRef.getReference());
                            } catch (Exception e){
                                log.warn("ERROR DUPLICATING ATTACHMENTS : " + e.toString());
                            }
                        }
                    }
                }

                Map<String, String> properties = assignment.getProperties();
                existingAssignment.getProperties().entrySet().stream()
                        .filter(e -> !PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS.contains(e.getKey()))
                        .forEach(e -> properties.put(e.getKey(), e.getValue()));

                assignmentRepository.newAssignment(assignment);
                log.debug("Created duplicate assignment {} from {}", assignment.getId(), assignmentId);

                String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                // event for tracking
                eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, reference, true));
            }
        }
        return assignment;
    }

    @Override
    @Transactional
    public void deleteAssignment(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        log.debug("Attempting to delete assignment with id = {}", assignment.getId());
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        if (!allowRemoveAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT, null);
        }

        assignmentRepository.deleteAssignment(assignment.getId());

        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, reference, true));

        // remove any realm defined for this resource
        try {
            authzGroupService.removeAuthzGroup(reference);
            log.debug("successful delete for assignment with id = {}", assignment.getId());
        } catch (AuthzPermissionException e) {
            log.warn("deleting realm for assignment reference = {}", reference, e);
        }
    }

    @Override
    @Transactional
    public void softDeleteAssignment(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        // we don't actually want to delete assignments just mark them as deleted "soft delete feature"
        log.debug("Attempting to soft delete assignment with id = {}", assignment.getId());
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        if (!allowRemoveAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT, null);
        }

        assignmentRepository.softDeleteAssignment(assignment.getId());

        // we post the same event as remove assignment
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, reference, true));
    }

    // TODO removing related content from other tools shouldn't be the concern for assignments service
    // it should post an event and let those tools take action.
    // * Unless a transaction is required
    @Override
    @Transactional
    public void deleteAssignmentAndAllReferences(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        log.debug("Removing all associated reference to assignment with id = {}", assignment.getId());

        Entity entity = createAssignmentEntity(assignment.getId());

        // CHECK PERMISSION
        permissionCheck(SECURE_REMOVE_ASSIGNMENT, entity.getReference(), null);

        // we may need to remove associated calendar events and annc, so get the basic info here
//            ResourcePropertiesEdit pEdit = assignment.getPropertiesEdit();
//            String context = assignment.getContext();

        // 1. remove associated calendar events, if exists
        removeAssociatedCalendarItem(getCalendar(assignment.getContext()), assignment);

        // 2. remove associated announcement, if exists
        removeAssociatedAnnouncementItem(getAnnouncementChannel(assignment.getContext()), assignment);

        // 3. remove Gradebook items, if linked
        removeAssociatedGradebookItem(assignment, assignment.getContext());

        // 4. remove tags as necessary
        removeAssociatedTaggingItem(assignment);

        // 5. remove assignment submissions
//            List submissions = getSubmissions(assignment);
//            if (submissions != null) {
//                for (Iterator sIterator = submissions.iterator(); sIterator.hasNext(); ) {
//                    AssignmentSubmission s = (AssignmentSubmission) sIterator.next();
//                    String sReference = s.getReference();
//                    try {
//                        removeSubmission(editSubmission(sReference));
//                    } catch (PermissionException e) {
//                        M_log.warn("removeAssignmentAndAllReference: User does not have permission to remove submission " + sReference + " for assignment: " + assignment.getId() + e.getMessage());
//                    } catch (InUseException e) {
//                        M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " is in use. " + e.getMessage());
//                    } catch (IdUnusedException e) {
//                        M_log.warn("removeAssignmentAndAllReference: submission " + sReference + " for assignment: " + assignment.getId() + " does not exist. " + e.getMessage());
//                    }
//                }
//            }

        // 6. remove associated content object
//            try {
//                removeAssignmentContent(editAssignmentContent(assignment.getContent().getReference()));
//            } catch (AssignmentContentNotEmptyException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): cannot remove non-empty AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (PermissionException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): not allowed to remove AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (InUseException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): AssignmentContent object for assignment = " + assignment.getId() + " is in used. " + e.getMessage());
//            } catch (IdUnusedException e) {
//                M_log.warn(" deleteAssignmentAndAllReferences(): cannot find AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            }

        // 7. remove assignment
        softDeleteAssignment(assignment);

        // close the edit object
//            ((BaseAssignmentEdit) assignment).closeEdit();

        // 8. remove any realm defined for this resource
//            try {
//                authzGroupService.removeAuthzGroup(assignment.getReference());
//            } catch (AuthzPermissionException e) {
//                M_log.warn(" deleteAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
//            }
//
//            // track event
//            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));
    }

    @Override
    @Transactional
    public AssignmentSubmission addSubmission(String assignmentId, String submitter) throws PermissionException {
        try {
            Assignment assignment = getAssignment(assignmentId);

            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
            if (assignment.getTypeOfAccess() == Assignment.Access.GROUP) {
                if (!permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignment)) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
                }
            } else {
                if (!permissionCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference, null)) {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
                }
            }

            AssignmentSubmission submission = new AssignmentSubmission();
            Set<AssignmentSubmissionSubmitter> submissionSubmitters = new HashSet<>();

            try {
                Site site = siteService.getSite(assignment.getContext());

                if (assignment.getIsGroup()) {
                    Group group = site.getGroup(submitter);
                    if (group != null && assignment.getGroups().contains(group.getReference())) {
                    	group.getMembers().stream()
                    	.filter(m -> (m.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)
                    			|| group.isAllowed(m.getUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION))
                    			&& !m.getRole().isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)
                    			&& !group.isAllowed(m.getUserId(), SECURE_GRADE_ASSIGNMENT_SUBMISSION))
                    	.forEach(member -> {
                			AssignmentSubmissionSubmitter ass = new AssignmentSubmissionSubmitter();
                    		ass.setSubmitter(member.getUserId());
                    		submissionSubmitters.add(ass);
                    	});
                    	submission.setGroupId(submitter);
                    } else {
                        log.warn("A submission cannot be added to assignment: {}, for group: {}", assignmentId, submitter);
                    }
                } else {
                    if (site.getMember(submitter) != null) {
                        AssignmentSubmissionSubmitter submissionSubmitter = new AssignmentSubmissionSubmitter();
                        submissionSubmitter.setSubmitter(submitter);
                        submissionSubmitters.add(submissionSubmitter);
                    } else {
                        log.warn("Unknown submitter while adding a submission to assignment: {}", assignmentId);
                    }
                }
            } catch (IdUnusedException iue) {
                log.warn("Site not found while attempting to add a submission to assignment: {}, site: {}", assignmentId, assignment.getContext());
                return null;
            }

            String currentUser = sessionManager.getCurrentSessionUserId();
            // identify who the submittee is using the session
            submissionSubmitters.stream().filter(s -> s.getSubmitter().equals(currentUser)).findFirst().ifPresent(s -> s.setSubmittee(true));

            assignmentRepository.newSubmission(assignment, submission, Optional.of(submissionSubmitters), Optional.empty(), Optional.empty(), Optional.empty());

            String submissionReference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION, submissionReference, true));

            log.debug("New submission: {} added to assignment: {}", submission.getId(), assignmentId);
            return submission;
        } catch (IdUnusedException iue) {
            log.warn("A submission cannot be added to an unknown assignment: {}", assignmentId);
        }

        return null;
    }

    @Override
    public AssignmentSubmission mergeSubmission(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        // TODO this will probably be handled in merge Assignments as submissions are children of assignments
//        AssignmentSubmission submissionFromXml = new AssignmentSubmission();
//
//        // check for a valid submission name
//        if (!Validator.checkResourceId(submissionFromXml.getId())) throw new IdInvalidException(submissionFromXml.getId());
//
//        // check security (throws if not permitted)
//        unlock(SECURE_ADD_ASSIGNMENT_SUBMISSION, submissionFromXml.getReference());
//
//        // reserve a submission with this id from the info store - if it's in use, this will return null
//        AssignmentSubmissionEdit submission = m_submissionStorage.put(	submissionFromXml.getId(),
//                submissionFromXml.getAssignmentId(),
//                submissionFromXml.getSubmitterIdString(),
//                (submissionFromXml.getTimeSubmitted() != null)?String.valueOf(submissionFromXml.getTimeSubmitted().getTime()):null,
//                Boolean.valueOf(submissionFromXml.getSubmitted()).toString(),
//                Boolean.valueOf(submissionFromXml.getGraded()).toString());
//        if (submission == null)
//        {
//            throw new IdUsedException(submissionFromXml.getId());
//        }
//
//        // transfer from the XML read submission object to the SubmissionEdit
//        ((BaseAssignmentSubmissionEdit) submission).set(submissionFromXml);
//
//        ((BaseAssignmentSubmissionEdit) submission).setEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION);
//
//        return submission;
        return null;
    }

    @Override
    @Transactional
    public void removeSubmission(AssignmentSubmission submission) throws PermissionException {
        if (submission != null) {
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            // check security
            if (!permissionCheck(SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference, null)) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_REMOVE_ASSIGNMENT_SUBMISSION, reference);
            }

            // remove submission
            assignmentRepository.deleteSubmission(submission.getId());

            // track event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT_SUBMISSION, reference, true));

            try {
                authzGroupService.removeAuthzGroup(authzGroupService.getAuthzGroup(reference));
            } catch (AuthzPermissionException e) {
                log.warn("removing realm for : {} : {}", reference, e.getMessage());
            } catch (GroupNotDefinedException e) {
                log.warn("cannot find group for submission : {} : {}", reference, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void updateAssignment(Assignment assignment) throws PermissionException {
        Assert.notNull(assignment, "Assignment cannot be null");
        Assert.notNull(assignment.getId(), "Assignment doesn't appear to have been persisted yet");

        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        // security check
        if (!allowUpdateAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT, null);
        }
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT, reference, true));

        assignment.setDateModified(Instant.now());
        assignmentRepository.update(assignment);

    }

    @Override
    @Transactional
    public void updateSubmission(AssignmentSubmission submission) throws PermissionException {
        Assert.notNull(submission, "Submission cannot be null");
        Assert.notNull(submission.getId(), "Submission doesn't appear to have been persisted yet");

        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        if (!allowUpdateSubmission(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT_SUBMISSION, null);
        }
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_SUBMISSION, reference, true));

        submission.setDateModified(Instant.now());
        assignmentRepository.updateSubmission(submission);

        // Assignment Submission Notifications
        Instant dateReturned = submission.getDateReturned();
        Instant dateSubmitted = submission.getDateSubmitted();
        if (!submission.getSubmitted()) {
            // if the submission is not submitted then saving a submission event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_SAVE_ASSIGNMENT_SUBMISSION, reference, true));
        } else if (dateReturned == null && !submission.getReturned() && (dateSubmitted == null || submission.getDateModified().toEpochMilli() - dateSubmitted.toEpochMilli() > 1000 * 60)) {
            // make sure the last modified time is at least one minute after the submit time
            if (!(StringUtils.trimToNull(submission.getSubmittedText()) == null && submission.getAttachments().isEmpty() && StringUtils.trimToNull(submission.getGrade()) == null && StringUtils.trimToNull(submission.getFeedbackText()) == null && StringUtils.trimToNull(submission.getFeedbackComment()) == null && submission.getFeedbackAttachments().isEmpty())) {
                if (submission.getGraded()) {
                	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
                    for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                        try {
                            User user = userDirectoryService.getUser(submitter.getSubmitter());
                            LRS_Statement statement = getStatementForAssignmentGraded(reference, submission.getAssignment(), submission, user);
                            // graded and saved before releasing it
                            Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                            eventTrackingService.post(event);
                        } catch (UserNotDefinedException e) {
                            log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                        }
                    }
                }
            }
        } else if (dateReturned != null && submission.getGraded() && (dateSubmitted == null || dateReturned.isAfter(dateSubmitted) || dateSubmitted.isAfter(dateReturned) && submission.getDateModified().isAfter(dateSubmitted))) {
            if (submission.getGraded()) {
            	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
                for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                    try {
                        User user = userDirectoryService.getUser(submitter.getSubmitter());
                    	LRS_Statement statement = getStatementForAssignmentGraded(reference, submission.getAssignment(), submission, user);
                    	// releasing a submitted assignment or releasing grade to an unsubmitted assignment
                    	Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                    	eventTrackingService.post(event);
                    } catch (UserNotDefinedException e) {
                        log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                    }
                }
            }

            // if this is releasing grade, depending on the release grade notification setting, send email notification to student
            sendGradeReleaseNotification(submission);
        } else if (dateSubmitted == null) {
           	//TODO: This should use an LRS_Group when that exists rather than firing off individual events for each LRS_Actor KNL-1560
            for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                try {
                    User user = userDirectoryService.getUser(submitter.getSubmitter());
                    LRS_Statement statement = getStatementForUnsubmittedAssignmentGraded(reference, submission.getAssignment(), submission, user);
                    // releasing a submitted assignment or releasing grade to an unsubmitted assignment
                    Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement);
                    eventTrackingService.post(event);
                } catch (UserNotDefinedException e) {
                    log.warn("Assignments could not find user ({}) while registering Event for LRSS", submitter.getSubmitter());
                }
            }
        } else {
            // submitting a submission
            Assignment a = submission.getAssignment();
            LRS_Statement statement = getStatementForSubmitAssignment(a.getId(), serverConfigurationService.getAccessUrl(), a.getTitle());
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_SUBMIT_ASSIGNMENT_SUBMISSION, reference, null, true, NotificationService.NOTI_OPTIONAL, statement));

            // only doing the notification for real online submissions
            if (submission.getAssignment().getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                // instructor notification
                notificationToInstructors(submission, submission.getAssignment());

                // student notification, whether the student gets email notification once he submits an assignment
                notificationToStudent(submission);
            }
        }
    }

    @Override
    public Assignment getAssignment(Reference reference) throws IdUnusedException, PermissionException {
        Assert.notNull(reference, "Reference cannot be null");
        if (StringUtils.equals(SAKAI_ASSIGNMENT, reference.getType())) {
            return getAssignment(reference.getId());
        }
        return null;
    }

    @Override
    public Assignment getAssignment(String assignmentId) throws IdUnusedException, PermissionException {
        log.debug("GET ASSIGNMENT : ID : {}", assignmentId);

        Assignment assignment = assignmentRepository.findAssignment(assignmentId);
        if (assignment == null) throw new IdUnusedException(assignmentId);

        String currentUserId = sessionManager.getCurrentSessionUserId();
        // check security on the assignment

        return checkAssignmentAccessibleForUser(assignment, currentUserId);
    }

    @Override
    public AssignmentConstants.Status getAssignmentCannonicalStatus(String assignmentId) throws IdUnusedException, PermissionException {
        Assignment assignment = getAssignment(assignmentId);
        ZonedDateTime currentTime = ZonedDateTime.now();

        // TODO these status's should be an enum and translation should occur in tool
        if (assignment.getDraft()) {
            return AssignmentConstants.Status.DRAFT;
        } else if (assignment.getOpenDate().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.NOT_OPEN;
        } else if (assignment.getDueDate().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.OPEN;
        } else if ((assignment.getCloseDate() != null) && (assignment.getCloseDate().isBefore(currentTime.toInstant()))) {
            return AssignmentConstants.Status.CLOSED;
        } else {
            return AssignmentConstants.Status.DUE;
        }
    }

    @Override
    public Collection<Assignment> getAssignmentsForContext(String context) {
        log.debug("GET ASSIGNMENTS : CONTEXT : {}", context);
        List<Assignment> assignments = new ArrayList<>();
        if (StringUtils.isBlank(context)) return assignments;

        // access is checked for each assignment:
        //   - drafts accessible by owner or if user has share drafts permission
        //   - if assignment is restricted to groups only those in the group
        //   - minimally user needs read permission
        for (Assignment assignment : assignmentRepository.findAssignmentsBySite(context)) {
            if (assignment.getDraft()) {
                if (isDraftAssignmentVisible(assignment)) {
                    // only those who can see a draft assignment
                    assignments.add(assignment);
                }
            } else if (assignment.getTypeOfAccess() == Assignment.Access.GROUP) {
                if (permissionCheckWithGroups(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, assignment)) {
                    assignments.add(assignment);
                }
            } else if (allowGetAssignment(context)) {
                assignments.add(assignment);
            }
        }

        return assignments;
    }

    @Override
    public Collection<Assignment> getDeletedAssignmentsForContext(String context) {
        log.debug("GET DELETED ASSIGNMENTS : CONTEXT : {}", context);
        List<Assignment> assignments = new ArrayList<>();
        if (StringUtils.isBlank(context)) return assignments;

        return assignmentRepository.findDeletedAssignmentsBySite(context);
    }

    @Override
    public Map<Assignment, List<String>> getSubmittableAssignmentsForContext(String context) {
        Map<Assignment, List<String>> submittable = new HashMap<>();
        if (!allowGetAssignment(context)) {
            // no permission to read assignment in context
            return submittable;
        }

        try {
            Site site = siteService.getSite(context);
            Set<String> siteSubmitterIds = authzGroupService.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION, Arrays.asList(site.getReference()));
            Map<String, Set<String>> groupIdUserIds = new HashMap<>();
            for (Group group : site.getGroups()) {
                String groupRef = group.getReference();
                for (Member member : group.getMembers()) {
                    if (member.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)) {
                        if (!groupIdUserIds.containsKey(groupRef)) {
                            groupIdUserIds.put(groupRef, new HashSet<>());
                        }
                        groupIdUserIds.get(groupRef).add(member.getUserId());
                    }
                }
            }

            // TODO this called getAccessibleAssignments need to implement
            Collection<Assignment> assignments = getAssignmentsForContext(context);
            for (Assignment assignment : assignments) {
                Set<String> userIds = new HashSet<>();
                if (assignment.getTypeOfAccess() == Assignment.Access.GROUP) {
                    for (String groupRef : assignment.getGroups()) {
                        if (groupIdUserIds.containsKey(groupRef)) {
                            userIds.addAll(groupIdUserIds.get(groupRef));
                        }
                    }
                } else {
                    userIds.addAll(siteSubmitterIds);
                }
                submittable.put(assignment, new ArrayList<>(userIds));
            }
        } catch (IdUnusedException e) {
            log.debug("Could not retrieve submittable assignments for nonexistent site: {}", context);
        }

        return submittable;
    }

    @Override
    public AssignmentSubmission getSubmission(String submissionId) throws IdUnusedException, PermissionException {
        return assignmentRepository.findSubmission(submissionId);
    }

    @Override
    public AssignmentSubmission getSubmission(String assignmentId, User person) throws PermissionException {
        return getSubmission(assignmentId, person.getId());
    }

    @Override
    public AssignmentSubmission getSubmission(String assignmentId, String submitterId) throws PermissionException {

        if (!StringUtils.isAnyBlank(assignmentId, submitterId)) {
            // normal submission lookup where submitterId is for a user
            AssignmentSubmission submission = assignmentRepository.findSubmissionForUser(assignmentId, submitterId);
            if (submission == null) {
                // if not found submitterId could be a group id
                submission = assignmentRepository.findSubmissionForGroup(assignmentId, submitterId);
            }

            if (submission != null) {
                String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
                if (allowGetSubmission(reference)) {
                    return submission;
                } else {
                    throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ACCESS_ASSIGNMENT_SUBMISSION, reference);
                }
            } else {
                // submission not found looked for a user submission and group submission
                log.debug("No submission found for user {} in assignment {}", submitterId, assignmentId);
            }
        }
        return null;
    }

    private Map<User, AssignmentSubmission> getUserSubmissionMap(Assignment assignment) {
        Map<User, AssignmentSubmission> userSubmissionMap = new HashMap<>();
        if (assignment != null) {
            if (assignment.getIsGroup()) {
                // All this block does is some verification of members in the group and the submissions submitters
                try {
                    Site site = siteService.getSite(assignment.getContext());
                    for (AssignmentSubmission submission : assignment.getSubmissions()) {
                        String gid = submission.getGroupId();
                        if (StringUtils.isNotBlank(gid)) {
                            Group group = site.getGroup(gid);
                            if (group != null) {
                                Set<String> members = group.getMembers().stream().map(Member::getUserId).collect(Collectors.toSet());
                                Set<String> submitters = submission.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toSet());
                                log.debug("Checking for consistency of group members [{}] to submitters [{}]", members.size(), submitters.size());
                                if (Collections.disjoint(members, submitters)) {
                                    log.warn("DISJOINT group members and submitters detected");
                                    List<String> submittersNotMembers = submitters.stream().filter(s -> !members.contains(s)).collect(Collectors.toList());
                                    log.warn("DISJOINT there are {} submitters that are not a member of a group: {}", submittersNotMembers.size(), submittersNotMembers);
                                    List<String> membersNotSubmitters = members.stream().filter(s -> !submitters.contains(s)).collect(Collectors.toList());
                                    log.warn("DISJOINT there are {} members that are not a submitter: {}", membersNotSubmitters.size(), membersNotSubmitters);
                                } else {
                                    log.debug("All members of group: {}::{} are submitters", gid, group.getTitle());
                                }
                            } else {
                                log.warn("Submission contains a group that doesn't exist in the site, submission: {}, group: {}", submission.getId(), gid);
                                break;
                            }
                        }
                    }
                } catch (IdUnusedException e) {
                    log.warn("Could not fetch site for assignment: {} with a context of: {}");
                }
            }
            // Simply we add every AssignmentSubmissionSubmitter to the Map, this works equally well for group submissions
            for (AssignmentSubmission submission : assignment.getSubmissions()) {
                for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                    try {
                        User user = userDirectoryService.getUser(submitter.getSubmitter());
                        userSubmissionMap.put(user, submission);
                    } catch (UserNotDefinedException e) {
                        log.warn("Could not find user: {}, that is a submitter for submission: {}", submitter.getSubmitter(), submission.getId());
                    }
                }
            }
        }
        return userSubmissionMap;
    }

    @Override
    public AssignmentSubmission getSubmission(List<AssignmentSubmission> submissions, User person) {
        throw new UnsupportedOperationException("Method is deprecated, remove");
    }

    @Override
    public Set<AssignmentSubmission> getSubmissions(Assignment assignment) {
        assignmentRepository.initializeAssignment(assignment);
        return assignment.getSubmissions();
    }

    @Override
    public String getAssignmentStatus(String assignmentId) {
        Assignment assignment = null;
        try {
            assignment = getAssignment(assignmentId);

            Instant now = Instant.now();
            if (assignment.getDraft()) {
                return resourceLoader.getString("gen.dra1");
            } else if (assignment.getOpenDate().isAfter(now)) {
                return resourceLoader.getString("gen.notope");
            } else if (assignment.getDueDate().isAfter(now)) {
                return resourceLoader.getString("gen.open");
            } else if ((assignment.getCloseDate() != null) && (assignment.getCloseDate().isBefore(now))) {
                return resourceLoader.getString("gen.closed");
            } else {
                return resourceLoader.getString("gen.due");
            }
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not determine the status for assignment: {}, {}", assignmentId, e.getMessage());
        }
        return null;
    }

    @Override
    public String getSubmissionStatus(String submissionId) {
        String status = "";
        AssignmentSubmission submission;
        try {
            submission = getSubmission(submissionId);
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not get submission with id {}, {}", submissionId, e.getMessage());
            return status;
        }
        Assignment assignment = submission.getAssignment();
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        boolean allowGrade = assignment != null && allowGradeSubmission(assignmentReference);

        Instant submitTime = submission.getDateSubmitted();
        Instant returnTime = submission.getDateReturned();
        Instant lastModTime = submission.getDateModified();

        if (submission.getSubmitted() || (!submission.getSubmitted() && allowGrade)) {
            if (submitTime != null) {
                if (submission.getReturned()) {
                    if (returnTime != null && returnTime.isBefore(submitTime)) {
                        if (!submission.getGraded()) {
                            status = resourceLoader.getString("gen.resub") + " " + getUsersLocalDateTimeString(submitTime);
                            if (submitTime.isAfter(assignment.getDueDate())) {
                                status = status + resourceLoader.getString("gen.late2");
                            }
                        } else
                            status = resourceLoader.getString("gen.returned");
                    } else
                        status = resourceLoader.getString("gen.returned");
                } else if (submission.getGraded() && allowGrade) {
                    status = StringUtils.isNotBlank(submission.getGrade()) ? resourceLoader.getString("grad3") : resourceLoader.getString("gen.commented");
                } else {
                    if (allowGrade) {
                        // ungraded submission
                        status = resourceLoader.getString("ungra");
                    } else {
                        status = resourceLoader.getString("gen.subm4") + " " + getUsersLocalDateTimeString(submitTime);
                    }
                }
            } else {
                if (submission.getReturned()) {
                    // instructor can return grading to non-submitted user
                    status = resourceLoader.getString("gen.returned");
                } else if (submission.getGraded() && allowGrade) {
                    // instructor can grade non-submitted ones
                    status = StringUtils.isNotBlank(submission.getGrade()) ? resourceLoader.getString("grad3") : resourceLoader.getString("gen.commented");
                } else {
                    if (allowGrade) {
                        // show "no submission" to graders
                        status = resourceLoader.getString("listsub.nosub");
                    } else {
                        // show "not started" to students
                        status = resourceLoader.getString("gen.notsta");
                    }
                }
            }
        } else {
            if (submission.getGraded()) {
                if (submission.getReturned()) {
                    // modified time is after returned time + 10 seconds
                    if (lastModTime != null && returnTime != null && lastModTime.isAfter(returnTime.plusSeconds(10)) && !allowGrade) {
                        // working on a returned submission now
                        status = resourceLoader.getString("gen.dra2") + " " + resourceLoader.getString("gen.inpro");
                    } else {
                        // not submitted submmission has been graded and returned
                        status = resourceLoader.getString("gen.returned");
                    }
                } else if (allowGrade) {
                    // grade saved but not release yet, show this to graders
                    status = StringUtils.isNotBlank(submission.getGrade()) ? resourceLoader.getString("grad3") : resourceLoader.getString("gen.commented");
                } else {
                    // submission saved, not submitted.
                    status = resourceLoader.getString("gen.dra2") + " " + resourceLoader.getString("gen.inpro");
                }
            } else {
                if (allowGrade)
                    status = resourceLoader.getString("ungra");
                else
                    // submission saved, not submitted.
                    status = resourceLoader.getString("gen.dra2") + " " + resourceLoader.getString("gen.inpro");
            }
        }

        return status;
    }

    // TODO this could probably be removed
    @Override
    public List<User> getSortedGroupUsers(Group g) {
        List<User> users = new ArrayList<>();
		g.getMembers().stream()
		.filter(m -> (m.getRole().isAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION)
				|| g.isAllowed(m.getUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION))
				&& !m.getRole().isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)
				&& !g.isAllowed(m.getUserId(), SECURE_GRADE_ASSIGNMENT_SUBMISSION))
		.forEach( member -> {
			try {
				users.add(userDirectoryService.getUser(member.getUserId()));
			} catch (Exception e) {
				log.warn("Creating a list of users, user = {}, {}", member.getUserId(), e.getMessage());
			}
		});
        users.sort(new UserComparator());
        return users;
    }

    @Override
    public int countSubmissions(String assignmentReference, Boolean graded) {
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            Assignment assignment = getAssignment(assignmentId);

            boolean isNonElectronic = false;
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                isNonElectronic = true;
            }
            // if the assignment is non-electronic don't include submission date or is user submission
            return (int) assignmentRepository.countAssignmentSubmissions(assignmentId, graded, !isNonElectronic, !isNonElectronic);
        } catch (Exception e) {
            log.warn("Couldn't count submissions for assignment reference {}, {}", assignmentReference, e.getMessage());
        }
        return 0;
    }

    @Override
    public byte[] getGradesSpreadsheet(String ref) throws IdUnusedException, PermissionException {
        return new byte[0];
    }

    @Override
    public void getSubmissionsZip(OutputStream out, String reference, String query) throws IdUnusedException, PermissionException {
        boolean withStudentSubmissionText = false;
        boolean withStudentSubmissionAttachment = false;
        boolean withGradeFile = false;
        boolean withFeedbackText = false;
        boolean withFeedbackComment = false;
        boolean withFeedbackAttachment = false;
        boolean withoutFolders = false;
        boolean includeNotSubmitted = false;
        String gradeFileFormat = "csv";
        String viewString = "";
        String contextString = "";
        String searchString = "";
        String searchFilterOnly = "";

        if (query != null) {
            StringTokenizer queryTokens = new StringTokenizer(query, "&");

            // Parsing the range list
            while (queryTokens.hasMoreTokens()) {
                String token = queryTokens.nextToken().trim();

                // check against the content elements selection
                if (token.contains("studentSubmissionText")) {
                    // should contain student submission text information
                    withStudentSubmissionText = true;
                } else if (token.contains("studentSubmissionAttachment")) {
                    // should contain student submission attachment information
                    withStudentSubmissionAttachment = true;
                } else if (token.contains("gradeFile")) {
                    // should contain grade file
                    withGradeFile = true;
                    if (token.contains("gradeFileFormat=csv")) {
                        gradeFileFormat = "csv";
                    } else if (token.contains("gradeFileFormat=excel")) {
                        gradeFileFormat = "excel";
                    }
                } else if (token.contains("feedbackTexts")) {
                    // inline text
                    withFeedbackText = true;
                } else if (token.contains("feedbackComments")) {
                    // comments  should be available
                    withFeedbackComment = true;
                } else if (token.contains("feedbackAttachments")) {
                    // feedback attachment
                    withFeedbackAttachment = true;
                } else if (token.contains("withoutFolders")) {
                    // feedback attachment
                    withoutFolders = true;
                } else if (token.contains("includeNotSubmitted")) {
                    // include empty submissions
                    includeNotSubmitted = true;
                } else if (token.contains("contextString")) {
                    // context
                    contextString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                } else if (token.contains("viewString")) {
                    // view
                    viewString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                } else if (token.contains("searchString")) {
                    // search
                    searchString = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                } else if (token.contains("searchFilterOnly")) {
                    // search and group filter only
                    searchFilterOnly = token.contains("=") ? token.substring(token.indexOf("=") + 1) : "";
                }
            }
        }

        byte[] rv = null;

        try {
            String id = AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getId();
            Assignment assignment = getAssignment(id);

            if (assignment.getIsGroup()) {
                Collection<Group> submitterGroups = getSubmitterGroupList(searchFilterOnly,
                        viewString.length() == 0 ? AssignmentConstants.ALL : viewString,
                        searchString,
                        id,
                        contextString == null ? assignment.getContext() : contextString);
                if (submitterGroups != null && !submitterGroups.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<>();
                    for (Group g : submitterGroups) {
                        log.debug("ZIP GROUP " + g.getTitle());
                        AssignmentSubmission sub = getSubmission(id, g.getId());
                        log.debug("ZIP GROUP " + g.getTitle() + " SUB " + (sub == null ? "null" : sub.getId()));
                        if (sub != null) {
                            submissions.add(sub);
                        }
                    }
                    StringBuilder exceptionMessage = new StringBuilder();

                    if (allowGradeSubmission(reference)) {
                        zipGroupSubmissions(reference,
                                assignment.getTitle(),
                                assignment.getTypeOfGrade().toString(),
                                assignment.getTypeOfSubmission(),
                                new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(applicationContext.getBean(AssignmentService.class), siteService, userDirectoryService)),
                                out,
                                exceptionMessage,
                                withStudentSubmissionText,
                                withStudentSubmissionAttachment,
                                withGradeFile,
                                withFeedbackText,
                                withFeedbackComment,
                                withFeedbackAttachment,
                                gradeFileFormat,
                                includeNotSubmitted);

                        if (exceptionMessage.length() > 0) {
                            // log any error messages
                            log.warn("Encountered an issue while zipping submissions for ref = {}, exception message {}", reference, exceptionMessage);
                        }
                    }
                }
            } else {

                //List<String> submitterIds = getSubmitterIdList(searchFilterOnly, viewString.length() == 0 ? AssignmentConstants.ALL:viewString, searchString, aRef, contextString == null? a.getContext():contextString);
                Map<User, AssignmentSubmission> submitters = getSubmitterMap(searchFilterOnly,
                        viewString.length() == 0 ? AssignmentConstants.ALL : viewString,
                        searchString,
                        reference,
                        contextString == null ? assignment.getContext() : contextString);

                if (!submitters.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<AssignmentSubmission>(submitters.values());

                    StringBuilder exceptionMessage = new StringBuilder();
                    SortedIterator sortedIterator;
                    if (assignmentUsesAnonymousGrading(assignment)){
                        sortedIterator = new SortedIterator(submissions.iterator(), new AnonymousSubmissionComparator());
                    } else {
                        sortedIterator = new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(applicationContext.getBean(AssignmentService.class), siteService, userDirectoryService));
                    }
                    if (allowGradeSubmission(reference)) {
                        zipSubmissions(reference,
                                assignment.getTitle(),
                                assignment.getTypeOfGrade(),
                                assignment.getTypeOfSubmission(),
                                sortedIterator,
                                out,
                                exceptionMessage,
                                withStudentSubmissionText,
                                withStudentSubmissionAttachment,
                                withGradeFile,
                                withFeedbackText,
                                withFeedbackComment,
                                withFeedbackAttachment,
                                withoutFolders,
                                gradeFileFormat,
                                includeNotSubmitted,
                                assignment.getContext());
                        if (exceptionMessage.length() > 0) {
                            log.warn("Encountered and issue while zipping submissions for ref = {}, exception message {}", reference, exceptionMessage);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Cannot create submissions zip file for reference = {}", reference, e);
        }
    }

    @Override
    public String assignmentReference(String context, String id) {
        return AssignmentReferenceReckoner.reckoner().context(context).id(id).reckon().getReference();
    }

    @Override
    public String assignmentReference(String id) {
        Assignment assignment = assignmentRepository.findAssignment(id);
        if (assignment != null) {
            return AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        }
        return null;
    }

    @Override
    public String submissionReference(String context, String id, String assignmentId) {
        return AssignmentReferenceReckoner.reckoner().context(context).id(id).container(assignmentId).subtype("s").reckon().getReference();
    }

    @Override
    public boolean canSubmit(String context, Assignment a, String userId) {
        // submissions are never allowed to non-electronic assignments
        if (a.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
            return false;
        }

        // return false if not allowed to submit at all
        if (!allowAddSubmissionCheckGroups(a) && !allowAddAssignment(context)) return false;

        //If userId is not defined look it up
        if (userId == null) {
            userId = sessionManager.getCurrentSessionUserId();
        }

        // if user can submit to this assignment
        Collection visibleAssignments = getAssignmentsForContext(context); // , userId); // TODO need to come up with a generic method for getting assignments for everyone
        if (visibleAssignments == null || !visibleAssignments.contains(a)) return false;

        try {
            // get user
            User u = userDirectoryService.getUser(userId);

            Instant currentTime = Instant.now();

            // return false if the assignment is draft or is not open yet
            Instant openTime = a.getOpenDate();
            if (a.getDraft() || openTime.isAfter(currentTime)) {
                return false;
            }

            // return false if the current time has passed the assignment close time
            Instant closeTime = a.getCloseDate();

            // get user's submission
            AssignmentSubmission submission = null;

            submission = getSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getId(), u);

            // check for allow resubmission or not first
            // return true if resubmission is allowed and current time is before resubmission close time
            // get the resubmit settings from submission object first
            String allowResubmitNumString = submission != null ? submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) : null;
            if (allowResubmitNumString != null && submission.getDateSubmitted() != null && submission.getSubmitted()) {
                try {
                    int allowResubmitNumber = Integer.parseInt(allowResubmitNumString);
                    String allowResubmitCloseTime = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                    Instant resubmitCloseTime = null;

                    if (allowResubmitCloseTime != null) {
                        // see if a resubmission close time is set on submission level
                        resubmitCloseTime = Instant.ofEpochMilli(Long.parseLong(allowResubmitCloseTime));
                    } else {
                        // otherwise, use assignment close time as the resubmission close time
                        resubmitCloseTime = a.getCloseDate();
                    }
                    return (allowResubmitNumber > 0 || allowResubmitNumber == -1) && currentTime.isBefore(resubmitCloseTime);
                } catch (NumberFormatException e) {
                    log.warn("allowResubmitNumString = {}", allowResubmitNumString, e);
                }
            }

            if (submission == null || submission.getDateSubmitted() == null) {
                // if there is no submission yet
                if (currentTime.isAfter(closeTime)) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (!submission.getSubmitted() && !currentTime.isAfter(closeTime)) {
                    // return true for drafted submissions
                    return true;
                } else
                    return false;
            }
        } catch (UserNotDefinedException e) {
            // cannot find user
            log.warn("Unknown user for assignment ref = {}", AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference());
        } catch (PermissionException e) {
            log.warn("User does not have permission, {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean canSubmit(String context, Assignment a) {
        return canSubmit(context, a, null);
    }

    @Override
    @Transactional
    public Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String assignmentId, String contextString) {
        Collection<Group> rv = new ArrayList<Group>();
        allOrOneGroup = StringUtils.trimToNull(allOrOneGroup);
        try {
            Assignment a = getAssignment(assignmentId);
            if (a != null) {
                Site st = siteService.getSite(contextString);
                if (StringUtils.equals(allOrOneGroup, AssignmentConstants.ALL) || StringUtils.isEmpty(allOrOneGroup)) {
                    if (a.getTypeOfAccess().equals(Assignment.Access.SITE)) {
                        for (Group group : st.getGroups()) {
                            //if (_gg.getProperties().get(GROUP_SECTION_PROPERTY) == null) {		// NO SECTIONS (this might not be valid test for manually created sections)
                            rv.add(group);
                            //}
                        }
                    } else {
                        for (String groupRef : a.getGroups()) {
                            Group group = st.getGroup(groupRef);        // NO SECTIONS (this might not be valid test for manually created sections)
                            if (group != null) {
                                rv.add(group);
                            }
                        }
                    }
                } else {
                    Group group = st.getGroup(allOrOneGroup);
                    if (group != null) {// && _gg.getProperties().get(GROUP_SECTION_PROPERTY) == null) {
                        rv.add(group);
                    }
                }

                for (Group g : rv) {
                    AssignmentSubmission uSubmission = getSubmission(assignmentId, g.getId());
                    if (uSubmission == null) {
                        if (allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference())) {
                            if (a.getIsGroup()) {
                                // temporarily allow the user to read and write from assignments (asn.revise permission)
                                SecurityAdvisor securityAdvisor = new MySecurityAdvisor(
                                        sessionManager.getCurrentSessionUserId(),
                                        new ArrayList<>(Arrays.asList(SECURE_ADD_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT_SUBMISSION)),
                                        ""/* no submission id yet, pass the empty string to advisor*/);
                                try {
                                    securityService.pushAdvisor(securityAdvisor);
                                    log.debug("context {} for assignment {} for group {}", contextString, a.getId(), g.getId());
                                    AssignmentSubmission s = addSubmission(a.getId(), g.getId());
                                    s.setSubmitted(true);
                                    s.setUserSubmission(false);
                                    s.setDateModified(Instant.now());

                                    // set the resubmission properties
                                    // get the assignment setting for resubmitting
                                    Map<String, String> assignmentProperties = a.getProperties();
                                    String assignmentAllowResubmitNumber = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                    if (assignmentAllowResubmitNumber != null) {
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                                        String assignmentAllowResubmitCloseDate = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                                        // if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME,
                                                assignmentAllowResubmitCloseDate != null ? assignmentAllowResubmitCloseDate : String.valueOf(a.getCloseDate().toEpochMilli()));
                                    }

                                    assignmentRepository.updateSubmission(s);
                                    // clear the permission
                                } catch (Exception e) {
                                    log.warn("exception thrown while creating empty submission for group who has not submitted, {}", e.getMessage());
                                } finally {
                                    securityService.popAdvisor(securityAdvisor);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IdUnusedException aIdException) {
            log.warn("Assignment id not used: {}, {}", assignmentId, aIdException.getMessage());
        } catch (PermissionException aPerException) {
            log.warn("Not allowed to get assignment {}, {}", assignmentId, aPerException.getMessage());
        }

        return rv;
    }

    @Override
    public boolean getAllowSubmitByInstructor() {
        return allowSubmitByInstructor;
    }

    @Override
    @Transactional
    public List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
        List<String> rv = new ArrayList<>();
        Map<User, AssignmentSubmission> submitterMap = getSubmitterMap(searchFilterOnly, allOrOneGroup, searchString, aRef, contextString);
        for (User u : submitterMap.keySet()) {
            rv.add(u.getId());
        }

        return rv;
    }

    @Override
    @Transactional
    public Map<User, AssignmentSubmission> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
        Map<User, AssignmentSubmission> rv = new HashMap<>();
        if (StringUtils.isBlank(aRef)) return rv;

        List<User> rvUsers;
        allOrOneGroup = StringUtils.trimToNull(allOrOneGroup);
        searchString = StringUtils.trimToNull(searchString);
        boolean bSearchFilterOnly = "true".equalsIgnoreCase(searchFilterOnly);

        try {
            String id = AssignmentReferenceReckoner.reckoner().reference(aRef).reckon().getId();
            Assignment a = getAssignment(id);

            if (a == null) return rv;

            // SAK-27824
            if (assignmentUsesAnonymousGrading(a)) {
                bSearchFilterOnly = false;
                searchString = "";
            }

            if (bSearchFilterOnly) {
                if (allOrOneGroup == null && searchString == null) {
                    // if the option is set to "Only show user submissions according to Group Filter and Search result"
                    // if no group filter and no search string is specified, no user will be shown first by default;
                    return rv;
                } else {
                    List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);
                    if (allOrOneGroup == null) {
                        // search is done for all submitters
                        rvUsers = getSearchedUsers(searchString, allowAddSubmissionUsers, false);
                    } else {
                        // group filter first
                        rvUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);
                        if (searchString != null) {
                            // then search
                            rvUsers = getSearchedUsers(searchString, rvUsers, true);
                        }
                    }
                }
            } else {
                List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(aRef);

                // SAK-28055 need to take away those users who have the permissions defined in sakai.properties
                String resourceString = AssignmentReferenceReckoner.reckoner().context(a.getContext()).reckon().getReference();
                String[] permissions = serverConfigurationService.getStrings("assignment.submitter.remove.permission");
                if (permissions != null) {
                    for (String permission : permissions) {
                        allowAddSubmissionUsers.removeAll(securityService.unlockUsers(permission, resourceString));
                    }
                } else {
                    allowAddSubmissionUsers.removeAll(securityService.unlockUsers(SECURE_ADD_ASSIGNMENT, resourceString));
                }

                // Step 1: get group if any that is selected
                rvUsers = getSelectedGroupUsers(allOrOneGroup, contextString, a, allowAddSubmissionUsers);

                // Step 2: get all student that meets the search criteria based on previous group users. If search is null or empty string, return all users.
                rvUsers = getSearchedUsers(searchString, rvUsers, true);
            }

            if (!rvUsers.isEmpty()) {
                List<String> groupRefs = new ArrayList<String>();
                Map<User, AssignmentSubmission> userSubmissionMap = getUserSubmissionMap(a);
                for (User u : rvUsers) {
                    AssignmentSubmission uSubmission = userSubmissionMap.get(u);

                    if (uSubmission != null) {
                        rv.put(u, uSubmission);
                    } else {
                        // add those users who haven't made any submissions and with submission rights
                        //only initiate the group list once
                        if (groupRefs.isEmpty()) {
                            if (a.getTypeOfAccess() == Assignment.Access.SITE) {
                                // for site range assignment, add the site reference first
                                groupRefs.add(siteService.siteReference(contextString));
                            }
                            // add all groups inside the site
                            Collection<Group> groups = getGroupsAllowGradeAssignment(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference());
                            for (Group g : groups) {
                                groupRefs.add(g.getReference());
                            }
                        }
                        // construct fake submissions for grading purpose if the user has right for grading
                        if (allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference())) {
                            SecurityAdvisor securityAdvisor = new MySecurityAdvisor(
                                    sessionManager.getCurrentSessionUserId(),
                                    new ArrayList<>(Arrays.asList(SECURE_ADD_ASSIGNMENT_SUBMISSION, SECURE_UPDATE_ASSIGNMENT_SUBMISSION)),
                                    groupRefs/* no submission id yet, pass the empty string to advisor*/);
                            try {
                                // temporarily allow the user to read and write from assignments (asn.revise permission)
                                securityService.pushAdvisor(securityAdvisor);

                                AssignmentSubmission s = addSubmission(a.getId(), u.getId());
                                if (s != null) {
                                    // Note: If we had s.setSubmitted(false);, this would put it in 'draft mode'
                                    s.setSubmitted(true);
                                    /*
                                     * SAK-29314 - Since setSubmitted represents whether the submission is in draft mode state, we need another property. So we created isUserSubmission.
									 * This represents whether the submission was geenrated by a user.
									 * We set it to false because these submissions are generated so that the instructor has something to grade;
									 * the user did not in fact submit anything.
									 */
                                    s.setUserSubmission(false);
                                    s.setDateModified(Instant.now());

                                    // set the resubmission properties
                                    // get the assignment setting for resubmitting
                                    Map<String, String> assignmentProperties = a.getProperties();
                                    String assignmentAllowResubmitNumber = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                    if (assignmentAllowResubmitNumber != null) {
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                                        String assignmentAllowResubmitCloseDate = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                                        // if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                                        s.getProperties().put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME,
                                                assignmentAllowResubmitCloseDate != null ? assignmentAllowResubmitCloseDate : String.valueOf(a.getCloseDate().toEpochMilli()));
                                    }

                                    assignmentRepository.updateSubmission(s);
                                    rv.put(u, s);
                                }
                            } catch (Exception e) {
                                log.warn("Exception thrown while creating empty submission for student who has not submitted, {}", e.getMessage());
                            } finally {
                                // clear the permission
                                securityService.popAdvisor(securityAdvisor);
                            }
                        }
                    }
                }
            }
        } catch (IdUnusedException aIdException) {
            log.warn("Assignment id not used: {}, {}", aRef, aIdException.getMessage());
        } catch (PermissionException aPerException) {
            log.warn("Not allowed to get assignment {}, {}", aRef, aPerException.getMessage());
        }

        return rv;
    }

    private List<User> getSelectedGroupUsers(String allOrOneGroup, String contextString, Assignment a, List allowAddSubmissionUsers) {
        Collection<String> authzRefs = new ArrayList<>();

        List<User> selectedGroupUsers = new ArrayList<>();
        if (StringUtils.isNotBlank(allOrOneGroup)) {
            // now are we view all sections/groups or just specific one?
            if (allOrOneGroup.equals(AssignmentConstants.ALL)) {
                if (allowAllGroups(contextString)) {
                    // site range
                    try {
                        Site site = siteService.getSite(contextString);
                        authzRefs.add(site.getReference());
                    } catch (IdUnusedException e) {
                        log.warn("Cannot find site {} {}", contextString, e.getMessage());
                    }
                } else {
                    // get all those groups that user is allowed to grade
                    Collection<Group> groups = getGroupsAllowGradeAssignment(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference());
                    groups.forEach(g -> authzRefs.add(g.getReference()));
                }
            } else {
                // filter out only those submissions from the selected-group members
                try {
                    Group group = siteService.getSite(contextString).getGroup(allOrOneGroup);
                    authzRefs.add(group.getReference());
                } catch (IdUnusedException e) {
                    log.warn("Cannot add groupId = {}, {}", allOrOneGroup, e.getMessage());
                }
            }

            for (String ref : authzRefs) {
                try {
                    AuthzGroup group = authzGroupService.getAuthzGroup(ref);
                    for (String userId : group.getUsers()) {
                        // don't show user multiple times
                        try {
                            User u = userDirectoryService.getUser(userId);
                            if (u != null && allowAddSubmissionUsers.contains(u)) {
                                if (!selectedGroupUsers.contains(u)) {
                                    selectedGroupUsers.add(u);
                                }
                            }
                        } catch (UserNotDefinedException uException) {
                            log.warn("User not found with id = {}, {}", userId, uException.getMessage());
                        }
                    }
                } catch (GroupNotDefinedException gException) {
                    log.warn("Group not found with reference = {}, {}", ref, gException.getMessage());
                }
            }
        }
        return selectedGroupUsers;
    }

    private List<User> getSearchedUsers(String searchString, List<User> userList, boolean retain) {
        List<User> rv = new ArrayList<>();
        if (StringUtils.isNotBlank(searchString)) {
            searchString = searchString.toLowerCase();
            for (User u : userList) {
                // search on user sortname, eid, email
                String[] fields = {u.getSortName(), u.getEid(), u.getEmail()};
                List<String> l = Arrays.asList(fields);
                for (String s : l) {
                    if (StringUtils.containsIgnoreCase(s, searchString)) {
                        rv.add(u);
                        break;
                    }
                }
            }
        } else if (retain) {
            // retain the original list
            rv = userList;
        }
        return rv;
    }

    @Override
    public String escapeInvalidCharsEntry(String accentedString) {
        String decomposed = Normalizer.normalize(accentedString, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    public boolean assignmentUsesAnonymousGrading(Assignment assignment) {
        if (assignment != null) {
            return Boolean.valueOf(assignment.getProperties().get(AssignmentServiceConstants.NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
        }
        return false;
    }

    @Override
    public Integer getScaleFactor() {
        Integer decimals = serverConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT);
        return Double.valueOf(Math.pow(10.0, decimals)).intValue();
    }

    @Override
    public String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment, boolean allowAddAssignment, boolean allowSubmitAssignment) throws Exception {
        Assignment a = getAssignment(assignmentId);

        String assignmentContext = a.getContext(); // assignment context
        if (allowReadAssignment && a.getOpenDate().isBefore(ZonedDateTime.now().toInstant())) {
            // this checks if we want to display an assignment link
            try {
                Site site = siteService.getSite(assignmentContext);
                // site id
                ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                // Three different urls to be rendered depending on the
                // user's permission
                if (allowAddAssignment) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doView_assignment";
                } else if (allowSubmitAssignment) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doView_submission";
                } else {
                    // user can read the assignment, but not submit, so
                    // render the appropriate url
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + AssignmentReferenceReckoner.reckoner().context(context).id(assignmentId).reckon().getReference()
                            + "&panel=Main&sakai_action=doView_assignment_as_student";
                }
            } catch (IdUnusedException e) {
                // No site found
                throw new IdUnusedException("No site found while creating assignment url");
            }
        }
        return "";
    }

    @Override
    public String getDeepLink(String context, String assignmentId) throws Exception {
        boolean allowReadAssignment = allowGetAssignment(context);
        boolean allowAddAssignment = allowAddAssignment(context);
        boolean allowSubmitAssignment = allowAddSubmission(context);

        return getDeepLinkWithPermissions(context, assignmentId, allowReadAssignment, allowAddAssignment, allowSubmitAssignment);
    }

    @Override
    public String getCsvSeparator() {
        String defaultSeparator = ",";
        //If the decimal separator is a comma
        if (",".equals(formattedText.getDecimalSeparator())) {
            defaultSeparator = ";";
        }

        return serverConfigurationService.getString("csv.separator", defaultSeparator);
    }

    @Override
    public String getXmlAssignment(Assignment assignment) {
        return assignmentRepository.toXML(assignment);
    }

    // TODO getGradeForUser each submitter now has a column for grade
//    @Override
//    public String getGradeForSubmitter(String submissionId, String userId) {
//        if (m_grades != null) {
//            Iterator<String> _it = m_grades.iterator();
//            while (_it.hasNext()) {
//                String _s = _it.next();
//                if (_s.startsWith(id + "::")) {
//                    //return _s.endsWith("null") ? null: _s.substring(_s.indexOf("::") + 2);
//                    if(_s.endsWith("null"))
//                    {
//                        return null;
//                    }
//                    else
//                    {
//                        String grade=_s.substring(_s.indexOf("::") + 2);
//                        if (grade != null && grade.length() > 0 && !"0".equals(grade))
//                        {
//                            int factor = getAssignment().getContent().getFactor();
//                            int dec = (int)Math.log10(factor);
//                            String decSeparator = FormattedText.getDecimalSeparator();
//                            String decimalGradePoint = "";
//                            try
//                            {
//                                Integer.parseInt(grade);
//                                // if point grade, display the grade with factor decimal place
//                                int length = grade.length();
//                                if (length > dec) {
//                                    decimalGradePoint = grade.substring(0, grade.length() - dec) + decSeparator + grade.substring(grade.length() - dec);
//                                }
//                                else {
//                                    String newGrade = "0".concat(decSeparator);
//                                    for (int i = length; i < dec; i++) {
//                                        newGrade = newGrade.concat("0");
//                                    }
//                                    decimalGradePoint = newGrade.concat(grade);
//                                }
//                            }
//                            catch (NumberFormatException e) {
//                                try {
//                                    Float.parseFloat(grade);
//                                    decimalGradePoint = grade;
//                                }
//                                catch (Exception e1) {
//                                    return grade;
//                                }
//                            }
//                            // get localized number format
//                            NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,false);
//                            DecimalFormat dcformat = (DecimalFormat) nbFormat;
//                            // show grade in localized number format
//                            try {
//                                Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
//                                decimalGradePoint = nbFormat.format(dblGrade);
//                            }
//                            catch (Exception e) {
//                                return grade;
//                            }
//                            return decimalGradePoint;
//                        }
//                        else
//                        {
//                            return StringUtils.trimToEmpty(grade);
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public String getGradeForUserInGradeBook(String assignmentId, String userId) {
        if (StringUtils.isAnyBlank(assignmentId, userId)) return null;

        String rv = null;
        Assignment m;
        try {
            m = getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not get assignment with id = {}", assignmentId, e);
            return null;
        }

        String gAssignmentName = StringUtils.trimToEmpty(m.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
        String gradebookUid = m.getContext();
        org.sakaiproject.service.gradebook.shared.Assignment gradebookAssignment = gradebookService.getAssignment(gradebookUid, gAssignmentName);
        if (gradebookAssignment != null) {
            final GradeDefinition def = gradebookService.getGradeDefinitionForStudentForItem(gradebookUid, gradebookAssignment.getId(), userId);
            String gString = def.getGrade();
            if (StringUtils.isNotBlank(gString)) {
                try {
                    String decSeparator = formattedText.getDecimalSeparator();
                    rv = StringUtils.replace(gString, (",".equals(decSeparator) ? "." : ","), decSeparator);
                    NumberFormat nbFormat = formattedText.getNumberFormat(new Double(Math.log10(m.getScaleFactor())).intValue(), new Double(Math.log10(m.getScaleFactor())).intValue(), false);
                    DecimalFormat dcformat = (DecimalFormat) nbFormat;
                    Double dblGrade = dcformat.parse(rv).doubleValue();
                    rv = nbFormat.format(dblGrade);
                } catch (Exception e) {
                    log.warn("Could not format grade [{}]", gString, e);
                    return null;
                }
            }
        }
        return rv;
    }

    /**
     * Contains logic to consistently output a String based version of a grade
     * Interprets the grade using the scale for display
     *
     * This should probably be moved to a static utility class - ern
     *
     * @param grade
     * @param typeOfGrade
     * @param scaleFactor
     * @return
     */
    @Override
    public String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor) {
        String returnGrade = StringUtils.trimToEmpty(grade);

        switch (typeOfGrade) {
            case SCORE_GRADE_TYPE:
                if (!returnGrade.isEmpty() && !"0".equals(returnGrade)) {
                    int dec = new Double(Math.log10(scaleFactor)).intValue();
                    String decSeparator = formattedText.getDecimalSeparator();
                    String decimalGradePoint = null;
                    try {
                        Integer.parseInt(returnGrade);
                        // if point grade, display the grade with factor decimal place
                        if (returnGrade.length() > dec) {
                            decimalGradePoint = returnGrade.substring(0, returnGrade.length() - dec) + decSeparator + returnGrade.substring(returnGrade.length() - dec);
                        } else {
                            String newGrade = "0".concat(decSeparator);
                            for (int i = returnGrade.length(); i < dec; i++) {
                                newGrade = newGrade.concat("0");
                            }
                            decimalGradePoint = newGrade.concat(returnGrade);
                        }
                    } catch (NumberFormatException nfe1) {
                        log.debug("Could not parse grade [{}] as an Integer trying as a Float, {}", returnGrade, nfe1.getMessage());
                        try {
                            Float.parseFloat(returnGrade);
                            decimalGradePoint = returnGrade;
                        } catch (NumberFormatException nfe2) {
                            log.debug("Could not parse grade [{}] as a Float, {}", returnGrade, nfe2.getMessage());
                        }
                    }
                    // get localized number format
                    NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, false);
                    DecimalFormat dcformat = (DecimalFormat) nbFormat;
                    // show grade in localized number format
                    try {
                        Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
                        decimalGradePoint = nbFormat.format(dblGrade);
                        returnGrade = decimalGradePoint;
                    } catch (Exception e) {
                        log.warn("Could not parse grade [{}], {}", returnGrade, e.getMessage());
                    }
                }
                break;
            case UNGRADED_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("gen.nograd")) {
                    returnGrade = resourceLoader.getString("gen.nograd");
                }
                break;
            case PASS_FAIL_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Pass")) {
                    returnGrade = resourceLoader.getString("pass");
                } else if (returnGrade.equalsIgnoreCase("Fail")) {
                    returnGrade = resourceLoader.getString("fail");
                } else {
                    returnGrade = resourceLoader.getString("ungra");
                }
                break;
            case CHECK_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Checked")) {
                    returnGrade = resourceLoader.getString("gen.checked");
                } else {
                    returnGrade = resourceLoader.getString("ungra");
                }
                break;
            default:
                if (returnGrade.isEmpty()) {
                    returnGrade = resourceLoader.getString("ungra");
                }
        }
        return returnGrade;
    }

    @Override
    public String getMaxPointGradeDisplay(int factor, int maxGradePoint) {
        // formated to show factor decimal places, for example, 1000 to 100.0
        // get localized number format
        //        NumberFormat nbFormat = FormattedText.getNumberFormat((int)Math.log10(factor),(int)Math.log10(factor),false);
        // show grade in localized number format
        //        Double dblGrade = new Double(maxGradePoint/(double)factor);
        //        return nbFormat.format(dblGrade);
        return getGradeDisplay(Integer.toString(maxGradePoint), Assignment.GradeType.SCORE_GRADE_TYPE, factor);
    }

    @Override
    public Optional<AssignmentSubmissionSubmitter> getSubmissionSubmittee(AssignmentSubmission submission) {
        Objects.requireNonNull(submission, "Submission cannot be null");
        return submission.getSubmitters().stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst();
    }

    @Override
    public Collection<User> getSubmissionSubmittersAsUsers(AssignmentSubmission submission) {
        Objects.requireNonNull(submission, "Submission cannot be null");
        List<User> submitters = new ArrayList<>();
        for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
            try {
                User user = userDirectoryService.getUser(submitter.getSubmitter());
                submitters.add(user);
            } catch (UserNotDefinedException e) {
                log.warn("Could not find user with id: {}", submitter.getSubmitter());
            }
        }
        return submitters;
    }

    @Override
    public boolean isPeerAssessmentOpen(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            Instant now = Instant.now();
            return now.isBefore(assignment.getPeerAssessmentPeriodDate()) && now.isAfter(assignment.getCloseDate());
        }
        return false;
    }

    @Override
    public boolean isPeerAssessmentPending(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            return Instant.now().isBefore(assignment.getCloseDate());
        }
        return false;
    }

    @Override
    public boolean isPeerAssessmentClosed(Assignment assignment) {
        if (assignment.getAllowPeerAssessment()) {
            return Instant.now().isAfter(assignment.getPeerAssessmentPeriodDate());
        }
        return false;
    }

    private Collection<Group> getGroupsAllowFunction(String function, String context, String userId) {
        Collection<Group> rv = new HashSet<>();

        try {
            Site site = siteService.getSite(context);

            if (StringUtils.isBlank(userId)) {
                userId = sessionManager.getCurrentSessionUserId();
            }

            Collection<Group> groups = site.getGroups();
            // if the user has SECURE_ALL_GROUPS in the context (site), select all site groups
            if (securityService.unlock(userId, SECURE_ALL_GROUPS, siteService.siteReference(context))
                    && permissionCheck(function, siteService.siteReference(context), null)) {
                rv.addAll(groups);
            } else {
                // get a list of the group refs, which are authzGroup ids
                Set<String> groupRefs = groups.stream().map(Group::getReference).collect(Collectors.toSet());

                // ask the authzGroup service to filter them down based on function
                Set<String> allowedGroupRefs = authzGroupService.getAuthzGroupsIsAllowed(userId, function, groupRefs);

                // pick the Group objects from the site's groups to return, those that are in the allowedGroupRefs list
                rv = groups.stream().filter(g -> allowedGroupRefs.contains(g.getReference())).collect(Collectors.toSet());
            }
        } catch (IdUnusedException e) {
            log.debug("site {} not found, {}", context, e.getMessage());
        }

        return rv;
    }

    private Assignment checkAssignmentAccessibleForUser(Assignment assignment, String currentUserId) throws PermissionException {

        if (assignment.getTypeOfAccess() == Assignment.Access.GROUP) {
            String context = assignment.getContext();
            Collection<String> asgGroups = assignment.getGroups();
            Collection<Group> allowedGroups = getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, currentUserId);
            // reject and throw PermissionException if there is no intersection
            if (!allowAllGroups(context)
                    && !StringUtils.equals(assignment.getAuthor(), currentUserId)
                    && !CollectionUtils.containsAny(asgGroups, allowedGroups.stream().map(Group::getReference).collect(Collectors.toSet()))) {
                throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getId());
            }
        }

        if (allowAddAssignment(assignment.getContext())) {
            // always return for users that can add assignment in the context
            return assignment;
        } else if (isAvailableOrSubmitted(assignment, currentUserId)) {
            return assignment;
        }
        throw new PermissionException(currentUserId, SECURE_ACCESS_ASSIGNMENT, assignment.getId());
    }

    private boolean isAvailableOrSubmitted(Assignment assignment, String userId) {
        if (!assignment.getDeleted()) {
            // show not deleted, not draft, opened assignments
            // TODO do we need to use time zone
            Instant openTime = assignment.getOpenDate();
            Instant visibleTime = assignment.getVisibleDate();
            if ((openTime != null && Instant.now().isAfter(openTime))
                    || (visibleTime != null && Instant.now().isAfter(visibleTime))
                    && !assignment.getDraft()) {
                return true;
            }
        } else {
            try {
                if (assignment.getDeleted() &&
                        assignment.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION &&
                        getSubmission(assignment.getId(), userId) != null) {
                    // and those deleted but not non-electronic assignments but the user has made submissions to them
                    return true;
                }
            } catch (PermissionException e) {
                log.warn("User doesn't have permission to access submission, {}", e.getMessage());
            }
        }
        return false;
    }

    private boolean isDraftAssignmentVisible(Assignment assignment) {
        return StringUtils.equals(assignment.getAuthor(), userDirectoryService.getCurrentUser().getId()) // the author can see it
                || permissionCheck(SECURE_SHARE_DRAFTS, siteService.siteReference(assignment.getContext()), null); // any role user with share draft permission
    }

    private boolean permissionCheck(String permission, String resource, String user) {
        boolean access = false;
        if (!StringUtils.isAnyBlank(resource, permission)) {
            if (StringUtils.isBlank(user)) {
                access = securityService.unlock(permission, resource);
                log.debug("checking permission [{}] in context [{}] for current user: {}", permission, resource, access);
            } else {
                access = securityService.unlock(user, permission, resource);
                log.debug("checking permission [{}] in context [{}] for user [{}]: {}", permission, resource, user, access);
            }
        }
        return access;
    }

    private boolean permissionCheckWithGroups(String permission, Assignment assignment) {
        Collection<String> groupIds = assignment.getGroups();
        if (groupIds.isEmpty()) {
            return securityService.unlock(permission, siteService.siteReference(assignment.getContext()));
        } else {
            // check the permission in the groups
            for (String groupId : groupIds) {
                if (securityService.unlock(permission, groupId)) {
                    return true;
                }
            }
            // lastly if the user has permission asn.all.groups and has permission for the site
            if (allowAllGroups(assignment.getContext()) && securityService.unlock(permission, siteService.siteReference(assignment.getContext()))) {
                return true;
            }
        }
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // TODO
    // cleaning up the following entries in other tools should
    // probably happen as the result of posting an event in the
    // respective tools service and not be chained here
    // only if a rollback were needed would we want to include here
    // /////////////////////////////////////////////////////////////
    private void removeAssociatedTaggingItem(Assignment assignment) {
        try {
            if (taggingManager.isTaggable()) {
                for (TaggingProvider provider : taggingManager.getProviders()) {
                    provider.removeTags(assignmentActivityProducer.getActivity(assignment));
                }
            }
        } catch (PermissionException pe) {
            log.warn("removeAssociatedTaggingItem: User does not have permission to remove tags for assignment: " + assignment.getId() + " via transferCopyEntities");
        }
    }

    private void removeAssociatedGradebookItem(Assignment assignment, String context) {
        String associatedGradebookAssignment = assignment.getProperties().get(AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        if (StringUtils.isNotBlank(associatedGradebookAssignment)) {
            try {
                boolean isExternalAssignmentDefined = gradebookExternalAssessmentService.isExternalAssignmentDefined(context, associatedGradebookAssignment);
                if (isExternalAssignmentDefined) {
                    gradebookExternalAssessmentService.removeExternalAssessment(context, associatedGradebookAssignment);
                }
            } catch (GradebookNotFoundException gnfe) {
                // this may occur if no gradebook tool exists in the site
                log.debug("Attempted to remove associated gradebook item, {}", gnfe.getMessage());
            }
        }
    }

    private Calendar getCalendar(String context) {
        Calendar calendar = null;

        String calendarId = serverConfigurationService.getString("calendar", null);
        if (calendarId == null) {
            calendarId = calendarService.calendarReference(context, siteService.MAIN_CONTAINER);
            try {
                calendar = calendarService.getCalendar(calendarId);
            } catch (IdUnusedException e) {
                log.warn("No calendar found for site: " + context);
                calendar = null;
            } catch (PermissionException e) {
                log.error("The current user does not have permission to access the calendar for context: {}", context, e);
            } catch (Exception ex) {
                log.error("Unknown exception occurred retrieving calendar for site: {}", context, ex);
                calendar = null;
            }
        }

        return calendar;
    }

    private void removeAssociatedCalendarItem(Calendar calendar, Assignment assignment) {
        Map<String, String> properties = assignment.getProperties();
        String isThereEvent = properties.get(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
        if (isThereEvent != null && isThereEvent.equals(Boolean.TRUE.toString())) {
            // remove the associated calendar event
            if (calendar != null) {
                // already has calendar object
                // get the old event
                CalendarEvent event = null;
                String oldEventId = properties.get(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
                if (oldEventId != null) {
                    try {
                        event = calendar.getEvent(oldEventId);
                    } catch (Exception e) {
                        log.warn("Could not get the calendar event with id = {}", oldEventId, e);
                    }
                }

                // remove the event if it exists
                if (event != null) {
                    try {
                        calendar.removeEvent(calendar.getEditEvent(event.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
                        properties.remove(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
                        properties.remove(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
                    } catch (PermissionException ee) {
                        log.warn("Not allowed to remove calendar event for assignment = {}", assignment.getId());
                    } catch (InUseException ee) {
                        log.warn("Someone else is editing calendar event for assignment = {}", assignment.getId());
                    } catch (IdUnusedException ee) {
                        log.warn("Calendar event are in use for assignment = {} and event = {}", assignment.getId(), event.getId());
                    }
                }
            }
        }
    }


    private AnnouncementChannel getAnnouncementChannel(String contextId) {
        AnnouncementChannel channel = null;
        String channelId = serverConfigurationService.getString(announcementService.ANNOUNCEMENT_CHANNEL_PROPERTY, null);
        if (channelId == null) {
            channelId = announcementService.channelReference(contextId, siteService.MAIN_CONTAINER);
            try {
                channel = announcementService.getAnnouncementChannel(channelId);
            } catch (IdUnusedException e) {
                log.warn("No announcement channel found with id = {}", channelId);
                channel = null;
            } catch (PermissionException e) {
                log.warn("Current user not authorized to delete announcement with id = {}", channelId, e);
                channel = null;
            }
        }
        return channel;
    }

    private void removeAssociatedAnnouncementItem(AnnouncementChannel channel, Assignment assignment) {
        Map<String, String> properties = assignment.getProperties();
        if (channel != null) {
            String openDateAnnounced = StringUtils.trimToNull(properties.get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
            String openDateAnnouncementId = StringUtils.trimToNull(properties.get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
            if (openDateAnnounced != null && openDateAnnouncementId != null) {
                try {
                    channel.removeMessage(openDateAnnouncementId);
                } catch (PermissionException e) {
                    log.warn("User does not have permission", e);
                }
            }
        }
    }

    // TODO zipSubmissions and zipGroupSubmissions should be combined
    private void zipSubmissions(String assignmentReference, String assignmentTitle, Assignment.GradeType gradeType, Assignment.SubmissionType typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, boolean withoutFolders, String gradeFileFormat, boolean includeNotSubmitted, String siteId) {
        ZipOutputStream out = null;

        boolean isAdditionalNotesEnabled = false;
        Site st = null;
        try {
            st = siteService.getSite(siteId);
            isAdditionalNotesEnabled = candidateDetailProvider != null && candidateDetailProvider.isAdditionalNotesEnabled(st);
        } catch (IdUnusedException e) {
            log.warn("Could not find site {} - isAdditionalNotesEnabled set to false", siteId);
        }

        try {
            out = new ZipOutputStream(outputStream);

            // create the folder structure - named after the assignment's title
            final String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            final SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            final SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeType.toString(), getCsvSeparator());

            String submittedText = "";
            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            if (isAdditionalNotesEnabled) {
                sheet.addHeader(resourceLoader.getString("grades.id"), resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.lastname"),
                        resourceLoader.getString("grades.firstname"), resourceLoader.getString("grades.grade"),
                        resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"), resourceLoader.getString("gen.notes"));
            } else {
                sheet.addHeader(resourceLoader.getString("grades.id"), resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.lastname"),
                        resourceLoader.getString("grades.firstname"), resourceLoader.getString("grades.grade"),
                        resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"));
            }

            // allow add assignment members
            final List<User> allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String caughtException = null;
            String caughtStackTrace = null;
            final StringBuilder submittersAdditionalNotesHtml = new StringBuilder();

            while (submissions.hasNext()) {
            	final AssignmentSubmission s = (AssignmentSubmission) submissions.next();
                boolean isAnon = assignmentUsesAnonymousGrading(s.getAssignment());
                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    // get the submitter who submitted the submission see if the user is still in site
                    final Optional<AssignmentSubmissionSubmitter> assignmentSubmitter = s.getSubmitters().stream().findAny();
                    try {
                        User u = null;
                        if (assignmentSubmitter.isPresent()) {
                            u = userDirectoryService.getUser(assignmentSubmitter.get().getSubmitter());
                        }
                        if (allowAddSubmissionUsers.contains(u)) {
                        	String submittersName = root;

                            final User[] submitters = s.getSubmitters().stream().map(p -> {
                                try {
                                    return userDirectoryService.getUser(p.getSubmitter());
                                } catch (UserNotDefinedException e) {
                                    log.warn("User not found {}, {}", p.getSubmitter(), e.getMessage());
                                }
                                return null;
                            }).filter(Objects::nonNull).toArray(User[]::new);

                            String submittersString = "";
                            for (int i = 0; i < submitters.length; i++) {
                                if (i > 0) {
                                    submittersString = submittersString.concat("; ");
                                }
                                String fullName = submitters[i].getSortName();
                                // in case the user doesn't have first name or last name
                                if (!fullName.contains(",")) {
                                    fullName = fullName.concat(",");
                                }
                                submittersString = submittersString.concat(fullName);
                                // add the eid to the end of it to guarantee folder name uniqness
                                // if user Eid contains non ascii characters, the user internal id will be used
                                final String userEid = submitters[i].getEid();
                                final String candidateEid = escapeInvalidCharsEntry(userEid);
                                if (candidateEid.equals(userEid)) {
                                    submittersString = submittersString + "(" + candidateEid + ")";
                                } else {
                                    submittersString = submittersString + "(" + submitters[i].getId() + ")";
                                }
                                submittersString = escapeInvalidCharsEntry(submittersString);
                                // Work out if submission is late.
                                final String latenessStatus = whenSubmissionMade(s);
                                log.debug("latenessStatus: " + latenessStatus);
                                
                                final String anonTitle = resourceLoader.getString("grading.anonymous.title");
                                final String fullAnonId = s.getId() + " " + anonTitle;

                                String[] params = new String[7];
                                if (isAdditionalNotesEnabled && candidateDetailProvider != null) {
                                    final List<String> notes = candidateDetailProvider.getAdditionalNotes(submitters[i], st).orElse(new ArrayList<String>());

                                    if (!notes.isEmpty()) {
                                        params = new String[notes.size() + 7];
                                        System.arraycopy(notes.toArray(new String[notes.size()]), 0, params, 7, notes.size());
                                    }
                                }

                                // SAK-17606
                                if (!isAnon) {
                                	log.debug("Zip user: " + submitters[i].toString());
                                    params[0] = submitters[i].getDisplayId();
                                    params[1] = submitters[i].getEid();
                                    params[2] = submitters[i].getLastName();
                                    params[3] = submitters[i].getFirstName();
                                    params[4] = getGradeDisplay(s.getGrade(), s.getAssignment().getTypeOfGrade(), s.getAssignment().getScaleFactor());
                                    if (s.getDateSubmitted() != null) {
                                    	params[5] = s.getDateSubmitted().toString(); // TODO may need to be formatted
                                    } else {
                                    	params[5] = "";	
                                    }
                                    params[6] = latenessStatus;
                                } else {
                                    params[0] = fullAnonId;
                                    params[1] = fullAnonId;
                                    params[2] = anonTitle;
                                    params[3] = anonTitle;
                                    params[4] = getGradeDisplay(s.getGrade(), s.getAssignment().getTypeOfGrade(), s.getAssignment().getScaleFactor());
                                    if (s.getDateSubmitted() != null) {
                                    	params[5] = s.getDateSubmitted().toString(); // TODO may need to be formatted
                                    } else {
                                    	params[5] = "";	
                                    }
                                    params[6] = latenessStatus;
                                }
                                sheet.addRow(params);
                            }

                            if (StringUtils.trimToNull(submittersString) != null) {
                                submittersName = submittersName.concat(StringUtils.trimToNull(submittersString));
                                submittedText = s.getSubmittedText();

                                // SAK-17606
                                if (isAnon) {
                                    submittersString = s.getId() + " " + resourceLoader.getString("grading.anonymous.title");
                                    submittersName = root + submittersString;
                                }

                                if (!withoutFolders) {
                                    submittersName = submittersName.concat("/");
                                } else {
                                    submittersName = submittersName.concat("_");
                                }

                                // record submission timestamp
                                if (!withoutFolders && s.getSubmitted() && s.getDateSubmitted() != null) {
                                    final String zipEntryName = submittersName + "timestamp.txt";
                                    final String textEntryString = s.getDateSubmitted().toString();
                                    createTextZipEntry(out, zipEntryName, textEntryString);
                                }

                                // create the folder structure - named after the submitter's name
                                if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                    // include student submission text
                                    if (withStudentSubmissionText) {
                                        // create the text file only when a text submission is allowed
                                    	final StringBuilder submittersNameString = new StringBuilder(submittersName);
                                        //remove folder name if Download All is without user folders
                                        if (!withoutFolders) {
                                            submittersNameString.append(submittersString);
                                        }
                                    	
                                    	final String zipEntryName = submittersNameString.append("_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE).toString();
                                        createTextZipEntry(out, zipEntryName, submittedText);
                                    }

                                    // include student submission feedback text
                                    if (withFeedbackText) {
                                        // create a feedbackText file into zip
                                    	final String zipEntryName = submittersName + "feedbackText.html";
                                        final String textEntryString = s.getFeedbackText();
                                        createTextZipEntry(out, zipEntryName, textEntryString);
                                    }
                                }

                                if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION && withStudentSubmissionAttachment) {
                                    // include student submission attachment
                                    //remove "/" that creates a folder if Download All is without user folders
                                    String sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt");//jh + "/";
                                    if (!withoutFolders) {
                                    	// create a attachment folder for the submission attachments
                                        sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt") + "/";
                                        sSubAttachmentFolder = escapeInvalidCharsEntry(sSubAttachmentFolder);
                                        final ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                        out.putNextEntry(sSubAttachmentFolderEntry);
                                    } else {
                                    	sSubAttachmentFolder += "_";
                                        //submittersName = submittersName.concat("_");
                                    }

                                    // add all submission attachment into the submission attachment folder
                                    zipAttachments(out, submittersName, sSubAttachmentFolder, s.getAttachments());
                                    out.closeEntry();
                                }

                                if (withFeedbackComment) {
                                    // the comments.txt file to show instructor's comments
                                	final String zipEntryName = submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                                    final String textEntryString = formattedText.encodeUnicode(s.getFeedbackComment());
                                    createTextZipEntry(out, zipEntryName, textEntryString);
                                }

                                if (withFeedbackAttachment) {
                                    // create an attachment folder for the feedback attachments
                                    String feedbackSubAttachmentFolder = submittersName + resourceLoader.getString("download.feedback.attachment");
                                    if (!withoutFolders) {
                                        feedbackSubAttachmentFolder += "/";
                                        final ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
                                        out.putNextEntry(feedbackSubAttachmentFolderEntry);
                                    } else {
                                        submittersName = submittersName.concat("_");
                                    }

                                    // add all feedback attachment folder
                                    zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
                                    out.closeEntry();
                                }
                            } // if

                            if (isAdditionalNotesEnabled && candidateDetailProvider != null) {
                                final List<String> notes = candidateDetailProvider.getAdditionalNotes(u, st).orElse(new ArrayList<String>());
                                if (!notes.isEmpty()) {
                                    final StringBuilder noteList = new StringBuilder("<ul>");
                                    for (String note : notes) {
                                        noteList.append("<li>" + StringEscapeUtils.escapeHtml(note) + "</li>");
                                    }
                                    noteList.append("</ul>");
                                    submittersAdditionalNotesHtml.append("<tr><td style='padding-right:10px;padding-left:10px'>" + submittersString + "</td><td style='padding-right:10px'>" + noteList + "</td></tr>");
                                }
                            }
                        } else {
                            log.warn("Can't add submission: {} to zip, missing the submittee or they are no longer allowed to submit in the site", s.getId());
                        }
                    } catch (Exception e) {
                        caughtException = e.toString();
                        if (log.isDebugEnabled()) {
                            caughtStackTrace = ExceptionUtils.getStackTrace(e);
                        }
                        break;
                    }
                } // if the user is still in site

            } // while -- there is submission

            if (caughtException == null) {
                // continue
                if (withGradeFile) {
                    final ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
                    out.putNextEntry(gradesCSVEntry);
                    sheet.write(out);
                    out.closeEntry();
                }

                if (isAdditionalNotesEnabled) {
                	final ZipEntry additionalEntry = new ZipEntry(root + resourceLoader.getString("assignment.additional.notes.file.title") + ".html");
                    out.putNextEntry(additionalEntry);

                    String htmlString = emailUtil.htmlPreamble("additionalnotes");
                    htmlString += "<h1>" + resourceLoader.getString("assignment.additional.notes.export.title") + "</h1>";
                    htmlString += "<div>" + resourceLoader.getString("assignment.additional.notes.export.header") + "</div><br/>";
                    htmlString += "<table border=\"1\"  style=\"border-collapse:collapse;\"><tr><th>" + resourceLoader.getString("gen.student") + "</th><th>" + resourceLoader.getString("gen.notes") + "</th>" + submittersAdditionalNotesHtml + "</table>";
                    htmlString += "<br/><div>" + resourceLoader.getString("assignment.additional.notes.export.footer") + "</div>";
                    htmlString += emailUtil.htmlEnd();
                    log.debug("Additional information html: " + htmlString);

                    final byte[] wes = htmlString.getBytes();
                    out.write(wes);
                    additionalEntry.setSize(wes.length);
                    out.closeEntry();
                }
            } else {
                // log the error
                exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
                if (log.isDebugEnabled()) {
                    exceptionMessage.append(caughtStackTrace);
                }
            }
        } catch (IOException e) {
            exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
        } finally {
            // Complete the ZIP file
            if (out != null) {
                try {
                    out.finish();
                    out.flush();
                } catch (IOException e) {
                    // tried
                }
                try {
                    out.close();
                } catch (IOException e) {
                    // tried
                }
            }
        }
    }

    // TODO zipSubmissions and zipGroupSubmissions should be combined
    protected void zipGroupSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, Assignment.SubmissionType typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, String gradeFileFormat, boolean includeNotSubmitted) {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(outputStream);

            // create the folder structure - named after the assignment's title
            final String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            final SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            final SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeTypeString, getCsvSeparator());

            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            // Write the header
            sheet.addHeader("Group", resourceLoader.getString("grades.eid"), resourceLoader.getString("grades.members"),
                    resourceLoader.getString("grades.grade"), resourceLoader.getString("grades.submissionTime"), resourceLoader.getString("grades.late"));

            // allow add assignment members
            allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String caughtException = null;
            String caughtStackTrace = null;
            while (submissions.hasNext()) {
                final AssignmentSubmission s = (AssignmentSubmission) submissions.next();

                log.debug(this + " ZIPGROUP " + (s == null ? "null" : s.getId()));

                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    try {
                    	final StringBuilder submittersName = new StringBuilder(root);

                        final User[] submitters = s.getSubmitters().stream().map(p -> {
                            try {
                                return userDirectoryService.getUser(p.getSubmitter());
                            } catch (UserNotDefinedException e) {
                                log.warn("User not found {}", p.getSubmitter());
                                return null;
                            }
                        }).filter(Objects::nonNull).toArray(User[]::new);

                        final String submitterString = submitters[0].getDisplayName();// TODO gs.getGroup().getTitle() + " (" + gs.getGroup().getId() + ")";
                        final StringBuilder submittersString = new StringBuilder();
                        final StringBuilder submitters2String = new StringBuilder();

                        for (int i = 0; i < submitters.length; i++) {
                            if (i > 0) {
                                submittersString.append("; ");
                                submitters2String.append("; ");
                            }
                            String fullName = submitters[i].getSortName();
                            // in case the user doesn't have first name or last name
                            if (fullName.indexOf(",") == -1) {
                                fullName = fullName.concat(",");
                            }
                            submittersString.append(fullName);
                            submitters2String.append(submitters[i].getDisplayName());
                            // add the eid to the end of it to guarantee folder name uniqness
                            submittersString.append("(" + submitters[i].getEid() + ")");
                        }
                        final String latenessStatus = whenSubmissionMade(s);

                        final String gradeDisplay = getGradeDisplay(s.getGrade(), s.getAssignment().getTypeOfGrade(), s.getAssignment().getScaleFactor());
                        
                        //Adding the row

                        sheet.addRow(submitterString, submittersString.toString(), submitters2String.toString(), // TODO gs.getGroup().getTitle(), gs.getGroup().getId(), submitters2String,
                        		gradeDisplay, s.getDateSubmitted() != null ? s.getDateSubmitted().toString(): StringUtils.EMPTY, latenessStatus);


                        if (StringUtils.trimToNull(submitterString) != null) {
                            submittersName.append(StringUtils.trimToNull(submitterString));
                            final String submittedText = s.getSubmittedText();

                            submittersName.append("/");

                            // record submission timestamp
                            if (s.getSubmitted() && s.getDateSubmitted() != null) {
                            	createTextZipEntry(out, submittersName + "timestamp.txt", s.getDateSubmitted().toString());
                            }

                            // create the folder structure - named after the submitter's name
                            if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                // include student submission text
                                if (withStudentSubmissionText) {
                                    // create the text file only when a text submission is allowed
                                	final String zipEntryName = submittersName + submitterString + "_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE;
                                	createTextZipEntry(out, zipEntryName, submittedText);
                                }

                                // include student submission feedback text
                                if (withFeedbackText) {
                                    // create a feedbackText file into zip
                                	createTextZipEntry(out, submittersName + "feedbackText.html", s.getFeedbackText());
                                }
                            }

                            if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION && withStudentSubmissionAttachment) {
                                // include student submission attachment
                                // create a attachment folder for the submission attachments
                                final String sSubAttachmentFolder = submittersName + resourceLoader.getString("stuviewsubm.submissatt") + "/";
                                final ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                out.putNextEntry(sSubAttachmentFolderEntry);
                                // add all submission attachment into the submission attachment folder
                                zipAttachments(out, submittersName.toString(), sSubAttachmentFolder, s.getAttachments());
                                out.closeEntry();
                            }

                            if (withFeedbackComment) {
                                // the comments.txt file to show instructor's comments
                            	final String zipEntryName = submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                            	final String textEntryString = formattedText.encodeUnicode(s.getFeedbackComment());
                            	createTextZipEntry(out, zipEntryName, textEntryString);
                            }

                            if (withFeedbackAttachment) {
                                // create an attachment folder for the feedback attachments
                            	final String feedbackSubAttachmentFolder = submittersName + resourceLoader.getString("download.feedback.attachment") + "/";
                            	final ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
                                out.putNextEntry(feedbackSubAttachmentFolderEntry);
                                // add all feedback attachment folder
                                zipAttachments(out, submittersName.toString(), feedbackSubAttachmentFolder, s.getFeedbackAttachments());
                                out.closeEntry();
                            }

                            if (!submittersString.toString().trim().isEmpty()) {
                                // the comments.txt file to show instructor's comments
                            	final String zipEntryName = submittersName + "members" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE;
                            	final String textEntryString = formattedText.encodeUnicode(submittersString.toString());
                            	createTextZipEntry(out, zipEntryName, textEntryString);
                            }

                        } // if
                    } catch (Exception e) {
                        caughtException = e.toString();
                        if (log.isDebugEnabled()) {
                            caughtStackTrace = ExceptionUtils.getStackTrace(e);
                        }
                        break;
                    }
                } // if the user is still in site

            } // while -- there is submission

            if (caughtException == null) {
                // continue
                if (withGradeFile) {
                    final ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
                    out.putNextEntry(gradesCSVEntry);
                    sheet.write(out);
                    out.closeEntry();
                }
            } else {
                // log the error
                exceptionMessage.append(" Exception " + caughtException + " for creating submission zip file for assignment " + "\"" + assignmentTitle + "\"\n");
                if (log.isDebugEnabled()) {
                    exceptionMessage.append(caughtStackTrace);
                }
            }
        } catch (IOException e) {
            exceptionMessage.append("IOException for creating submission zip file for assignment " + "\"" + assignmentTitle + "\" exception: " + e + "\n");
        } finally {
            // Complete the ZIP file
            if (out != null) {
                try {
                    out.finish();
                    out.flush();
                } catch (IOException e) {
                    // tried
                }
                try {
                    out.close();
                } catch (IOException e) {
                    // tried
                }
            }
        }
    }

	private void createTextZipEntry(ZipOutputStream out, final String zipEntryName, final String textEntryString)
			throws IOException {
		final ZipEntry textEntry = new ZipEntry(zipEntryName);
		out.putNextEntry(textEntry);
		if(textEntryString != null) {
			final byte[] text = textEntryString.getBytes();
			out.write(text);
			textEntry.setSize(text.length);
		}
		out.closeEntry();
	}

    // TODO refactor this
    private String whenSubmissionMade(AssignmentSubmission s) {
        Instant dueTime = s.getAssignment().getDueDate();
        Instant submittedTime = s.getDateSubmitted();
        String latenessStatus;
        if (submittedTime == null) {
            latenessStatus = resourceLoader.getString("grades.lateness.unknown");
        } else if (dueTime != null && submittedTime.isAfter(dueTime)) {
            latenessStatus = resourceLoader.getString("grades.lateness.late");
        } else {
            latenessStatus = resourceLoader.getString("grades.lateness.ontime");
        }
        return latenessStatus;
    }

    // TODO refactor this
    private void zipAttachments(ZipOutputStream out, String submittersName, String sSubAttachmentFolder, Collection<String> attachments) {
        int attachedUrlCount = 0;
        InputStream content = null;
        Map<String, Integer> done = new HashMap<>();
        for (String r : attachments) {
            try {
                String attachId = removeReferencePrefix(r);
                ContentResource resource = contentHostingService.getResource(attachId);

                String contentType = resource.getContentType();

                ResourceProperties props = resource.getProperties();
                String displayName = props.getPropertyFormatted(props.getNamePropDisplayName());
                displayName = escapeInvalidCharsEntry(displayName);

                // for URL content type, encode a redirect to the body URL
                if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL)) {
                    displayName = "attached_URL_" + attachedUrlCount;
                    attachedUrlCount++;
                }

                // buffered stream input
                content = resource.streamContent();
                byte data[] = new byte[1024 * 10];
                BufferedInputStream bContent = null;
                try {
                    bContent = new BufferedInputStream(content, data.length);

                    String candidateName = sSubAttachmentFolder + displayName;
                    String realName = null;
                    Integer already = done.get(candidateName);
                    if (already == null) {
                        realName = candidateName;
                        done.put(candidateName, 1);
                    } else {
                        String fileName = FilenameUtils.removeExtension(candidateName);
                        String fileExt = FilenameUtils.getExtension(candidateName);
                        if (!"".equals(fileExt.trim())) {
                            fileExt = "." + fileExt;
                        }
                        realName = fileName + "+" + already + fileExt;
                        done.put(candidateName, already + 1);
                    }

                    ZipEntry attachmentEntry = new ZipEntry(realName);
                    out.putNextEntry(attachmentEntry);
                    int bCount = -1;
                    while ((bCount = bContent.read(data, 0, data.length)) != -1) {
                        out.write(data, 0, bCount);
                    }

                    try {
                        out.closeEntry(); // The zip entry need to be closed
                    } catch (IOException ioException) {
                        log.warn(":zipAttachments: problem closing zip entry " + ioException);
                    }
                } catch (IllegalArgumentException iException) {
                    log.warn(":zipAttachments: problem creating BufferedInputStream with content and length " + data.length + iException);
                } finally {
                    if (bContent != null) {
                        try {
                            bContent.close(); // The BufferedInputStream needs to be closed
                        } catch (IOException ioException) {
                            log.warn(":zipAttachments: problem closing FileChannel " + ioException);
                        }
                    }
                }
            } catch (PermissionException e) {
                log.warn(" zipAttachments--PermissionException submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (IdUnusedException e) {
                log.warn(" zipAttachments--IdUnusedException submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (TypeException e) {
                log.warn(" zipAttachments--TypeException: submittersName="
                        + submittersName + " attachment reference=" + r);
            } catch (IOException e) {
                log.warn(" zipAttachments--IOException: Problem in creating the attachment file: submittersName="
                        + submittersName + " attachment reference=" + r + " error " + e);
            } catch (ServerOverloadException e) {
                log.warn(" zipAttachments--ServerOverloadException: submittersName="
                        + submittersName + " attachment reference=" + r);
            } finally {
                if (content != null) {
                    try {
                        content.close(); // The input stream needs to be closed
                    } catch (IOException ioException) {
                        log.warn(":zipAttachments: problem closing Inputstream content " + ioException);
                    }
                }
            }
        } // for
    }

    @Transactional
    public void resetAssignment(Assignment assignment) {
        assignmentRepository.resetAssignment(assignment);
    }

    @Override
    @Transactional
    public void postReviewableSubmissionAttachments(AssignmentSubmission submission) {
        try {
            Optional<AssignmentSubmissionSubmitter> submitter = submission.getSubmitters().stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst();
            if (submitter.isPresent()) {
                Assignment assignment = submission.getAssignment();
                String assignmentRef = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                List<ContentResource> resources = new ArrayList<>();
                for (String attachment : submission.getAttachments()) {
                    Reference attachmentRef = entityManager.newReference(attachment);
                    try {
                        ContentResource resource = contentHostingService.getResource(attachmentRef.getId());
                        if (contentReviewService.isAcceptableContent(resource)) {
                            resources.add(resource);
                        }
                    } catch (TypeException e) {
                        log.warn("Could not retrieve content resource: {}, {}", assignmentRef, e.getMessage());
                    }
                }
                try {
                    contentReviewService.queueContent(submitter.get().getSubmitter(), assignment.getContext(), assignmentRef, resources);
                } catch (QueueException e) {
                    log.warn("Could not queue submission: {} for review, {}", submission.getId(), e.getMessage());
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not locate submission: {}, {}", submission.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void transferCopyEntities(String fromContext, String toContext, List<String> ids) {
        transferCopyEntitiesRefMigrator(fromContext, toContext, ids);
    }

    @Override
    public String[] myToolIds() {
        return new String[] { "sakai.assignment", "sakai.assignment.grades" };
    }

    @Override
    @Transactional
    public void transferCopyEntities(String fromContext, String toContext, List<String> ids, boolean cleanup) {
        transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
    }

    @Override
    @Transactional
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {
        if (transversalMap != null && !transversalMap.isEmpty()) {
            Collection<Assignment> assignments = getAssignmentsForContext(toContext);
            for (Assignment assignment : assignments) {
                try {
                    String msgBody = assignment.getInstructions();
                    StringBuffer msgBodyPreMigrate = new StringBuffer(msgBody);
                    msgBody = linkMigrationHelper.migrateAllLinks(transversalMap.entrySet(), msgBody);
//                    SecurityAdvisor securityAdvisor = new MySecurityAdvisor(sessionManager.getCurrentSessionUserId(),
//                            new ArrayList<String>(Arrays.asList(SECURE_UPDATE_ASSIGNMENT_CONTENT)),
//                            AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
                    try {
                        if (!msgBody.equals(msgBodyPreMigrate.toString())) {
                            // add permission to update assignment content
//                            securityService.pushAdvisor(securityAdvisor);
                            assignment.setInstructions(msgBody);
                            updateAssignment(assignment);
                        }
                    } catch (Exception e) {
                        // exception
                        log.warn("UpdateEntityReference: cannot get assignment content for {}, {}", assignment.getId(), e.getMessage());
                    } finally {
                        // remove advisor
//                        securityService.popAdvisor(securityAdvisor);
                    }
                } catch (Exception ee) {
                    log.warn("UpdateEntityReference: remove Assignment and all references for {}, {}", assignment.getId(), ee.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids) {
        Map<String, String> transversalMap = new HashMap<>();
        Collection<Assignment> assignments = getAssignmentsForContext(fromContext);

        for (Assignment oAssignment : assignments) {
            String oAssignmentId = oAssignment.getId();
            String nAssignmentId = null;

            if (ids == null || ids.isEmpty() || ids.contains(oAssignmentId)) {
                try {
                    Assignment nAssignment = addAssignment(toContext);
                    nAssignmentId = nAssignment.getId();

                    nAssignment.setTitle(oAssignment.getTitle());
                    // replace all occurrence of old context with new context inside instruction text
                    if(StringUtils.isNotBlank(oAssignment.getInstructions())){
                    	nAssignment.setInstructions(oAssignment.getInstructions().replaceAll(fromContext, toContext));
                    }
                    nAssignment.setTypeOfGrade(oAssignment.getTypeOfGrade());
                    nAssignment.setTypeOfSubmission(oAssignment.getTypeOfSubmission());
                    // when importing, refer to property to determine draft status
                    if (serverConfigurationService.getBoolean("import.importAsDraft", true)) {
                        nAssignment.setDraft(true);
                    } else {
                        nAssignment.setDraft(oAssignment.getDraft());
                    }

                    nAssignment.setCloseDate(oAssignment.getCloseDate());
                    nAssignment.setDropDeadDate(oAssignment.getDropDeadDate());
                    nAssignment.setDueDate(oAssignment.getDueDate());
                    nAssignment.setOpenDate(oAssignment.getOpenDate());
                    nAssignment.setHideDueDate(oAssignment.getHideDueDate());

                    nAssignment.setPosition(oAssignment.getPosition());
                    nAssignment.setAllowAttachments(oAssignment.getAllowAttachments());
                    nAssignment.setHonorPledge(oAssignment.getHonorPledge());
                    nAssignment.setIndividuallyGraded(oAssignment.getIndividuallyGraded());
                    nAssignment.setMaxGradePoint(oAssignment.getMaxGradePoint());
                    nAssignment.setScaleFactor(oAssignment.getScaleFactor());
                    nAssignment.setReleaseGrades(oAssignment.getReleaseGrades());

                    // group assignment
                    if (oAssignment.getTypeOfAccess() == Assignment.Access.GROUP) {
                        nAssignment.setTypeOfAccess(Assignment.Access.GROUP);
                        Site oSite = siteService.getSite(oAssignment.getContext());
                        Site nSite = siteService.getSite(nAssignment.getContext());

                        boolean siteChanged = false;
                        Collection<Group> nGroups = nSite.getGroups();
                        for (String groupId : oAssignment.getGroups()) {
                            Group oGroup = oSite.getGroup(groupId);
                            Optional<Group> existingGroup = nGroups.stream().filter(g -> StringUtils.equals(g.getTitle(), oGroup.getTitle())).findAny();
                            Group nGroup;
                            if (existingGroup.isPresent()) {
                                // found a matching group
                                nGroup = existingGroup.get();
                            } else {
                                // create group
                                nGroup = nSite.addGroup();
                                nGroup.setTitle(oGroup.getTitle());
                                nGroup.setDescription(oGroup.getDescription());
                                nGroup.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());
                                siteChanged = true;
                            }
                            nAssignment.getGroups().add(nGroup.getReference());
                        }
                        if (siteChanged) siteService.save(nSite);
                        nAssignment.setIsGroup(oAssignment.getIsGroup());
                    }

                    // review service
                    nAssignment.setContentReview(oAssignment.getContentReview());

                    // attachments
                    Set<String> oAttachments = oAssignment.getAttachments();
                    List<Reference> nAttachments = entityManager.newReferenceList();
                    for (String oAttachment : oAttachments) {
                        Reference oReference = entityManager.newReference(oAttachment);
                        String oAttachmentId = oReference.getId();
                        // transfer attachment, replace the context string if necessary and add new attachment
                        String nReference = transferAttachment(fromContext, toContext, oAttachmentId);
                        nAssignment.getAttachments().add(nReference);
                    }

                    // peer review
                    nAssignment.setAllowPeerAssessment(oAssignment.getAllowPeerAssessment());
                    nAssignment.setPeerAssessmentAnonEval(oAssignment.getPeerAssessmentAnonEval());
                    nAssignment.setPeerAssessmentInstructions(oAssignment.getPeerAssessmentInstructions());
                    nAssignment.setPeerAssessmentNumberReviews(oAssignment.getPeerAssessmentNumberReviews());
                    nAssignment.setPeerAssessmentStudentReview(oAssignment.getPeerAssessmentStudentReview());
                    nAssignment.setPeerAssessmentPeriodDate(oAssignment.getPeerAssessmentPeriodDate());
                    if (nAssignment.getPeerAssessmentPeriodDate() == null && nAssignment.getCloseDate() != null) {
                        // set the peer period time to be 10 mins after accept until date
                        Instant tenMinutesAfterCloseDate = Instant.from(nAssignment.getCloseDate().plus(Duration.ofMinutes(10)));
                        nAssignment.setPeerAssessmentPeriodDate(tenMinutesAfterCloseDate);
                    }

                    // properties
                    Map<String, String> nProperties = nAssignment.getProperties();
                    nProperties.putAll(oAssignment.getProperties());
                    // remove the link btw assignment and announcement item. One can announce the open date afterwards
                    nProperties.remove(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
                    nProperties.remove(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED);
                    nProperties.remove(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID);

                    // remove the link btw assignment and calendar item. One can add the due date to calendar afterwards
                    nProperties.remove(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
                    nProperties.remove(AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
                    nProperties.remove(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);

                    // gradebook-integration link
                    String associatedGradebookAssignment = nProperties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                    if (StringUtils.isNotBlank(associatedGradebookAssignment)) {
                        // see if the old assignment's associated gradebook item is an internal gradebook entry or externally defined
                        boolean isExternalAssignmentDefined = gradebookExternalAssessmentService.isExternalAssignmentDefined(oAssignment.getContext(), associatedGradebookAssignment);
                        if (isExternalAssignmentDefined) {
                            // if this is an external defined (came from assignment)
                            // mark the link as "add to gradebook" for the new imported assignment, since the assignment is still of draft state
                            // later when user posts the assignment, the corresponding assignment will be created in gradebook.
                            nProperties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                            nProperties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
                        }
                    }

                    updateAssignment(nAssignment);

                    transversalMap.put("assignment/" + oAssignmentId, "assignment/" + nAssignmentId);
                    log.info("Old assignment id: {} - new assignment id: {}", oAssignmentId, nAssignmentId);

                    try {
                        if (taggingManager.isTaggable()) {
                            for (TaggingProvider provider : taggingManager.getProviders()) {
                                provider.transferCopyTags(assignmentActivityProducer.getActivity(oAssignment), assignmentActivityProducer.getActivity(nAssignment));
                            }
                        }
                    } catch (PermissionException pe) {
                        log.error("{} oAssignmentId={} nAssignmentId={}", pe.toString(), oAssignmentId, nAssignmentId);
                    }

                    // Import supplementary items if they are present in the assignment to be imported
                    // Model Answer
                    AssignmentModelAnswerItem oModelAnswerItem = assignmentSupplementItemService.getModelAnswer(oAssignmentId);
                    if (oModelAnswerItem != null) {
                        AssignmentModelAnswerItem nModelAnswerItem = assignmentSupplementItemService.newModelAnswer();
                        assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
                        nModelAnswerItem.setAssignmentId(nAssignmentId);
                        nModelAnswerItem.setText(oModelAnswerItem.getText());
                        nModelAnswerItem.setShowTo(oModelAnswerItem.getShowTo());
                        Set<AssignmentSupplementItemAttachment> oModelAnswerItemAttachments = oModelAnswerItem.getAttachmentSet();
                        Set<AssignmentSupplementItemAttachment> nModelAnswerItemAttachments = new HashSet<>();
                        for (AssignmentSupplementItemAttachment oAttachment : oModelAnswerItemAttachments) {
                            AssignmentSupplementItemAttachment nAttachment = assignmentSupplementItemService.newAttachment();
                            // New attachment creation
                            String nAttachmentId = transferAttachment(fromContext, toContext, removeReferencePrefix(oAttachment.getAttachmentId()));
                            if (StringUtils.isNotEmpty(nAttachmentId)) {
                                nAttachment.setAssignmentSupplementItemWithAttachment(nModelAnswerItem);
                                nAttachment.setAttachmentId(nAttachmentId);
                                assignmentSupplementItemService.saveAttachment(nAttachment);
                                nModelAnswerItemAttachments.add(nAttachment);
                            }
                        }
                        nModelAnswerItem.setAttachmentSet(nModelAnswerItemAttachments);
                        assignmentSupplementItemService.saveModelAnswer(nModelAnswerItem);
                    }

                    // Private Note
                    AssignmentNoteItem oNoteItem = assignmentSupplementItemService.getNoteItem(oAssignmentId);
                    if (oNoteItem != null) {
                        AssignmentNoteItem nNoteItem = assignmentSupplementItemService.newNoteItem();
                        //assignmentSupplementItemService.saveNoteItem(nNoteItem);
                        nNoteItem.setAssignmentId(nAssignment.getId());
                        nNoteItem.setNote(oNoteItem.getNote());
                        nNoteItem.setShareWith(oNoteItem.getShareWith());
                        nNoteItem.setCreatorId(userDirectoryService.getCurrentUser().getId());
                        assignmentSupplementItemService.saveNoteItem(nNoteItem);
                    }

                    // All Purpose
                    AssignmentAllPurposeItem oAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(oAssignmentId);
                    if (oAllPurposeItem != null) {
                        AssignmentAllPurposeItem nAllPurposeItem = assignmentSupplementItemService.newAllPurposeItem();
                        assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                        nAllPurposeItem.setAssignmentId(nAssignment.getId());
                        nAllPurposeItem.setTitle(oAllPurposeItem.getTitle());
                        nAllPurposeItem.setText(oAllPurposeItem.getText());
                        nAllPurposeItem.setHide(oAllPurposeItem.getHide());
                        nAllPurposeItem.setReleaseDate(null);
                        nAllPurposeItem.setRetractDate(null);
                        Set<AssignmentSupplementItemAttachment> oAllPurposeItemAttachments = oAllPurposeItem.getAttachmentSet();
                        Set<AssignmentSupplementItemAttachment> nAllPurposeItemAttachments = new HashSet<>();
                        for (AssignmentSupplementItemAttachment oAttachment : oAllPurposeItemAttachments) {
                            AssignmentSupplementItemAttachment nAttachment = assignmentSupplementItemService.newAttachment();
                            // New attachment creation
                            String nAttachId = transferAttachment(fromContext, toContext, removeReferencePrefix(oAttachment.getAttachmentId()));
                            if (StringUtils.isNotEmpty(nAttachId)) {
                                nAttachment.setAssignmentSupplementItemWithAttachment(nAllPurposeItem);
                                nAttachment.setAttachmentId(nAttachId);
                                assignmentSupplementItemService.saveAttachment(nAttachment);
                                nAllPurposeItemAttachments.add(nAttachment);
                            }
                        }
                        nAllPurposeItem.setAttachmentSet(nAllPurposeItemAttachments);
                        assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurposeItem);
                        Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<>();
                        AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
                        access.setAccess(userDirectoryService.getCurrentUser().getId());
                        access.setAssignmentAllPurposeItem(nAllPurposeItem);
                        assignmentSupplementItemService.saveAllPurposeItemAccess(access);
                        accessSet.add(access);
                        nAllPurposeItem.setAccessSet(accessSet);
                        assignmentSupplementItemService.saveAllPurposeItem(nAllPurposeItem);
                    }
                } catch (Exception e) {
                    log.error("{} oAssignmentId={} nAssignmentId={}", e.toString(), oAssignmentId, nAssignmentId);
                }
            }
        }
        return transversalMap;
    }

    @Override
    @Transactional
    public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup) {
        Map<String, String> transversalMap = new HashMap<>();

        try {
            if (cleanup) {
                Collection<Assignment> assignments = getAssignmentsForContext(toContext);
                for (Assignment assignment : assignments) {
                    String assignmentId = assignment.getId();

//                    SecurityAdvisor securityAdvisor = new MySecurityAdvisor(sessionManager.getCurrentSessionUserId(),
//                            new ArrayList<>(Arrays.asList(SECURE_UPDATE_ASSIGNMENT, SECURE_REMOVE_ASSIGNMENT)),
//                            assignmentId);
                    try {
                        // advisor to allow edit and remove assignment
//                        securityService.pushAdvisor(securityAdvisor);

                        // remove this assignment with all its associated items
                        deleteAssignmentAndAllReferences(assignment);
                    } catch (Exception e) {
                        log.warn("Remove assignment and all references for {}, {}", assignmentId, e.getMessage());
                    } finally {
                        // remove SecurityAdvisor
//                        securityService.popAdvisor(securityAdvisor);
                    }
                }
            }
            transversalMap.putAll(transferCopyEntitiesRefMigrator(fromContext, toContext, ids));
        } catch (Exception e) {
            log.info("End removing Assignmentt data {}", e.getMessage());
        }

        return transversalMap;
    }

    private String transferAttachment(String fromContext, String toContext, String oAttachmentId) {
        String reference = "";
        String nAttachmentId = oAttachmentId.replaceAll(fromContext, toContext);
        try {
            ContentResource attachment = contentHostingService.getResource(nAttachmentId);
            reference = attachment.getReference();
        } catch (IdUnusedException iue) {
            try {
                ContentResource oAttachment = contentHostingService.getResource(oAttachmentId);
                try (InputStream content = new ByteArrayInputStream(oAttachment.getContent())) {
                    if (contentHostingService.isAttachmentResource(nAttachmentId)) {
                        // add the new resource into attachment collection area
                        ContentResource attachment = contentHostingService.addAttachmentResource(
                                Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)),
                                toContext,
                                toolManager.getTool("sakai.assignment.grades").getTitle(),
                                oAttachment.getContentType(),
                                content,
                                oAttachment.getProperties());
                        reference = attachment.getReference();
                    } else {
                        // add the new resource into resource area
                        ContentResource attachment = contentHostingService.addResource(
                                Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)),
                                toContext,
                                1,
                                oAttachment.getContentType(),
                                content,
                                oAttachment.getProperties(),
                                Collections.emptyList(),
                                false,
                                null,
                                null,
                                NotificationService.NOTI_NONE);
                        reference = attachment.getReference();
                    }
                } catch (Exception e) {
                    // if the new resource cannot be added
                    log.warn("Cannot add new attachment with id = {}, {}", nAttachmentId, e.getMessage());
                }
            } catch (Exception e) {
                // if cannot find the original attachment, do nothing.
                log.warn("Cannot get the original attachment with id = {}, {}", oAttachmentId, e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Could not get the new attachment with id = {}, {}", nAttachmentId, e.getMessage());
        }
        return reference;
    }

    private LRS_Statement getStatementForAssignmentGraded(String reference, Assignment a, AssignmentSubmission s, User studentUser) {
    	LRS_Actor instructor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + reference, "received-grade-assignment");
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        Map<String, String> descMap = new HashMap<>();
        String resubmissionNumber = StringUtils.defaultString(s.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER), "0");
        descMap.put("en-US", "User received a grade for their assginment: " + a.getTitle() + "; Submission #: " + resubmissionNumber);
        lrsObject.setDescription(descMap);
        LRS_Actor student = learningResourceStoreService.getActor(studentUser.getId());
        student.setName(studentUser.getDisplayName());
        return new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, true), null);
    }

    private LRS_Result getLRS_Result(Assignment a, AssignmentSubmission s, boolean completed) {
        LRS_Result result = null;
        String decSeparator = formattedText.getDecimalSeparator();
        // gradeDisplay ready to conversion to Float
        String gradeDisplay = StringUtils.replace(getGradeDisplay(s.getGrade(), a.getTypeOfGrade(), a.getScaleFactor()), decSeparator, ".");
        if (Assignment.GradeType.SCORE_GRADE_TYPE == a.getTypeOfGrade() && NumberUtils.isCreatable(gradeDisplay)) { // Points
            String maxGradePointDisplay = StringUtils.replace(getMaxPointGradeDisplay(a.getScaleFactor(), a.getMaxGradePoint()), decSeparator, ".");
            result = new LRS_Result(new Float(gradeDisplay), 0.0f, new Float(maxGradePointDisplay), null);
            result.setCompletion(completed);
        } else {
            result = new LRS_Result(completed);
            result.setGrade(getGradeDisplay(s.getGrade(), a.getTypeOfGrade(), a.getScaleFactor()));
        }
        return result;
    }

    private LRS_Statement getStatementForUnsubmittedAssignmentGraded(String reference, Assignment a, AssignmentSubmission s, User studentUser) {
    	LRS_Actor instructor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getAccessUrl() + reference, "received-grade-unsubmitted-assignment");
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        Map<String, String> descMap = new HashMap<>();
        descMap.put("en-US", "User received a grade for an unsubmitted assginment: " + a.getTitle());
        lrsObject.setDescription(descMap);
        LRS_Actor student = learningResourceStoreService.getActor(studentUser.getId());
        student.setName(studentUser.getDisplayName());
        return new LRS_Statement(student, verb, lrsObject, getLRS_Result(a, s, false), null);
    }

    private LRS_Statement getStatementForSubmitAssignment(String reference, String accessUrl, String assignmentName) {
    	LRS_Actor actor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.attempted);
        LRS_Object lrsObject = new LRS_Object(accessUrl + reference, "submit-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User submitted an assignment");
        lrsObject.setActivityName(nameMap);
        // Add description
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User submitted an assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    private void sendGradeReleaseNotification(AssignmentSubmission submission) {
        Set<User> filteredUsers;
        Assignment assignment = submission.getAssignment();
        Map<String, String> assignmentProperties = assignment.getProperties();
        String siteId = assignment.getContext();
        String resubmitNumber = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

        boolean released = BooleanUtils.toBoolean(submission.getGradeReleased());
        Set<String> submitterIds = submission.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toSet());
        try {
            Set<String> siteUsers = siteService.getSite(siteId).getUsers();
            filteredUsers = submitterIds.stream().filter(siteUsers::contains).map(id -> {
                try {
                    return userDirectoryService.getUser(id);
                } catch (UserNotDefinedException e) {
                    log.warn("Could not find user with id = {}, {}", id, e.getMessage());
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (IdUnusedException e) {
            log.warn("Site ({}) not found.", siteId);
            return;
        }

        if (released && StringUtils.equals(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH, assignmentProperties.get(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE))) {
            // send email to every submitters
            if (!filteredUsers.isEmpty()) {
                // send the message immidiately
                emailService.sendToUsers(filteredUsers, emailUtil.getHeaders(null, "releasegrade"), emailUtil.getNotificationMessage(submission, "releasegrade"));
            }
        }
        if (StringUtils.isNotBlank(resubmitNumber) && StringUtils.equals(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH, assignmentProperties.get(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE))) {
            // send email to every submitters
            if (!filteredUsers.isEmpty()) {
                // send the message immidiately
                emailService.sendToUsers(filteredUsers, emailUtil.getHeaders(null, "releaseresumbission"), emailUtil.getNotificationMessage(submission, "releaseresumbission"));
            }
        }
    }

    private void notificationToInstructors(AssignmentSubmission submission, Assignment assignment) {
        String notiOption = assignment.getProperties().get(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE);
        if (notiOption != null && !notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE)) {
            // need to send notification email
            String context = assignment.getContext();
            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

            // compare the list of users with the receive.notifications and list of users who can actually grade this assignment
            List<User> receivers = allowReceiveSubmissionNotificationUsers(context);
            List allowGradeAssignmentUsers = allowGradeAssignmentUsers(assignmentReference);
            receivers.retainAll(allowGradeAssignmentUsers);

            String messageBody = emailUtil.getNotificationMessage(submission, "submission");

            if (notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH)) {
                // send the message immediately
                emailService.sendToUsers(receivers, emailUtil.getHeaders(null, "submission"), messageBody);
            } else if (notiOption.equals(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST)) {
                // just send plain/text version for now
                String digestMsgBody = emailUtil.getPlainTextNotificationMessage(submission, "submission");

                // digest the message to each user
                for (User user : receivers) {
                    digestService.digest(user.getId(), emailUtil.getSubject("submission"), digestMsgBody);
                }
            }
        }
    }

    private void notificationToStudent(AssignmentSubmission submission) {
        if (serverConfigurationService.getBoolean("assignment.submission.confirmation.email", true)) {
            Set<String> submitterIds = submission.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toSet());
            Set<User> users = submitterIds.stream().map(id -> {
                try {
                    return userDirectoryService.getUser(id);
                } catch (UserNotDefinedException e) {
                    log.warn("Could not find user with id = {}, {}", id, e.getMessage());
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            emailService.sendToUsers(users, emailUtil.getHeaders(null, "submission"), emailUtil.getNotificationMessage(submission, "submission"));
        }
    }

    @Override
    public String getUsersLocalDateTimeString(Instant date) {
        ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                                                .withZone(zone)
                                                .withLocale(resourceLoader.getLocale());
        return df.format(date);
    }

    private String removeReferencePrefix(String referenceId) {
        if (referenceId.startsWith(REF_PREFIX)) {
            referenceId = referenceId.replaceFirst(REF_PREFIX, "");
        }
        return referenceId;
    }

    @Override
    @Transactional
    public List<ContentReviewResult> getContentReviewResults(AssignmentSubmission s){
        ArrayList<ContentReviewResult> reviewResults = new ArrayList<ContentReviewResult>();
        //get all the attachments for this submission and populate the reviewResults
        List<ContentResource> contentResources = getAllAcceptableAttachments(s);
        for(ContentResource cr : contentResources){
            ContentReviewResult reviewResult = new ContentReviewResult();
            reviewResult.setContentResource(cr);
            ContentReviewItem cri = contentReviewService.getContentReviewItemByContentId(cr.getId());
            if(cri == null){
                log.warn("Retrieved null ContentReviewItem for content " + cr.getId());
                continue;
            }
            reviewResult.setContentReviewItem(cri);

            AssignmentReferenceReckoner.AssignmentReference referenceReckoner = AssignmentReferenceReckoner.reckoner().assignment(s.getAssignment()).reckon();
            reviewResult.setReviewReport(getReviewReport(cr, referenceReckoner.getReference()));
            String iconUrl = getReviewIconCssClass(reviewResult);
            reviewResult.setReviewIconCssClass(iconUrl);
            reviewResult.setReviewError(getReviewError(reviewResult.getStatus()));

            if ("true".equals(reviewResult.isInline())) {
                reviewResults.add(0, reviewResult);
            } else {
                reviewResults.add(reviewResult);
            }
        }
        return reviewResults;
    }

    /**
     * Gets all attachments in the submission that are acceptable to the content review service
     */
    private List<ContentResource> getAllAcceptableAttachments(AssignmentSubmission s) {
        List<ContentResource> attachments = new ArrayList<>();
        for (String attachment : s.getAttachments()) {
            Reference attachmentRef = entityManager.newReference(attachment);
            try {
                ContentResource resource = contentHostingService.getResource(attachmentRef.getId());
                if (contentReviewService.isAcceptableContent(resource)) {
                    attachments.add(resource);
                }
            } catch (Exception e) {
                log.warn(":getAllAcceptableAttachments() {} ", e.getMessage());
            }
        }
        return attachments;
    }

    private String getReviewIconCssClass(ContentReviewResult reviewResult) {
        if (reviewResult == null) {
            log.debug("{} getReviewIconCssClass(ContentResource, int) called with reviewResult == null", reviewResult.getContentResource().getId());
            return null;
        }

        Long status = reviewResult.getStatus();
        String reviewReport = reviewResult.getReviewReport();
        String iconCssClass = null;

        if (!"Error".equals(reviewReport)) {
            iconCssClass = contentReviewService.getIconCssClassforScore(reviewResult.getReviewScore(), reviewResult.getContentResource().getId());
        } else if (ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE.equals(status) || ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE.equals(status)) {
            iconCssClass = "contentReviewIconPending";
        } else {
            iconCssClass = "contentReviewIconWarn";
        }

        return iconCssClass;
    }

    private String getReviewReport(ContentResource cr, String assignmentReference) {
        if (cr == null) {
            log.debug("getReviewReport(ContentResource) called with cr == null"/*, this.getId()*/);
            return "Error";
        }
        try {
            String contentId = cr.getId();
            if (allowGradeSubmission(assignmentReference)) {
                return contentReviewService.getReviewReportInstructor(contentId, assignmentReference, userDirectoryService.getCurrentUser().getId());
            } else {
                return contentReviewService.getReviewReportStudent(contentId, assignmentReference, userDirectoryService.getCurrentUser().getId());
            }
        } catch (Exception e) {
            log.warn(":getReviewReport(ContentResource) {}", e.getMessage());
            return "Error";
        }
    }

    private String getReviewError(Long status){
        if (status == null){
            log.debug("getReviewReport(ContentResource) called with status == null");
            return null;
        }
        //This should use getLocalizedReviewErrorMesage(contentId) to get a i18n message of the error
        String errorMessage = null;
        if (status.equals(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_NO_RETRY_CODE)){
            errorMessage = resourceLoader.getString("content_review.error.REPORT_ERROR_NO_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.REPORT_ERROR_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_NO_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_RETRY_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_RETRY_EXCEEDED_CODE");
        } else if (status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE)) {
            errorMessage = resourceLoader.getString("content_review.error.SUBMISSION_ERROR_USER_DETAILS_CODE");
        } else if (ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE.equals(status) || ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE.equals(status)) {
            errorMessage = resourceLoader.getString("content_review.pending.info");
        }

        if (errorMessage == null) {
            errorMessage = resourceLoader.getString("content_review.error");
        }

        return errorMessage;
    }
}
