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
package org.sakaiproject.assignment.entityproviders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static org.sakaiproject.assignment.api.AssignmentConstants.*;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.fileupload.FileItem;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.ContentReviewResult;
import org.sakaiproject.assignment.api.MultiGroupRecord;
import org.sakaiproject.assignment.api.sort.AssignmentSubmissionComparator;
import org.sakaiproject.assignment.tool.AssignmentToolUtils;
import org.sakaiproject.assignment.api.model.*;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.tsugi.lti.LTIUtil;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

@Slf4j
@Setter
public class AssignmentEntityProvider extends AbstractEntityProvider implements EntityProvider,
        CoreEntityProvider, Resolvable, ActionsExecutable, Describeable,
        AutoRegisterEntityProvider, PropertyProvideable, Outputable, Inputable {

    public final static String ENTITY_PREFIX = "assignment";

    private static ResourceLoader rb = new ResourceLoader("assignment");

    private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
    private AssignmentService assignmentService;
    private AssignmentToolUtils assignmentToolUtils;
    private ContentHostingService contentHostingService;
    private ContentTypeImageService contentTypeImageService;
    private EntityBroker entityBroker;
    private EntityManager entityManager;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private ToolManager toolManager;
    private AssignmentSupplementItemService assignmentSupplementItemService;
    private GradingService gradingService;
    private ServerConfigurationService serverConfigurationService;
    private UserDirectoryService userDirectoryService;
    private UserTimeService userTimeService;
    private FormattedText formattedText;
    private LTIService ltiService;

    // HTML is deliberately not handled here, so that it will be handled by RedirectingAssignmentEntityServlet
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON, Formats.FORM };
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
    }

    public Object getSampleEntity() {
        return new SimpleAssignment();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix
     * ()
     */
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists
     * (java.lang.String)
     */
    public boolean entityExists(String id) {

        // This will look up the ref from the database, so if ref is not null,
        // that means it found one.
        return assignmentService.assignmentReference(id) != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#
     * getEntity(org.sakaiproject.entitybroker.EntityReference)
     */
    public Object getEntity(EntityReference ref) {

        if (ref == null || ref.getId() == null) {
            throw new IllegalArgumentException("ref and id must be set for assignments");
        }
        SimpleAssignment assignment;
        try {
            assignment = new SimpleAssignment(assignmentService.getAssignment(ref.getId()), true);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No assignment found: " + ref, ref.toString(), e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }
        return assignment;
    }

    @EntityCustomAction(action = "annc", viewKey = EntityView.VIEW_LIST)
    public Map<String, Object> getAssignDataForAnnouncement(EntityView view,
                                                            Map<String, Object> params) {

        Map<String, Object> assignData = new HashMap<>();

        String context = view.getPathSegment(2);
        String assignmentId = view.getPathSegment(3);
        if (context == null || assignmentId == null) {
            // format of the view should be in a standard assignment reference
            throw new IllegalArgumentException(
                    "Must include context and assignmentId in the path ("
                            + view
                            + "): e.g. /assignment/a/{context}/{assignmentId}");
        }

        SecurityAdvisor securityAdvisor = createSecurityAdvisor(
            sessionManager.getCurrentSessionUserId(),
            SECURE_ADD_ASSIGNMENT,
            assignmentService.assignmentReference(null, context)
        );

        try {
            // enable permission to view possible draft assignment
            securityService.pushAdvisor(securityAdvisor);

            Assignment a = assignmentService.getAssignment(assignmentService
                    .assignmentReference(context, assignmentId));
            assignData.put("assignment", a);
            assignData.put("context", context);
            assignData.put("assignmentId", assignmentId);

            // This is for checking to see if there is a link to announcements
            // in the assignment
            String announcementCheck = a.getProperties().get("CHEF:assignment_opendate_announcement_message_id");

            // the message id passed in through parameters
            String messageId = (String) params.get("messageId");

            // Lots of checks to make absolutely sure this is the assignment we
            // are looking for
            if (announcementCheck != null && !"".equals(announcementCheck)
                    && messageId != null && !"".equals(messageId)
                    && announcementCheck.equals(messageId)) {
                assignData.put("assignmentTitle", a.getTitle());
                String assignmentContext = a.getContext(); // assignment context
                boolean allowReadAssignment = assignmentService.allowGetAssignment(assignmentContext);
                // check for read permission
                if (allowReadAssignment
                        && a.getOpenDate().isBefore(Instant.now())) {
                    // this checks if we want to display an assignment link
                    try {
                        Site site = siteService.getSite(assignmentContext); // site id
                        ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
                        boolean allowAddAssignment = assignmentService.allowAddAssignment(assignmentContext);
                        // this checks for the asn.new permission and
                        // determines the url we present the user
                        boolean allowSubmitAssignment = assignmentService.allowAddSubmission(assignmentContext);
                        // this checks for the asn.submit permission and
                        // determines the url we present the user

                        // Three different urls to be rendered depending on the
                        // user's permission
                        if (allowAddAssignment) {
                            assignData.put("assignmentUrl",
                                            serverConfigurationService
                                                    .getPortalUrl()
                                                    + "/directtool/"
                                                    + fromTool.getId()
                                                    + "?assignmentId="
                                                    + AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()
                                                    + "&panel=Main&sakai_action=doView_assignment");
                        } else if (allowSubmitAssignment) {
                            String sakaiAction = "doView_submission";
                            if(a.getHonorPledge()) {
                                sakaiAction = "doView_assignment_honorPledge";
                            }
                            assignData.put("assignmentUrl",
                                            serverConfigurationService
                                                    .getPortalUrl()
                                                    + "/directtool/"
                                                    + fromTool.getId()
                                                    + "?assignmentReference="
                                                    + AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()
                                                    + "&panel=Main&sakai_action=" + sakaiAction);
                        } else {
                            // user can read the assignment, but not submit, so
                            // render the appropriate url
                            assignData.put("assignmentUrl",
                                            serverConfigurationService
                                                    .getPortalUrl()
                                                    + "/directtool/"
                                                    + fromTool.getId()
                                                    + "?assignmentId="
                                                    + a.getId()
                                                    + "&panel=Main&sakai_action=doView_assignment_as_student");
                        }
                    } catch (IdUnusedException e) {
                        // No site found
                        assignData.remove("assignment");
                        assignData.remove("context");
                        assignData.remove("assignmentId");
                        assignData.remove("assignmentTitle");
                        assignData.remove("assignmentUrl");
                        throw new IdUnusedException("No site found while creating assignment url");
                    }
                }
            }
        } catch (IdUnusedException e) {
            assignData.remove("assignment");
            assignData.remove("context");
            assignData.remove("assignmentId");
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new EntityNotFoundException("No assignment found", assignmentId, e);
        } catch (PermissionException e) {
            assignData.remove("assignment");
            assignData.remove("context");
            assignData.remove("assignmentId");
            assignData.remove("assignmentTitle");
            assignData.remove("assignmentUrl");
            throw new SecurityException(e);
        } finally {
            securityService.popAdvisor(securityAdvisor);
        }
        return assignData;
    }

    private SecurityAdvisor createSecurityAdvisor(String currentUserId, String requiredFunction, String requiredReference) {

        return (userId, function, reference) -> currentUserId.equals(userId) &&
                requiredFunction.equals(function) &&
                requiredReference.equals(reference)
                ? SecurityAdvisor.SecurityAdvice.ALLOWED
                : SecurityAdvisor.SecurityAdvice.PASS
                ;
    }

    @EntityCustomAction(action = "deepLinkWithPermissions", viewKey = EntityView.VIEW_LIST)
    public Map<String, String> getAssignmentDeepLinks(EntityView view,
                                                      Map<String, Object> params) {

        String context = view.getPathSegment(2);
        String assignmentId = view.getPathSegment(3);
        if (context == null || assignmentId == null) {
            // format of the view should be in a standard assignment reference
            throw new IllegalArgumentException(
                    "Must include context and assignmentId in the path ("
                            + view
                            + "): e.g. /direct/assignment/deepLinkWithPermissions/{context}/{assignmentId}");
        }

        Map<String, String> assignData = new HashMap<>();

        try {
            Assignment a = assignmentService.getAssignment(assignmentId);
            assignData.put("assignmentId", assignmentId);
            assignData.put("assignmentTitle", a.getTitle());

            boolean allowReadAssignment = params.get("allowReadAssignment") != null ? ((Boolean) params
                    .get("allowReadAssignment")).booleanValue() : false;
            boolean allowAddAssignment = params.get("allowAddAssignment") != null ? ((Boolean) params
                    .get("allowAddAssignment")).booleanValue() : false;
            boolean allowSubmitAssignment = params.get("allowSubmitAssignment") != null ? ((Boolean) params
                    .get("allowSubmitAssignment")).booleanValue() : false;
            boolean allowGradeAssignment = params.get("allowGradeAssignment") != null ? ((Boolean) params
                    .get("allowGradeAssignment")).booleanValue() : false;

            assignData.put("assignmentUrl"
                    , assignmentService.getDeepLinkWithPermissions(context, assignmentId
                            , allowReadAssignment, allowAddAssignment, allowSubmitAssignment, allowGradeAssignment));
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("Assignment or site not found", assignmentId, e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        } catch (Exception e) {
            throw new EntityException(e.getMessage(), assignmentId);
        }
        return assignData;
    }

    @EntityCustomAction(action = "deepLink", viewKey = EntityView.VIEW_LIST)
    public Map<String, String> getAssignmentDeepLink(EntityView view,
                                                     Map<String, Object> params) {

        String context = view.getPathSegment(2);
        String assignmentId = view.getPathSegment(3);
        if (context == null || assignmentId == null) {
            // format of the view should be in a standard assignment reference
            throw new IllegalArgumentException(
                    "Must include context and assignmentId in the path ("
                            + view
                            + "): e.g. /direct/assignment/deepLink/{context}/{assignmentId}");
        }

        Map<String, String> assignData = new HashMap<String, String>();

        try {
            Assignment a = assignmentService.getAssignment(assignmentId);
            assignData.put("assignmentId", assignmentId);
            assignData.put("assignmentTitle", a.getTitle());
            assignData.put("assignmentUrl", assignmentService.getDeepLink(context, assignmentId, sessionManager.getCurrentSessionUserId()));
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("Assignment or site not found", assignmentId, e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        } catch (Exception e) {
            throw new EntityException(e.getMessage(), assignmentId);
        }
        return assignData;
    }

    /**
     * site/siteId
     */
    @EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
    public List<?> getAssignmentsForSite(EntityView view,
                                         Map<String, Object> params) {

        List<SimpleAssignment> rv = new ArrayList<>();
        String siteId = view.getPathSegment(2);

        // check user can access this site
        Site site;
        try {
            site = siteService.getSiteVisit(siteId);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
        } catch (PermissionException e) {
            throw new EntityNotFoundException("No access to site: " + siteId, siteId);
        }

        assignmentService.getAssignmentsForContext(siteId).stream()
        .map(a->{
            SimpleAssignment as = new SimpleAssignment(a);
            if(as.getSubmissions() != null && !as.getSubmissions().isEmpty() && !canGrade(a)) {
                for(SimpleSubmission ss : as.getSubmissions()) {
                    ss.setPrivateNotes(null);
                    if(!ss.getReturned()) {
                        ss.setFeedbackText(null);
                        ss.setGrade(null);
                        ss.setFeedbackAttachments(null);
                        ss.setFeedbackComment(null);
                        for(SimpleSubmitter sser : ss.getSubmitters()) {
                            sser.setGrade(null);
                        }
                    }
                }
           }
           return as;
        })
        .filter(a->a!=null)
        .forEach(rv::add);

        return rv;
    }

    /**
     * my
     */
    @EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
    public List<?> getMyAssignmentsForAllSite(EntityView view,
                                              Map<String, Object> params) {
        List<SimpleAssignment> rv = new ArrayList<SimpleAssignment>();
        String userId = sessionManager.getCurrentSessionUserId();

        // get list of all sites
        List<Site> sites = siteService.getSites(SiteService.SelectionType.ACCESS, null, null, null, SiteService.SortType.TITLE_ASC, null);
        // no need to check user can access this site, as the get sites only returned accessible sites

        // get all assignments from each site
        for (Site site : sites) {
            String siteId = site.getId();
            assignmentService.getAssignmentsForContext(siteId).stream().map(SimpleAssignment::new).forEach(rv::add);
        }

        return rv;

    }

    /**
     * item/assignmentId
     */
    @EntityCustomAction(action = "item", viewKey = EntityView.VIEW_LIST)
    public SimpleAssignment getAssignment(EntityView view, Map<String, Object> params) {

        String assignmentId = view.getPathSegment(2);

        // check user can access this assignment
        Assignment a;
        try {
            a = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("Invalid assignment id: " + assignmentId, assignmentId);
        } catch (PermissionException e) {
            throw new EntityNotFoundException("No access to assignment: " + assignmentId, assignmentId);
        }
        return new SimpleAssignment(a, true);
    }

    @EntityCustomAction(action = "addTimeSheet", viewKey = EntityView.VIEW_NEW)
    public int addTimeSheet(Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        User user;
        try {
            user = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException unde) {
            log.warn("You need to be logged in to add time sheet register");
            throw new EntityException("You need to be logged in to add time sheet register", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String assignmentId = (String) params.get("tsAssignmentId");
        if (StringUtils.isBlank(assignmentId)) {
            log.warn("You need to supply the assignmentId and ref");
            throw new EntityException("You need to supply the assignmentId and ref", "", HttpServletResponse.SC_NOT_FOUND);
        }

        AssignmentSubmission submission;
        try {
            submission = assignmentService.getSubmission(assignmentId, user);
        } catch (PermissionException pe) {
            log.warn("You can't modify this sumbitter");
            throw new EntityException("You can't modify this sumbitter", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        if (submission == null) {
            String submitterId = user.getId();

            try {
                submission = assignmentService.addSubmission(assignmentId, submitterId);
                if (submission != null) {
                    submission.setSubmitted(true);
                    submission.setUserSubmission(false);
                    submission.setDateModified(Instant.now());
                    submission.getSubmitters().stream().filter(sb -> sb.getSubmitter().equals(submitterId)).findAny().ifPresent(sb -> sb.setSubmittee(false));
                    assignmentService.updateSubmission(submission);
                }
            } catch (PermissionException e) {
                log.warn("Could not add submission for assignment/submitter: {}/{}, {}", assignmentId, submitterId, e.toString());
                throw new EntityException("You can't modify this sumbitter", "", HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        String duration = (String) params.get("tsDuration");

        if (!assignmentService.isValidTimeSheetTime(duration)) {
            log.warn("Wrong time format. Must match XXh YYm");
            throw new EntityException("Wrong time format. Must be XXHXXM","",HttpServletResponse.SC_BAD_REQUEST);
        }

        String comment = (String) params.get("tsComment");
        comment = formattedText.processFormattedText(comment, null, null);
        if (StringUtils.isBlank(comment)) {
            log.warn("Comment field format is not valid");
            throw new EntityException("Comment field format is not valid", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Instant startTime;
        try {
            int month = Integer.parseInt((String) params.get("new_ts_record_month"));
            int day = Integer.parseInt((String) params.get("new_ts_record_day"));
            int year = Integer.parseInt((String) params.get("new_ts_record_year"));
            int hour = Integer.parseInt((String) params.get("new_ts_record_hour"));
            int min = Integer.parseInt((String) params.get("new_ts_record_minute"));

            startTime = LocalDateTime.of(year, month, day, hour, min, 0).atZone(userTimeService.getLocalTimeZone().toZoneId()).toInstant();
        } catch (NumberFormatException nfe) {
            startTime = Instant.now();
        }

        AssignmentSubmissionSubmitter submissionSubmitter = submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(user.getId())).findAny().orElse(null);

        if (submissionSubmitter == null) {
            log.warn("You submitter does not exist");
            throw new EntityException("You submitter does not exist", "", HttpServletResponse.SC_NOT_FOUND);
        }

        TimeSheetEntry timeSheet = new TimeSheetEntry();
        timeSheet.setComment(comment);
        timeSheet.setStartTime(startTime);
        timeSheet.setDuration(duration);
        timeSheet.setUserId(userId);

        try {
            assignmentService.saveTimeSheetEntry(submissionSubmitter, timeSheet);
        } catch (PermissionException e) {
            log.warn("You can't modify this sumbitter");
            throw new EntityException("You can't modify this sumbitter", "", HttpServletResponse.SC_UNAUTHORIZED);
        }
        return HttpServletResponse.SC_OK;
    }

    @EntityCustomAction(action = "removeTimeSheet", viewKey = EntityView.VIEW_NEW)
    public int removeTimeSheet(Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        User user;
        try {
            user = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException unde) {
            log.warn("You need to be logged in to add time sheet register");
            throw new EntityException("You need to be logged in to remove time sheet register", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String assignmentId = (String) params.get("tsAssignmentId");
        if (StringUtils.isBlank(assignmentId)) {
            log.warn("You need to supply the assignmentId and ref");
            throw new EntityException("You need to supply the assignmentId and ref", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        AssignmentSubmission submission;
        try {
            submission = assignmentService.getSubmission(assignmentId, user);
        } catch (PermissionException e1) {
            log.warn("You can't modify this sumbitter");
            throw new EntityException("You can't modify this sumbitter", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        List<Long> timeSheetIds;
        Object ts = params.get("selectedTimeSheets[]");
        if (ts instanceof String[]) {
            List<String> list = Arrays.asList((String[]) ts);
            timeSheetIds = list.stream().filter(Objects::nonNull).map(Long::parseLong).collect(Collectors.toList());
        } else if (ts instanceof String) {
            timeSheetIds = Collections.singletonList(Long.parseLong(ts.toString()));
        } else {
            log.warn("Selected time sheets could not be retrieved from request parameters");
            throw new EntityException("Selected time sheet must be provided.", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        for (Long timeSheetId : timeSheetIds) {
            if (null == timeSheetId) {
                log.warn("A selected time sheet was null");
                throw new EntityException("You need to supply the submissionId and ref", "", HttpServletResponse.SC_BAD_REQUEST);
            }

            try {
                assignmentService.deleteTimeSheetEntry(timeSheetId);
            } catch (PermissionException e) {
                log.warn("Could not delete the selected time sheet");
                throw new EntityException("You can't modify this sumbitter", "", HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        return HttpServletResponse.SC_OK;
    }

    @EntityCustomAction(action = "getTimeSheet", viewKey = EntityView.VIEW_LIST)
    public Map<String, String> getTimeSheet(EntityView view , Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        Map<String, String> assignData = new HashMap<>();

        User u;
        try {
            u = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e2) {
            log.warn("You need to be logged in to add time sheet register");
            throw new EntityException("You need to be logged in to get timesheet", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        String assignmentId = view.getPathSegment(2);

        if (StringUtils.isBlank(assignmentId)) {
            log.warn("You need to supply the assignmentId and ref");
            throw new EntityException("Cannot execute custom action (getTimeSheet): Illegal arguments: Must include context and assignmentId in the path (/assignment.json): e.g. /assignment/getTimeSheet/{assignmentId} (rethrown)", assignmentId, HttpServletResponse.SC_BAD_REQUEST);
        }

        AssignmentSubmission as;
        try {
            as = assignmentService.getSubmission(assignmentId, u);
        } catch (PermissionException e1) {
            log.warn("You can't modify this sumbitter");
            throw new EntityException("You don't have permissions read submission " + assignmentId, "", HttpServletResponse.SC_FORBIDDEN);
        }
        if(as == null) {
            throw new EntityException("There are not worklog records for this assignment ", assignmentId, HttpServletResponse.SC_BAD_REQUEST); 
        }
        AssignmentSubmissionSubmitter submitter = as.getSubmitters().stream().findAny().orElseThrow(() -> new EntityException("There are not worklog records for this assignment ", assignmentId, HttpServletResponse.SC_BAD_REQUEST));
        assignData.put("timeSpent", submitter.getSubmittee() ? submitter.getTimeSpent() : assignmentService.getTotalTimeSheet(submitter));

        return assignData;
    }

    private Map<String, Object> submissionToMap(Set<String> activeSubmitters, Assignment assignment, SimpleAssignment simpleAssignment, AssignmentSubmission as, boolean hydrate) {

        Map<String, Object> submission = new HashMap<>();

        submission.put("id", as.getId());
        submission.put("hydrated", hydrate);

        if (as.getUserSubmission()) submission.put("submitted", as.getUserSubmission());

        if (as.getGraded()) submission.put("graded", as.getGraded());

        if (hydrate) {
            Set<AssignmentSubmissionSubmitter> submitters = as.getSubmitters();

            // Add the SubmissionReview Launch information if this tool has requested it
            // https://www.imsglobal.org/spec/lti-ags/v2p0#submission-review-message
            // LTI supports group assignments but requires a user ID as the submitter - 
            // it does not accept group IDs directly. For group assignments, we use
            // a representative user ID from the group.

            if (submitters.size() >= 1) {
                String submitterId = null;
                Optional<AssignmentSubmissionSubmitter> submittee = assignmentService.getSubmissionSubmittee(as);
                if (submittee.isPresent()) {
                    submitterId = submittee.get().getSubmitter();
                    log.debug("LTI found submittee: {}", submitterId);
                }
                                
                Integer contentKey = assignment.getContentId();
                if (StringUtils.isNotBlank(submitterId) && contentKey != null) {
                    String siteId = assignment.getContext();
                    Map<String, Object> content = ltiService.getContent(contentKey.longValue(), siteId);
                    if (content != null) {
                        String contentItem = StringUtils.trimToEmpty((String) content.get(LTIService.LTI_CONTENTITEM));
                        boolean submissionReviewAvailable = contentItem.indexOf("\"submissionReview\"") > 0;
                        
                        String ltiSubmissionLaunch = "/access/lti/site/" + siteId + "/content:" + contentKey;
                        
                        // Always use for_user parameter since LTI requires a user ID
                        ltiSubmissionLaunch += "?for_user=" + submitterId;
                        
                        if (submissionReviewAvailable) {
                            ltiSubmissionLaunch = ltiSubmissionLaunch + "&message_type=content_review";
                        }
                        log.debug("ltiSubmissionLaunch={}", ltiSubmissionLaunch);
                        submission.put("ltiSubmissionLaunch", ltiSubmissionLaunch);
                    }
                }
            }

            if (assignment.getTypeOfGrade() == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
                submission.put("grade", StringUtils.isBlank(as.getGrade()) ? AssignmentConstants.UNGRADED_GRADE_STRING : as.getGrade());
            } else if (StringUtils.isNotBlank(as.getGrade())) {
                submission.put("grade", assignmentService.getGradeDisplay(as.getGrade(), assignment.getTypeOfGrade(), assignment.getScaleFactor()));
            }

            boolean draft = assignmentToolUtils.isDraftSubmission(as);
            if (draft) {
                submission.put("draft", draft);
            }

            Instant now = Instant.now();
            Instant due = simpleAssignment.getDueTime();
            Instant close = simpleAssignment.getCloseTime();
            boolean visible = !draft || (close != null ? now.isAfter(close) : due != null && now.isAfter(due));
            submission.put("visible", visible);

            if (as.getDateSubmitted() != null || (draft && visible)) {

                String submittedText = as.getSubmittedText();
                if (StringUtils.isNotBlank(submittedText)) {
                    submission.put("submittedText", submittedText);
                }
                if (as.getDateSubmitted() != null) {
                    submission.put("dateSubmittedEpochSeconds", as.getDateSubmitted().getEpochSecond());
                    String dateSubmitted = userTimeService.dateTimeFormat(as.getDateSubmitted(), null, null);
                    if (StringUtils.isNotBlank(dateSubmitted)) {
                        submission.put("dateSubmitted", dateSubmitted);
                    }
                    submission.put("late", as.getDateSubmitted().compareTo(as.getAssignment().getDueDate()) > 0);
                }

                List<Map<String, String>> submittedAttachments = as.getAttachments().stream().map(ref -> {

                        String id = entityManager.newReference(ref).getId();
                        try {
                            ContentResource cr = contentHostingService.getResource(id);
                            Map<String, String> attachment = new HashMap<>();
                            attachment.put("name", cr.getProperties().getPropertyFormatted(cr.getProperties().getNamePropDisplayName()));
                            attachment.put("creationDate", cr.getProperties().getPropertyFormatted(cr.getProperties().getNamePropCreationDate()));
                            attachment.put("contentLength", cr.getProperties().getPropertyFormatted(cr.getProperties().getNamePropContentLength()));
                            attachment.put("ref", cr.getReference());
                            attachment.put("url", cr.getUrl());
                            attachment.put("type", cr.getContentType());
                            attachment.put("iconClass", contentTypeImageService.getContentTypeImageClass(cr.getContentType()));
                            return attachment;
                        } catch (Exception e) {
                            log.info("There was an attachment on submission {} that was invalid", as.getId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!submittedAttachments.isEmpty()) {
                    submission.put("submittedAttachments", submittedAttachments);
                }

                SecurityAdvisor securityAdvisor = (String userId, String function, String reference) -> {

                    if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
                        return SecurityAdvisor.SecurityAdvice.ALLOWED;
                    } else {
                        return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
                    }
                };

                Map<String, DecoratedAttachment> previewableAttachments = new HashMap<>();

                try {
                    securityService.pushAdvisor(securityAdvisor);
                    submittedAttachments.forEach(da -> {

                        try {
                            ResourceProperties props = contentHostingService.getProperties(da.get("ref").replaceFirst("\\/content", ""));
                            String previewId = props.getProperty(ContentHostingService.PREVIEW);
                            if (StringUtils.isNotEmpty(previewId)) {
                                previewableAttachments.put(da.get("ref"), new DecoratedAttachment(contentHostingService.getResource(previewId)));
                            }
                        } catch (Exception e) {
                            log.warn("Could not access properties for resource {}, {}", da.get("ref"), e.toString());
                        }
                    });
                } finally {
                    securityService.popAdvisor(securityAdvisor);
                }

                if (!previewableAttachments.isEmpty()) {
                    submission.put("previewableAttachments", previewableAttachments);
                }
            }

            //peer review
            if (assignment.getAllowPeerAssessment()
                    && assignment.getPeerAssessmentStudentReview()
                    && assignmentService.isPeerAssessmentClosed(assignment)) {
                List<PeerAssessmentItem> reviews = assignmentPeerAssessmentService.getPeerAssessmentItems(as.getId(), assignment.getScaleFactor());
                if (reviews != null) {
                    List<SimplePeerAssessmentItem> completedReviews = new ArrayList<>();
                    for (PeerAssessmentItem review : reviews) {
                        if (!review.getRemoved() && (review.getScore() != null || (StringUtils.isNotBlank(review.getComment())))) {
                            //only show peer reviews that have either a score or a comment saved
                            try {
                                if (assignment.getIsGroup()) {
                                    String siteId = assignment.getContext();
                                    Site site = siteService.getSite(siteId);
                                    review.setAssessorDisplayName(site.getGroup(review.getId().getAssessorUserId()).getTitle());
                                } else {
                                    review.setAssessorDisplayName(userDirectoryService.getUser(review.getId().getAssessorUserId()).getDisplayName());
                                }
                            } catch (IdUnusedException | UserNotDefinedException e) {
                                //reviewer doesn't exist or one of userId/groupId/siteId is wrong
                                log.warn("Either no site, or user: {}", e.toString());
                                //set a default one:
                                review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
                            }
                            // get attachments for peer review item
                            List<PeerAssessmentAttachment> attachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(review.getId().getSubmissionId(), review.getId().getAssessorUserId());
                            if (attachments != null && !attachments.isEmpty()) {
                                List<Reference> attachmentRefList = new ArrayList<>();
                                for (PeerAssessmentAttachment attachment : attachments) {
                                    try {
                                        Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getResourceId()));
                                        attachmentRefList.add(ref);
                                    } catch (Exception e) {
                                        log.warn("Exception while creating reference: {}", e.toString());
                                    }
                                }
                                if (!attachmentRefList.isEmpty()) review.setAttachmentRefList(attachmentRefList);
                            }
                            completedReviews.add(new SimplePeerAssessmentItem(review));
                        }
                    }
                    if (!completedReviews.isEmpty()) {
                        submission.put("peerReviews", completedReviews);
                    }
                }
            }
            submission.putAll(getOriginalityProperties(as));
        }

        List<Map<String, Object>> submitters
            = as.getSubmitters().stream().map(ass -> {

                String userId = ass.getSubmitter();
                if (!activeSubmitters.contains(userId)) {
                    return null;
                }

                try {
                    Map<String, Object> submitter = new HashMap<>();
                    submitter.put("id", ass.getSubmitter());

                    if (hydrate) {
                        String grade = assignmentService.getGradeForSubmitter(ass.getSubmission(), ass.getSubmitter());
                        if (StringUtils.isNotBlank(grade)) submitter.put("grade", grade);

                        boolean overridden = assignmentService.isGradeOverridden(ass.getSubmission(), ass.getSubmitter());
                        if (overridden) submitter.put("overridden", overridden);

                        if (StringUtils.isNotBlank(ass.getTimeSpent())) submitter.put("timeSpent", ass.getTimeSpent());
                    }

                    if (!simpleAssignment.isAnonymousGrading()) {
                        User user = userDirectoryService.getUser(ass.getSubmitter());
                        submitter.put("displayName", user.getDisplayName());
                        submitter.put("sortName", user.getSortName());
                        submitter.put("displayId", user.getDisplayId());
                    } else {
                        String displayName = ass.getSubmission().getId() + " " + rb.getString("grading.anonymous.title");
                        submitter.put("displayName", displayName);
                        submitter.put("sortName", displayName);
                    }

                    return submitter;
                } catch (UserNotDefinedException unde) {
                    log.warn("One of the submitters on submission {} is not a valid user. Maybe"
                        + " they have been removed from your SAKAI_USER table?", ass.getId());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

        if (!submitters.isEmpty()) {
            submission.put("submitters", submitters);
        } else {
            return null;
        }

        if (StringUtils.isNotBlank(as.getGroupId())) {
            submission.put("groupId", as.getGroupId());
        }

        if (hydrate) {
            if (as.getUserSubmission()) submission.put("userSubmission", as.getUserSubmission());

            if (as.getReturned()) submission.put("returned", as.getReturned());

            if (StringUtils.isNotBlank(as.getFeedbackText())) submission.put("feedbackText", as.getFeedbackText());

            if (StringUtils.isNotBlank(as.getFeedbackComment())) submission.put("feedbackComment", as.getFeedbackComment());

            if (StringUtils.isNotBlank(as.getPrivateNotes())) submission.put("privateNotes", as.getPrivateNotes());

            List<DecoratedAttachment> feedbackAttachments = as.getFeedbackAttachments().stream().map(ref -> {

                    String id = entityManager.newReference(ref).getId();
                    try {
                        return new DecoratedAttachment(contentHostingService.getResource(id));
                    } catch (Exception e) {
                        log.warn("Attachment {} on submission {} is invalid", id, as.getId());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

            if (!feedbackAttachments.isEmpty()) submission.put("feedbackAttachments", feedbackAttachments);

            String status = assignmentService.getSubmissionStatus(as.getId(), true);
            if (StringUtils.isNotBlank(status)) submission.put("status", status);

            if (!as.getProperties().isEmpty()) submission.put("properties", as.getProperties());
        }

        return submission;
    }

    @EntityCustomAction(action = "fullSubmissions", viewKey = EntityView.VIEW_LIST)
    public ActionReturn getFullSubmissions(EntityView view , Map<String, Object> params) {

        String gradableId = (String) params.get("gradableId");
        String ids = (String) params.get("submissionIds");

        if (StringUtils.isBlank(gradableId) || StringUtils.isBlank(ids)) {
            throw new EntityException("Need gradableId and submissionIds", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Assignment nonFinalAssignment = null;
        try {
            nonFinalAssignment = assignmentService.getAssignment(gradableId);
        } catch (IdUnusedException e) {
            throw new EntityException("No assignment for id", gradableId, HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }

        final Assignment assignment = nonFinalAssignment;

        String assignmentReference
            = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        Map<String, Object> data  = new HashMap<>();

        data.put("assignmentCloseTime", assignment.getCloseDate());

        data.put("groups", assignmentService.getGroupsAllowGradeAssignment(assignmentReference)
            .stream().map(SimpleGroup::new).collect(Collectors.toList()));

        String siteId = assignment.getContext();

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No site found", siteId, e);
        }

        SimpleAssignment simpleAssignment = new SimpleAssignment(assignment, false);

        Set<String> activeSubmitters = site.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);

        data.put("submissions", Arrays.asList(ids.split(",")).stream()
            .map(id-> {

                try {
                    return submissionToMap(activeSubmitters, assignment, simpleAssignment, assignmentService.getSubmission(id), true);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull).collect(Collectors.toList()));

        return new ActionReturn(data);
    }

    @EntityCustomAction(action = "gradable", viewKey = EntityView.VIEW_LIST)
    public ActionReturn getGradableForSite(EntityView view , Map<String, Object> params) {

        getCheckedCurrentUser();

        String gradableId = (String) params.get("gradableId");
        String submissionId = (String) params.get("submissionId");

        if (StringUtils.isBlank(gradableId) || StringUtils.isBlank(submissionId)) {
            throw new EntityException("Need gradableId and submissionId", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Assignment nonFinalAssignment = null;
        try {
            nonFinalAssignment = assignmentService.getAssignment(gradableId);
        } catch (IdUnusedException e) {
            throw new EntityException("No assignment for id", gradableId, HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }

        final Assignment assignment = nonFinalAssignment;

        String siteId = assignment.getContext();

        if (!canGrade(assignment)) {
            throw new EntityException("Forbidden", "", HttpServletResponse.SC_FORBIDDEN);
        }

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No site found", siteId, e);
        }

        SimpleAssignment simpleAssignment = new SimpleAssignment(assignment, false);

        String assignmentReference
                = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();

        List<SimpleGroup> groups = assignmentService.getGroupsAllowGradeAssignment(assignmentReference)
                .stream().map(SimpleGroup::new).sorted((group, otherGroup) -> StringUtils.compare(group.getTitle(), otherGroup.getTitle())).collect(Collectors.toList());

        // Get all users who are allowed to submit.
        List<User> allowAddSubmissionUsers = assignmentService.allowAddSubmissionUsers(assignmentReference);
        Set<String> activeSubmitters = allowAddSubmissionUsers.stream().map(Entity::getId).collect(Collectors.toSet());

        // Get users who are visible to the current user from all groups
        List<User> visibleUsers = assignmentService.getSelectedGroupUsers(ALL, site.getId(), assignment, allowAddSubmissionUsers);
        Set<String> visibleUserIds = visibleUsers.stream().map(Entity::getId).collect(Collectors.toSet());

        // Get sorted submissions visible for the current user
        List<AssignmentSubmission> submissions = (new ArrayList<>(assignment.getSubmissions())).stream()
                // Filter the submissions based on the submission submitter IDs
                .filter(submission -> submission.getSubmitters().stream()
                        .map(AssignmentSubmissionSubmitter::getSubmitter)
                        .anyMatch(visibleUserIds::contains))
                .sorted(new AssignmentSubmissionComparator(assignmentService, siteService, userDirectoryService))
                .collect(Collectors.toList());

        int submissionIndex = -1;
        for (int i = 0; i < submissions.size(); i++) {

            AssignmentSubmission as = submissions.get(i);
            if (submissionId.equals(as.getId())) {
                submissionIndex = i;
                break;
            }
        }

        List<Map<String, Object>> submissionMaps = new ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {

            AssignmentSubmission as = submissions.get(i);

            if (i == submissionIndex
                    || i == (submissionIndex - 1)
                    || i == (submissionIndex + 1)) {
                submissionMaps.add(submissionToMap(activeSubmitters, assignment, simpleAssignment, as, true));
            } else {
                submissionMaps.add(submissionToMap(activeSubmitters, assignment, simpleAssignment, as, false));
            }
        }
        submissionMaps.removeAll(Collections.singleton(null));

        Integer contentKey = assignment.getContentId();
        if (contentKey != null) {
            // Default assignment-wide launch to tool if there is not a SubmissionReview launch in a submission
            simpleAssignment.ltiGradableLaunch = "/access/lti/site/" + siteId + "/content:" + contentKey;

            Map<String, Object> content = ltiService.getContent(contentKey.longValue(), site.getId());
            if (content != null) {
                Long toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
                Map<String, Object> tool = (toolKey != null) ? ltiService.getTool(toolKey, site.getId()) : null;
                simpleAssignment.ltiFrameHeight = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
                String contentItem = StringUtils.trimToEmpty((String) content.get(LTIService.LTI_CONTENTITEM));

                for (Map<String, Object> submission : submissionMaps) {
                    if (!submission.containsKey("userSubmission")) continue;
                    String ltiSubmissionLaunch = null;

                    try {
                        String subId = (String) submission.get("id");
                        if (StringUtils.isNotBlank(subId)) {
                            AssignmentSubmission as = assignmentService.getSubmission(subId);

                            String submitterId = null;
                            Optional<AssignmentSubmissionSubmitter> submittee = assignmentService.getSubmissionSubmittee(as);
                            if (submittee.isPresent()) {
                                submitterId = submittee.get().getSubmitter();
                                log.debug("LTI found submittee: {}", submitterId);
                            }

                            if (StringUtils.isNotBlank(submitterId)) {
                                ltiSubmissionLaunch = "/access/lti/site/" + siteId + "/content:" + contentKey + "?for_user=" + submitterId;

                                // Check for submission review capability
                                if (contentItem.indexOf("\"submissionReview\"") > 0) {
                                    ltiSubmissionLaunch = ltiSubmissionLaunch + "&message_type=content_review";
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Could not get submitter ID for LTI: {}", e.toString());
                    }

                    if (ltiSubmissionLaunch != null && !ltiSubmissionLaunch.isBlank()
                            && submission.get("ltiSubmissionLaunch") == null) {
                        submission.put("ltiSubmissionLaunch", ltiSubmissionLaunch);
                    }
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("submissions", submissionMaps);
        data.put("totalSubmissions", submissionMaps.size());
        data.put("gradable", simpleAssignment);
        data.put("groups", groups);
        data.put("previewMimetypes", contentHostingService.getHtmlForRefMimetypes());
        data.put("showOfficialPhoto", serverConfigurationService.getBoolean("assignment.show.official.photo", true));
        data.put("letterGradeOptions", serverConfigurationService.getString("assignment.letterGradeOptions", "A+,A,A-,B+,B,B-,C+,C,C-,D+,D,D-,E,F"));

        return new ActionReturn(data);
    }

    private Map<String, String> getOriginalityProperties(AssignmentSubmission as) {

        Map<String, String> props = new HashMap<>();

        String originalityServiceName = this.assignmentService.getContentReviewServiceName();
        props.put("originalityServiceName", originalityServiceName);

        int reviewCounting = 1;
        for (ContentReviewResult c : this.assignmentService.getSortedContentReviewResults(as)) {
            //real part; will work on a Turnitin-enabled server
            props.put("originalityLink" + Integer.toString(reviewCounting), c.getReviewReport());
            props.put("originalityIcon" + Integer.toString(reviewCounting), c.getReviewIconCssClass());
            props.put("originalityScore" + Integer.toString(reviewCounting), Integer.toString(c.getReviewScore()));
            props.put("originalityName" + Integer.toString(reviewCounting), c.getContentResource().getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
            props.put("originalityInline" + Integer.toString(reviewCounting), Boolean.valueOf(c.isInline()).toString());
            props.put("originalityStatus" + Integer.toString(reviewCounting), Boolean.valueOf(c.isPending()).toString());
            props.put("originalityError" + Integer.toString(reviewCounting), c.getReviewError());
            reviewCounting++;
        }
        return props;
    }

    @EntityCustomAction(action = "getGrade", viewKey = EntityView.VIEW_LIST)
    public ActionReturn getGrade(Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        String courseId = (String) params.get("courseId");
        String gradableId = (String) params.get("gradableId");
        String studentId = (String) params.get("studentId");
        String submissionId = (String) params.get("submissionId");
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(gradableId)
                || StringUtils.isBlank(studentId) || StringUtils.isBlank(submissionId)) {
            throw new EntityException("You need to supply the courseId, gradableId, studentId and grade", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        AssignmentSubmission submission = null;
        try {
            submission = assignmentService.getSubmission(submissionId);
        } catch (IdUnusedException iue) {
            throw new EntityException("submissionId not found.", "", HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException pe) {
            throw new EntityException("You don't have permissions read submission " + submissionId, "", HttpServletResponse.SC_FORBIDDEN);
        }

        Site site = null;
        try {
            site = siteService.getSite(courseId);
        } catch (IdUnusedException iue) {
            throw new EntityException("The courseId (site id) you supplied is invalid", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Assignment assignment = submission.getAssignment();

        Map<String, Object> retval = new HashMap<>();
        retval.put("id", submission.getId());

        // Return the default representation of a grade if we don't return a formatted version
        // See similar code in submissionToMap which does this in a different order
        if (StringUtils.isNotBlank(submission.getGrade())) {
            retval.put("grade", submission.getGrade());
        }

        if (assignment.getTypeOfGrade() == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
            retval.put("grade", StringUtils.isBlank(submission.getGrade()) ? AssignmentConstants.UNGRADED_GRADE_STRING : submission.getGrade());
        } else if (StringUtils.isNotBlank(submission.getGrade())) {
            retval.put("grade", assignmentService.getGradeDisplay(submission.getGrade(), assignment.getTypeOfGrade(), assignment.getScaleFactor()));
        }

        if (StringUtils.isNotBlank(submission.getFeedbackComment())) retval.put("feedbackComment", submission.getFeedbackComment());

        return new ActionReturn(retval);
    }

    @EntityCustomAction(action = "setGrade", viewKey = EntityView.VIEW_NEW)
    public ActionReturn setGrade(Map<String, Object> params) {
        String userId = getCheckedCurrentUser();

        String courseId = (String) params.get("courseId");
        String gradableId = (String) params.get("gradableId");
        String grade = (String) params.get("grade");
        String studentId = (String) params.get("studentId");
        String submissionId = (String) params.get("submissionId");
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(gradableId)
                || grade == null || StringUtils.isBlank(studentId) || StringUtils.isBlank(submissionId)) {
            throw new EntityException("You need to supply the courseId, gradableId, studentId and grade", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        AssignmentSubmission submission = null;
        try {
            submission = assignmentService.getSubmission(submissionId);
        } catch (IdUnusedException iue) {
            throw new EntityException("submissionId not found.", "", HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException pe) {
            throw new EntityException("You don't have permissions read submission " + submissionId, "", HttpServletResponse.SC_FORBIDDEN);
        }

        Site site = null;
        try {
            site = siteService.getSite(courseId);
        } catch (IdUnusedException iue) {
            throw new EntityException("The courseId (site id) you supplied is invalid", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        String privateNotes = formattedText.escapeHtmlFormattedText((String) params.get("privateNotes"));
        String feedbackText = formattedText.escapeHtmlFormattedText((String) params.get("feedbackText"));
        String feedbackComment = formattedText.escapeHtmlFormattedText((String) params.get("feedbackComment"));

        String gradeOption = (String) params.get("gradeOption");
        gradeOption = StringUtils.isBlank(gradeOption) ? SUBMISSION_OPTION_SAVE : gradeOption;

        String resubmitNumber = (String) params.get("resubmitNumber");
        String resubmitDate = (String) params.get("resubmitDate");
        String extensionDate = (String) params.get("extensionDate");
        List<String> alerts = new ArrayList<>();

        Assignment assignment = submission.getAssignment();

        if (!canGrade(assignment)) {
            throw new EntityException("You don't have permission to set grades", "", HttpServletResponse.SC_FORBIDDEN);
        }

        switch (assignment.getTypeOfGrade()) {
            case SCORE_GRADE_TYPE:
                grade = assignmentToolUtils.scalePointGrade(grade, assignment.getScaleFactor(), alerts);
                break;
            case UNGRADED_GRADE_TYPE:
                grade = ASSN_GRADE_TYPE_NOGRADE_PROP;
                break;
            case PASS_FAIL_GRADE_TYPE:
                if (AssignmentConstants.UNGRADED_GRADE_STRING.equals(grade)) grade = null;
            default:
        }

        Map<String, Object> options = new HashMap<>();
        options.put(GRADE_SUBMISSION_GRADE, grade);

        // check for grade overrides
        if (AssignmentToolUtils.allowGroupOverrides(assignment, assignmentService)) {
            submission.getSubmitters().forEach(s -> {

                String ug = StringUtils.trimToNull((String) params.get(GRADE_SUBMISSION_GRADE + "_" + s.getSubmitter()));
                if (ug != null && !ug.equals(AssignmentConstants.UNGRADED_GRADE_STRING)) {
                    if (assignment.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                        ug = assignmentToolUtils.scalePointGrade(ug, assignment.getScaleFactor(), alerts);
                    }
                    options.put(GRADE_SUBMISSION_GRADE + "_" + s.getSubmitter(), ug);
                }
            });
        }

        options.put(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
        options.put(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
        options.put(GRADE_SUBMISSION_PRIVATE_NOTES, privateNotes);
        options.put(ALLOW_RESUBMIT_NUMBER, resubmitNumber);

        if (StringUtils.isNotBlank(resubmitDate)) {
            options.put(ALLOW_RESUBMIT_CLOSE_EPOCH_MILLIS, resubmitDate);
        }
        if (StringUtils.isNotBlank(extensionDate)){
            options.put(ALLOW_EXTENSION_CLOSE_EPOCH_MILLIS, extensionDate);
        }
        Set<String> attachmentKeys
            = params.keySet().stream().filter(k -> k.startsWith("attachment")).collect(Collectors.toSet());

        final List<Reference> attachmentRefs = attachmentKeys.stream().map(k -> {
            FileItem item = (FileItem) params.get(k);
            try {
                // make a set of properties to add for the new resource
                ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, item.getName());
                props.addProperty(ResourceProperties.PROP_DESCRIPTION, item.getName());
                ContentResource cr = contentHostingService.addAttachmentResource(item.getName(),
                    courseId, "Assignments", item.getContentType(), item.getInputStream(), props);
                return entityManager.newReference(cr.getReference());
            } catch (Exception e) {
                throw new EntityException("Error while storing attachments", "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }).collect(Collectors.toList());

        options.put(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT,  attachmentRefs);

        options.put(GRADE_SUBMISSION_DONT_CLEAR_CURRENT_ATTACHMENTS, Boolean.TRUE);

        // Add any rubrics params
        params.keySet().stream().filter(k -> k.startsWith(RubricsConstants.RBCS_PREFIX)).forEach(k -> options.put(k, params.get(k)));

        options.put("siteId", (String) params.get("siteId"));

        assignmentToolUtils.gradeSubmission(submission, gradeOption, options, alerts);

        Set<String> activeSubmitters = site.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);

        if (submission != null) {
            boolean anonymousGrading = assignmentService.assignmentUsesAnonymousGrading(assignment);
            try {
                return new ActionReturn(Map.of("assignmentCloseTime", assignment.getCloseDate(),
                                                "submission", submissionToMap(activeSubmitters, assignment, new SimpleAssignment(assignment),  submission, true)));
            } catch (Exception e) {
                throw new EntityException("Failed to set grade on " + submissionId, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new EntityException("Failed to set grade on " + submissionId, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @EntityCustomAction(action = "removeFeedbackAttachment", viewKey = EntityView.VIEW_LIST)
    public String removeFeedbackAttachment(Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        String submissionId = (String) params.get("submissionId");
        String ref = (String) params.get("ref");

        if (StringUtils.isBlank(submissionId) || StringUtils.isBlank(ref)) {
            throw new EntityException("You need to supply the submissionId and ref", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        AssignmentSubmission as = null;
        try {
            as = assignmentService.getSubmission(submissionId);
            as.getFeedbackAttachments().remove(ref);
            assignmentService.updateSubmission(as);
        } catch (IdUnusedException iue) {
            throw new EntityException("Invalid submissionId " + submissionId, "", HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException pe) {
            throw new EntityException("You can't modify this submission", "", HttpServletResponse.SC_FORBIDDEN);
        }

        try {
            contentHostingService.removeResource(assignmentService.removeReferencePrefix(ref));
        } catch (Exception e) {
            log.warn("Exception caught while removing resource " + ref + ". It may have been removed previously.");
        }

        return "SUCCESS";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
     * #findEntityRefs(java.lang.String[], java.lang.String[],
     * java.lang.String[], boolean)
     */
    public List<String> findEntityRefs(String[] prefixes, String[] name,
                                       String[] searchValue, boolean exactMatch) {
        String siteId = null;
        String userId = null;
        List<String> rv = new ArrayList<String>();

        if (ENTITY_PREFIX.equals(prefixes[0])) {

            for (int i = 0; i < name.length; i++) {
                if ("context".equalsIgnoreCase(name[i])
                        || "site".equalsIgnoreCase(name[i]))
                    siteId = searchValue[i];
                else if ("user".equalsIgnoreCase(name[i])
                        || "userId".equalsIgnoreCase(name[i]))
                    userId = searchValue[i];
            }

            if (siteId != null) {
                // filter to obtain only grade-able assignments
                for (Assignment assignment : assignmentService.getAssignmentsForContext(siteId)) {
                    if (!assignment.getDraft() && assignmentService.allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference())) {
                        rv.add(Entity.SEPARATOR + ENTITY_PREFIX + Entity.SEPARATOR + assignment.getId());
                    }
                }
            }
        }
        return rv;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
     * #getProperties(java.lang.String)
     */
    public Map<String, String> getProperties(String reference) {
        Map<String, String> props = new HashMap<String, String>();
        String defaultView = "doView_submission";
        String[] refParts = reference.split(Entity.SEPARATOR);
        String submissionId = "null"; // setting to the string null
        String decWrapper = null;
        String decWrapperTag = "";
        String decSiteId = "";
        String decPageId = "";

        String assignmentId = refParts[2];
        if (refParts.length >= 4) {
            defaultView = refParts[3];
            if (refParts.length >= 5) {
                submissionId = refParts[4].replaceAll("_", Entity.SEPARATOR);
            }
            if (refParts.length >= 6) {
                decWrapper = refParts[5].replaceAll("_", Entity.SEPARATOR);
                if (decWrapper != null && !"".equals(decWrapper)) {
                    String[] splitDec = decWrapper.split(Entity.SEPARATOR);
                    if (splitDec.length == 3) {
                        decWrapperTag = splitDec[0];
                        decSiteId = splitDec[1];
                        decPageId = splitDec[2];
                    }
                }
            }
        }

        boolean canUserAccessWizardPageAndLinkedArtifcact = false;
        if (StringUtils.isNotEmpty(decSiteId) && StringUtils.isNotEmpty(decPageId) && !"null".equals(submissionId)) {
            Map<String, Object> params = new HashMap<>();
            params.put("siteId", decSiteId);
            params.put("pageId", decPageId);
            params.put("linkedArtifactId", submissionId);
            ActionReturn ret = entityBroker.executeCustomAction("/matrixcell/"
                            + decPageId, "canUserAccessWizardPageAndLinkedArtifcact",
                    params, null);
            if (ret != null && ret.getEntityData() != null) {
                Object returnData = ret.getEntityData().getData();
                canUserAccessWizardPageAndLinkedArtifcact = (Boolean) returnData;
            }
        }

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            Entity entity = assignmentService.createAssignmentEntity(assignmentId);
            props.put("title", assignment.getTitle());
            props.put("author", assignment.getAuthor());
            props.put("description", entity.getReference());
            props.put("draft", "" + assignment.getDraft());
            props.put("siteId", assignment.getContext());
            props.put("section", assignment.getSection());
            props.put("status", assignmentService.getAssignmentCannonicalStatus(assignmentId).toString());
            props.put("portalURL", entity.getUrl());
            if (assignment.getDateCreated() != null) {
                props.put("created_time", assignment.getDateCreated().toString());
            }
			if (assignment.getModifier() != null) {
				props.put("modified_by", assignment.getModifier());
			}
            if (assignment.getDateModified() != null) {
                props.put("modified_time", assignment.getDateModified().toString());
            }
            props.put("due_time", assignment.getDueDate().toString());
            props.put("open_time", assignment.getOpenDate().toString());
            if (assignment.getDropDeadDate() != null) {
                props.put("retract_time", assignment.getDropDeadDate().toString());
            }

            Site site = siteService.getSite(assignment.getContext());
            String placement = site.getToolForCommonId("sakai.assignment.grades").getId();

            props.put("security.user", sessionManager.getCurrentSessionUserId());
            props.put("security.site.function", SiteService.SITE_VISIT);
            props.put("security.site.ref", site.getReference());
            props.put("security.assignment.function", SECURE_ACCESS_ASSIGNMENT);
            props.put("security.assignment.grade.function", SECURE_GRADE_ASSIGNMENT_SUBMISSION);
            props.put("security.assignment.grade.ref", entity.getReference());
            props.put("url",
                    "/portal/tool/" + placement
                    + "?assignmentId=" + assignment.getId()
                    + "&submissionId=" + submissionId
                    + "&assignmentReference=" + entity.getReference()
                    + "&panel=Main&sakai_action=" + defaultView);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No assignment found", reference, e);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }
        return props;
    }

    // PROPERTY STUFF

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
     * #getPropertyValue(java.lang.String, java.lang.String)
     */
    public String getPropertyValue(String reference, String name) {
        String rv = null;
        // lazy code, if any of the parts of getProperties is found to be slow
        // this should be
        // changed.
        Map<String, String> props = getProperties(reference);
        if (props != null && props.containsKey(name)) {
            rv = props.get(name);
        }
        return rv;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider
     * #setPropertyValue(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setPropertyValue(String reference, String name, String value) {
        // TODO: add ability to set properties of an assignment
    }

    @EntityCustomAction(action ="checkForUsersInMultipleGroups", viewKey = EntityView.VIEW_LIST)
    public List<MultiGroupRecord> checkForUsersInMultipleGroups(final EntityView view, final Map<String, Object> params) {
        final String siteId = StringUtils.trimToEmpty((String) params.get("siteId"));
        final String asnRef = StringUtils.trimToEmpty((String) params.get("asnRef"));

        if (siteId.isEmpty()) {
            throw new IllegalArgumentException("Site Id must be provided.");
        }

        // Permission check to avoid revealing group memberships, user must be able to edit the given assignment,
        // or if none given, add assignments in the site
        if (!asnRef.isEmpty() && !assignmentService.allowUpdateAssignment(asnRef)) {
            throw new SecurityException(new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_UPDATE_ASSIGNMENT, null));
        }
        if (asnRef.isEmpty() && !assignmentService.allowAddAssignment(siteId)) {
            throw new SecurityException(new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_ADD_ASSIGNMENT, null));
        }

        final List<String> groupIds;
        Object groups = params.get("selectedGroups[]");
        if (groups != null && groups instanceof String[]) {
            groupIds = Arrays.asList((String[]) groups);
        } else if (groups != null && groups instanceof String) {
            groupIds = Collections.singletonList((String) groups);
        } else {
            throw new IllegalArgumentException("Selected groups must be provided.");
        }

        try {
            List<Group> selectedGroups = siteService.getSite(siteId).getGroups().stream()
                .filter(g -> groupIds.contains(g.getId())).collect(Collectors.toList());
            return assignmentService.checkAssignmentForUsersInMultipleGroups(siteId, selectedGroups);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Site Id must be provided.");
        }
    }

    @EntityCustomAction(action = "itemSubmission", viewKey = EntityView.VIEW_LIST)
    public List<SimpleSubmission> getItemSubmission(EntityView view, Map<String, Object> params) {

        String userId = getCheckedCurrentUser();

        User u;
        try {
            u = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e2) {
            log.warn("You need to be logged in to get the assignment");
            throw new EntityException("You need to be logged in to get the assignment", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        String assignmentId = view.getPathSegment(2);
        if (StringUtils.isBlank(assignmentId)) {
            log.warn("You need to supply the assignmentId and ref");
            throw new EntityException("Cannot execute custom action (getTimeSheet): Illegal arguments: Must include context and assignmentId in the path (/assignment.json): e.g. /assignment/getTimeSheet/{assignmentId} (rethrown)", assignmentId, HttpServletResponse.SC_BAD_REQUEST);
        }
        Assignment assignment;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException e) {
            log.warn("Assignment not found");
            throw new EntityNotFoundException("Assignment not found", assignmentId, e);
        } catch (PermissionException e1) {
            log.warn("You can't modify this sumbitter");
            throw new EntityException("You don't have permissions read assignment " + assignmentId, "", HttpServletResponse.SC_FORBIDDEN);
        }

        List<SimpleSubmission> rv = new ArrayList<>();

        Site site = null;
        try {
            site = siteService.getSite(assignment.getContext());
        } catch (IdUnusedException e) {
            log.warn("Site not found");
            throw new EntityNotFoundException("No site found", assignment.getContext(), e);
        }
        Set<String> activeSubmitters = site.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);

        if (canGrade(assignment)) {
            rv = assignment.getSubmissions().stream().map(ss -> {
                try {
                    return new SimpleSubmission(ss, new SimpleAssignment(assignment), activeSubmitters);
                } catch (Exception e) {
                    log.error("Exception while creating SimpleSubmission", e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            AssignmentSubmission as;
            try {
                as = assignmentService.getSubmission(assignmentId, u);
            } catch (PermissionException e1) {
                log.warn("You can't modify this sumbitter");
                throw new EntityException("You don't have permissions read submission " + assignmentId, "", HttpServletResponse.SC_FORBIDDEN);
            }

            if (as == null) {
                throw new EntityException("No existe informacion sobre la tarea para el usuario  ", assignmentId, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                try {
                    rv.add(new SimpleSubmission(as, new SimpleAssignment(assignment), activeSubmitters));
                } catch (Exception e) {
                    log.error("Exception while creating SimpleSubmission", e);
                    return null;
                }
            }
        }
        return rv;
    }

    private String getCheckedCurrentUser() throws EntityException {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("No current session user");
            throw new EntityException("Unauthorized", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        return userId;
    }

    private boolean canGrade(Assignment assignment) {

        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        return assignmentService.allowGradeSubmission(reference);
    }

    @Getter
    public class DecoratedAttachment implements Comparable<Object> {

        private String name;
        private String ref;
        private long size;
        private String type;
        private String url;

        public DecoratedAttachment(ContentResource cr) {

            this.url = cr.getUrl();
            this.name = cr.getProperties().getPropertyFormatted(cr.getProperties().getNamePropDisplayName());
            this.ref = cr.getReference();
            this.type = cr.getContentType();
            this.size = cr.getContentLength();
        }

        public int compareTo(Object other) {
            return this.getUrl().compareTo(
                    ((DecoratedAttachment) other).getUrl());
        }
    }

    @Data
    public class SimpleAssignment {
        /**
         * the assignment id
         */
        private String id;

        /**
         * the AssignmentContent of this Assignment.
         */
        private Assignment content;

        /**
         * the first time at which the assignment can be viewed; may be null.
         */
        private Instant openTime;

        /**
         * the first time at which the assignment can be viewed; (String)
         */
        private String openTimeString;

        /**
         * the time at which the assignment is due; may be null.
         */
        private Instant dueTime;

        /**
         * the time at which the assignment is due; (String)
         */
        private String dueTimeString;

        /**
         * the drop dead time after which responses to this assignment are
         * considered late; may be null.
         */
        private Instant dropDeadTime;

        /**
         * the drop dead time after which responses to this assignment are
         * considered late; (String)
         */
        private String dropDeadTimeString;

        /**
         * the close time after which this assignment can no longer be viewed,
         * and after which submissions will not be accepted. May be null.
         */
        private Instant closeTime;

        /**
         * the close time after which this assignment can no longer be viewed,
         * and after which submissions will not be accepted. (String)
         */
        private String closeTimeString;

        /**
         * the section info.
         */
        private String section;

        /**
         * the context at the time of creation.
         */
        private String context;

        /**
         * Get whether this is a draft or final copy.
         */
        private boolean draft;

        /**
         * the creator of this object.
         */
        private String creator;

        /**
         * the time that this object was created.
         */
        private Instant timeCreated;

        /**
         * the list of authors.
         */
        private String author;

        /**
         * the assignment instructions.
         */
        private String instructions;

        /**
         * the time of last modificaiton.
         */
        private Instant timeLastModified;

        /**
         * the author of last modification
         */
        private String authorLastModified;

        /**
         * the title
         */
        private String title;

        /**
         * Return string representation of assignment status
         */
        private String status;

        /**
         * the position order field for the assignment.
         */
        private int position;

        /**
         * the groups defined for this assignment.
         */
        private Collection groups;

        /**
         * the access mode for the assignment - how we compute who has access to
         * the assignment.
         */
        private String access;

        /**
         * the attachment list
         */
        private List<DecoratedAttachment> attachments;

        /**
         * Grade scale description.
         */
        private String gradeScale;

        /**
         * Max points used when grade scale = "Points"
         */
        private String gradeScaleMaxPoints;

        /**
         * Submission type description (e.g. inline only, inline and attachments)
         */
        private String submissionType;

        /**
         * Allow re-submission flag
         */
        private boolean allowResubmission;

        /**
         * Number of allowed re-submissions, using the current
         * function in AssignmentAction assignment_resubmission_option_into_context()
         */
        private String resubmissionNumber = "0";

        /**
         * Honor Pledge is set or not
         */
        private boolean honorPledge;

        /**
         * Supplement items: model answer text
         */
        private String modelAnswerText;

        /**
         * Supplement items: private note text
         */
        private String privateNoteText;


        /**
         * Supplement items: all purpose item text
         */
        private String allPurposeItemText;

        /**
         * the linked gradebook item id and name
         */
        private Long gradebookItemId;
        private String gradebookItemName;

        private boolean anonymousGrading;


        private Boolean estimateRequired;

        private String estimate;

        private Boolean allowPeerAssessment;

        private String maxGradePoint;

        private String ltiGradableLaunch;

        private String ltiFrameHeight;

        private List<SimpleSubmission> submissions;

        public SimpleAssignment() {
        }

        public SimpleAssignment(Assignment a) {
            this(a, true);
        }

        public SimpleAssignment(Assignment a, boolean hydrate) {

            super();

            if (a == null) {
                return;
            }

            Site site = null;
            try {
                site = siteService.getSite(a.getContext());
            } catch (IdUnusedException e) {
                throw new EntityNotFoundException("No site found", a.getContext(), e);
            }

            this.id = a.getId();
            this.openTime = a.getOpenDate();
            this.openTimeString = a.getOpenDate().toString();
            this.dueTime = a.getDueDate();
            this.dueTimeString = a.getDueDate().toString();
            this.dropDeadTime = a.getDropDeadDate();
            this.dropDeadTimeString = a.getDropDeadDate().toString();
            this.closeTime = a.getCloseDate();
            this.closeTimeString = a.getCloseDate().toString();
            this.section = a.getSection();
            this.context = a.getContext();
            this.draft = a.getDraft();
            this.timeCreated = a.getDateCreated();
            this.author = a.getAuthor();
            this.timeLastModified = a.getDateModified();
            this.authorLastModified = a.getModifier();
            this.title = a.getTitle();
            try {
                this.status = assignmentService.getAssignmentCannonicalStatus(a.getId()).toString();
            } catch (IdUnusedException | PermissionException e) {
                log.warn("Couldn't get Assignment status, {}", e.getMessage());
            }
            this.position = (a.getPosition() != null) ? a.getPosition() : 0;
            this.groups = a.getGroups();
            this.access = a.getTypeOfAccess().toString();
            this.instructions = a.getInstructions();
            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                this.maxGradePoint = assignmentService.getMaxPointGradeDisplay(a.getScaleFactor(), a.getMaxGradePoint());
            }

            this.anonymousGrading = assignmentService.assignmentUsesAnonymousGrading(a);
            String gradebookAssignmentProp = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
            if (StringUtils.isNotBlank(gradebookAssignmentProp)) {
                // try to get internal gradebook assignment first
                org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getAssignment(a.getContext(), a.getContext(), gradebookAssignmentProp);
                if (gAssignment != null) {
                    // linked Gradebook item is internal
                    this.gradebookItemId = gAssignment.getId();
                    this.gradebookItemName = gAssignment.getName();
                } else {
                    // If the linked assignment is not internal to Gradebook, try the external assignment service
                    // However, there is no API available in GradebookExternalAssessmentService of getExternalAssignment()
                    // We will first check whether the external assignment is defined, and then get it through GradingService
                    boolean isExternalAssignmentDefined = gradingService.isExternalAssignmentDefined(a.getContext(), gradebookAssignmentProp);
                    if (isExternalAssignmentDefined) {
                        // since the gradebook item is externally defined, the item is named after the external object's title
                        gAssignment = gradingService.getExternalAssignment(a.getContext(), gradebookAssignmentProp);
                        if (gAssignment != null) {
                            this.gradebookItemId = gAssignment.getId();
                            this.gradebookItemName = gAssignment.getName();
                        }
                    }
                }
            } else if (!GRADEBOOK_INTEGRATION_NO.equals(a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK))) {
                log.warn("Assignment has gradebook integration mode '{}' but PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT is null",
                        a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
            }

            this.attachments = a.getAttachments().stream().map(att -> {

                    String id = entityManager.newReference(att).getId();
                    try {
                        return new DecoratedAttachment(contentHostingService.getResource(id));
                    } catch (Exception e) {
                        log.warn("Attachment {} on assignment {} is invalid", id, a.getId());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

            // Translate grade scale from its numeric value to its description.
            this.gradeScale = a.getTypeOfGrade().toString();

            this.honorPledge = a.getHonorPledge();

            // If grade scale is "points" we also capture the maximum points allowed.
            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                Integer scaleFactor = a.getScaleFactor() != null ? a.getScaleFactor() : assignmentService.getScaleFactor();
                this.gradeScaleMaxPoints = assignmentService.getMaxPointGradeDisplay(scaleFactor, a.getMaxGradePoint());
            }

            // Use the number of submissions allowed as an indicator that re-submission is permitted.
            if (a.getProperties().get(ALLOW_RESUBMIT_NUMBER) != null && a.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                Integer allowResubmitNumber = Integer.parseInt(
                    a.getProperties().get(ALLOW_RESUBMIT_NUMBER)
                );

                this.resubmissionNumber =
                    allowResubmitNumber == -1
                    ? rb.getString("allow.resubmit.number.unlimited")
                    : allowResubmitNumber.toString();

                this.allowResubmission = true;
            }
            this.submissionType = a.getTypeOfSubmission().toString();

            // Supplement Items
            AssignmentModelAnswerItem assignmentModelAnswerItem = assignmentSupplementItemService.getModelAnswer(a.getId());
            if (assignmentModelAnswerItem != null) {
                this.modelAnswerText = assignmentModelAnswerItem.getText();
            }
            AssignmentNoteItem assignmentNoteItem = assignmentSupplementItemService.getNoteItem(a.getId());
            if (assignmentNoteItem != null) {
                this.privateNoteText = assignmentNoteItem.getNote();
            }
            AssignmentAllPurposeItem assignmentAllPurposeItem = assignmentSupplementItemService.getAllPurposeItem(a.getId());
            if (assignmentAllPurposeItem != null) {
                this.allPurposeItemText = assignmentAllPurposeItem.getText();
            }
            this.estimateRequired = assignmentService.isTimeSheetEnabled(a.getContext()) && a.getEstimateRequired();
            this.estimate = a.getEstimate();

            this.allowPeerAssessment = a.getAllowPeerAssessment();

            Set<String> activeSubmitters = site.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);

            if (hydrate) {
                if (canGrade(a) && a.getSubmissions().stream().findAny().isPresent()) {
                    this.submissions = new ArrayList<>();
                    this.submissions = a.getSubmissions().stream().map(ss -> {
                        try {
                            return new SimpleSubmission(ss, this, activeSubmitters);
                        } catch (Exception e) {
                            log.error("Exception while creating SimpleSubmission", e);
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList());
                } else {
                    AssignmentSubmission as = null;
                    try {
                        as = assignmentService.getSubmission(a.getId(), userDirectoryService.getCurrentUser());
                    } catch (PermissionException e1) {
                        log.warn("You can't modify this sumbitter");
                    }

                    if (as != null) {
                        try {
                            this.submissions = new ArrayList<>();
                            this.submissions.add(new SimpleSubmission(as, this, activeSubmitters));
                        } catch (Exception e) {
                            log.error("Exception while creating SimpleSubmission", e);
                        }
                    }
                }
            }
        }
    }

    @Data
    public class SimpleSubmitter {

        private String id;
        private String displayName;
        private String sortName;
        private String displayId;
        private String grade;
        private boolean overridden;
        private String timeSpent;

        public SimpleSubmitter(AssignmentSubmissionSubmitter ass, boolean anonymousGrading) throws UserNotDefinedException {

            super();

            this.id = ass.getSubmitter();
            this.grade = assignmentService.getGradeForSubmitter(ass.getSubmission(), id);
            this.overridden = assignmentService.isGradeOverridden(ass.getSubmission(), id);
            this.timeSpent = ass.getTimeSpent();

            if (!anonymousGrading) {
                User user = userDirectoryService.getUser(this.id);
                this.displayName = user.getDisplayName();
                this.sortName = user.getSortName();
                this.displayId = user.getDisplayId();
            } else {
                this.displayName = ass.getSubmission().getId() + " " + rb.getString("grading.anonymous.title");
                this.sortName = this.displayName;
            }
        }
    }

    @Data
    public class SimpleSubmission {

        private String id;
        private String gradableId;
        private String submittedText;
        private String dateSubmitted;
        private Long dateSubmittedEpochSeconds;
        private Boolean submitted;
        private List<DecoratedAttachment> submittedAttachments;
        private Map<String, DecoratedAttachment> previewableAttachments = new HashMap<>();
        private List<SimpleSubmitter> submitters;
        private Boolean userSubmission;
        private Boolean late;
        private Boolean graded;
        private Boolean returned;
        private String feedbackText;
        private String feedbackComment;
        private String privateNotes;
        private String groupId;
        private String status;
        private String grade;
        private List<DecoratedAttachment> feedbackAttachments;
        private Map<String, String> properties = new HashMap<>();
        private Instant assignmentCloseTime;
        private boolean draft;
        private boolean visible;
        public String ltiSubmissionLaunch = null;
        private boolean canSubmit;

        public SimpleSubmission(AssignmentSubmission as, SimpleAssignment sa, Set<String> activeSubmitters) throws Exception {

            super();

            this.id = as.getId();
            this.gradableId = as.getAssignment().getId();
            this.assignmentCloseTime = sa.getCloseTime();
            this.draft = assignmentToolUtils.isDraftSubmission(as);
            this.submitted = as.getSubmitted();

            Instant due = sa.getDueTime();
            Instant close = sa.getCloseTime();
            this.visible = Instant.now().isAfter(Optional.ofNullable(due).orElse(Instant.now()))
                && Instant.now().isAfter(Optional.ofNullable(close).orElse(Instant.now()));

            String currentUserId = userDirectoryService.getCurrentUser().getId();
            boolean isCurrentUserSubmitter = as.getSubmitters().stream()
                .anyMatch(ass -> currentUserId.equals(ass.getSubmitter()));

            if (this.submitted || (this.draft && this.visible) || isCurrentUserSubmitter) {
                this.submittedText = as.getSubmittedText();
                if (this.submitted) {
                    Instant dateSubmitted = as.getDateSubmitted();
                    if (dateSubmitted != null) {
                        this.dateSubmitted = userTimeService.dateTimeFormat(dateSubmitted, null, null);
                        this.dateSubmittedEpochSeconds = dateSubmitted.getEpochSecond();
                    }
                }
                if (as.getDateSubmitted() != null) {
                    this.late = as.getDateSubmitted().compareTo(as.getAssignment().getDueDate()) > 0;
                }

                this.submittedAttachments = as.getAttachments().stream().map(ref -> {
                    String id = entityManager.newReference(ref).getId();
                    try {
                        return new DecoratedAttachment(contentHostingService.getResource(id));
                    } catch (Exception e) {
                        log.warn("Failed to load attachment [{}] for submission [{}], {}", id, as.getId(), e.toString());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

                SecurityAdvisor securityAdvisor = (String userId, String function, String reference) -> {

                    if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
                        return SecurityAdvisor.SecurityAdvice.ALLOWED;
                    } else {
                        return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
                    }
                };

                try {
                    securityService.pushAdvisor(securityAdvisor);
                    this.submittedAttachments.forEach(da -> {

                        try {
                            ResourceProperties props = contentHostingService.getProperties(da.getRef().replaceFirst("\\/content", ""));
                            String previewId = props.getProperty(ContentHostingService.PREVIEW);
                            if (StringUtils.isNotEmpty(previewId)) {
                                previewableAttachments.put(da.getRef(), new DecoratedAttachment(contentHostingService.getResource(previewId)));
                            }
                        } catch (Exception e) {
                            log.warn("Could not access properties for resource {}, {}", da.getRef(), e.toString());
                        }
                    });
                } finally {
                    securityService.popAdvisor(securityAdvisor);
                }
            }

            this.submitters
                = as.getSubmitters().stream().map(ass -> {

                    String userId = ass.getSubmitter();
                    if (!activeSubmitters.contains(userId)) {
                        return null;
                    }

                    try {
                        return new SimpleSubmitter(ass, sa.isAnonymousGrading());
                    } catch (UserNotDefinedException unde) {
                        log.warn("One of the submitters on submission {} is not a valid user. Maybe"
                            + " they have been removed from your SAKAI_USER table?", ass.getId());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

            if (this.submitters.isEmpty()) {
                throw new Exception("No submitters for this submission: " + as.getId());
            }

            this.canSubmit = assignmentService.canSubmit(as.getAssignment(), this.submitters.stream().findAny().get().getId());

            this.groupId = as.getGroupId();
            this.userSubmission = as.getUserSubmission();
            this.returned = as.getReturned();
            this.feedbackText = as.getFeedbackText();
            this.feedbackComment = as.getFeedbackComment();
            this.privateNotes = as.getPrivateNotes();
            this.feedbackAttachments = as.getFeedbackAttachments().stream().map(ref -> {

                    String id = entityManager.newReference(ref).getId();
                    try {
                        return new DecoratedAttachment(contentHostingService.getResource(id));
                    } catch (Exception e) {
                        log.warn("Attachment {} on submission {} is invalid", id, as.getId());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            this.status = assignmentService.getSubmissionStatus(id, true);
            this.graded = as.getGraded();
            this.grade = assignmentService.getGradeForSubmitter(as, as.getSubmitters().isEmpty() ? null : as.getSubmitters().stream().findAny().get().getSubmitter());
            this.properties.putAll(as.getProperties());
        }
    }

    @Data
    public class SimpleGroup {

        private String id;
        private String reference;
        private String title;
        private Set<String> users;

        public SimpleGroup(Group g) {

            super();

            this.id = g.getId();
            this.reference = g.getReference();
            this.title = g.getTitle();
            this.users = g.getUsers();
        }
    }

    @Getter
    public class SimplePeerAssessmentItem {

        private String assessorUserId;
        private String submissionId;
        private String assignmentId;
        private Integer score;
        private String scoreDisplay;
        private String comment;
        private Boolean removed;
        private Boolean submitted;
        private List<String> attachmentUrlList;
        private String assessorDisplayName;
        private Integer scaledFactor;
        private boolean draft;

        public SimplePeerAssessmentItem(PeerAssessmentItem item) {
            this.assessorUserId = item.getId().getAssessorUserId();
            this.submissionId = item.getId().getSubmissionId();
            this.assignmentId = item.getAssignmentId();
            this.score = item.getScore();
            this.scoreDisplay = item.getScoreDisplay();
            this.comment = item.getComment();
            this.removed = item.getRemoved();
            this.submitted = item.getSubmitted();
            this.assessorDisplayName = item.getAssessorDisplayName();
            this.scaledFactor = item.getScaledFactor();
            this.draft = item.isDraft();

            this.attachmentUrlList = Optional.ofNullable(item.getAttachmentRefList())
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(Reference::getUrl)
                    .collect(Collectors.toList());
        }
    }
}
