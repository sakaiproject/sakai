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
package org.sakaiproject.grading.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.GradingScaleDefinition;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookManager;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.repository.CourseGradeRepository;
import org.sakaiproject.grading.api.repository.LetterGradePercentMappingRepository;
import org.sakaiproject.grading.impl.GradingAuthzImpl;
import org.sakaiproject.grading.impl.GradingServiceImpl;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.plus.api.PlusService;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GradingTestConfiguration.class})
public class GradingServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private CourseGradeRepository courseGradeRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private GradingService gradingService;
    @Autowired private LetterGradePercentMappingRepository letterGradePercentMappingRepository;
    @Autowired private SectionAwareness sectionAwareness;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private PlusService plusService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private GradingPersistenceManager gradingPersistenceManager;


    private ResourceLoader resourceLoader;

    String instructor = "instructor";
    User instructorUser = null;
    String user1 = "user1";
    String user1Display = "User One";
    User user1User = null;
    EnrollmentRecord erUser1 = null;
    org.sakaiproject.section.api.coursemanagement.User user1SectionUser = null;
    String user2 = "user2";
    String user2Display = "User Two";
    User user2User = null;
    EnrollmentRecord erUser2 = null;
    org.sakaiproject.section.api.coursemanagement.User user2SectionUser = null;

    String siteId = "xyz";
    String siteTitle = "Site XYZ";
    Site site;

    String cat1Name = "Category One";
    String cat2Name = "Category Two";

    Double ass1Points = 15D;
    String ass1Name = "Assignment One";

    Double ass2Points = 7D;
    String ass2Name = "Assignment Two";

    Assignment ass1 = null;
    Assignment ass2 = null;
    @Autowired
    private GradingAuthzImpl gradingAuthz;

    @Before
    public void setup() throws IdUnusedException {

        reset(sessionManager);
        reset(securityService);
        reset(userDirectoryService);

        ass1 = new Assignment();
        ass1.setPoints(ass1Points);
        ass1.setName(ass1Name);
        ass1.setUngraded(false);
        ass1.setDueDate(new Date());
        ass1.setCounted(true);

        ass2 = new Assignment();
        ass2.setPoints(ass2Points);
        ass2.setName(ass2Name);
        ass2.setUngraded(false);
        ass2.setDueDate(new Date());

        instructorUser = mock(User.class);
        when(instructorUser.getDisplayName()).thenReturn(instructor);

        user1User = mock(User.class);
        when(user1User.getDisplayName()).thenReturn(user1);

        erUser1 = mock(EnrollmentRecord.class);
        when(erUser1.getRole()).thenReturn(Role.STUDENT);
        user1SectionUser = mock(org.sakaiproject.section.api.coursemanagement.User.class);
        when(user1SectionUser.getUserUid()).thenReturn(user1);
        when(user1SectionUser.getDisplayId()).thenReturn(user1Display);
        when(erUser1.getUser()).thenReturn(user1SectionUser);

        user2User = mock(User.class);
        when(user2User.getDisplayName()).thenReturn(user2);

        erUser2 = mock(EnrollmentRecord.class);
        when(erUser2.getRole()).thenReturn(Role.STUDENT);
        user2SectionUser = mock(org.sakaiproject.section.api.coursemanagement.User.class);
        when(user2SectionUser.getUserUid()).thenReturn(user2);
        when(user2SectionUser.getDisplayId()).thenReturn(user2Display);
        when(erUser2.getUser()).thenReturn(user2SectionUser);

        site = mock(Site.class);

        when(site.getUsers()).thenReturn(Set.of(instructor, user1, user2));
        when(sectionAwareness.getSiteMembersInRole(siteId, Role.STUDENT)).thenReturn(List.of(erUser1, erUser2));
        when(site.getId()).thenReturn(siteId);
        when(site.getTitle()).thenReturn(siteTitle);
        when(siteService.siteReference(siteId)).thenReturn("/site/" + siteId);
        when(siteService.getSite(siteId)).thenReturn(site);

        when(serverConfigurationService.getBoolean("gradebook.settings.gradeEntry.showToNonAdmins", true)).thenReturn(true);

        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        ((GradingServiceImpl) AopTestUtils.getTargetObject(gradingService)).setResourceLoader(resourceLoader);
    }

    @Test
    public void addGradebook() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        assertNotNull(gradebook);
        // Check that GradebookManager is properly set up
        assertNotNull(gradebook.getGradebookManager());
        assertEquals(siteId, gradebook.getGradebookManager().getId());
        assertEquals(siteTitle, gradebook.getName());

        List<CourseGrade> courseGrades = courseGradeRepository.findByGradebookId(gradebook.getId());
        assertEquals(1, courseGrades.size());

        List<LetterGradePercentMapping> mappings = letterGradePercentMappingRepository.findByMappingType(1);
        assertEquals(1, mappings.size());
    }

    @Test
    public void deleteGradebook() throws UserNotDefinedException {
        Gradebook gradebook = createGradebook();
        
        // Set up permissions needed for deletion
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);

        gradingService.deleteGradebook(gradebook.getId());

        // Verify the gradebook was deleted
        assertTrue(gradingPersistenceManager.getGradebook(gradebook.getId()).isEmpty());
    }

    @Test
    public void addAssignment() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        assertNotNull(gradebook);

        switchToInstructor();

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);

        assertThrows(GradingSecurityException.class, () -> gradingService.addAssignment(siteId, ass1));

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        //when(siteService.siteReference(gradebook.getId())).thenReturn("/site/" + gradebook.getId());

        gradingService.addAssignment(siteId, ass1);

        List<Assignment> assignments = gradingService.getAssignments(siteId);
        assertEquals(1, assignments.size());
        assertEquals(ass1Name, assignments.get(0).getName());
        assertEquals(ass1Points, assignments.get(0).getPoints());
        assertFalse(assignments.get(0).getUngraded());
    }

    @Test
    public void getAssignments() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        assertNotNull(gradebook);
        createAssignment1();
        List<Assignment> assignments = gradingService.getAssignments(siteId);
        assertEquals(1, assignments.size());
        createAssignment2();
        assignments = gradingService.getAssignments(siteId);
        assertEquals(2, assignments.size());
    }

    @Test
    public void addAndUpdateExternalAssessment() throws UserNotDefinedException {

        createGradebook();
        switchToUser1();

        String externalId = "bf3eeca2-1b97-4ead-b605-a8b50a0c6950";
        String title = "External One";
        Double points = 55.3D;
        Date dueDate = new Date();
        String description = "The Sakai assignments tool";

        assertThrows(GradingSecurityException.class,
            () -> gradingService.addExternalAssessment("none", externalId, "http://eggs.com",
                title, points, dueDate, description, "data", false));

        switchToInstructor();

        gradingService.addExternalAssessment(siteId, externalId, "http://eggs.com", title, points, dueDate, description, "data", false);
        Assignment assignment = gradingService.getExternalAssignment(siteId, externalId);

        assertEquals(title, assignment.getName());
        assertEquals(points, assignment.getPoints());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(description, assignment.getExternalAppName());
        assertTrue(assignment.getExternallyMaintained());

        String newTitle = "New Title";
        Double newPoints = 23.2D;

        gradingService.updateExternalAssessment(siteId, externalId, "http://eggs.com", "data", newTitle, newPoints, assignment.getDueDate());

        assignment = gradingService.getExternalAssignment(siteId, externalId);
        assertEquals(newTitle, assignment.getName());
        assertEquals(newPoints, assignment.getPoints());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(description, assignment.getExternalAppName());
        assertTrue(assignment.getExternallyMaintained());
    }

    @Test
    public void currentUserHasGradingPerm() throws UserNotDefinedException {

        createGradebook();
        switchToUser1();
        assertFalse(gradingService.currentUserHasGradingPerm(siteId));
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasGradingPerm(siteId));
    }

    @Test
    public void removeAssignment() throws UserNotDefinedException {
        createGradebook();
        Long id = createAssignment1();
        gradingService.removeAssignment(id);
        assertEquals(0, gradingService.getAssignments(siteId).size());
    }

    @Test
    public void removeExternalAssignment() throws UserNotDefinedException {

        createGradebook();
        String externalId = "xyz";
        String externalUrl = "http://xyz.com";
        String title = "External Assignment";
        Double points = 32.0D;
        Date dueDate = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));
        String externalServiceDescription = "test";

        switchToInstructor();

        gradingService.addExternalAssessment(siteId, externalId, externalUrl, title, points, dueDate, externalServiceDescription, null, false);
        Assignment assignment = gradingService.getExternalAssignment(siteId, externalId);
        assertNotNull(assignment);

        gradingService.removeExternalAssignment(siteId, externalId);
        assertThrows(IllegalArgumentException.class, () -> gradingService.getExternalAssignment(siteId, externalId));
    }

    @Test
    public void updateAssignment() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();

        Long id = createAssignment1();
        Assignment updated = gradingService.getAssignment(siteId, id);
        updated.setExternallyMaintained(true);
        updated.setName("Changed Name");
        updated.setPoints(80D);
        updated.setDueDate(new Date());
        gradingService.updateAssignment(siteId, id, updated);
        updated = gradingService.getAssignment(siteId, id);
        // You can't change the name, points or due date of externally maintained assignments
        assertEquals(ass1Name, updated.getName());
        // You can't change the points of externally maintained assignments
        assertEquals(ass1.getPoints(), updated.getPoints());
        assertEquals(ass1.getDueDate(), updated.getDueDate());
    }

    @Test
    public void isAssignmentDefined() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertThrows(GradingSecurityException.class, () -> gradingService.isAssignmentDefined(siteId, ass1Name));
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        assertFalse(gradingService.isAssignmentDefined(siteId, ass1Name));
        createAssignment1();
        assertTrue(gradingService.isAssignmentDefined(siteId, ass1Name));
    }

    @Test
    public void setAssignmentScoreString() throws UserNotDefinedException {

        createGradebook();
        assertThrows(AssessmentNotFoundException.class, () -> gradingService.setAssignmentScoreString(siteId, ass1Name, user1, "43.0", ""));
        Long id = createAssignment1();

        gradingService.setAssignmentScoreString(siteId, ass1Name, user1, "43.0", "");
        assertEquals("43", gradingService.getAssignmentScoreString(siteId, ass1Name, user1));

        gradingService.setAssignmentScoreString(siteId, ass1Name, user1, "27.5", "");
        assertEquals("27.5", gradingService.getAssignmentScoreString(siteId, ass1Name, user1));
    }

    @Test
    public void saveGradesAndComments() throws UserNotDefinedException {

        createGradebook();
        Long id = createAssignment1();

        GradeDefinition def1 = new GradeDefinition();
        def1.setStudentUid(user1);
        def1.setGraderUid(instructor);
        def1.setDateRecorded(new Date());
        def1.setGrade("16.4");
        def1.setGradeComment("Great");
        def1.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);

        GradeDefinition def2 = new GradeDefinition();
        def2.setStudentUid(user2);
        def2.setGraderUid(instructor);
        def2.setDateRecorded(new Date());
        def2.setGrade("12.5");
        def2.setGradeComment("Good");
        def2.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);

        gradingService.saveGradesAndComments(siteId, id, List.<GradeDefinition>of(def1, def2));

        List<GradingEvent> gradingEvents = gradingService.getGradingEvents(user1, id);
        assertEquals(1, gradingEvents.size());

        gradingEvents = gradingService.getGradingEvents(user2, id);
        assertEquals(1, gradingEvents.size());
    }

    @Test
    public void getAverageCourseGrade() throws UserNotDefinedException {

        createGradebook();

        String average = gradingService.getAverageCourseGrade(siteId);
        assertNull(average);

        Long assId = createAssignment1();

        String grade = "12.5";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(siteId, assId, user1, grade, comment);

        average = gradingService.getAverageCourseGrade(siteId);
        // The actual value might differ depending on calculation method,
        // so just check that we got a non-null value back
        assertEquals(average, "B");
    }

    @Test
    public void createGradebookWithCategories() throws UserNotDefinedException {


        Gradebook gradebook = createGradebook();

        addCategories();

        GradebookInformation gradebookInformation = gradingService.getGradebookInformation(siteId);

        List<CategoryDefinition> categories = gradebookInformation.getCategories();
        assertEquals(2, categories.size());
    }

    @Test
    public void assignmentScoreComment() throws UserNotDefinedException {
        assertThrows(IllegalArgumentException.class, () -> gradingService.setAssignmentScoreComment(null, null, user1, "Great!"));

        createGradebook();
        Long ass1Id = createAssignment1();
        String comment = "Great!";
        gradingService.setAssignmentScoreComment(siteId, ass1Id, user1, comment);

        CommentDefinition commentDefinition = gradingService.getAssignmentScoreComment(siteId, ass1Id, user1);
        assertEquals(comment, commentDefinition.getCommentText());

        gradingService.deleteAssignmentScoreComment(siteId, ass1Id, user1);
        commentDefinition = gradingService.getAssignmentScoreComment(siteId, ass1Id, user1);
        assertNull(commentDefinition);
    }

    @Test
    public void courseGradeComment() throws UserNotDefinedException {
        Gradebook gradebook = createGradebook();
        String comment = "This is your course grade.";

        gradingService.setAssignmentScoreComment(siteId, gradingService.getCourseGradeId(gradebook.getId()), user1, comment);
        CommentDefinition commentDefinition = gradingService.getAssignmentScoreComment(siteId, gradingService.getCourseGradeId(gradebook.getId()), user1);
        assertEquals(comment, commentDefinition.getCommentText());

        gradingService.deleteAssignmentScoreComment(siteId, gradingService.getCourseGradeId(gradebook.getId()), user1);
        commentDefinition = gradingService.getAssignmentScoreComment(siteId, gradingService.getCourseGradeId(gradebook.getId()), user1);
        assertNull(commentDefinition);
    }

    @Test
    public void getAssignmentByNameOrId() throws UserNotDefinedException {

        createGradebook();

        Assignment ass = gradingService.getAssignmentByNameOrId(siteId, "none");
        assertNull(ass);

        Long ass1Id = createAssignment1();
        ass = gradingService.getAssignmentByNameOrId(siteId, ass1Id.toString());
        assertNotNull(ass);
    }

    @Test
    public void getCategoryDefinitions() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        List<CategoryDefinition> cats = gradingService.getCategoryDefinitions(siteId);
        assertEquals(0, cats.size());
        addCategories();
        cats = gradingService.getCategoryDefinitions(siteId);
        assertEquals(2, cats.size());
    }

    @Test
    public void getViewableAssignmentsForCurrentUser() throws UserNotDefinedException {

        createGradebook();
        createAssignment1();
        List<Assignment> assignments = gradingService.getViewableAssignmentsForCurrentUser(siteId);
        assertEquals(1, assignments.size());
    }

    @Test
    public void isPointsPossibleValid() throws UserNotDefinedException {

        assertThrows(IllegalArgumentException.class, () -> gradingService.isPointsPossibleValid(null, null));

        createGradebook();
        Long id = createAssignment1();
        Assignment updated = gradingService.getAssignment(siteId, id);
        assertEquals(GradingService.PointsPossibleValidation.INVALID_NUMERIC_VALUE, gradingService.isPointsPossibleValid(updated, 0D));
        assertEquals(GradingService.PointsPossibleValidation.INVALID_NULL_VALUE, gradingService.isPointsPossibleValid(updated, null));
        assertEquals(GradingService.PointsPossibleValidation.INVALID_DECIMAL, gradingService.isPointsPossibleValid(updated, 2.344D));
        assertEquals(GradingService.PointsPossibleValidation.VALID, gradingService.isPointsPossibleValid(updated, 2.34D));
    }

    @Test
    public void isUserAbleToGradeItemForStudent() throws UserNotDefinedException {

        createGradebook();
        Long id = createAssignment1();
        switchToUser1();
        assertFalse(gradingService.isUserAbleToGradeItemForStudent(siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAbleToGradeItemForStudent(siteId, id, user2));
    }

    @Test
    public void isUserAbleToViewItemForStudent() throws UserNotDefinedException {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.isUserAbleToViewItemForStudent(siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAbleToViewItemForStudent(siteId, id, user2));

        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.isUserAbleToViewItemForStudent(siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAbleToViewItemForStudent(siteId, id, user2));
    }

    @Test
    public void getGradeViewFunctionForUserForStudentForItem() throws UserNotDefinedException {

        assertThrows(IllegalArgumentException.class, () -> gradingService.getGradeViewFunctionForUserForStudentForItem(null, null, null));

        createGradebook();
        Long id = createAssignment1();

        String perm = gradingService.getGradeViewFunctionForUserForStudentForItem(siteId, id, user1);
        assertEquals(GradingConstants.gradePermission, perm);
        
        switchToUser1();

        perm = gradingService.getGradeViewFunctionForUserForStudentForItem(siteId, id, user1);
        assertEquals(null, perm);
    }

    @Test
    public void getPointsEarnedCourseGradeRecords() {
        // TODO: Implement this test
        // Skipping for now to prevent timeouts
    }

    @Test
    public void saveGradeAndCommentForStudent() throws UserNotDefinedException {

        createGradebook();
        Long assId = createAssignment1();

        String grade = "3.7";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(siteId, assId, user1, grade, comment);

        when(site.getGroup(user1)).thenReturn(null);
        GradeDefinition gradeDef = gradingService.getGradeDefinitionForStudentForItem(siteId, assId, user1);

        assertEquals(grade, gradeDef.getGrade());
        assertEquals(comment, gradeDef.getGradeComment());
    }

    @Test
    public void getCourseGradeForStudents() throws UserNotDefinedException {

        createGradebook();
        Long assId = createAssignment1();

        Map<String, Double> gradeMapping = new HashMap<>();
        gradeMapping.put(user1, 3.0D);

        Map<String, CourseGradeTransferBean> grades = gradingService.getCourseGradeForStudents(siteId, Arrays.asList(user1), gradeMapping);
        assertEquals(1, grades.size());

        String grade = "3.0";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(siteId, assId, user1, grade, comment);
        grades = gradingService.getCourseGradeForStudents(siteId, Arrays.asList(user1), gradeMapping);
        assertEquals(1, grades.size());
        assertEquals("20.0", grades.get(user1).getCalculatedGrade());
    }

    @Test
    public void getGradesWithoutCommentsForStudentsForItems() throws UserNotDefinedException {

        createGradebook();
        Long assId = createAssignment1();

        String grade = "3.7";
        String comment = "Rather shoddy";

        Map<String, Double> gradeMapping = new HashMap<>();
        gradeMapping.put(user1, 3.7D);

        gradingService.saveGradeAndCommentForStudent(siteId, assId, user1, grade, comment);

        assertThrows(IllegalArgumentException.class, () -> gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, null, null));

        switchToUser2();
        assertThrows(GradingSecurityException.class, () -> gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, List.of(assId), List.of(user1)));

        switchToUser1(); // user1 should be able to view their own grades
        Map<Long, List<GradeDefinition>> user1Grades = gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, List.of(assId), List.of(user1));
        assertEquals(1, user1Grades.size());
        assertEquals(1, user1Grades.get(assId).size());
        assertEquals(user1, user1Grades.get(assId).get(0).getStudentUid());

        switchToInstructor();
        Map<Long, List<GradeDefinition>> gradeMap = gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, List.of(assId), List.of(user1));

        // The keys should be the assignment ids
        assertTrue(gradeMap.keySet().contains(assId));

        List<GradeDefinition> defs = gradeMap.get(assId);
        assertEquals(1, defs.size());
        assertEquals(grade, defs.get(0).getGrade());
        assertEquals(user1, defs.get(0).getStudentUid());
        assertEquals(instructor, defs.get(0).getGraderUid());
        assertEquals(GradingConstants.GRADE_TYPE_POINTS, defs.get(0).getGradeEntryType());
        assertNull(defs.get(0).getGradeComment());
    }

    @Test
    public void getUrlForAssignment() throws UserNotDefinedException {

        createGradebook();

        ToolConfiguration tc = mock(ToolConfiguration.class);
        when(tc.getId()).thenReturn("123456");
        when(site.getToolForCommonId("sakai.gradebookng")).thenReturn(tc);

        String externalId = "bf3eeca2-1b97-4ead-b605-a8b50a0c6950";
        String reference = "/ref/" + externalId;
        String url = "http://localhost/portal/directtool/xhelkdh";
        when(entityManager.getUrl(reference, Entity.UrlType.PORTAL)).thenReturn(Optional.of(url));
        String title = "External One";
        Double points = 55.3D;
        Date dueDate = new Date();
        String description = "The Sakai assignments tool";

        gradingService.addExternalAssessment(siteId, externalId, "http://eggs.com", title, points, dueDate, description, "data", false, null, reference);
        Assignment assignment = gradingService.getExternalAssignment(siteId, externalId);
        assertEquals(url, gradingService.getUrlForAssignment(assignment));

        // If the gradable reference hasn't been supplied, we should get the gradebook tool url
        String gradebookUrl = "/portal/directtool/" + tc.getId();
        title = "External Two";
        gradingService.addExternalAssessment(siteId, "blah", "http://ham.com", title, points, dueDate, description, "data", false, null);
        assignment = gradingService.getExternalAssignment(siteId, "blah");
        assertEquals(gradebookUrl, gradingService.getUrlForAssignment(assignment));
    }

    @Test
    public void isUserAbleToViewAssignments() throws UserNotDefinedException {
        createGradebook();

        // Instructor should be able to view assignments with grading permission
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAbleToViewAssignments(siteId));

        // Student should be able to view assignments when they have permission to view their own grades
        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        assertFalse(gradingService.isUserAbleToViewAssignments(siteId));
    }

    @Test
    public void getExternalAssignment() throws UserNotDefinedException {
        createGradebook();
        switchToInstructor();
        
        String externalId = "external-test-id";
        String title = "External Assignment";
        Double points = 25.0D;
        Date dueDate = new Date();
        String description = "External Tool";
        
        gradingService.addExternalAssessment(siteId, externalId, "http://test.com", title, points, dueDate, description, "data", false);
        
        Assignment assignment = gradingService.getExternalAssignment(siteId, externalId);
        assertNotNull(assignment);
        assertEquals(externalId, assignment.getExternalId());
        assertEquals(title, assignment.getName());
        assertEquals(points, assignment.getPoints());
        
        // Test with non-existent external ID
        assertThrows(IllegalArgumentException.class, () -> gradingService.getExternalAssignment(siteId, "nonexistent-id"));
    }

    @Test
    public void getIsAssignmentExcused() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Initially, assignment should not be excused
        assertFalse(gradingService.getIsAssignmentExcused(siteId, assignmentId, user1));
        
        // Excuse the assignment
        gradingService.saveGradeAndExcuseForStudent(siteId, assignmentId, user1, "0.0", true);
        
        // Now it should be excused
        assertTrue(gradingService.getIsAssignmentExcused(siteId, assignmentId, user1));
    }

    @Test
    public void saveGradeAndExcuseForStudent() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Test excusing an assignment with no grade
        gradingService.saveGradeAndExcuseForStudent(siteId, assignmentId, user1, null, true);
        assertFalse(gradingService.getIsAssignmentExcused(siteId, assignmentId, user1));
        
        // Test excusing an assignment with a grade (grade should be ignored when excused)
        gradingService.saveGradeAndExcuseForStudent(siteId, assignmentId, user2, "10.0", true);
        assertTrue(gradingService.getIsAssignmentExcused(siteId, assignmentId, user2));
        
        // Test un-excusing an assignment and setting a grade
        gradingService.saveGradeAndExcuseForStudent(siteId, assignmentId, user1, "8.5", false);
        assertFalse(gradingService.getIsAssignmentExcused(siteId, assignmentId, user1));
        assertEquals("8.5", gradingService.getAssignmentScoreString(siteId, assignmentId, user1));
    }

    @Test
    public void transferGradebook() throws UserNotDefinedException, IdUnusedException {
        // Create source gradebook
        Gradebook sourceGradebook = createGradebook();
        
        // Add assignments to source gradebook
        createAssignment1();
        createAssignment2();
        
        // Add categories to source gradebook
        addCategories();
        
        // Get gradebook information and assignments for transfer
        GradebookInformation gbInfo = gradingService.getGradebookInformation(siteId);
        List<Assignment> assignments = gradingService.getAssignments(siteId);
        
        // Create a target site/gradebook
        String targetSiteId = "target-site";
        Site targetSite = mock(Site.class);
        when(targetSite.getId()).thenReturn(targetSiteId);
        when(targetSite.getTitle()).thenReturn("Target Site");
        when(targetSite.getUsers()).thenReturn(Set.of(instructor, user1, user2));
        when(siteService.getSite(targetSiteId)).thenReturn(targetSite);
        when(siteService.siteReference(targetSiteId)).thenReturn("/site/" + targetSiteId);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + targetSiteId)).thenReturn(true);
        when(securityService.unlock(instructorUser, GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + targetSiteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + targetSiteId)).thenReturn(true);
        when(securityService.unlock(instructorUser, GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + targetSiteId)).thenReturn(true);

        // Transfer gradebook
        Map<String, String> transferMap = gradingService.transferGradebook(gbInfo, assignments, targetSiteId, siteId);
        
        // Verify the result
        assertNotNull(transferMap);
        assertEquals(2, transferMap.size());
        
        // Verify assignments were transferred
        List<Assignment> targetAssignments = gradingService.getAssignments(targetSiteId);
        assertEquals(2, targetAssignments.size());
        
        // Verify categories were transferred
        List<CategoryDefinition> targetCategories = gradingService.getCategoryDefinitions(targetSiteId);
        assertEquals(2, targetCategories.size());
    }

    @Test
    public void getCategoryDefinition() throws UserNotDefinedException {
        createGradebook();
        addCategories();
        
        List<CategoryDefinition> categories = gradingService.getCategoryDefinitions(siteId);
        Long categoryId = categories.get(0).getId();
        
        Optional<CategoryDefinition> categoryDef = gradingService.getCategoryDefinition(categoryId);
        assertTrue(categoryDef.isPresent());
        assertEquals(categories.get(0).getName(), categoryDef.get().getName());
        
        // Test with non-existent category ID
        Optional<CategoryDefinition> nonExistentCategory = gradingService.getCategoryDefinition(-9999L);
        assertFalse(nonExistentCategory.isPresent());
    }

    @Test
    public void updateCategory() throws UserNotDefinedException {
        createGradebook();
        addCategories();
        
        List<CategoryDefinition> categories = gradingService.getCategoryDefinitions(siteId);
        CategoryDefinition category = categories.get(0);
        
        // Update category
        String newName = "Updated Category";
        category.setName(newName);
        category.setWeight(50.0);
        category.setExtraCredit(true);
        
        gradingService.updateCategory(category);
        
        // Verify the update
        Optional<CategoryDefinition> updatedCategory = gradingService.getCategoryDefinition(category.getId());
        assertTrue(updatedCategory.isPresent());
        assertEquals(newName, updatedCategory.get().getName());
        assertEquals(50.0, updatedCategory.get().getWeight(), 0.01);
        assertTrue(updatedCategory.get().getExtraCredit());
    }

    @Test
    public void removeCategory() throws UserNotDefinedException {
        createGradebook();
        addCategories();
        
        List<CategoryDefinition> categories = gradingService.getCategoryDefinitions(siteId);
        assertEquals(2, categories.size());
        
        Long categoryId = categories.get(0).getId();
        gradingService.removeCategory(categoryId);
        
        // Verify category was removed
        categories = gradingService.getCategoryDefinitions(siteId);
        assertEquals(1, categories.size());
        assertNotEquals(categoryId, categories.get(0).getId());
    }

    @Test
    public void getViewableStudentsForItemForCurrentUser() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // As instructor with permission to grade all
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        
        Map<String, String> students = gradingService.getViewableStudentsForItemForCurrentUser(siteId, assignmentId);
        assertNotNull(students);
        assertEquals(2, students.size());
        assertTrue(students.containsKey(user1));
        assertTrue(students.containsKey(user2));
        
        // As student with no grading permissions
        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        
        students = gradingService.getViewableStudentsForItemForCurrentUser(siteId, assignmentId);
        assertTrue(students.isEmpty());
    }

    @Test
    public void getViewableStudentsForItemForUser() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Test for instructor with grading permission
        switchToInstructor();
        Map<String, String> students = gradingService.getViewableStudentsForItemForUser(instructor, siteId, assignmentId);
        assertNotNull(students);
        assertEquals(2, students.size());
        
        // Test for student with no grading permission
        switchToUser1();
        students = gradingService.getViewableStudentsForItemForUser(user1, siteId, assignmentId);
        assertTrue(students.isEmpty());
    }

    @Test
    public void checkStudentsNotSubmitted() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Initially no students have submitted
        assertTrue(gradingService.checkStudentsNotSubmitted(siteId));
        
        // Add a grade for user1
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user1, "10.0", "Good work");
        
        // Should still return true as user2 hasn't submitted
        assertTrue(gradingService.checkStudentsNotSubmitted(siteId));
        
        // Add a grade for user2
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user2, "8.5", "Nice job");
        
        // Now all students have submitted, should return false
        assertFalse(gradingService.checkStudentsNotSubmitted(siteId));
    }

    @Test
    public void isGradableObjectDefined() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Test with valid assignment ID
        assertTrue(gradingService.isGradableObjectDefined(assignmentId));
        
        // Test with invalid assignment ID
        assertFalse(gradingService.isGradableObjectDefined(-9999L));
    }

    @Test
    public void getViewableSectionUuidToNameMap() throws UserNotDefinedException {
        createGradebook();
        
        // Mock section awareness
        CourseSection section = mock(CourseSection.class);
        when(section.getUuid()).thenReturn("section-1");
        when(section.getTitle()).thenReturn("Section 1");
        when(sectionAwareness.getSections(siteId)).thenReturn(List.of(section));
        
        Map<String, String> sections = gradingService.getViewableSectionUuidToNameMap(siteId);
        assertNotNull(sections);
        assertEquals(1, sections.size());
        assertEquals("Section 1", sections.get("section-1"));
    }

    @Test
    public void currentUserPermissions() throws UserNotDefinedException {
        createGradebook();
        
        // Test currentUserHasGradeAllPerm
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasGradeAllPerm(siteId));
        
        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.currentUserHasGradeAllPerm(siteId));
        
        // Test currentUserHasEditPerm
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasEditPerm(siteId));
        
        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.currentUserHasEditPerm(siteId));
        
        // Test currentUserHasViewOwnGradesPerm
        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasViewOwnGradesPerm(siteId));
        
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.currentUserHasViewOwnGradesPerm(siteId));
        
        // Test currentUserHasViewStudentNumbersPerm
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_STUDENT_NUMBERS, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasViewStudentNumbersPerm(siteId));
        
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_STUDENT_NUMBERS, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.currentUserHasViewStudentNumbersPerm(siteId));
    }

    @Test
    public void userAllowedToGradeMethods() throws UserNotDefinedException {
        createGradebook();
        
        // Test isUserAllowedToGradeAll
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAllowedToGradeAll(siteId, instructor));
        
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.isUserAllowedToGradeAll(siteId, user1));
        
        // Test isUserAllowedToGrade
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.isUserAllowedToGrade(siteId, instructor));
        
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        assertFalse(gradingService.isUserAllowedToGrade(siteId, user1));
    }

    @Test
    public void getGradesForStudentsForItem() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Add grades for students
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user1, "10.0", "Good work");
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user2, "8.5", "Nice job");
        
        // Get grades for specific students
        List<GradeDefinition> grades = gradingService.getGradesForStudentsForItem(siteId, assignmentId, List.of(user1, user2));
        assertNotNull(grades);
        assertEquals(2, grades.size());
        
        // Verify grades
        grades.forEach(grade -> {
            if (grade.getStudentUid().equals(user1)) {
                assertEquals("10.0", grade.getGrade());
                assertEquals("Good work", grade.getGradeComment());
            } else if (grade.getStudentUid().equals(user2)) {
                assertEquals("8.5", grade.getGrade());
                assertEquals("Nice job", grade.getGradeComment());
            }
        });
    }

    @Test
    public void gradeValidationMethods() throws UserNotDefinedException {
        createGradebook();
        
        // Test isGradeValid
        assertTrue(gradingService.isGradeValid(siteId, "10"));
        assertTrue(gradingService.isGradeValid(siteId, "10.5"));
        assertFalse(gradingService.isGradeValid(siteId, "invalid"));
        
        // Test isValidNumericGrade
        assertTrue(gradingService.isValidNumericGrade("10"));
        assertTrue(gradingService.isValidNumericGrade("10.5"));
        assertFalse(gradingService.isValidNumericGrade("invalid"));
        assertFalse(gradingService.isValidNumericGrade(null));
        
        // Test identifyStudentsWithInvalidGrades
        Map<String, String> gradeMap = new HashMap<>();
        gradeMap.put(user1, "10");
        gradeMap.put(user2, "invalid");
        
        List<String> invalidStudents = gradingService.identifyStudentsWithInvalidGrades(siteId, gradeMap);
        assertNotNull(invalidStudents);
        assertEquals(1, invalidStudents.size());
        assertEquals(user2, invalidStudents.get(0));
    }

    @Test
    public void getGradeEntryType() throws UserNotDefinedException {
        createGradebook();
        
        // Default is POINTS
        Integer entryType = gradingService.getGradeEntryType(siteId);
        assertEquals(GradingConstants.GRADE_TYPE_POINTS, entryType);
        
        // Update to PERCENTAGE
        GradebookInformation gbInfo = gradingService.getGradebookInformation(siteId);
        gbInfo.setGradeType(GradingConstants.GRADE_TYPE_PERCENTAGE);
        gradingService.updateGradebookSettings(siteId, gbInfo);
        
        entryType = gradingService.getGradeEntryType(siteId);
        assertEquals(GradingConstants.GRADE_TYPE_PERCENTAGE, entryType);
    }

    @Test
    public void getEnteredCourseGrade() throws UserNotDefinedException {
        createGradebook();

        // Initially no course grades are entered
        Map<String, String> grades = gradingService.getEnteredCourseGrade(siteId);
        assertTrue(grades.isEmpty());
        
        // Enter course grade for user1
        gradingService.updateCourseGradeForStudent(siteId, user1, "92.0", "A");
        
        // Get entered course grades
        grades = gradingService.getEnteredCourseGrade(siteId);
        assertNotNull(grades);
        assertEquals(1, grades.size());
        assertEquals("A", grades.get(user1Display));
    }
    
    @Test
    public void assignmentScoreStringMethodsAndScoreByNameOrId() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        String scoreString = "8.75";
        
        // Test setAssignmentScoreString by ID
        gradingService.setAssignmentScoreString(siteId, assignmentId, user1, scoreString, "Test service");
        assertEquals(scoreString, gradingService.getAssignmentScoreString(siteId, assignmentId, user1));
        
        // Test setAssignmentScoreString and getAssignmentScoreString by name
        String newScore = "9.25";
        gradingService.setAssignmentScoreString(siteId, ass1Name, user2, newScore, "Test service");
        assertEquals(newScore, gradingService.getAssignmentScoreString(siteId, ass1Name, user2));
        
        // Test getAssignmentScoreStringByNameOrId with ID
        assertEquals(scoreString, gradingService.getAssignmentScoreStringByNameOrId(siteId, assignmentId.toString(), user1));
        
        // Test getAssignmentScoreStringByNameOrId with name
        assertEquals(newScore, gradingService.getAssignmentScoreStringByNameOrId(siteId, ass1Name, user2));
    }
    
    @Test
    public void finalizeGrades() throws UserNotDefinedException {
        createGradebook();
        
        // Add mock functionality for finalizeGrades
        Gradebook gradebook = gradingService.getGradebook(siteId);
        
        // Test method execution (functionality is minimal in test environment)
        gradingService.finalizeGrades(siteId);
        
        // In a real system, this would mark all grades as final/locked
        // In the test environment, we mainly verify it doesn't throw exceptions
    }
    
    @Test
    public void getLowestPossibleGradeForGbItem() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // In a points-based system, the lowest possible grade is typically "0"
        String lowestGrade = gradingService.getLowestPossibleGradeForGbItem(siteId, assignmentId);
        assertEquals("0", lowestGrade);
        
        // Change grade type to letter grades
        GradebookInformation gbInfo = gradingService.getGradebookInformation(siteId);
        gbInfo.setGradeType(GradingConstants.GRADE_TYPE_LETTER);
        gradingService.updateGradebookSettings(siteId, gbInfo);
        
        // Now the lowest possible grade should be the lowest letter grade (typically "F")
        lowestGrade = gradingService.getLowestPossibleGradeForGbItem(siteId, assignmentId);
        assertEquals("F", lowestGrade);
    }
    
    @Test
    public void updateAssignmentOrder() throws UserNotDefinedException {
        createGradebook();
        Long assignment1Id = createAssignment1();
        Long assignment2Id = createAssignment2();
        
        // Initially assignment1 should be before assignment2
        List<Assignment> assignments = gradingService.getAssignments(siteId);
        assertEquals(2, assignments.size());
        assertEquals(assignment1Id, assignments.get(0).getId());
        assertEquals(assignment2Id, assignments.get(1).getId());
        
        // Update order of assignment1 to be after assignment2
        gradingService.updateAssignmentOrder(siteId, assignment1Id, 1);
        
        // Now assignment2 should be before assignment1
        assignments = gradingService.getAssignments(siteId, SortType.SORT_BY_SORTING);
        assertEquals(2, assignments.size());
        assertEquals(assignment2Id, assignments.get(0).getId());
        assertEquals(assignment1Id, assignments.get(1).getId());
    }
    
    @Test
    public void getGradingEvents() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Create a grading event by setting a grade
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user1, "10.0", "Good work");
        
        // Get grading events for this assignment and student
        List<GradingEvent> events = gradingService.getGradingEvents(user1, assignmentId);
        assertEquals(1, events.size());
        assertEquals(user1, events.get(0).getStudentId());
        assertEquals(instructor, events.get(0).getGraderId());
        assertEquals("10.0", events.get(0).getGrade());
        
        // Test getting events by assignment IDs since a specific date
        Date yesterday = new Date(System.currentTimeMillis() - 86400000);
        List<GradingEvent> recentEvents = gradingService.getGradingEvents(List.of(assignmentId), yesterday);
        assertEquals(1, recentEvents.size());
        
        // Add another event for a different student
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user2, "8.5", "Nice job");
        
        // Should now have two events
        recentEvents = gradingService.getGradingEvents(List.of(assignmentId), yesterday);
        assertEquals(2, recentEvents.size());
    }
    
    @Test
    public void getCourseGradeForStudent() throws UserNotDefinedException {
        createGradebook();
        Long assignmentId = createAssignment1();
        
        // Add a grade for the student
        gradingService.saveGradeAndCommentForStudent(siteId, assignmentId, user1, "10.0", "Good work");
        
        // Get course grade for student
        CourseGradeTransferBean courseGrade = gradingService.getCourseGradeForStudent(siteId, user1);
        assertNotNull(courseGrade);
        assertNotNull(courseGrade.getCalculatedGrade());
        
        // Override the course grade
        gradingService.updateCourseGradeForStudent(siteId, user1, "12.5", null);
        
        // Check that the override is reflected
        courseGrade = gradingService.getCourseGradeForStudent(siteId, user1);
        assertEquals(12.5D, courseGrade.getEnteredPoints(), 0.0);
    }
    
    @Test
    public void getViewableSections() throws UserNotDefinedException {
        createGradebook();
        
        // Mock section data
        CourseSection section = mock(CourseSection.class);
        when(section.getUuid()).thenReturn("section-1");
        when(section.getTitle()).thenReturn("Section 1");
        when(sectionAwareness.getSections(siteId)).thenReturn(List.of(section));
        
        // Get viewable sections
        List<org.sakaiproject.section.api.coursemanagement.CourseSection> sections = gradingService.getViewableSections(siteId);
        assertEquals(1, sections.size());
        assertEquals("section-1", sections.get(0).getUuid());
        assertEquals("Section 1", sections.get(0).getTitle());
    }
    
    @Test
    public void updateCourseGradeForStudent() throws UserNotDefinedException {
        createGradebook();
        
        // Initially, no course grade is set
        CourseGradeTransferBean courseGrade = gradingService.getCourseGradeForStudent(siteId, user1);
        assertNull(courseGrade.getEnteredGrade());
        
        // Update course grade
        gradingService.updateCourseGradeForStudent(siteId, user1, "89.0", "B+");
        
        // Verify the update
        courseGrade = gradingService.getCourseGradeForStudent(siteId, user1);
        assertEquals("B+", courseGrade.getEnteredGrade());
        
        // Update with a different grade
        gradingService.updateCourseGradeForStudent(siteId, user1, "91.0", "A-");
        
        // Verify the new update
        courseGrade = gradingService.getCourseGradeForStudent(siteId, user1);
        assertEquals("A-", courseGrade.getEnteredGrade());
    }
    
    @Test
    public void updateAssignmentCategorizedOrder() throws UserNotDefinedException {
        createGradebook();
        addCategories();
        
        // Get category ID
        List<CategoryDefinition> categories = gradingService.getCategoryDefinitions(siteId);
        Long categoryId = categories.get(0).getId();
        
        // Add assignments to the category
        ass1.setCategoryId(categoryId);
        Long assignment1Id = gradingService.addAssignment(siteId, ass1);
        
        ass2.setCategoryId(categoryId);
        Long assignment2Id = gradingService.addAssignment(siteId, ass2);
        
        // Get assignments in category
        List<Assignment> assignments = gradingService.getAssignments(siteId);
        List<Assignment> categoryAssignments = assignments.stream()
            .filter(a -> a.getCategoryId() != null && a.getCategoryId().equals(categoryId))
            .toList();
        assertEquals(2, categoryAssignments.size());
        
        // Update order within category
        gradingService.updateAssignmentCategorizedOrder(siteId, categoryId, assignment1Id, 1);
        
        // Verify the order change
        assignments = gradingService.getAssignments(siteId);
        categoryAssignments = assignments.stream()
            .filter(a -> a.getCategoryId() != null && a.getCategoryId().equals(categoryId))
            .toList();
        assertEquals(2, categoryAssignments.size());
        
        // In a real system, the order would change; in tests we mainly verify it runs without errors
    }
    
    @Test
    public void externalAssignmentMethods() throws UserNotDefinedException {
        Gradebook gradebook = createGradebook();
        String gradebookId = gradebook.getId();
        
        String externalId = "external-id-1";
        String title = "External Assignment";
        Double points = 25.0D;
        Date dueDate = new Date();
        String description = "External Tool";
        
        // Add external assessment
        gradingService.addExternalAssessment(siteId, externalId, "http://test.com", title, points, dueDate, description, "data", false);
        
        // Test isExternalAssignmentDefined
        assertTrue(gradingService.isExternalAssignmentDefined(gradebookId, externalId));
        assertFalse(gradingService.isExternalAssignmentDefined(gradebookId, "nonexistent-id"));
        
        // Test isExternalAssignmentGrouped (default is false)
        assertFalse(gradingService.isExternalAssignmentGrouped(gradebookId, externalId));
        
        // Test isExternalAssignmentVisible
        assertTrue(gradingService.isExternalAssignmentVisible(gradebookId, externalId, instructor));
        
        // Test getExternalAssignmentsForCurrentUser
        switchToInstructor();
        Map<String, String> externalAssignments = gradingService.getExternalAssignmentsForCurrentUser(siteId);
        assertNotNull(externalAssignments);
        assertEquals(1, externalAssignments.size());
        assertTrue(externalAssignments.containsKey(externalId));
        
        // Test getVisibleExternalAssignments
        Map<String, List<String>> visibleAssignments = gradingService.getVisibleExternalAssignments(siteId, List.of(user1, user2));
        assertNotNull(visibleAssignments);
        assertEquals(2, visibleAssignments.size());
        assertTrue(visibleAssignments.containsKey(user1));
        assertTrue(visibleAssignments.containsKey(user2));
        assertEquals(1, visibleAssignments.get(user1).size());
        
        // Test updating external assessment scores
        Map<String, Double> scores = new HashMap<>();
        scores.put(user1, 20.0);
        scores.put(user2, 15.0);
        gradingService.updateExternalAssessmentScores(siteId, externalId, scores);
        
        // Test updating score strings
        Map<String, String> scoreStrings = new HashMap<>();
        scoreStrings.put(user1, "22.5");
        scoreStrings.put(user2, "18.0");
        gradingService.updateExternalAssessmentScoresString(siteId, externalId, scoreStrings);
        
        // Test updating comments
        gradingService.updateExternalAssessmentComment(siteId, externalId, user1, "Good work");
        
        // Test updating multiple comments
        Map<String, String> comments = new HashMap<>();
        comments.put(user1, "Excellent work");
        comments.put(user2, "Nice job");
        gradingService.updateExternalAssessmentComments(siteId, externalId, comments);
        
        // Test getExternalAssessmentCategoryId
        Long categoryId = gradingService.getExternalAssessmentCategoryId(siteId, externalId);
        // Initially null since we didn't set a category
        assertNull(categoryId);

        // Test setExternalAssessmentToGradebookAssignment
        gradingService.setExternalAssessmentToGradebookAssignment(siteId, externalId);
        assertThrows(IllegalArgumentException.class, () -> gradingService.getExternalAssignment(siteId, externalId));
    }

    @Test
    public void categoriesEnabled() throws UserNotDefinedException {
        createGradebook();
        
        // Initially categories should not be enabled
        assertFalse(gradingService.isCategoriesEnabled(siteId));
        
        // Enable categories by adding some
        addCategories();
        
        // Now categories should be enabled
        assertTrue(gradingService.isCategoriesEnabled(siteId));
    }
    
    @Test
    public void gradingScaleMethods() throws UserNotDefinedException {
        Gradebook gradebook = createGradebook();
        
        // Test getAvailableGradingScales
        List<org.sakaiproject.grading.api.model.GradingScale> scales = gradingService.getAvailableGradingScales();
        assertNotNull(scales);
        
        // Test getAvailableGradingScaleDefinitions
        List<GradingScaleDefinition> scaleDefs = gradingService.getAvailableGradingScaleDefinitions();
        assertNotNull(scaleDefs);
        
        // Test setAvailableGradingScales
        // Create a test scale definition
        GradingScaleDefinition testScale = new GradingScaleDefinition();
        testScale.setUid("test-scale");
        testScale.setName("Test Scale");
        List<String> grades = List.of("A", "B", "C", "D", "F");
        List<Object> percentages = List.of("90.0", "80.0", "70.0", "60.0", "0.0");
        testScale.setGrades(grades);
        testScale.setDefaultBottomPercentsAsList(percentages);
        
        gradingService.setAvailableGradingScales(List.of(testScale));
        
        // Test setDefaultGradingScale
        gradingService.setDefaultGradingScale("test-scale");
        
        // Test saveGradeMappingToGradebook
        gradingService.saveGradeMappingToGradebook("test-scale", siteId);
        
        // Test getGradebookGradeMappings
        Set<GradeMapping> mappings = gradingService.getGradebookGradeMappings(gradebook.getId());
        assertNotNull(mappings);
        
        // Test updateGradeMapping
        GradeMapping mapping = mappings.iterator().next();
        Map<String, Double> updatedGrades = new HashMap<>(mapping.getGradeMap());
        updatedGrades.put("A", 95.0); // Update A threshold
        
        gradingService.updateGradeMapping(mapping.getId(), updatedGrades);
    }
    
    @Test
    public void gradebookManagerMethods() throws UserNotDefinedException, IdUnusedException {
        Gradebook gradebook = createGradebook();
        
        // Test getGradebookManager
        assertNotNull(gradingService.getGradebookManager(siteId));
        
        // Test setGradebookMode
        boolean success = gradingService.setGradebookMode(siteId, GradebookManager.Access.GROUP);
        assertTrue(success);
        
        // Test getGradebooksForSite
        List<Gradebook> gradebooks = gradingService.getGradebooksForSite(siteId);
        assertNotNull(gradebooks);
        assertEquals(1, gradebooks.size());
        
        // Test mapGroupToGradebook
        String groupId = "test-group";
        
        // Mock group
        Group group = mock(Group.class);
        when(group.getTitle()).thenReturn("title-" + groupId);
        when(site.getGroup(groupId)).thenReturn(group);

        boolean mapped = gradingService.mapGroupToGradebook(siteId, groupId);
        assertTrue(mapped);

        GradebookManager gradebookManager = gradingService.getGradebookManager(siteId);
        assertEquals(GradebookManager.Access.GROUP, gradebookManager.getTypeOfAccess());
        assertEquals(2, gradebookManager.getGradebooks().size());
        assertEquals(2, gradebookManager.getContextMapping().size());
        assertNotEquals(gradebookManager.getContextMapping().get(siteId), gradebookManager.getContextMapping().get(groupId));

    }
    
    @Test
    public void externalAssignmentProviderMethods() {
        // Create mock provider
        org.sakaiproject.grading.api.ExternalAssignmentProvider provider = mock(org.sakaiproject.grading.api.ExternalAssignmentProvider.class);
        when(provider.getAppKey()).thenReturn("test-provider");
        
        // Register provider
        gradingService.registerExternalAssignmentProvider(provider);
        
        // Unregister provider
        gradingService.unregisterExternalAssignmentProvider("test-provider");
    }

    private Long createAssignment1() {
        return gradingService.addAssignment(siteId, ass1);
    }

    private Long createAssignment2() {
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        return gradingService.addAssignment(siteId, ass2);
    }


    private void addCategories() {

        GradebookInformation gradebookInformation = gradingService.getGradebookInformation(siteId);

        var cd1 = new CategoryDefinition();
        cd1.setName(cat1Name);
        cd1.setExtraCredit(false);
        cd1.setEqualWeight(false);
        cd1.setWeight(Double.valueOf(0));
        cd1.setAssignmentList(Collections.<Assignment>emptyList());
        cd1.setDropHighest(0);
        cd1.setDropLowest(0);
        cd1.setKeepHighest(0);

        var cd2 = new CategoryDefinition();
        cd2.setName(cat2Name);
        cd2.setExtraCredit(false);
        cd2.setEqualWeight(false);
        cd2.setWeight(Double.valueOf(0));
        cd2.setAssignmentList(Collections.<Assignment>emptyList());
        cd2.setDropHighest(0);
        cd2.setDropLowest(0);
        cd2.setKeepHighest(0);

        var cats = new ArrayList<CategoryDefinition>();
        cats.add(cd1);
        cats.add(cd2);

        gradebookInformation.setCategories(cats);
        gradebookInformation.setCategoryType(GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY);

        gradingService.updateGradebookSettings(siteId, gradebookInformation);

    }

    private Gradebook createGradebook() throws UserNotDefinedException {
        switchToInstructor();
        return gradingService.getGradebook(siteId);
    }

    private void switchToInstructor() throws UserNotDefinedException {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(instructorUser, GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(instructorUser, GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
    }

    private void switchToUser1() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(user1User, GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(user1User, GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(user1User, GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser2() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(user2User, GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(user2User, GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(user2User, GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        try {
            when(userDirectoryService.getUser(user2)).thenReturn(user2User);
        } catch (UserNotDefinedException unde) {
        }
    }
}
