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

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.MultiGroupRecord;
import org.sakaiproject.assignment.tool.AssignmentToolUtils;
import org.sakaiproject.assignment.api.model.*;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
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
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
@Setter
public class AssignmentEntityProvider extends AbstractEntityProvider implements EntityProvider,
        CoreEntityProvider, Resolvable, ActionsExecutable, Describeable,
        AutoRegisterEntityProvider, PropertyProvideable, Outputable, Inputable {

    public final static String ENTITY_PREFIX = "assignment";

    private static ResourceLoader rb = new ResourceLoader("assignment");

    private AssignmentService assignmentService;
    private AssignmentToolUtils assignmentToolUtils;
    private ContentHostingService contentHostingService;
    private EntityBroker entityBroker;
    private EntityManager entityManager;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private AssignmentSupplementItemService assignmentSupplementItemService;
    private GradebookService gradebookService;
    private GradebookExternalAssessmentService gradebookExternalService;
    private ServerConfigurationService serverConfigurationService;
    private UserDirectoryService userDirectoryService;
    private UserTimeService userTimeService;
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
            assignment = new SimpleAssignment(assignmentService.getAssignment(ref.getId()));
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

        assignmentService.getAssignmentsForContext(siteId).stream().map(SimpleAssignment::new).forEach(rv::add);
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
        return new SimpleAssignment(a);
    }

    @Getter
    public class GraderUser {

        private String displayName;
        private String sortName;
        private String id;

        public GraderUser(User sakaiUser) {

            super();

            this.displayName = sakaiUser.getDisplayName();
            this.sortName = sakaiUser.getSortName();
            this.id = sakaiUser.getId();
        }
    }

    @EntityCustomAction(action = "gradable", viewKey = EntityView.VIEW_LIST)
    public ActionReturn getGradableForSite(EntityView view , Map<String, Object> params) {

        String gradableId = (String) params.get("gradableId");

        if (StringUtils.isBlank(gradableId)) {
            throw new EntityException("Need gradableId", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Assignment assignment = null;
        try {
            assignment = assignmentService.getAssignment(gradableId);
        } catch (IdUnusedException e) {
            throw new EntityException("No assignment for id", gradableId, HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException e) {
            throw new SecurityException(e);
        }

        String siteId = assignment.getContext();

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new EntityNotFoundException("No site found", siteId, e);
        }

        SimpleAssignment simpleAssignment = new SimpleAssignment(assignment);

        Set<String> activeSubmitters = site.getUsersIsAllowed(SECURE_ADD_ASSIGNMENT_SUBMISSION);

        // A list of mappings of submission id to student id list
        List<SimpleSubmission> submissions
            = assignment.getSubmissions().stream().map(as -> {
                try {
                    return new SimpleSubmission(as, simpleAssignment, activeSubmitters);
                } catch (Exception e) {
                    log.error("Exception while creating SimpleSubmission", e);
                    // This can happen if there are no submitters.
                    return null;
                }

                }).filter(Objects::nonNull).collect(Collectors.toList());

        Integer contentKey = assignment.getContentId();
        if ( contentKey != null ) {
            // Fall back launch for SimpleAssignments without any user-submission
            simpleAssignment.ltiGradableLaunch = "/access/basiclti/site/" + siteId + "/content:" + contentKey;
            Map<String, Object> content = ltiService.getContent(contentKey.longValue(), site.getId());
            String contentItem = StringUtils.trimToEmpty((String) content.get(LTIService.LTI_CONTENTITEM));

            for (SimpleSubmission submission : submissions) {
                if ( ! submission.userSubmission ) continue;
				String ltiSubmissionLaunch = null;
                for(SimpleSubmitter submitter: submission.submitters) {
                    if ( submitter.id != null ) {
                        ltiSubmissionLaunch = "/access/basiclti/site/" + siteId + "/content:" + contentKey + "?for_user=" + submitter.id;

                        // Instead of parsing, the JSON we just look for a simple existance of the submission review entry
                        // Delegate the complex understanding of the launch to SakaiBLTIUtil
                        if ( contentItem.indexOf("\"submissionReview\"") > 0 ) {
                            ltiSubmissionLaunch = ltiSubmissionLaunch + "&message_type=content_review";
                        }
                    }
                }
                submission.ltiSubmissionLaunch = ltiSubmissionLaunch;
            }
        }

        List<SimpleGroup> groups = site.getGroups().stream().map(SimpleGroup::new).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("gradable", simpleAssignment);
        data.put("submissions", submissions);
        data.put("groups", groups);
        data.put("showOfficialPhoto", serverConfigurationService.getBoolean("assignment.show.official.photo", true));
        String lOptions = serverConfigurationService.getString("assignment.letterGradeOptions", "A+,A,A-,B+,B,B-,C+,C,C-,D+,D,D-,E,F");
        data.put("letterGradeOptions", lOptions);

        return new ActionReturn(data);
    }

    @EntityCustomAction(action = "grades", viewKey = EntityView.VIEW_LIST)
    public ActionReturn getGrades(Map<String, Object> params) {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("getGrades attempt when not logged in");
            throw new EntityException("You need to be logged in to get grades", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        String courseId = (String) params.get("courseId");
        String gradableId = (String) params.get("gradableId");

        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(gradableId)) {
            throw new EntityException("You need to supply a courseId and a gradableId", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Site site = null;
        try {
            site = siteService.getSite(courseId);
        } catch (IdUnusedException iue) {
            throw new EntityException("The courseId (site id) you supplied is invalid", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        if (!securityService.unlock(userId, SECURE_GRADE_ASSIGNMENT_SUBMISSION, "/site/" + courseId)) {
            throw new EntityException("You don't have permission to get grades", "", HttpServletResponse.SC_FORBIDDEN);
        }

        Assignment assignment;

        try {
            assignment = assignmentService.getAssignment(gradableId);
        } catch (IdUnusedException idue) {
            throw new EntityException("No gradable for id " + gradableId, "", HttpServletResponse.SC_BAD_REQUEST);
        } catch (PermissionException pe) {
            throw new EntityException("You don't have permission to read the assignment", "", HttpServletResponse.SC_FORBIDDEN);
        }

        // A map of submissionId -> grade
        Map<String, String> grades = assignment.getSubmissions().stream().collect(Collectors.toMap(s -> s.getId(), s -> {

            Set<AssignmentSubmissionSubmitter> submitters = s.getSubmitters();

            if (submitters.size() > 0) {
                if (assignment.getTypeOfGrade() == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
                    return s.getGrade() == null ? "ungraded" : s.getGrade();
                } else {
                    return assignmentService.getGradeDisplay(s.getGrade(), assignment.getTypeOfGrade(), assignment.getScaleFactor());
                }
            } else {
                return "";
            }
        }));

        Map<String, Object> data = new HashMap<>();
        data.put("grades", grades);

        return new ActionReturn(data);
    }

    @EntityCustomAction(action = "setGrade", viewKey = EntityView.VIEW_NEW)
    public ActionReturn setGrade(Map<String, Object> params) {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("setGrade attempt when not logged in");
            throw new EntityException("You need to be logged in to set grades", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

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

        String privateNotes = (String) params.get("privateNotes");
        String feedbackText = (String) params.get("feedbackText");
        String feedbackComment = (String) params.get("feedbackComment");

        String gradeOption = (String) params.get("gradeOption");
        gradeOption = StringUtils.isBlank(gradeOption) ? SUBMISSION_OPTION_SAVE : gradeOption;

        String resubmitNumber = (String) params.get("resubmitNumber");
        String resubmitDate = (String) params.get("resubmitDate");

        List<String> alerts = new ArrayList<>();

        Assignment assignment = submission.getAssignment();

        if (assignment.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
            grade = assignmentToolUtils.scalePointGrade(grade, assignment.getScaleFactor(), alerts);
        } else if (assignment.getTypeOfGrade() == Assignment.GradeType.PASS_FAIL_GRADE_TYPE && grade.equals("ungraded")) {
            grade = null;
        }

        Map<String, Object> options = new HashMap<>();
        options.put(GRADE_SUBMISSION_GRADE, grade);
        options.put(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
        options.put(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
        options.put(GRADE_SUBMISSION_PRIVATE_NOTES, privateNotes);
        options.put(WITH_GRADES, true);
        options.put(ALLOW_RESUBMIT_NUMBER, resubmitNumber);

        if (!StringUtils.isBlank(resubmitDate)) {
            options.put(ALLOW_RESUBMIT_CLOSE_EPOCH_MILLIS, resubmitDate);
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
                return new ActionReturn(new SimpleSubmission(submission, new SimpleAssignment(assignment), activeSubmitters));
            } catch (Exception e) {
                throw new EntityException("Failed to set grade on " + submissionId, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new EntityException("Failed to set grade on " + submissionId, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @EntityCustomAction(action = "removeFeedbackAttachment", viewKey = EntityView.VIEW_LIST)
    public String removeFeedbackAttachment(Map<String, Object> params) {

        String userId = sessionManager.getCurrentSessionUserId();

        if (StringUtils.isBlank(userId)) {
            log.warn("removeFeedbackAttachment attempt when not logged in");
            throw new EntityException("You need to be logged in to remove feedback attachments", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

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
        if (!"".equals(decSiteId) && !"".equals(decPageId)
                && !"null".equals(submissionId)) {
            Map<String, Object> params = new HashMap<String, Object>();
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
            Entity entity = assignmentService.createAssignmentEntity(assignment);
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

        private Boolean allowPeerAssessment;

        private String maxGradePoint;

        private String ltiGradableLaunch;

        public SimpleAssignment() {
        }

        public SimpleAssignment(Assignment a) {

            super();

            if (a == null) {
                return;
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
            if (gradebookService.isGradebookDefined(a.getContext())) {
                if (StringUtils.isNotBlank(gradebookAssignmentProp)) {
                    // try to get internal gradebook assignment first
                    org.sakaiproject.service.gradebook.shared.Assignment gAssignment = gradebookService.getAssignment(a.getContext(), gradebookAssignmentProp);
                    if (gAssignment != null) {
                        // linked Gradebook item is internal
                        this.gradebookItemId = gAssignment.getId();
                        this.gradebookItemName = gAssignment.getName();
                    } else {
                        // If the linked assignment is not internal to Gradebook, try the external assignment service
                        // However, there is no API available in GradebookExternalAssessmentService of getExternalAssignment()
                        // We will first check whether the external assignment is defined, and then get it through GradebookService
                        boolean isExternalAssignmentDefined = gradebookExternalService.isExternalAssignmentDefined(a.getContext(), gradebookAssignmentProp);
                        if (isExternalAssignmentDefined) {
                            // since the gradebook item is externally defined, the item is named after the external object's title
                            gAssignment = gradebookService.getAssignment(a.getContext(), a.getTitle());
                            if (gAssignment != null) {
                                this.gradebookItemId = gAssignment.getId();
                                this.gradebookItemName = gAssignment.getName();
                            }
                        }
                    }
                } else {
                    log.warn("The property \"prop_new_assignment_add_to_gradebook\" is null for the assignment feed");
                }
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

            // If grade scale is "points" we also capture the maximum points allowed.
            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                Integer scaleFactor = a.getScaleFactor() != null ? a.getScaleFactor() : assignmentService.getScaleFactor();
                this.gradeScaleMaxPoints = assignmentService.getMaxPointGradeDisplay(scaleFactor, a.getMaxGradePoint());
            }

            // Use the number of submissions allowed as an indicator that re-submission is permitted.
            if (a.getProperties().get(ALLOW_RESUBMIT_NUMBER) != null && a.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
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

            this.allowPeerAssessment = a.getAllowPeerAssessment();
        }
    }

    @Data
    public class SimpleSubmitter {

        private String id;
        private String displayName;
        private String sortName;

        public SimpleSubmitter(AssignmentSubmissionSubmitter ass, boolean anonymousGrading) throws UserNotDefinedException {

            super();

            this.id = ass.getSubmitter();
            if (!anonymousGrading) {
                User user = userDirectoryService.getUser(this.id);
                this.displayName = user.getDisplayName();
                this.sortName = user.getSortName();
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
        private List<DecoratedAttachment> feedbackAttachments;
        private Map<String, String> properties = new HashMap<>();
        private Instant assignmentCloseTime;
        private boolean draft;
        private boolean visible;
        public String ltiSubmissionLaunch = null;

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
            if (this.submitted || (this.draft && this.visible)) {
                this.submittedText = as.getSubmittedText();
                if (this.submitted) {
                    this.dateSubmitted
                        = userTimeService.dateTimeFormat(as.getDateSubmitted(), null, null);
                }
                if (as.getDateSubmitted() != null) {
                    this.late = as.getDateSubmitted().compareTo(as.getAssignment().getDueDate()) > 0;
                }

                this.submittedAttachments = as.getAttachments().stream().map(ref -> {

                        String id = entityManager.newReference(ref).getId();
                        try {
                            return new DecoratedAttachment(contentHostingService.getResource(id));
                        } catch (Exception e) {
                            log.info("There was an attachment on submission {} that was invalid", as.getId());
                            return null;
                        }
                    }).collect(Collectors.toList());

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
                throw new Exception("No submitters for this submission");
            }
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
            this.graded = as.getGraded();
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
}
