package org.sakaiproject.assignment.impl;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
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
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {
    @Setter private boolean allowGroupAssignments;
    @Setter private boolean allowGroupAssignmentsInGradebook;
    @Setter private AnnouncementService announcementService;
    @Setter private AssignmentActivityProducer assignmentActivityProducer;
    @Setter private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
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
    public boolean allowAddAssignment(String context) {
        return false;
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
        return null;
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
    public boolean allowAddAssignmentContent(String context) {
        return false;
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
        return null;
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
        return null;
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
    public AssignmentSubmission getSubmission(String assignmentReference, String submitterId) {
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
}
