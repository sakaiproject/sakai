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

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.repository.CourseGradeRepository;
import org.sakaiproject.grading.api.repository.LetterGradePercentMappingRepository;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.impl.GradingServiceImpl;
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
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private PlusService plusService;
    @Autowired private UserDirectoryService userDirectoryService;

    private ResourceLoader resourceLoader;

    String instructor = "instructor";
    User instructorUser = null;
    String user1 = "user1";
    User user1User = null;
    String user2 = "user2";
    User user2User = null;

    String siteId = "xyz";

    String cat1Name = "Category One";
    String cat2Name = "Category Two";

    Double ass1Points = 15D;
    String ass1Name = "Assignment One";

    Double ass2Points = 7D;
    String ass2Name = "Assignment Two";

    Assignment ass1 = null;
    Assignment ass2 = null;

    @Before
    public void setup() {

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

        user2User = mock(User.class);
        when(user2User.getDisplayName()).thenReturn(user2);

        when(siteService.siteReference(siteId)).thenReturn("/site/" + siteId);

        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        ((GradingServiceImpl) AopTestUtils.getTargetObject(gradingService)).setResourceLoader(resourceLoader);
    }

    @Test
    public void addGradebook() {

        Gradebook gradebook = createGradebook();
        assertEquals(siteId, gradebook.getUid());
        assertEquals(siteId, gradebook.getName());

        List<CourseGrade> courseGrades = courseGradeRepository.findByGradebook_Id(gradebook.getId());
        assertEquals(1, courseGrades.size());

        List<LetterGradePercentMapping> mappings = letterGradePercentMappingRepository.findByMappingType(1);
        assertEquals(1, mappings.size());
    }

    @Test
    public void deleteGradebook() {

        Gradebook gradebook = createGradebook();
        //try {
            gradingService.deleteGradebook(gradebook.getUid());
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
    }

    @Test
    public void addAssignment() {

        Gradebook gradebook = createGradebook();
        assertEquals(siteId, gradebook.getUid());

        switchToInstructor();

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);

        assertThrows(GradingSecurityException.class, () -> gradingService.addAssignment(gradebook.getUid(), siteId, ass1));

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        //when(siteService.siteReference(gradebook.getUid())).thenReturn("/site/" + gradebook.getUid());

        gradingService.addAssignment(gradebook.getUid(), siteId, ass1);

        List<Assignment> assignments = gradingService.getAssignments(gradebook.getUid(), siteId, SortType.SORT_BY_SORTING);
        assertEquals(1, assignments.size());
        assertEquals(ass1Name, assignments.get(0).getName());
        assertEquals(ass1Points, assignments.get(0).getPoints());
        assertFalse(assignments.get(0).getUngraded());
    }

    @Test
    public void getAssignments() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);
        List<Assignment> assignments = gradingService.getAssignments(gradebook.getUid(), siteId, SortType.SORT_BY_SORTING);
        assertEquals(1, assignments.size());
        Long id2 = createAssignment2(gradebook);
        assignments = gradingService.getAssignments(gradebook.getUid(), siteId, SortType.SORT_BY_SORTING);
        assertEquals(2, assignments.size());
    }

    @Test
    public void addAndUpdateExternalAssessment() {

        Gradebook gradebook = createGradebook();
        switchToUser1();

        String externalId = "bf3eeca2-1b97-4ead-b605-a8b50a0c6950";
        String title = "External One";
        Double points = 55.3D;
        Date dueDate = new Date();
        String description = "The Sakai assignments tool";

        assertThrows(GradingSecurityException.class,
            () -> gradingService.addExternalAssessment("none", "nothing", externalId, "http://eggs.com",
                title, points, dueDate, description, "data", false, null, null));

        switchToInstructor();

        gradingService.addExternalAssessment(gradebook.getUid(), gradebook.getUid(), externalId, "http://eggs.com", title, points, dueDate, description, "data", false, null, null);
        Assignment assignment = gradingService.getExternalAssignment(gradebook.getUid(), externalId);//TODO mi caso?

        assertEquals(title, assignment.getName());
        assertEquals(points, assignment.getPoints());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(description, assignment.getExternalAppName());
        assertTrue(assignment.getExternallyMaintained());

        String newTitle = "New Title";
        Double newPoints = 23.2D;

        gradingService.updateExternalAssessment(gradebook.getUid(), externalId, "http://eggs.com", "data", newTitle, null, newPoints, assignment.getDueDate(), null);

        assignment = gradingService.getExternalAssignment(gradebook.getUid(), externalId);
        assertEquals(newTitle, assignment.getName());
        assertEquals(newPoints, assignment.getPoints());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(description, assignment.getExternalAppName());
        assertTrue(assignment.getExternallyMaintained());
    }

    @Test
    public void currentUserHasGradingPerm() {

        Gradebook gradebook = createGradebook();
        switchToUser1();
        assertFalse(gradingService.currentUserHasGradingPerm(gradebook.getUid()));
        switchToInstructor();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        assertTrue(gradingService.currentUserHasGradingPerm(gradebook.getUid()));
    }

    @Test
    public void removeAssignment() {

        Gradebook gradebook = createGradebook();

        Long id = createAssignment1(gradebook);

        gradingService.removeAssignment(id);
        assertEquals(0, gradingService.getAssignments(gradebook.getUid(), siteId, SortType.SORT_BY_SORTING).size());
    }

    @Test
    public void removeExternalAssignment() {

        Gradebook gradebook = createGradebook();
        String externalId = "xyz";
        String externalUrl = "http://xyz.com";
        String title = "External Assignment";
        Double points = 32.0D;
        Date dueDate = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));
        String externalServiceDescription = "test";

        switchToInstructor();

        gradingService.addExternalAssessment(gradebook.getUid(), gradebook.getUid(), externalId, externalUrl,
                title, points, dueDate, externalServiceDescription, null, false, null, null);

        Assignment assignment = gradingService.getExternalAssignment(gradebook.getUid(), externalId);

        assertNotNull(assignment);

        gradingService.removeExternalAssignment(gradebook.getUid(), externalId, null);

        assertThrows(IllegalArgumentException.class, () -> gradingService.getExternalAssignment(gradebook.getUid(), externalId));
    }

    @Test
    public void updateAssignment() {

        Gradebook gradebook = createGradebook();

        Long id = createAssignment1(gradebook);
        Assignment updated = gradingService.getAssignment(gradebook.getUid(), siteId, id);
        updated.setExternallyMaintained(true);
        updated.setName("Changed Name");
        updated.setPoints(80D);
        updated.setDueDate(new Date());
        gradingService.updateAssignment(gradebook.getUid(), siteId, id, updated);
        updated = gradingService.getAssignment(gradebook.getUid(), siteId, id);
        // You can't change the name, points or due date of externally maintained assignments
        assertEquals(ass1Name, updated.getName());
        // You can't change the points of externally maintained assignments
        assertEquals(ass1.getPoints(), updated.getPoints());
        assertEquals(ass1.getDueDate(), updated.getDueDate());
    }

    @Test
    public void isAssignmentDefined() {

        Gradebook gradebook = createGradebook();
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + gradebook.getUid())).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(false);
        assertThrows(GradingSecurityException.class, () -> gradingService.isAssignmentDefined(gradebook.getUid(), siteId, ass1Name));
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + gradebook.getUid())).thenReturn(true);
        assertFalse(gradingService.isAssignmentDefined(gradebook.getUid(), siteId, ass1Name));
        Long id = createAssignment1(gradebook);
        assertTrue(gradingService.isAssignmentDefined(gradebook.getUid(), siteId, ass1Name));
    }

    @Test
    public void setAssignmentScoreString() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);

        gradingService.setAssignmentScoreString(gradebook.getUid(), siteId, id, user1, "43.0", "");
        assertEquals("43", gradingService.getAssignmentScoreString(gradebook.getUid(), siteId, id, user1));

        gradingService.setAssignmentScoreString(gradebook.getUid(), siteId, id, user1, "27.5", "");
        assertEquals("27.5", gradingService.getAssignmentScoreString(gradebook.getUid(), siteId, id, user1));
    }

    @Test
    public void saveGradesAndComments() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);

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

        gradingService.saveGradesAndComments(gradebook.getUid(), siteId, id, List.<GradeDefinition>of(def1, def2));

        List<GradingEvent> gradingEvents = gradingService.getGradingEvents(user1, id);
        assertEquals(1, gradingEvents.size());

        gradingEvents = gradingService.getGradingEvents(user2, id);
        assertEquals(1, gradingEvents.size());
    }

    @Test
    public void getAverageCourseGrade() {

        Gradebook gradebook = createGradebook();
        String average = gradingService.getAverageCourseGrade(gradebook.getUid(), siteId);
        assertNull(average);

        Long assId = createAssignment1(gradebook);

        String grade = "3.7";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(gradebook.getUid(), siteId, assId, user1, grade, comment);

        average = gradingService.getAverageCourseGrade(gradebook.getUid(), siteId);
        //assertEquals("3.7", average);
    }

    @Test
    public void createGradebookWithCategories() {

        switchToInstructor();

        gradingService.addGradebook(siteId, siteId);
        Gradebook gradebook = gradingService.getGradebook(siteId, siteId);

        addCategories(gradebook);

        GradebookInformation gradebookInformation = gradingService.getGradebookInformation(siteId, siteId);

        List<CategoryDefinition> categories = gradebookInformation.getCategories();
        assertEquals(2, categories.size());
    }

    @Test
    public void assignmentScoreComment() {

        assertThrows(IllegalArgumentException.class, () -> gradingService.setAssignmentScoreComment(null, null, user1, "Great!"));

        Gradebook gradebook = createGradebook();

        Long ass1Id = createAssignment1(gradebook);

        String comment = "Great!";

        gradingService.setAssignmentScoreComment(gradebook.getUid(), ass1Id, user1, comment);

        CommentDefinition commentDefinition = gradingService.getAssignmentScoreComment(gradebook.getUid(), ass1Id, user1);
        assertEquals(comment, commentDefinition.getCommentText());

        gradingService.deleteAssignmentScoreComment(gradebook.getUid(), ass1Id, user1);
        commentDefinition = gradingService.getAssignmentScoreComment(gradebook.getUid(), ass1Id, user1);
        assertNull(commentDefinition);
    }

    @Test
    public void courseGradeComment() {
        Gradebook gradebook = createGradebook();
        String comment = "This is your course grade.";

        gradingService.setAssignmentScoreComment(gradebook.getUid(), gradingService.getCourseGradeId(gradebook.getId()), user1, comment);
        CommentDefinition commentDefinition = gradingService.getAssignmentScoreComment(gradebook.getUid(), gradingService.getCourseGradeId(gradebook.getId()), user1);
        assertEquals(comment, commentDefinition.getCommentText());

        gradingService.deleteAssignmentScoreComment(gradebook.getUid(), gradingService.getCourseGradeId(gradebook.getId()), user1);
        commentDefinition = gradingService.getAssignmentScoreComment(gradebook.getUid(), gradingService.getCourseGradeId(gradebook.getId()), user1);
        assertNull(commentDefinition);
    }

    @Test
    public void getAssignmentByNameOrId() {

        Gradebook gradebook = createGradebook();

        Assignment ass = gradingService.getAssignmentByNameOrId(gradebook.getUid(), siteId, "none");
        assertNull(ass);

        Long ass1Id = createAssignment1(gradebook);

        ass = gradingService.getAssignmentByNameOrId(gradebook.getUid(), siteId, ass1Id.toString());
        assertNotNull(ass);
    }

    @Test
    public void getCategoryDefinitions() {

        switchToInstructor();

        gradingService.addGradebook(siteId, siteId);
        Gradebook gradebook = gradingService.getGradebook(siteId, siteId);
        List<CategoryDefinition> cats = gradingService.getCategoryDefinitions(gradebook.getUid(), siteId);
        assertEquals(0, cats.size());

        addCategories(gradebook);

        cats = gradingService.getCategoryDefinitions(gradebook.getUid(), siteId);
        assertEquals(2, cats.size());
    }

    @Test
    public void getViewableAssignmentsForCurrentUser() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);
        List<Assignment> assignments = gradingService.getViewableAssignmentsForCurrentUser(gradebook.getUid(), siteId, SortType.SORT_BY_SORTING);
        assertEquals(1, assignments.size());
    }
