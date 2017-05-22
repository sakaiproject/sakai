package org.sakaiproject.assignment.impl;

import static org.sakaiproject.assignment.api.AssignmentServiceConstants.APPLICATION_ID;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REFERENCE_ROOT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REF_TYPE_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REF_TYPE_CONTENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REF_TYPE_GRADES;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REF_TYPE_SUBMISSION;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REF_TYPE_SUBMISSIONS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_CONTENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ALL_GROUPS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_REMOVE_ASSIGNMENT_SUBMISSION;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_SHARE_DRAFTS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT_SUBMISSION;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentEntity;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.impl.sort.AnonymousSubmissionComparator;
import org.sakaiproject.assignment.impl.sort.AssignmentSubmissionComparator;
import org.sakaiproject.assignment.persistence.AssignmentRepository;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Validator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private static ResourceLoader rb = new ResourceLoader("assignment");

    @Setter private AnnouncementService announcementService;
    @Setter private AssignmentActivityProducer assignmentActivityProducer;
    @Setter private ObjectFactory<AssignmentEntity> assignmentEntityFactory;
    @Setter private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private CalendarService calendarService;
    @Setter private CandidateDetailProvider candidateDetailProvider;
    @Setter private ContentHostingService contentHostingService;
    @Setter private DeveloperHelperService developerHelperService;
    @Setter private DigestService digestService;
    @Setter private EmailService emailService;
    @Setter private EntityManager entityManager;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private FunctionManager functionManager;
    @Setter private GradebookExternalAssessmentService gradebookExternalAssessmentService;
    @Setter private GradebookService gradebookService;
    @Setter private GradeSheetExporter gradeSheetExporter;
    @Setter private IdManager idManager;
    @Setter private MemoryService memoryService;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private TaggingManager taggingManager;
    @Setter private TimeService timeService;
    @Setter private ToolManager toolManager;
    @Setter private UserDirectoryService userDirectoryService;

    private DateTimeFormatter dateTimeFormatter;

    public void init() {
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    }

    @Override
    public String getLabel() {
        return "assignment";
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        return null;
    }

    @Override
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans, Set<String> userListAllowImport) {
        return null;
    }

    @Override
    public boolean parseEntityReference(String stringReference, Reference reference) {
        if (StringUtils.startsWith(stringReference, REFERENCE_ROOT)) {
            AssignmentReferenceReckoner.AssignmentReference reckoner = AssignmentReferenceReckoner.reckoner().reference(stringReference).reckon();
            reference.set(APPLICATION_ID, reckoner.getSubtype(), reckoner.getId(), reckoner.getContainer(), reckoner.getContext());
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
                // entity = createSubmissionEntity();
                // TODO assignment submission entities
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
        String url = null;

        try {
            switch (reference.getSubType()) {
                case REF_TYPE_CONTENT:
                case REF_TYPE_ASSIGNMENT:
                    Assignment a = getAssignment(reference.getId());
                    url = createAssignmentEntity(a.getId()).getUrl();
                    break;
                case REF_TYPE_SUBMISSION:
                    // url = createAssignmentEntity(a.getId()).getUrl();
                    // TODO assignment submission entities
                    break;
                default:
                    log.warn("Unknown Entity subtype: {} in ref: {}", reference.getSubType(), reference.getReference());
            }
        } catch (Exception e) {
            log.warn("Could not get entity url with ref = {}", reference.getReference(), e);
        }

        return url;
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference reference, String userId) {
        Collection<String> references = new ArrayList<>();

        // for AssignmentService assignments:
        // if access set to SITE, use the assignment and site authzGroups.
        // if access set to GROUPED, use the assignment, and the groups, but not the site authzGroups.
        // if the user has SECURE_ALL_GROUPS in the context, ignore GROUPED access and treat as if SITE

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
                                grouped = Assignment.Access.GROUPED == a.getAccess();
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
        return new HttpAccess() {
            @Override
            public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
                    throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException {
                if (sessionManager.getCurrentSessionUserId() == null) {
                    // fail the request, user not logged in yet.
                } else {
                    if (REF_TYPE_SUBMISSIONS.equals(ref.getSubType())) {
                        String queryString = req.getQueryString();
                        res.setContentType("application/zip");
                        res.setHeader("Content-Disposition", "attachment; filename = bulk_download.zip");

                        try (OutputStream out = res.getOutputStream()) {
                            // get the submissions zip blob
                            getSubmissionsZip(out, ref.getReference(), queryString);

                        } catch (Exception ignore) {
                            log.error("Could not stream the zip of submissions for ref = {}", ref.getReference());
                        }
                    } else if (REF_TYPE_GRADES.equals(ref.getSubType())) {
                        res.setContentType("application/vnd.ms-excel");
                        res.setHeader("Content-Disposition", "attachment; filename = export_grades_file.xls");

                        try (OutputStream out = res.getOutputStream()) {
                            gradeSheetExporter.getGradesSpreadsheet(out, ref.getReference());
                        } catch (Exception ignore) {
                            log.error("Could not stream the grades for ref = {}", ref.getReference());
                        }
                    } else {
                        log.warn("Unhandled reference = {}", ref.getReference());
                        throw new EntityNotDefinedException(ref.getReference());
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
    public List allowReceiveSubmissionNotificationUsers(String context) {
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
    public Collection getGroupsAllowAddAssignment(String context) {
        return getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, null);
    }

    @Override
    public Collection getGroupsAllowGradeAssignment(String context, String assignmentReference) {
        return getGroupsAllowFunction(SECURE_ADD_ASSIGNMENT, context, null);
    }

    @Override
    public boolean allowUpdateAssignment(String assignmentReference) {
        return permissionCheck(SECURE_UPDATE_ASSIGNMENT, assignmentReference, null);
    }

    @Override
    public boolean allowRemoveAssignment(String assignmentReference) {
        return permissionCheck(SECURE_REMOVE_ASSIGNMENT, assignmentReference, null);
    }

    @Override
    public Collection getGroupsAllowRemoveAssignment(String context) {
        return getGroupsAllowFunction(SECURE_REMOVE_ASSIGNMENT, context, null);
    }

    @Override
    public boolean allowAddAssignment(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        if (permissionCheck(SECURE_ADD_ASSIGNMENT_CONTENT, resourceString, null)) return true;
        // if not, see if the user has any groups to which adds are allowed
        return (!getGroupsAllowAddAssignment(context).isEmpty());
    }

    @Override
    public boolean allowAddSubmission(String context) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        return permissionCheck(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, null);
    }

    @Override
    public boolean allowAddSubmissionCheckGroups(String context, Assignment assignment) {
        String resourceString = AssignmentReferenceReckoner.reckoner().context(context).subtype("s").reckon().getReference();
        return permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, resourceString, assignment);
    }

    @Override
    public List<User> allowAddSubmissionUsers(String assignmentReference) {
        return securityService.unlockUsers(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
    }

    @Override
    public List allowGradeAssignmentUsers(String assignmentReference) {
        List<User> users = securityService.unlockUsers(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference);

        try {
            Assignment a = getAssignment(assignmentReference);
            if (a.getAccess() == Assignment.Access.GROUPED) {
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
                                        log.warn("problem with getting user = {}", userId);
                                    }
                                }
                            }
                        }
                    }
                } catch (GroupNotDefinedException gnde) {
                    log.warn("Cannot get authz group for site = {}", a.getContext());
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch assignment with assignmentReference = {}", assignmentReference);
        }

        return users;
    }

    @Override
    public List allowAddAnySubmissionUsers(String context) {
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
    public List allowAddAssignmentUsers(String context) {
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
    public boolean allowGradeSubmission(String assignmentReference) {
        return permissionCheck(SECURE_GRADE_ASSIGNMENT_SUBMISSION, assignmentReference, null);
    }

    @Override
    public Assignment addAssignment(String context) throws PermissionException {
        // security check
        if (!allowAddAssignment(context)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null);
        }

        Assignment assignment = new Assignment();
        assignment.setContext(context);
        assignmentRepository.newAssignment(assignment);

        log.debug("Created new assignment {}", assignment.getId());

        // event for tracking
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, assignment.getId(), true));

        return assignment;
    }

    @Override
    public Assignment mergeAssignment(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        return null;
    }

    @Override
    public Assignment addDuplicateAssignment(String context, String assignmentId) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException {
        Assignment assignment = null;

        if (StringUtils.isNotBlank(assignmentId)) {
            if (!assignmentRepository.existsAssignment(assignmentId)) {
                throw new IdUnusedException(assignmentId);
            } else {
                log.debug("duplicating assignment with ref = {}", assignmentId);

                Assignment existingAssignment = getAssignment(assignmentId);

                assignment.setContext(context);
                assignment.setTitle(existingAssignment.getTitle() + " - " + rb.getString("assignment.copy"));
                assignment.setSection(existingAssignment.getSection());
                assignment.setOpenDate(existingAssignment.getOpenDate());
                assignment.setDueDate(existingAssignment.getDueDate());
                assignment.setDropDeadDate(existingAssignment.getDropDeadDate());
                assignment.setCloseDate(existingAssignment.getCloseDate());
                assignment.setDraft(true);
                assignment.setIsGroup(existingAssignment.getIsGroup());
                Map<String, String> properties = existingAssignment.getProperties();
                addLiveProperties(properties);
                assignment.setProperties(properties);
                assignmentRepository.newAssignment(assignment);
            }
        }
        return assignment;
    }

    @Override
    public void removeAssignment(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        // TODO we don't actually want to delete assignments just mark them as deleted "soft delete feature"
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

    // TODO removing related content from other tools shouldn't be the concern for assignments service
    // it should post an event and let those tools take action.
    // * Unless a transaction is required
    @Override
    public void removeAssignmentAndAllReferences(Assignment assignment) throws PermissionException {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        // TODO we don't actually want to delete assignments just mark them as deleted "soft delete feature"
        log.debug("attempting to delete assignment with id = {}", assignment.getId());
        Entity entity = createAssignmentEntity(assignment.getId());

        log.debug(this + " removeAssignmentAndAllReferences with id : " + assignment.getId());

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
//                M_log.warn(" removeAssignmentAndAllReferences(): cannot remove non-empty AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (PermissionException e) {
//                M_log.warn(" removeAssignmentAndAllReferences(): not allowed to remove AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            } catch (InUseException e) {
//                M_log.warn(" removeAssignmentAndAllReferences(): AssignmentContent object for assignment = " + assignment.getId() + " is in used. " + e.getMessage());
//            } catch (IdUnusedException e) {
//                M_log.warn(" removeAssignmentAndAllReferences(): cannot find AssignmentContent object for assignment = " + assignment.getId() + ". " + e.getMessage());
//            }

        // 7. remove assignment
        removeAssignment(assignment);

        // close the edit object
//            ((BaseAssignmentEdit) assignment).closeEdit();

        // 8. remove any realm defined for this resource
//            try {
//                authzGroupService.removeAuthzGroup(assignment.getReference());
//            } catch (AuthzPermissionException e) {
//                M_log.warn(" removeAssignment: removing realm for assignment reference=" + assignment.getReference() + " : " + e.getMessage());
//            }
//
//            // track event
//            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT, assignment.getReference(), true));
    }

    @Override
    @Transactional
    public AssignmentSubmission addSubmission(String assignmentId, String submitter) throws PermissionException {
        AssignmentSubmission submission = null;
        try {
            Assignment assignment = getAssignment(assignmentId);

            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
            if (!permissionCheckWithGroups(SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference, assignment)) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, assignmentReference);
            }

            User user = null;
            try {
                user = userDirectoryService.getUser(submitter);
            } catch (UserNotDefinedException e) {
                try {
                    user = userDirectoryService.getUser(sessionManager.getCurrentSessionUserId());
                } catch (UserNotDefinedException e1) {
                    log.error("Unknown submitter while adding a submission to assignment: {}", assignmentId);
                    return null;
                }
            }

            AssignmentSubmissionSubmitter submissionSubmitter = new AssignmentSubmissionSubmitter();
            submissionSubmitter.setSubmitter(user.getId());
            submissionSubmitter.setSubmittee(true);
            Set<AssignmentSubmissionSubmitter> submissionSubmitters = new HashSet<>();
            submissionSubmitters.add(submissionSubmitter);

            submission = new AssignmentSubmission();
            assignmentRepository.newSubmission(assignment, submission, Optional.of(submissionSubmitters), Optional.empty(), Optional.empty(), Optional.empty());

            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_SUBMISSION, submission.getId(), true));
        } catch (IdUnusedException iue) {
            log.error("A submission cannot be added to an unknown assignement: {}", assignmentId);
        }

        return submission;
    }

    @Override
    public AssignmentSubmission mergeSubmission(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        return null;
    }

    @Override
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
    public void updateAssignment(Assignment assignment) throws PermissionException {
        Assert.notNull(assignment, "Assignment cannot be null");
        Assert.notNull(assignment.getId(), "Assignment doesn't appear to have been persisted yet");

        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        // security check
        if (!allowUpdateAssignment(reference)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT, null);
        }

        assignmentRepository.updateAssignment(assignment);
    }

    @Override
    public void updateSubmission(AssignmentSubmission submission) throws PermissionException {
        Assert.notNull(submission, "Submission cannot be null");
        Assert.notNull(submission.getId(), "Submission doesn't appear to have been persisted yet");
        Assert.notNull(submission.getAssignment(), "Submission doesn't appear to have been persisted yet");

        if (!allowAddSubmission(submission.getAssignment().getContext())) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT_SUBMISSION, null);
        }

        assignmentRepository.updateSubmission(submission);
    }

    @Override
    public Assignment getAssignment(Reference reference) throws IdUnusedException, PermissionException {
        Assert.notNull(reference, "Reference cannot be null");
        return getAssignment(reference.getId());
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
        } else if (assignment.getOpenDate().toInstant().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.NOT_OPEN;
        } else if (assignment.getDueDate().toInstant().isAfter(currentTime.toInstant())) {
            return AssignmentConstants.Status.OPEN;
        } else if ((assignment.getCloseDate() != null) && (assignment.getCloseDate().toInstant().isBefore(currentTime.toInstant()))) {
            return AssignmentConstants.Status.CLOSED;
        } else {
            return AssignmentConstants.Status.DUE;
        }
    }

    @Override
    public AssignmentSubmission getSubmission(String submissionId) throws IdUnusedException, PermissionException {
        return assignmentRepository.findSubmission(submissionId);
    }

    @Override
    public Collection<Assignment> getAssignmentsForContext(String context) {
        log.debug("GET ASSIGNMENTS : CONTEXT : {}", context);
        List<Assignment> assignments = new ArrayList<>();
        if (StringUtils.isBlank(context)) return assignments;

        for (Assignment assignment : assignmentRepository.findAssignmentsBySite(context)) {
            String deleted = assignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_DELETED);
            if (StringUtils.isBlank(deleted)) {
                // not deleted, show it
                if (assignment.getDraft()) {
                    if (isDraftAssignmentVisible(assignment)) {
                        // only those who can see a draft assignment
                        assignments.add(assignment);
                    }
                } else {
                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }

    @Override
    public Map<Assignment, List<String>> getSubmittableAssignmentsForContext(String context) {
        return null;
    }

    @Override
    public AssignmentSubmission getSubmission(String assignmentId, User person) {
        return null;
    }

    @Override
    public AssignmentSubmission getSubmission(String assignmentId, String submitterId) {
        return null;
    }

    @Override
    public Map<User, AssignmentSubmission> getUserSubmissionMap(Assignment assignment, List<User> users) {
        return null;
    }

    @Override
    public AssignmentSubmission getSubmission(List submissions, User person) {
        return null;
    }

    @Override
    public Set<AssignmentSubmission> getSubmissions(Assignment assignment) {
        return assignment.getSubmissions();
    }

    @Override
    public List getSortedGroupUsers(Group g) {
        return null;
    }

    @Override
    public int getSubmittedSubmissionsCount(String assignmentRef) {
        return 0;
    }

    @Override
    public int getUngradedSubmissionsCount(String assignmentRef) {
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
            String aRef = AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getId();
            Assignment assignment = getAssignment(aRef);

            if (assignment.getIsGroup()) {
                Collection<Group> submitterGroups = getSubmitterGroupList(searchFilterOnly,
                        viewString.length() == 0 ? AssignmentConstants.ALL : viewString,
                        searchString,
                        aRef,
                        contextString == null ? assignment.getContext() : contextString);
                if (submitterGroups != null && !submitterGroups.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<>();
                    for (Group g : submitterGroups) {
                        log.debug("ZIP GROUP " + g.getTitle());
                        AssignmentSubmission sub = getSubmission(aRef, g.getId());
                        log.debug("ZIP GROUP " + g.getTitle() + " SUB " + (sub == null ? "null" : sub.getId()));
                        if (sub != null) {
                            submissions.add(sub);
                        }
                    }
                    StringBuilder exceptionMessage = new StringBuilder();

                    if (allowGradeSubmission(aRef)) {
                        zipGroupSubmissions(aRef,
                                assignment.getTitle(),
                                assignment.getTypeOfGrade().toString(),
                                assignment.getTypeOfSubmission(),
                                new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(siteService)),
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
                        aRef,
                        contextString == null ? assignment.getContext() : contextString);

                if (!submitters.isEmpty()) {
                    List<AssignmentSubmission> submissions = new ArrayList<AssignmentSubmission>(submitters.values());

                    StringBuilder exceptionMessage = new StringBuilder();

                    if (allowGradeSubmission(aRef)) {
                        SortedIterator sortedIterator;
                        if (assignmentUsesAnonymousGrading(assignment)) {
                            sortedIterator = new SortedIterator(submissions.iterator(), new AnonymousSubmissionComparator());
                        } else {
                            sortedIterator = new SortedIterator(submissions.iterator(), new AssignmentSubmissionComparator(siteService));
                        }
                        zipSubmissions(aRef,
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
        return null;
    }

    @Override
    public String assignmentReference(String id) {
        return null;
    }

    @Override
    public String submissionReference(String context, String id, String assignmentId) {
        return null;
    }

    @Override
    public String gradesSpreadsheetReference(String context, String assignmentId) {
        return null;
    }

    @Override
    public String submissionsZipReference(String context, String assignmentId) {
        return null;
    }

    @Override
    public boolean canSubmit(String context, Assignment a, String userId) {
        // submissions are never allowed to non-electronic assignments
        if (a.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
            return false;
        }

        // return false if not allowed to submit at all
        if (!allowAddSubmissionCheckGroups(context, a) && !allowAddAssignment(context)) return false;

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

            LocalDateTime currentTime = LocalDateTime.now();

            // return false if the assignment is draft or is not open yet
            LocalDateTime openTime = LocalDateTime.ofInstant(a.getOpenDate().toInstant(), ZoneId.systemDefault());
            if (a.getDraft() || openTime.isAfter(currentTime)) {
                return false;
            }

            // return false if the current time has passed the assignment close time
            LocalDateTime closeTime = LocalDateTime.ofInstant(a.getCloseDate().toInstant(), ZoneId.systemDefault());

            // get user's submission
            AssignmentSubmission submission = null;

            submission = getSubmission(createAssignmentEntity(a.getId()).getReference(), u);

            // check for allow resubmission or not first
            // return true if resubmission is allowed and current time is before resubmission close time
            // get the resubmit settings from submission object first
            String allowResubmitNumString = submission != null ? submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) : null;
            if (allowResubmitNumString != null && submission.getDateSubmitted() != null && this.hasBeenSubmitted(submission)) {
                try {
                    int allowResubmitNumber = Integer.parseInt(allowResubmitNumString);
                    String allowResubmitCloseTime = submission.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                    LocalDateTime resubmitCloseTime = null;

                    if (allowResubmitCloseTime != null) {
                        // see if a resubmission close time is set on submission level
                        resubmitCloseTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(allowResubmitCloseTime)), ZoneId.systemDefault());
                    } else {
                        // otherwise, use assignment close time as the resubmission close time
                        resubmitCloseTime = LocalDateTime.ofInstant(a.getCloseDate().toInstant(), ZoneId.systemDefault());
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
            log.warn("Unknown user for assignment ref = {}", createAssignmentEntity(a.getId()).getReference());
            return false;
        }
    }

    @Override
    public boolean canSubmit(String context, Assignment a) {
        return canSubmit(context, a, null);
    }

    @Override
    public Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString) {
        return null;
    }

    @Override
    public boolean getAllowSubmitByInstructor() {
        return false;
    }

    @Override
    public List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString) {
        return null;
    }

    @Override
    public Map<User, AssignmentSubmission> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString) {
        return null;
    }

    @Override
    public String escapeInvalidCharsEntry(String accentedString) {
        return null;
    }

    @Override
    public boolean assignmentUsesAnonymousGrading(Assignment a) {
        return false;
    }

    @Override
    public Integer getScaleFactor() {
        return null;
    }

    @Override
    public boolean hasBeenSubmitted(AssignmentSubmission s) {
        return false;
    }

    @Override
    public String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment, boolean allowAddAssignment, boolean allowSubmitAssignment) throws Exception {
        Assignment a = getAssignment(assignmentId);

        String assignmentContext = a.getContext(); // assignment context
        if (allowReadAssignment && a.getOpenDate().toInstant().isBefore(ZonedDateTime.now().toInstant())) {
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
                            + createAssignmentEntity(assignmentId)
                            + "&panel=Main&sakai_action=doView_assignment";
                } else if (allowSubmitAssignment) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + createAssignmentEntity(assignmentId)
                            + "&panel=Main&sakai_action=doView_submission";
                } else {
                    // user can read the assignment, but not submit, so
                    // render the appropriate url
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?assignmentId=" + assignmentId + "&assignmentReference="
                            + createAssignmentEntity(assignmentId)
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
        if (",".equals(FormattedText.getDecimalSeparator())) {
            defaultSeparator = ";";
        }

        return serverConfigurationService.getString("csv.separator", defaultSeparator);
    }

    @Override
    public String getXmlAssignment(Assignment assignment) {
        return assignmentRepository.toXML(assignment);
    }

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
                    String decSeparator = FormattedText.getDecimalSeparator();
                    rv = StringUtils.replace(gString, (",".equals(decSeparator) ? "." : ","), decSeparator);
                    NumberFormat nbFormat = FormattedText.getNumberFormat(new Double(Math.log10(m.getScaleFactor())).intValue(), new Double(Math.log10(m.getScaleFactor())).intValue(), false);
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

    @Override
    public String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor) {
        if (StringUtils.isBlank(grade)) return null;

        if (typeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
            if (grade != null && grade.length() > 0 && !"0".equals(grade)) {
                int dec = new Double(Math.log10(scaleFactor)).intValue();
                String decSeparator = FormattedText.getDecimalSeparator();
                String decimalGradePoint = "";
                try {
                    Integer.parseInt(grade);
                    // if point grade, display the grade with factor decimal place
                    int length = grade.length();
                    if (length > dec) {
                        decimalGradePoint = grade.substring(0, grade.length() - dec) + decSeparator + grade.substring(grade.length() - dec);
                    } else {
                        String newGrade = "0".concat(decSeparator);
                        for (int i = length; i < dec; i++) {
                            newGrade = newGrade.concat("0");
                        }
                        decimalGradePoint = newGrade.concat(grade);
                    }
                } catch (NumberFormatException e) {
                    try {
                        Float.parseFloat(grade);
                        decimalGradePoint = grade;
                    } catch (Exception e1) {
                        return grade;
                    }
                }
                // get localized number format
                NumberFormat nbFormat = FormattedText.getNumberFormat(dec, dec, false);
                DecimalFormat dcformat = (DecimalFormat) nbFormat;
                // show grade in localized number format
                try {
                    Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
                    decimalGradePoint = nbFormat.format(dblGrade);
                } catch (Exception e) {
                    return grade;
                }
                return decimalGradePoint;
            } else {
                return StringUtils.trimToEmpty(grade);
            }
        } else if (typeOfGrade == Assignment.GradeType.UNGRADED_GRADE_TYPE) {
            String ret = "";
            if (grade != null) {
                if (grade.equalsIgnoreCase("gen.nograd")) ret = rb.getString("gen.nograd");
            }
            return ret;
        } else if (typeOfGrade == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
            String ret = rb.getString("ungra");
            if (grade != null) {
                if (grade.equalsIgnoreCase("Pass")) ret = rb.getString("pass");
                else if (grade.equalsIgnoreCase("Fail")) ret = rb.getString("fail");
            }
            return ret;
        } else if (typeOfGrade == Assignment.GradeType.CHECK_GRADE_TYPE) {
            String ret = rb.getString("ungra");
            if (grade != null) {
                if (grade.equalsIgnoreCase("Checked")) ret = rb.getString("gen.checked");
            }
            return ret;
        } else {
            if (grade != null && grade.length() > 0) {
                return StringUtils.trimToEmpty(grade);
            } else {
                // return "ungraded" in stead
                return rb.getString("ungra");
            }
        }
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
                return groups;
            }

            // get a list of the group refs, which are authzGroup ids
            Set<String> groupRefs = groups.stream().map(Group::getReference).collect(Collectors.toSet());

            // ask the authzGroup service to filter them down based on function
            Set<String> allowedGroupRefs = authzGroupService.getAuthzGroupsIsAllowed(userId, function, groupRefs);

            // pick the Group objects from the site's groups to return, those that are in the allowedGroupRefs list
            rv = groups.stream().filter(g -> allowedGroupRefs.contains(g.getReference())).collect(Collectors.toSet());
        } catch (IdUnusedException e) {
            log.debug("site {} not found, {}", context, e.getMessage());
        }

        return rv;
    }

    private Assignment checkAssignmentAccessibleForUser(Assignment assignment, String currentUserId) throws PermissionException {

        if (assignment.getAccess() == Assignment.Access.GROUPED) {
            String context = assignment.getContext();
            Collection<String> asgGroups = assignment.getGroups();
            Collection<Group> allowedGroups = getGroupsAllowFunction(SECURE_ACCESS_ASSIGNMENT, context, currentUserId);
            // reject and throw PermissionException if there is no intersection
            if (!assignment.getAuthors().contains(currentUserId) && !CollectionUtils.containsAny(asgGroups, allowedGroups.stream().map(Group::getReference).collect(Collectors.toSet()))) {
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
        String deleted = assignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_DELETED);
        if (StringUtils.isBlank(deleted)) {
            // show not deleted, not draft, opened assignments
            // TODO do we need to use time zone
            Date openTime = assignment.getOpenDate();
            Date visibleTime = assignment.getVisibleDate();
            if ((openTime != null && LocalDateTime.now().isAfter(LocalDateTime.ofInstant(openTime.toInstant(), ZoneId.systemDefault()))
                    || (visibleTime != null && LocalDateTime.now().isAfter(LocalDateTime.ofInstant(visibleTime.toInstant(), ZoneId.systemDefault()))))
                    && !assignment.getDraft()) {
                return true;
            }
        } else {
            if (StringUtils.equalsIgnoreCase(deleted, Boolean.TRUE.toString())
                    && (assignment.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)
                    && getSubmission(assignment.getId(), userId) != null) {
                // and those deleted but not non-electronic assignments but the user has made submissions to them
                return true;
            }
        }
        return false;
    }

    private boolean isDraftAssignmentVisible(Assignment assignment) {
        return assignment.getAuthors().contains(userDirectoryService.getCurrentUser().getId()) // the creator can see it
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

    private boolean permissionCheckWithGroups(String permission, String resource, Assignment assignment) {
        boolean access = securityService.unlock(permission, siteService.siteReference(assignment.getContext()));
        // doesn't have permission for site or not in all.groups
        if (!access || !allowAllGroups(assignment.getContext())) {
            Collection<String> groupIds = assignment.getGroups();
            // if there are no groups then return access
            if (!groupIds.isEmpty()) {
                // if there are groups then lets check
                for (String groupId : groupIds) {
                    if (securityService.unlock(permission, groupId)) {
                        access = true;
                        // no need to check other groups
                        break;
                    }
                }

                // TODO write a unit test to test SAK-23081
                // if user is in group and permission is to submit a submission and the assignment is a group submission then check
                if (!access && SECURE_ADD_ASSIGNMENT_SUBMISSION.equals(permission) && assignment.getIsGroup()) {
                    access = securityService.unlock(permission, resource);
                }
            }
        }
        return access;
    }

    private void addLiveProperties(Map<String, String> properties) {
        String current = sessionManager.getCurrentSessionUserId();
        properties.put(ResourceProperties.PROP_CREATOR, current);
        properties.put(ResourceProperties.PROP_MODIFIED_BY, current);

        String now = LocalDateTime.now().format(dateTimeFormatter);
        properties.put(ResourceProperties.PROP_CREATION_DATE, now);
        properties.put(ResourceProperties.PROP_MODIFIED_DATE, now);

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
        if (associatedGradebookAssignment != null) {
            boolean isExternalAssignmentDefined = gradebookExternalAssessmentService.isExternalAssignmentDefined(context, associatedGradebookAssignment);
            if (isExternalAssignmentDefined) {
                gradebookExternalAssessmentService.removeExternalAssessment(context, associatedGradebookAssignment);
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

    // TODO this needs to be refactored
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
            String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeType.toString(), getCsvSeparator());

            String submittedText = "";
            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            if (isAdditionalNotesEnabled) {
                sheet.addHeader(rb.getString("grades.id"), rb.getString("grades.eid"), rb.getString("grades.lastname"),
                        rb.getString("grades.firstname"), rb.getString("grades.grade"),
                        rb.getString("grades.submissionTime"), rb.getString("grades.late"), rb.getString("gen.notes"));
            } else {
                sheet.addHeader(rb.getString("grades.id"), rb.getString("grades.eid"), rb.getString("grades.lastname"),
                        rb.getString("grades.firstname"), rb.getString("grades.grade"),
                        rb.getString("grades.submissionTime"), rb.getString("grades.late"));
            }

            // allow add assignment members
            List allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String submittersName = "";
            String caughtException = null;
            String caughtStackTrace = null;
            String submittersAdditionalNotesHtml = "";

            while (submissions.hasNext()) {
                AssignmentSubmission s = (AssignmentSubmission) submissions.next();
                boolean isAnon = assignmentUsesAnonymousGrading(s.getAssignment());
                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    // get the submission user id and see if the user is still in site
                    String userId = s.getSubmitters().stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().orElse(null).getSubmitter();
                    try {
                        User u = userDirectoryService.getUser(userId);
                        if (allowAddSubmissionUsers.contains(u)) {
                            submittersName = root;

                            User[] submitters = s.getSubmitters().stream().map(p -> {
                                try {
                                    return userDirectoryService.getUser(p.getSubmitter());
                                } catch (UserNotDefinedException e) {
                                    log.warn("User not found {}", p.getSubmitter());
                                    return null;
                                }
                            }).filter(Objects::nonNull).toArray(User[]::new);

                            String submittersString = "";
                            for (int i = 0; i < submitters.length; i++) {
                                if (i > 0) {
                                    submittersString = submittersString.concat("; ");
                                }
                                String fullName = submitters[i].getSortName();
                                // in case the user doesn't have first name or last name
                                if (fullName.indexOf(",") == -1) {
                                    fullName = fullName.concat(",");
                                }
                                submittersString = submittersString.concat(fullName);
                                // add the eid to the end of it to guarantee folder name uniqness
                                // if user Eid contains non ascii characters, the user internal id will be used
                                String userEid = submitters[i].getEid();
                                String candidateEid = escapeInvalidCharsEntry(userEid);
                                if (candidateEid.equals(userEid)) {
                                    submittersString = submittersString + "(" + candidateEid + ")";
                                } else {
                                    submittersString = submittersString + "(" + submitters[i].getId() + ")";
                                }
                                submittersString = escapeInvalidCharsEntry(submittersString);
                                // Work out if submission is late.
                                String latenessStatus = whenSubmissionMade(s);

                                String fullAnonId = s.getAnonymousSubmissionId();
                                String anonTitle = rb.getString("grading.anonymous.title");

                                String[] params = new String[7];
                                if (isAdditionalNotesEnabled && candidateDetailProvider != null) {
                                    List<String> notes = candidateDetailProvider.getAdditionalNotes(submitters[i], st).orElse(new ArrayList<String>());

                                    if (!notes.isEmpty()) {
                                        params = new String[notes.size() + 7];
                                        System.arraycopy(notes.toArray(new String[notes.size()]), 0, params, 7, notes.size());
                                    }
                                }

                                // SAK-17606
                                if (!isAnon) {
                                    params[0] = submitters[i].getDisplayId();
                                    params[1] = submitters[i].getEid();
                                    params[2] = submitters[i].getLastName();
                                    params[3] = submitters[i].getFirstName();
                                    params[4] = s.getGrade(); // TODO may need to look at this
                                    params[5] = s.getDateSubmitted().toString(); // TODO may need to be formatted
                                    params[6] = latenessStatus;
                                } else {
                                    params[0] = fullAnonId;
                                    params[1] = fullAnonId;
                                    params[2] = anonTitle;
                                    params[3] = anonTitle;
                                    params[4] = s.getGrade();
                                    params[5] = s.getDateSubmitted().toString(); // TODO same as above
                                    params[6] = latenessStatus;
                                }
                                sheet.addRow(params);
                            }

                            if (StringUtils.trimToNull(submittersString) != null) {
                                submittersName = submittersName.concat(StringUtils.trimToNull(submittersString));
                                submittedText = s.getSubmittedText();

                                // SAK-17606
                                if (isAnon) {
                                    submittersName = root + s.getAnonymousSubmissionId();
                                    submittersString = s.getAnonymousSubmissionId();
                                }

                                if (!withoutFolders) {
                                    submittersName = submittersName.concat("/");
                                } else {
                                    submittersName = submittersName.concat("_");
                                }

                                // record submission timestamp
                                if (!withoutFolders) {
                                    if (s.getSubmitted() && s.getDateSubmitted() != null) {
                                        ZipEntry textEntry = new ZipEntry(submittersName + "timestamp.txt");
                                        out.putNextEntry(textEntry);
                                        byte[] b = (s.getDateSubmitted().toString()).getBytes();
                                        out.write(b);
                                        textEntry.setSize(b.length);
                                        out.closeEntry();
                                    }
                                }

                                // create the folder structure - named after the submitter's name
                                if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                    // include student submission text
                                    if (withStudentSubmissionText) {
                                        // create the text file only when a text submission is allowed
                                        String submittersNameString = submittersName + submittersString;

                                        //remove folder name if Download All is without user folders
                                        if (withoutFolders) {
                                            submittersNameString = submittersName;
                                        }
                                        ZipEntry textEntry = new ZipEntry(submittersNameString + "_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE);
                                        out.putNextEntry(textEntry);
                                        byte[] text = submittedText.getBytes();
                                        out.write(text);
                                        textEntry.setSize(text.length);
                                        out.closeEntry();
                                    }

                                    // include student submission feedback text
                                    if (withFeedbackText) {
                                        // create a feedbackText file into zip
                                        ZipEntry fTextEntry = new ZipEntry(submittersName + "feedbackText.html");
                                        out.putNextEntry(fTextEntry);
                                        byte[] fText = s.getFeedbackText().getBytes();
                                        out.write(fText);
                                        fTextEntry.setSize(fText.length);
                                        out.closeEntry();
                                    }
                                }

                                if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                    // include student submission attachment
                                    if (withStudentSubmissionAttachment) {
                                        //remove "/" that creates a folder if Download All is without user folders
                                        String sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt");//jh + "/";
                                        if (!withoutFolders) {
                                            // create a attachment folder for the submission attachments
                                            sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt") + "/";
                                            sSubAttachmentFolder = escapeInvalidCharsEntry(sSubAttachmentFolder);
                                            ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                            out.putNextEntry(sSubAttachmentFolderEntry);

                                        } else {
                                            sSubAttachmentFolder = sSubAttachmentFolder + "_";
                                            //submittersName = submittersName.concat("_");
                                        }

                                        // add all submission attachment into the submission attachment folder
                                        zipAttachments(out, submittersName, sSubAttachmentFolder, s.getSubmittedAttachments());
                                        out.closeEntry();
                                    }
                                }

                                if (withFeedbackComment) {
                                    // the comments.txt file to show instructor's comments
                                    ZipEntry textEntry = new ZipEntry(submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE);
                                    out.putNextEntry(textEntry);
                                    byte[] b = FormattedText.encodeUnicode(s.getFeedbackComment()).getBytes();
                                    out.write(b);
                                    textEntry.setSize(b.length);
                                    out.closeEntry();
                                }

                                if (withFeedbackAttachment) {
                                    // create an attachment folder for the feedback attachments
                                    String feedbackSubAttachmentFolder = submittersName + rb.getString("download.feedback.attachment");
                                    if (!withoutFolders) {
                                        feedbackSubAttachmentFolder = feedbackSubAttachmentFolder + "/";
                                        ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
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
                                List<String> notes = candidateDetailProvider.getAdditionalNotes(u, st).orElse(new ArrayList<String>());
                                if (!notes.isEmpty()) {
                                    String noteList = "<ul>";
                                    for (String note : notes) {
                                        noteList += "<li>" + StringEscapeUtils.escapeHtml(note) + "</li>";
                                    }
                                    noteList += "</ul>";
                                    submittersAdditionalNotesHtml += "<tr><td style='padding-right:10px;padding-left:10px'>" + submittersString + "</td><td style='padding-right:10px'>" + noteList + "</td></tr>";
                                }
                            }
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
                    ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
                    out.putNextEntry(gradesCSVEntry);
                    sheet.write(out);
                    out.closeEntry();
                }

                if (isAdditionalNotesEnabled) {
                    ZipEntry additionalEntry = new ZipEntry(root + rb.getString("assignment.additional.notes.file.title") + ".html");
                    out.putNextEntry(additionalEntry);

                    String htmlString = htmlPreamble("additionalnotes");
                    htmlString += "<h1>" + rb.getString("assignment.additional.notes.export.title") + "</h1>";
                    htmlString += "<div>" + rb.getString("assignment.additional.notes.export.header") + "</div><br/>";
                    htmlString += "<table border=\"1\"  style=\"border-collapse:collapse;\"><tr><th>" + rb.getString("gen.student") + "</th><th>" + rb.getString("gen.notes") + "</th>" + submittersAdditionalNotesHtml + "</table>";
                    htmlString += "<br/><div>" + rb.getString("assignment.additional.notes.export.footer") + "</div>";
                    htmlString += htmlEnd();
                    log.debug("Additional information html: " + htmlString);

                    byte[] wes = htmlString.getBytes();
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

    // TODO refactor this
    protected void zipGroupSubmissions(String assignmentReference, String assignmentTitle, String gradeTypeString, Assignment.SubmissionType typeOfSubmission, Iterator submissions, OutputStream outputStream, StringBuilder exceptionMessage, boolean withStudentSubmissionText, boolean withStudentSubmissionAttachment, boolean withGradeFile, boolean withFeedbackText, boolean withFeedbackComment, boolean withFeedbackAttachment, String gradeFileFormat, boolean includeNotSubmitted) {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(outputStream);

            // create the folder structure - named after the assignment's title
            String root = escapeInvalidCharsEntry(Validator.escapeZipEntry(assignmentTitle)) + Entity.SEPARATOR;

            SpreadsheetExporter.Type type = SpreadsheetExporter.Type.valueOf(gradeFileFormat.toUpperCase());
            SpreadsheetExporter sheet = SpreadsheetExporter.getInstance(type, assignmentTitle, gradeTypeString, getCsvSeparator());

            String submittedText = "";
            if (!submissions.hasNext()) {
                exceptionMessage.append("There is no submission yet. ");
            }

            // Write the header
            sheet.addHeader("Group", rb.getString("grades.eid"), rb.getString("grades.members"),
                    rb.getString("grades.grade"), rb.getString("grades.submissionTime"), rb.getString("grades.late"));

            // allow add assignment members
            List allowAddSubmissionUsers = allowAddSubmissionUsers(assignmentReference);

            // Create the ZIP file
            String submittersName = "";
            String caughtException = null;
            String caughtStackTrace = null;
            while (submissions.hasNext()) {
                AssignmentSubmission s = (AssignmentSubmission) submissions.next();

                log.debug(this + " ZIPGROUP " + (s == null ? "null" : s.getId()));

                //SAK-29314 added a new value where it's by default submitted but is marked when the user submits
                if ((s.getSubmitted() && s.getUserSubmission()) || includeNotSubmitted) {
                    try {
                        submittersName = root;

                        User[] submitters = s.getSubmitters().stream().map(p -> {
                            try {
                                return userDirectoryService.getUser(p.getSubmitter());
                            } catch (UserNotDefinedException e) {
                                log.warn("User not found {}", p.getSubmitter());
                                return null;
                            }
                        }).filter(Objects::nonNull).toArray(User[]::new);

                        String submitterString = submitters[0].getDisplayName();// TODO gs.getGroup().getTitle() + " (" + gs.getGroup().getId() + ")";
                        String submittersString = "";
                        String submitters2String = "";

                        for (int i = 0; i < submitters.length; i++) {
                            if (i > 0) {
                                submittersString = submittersString.concat("; ");
                                submitters2String = submitters2String.concat("; ");
                            }
                            String fullName = submitters[i].getSortName();
                            // in case the user doesn't have first name or last name
                            if (fullName.indexOf(",") == -1) {
                                fullName = fullName.concat(",");
                            }
                            submittersString = submittersString.concat(fullName);
                            submitters2String = submitters2String.concat(submitters[i].getDisplayName());
                            // add the eid to the end of it to guarantee folder name uniqness
                            submittersString = submittersString + "(" + submitters[i].getEid() + ")";
                        }
                        String latenessStatus = whenSubmissionMade(s);

                        //Adding the row
                        sheet.addRow(submitterString, submittersString, submitters2String, // TODO gs.getGroup().getTitle(), gs.getGroup().getId(), submitters2String,
                                s.getGrade(), s.getDateSubmitted().toString(), latenessStatus);

                        if (StringUtils.trimToNull(submitterString) != null) {
                            submittersName = submittersName.concat(StringUtils.trimToNull(submitterString));
                            submittedText = s.getSubmittedText();

                            submittersName = submittersName.concat("/");

                            // record submission timestamp
                            if (s.getSubmitted() && s.getDateSubmitted() != null) {
                                ZipEntry textEntry = new ZipEntry(submittersName + "timestamp.txt");
                                out.putNextEntry(textEntry);
                                byte[] b = (s.getDateSubmitted().toString()).getBytes();
                                out.write(b);
                                textEntry.setSize(b.length);
                                out.closeEntry();
                            }

                            // create the folder structure - named after the submitter's name
                            if (typeOfSubmission != Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                // include student submission text
                                if (withStudentSubmissionText) {
                                    // create the text file only when a text submission is allowed
                                    ZipEntry textEntry = new ZipEntry(submittersName + submitterString + "_submissionText" + AssignmentConstants.ZIP_SUBMITTED_TEXT_FILE_TYPE);
                                    out.putNextEntry(textEntry);
                                    byte[] text = submittedText.getBytes();
                                    out.write(text);
                                    textEntry.setSize(text.length);
                                    out.closeEntry();
                                }

                                // include student submission feedback text
                                if (withFeedbackText) {
                                    // create a feedbackText file into zip
                                    ZipEntry fTextEntry = new ZipEntry(submittersName + "feedbackText.html");
                                    out.putNextEntry(fTextEntry);
                                    byte[] fText = s.getFeedbackText().getBytes();
                                    out.write(fText);
                                    fTextEntry.setSize(fText.length);
                                    out.closeEntry();
                                }
                            }

                            if (typeOfSubmission != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION && typeOfSubmission != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                                // include student submission attachment
                                if (withStudentSubmissionAttachment) {
                                    // create a attachment folder for the submission attachments
                                    String sSubAttachmentFolder = submittersName + rb.getString("stuviewsubm.submissatt") + "/";
                                    ZipEntry sSubAttachmentFolderEntry = new ZipEntry(sSubAttachmentFolder);
                                    out.putNextEntry(sSubAttachmentFolderEntry);
                                    // add all submission attachment into the submission attachment folder
                                    zipAttachments(out, submittersName, sSubAttachmentFolder, s.getSubmittedAttachments());
                                    out.closeEntry();
                                }
                            }

                            if (withFeedbackComment) {
                                // the comments.txt file to show instructor's comments
                                ZipEntry textEntry = new ZipEntry(submittersName + "comments" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE);
                                out.putNextEntry(textEntry);
                                byte[] b = FormattedText.encodeUnicode(s.getFeedbackComment()).getBytes();
                                out.write(b);
                                textEntry.setSize(b.length);
                                out.closeEntry();
                            }

                            if (withFeedbackAttachment) {
                                // create an attachment folder for the feedback attachments
                                String feedbackSubAttachmentFolder = submittersName + rb.getString("download.feedback.attachment") + "/";
                                ZipEntry feedbackSubAttachmentFolderEntry = new ZipEntry(feedbackSubAttachmentFolder);
                                out.putNextEntry(feedbackSubAttachmentFolderEntry);
                                // add all feedback attachment folder
                                zipAttachments(out, submittersName, feedbackSubAttachmentFolder, s.getFeedbackAttachments());
                                out.closeEntry();
                            }

                            if (submittersString.trim().length() > 0) {
                                // the comments.txt file to show instructor's comments
                                ZipEntry textEntry = new ZipEntry(submittersName + "members" + AssignmentConstants.ZIP_COMMENT_FILE_TYPE);
                                out.putNextEntry(textEntry);
                                byte[] b = FormattedText.encodeUnicode(submittersString).getBytes();
                                out.write(b);
                                textEntry.setSize(b.length);
                                out.closeEntry();
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
                    ZipEntry gradesCSVEntry = new ZipEntry(root + "grades." + sheet.getFileExtension());
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

    // TODO refactor this
    private String whenSubmissionMade(AssignmentSubmission s) {
        Date dueTime = s.getAssignment().getDueDate();
        Date submittedTime = s.getDateSubmitted();
        String latenessStatus;
        if (submittedTime == null) {
            latenessStatus = rb.getString("grades.lateness.unknown");
        } else if (dueTime != null && submittedTime.after(dueTime)) {
            latenessStatus = rb.getString("grades.lateness.late");
        } else {
            latenessStatus = rb.getString("grades.lateness.ontime");
        }
        return latenessStatus;
    }

    // TODO refactor this
    private void zipAttachments(ZipOutputStream out, String submittersName, String sSubAttachmentFolder, Collection<String> attachments) {
        int attachedUrlCount = 0;
        InputStream content = null;
        Map<String, Integer> done = new HashMap<>();
        for (String r : attachments) {
// TODO maybe be a reference
//           Reference r = (Reference) attachments.get(j);
            try {
                ContentResource resource = contentHostingService.getResource(r);

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

    private String htmlPreamble(String submissionOrReleaseGrade) {
        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
        buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
        buf.append("<html>\n");
        buf.append("  <head><title>");
        buf.append(getSubject(submissionOrReleaseGrade));
        buf.append("</title></head>\n");
        buf.append("  <body>\n");
        return buf.toString();
    }

    private String htmlEnd() {
        return "\n  </body>\n</html>\n";
    }

    private String getSubject(String submissionOrReleaseGrade) {
        String subject = "";
        if ("submission".equals(submissionOrReleaseGrade))
            subject = rb.getString("noti.subject.content");
        else if ("releasegrade".equals(submissionOrReleaseGrade))
            subject = rb.getString("noti.releasegrade.subject.content");
        else if ("additionalnotes".equals(submissionOrReleaseGrade))
            subject = rb.getString("assignment.additional.notes.export.title");
        else
            subject = rb.getString("noti.releaseresubmission.subject.content");
        return "Subject: " + subject;
    }
}
