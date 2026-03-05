/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.webapi.controllers.test;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.controllers.GradesController;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class GradesControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Mock
    private AuthzGroupService authzGroupService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private GradingService gradingService;

    @Mock
    private PortalService portalService;

    @Mock
    private SecurityService securityService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private UserDirectoryService userDirectoryService;

    private AutoCloseable mocks;

    @Before
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);

        GradesController controller = new GradesController();
        controller.setSessionManager(sessionManager);
        controller.setSiteService(siteService);
        controller.setPortalService(portalService);

        ReflectionTestUtils.setField(controller, "authzGroupService", authzGroupService);
        ReflectionTestUtils.setField(controller, "entityManager", entityManager);
        ReflectionTestUtils.setField(controller, "gradingService", gradingService);
        ReflectionTestUtils.setField(controller, "securityService", securityService);
        ReflectionTestUtils.setField(controller, "userDirectoryService", userDirectoryService);

        Session session = org.mockito.Mockito.mock(Session.class);
        when(session.getUserId()).thenReturn("instructor1");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetSiteGradebookMatrix() throws Exception {
        String siteId = "site1";

        Site site = org.mockito.Mockito.mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));
        when(gradingService.currentUserHasGradingPerm(siteId)).thenReturn(true);

        Assignment assignment = new Assignment();
        assignment.setId(11L);
        assignment.setName("Essay 1");
        assignment.setPoints(100.0);
        assignment.setWeight(50.0);
        assignment.setReleased(true);
        assignment.setCounted(true);
        assignment.setCategoryId(2L);
        assignment.setCategoryName("Homework");

        when(gradingService.getAssignments(siteId, siteId, SortType.SORT_BY_SORTING)).thenReturn(List.of(assignment));

        CategoryDefinition category = new CategoryDefinition();
        category.setId(2L);
        category.setName("Homework");
        category.setWeight(40.0);
        category.setDropLowest(1);
        category.setDropKeepEnabled(true);
        when(gradingService.getCategoryDefinitions(siteId, siteId)).thenReturn(List.of(category));

        GradebookInformation info = new GradebookInformation();
        info.setGradeType(GradingConstants.GRADE_TYPE_POINTS);
        info.setCategoryType(GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY);
        info.setCourseGradeDisplayed(true);
        info.setCourseAverageDisplayed(true);
        info.setCoursePointsDisplayed(true);
        when(gradingService.getGradebookInformation(siteId, siteId)).thenReturn(info);

        when(gradingService.getViewableStudentsForItemForUser("instructor1", siteId, siteId, 11L))
                .thenReturn(Map.of("student1", "grade"));

        GradeDefinition gradeDefinition = new GradeDefinition();
        gradeDefinition.setStudentUid("student1");
        gradeDefinition.setGrade("95");
        gradeDefinition.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
        gradeDefinition.setDateRecorded(new Date(1700000000000L));
        gradeDefinition.setGradeReleased(true);

        when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList()))
                .thenReturn(Map.of(11L, List.of(gradeDefinition)));

        CourseGradeTransferBean courseGrade = new CourseGradeTransferBean();
        courseGrade.setCalculatedGrade("95.0");
        courseGrade.setMappedGrade("A");
        courseGrade.setPointsEarned(95.0);
        courseGrade.setTotalPointsPossible(100.0);
        when(gradingService.getCourseGradeForStudents(eq(siteId), eq(siteId), anyList()))
                .thenReturn(Map.of("student1", courseGrade));

        User student = org.mockito.Mockito.mock(User.class);
        when(student.getId()).thenReturn("student1");
        when(student.getEid()).thenReturn("s1");
        when(student.getDisplayId()).thenReturn("s1");
        when(student.getDisplayName()).thenReturn("Student One");
        when(student.getFirstName()).thenReturn("Student");
        when(student.getLastName()).thenReturn("One");
        when(userDirectoryService.getUser("student1")).thenReturn(student);

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId", is(siteId)))
                .andExpect(jsonPath("$.gradebookUid", is(siteId)))
                .andExpect(jsonPath("$.settings.gradeType", is(1)))
                .andExpect(jsonPath("$.categories[0].weight", is(40.0)))
                .andExpect(jsonPath("$.columns[0].points", is(100.0)))
                .andExpect(jsonPath("$.students[0].userId", is("student1")))
                .andExpect(jsonPath("$.students[0].courseGrade.calculatedGrade", is("95.0")))
                .andExpect(jsonPath("$.students[0].grades['11'].grade", is("95")))
                .andDo(document("get-site-gradebook-matrix", preprocessor));

        verify(gradingService).getGradesWithoutCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList());
        verify(gradingService, never()).getGradesWithCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList());
    }

    @Test
    public void testGetSiteGradebookMatrixIncludeComments() throws Exception {
        String siteId = "site1";

        Site site = org.mockito.Mockito.mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));
        when(gradingService.currentUserHasGradingPerm(siteId)).thenReturn(true);

        Assignment assignment = buildAssignment();
        when(gradingService.getAssignments(siteId, siteId, SortType.SORT_BY_SORTING)).thenReturn(List.of(assignment));
        when(gradingService.getCategoryDefinitions(siteId, siteId)).thenReturn(List.of(buildCategory()));
        when(gradingService.getGradebookInformation(siteId, siteId)).thenReturn(buildGradebookInformation());
        when(gradingService.getViewableStudentsForItemForUser("instructor1", siteId, siteId, 11L))
                .thenReturn(Map.of("student1", "grade"));

        GradeDefinition gradeDefinition = buildGradeDefinition("student1", "95");
        gradeDefinition.setGradeComment("Great work");
        when(gradingService.getGradesWithCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList()))
                .thenReturn(Map.of(11L, List.of(gradeDefinition)));

        when(gradingService.getCourseGradeForStudents(eq(siteId), eq(siteId), anyList()))
                .thenReturn(Map.of("student1", buildCourseGrade()));
        User student = buildStudentUser();
        when(userDirectoryService.getUser("student1")).thenReturn(student);

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook").param("includeComments", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId", is(siteId)))
                .andExpect(jsonPath("$.gradebookUid", is(siteId)))
                .andExpect(jsonPath("$.students[0].grades['11'].grade", is("95")))
                .andExpect(jsonPath("$.students[0].grades['11'].gradeComment", is("Great work")));

        verify(gradingService).getGradesWithCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList());
        verify(gradingService, never()).getGradesWithoutCommentsForStudentsForItems(eq(siteId), eq(siteId), anyList(), anyList());
    }

    @Test
    public void testGetSiteGradebookMatrixForGradebookUid() throws Exception {
        String siteId = "site1";
        String gradebookUid = "group1";

        Site site = org.mockito.Mockito.mock(Site.class);
        Group group = org.mockito.Mockito.mock(Group.class);
        when(site.getId()).thenReturn(siteId);
        when(site.getGroup(gradebookUid)).thenReturn(group);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));
        when(gradingService.currentUserHasGradingPerm(siteId)).thenReturn(true);

        Assignment assignment = buildAssignment();
        when(gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_SORTING)).thenReturn(List.of(assignment));
        when(gradingService.getCategoryDefinitions(gradebookUid, siteId)).thenReturn(List.of(buildCategory()));
        when(gradingService.getGradebookInformation(gradebookUid, siteId)).thenReturn(buildGradebookInformation());
        when(gradingService.getViewableStudentsForItemForUser("instructor1", gradebookUid, siteId, 11L))
                .thenReturn(Map.of("student1", "grade"));

        GradeDefinition gradeDefinition = buildGradeDefinition("student1", "95");
        when(gradingService.getGradesWithoutCommentsForStudentsForItems(eq(gradebookUid), eq(siteId), anyList(), anyList()))
                .thenReturn(Map.of(11L, List.of(gradeDefinition)));
        when(gradingService.getCourseGradeForStudents(eq(gradebookUid), eq(siteId), anyList()))
                .thenReturn(Map.of("student1", buildCourseGrade()));
        User student = buildStudentUser();
        when(userDirectoryService.getUser("student1")).thenReturn(student);

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook/" + gradebookUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId", is(siteId)))
                .andExpect(jsonPath("$.gradebookUid", is(gradebookUid)))
                .andExpect(jsonPath("$.settings.gradeType", is(1)))
                .andExpect(jsonPath("$.categories[0].weight", is(40.0)))
                .andExpect(jsonPath("$.columns[0].points", is(100.0)))
                .andExpect(jsonPath("$.students[0].userId", is("student1")))
                .andExpect(jsonPath("$.students[0].courseGrade.calculatedGrade", is("95.0")))
                .andExpect(jsonPath("$.students[0].grades['11'].grade", is("95")));

        verify(gradingService).getAssignments(gradebookUid, siteId, SortType.SORT_BY_SORTING);
        verify(gradingService).getGradesWithoutCommentsForStudentsForItems(eq(gradebookUid), eq(siteId), anyList(), anyList());
        verify(gradingService, never()).getGradesWithCommentsForStudentsForItems(eq(gradebookUid), eq(siteId), anyList(), anyList());
    }

    @Test
    public void testGetSiteGradebookMatrixBadRequestForUnknownGradebookUid() throws Exception {
        String siteId = "site1";
        String gradebookUid = "missing-group";

        Site site = org.mockito.Mockito.mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(site.getGroup(gradebookUid)).thenReturn(null);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook/" + gradebookUid))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetSiteGradebookMatrixUsesGradebookRealmForFallbackStudents() throws Exception {
        String siteId = "site1";
        String gradebookUid = "group1";
        String siteReference = "/site/site1";
        String groupReference = "/site/site1/group/group1";

        Site site = org.mockito.Mockito.mock(Site.class);
        Group group = org.mockito.Mockito.mock(Group.class);
        when(site.getId()).thenReturn(siteId);
        when(site.getReference()).thenReturn(siteReference);
        when(site.getGroup(gradebookUid)).thenReturn(group);
        when(group.getReference()).thenReturn(groupReference);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));
        when(gradingService.currentUserHasGradingPerm(siteId)).thenReturn(true);
        when(gradingService.currentUserHasGradeAllPerm(siteId)).thenReturn(true);

        Assignment assignment = buildAssignment();
        when(gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_SORTING)).thenReturn(List.of(assignment));
        when(gradingService.getCategoryDefinitions(gradebookUid, siteId)).thenReturn(List.of());
        when(gradingService.getGradebookInformation(gradebookUid, siteId)).thenReturn(new GradebookInformation());
        when(gradingService.getViewableStudentsForItemForUser("instructor1", gradebookUid, siteId, 11L))
                .thenReturn(Map.of());

        AuthzGroup gradebookRealm = org.mockito.Mockito.mock(AuthzGroup.class);
        when(gradebookRealm.getRoles()).thenReturn(java.util.Collections.emptySet());
        when(authzGroupService.getAuthzGroup(groupReference)).thenReturn(gradebookRealm);

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook/" + gradebookUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students.length()", is(0)));

        verify(authzGroupService).getAuthzGroup(groupReference);
        verify(authzGroupService, never()).getAuthzGroup(siteReference);
    }

    @Test
    public void testGetSiteGradebookMatrixForbiddenWithoutGradePermission() throws Exception {
        String siteId = "site1";

        Site site = org.mockito.Mockito.mock(Site.class);
        when(site.getId()).thenReturn(siteId);
        when(siteService.getOptionalSite(siteId)).thenReturn(Optional.of(site));
        when(gradingService.currentUserHasGradingPerm(siteId)).thenReturn(false);

        mockMvc.perform(get("/sites/" + siteId + "/grading/full-gradebook"))
                .andExpect(status().isForbidden());
    }

    private Assignment buildAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(11L);
        assignment.setName("Essay 1");
        assignment.setPoints(100.0);
        assignment.setWeight(50.0);
        assignment.setReleased(true);
        assignment.setCounted(true);
        assignment.setCategoryId(2L);
        assignment.setCategoryName("Homework");
        return assignment;
    }

    private CategoryDefinition buildCategory() {
        CategoryDefinition category = new CategoryDefinition();
        category.setId(2L);
        category.setName("Homework");
        category.setWeight(40.0);
        category.setDropLowest(1);
        category.setDropKeepEnabled(true);
        return category;
    }

    private GradebookInformation buildGradebookInformation() {
        GradebookInformation info = new GradebookInformation();
        info.setGradeType(GradingConstants.GRADE_TYPE_POINTS);
        info.setCategoryType(GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY);
        info.setCourseGradeDisplayed(true);
        info.setCourseAverageDisplayed(true);
        info.setCoursePointsDisplayed(true);
        return info;
    }

    private GradeDefinition buildGradeDefinition(String studentId, String grade) {
        GradeDefinition gradeDefinition = new GradeDefinition();
        gradeDefinition.setStudentUid(studentId);
        gradeDefinition.setGrade(grade);
        gradeDefinition.setGradeEntryType(GradingConstants.GRADE_TYPE_POINTS);
        gradeDefinition.setDateRecorded(new Date(1700000000000L));
        gradeDefinition.setGradeReleased(true);
        return gradeDefinition;
    }

    private CourseGradeTransferBean buildCourseGrade() {
        CourseGradeTransferBean courseGrade = new CourseGradeTransferBean();
        courseGrade.setCalculatedGrade("95.0");
        courseGrade.setMappedGrade("A");
        courseGrade.setPointsEarned(95.0);
        courseGrade.setTotalPointsPossible(100.0);
        return courseGrade;
    }

    private User buildStudentUser() {
        User student = org.mockito.Mockito.mock(User.class);
        when(student.getId()).thenReturn("student1");
        when(student.getEid()).thenReturn("s1");
        when(student.getDisplayId()).thenReturn("s1");
        when(student.getDisplayName()).thenReturn("Student One");
        when(student.getFirstName()).thenReturn("Student");
        when(student.getLastName()).thenReturn("One");
        return student;
    }
}