/*
    @Test
    public void isUserAbleToGradeItemForStudent() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);
        switchToUser1();
        assertFalse(gradingService.isUserAbleToGradeItemForStudent(gradebook.getUid(), siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(true);
        assertTrue(gradingService.isUserAbleToGradeItemForStudent(gradebook.getUid(), siteId, id, user2));
    }

    @Test
    public void isUserAbleToViewItemForStudent() {

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(false);
        assertFalse(gradingService.isUserAbleToViewItemForStudent(gradebook.getUid(), siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(true);
        assertTrue(gradingService.isUserAbleToViewItemForStudent(gradebook.getUid(), siteId,  id, user2));

        switchToUser1();
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(false);
        assertFalse(gradingService.isUserAbleToViewItemForStudent(gradebook.getUid(), siteId, id, user2));
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + gradebook.getUid())).thenReturn(true);
        assertTrue(gradingService.isUserAbleToViewItemForStudent(gradebook.getUid(), siteId, id, user2));
    }
*/
    @Test
    public void getGradeViewFunctionForUserForStudentForItem() {

        assertThrows(IllegalArgumentException.class, () -> gradingService.getGradeViewFunctionForUserForStudentForItem(null, null, null, null));

        Gradebook gradebook = createGradebook();
        Long id = createAssignment1(gradebook);

        String perm = gradingService.getGradeViewFunctionForUserForStudentForItem(gradebook.getUid(), siteId, id, user1);

        assertEquals(GradingConstants.gradePermission, perm);
        
        switchToUser1();

        perm = gradingService.getGradeViewFunctionForUserForStudentForItem(gradebook.getUid(), siteId, id, user1);

        assertEquals(null, perm);
    }

    @Test
    public void getPointsEarnedCourseGradeRecords() {
    }

    @Test
    public void saveGradeAndCommentForStudent() {

        Gradebook gradebook = createGradebook();
        Long assId = createAssignment1(gradebook);

        String grade = "3.7";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(gradebook.getUid(), siteId, assId, user1, grade, comment);

        Site site = mock(Site.class);
        when(site.getGroup(user1)).thenReturn(null);
        try {
            when(siteService.getSite(gradebook.getUid())).thenReturn(site);
        } catch (Exception e) {
        }

        GradeDefinition gradeDef = gradingService.getGradeDefinitionForStudentForItem(gradebook.getUid(), siteId, assId, user1);

        assertEquals(grade, gradeDef.getGrade());
        assertEquals(comment, gradeDef.getGradeComment());
    }

    @Test
    public void getCourseGradeForStudents() {

        Gradebook gradebook = createGradebook();
        Long assId = createAssignment1(gradebook);

        Map<String, Double> gradeMapping = new HashMap<>();
        gradeMapping.put(user1, 3.0D);

        Map<String, CourseGradeTransferBean> grades = gradingService.getCourseGradeForStudents(gradebook.getUid(), siteId, Arrays.asList(user1), gradeMapping);
        assertEquals(1, grades.size());

        String grade = "3.0";
        String comment = "Rather shoddy";

        gradingService.saveGradeAndCommentForStudent(gradebook.getUid(), siteId, assId, user1, grade, comment);
        grades = gradingService.getCourseGradeForStudents(gradebook.getUid(), siteId, Arrays.asList(user1), gradeMapping);
        assertEquals(1, grades.size());
        assertEquals("20.0", grades.get(user1).getCalculatedGrade());
    }

    @Test
    public void getGradesWithoutCommentsForStudentsForItems() {

        Gradebook gradebook = createGradebook();
        Long assId = createAssignment1(gradebook);

        String grade = "3.7";
        String comment = "Rather shoddy";

        Map<String, Double> gradeMapping = new HashMap<>();
        gradeMapping.put(user1, 3.7D);

        gradingService.saveGradeAndCommentForStudent(gradebook.getUid(), gradebook.getUid(), assId, user1, grade, comment);

        assertThrows(IllegalArgumentException.class, () -> gradingService.getGradesWithoutCommentsForStudentsForItems(gradebook.getUid(), gradebook.getUid(), null, null));

        switchToUser2();
        assertThrows(GradingSecurityException.class, () -> gradingService.getGradesWithoutCommentsForStudentsForItems(gradebook.getUid(), gradebook.getUid(), List.of(assId), List.of(user1)));

        switchToUser1(); // user1 should be able to view their own grades
        Map<Long, List<GradeDefinition>> user1Grades = gradingService.getGradesWithoutCommentsForStudentsForItems(gradebook.getUid(), gradebook.getUid(), List.of(assId), List.of(user1));
        assertEquals(1, user1Grades.size());
        assertEquals(1, user1Grades.get(assId).size());
        assertEquals(user1, user1Grades.get(assId).get(0).getStudentUid());

        switchToInstructor();
        Map<Long, List<GradeDefinition>> gradeMap = gradingService.getGradesWithoutCommentsForStudentsForItems(gradebook.getUid(), gradebook.getUid(), List.of(assId), List.of(user1));

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

    private Long createAssignment1(Gradebook gradebook) {

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + gradebook.getUid())).thenReturn(true);
        //when(siteService.siteReference(gradebook.getUid())).thenReturn("/site/" + gradebook.getUid());

        return gradingService.addAssignment(gradebook.getUid(), siteId, ass1);
    }

    private Long createAssignment2(Gradebook gradebook) {

        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + gradebook.getUid())).thenReturn(true);
        //when(siteService.siteReference(gradebook.getUid())).thenReturn("/site/" + gradebook.getUid());

        return gradingService.addAssignment(gradebook.getUid(), siteId, ass2);
    }


    private void addCategories(Gradebook gradebook) {

        GradebookInformation gradebookInformation = gradingService.getGradebookInformation(gradebook.getUid(), siteId);

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

        gradingService.updateGradebookSettings(gradebook.getUid(), siteId, gradebookInformation);

    }

    private Gradebook createGradebook() {

        switchToInstructor();

        return gradingService.addGradebook(siteId, siteId);
    }

    private void switchToInstructor() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(instructor);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(true);
        try {
            when(userDirectoryService.getUser(instructor)).thenReturn(instructorUser);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser1() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1);
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        try {
            when(userDirectoryService.getUser(user1)).thenReturn(user1User);
        } catch (UserNotDefinedException unde) {
        }
    }

    private void switchToUser2() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user2);
        when(securityService.unlock(GradingAuthz.PERMISSION_VIEW_OWN_GRADES, "/site/" + siteId)).thenReturn(true);
        when(securityService.unlock(GradingAuthz.PERMISSION_EDIT_ASSIGNMENTS, "/site/" + siteId)).thenReturn(false);
        when(securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, "/site/" + siteId)).thenReturn(false);
        try {
            when(userDirectoryService.getUser(user2)).thenReturn(user2User);
        } catch (UserNotDefinedException unde) {
        }
    }
}
