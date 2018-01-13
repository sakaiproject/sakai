/**
 * Copyright 2013 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.gradebook.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.gradebook.entity.Category;
import org.sakaiproject.gradebook.entity.Course;
import org.sakaiproject.gradebook.entity.Gradebook;
import org.sakaiproject.gradebook.entity.GradebookItem;
import org.sakaiproject.gradebook.entity.GradebookItemScore;
import org.sakaiproject.gradebook.entity.SparseGradebookItem;
import org.sakaiproject.gradebook.entity.Student;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * This is the common parts of the logic which is external to our app logic, this provides isolation
 * of the Sakai system from the app so that the integration can be adjusted for future versions or
 * even other systems without requiring rewriting large parts of the code
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class ExternalLogic {

    public final static String EXTERNAL_DATASOURCE = "GB_REST";
    public final static boolean EXTERNAL_ONLY = true;

    public String serverId = "UNKNOWN_SERVER_ID";
    public final static String NO_LOCATION = "noLocationAvailable";

    protected AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }

    protected GradebookExternalAssessmentService gradebookExternalAssessmentService;
    public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService gradebookExternalAssessmentService) {
        this.gradebookExternalAssessmentService = gradebookExternalAssessmentService;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void init() {
        log.info("INIT");
    }

    /**
     * @return the current location id of the current user
     */
    public String getCurrentLocationId() {
        String location = null;
        try {
            String context = toolManager.getCurrentPlacement().getContext();
            location = context;
            // Site s = siteService.getSite( context );
            // location = s.getReference(); // get the entity reference to the site
        } catch (Exception e) {
            // sakai failed to get us a location so we can assume we are not inside the portal
            return NO_LOCATION;
        }
        if (location == null) {
            location = NO_LOCATION;
        }
        return location;
    }

    /**
     * @param locationId
     *            a unique id which represents the current location of the user (entity reference)
     * @return the title for the context or "--------" (8 hyphens) if none found
     */
    public String getLocationTitle(String locationId) {
        String title = null;
        try {
            Site site = siteService.getSite(locationId);
            title = site.getTitle();
        } catch (IdUnusedException e) {
            log.warn("Cannot get the info about locationId: " + locationId);
            title = "----------";
        }
        return title;
    }

    /**
     * Get the user id from the user login name
     * @param loginname the eid for the user
     * @return the id IF the user exists OR null if they do not
     */
    public String getUserIdFromLoginName(String loginname) {
        String userId;
        try {
            User u = userDirectoryService.getUserByEid(loginname);
            userId = u.getId();
        } catch (UserNotDefinedException e) {
            userId = null;
        }
        return userId;
    }

    /**
     * Validate the session id given and optionally make it the current one
     * @param sessionId a sakai session id
     * @param makeCurrent if true and the session id is valid then it is made the current one
     * @return true if the session id is valid OR false if not
     */
    public boolean validateSessionId(String sessionId, boolean makeCurrent) {
        try {
            // this also protects us from null pointer where session service is not set or working
            Session s = sessionManager.getSession(sessionId);
            if (s != null && s.getUserId() != null) {
                if (makeCurrent) {
                    s.setActive();
                    sessionManager.setCurrentSession(s);
                    authzGroupService.refreshUser(s.getUserId());
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failure attempting to set sakai session id ("+sessionId+"): " + e.getMessage());
        }
        return true;
    }

    /**
     * @return the current sakai user session id OR null if none
     */
    public String getCurrentSessionId() {
        String sessionId = null;
        Session s = sessionManager.getCurrentSession();
        if (s != null) {
            sessionId = s.getId();
        }
        return sessionId;
    }

    /**
     * @return the current sakai user id (not username)
     */
    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    /**
     * @return the current Locale as Sakai understands it
     */
    public Locale getCurrentLocale() {
        return new ResourceLoader().getLocale();
    }

    /**
     * Get the display name for a user by their unique id
     * 
     * @param userId
     *            the current sakai user id (not username)
     * @return display name (probably firstname lastname) or "----------" (10 hyphens) if none found
     */
    public String getUserDisplayName(String userId) {
        String name = null;
        try {
            name = userDirectoryService.getUser(userId).getDisplayName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get user displayname for id: " + userId);
            name = "--------";
        }
        return name;
    }

    /**
     * Get a user by their unique id
     * @param userId user id
     * @return the populated User or null if none found
     */
    public org.sakaiproject.gradebook.entity.User getUser(String userId) {
        org.sakaiproject.gradebook.entity.User user = null;
        User u = null;
        try {
            u = userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            try {
                u = userDirectoryService.getUserByEid(userId);
            } catch (UserNotDefinedException e1) {
                log.warn("Cannot get user for id: " + userId);
            }
        }
        if (u != null) {
            user = new org.sakaiproject.gradebook.entity.User(u.getId(),
                    u.getEid(), u.getDisplayName(), u.getSortName(), u.getEmail());
            user.fname = u.getFirstName();
            user.lname = u.getLastName();
        }
        return user;
    }

    /**
     * @return the system email address or null if none available
     */
    public String getNotificationEmail() {
        // attempt to get the email address, if it is not there then we will not send an email
        String emailAddr = serverConfigurationService.getString("portal.error.email",
                serverConfigurationService.getString("mail.support") );
        if ("".equals(emailAddr)) {
            emailAddr = null;
        }
        return emailAddr;
    }

    /**
     * Check if this user has super admin access
     * 
     * @param userId
     *            the internal user id (not username)
     * @return true if the user has admin access, false otherwise
     */
    public boolean isUserAdmin(String userId) {
        return securityService.isSuperUser(userId);
    }

    /**
     * Check if a user has a specified permission within a context, primarily a convenience method
     * and passthrough
     * 
     * @param userId
     *            the internal user id (not username)
     * @param permission
     *            a permission string constant
     * @param locationId
     *            a unique id which represents the current location of the user (entity reference)
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
        if (securityService.unlock(userId, permission, locationId)) {
            return true;
        }
        return false;
    }

    /**
     * Get all the courses for the current user, note that this needs to be limited from
     * outside this method for security
     * 
     * @param siteId
     *            [OPTIONAL] limit the return to just this one site
     * @return the sites (up to 100 of them) which the user has instructor access in
     */
    public List<Course> getCoursesForInstructor(String siteId) {
        List<Course> courses = new Vector<Course>();
        if (siteId == null || "".equals(siteId)) {
            List<Site> sites = getInstructorSites();
            for (Site site : sites) {
                courses.add( makeCourseFromSite(site) );
            }
        } else {
            // return a single site and enrollments
            if (siteService.siteExists(siteId)) {
                if (siteService.allowUpdateSite(siteId) || siteService.allowViewRoster(siteId)) {
                    Site site;
                    try {
                        site = siteService.getSite(siteId);
                        Course c = makeCourseFromSite(site);
                        courses.add(c);
                    } catch (IdUnusedException e) {
                        site = null;
                    }
                }
            }
        }
        return courses;
    }

    @SuppressWarnings("deprecation")
    private Course makeCourseFromSite(Site site) {
        long createdTime = System.currentTimeMillis() / 1000;
        if (site.getCreatedTime() != null) {
            createdTime = site.getCreatedTime().getTime() / 1000;
        }
        Course c = new Course(site.getId(), 
                site.getTitle(), 
                site.getShortDescription(), 
                createdTime, 
                site.isPublished() );
        return c;
    }

    private List<Site> getInstructorSites() {
        // return a max of 100 sites
        List<Site> instSites = new ArrayList<Site>();
        List<Site> sites = siteService.getSites(SelectionType.UPDATE, null, null, null,
                SortType.TITLE_ASC, new PagingPosition(1, 100));
        for (Site site : sites) {
            // filter out admin sites
            String sid = site.getId();
            if (sid.startsWith("!") || sid.endsWith("Admin") || sid.equals("mercury")) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping site (" + sid + ") for current user in instructor courses");
                }
                continue;
            }
            instSites.add(site);
        }
        return instSites;
    }

    /**
     * Get the listing of students from the site gradebook, uses GB security so safe to call
     * 
     * @param siteId
     *            the id of the site to get students from
     * @return the list of Students
     */
    public List<Student> getStudentsForCourse(String siteId) {
        List<Student> students = new ArrayList<Student>();
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e1) {
            throw new IllegalArgumentException("No course found with id (" + siteId + ")");
        }
        // Get students grades for course. This method handles grade overrides.
        Map courseGradeMap = gradebookService.getImportCourseGrade(siteId, false);
        
        String siteRef = site.getReference();
        // use the method gradebook uses internally
        List<User> studentUsers = securityService.unlockUsers("section.role.student", siteRef);
        for (User user : studentUsers) {
            Student s = new Student(user.getId(), user.getEid(), user.getDisplayName(), user.getSortName(), user.getEmail());
            s.fname = user.getFirstName();
            s.lname = user.getLastName();
            if (courseGradeMap!= null && courseGradeMap.containsKey(s.username)){
            	s.courseGrade = (String)courseGradeMap.get(s.username); // Pull student course grade from the map.
            }
            students.add(s);
        }
        /*** this only works in the post-2.5 gradebook -AZ
        // Let the gradebook tell use how it defines the students The gradebookUID is the siteId
        String gbID = siteId;
        if (!gradebookService.isGradebookDefined(gbID)) {
            throw new IllegalArgumentException("No gradebook found for course (" + siteId
                    + "), gradebook must be installed in each course to use with this");
        }
        Map<String, String> studentToPoints = gradebookService.getImportCourseGrade(gbID);
        ArrayList<String> eids = new ArrayList<String>(studentToPoints.keySet());
        Collections.sort(eids);
        for (String eid : eids) {
            try {
                User user = userDirectoryService.getUserByEid(eid);
                students.add(new Student(user.getId(), user.getEid(), user.getDisplayName()));
            } catch (UserNotDefinedException e) {
                log.warn("Undefined user eid (" + eid + ") from site/gb (" + siteId + "): " + e);
            }
        }
         ***/
        return students;
    }
    /**
     * 
     * Get the category information 
     * @param gbID
     * @return
     */
    
    public List<Category> getCategoriesForCourse(String gbID) {
    	List<Category> categories = new ArrayList<Category>();
        for (CategoryDefinition categoryDefinition : gradebookService.getCategoryDefinitions(gbID)) {
        	List<SparseGradebookItem> assignmentsRest = getAssignmentsForCategories(categoryDefinition);
        	Category categoryRest=new Category(categoryDefinition.getName(), 
        			categoryDefinition.getWeight(),categoryDefinition.getDropLowest(),
        			categoryDefinition.getDropHighest(),categoryDefinition.getKeepHighest(),assignmentsRest);
        	categories.add(categoryRest);
        }
       
        return categories;
    }
    
    /**
     * 
     * @param categoryDefinition
     * @return basic information for assignments in the given category
     */
    private List<SparseGradebookItem> getAssignmentsForCategories(CategoryDefinition categoryDefinition) {
        List<Assignment> assignmentList = categoryDefinition.getAssignmentList();
        List<SparseGradebookItem> assignmentsRest = new ArrayList<SparseGradebookItem>();
        for (Assignment assign : assignmentList) {
            SparseGradebookItem gbItem = new SparseGradebookItem(assign.getId(), assign.getName());
            assignmentsRest.add(gbItem);
        }

        return assignmentsRest;
    }

    /**
     * @param userId
     *            the current sakai user id (not username)
     * @return true if the user has update access in any sites
     */
    public boolean isUserInstructor(String userId) {
        boolean inst = false;
        // admin never counts as in instructor
        if (!isUserAdmin(userId)) {
            int count = siteService.countSites(SelectionType.UPDATE, null, null, null);
            inst = (count > 0);
        }
        return inst;
    }

    /**
     * Check if the current user in an instructor for the given user id,
     * this will return the first course found in alpha order,
     * will only check the first 100 courses
     * 
     * @param studentUserId the Sakai user id for the student
     * @return the course ID of the course they are an instructor for the student OR null if they are not
     */
    public String isInstructorOfUser(String studentUserId) {
        if (studentUserId == null || "".equals(studentUserId)) {
            throw new IllegalArgumentException("studentUserId must be set");
        }
        String courseId = null;
        List<Site> sites = getInstructorSites();
        if (sites != null && ! sites.isEmpty()) {
            if (sites.size() >= 99) {
                // if instructor of 99 or more sites then auto-approved 
                courseId = sites.get(0).getId();
            } else {
                for (Site site : sites) {
                    Member member = site.getMember(studentUserId);
                    if (member != null) {
                        courseId = site.getId();
                        break;
                    }
                }
            }
        }
        return courseId;
    }

    /**
     * Gets the gradebook data for a given site, 
     * this uses the gradebook security so it is secure
     * 
     * @param siteId a sakai siteId (cannot be group Id)
     * @param gbItemName [OPTIONAL] an item name to fetch from this gradebook (limit to this item only),
     * if null then all items are returned
     * @throws IllegalArgumentException if no gradebook can be found
     */
    @SuppressWarnings("unchecked")
    public Gradebook getCourseGradebook(String siteId, String gbItemName) {
        // The gradebookUID is the siteId, the gradebookID is a long
        String gbID = siteId;
        if (!gradebookService.isGradebookDefined(gbID)) {
            throw new IllegalArgumentException("No gradebook found for site: " + siteId);
        }
        // verify permissions
        String userId = getCurrentUserId();
        if (userId == null 
                || ! siteService.allowUpdateSite(siteId) 
                || ! siteService.allowViewRoster(siteId) ) {
            throw new SecurityException("User ("+userId+") cannot access gradebook in site ("+siteId+")");
        }
        Gradebook gb = new Gradebook(gbID);
        gb.averageCourseGrade = gradebookService.getAverageCourseGrade(gbID);
        gb.students = getStudentsForCourse(siteId);
        Map<String, String> studentUserIds = new ConcurrentHashMap<String, String>();
        for (Student student : gb.students) {
            studentUserIds.put(student.userId, student.username);
        }
        ArrayList<String> studentIds = new ArrayList<String>(studentUserIds.keySet());
        if (gbItemName == null) {
            List<Assignment> gbitems = gradebookService.getAssignments(gbID);
            for (Assignment assignment : gbitems) {
                GradebookItem gbItem = makeGradebookItemFromAssignment(gbID, assignment, studentUserIds, studentIds);
                gb.items.add(gbItem);
            }
        } else {
            Assignment assignment = gradebookService.getAssignment(gbID, gbItemName);
            if (assignment != null) {
                GradebookItem gbItem = makeGradebookItemFromAssignment(gbID, assignment, studentUserIds, studentIds);
                gb.items.add(gbItem);
            } else {
                throw new IllegalArgumentException("Invalid gradebook item name ("+gbItemName+"), no item with this name found in cource ("+siteId+")");
            }
        }
        gb.category=getCategoriesForCourse(gbID);
        GradebookInformation gradebookInformation = gradebookService.getGradebookInformation(gbID);
        gb.displayReleasedGradeItemsToStudents=gradebookInformation.isDisplayReleasedGradeItemsToStudents();
        gb.gradebookScale=gradebookInformation.getGradeScale();
        int gradeType = gradebookInformation.getGradeType();
        if(gradeType==GradebookService.GRADE_TYPE_POINTS) {
        	gb.isPointFlag=true;
        } else if(gradeType==GradebookService.GRADE_TYPE_PERCENTAGE) {
        	gb.isPercentFlag=true;
        } else if(gradeType==GradebookService.GRADE_TYPE_LETTER) {
                gb.isLetterGradeFlag=true;
        }
        return gb;
    }

    private GradebookItem makeGradebookItemFromAssignment(String gbID, Assignment assignment,
            Map<String, String> studentUserIds, ArrayList<String> studentIds) {
        // build up the items listing
        GradebookItem gbItem = new GradebookItem(gbID, assignment.getName(), assignment
                .getPoints(), assignment.getDueDate(), assignment.getExternalAppName(),
                assignment.isReleased(), assignment.isCounted());
        gbItem.id = assignment.getId();
        /*
         *  We have to iterate through each student and get the grades out... 
         *  2.6 gradebook service has some issues
         */
        for (String studentId : studentIds) {
            // too expensive: if (gradebookService.getGradeViewFunctionForUserForStudentForItem(gbID, assignment.getId(), studentId) == null) {
            String grade = gradebookService.getAssignmentScoreString(gbID, assignment.getId(), studentId);
            if (grade != null) {
                GradebookItemScore score = new GradebookItemScore(assignment.getId().toString(),
                        studentId, grade );
                score.username = studentUserIds.get(studentId);
                CommentDefinition cd = gradebookService.getAssignmentScoreComment(gbID, assignment
                        .getId(), studentId);
                if (cd != null) {
                    score.comment = cd.getCommentText();
                    score.recorded = cd.getDateRecorded();
                    score.graderUserId = cd.getGraderUid();
                }
                gbItem.scores.add(score);
            }
        }
        /* This is another post 2.5 way
        List<GradeDefinition> grades = gradebookService.getGradesForStudentsForItem(siteId,
                assignment.getId(), studentIds);
        for (GradeDefinition gd : grades) {
            String studId = gd.getStudentUid();
            String studEID = studentUserIds.get(studId);
            GradebookItemScore score = new GradebookItemScore(assignment.getId().toString(),
                    studId, gd.getGrade(), studEID, gd.getGraderUid(), gd.getDateRecorded(),
                    gd.getGradeComment());
            gbItem.scores.add(score);
        }
        **/
        return gbItem;
    }

    /**
     * Save a gradebook item and optionally the scores within <br/>
     * Scores must have at least the studentId or username AND the grade set
     * 
     * @param gbItem
     *            the gradebook item to save, must have at least the gradebookId and name set
     * @return the updated gradebook item and scores, contains any errors that occurred
     * @throws IllegalArgumentException if the assignment is invalid and cannot be saved
     * @throws SecurityException if the current user does not have permissions to save
     */
    public GradebookItem saveGradebookItem(GradebookItem gbItem) {
        if (log.isDebugEnabled()) log.debug("saveGradebookItem("+gbItem+")");
        if (gbItem == null) {
            throw new IllegalArgumentException("gbItem cannot be null");
        }
        if (gbItem.gradebookId == null || "".equals(gbItem.gradebookId)) {
            throw new IllegalArgumentException("gbItem must have the gradebookId set");
        }
        if (gbItem.name == null || "".equals(gbItem.name)) {
            throw new IllegalArgumentException("gbItem must have the name set");
        }

        String gradebookId = gbItem.gradebookId;
        boolean isExternal = false;
        // try to find by eid first
        Assignment assignment = findAssignmentByExternalId(gradebookId, gbItem.eid);
        if (assignment != null) {
            if (log.isDebugEnabled()) log.debug("find external("+assignment+"), eid="+gbItem.eid);
            gbItem.id = assignment.getId();
            isExternal = true;
        }
        if (!EXTERNAL_ONLY) {
            // in the GB service we can only lookup by name or internal id
            if (assignment == null) {
                // try to get by internal id
                if (gbItem.id != null) {
                    assignment = gradebookService.getAssignment(gradebookId, gbItem.id);
                }
                if (assignment == null) {
                    // try to find by name
                    if (gradebookService.isAssignmentDefined(gradebookId, gbItem.name)) {
                        assignment = gradebookService.getAssignment(gradebookId, gbItem.id);
                        if (log.isDebugEnabled()) log.debug("found internal assign by name ("+gbItem.name+"): "+assignment);
                    }
                } else  {
                    if (log.isDebugEnabled()) log.debug("found internal assign by id ("+gbItem.id+"): "+assignment);
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("found assignment ("+assignment+") from gb item: "+gbItem);
        // now we have the item if it exists
        try {
            // try to save or update it
            if (assignment == null) {
                // no item so create one
                gbItem.name = makeSafeAssignmentName(gradebookId, gbItem.name);
                if (EXTERNAL_ONLY) {
                    gradebookExternalAssessmentService.addExternalAssessment(gradebookId, gbItem.eid, 
                            null, gbItem.name, gbItem.pointsPossible, gbItem.dueDate, EXTERNAL_DATASOURCE, false);
                    // GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException
                    // fetch the newly created assignment object so we can get the id from it
                    isExternal = true;
                    assignment = gradebookService.getAssignment(gradebookId, gbItem.id);
                    if (assignment != null) {
                        if (log.isDebugEnabled()) log.debug("found new external assignment ("+assignment+") from name: "+gbItem.name);
                    } else {
                        assignment = findAssignmentByExternalId(gradebookId, gbItem.eid);
                        if (assignment != null) {
                            if (log.isDebugEnabled()) log.debug("found new external assignment ("+assignment+") from eid: "+gbItem.eid);
                        } else {
                            log.warn("FATAL error finding new external assignment from item: "+gbItem);
                            throw new IllegalArgumentException("Unable to find the newly saved item: "+gbItem);
                        }
                    }
                    if (log.isDebugEnabled()) log.debug("created external assignment ("+assignment+") from item: "+gbItem);
                } else {
                    // create internal assignment
                    assignment = new Assignment();
                    /* setting External fields has no effect
                    assignment.setExternallyMaintained(false); // cannot modify it later if true
                    if (gbItem.eid != null) {
                        assignment.setExternalId(gbItem.eid);
                    }
                    assignment.setExternalAppName(gbItem.type);
                    */
                    // assign values
                    assignment.setDueDate(gbItem.dueDate);
                    assignment.setName(gbItem.name);
                    assignment.setPoints(gbItem.pointsPossible);
                    assignment.setReleased(gbItem.released);
                    gradebookService.addAssignment(gradebookId, assignment);
                    // SecurityException, AssignmentHasIllegalPointsException, RuntimeException
                    if (log.isDebugEnabled()) log.debug("created internal assignment ("+assignment+") from item: "+gbItem);
                }
                gbItem.id = assignment.getId();
            } else {
                if (gbItem.dueDate != null) {
                    assignment.setDueDate(gbItem.dueDate);
                }
                if (gbItem.pointsPossible != null && gbItem.pointsPossible >= 0d) {
                    assignment.setPoints(gbItem.pointsPossible);
                }
                String originalName = assignment.getName();
                if (gbItem.name != null && "".equals(gbItem.name)
                        && !gbItem.name.equals(assignment.getName()) ) {
                    assignment.setName( makeSafeAssignmentName(gradebookId, gbItem.name) );
                }
                if (isExternal) {
                    gradebookExternalAssessmentService.updateExternalAssessment(gradebookId, gbItem.eid, 
                            null, assignment.getName(), assignment.getPoints(), assignment.getDueDate(), false);
                    if (log.isDebugEnabled()) log.debug("updated external assignment ("+assignment+") from item: "+gbItem);
                } else {
                    // assign new values to existing assignment
                    /* setting External fields has no effect
                    if (gbItem.type != null) {
                        assignment.setExternalAppName(gbItem.type);
                        //assignment.setExternalId(gbItem.type);
                    }
                    */
                    // assignment.setReleased(gbItem.released); // no mod released setting from here
                    gradebookService.updateAssignment(gradebookId, assignment.getId(), assignment);
                    // SecurityException, RuntimeException
                    if (log.isDebugEnabled()) log.debug("updated internal assignment ("+assignment+") from item: "+gbItem);
                }
            }
        } catch (SecurityException e) {
            log.warn("security failure (gb="+gradebookId+", item="+gbItem+", asgn="+assignment+"): cannot create: " + e, e);
            throw e; // rethrow
        } catch (RuntimeException e) {
            String msg = "Invalid assignment (gb="+gradebookId+", item="+gbItem+", asgn="+assignment+"): cannot create: " + e;
            log.warn(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
        int errorsCount = 0;
        if (gbItem.scores != null && !gbItem.scores.isEmpty()) {
            // now update scores if there are any to update, 
            // this will not remove scores and will only add new ones
            for (GradebookItemScore score : gbItem.scores) {
                if (isBlank(score.username) && isBlank(score.userId)) {
                    score.error = "USER_INVALID: User ID and Name are both blank";
                    continue;
                }
                // find out if the student id is internal or external, and set the score vals and then studentId to the internal id
                String studentId = score.userId;
                if (isBlank(score.userId)) {
                    studentId = score.username;
                }
                try {
                    // try assuming it is the internal id first
                    score.username = userDirectoryService.getUserEid(studentId);
                    score.userId = studentId;
                } catch (UserNotDefinedException e) {
                    try {
                        // now try it as the external id
                        score.userId = userDirectoryService.getUserId(studentId);
                        score.username = studentId;
                        studentId = score.userId;
                    } catch (UserNotDefinedException ex) {
                        score.error = "USER_NOT_EXISTS: User (id="+score.userId+", name="+score.username+") could not be found";
                        errorsCount++;
                        continue;
                    }
                }
                score.assignId(gbItem.id, studentId);
                // null/blank scores are not allowed
                if (isBlank(score.grade)) {
                    score.error = "NO_SCORE_ERROR: score is blank";
                    errorsCount++;
                    continue;
                }

                Double dScore;
                try {
                    dScore = Double.valueOf(score.grade);
                } catch (NumberFormatException e) {
                    score.error = "SCORE_INVALID: Score ("+score.grade+") is invalid";
                    errorsCount++;
                    continue;
                }
                // Student Score should not be greater than the total points possible
                if (dScore > assignment.getPoints()) {
                    score.error = "POINTS: Score ("+dScore+") > PointsPossible ("+assignment.getPoints()+")";
                    errorsCount++;
                    continue;
                }
                try {
                    /** check against existing score ** NOT NEEDED
                    String currentScore = gradebookService.getAssignmentScoreString(gradebookUid, gbItem.name, studentId);
                    if (currentScore != null) {
                        try {
                            Double dCurrentScore = Double.valueOf(currentScore);
                            if (dScore < dCurrentScore) {
                                score.error = SCORE_UPDATE_ERRORS;
                                errorsCount++;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            // no comparison since this is not a numerical score
                        }
                    }
                    **/
                    // null grade deletes the score
                    if (isExternal) {
                        gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookId, gbItem.eid, studentId, dScore.toString());
                    } else {
                        gradebookService.setAssignmentScoreString(gradebookId, gbItem.id, studentId, dScore.toString(), EXTERNAL_DATASOURCE);
                    }
                    if (score.comment != null && ! "".equals(score.comment)) {
                        gradebookService.setAssignmentScoreComment(gradebookId, gbItem.id, studentId, score.comment);
                    }
                } catch (Exception e) {
                    // General errors, caused while performing updates (Tag: generalerrors)
                    String msg = "Failure saving score ("+score+"): "+e;
                    log.warn(msg, e);
                    score.error = "GENERAL: "+msg;
                    errorsCount++;
                }
                /* post-2.5 gradebook method
                try {
                    gradebookService.saveGradeAndCommentForStudent(gradebookUid, gbItemId,
                            studentId, score.grade, score.comment);
                } catch (InvalidGradeException e) {
                    scoreErrors.put(score, "SCORE_INVALID");
                    continue;
                }
                GradeDefinition gd = gradebookService.getGradeDefinitionForStudentForItem(
                        gradebookUid, gbItemId, studentId);
                score.assignId(gbItem.id, gd.getStudentUid());
                score.comment = gd.getGradeComment();
                score.grade = gd.getGrade();
                score.graderUserId = gd.getGraderUid();
                score.recorded = gd.getDateRecorded();
                */
            }
            // put the errors in the item if there are errors and they are set
            if (errorsCount > 0) {
                gbItem.scoreErrors = new HashMap<String, String>();
                for (GradebookItemScore score : gbItem.scores) {
                    if (score.error != null) {
                        gbItem.scoreErrors.put(score.id, score.error);
                    }
                }
            }
        }
        return gbItem;
    }

    /**
     * Check if a name exists already and if so, adjusts it so it is now safe without doing too many checks
     * @param gradebookId the gradebook unique id (can also be the siteId)
     * @param name the current assignment name
     * @return a name which is safe (may be the original if unused)
     */
    protected String makeSafeAssignmentName(String gradebookId, String name) {
        String safeName = name;
        if (gradebookService.isAssignmentDefined(gradebookId, name)) {
            // try to find a safe one
            safeName = name + "-1";
            if (gradebookService.isAssignmentDefined(gradebookId, name)) {
                // now make a name which we know will be safe
                safeName = StringUtils.abbreviate(name, 200) + "-" + System.currentTimeMillis();
            }
        }
        return safeName;
    }

    /**
     * Find an internal assignment (with internal id) for an assignment in a gradebook by the external id if it exists
     * 
     * @param gradebookId the gradebook unique id (can also be the siteId)
     * @param externalId the external id for the assignment (must be externally controlled)
     * @return the Assignment OR null if not found
     */
    protected Assignment findAssignmentByExternalId(String gradebookId, String externalId) {
        Assignment assignment = null;
        if (gradebookExternalAssessmentService.isExternalAssignmentDefined(gradebookId, externalId)) {
            @SuppressWarnings("unchecked")
            List<Assignment> assignments = gradebookService.getAssignments(gradebookId);
            for (Assignment a : assignments) {
                if (externalId.equals(a.getExternalId())
                        && EXTERNAL_DATASOURCE.equals(a.getExternalAppName()) ) {
                    assignment = a;
                    break;
                }
            }
        }
        return assignment;
    }

    /**
     * Remove an assignment using the external id to locate it,
     * will only remove assignments which actually are externally managed by us
     * 
     * @param gradebookId the gradebook unique id (can also be the siteId)
     * @param externalId the external id for the assignment (must be externally controlled)
     * @return the Assignment if found and removed OR null if not found and NOT removed
     */
    public GradebookItem removeGradebookItem(String gradebookId, String externalId) {
        GradebookItem removed = null;
        Assignment a = findAssignmentByExternalId(gradebookId, externalId);
        if (a != null) {
            /* NOTES:
             * (1) Removing the Assignment only does not work, adding external ones later will cause exceptions (externalId in use)
             *      gradebookService.removeAssignment(a.getId());
             * (2) Breaking the connection only does not actually remove the Assignment
             *      gradebookExternalAssessmentService.setExternalAssessmentToGradebookAssignment(gradebookId, externalId);
             * (3) The correct way which seems to remove the external assessment, Assignment, and scores is removeExternalAssessment
             *      gradebookExternalAssessmentService.removeExternalAssessment(gradebookId, externalId);
             */
            gradebookExternalAssessmentService.removeExternalAssessment(gradebookId, externalId);
            removed = new GradebookItem(gradebookId, a.getName(), a.getPoints(), a.getDueDate(), a.getCategoryName(), a.isReleased(), a.isCounted());
            removed.id = a.getId();
            removed.deleted = true;
        }
        return removed;
    }

    /**
     * String type: gets the printable name of this server
     */
    public static String SETTING_SERVER_NAME = "server.name";
    /**
     * String type: gets the unique id of this server (safe for clustering if used)
     */
    public static String SETTING_SERVER_ID = "server.cluster.id";
    /**
     * String type: gets the URL to this server
     */
    public static String SETTING_SERVER_URL = "server.main.URL";
    /**
     * String type: gets the URL to the portal on this server (or just returns the server URL if no
     * portal in use)
     */
    public static String SETTING_PORTAL_URL = "server.portal.URL";
    /**
     * Boolean type: if true then there will be data preloads and DDL creation, if false then data
     * preloads are disabled (and will cause exceptions if preload data is missing)
     */
    public static String SETTING_AUTO_DDL = "auto.ddl";

    /**
     * Retrieves settings from the configuration service (sakai.properties)
     * 
     * @param settingName
     *            the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
     *            mystuff.config, etc. OR one of the SETTING constants (e.g
     *            {@link #SETTING_AUTO_DDL})
     * 
     * @param defaultValue
     *            a specified default value to return if this setting cannot be found, <b>NOTE:</b>
     *            You can set the default value to null but you must specify the class type in
     *            parens
     * @return the value of the configuration setting OR the default value if none can be found
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T returnValue = defaultValue;
        if (SETTING_SERVER_NAME.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerName();
        } else if (SETTING_SERVER_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerUrl();
        } else if (SETTING_PORTAL_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getPortalUrl();
        } else if (SETTING_SERVER_ID.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerIdInstance();
        } else {
            if (defaultValue == null) {
                returnValue = (T) serverConfigurationService.getString(settingName);
                if ("".equals(returnValue)) {
                    returnValue = null;
                }
            } else {
                if (defaultValue instanceof Number) {
                    int num = ((Number) defaultValue).intValue();
                    int value = serverConfigurationService.getInt(settingName, num);
                    returnValue = (T) Integer.valueOf(value);
                } else if (defaultValue instanceof Boolean) {
                    boolean bool = ((Boolean) defaultValue).booleanValue();
                    boolean value = serverConfigurationService.getBoolean(settingName, bool);
                    returnValue = (T) Boolean.valueOf(value);
                } else if (defaultValue instanceof String) {
                    returnValue = (T) serverConfigurationService.getString(settingName,
                            (String) defaultValue);
                }
            }
        }
        return returnValue;
    }

    public static boolean isBlank(String str) {
        if (str == null || "".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * Set a current user for the current thread, create session if needed
     * @param userId the userId to set
     */
    public void setCurrentUser(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            // start a session if none is around
            currentSession = sessionManager.startSession(userId);
        }
        currentSession.setUserId(userId);
        currentSession.setActive();
        sessionManager.setCurrentSession(currentSession);
        authzGroupService.refreshUser(userId);
    }

}
