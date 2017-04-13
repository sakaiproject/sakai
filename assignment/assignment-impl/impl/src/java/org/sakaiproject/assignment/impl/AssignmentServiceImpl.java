package org.sakaiproject.assignment.impl;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentEntity;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.persistence.AssignmentRepository;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
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
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
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

    @Setter private boolean allowGroupAssignments;
    @Setter private boolean allowGroupAssignmentsInGradebook;

    public void init() {

    }

    public void destroy() {

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
    public boolean parseEntityReference(String reference, Reference ref) {
        return false;
    }

    @Override
    public String getEntityDescription(Reference ref) {
        return null;
    }

    @Override
    public ResourceProperties getEntityResourceProperties(Reference ref) {
        return null;
    }

    @Override
    public Entity getEntity(Reference ref) {
        return null;
    }

    @Override
    public String getEntityUrl(Reference ref) {
        return null;
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference ref, String userId) {
        return null;
    }

    @Override
    public HttpAccess getHttpAccess() {
        return null;
    }

    @Override
    public boolean allowReceiveSubmissionNotification(String context) {
        return false;
    }

    @Override
    public List allowReceiveSubmissionNotificationUsers(String context) {
        return null;
    }

    @Override
    public boolean allowAddSiteAssignment(String context) {
        return false;
    }

    @Override
    public boolean allowAllGroups(String context) {
        return false;
    }

    @Override
    public boolean allowGetAssignment(String assignmentReference) {
        return false;
    }

    @Override
    public Collection getGroupsAllowAddAssignment(String context) {
        return null;
    }

    @Override
    public Collection getGroupsAllowGradeAssignment(String context, String assignmentReference) {
        return getGroupsAllowFunction(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, context, null);
    }

    @Override
    public boolean allowUpdateAssignment(String assignmentReference) {
        return false;
    }

    @Override
    public boolean allowRemoveAssignment(String assignmentReference) {
        return false;
    }

    @Override
    public boolean allowAddAssignment(String context) {
        String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "c" + Entity.SEPARATOR + context + Entity.SEPARATOR;
        log.debug("Allow assignment with resource string = {}", resourceString);

        // check security (throws if not permitted)
        if (unlockCheck(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_CONTENT, resourceString)) {
            return true;
        }

        // if not, see if the user has any groups to which adds are allowed
        return (!getGroupsAllowAddAssignment(context).isEmpty());
    }

    @Override
    public boolean allowGetAssignmentContent(String contentReference) {
        return false;
    }

    @Override
    public boolean allowUpdateAssignmentContent(String contentReference) {
        return false;
    }

    @Override
    public boolean allowRemoveAssignmentContent(String contentReference) {
        return false;
    }

    @Override
    public boolean allowAddSubmission(String context) {
        return false;
    }

    @Override
    public boolean allowAddSubmissionCheckGroups(String context, Assignment assignment) {
        return false;
    }

    @Override
    public List allowAddSubmissionUsers(String assignmentReference) {
        return null;
    }

    @Override
    public List allowGradeAssignmentUsers(String assignmentReference) {
        return null;
    }

    @Override
    public List allowAddAnySubmissionUsers(String context) {
        return null;
    }

    @Override
    public List allowAddAssignmentUsers(String context) {
        return null;
    }

    @Override
    public boolean allowGetSubmission(String submissionReference) {
        return false;
    }

    @Override
    public boolean allowUpdateSubmission(String submissionReference) {
        return false;
    }

    @Override
    public boolean allowRemoveSubmission(String submissionReference) {
        return false;
    }

    @Override
    public boolean allowGradeSubmission(String submissionReference) {
        return false;
    }

    @Override
    public Assignment addAssignment(String context) throws PermissionException {
        log.debug("Start Add Assignment");

        // security check
        if (!allowAddAssignment(context)) {
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_CONTENT, null);
        }

        Assignment assignment = new Assignment();
        assignment.setContext(context);
        assignment = assignmentRepository.save(assignment);

        log.debug("Created new assignment {}", assignment.getId());

        // event for tracking
        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT_CONTENT, assignment.getId(), true));
        log.debug("End Add Assignment");

        return assignment;
    }

    @Override
    public Assignment mergeAssignment(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        return null;
    }

    @Override
    public Assignment addDuplicateAssignment(String context, String assignmentReference) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException {
        return null;
    }

    @Override
    public void removeAssignment(Assignment assignment) throws PermissionException {

    }

    @Override
    public void removeAssignmentAndAllReferences(Assignment assignment) throws PermissionException {

    }

    @Override
    public Assignment editAssignment(String id) throws IdUnusedException, PermissionException, InUseException {
        return null;
    }

    @Override
    public void commitEdit(Assignment assignment) {

    }

    @Override
    public void cancelEdit(Assignment assignment) {

    }

    @Override
    public AssignmentSubmission addSubmission(String context, String assignmentId, String submitter) throws PermissionException {
        return null;
    }

    @Override
    public AssignmentSubmission mergeSubmission(Element el) throws IdInvalidException, IdUsedException, PermissionException {
        return null;
    }

    @Override
    public void removeSubmission(AssignmentSubmission submission) throws PermissionException {

    }

    @Override
    public AssignmentSubmission editSubmission(String id) throws IdUnusedException, PermissionException, InUseException {
        return null;
    }

    @Override
    public void commitEdit(AssignmentSubmission submission) {

    }

    @Override
    public void cancelEdit(AssignmentSubmission submission) {

    }

    @Override
    public Iterator getAssignmentContents(User owner) {
        return null;
    }

    @Override
    public Assignment getAssignment(String assignmentId) throws IdUnusedException, PermissionException {
        log.debug("GET ASSIGNMENT : REF : {}", assignmentId);


        Assignment assignment = assignmentRepository.findAssignment(assignmentId);
        if (assignment == null) throw new IdUnusedException(assignmentId);

        String currentUserId = sessionManager.getCurrentSessionUserId();
        // check security on the assignment
        checkAssignmentAccessibleForUser(assignment, currentUserId);
        return assignment;
    }

    @Override
    public String getAssignmentStatus(String assignmentId) throws IdUnusedException, PermissionException {
        return null;
    }

    @Override
    public AssignmentSubmission getSubmission(String submissionId) throws IdUnusedException, PermissionException {
        return null;
    }

    @Override
    public Iterable<Assignment> getAssignmentsForContext(String context) {
        return null;
    }

    @Override
    public Iterator getAssignmentsForContext(String context, String userId) {
        return null;
    }

    @Override
    public List<Assignment> getListAssignmentsForContext(String context) {
        return null;
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
    public List getSubmissions(Assignment assignment) {
        return null;
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
    public void getSubmissionsZip(OutputStream out, String ref) throws IdUnusedException, PermissionException {

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
    public String contentReference(String context, String id) {
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
    public boolean getAllowGroupAssignments() {
        return false;
    }

    @Override
    public boolean getAllowGroupAssignmentsInGradebook() {
        return false;
    }

    @Override
    public boolean canSubmit(String context, Assignment a, String userId) {
        return false;
    }

    @Override
    public boolean canSubmit(String context, Assignment a) {
        return false;
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
        return null;
    }

    @Override
    public String getDeepLink(String context, String assignmentId) throws Exception {
        return null;
    }

    @Override
    public String getCsvSeparator() {
        return null;
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

        String gAssignmentName = StringUtils.trimToEmpty(m.getProperties().get(AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
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

    private String getAccessPoint(boolean relative) {
        return (relative ? "" : serverConfigurationService.getAccessUrl()) + AssignmentServiceConstants.REFERENCE_ROOT;
    }

    private boolean unlockCheck(String lock, String resource) {
        return securityService.unlock(lock, resource);
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
            if (securityService.unlock(userId, AssignmentServiceConstants.SECURE_ALL_GROUPS, siteService.siteReference(context))
                    && unlockCheck(function, siteService.siteReference(context))) {
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
            Collection<Group> allowedGroups = getGroupsAllowFunction(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, context, currentUserId);
            // reject and throw PermissionException if there is no intersection
            if (!assignment.getAuthors().contains(currentUserId) && !CollectionUtils.containsAny(asgGroups, allowedGroups.stream().map(Group::getReference).collect(Collectors.toSet()))) {
                throw new PermissionException(currentUserId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, assignment.getId());
            }
        }

        if (allowAddAssignment(assignment.getContext())) {
            // always return for users can add assignent in the context
            return assignment;
        } else if (isAvailableOrSubmitted(assignment, currentUserId)) {
            return assignment;
        }
        throw new PermissionException(currentUserId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, assignment.getId());
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
}
