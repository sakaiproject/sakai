/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.is;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.GradesController;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class GradesControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Mock
    private AuthzGroupService authzGroupService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private GradingService gradingService;

    @Mock
    protected PortalService portalService;

    @Mock
    private SecurityService securityService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    private AutoCloseable mocks;

    private String site1Id = "site1";
    private String site1Title = "Site 1";
    private Site site1 = mock(Site.class);
    private String site2Id = "site2";
    private String site2Title = "Site 2";
    private Site site2 = mock(Site.class);
    private String user1Id = "user1";
    private String user2Id = "user2";
    private String group1Id = "group1";
    private String group1Title = "Group 1";
    private Group group1 = mock(Group.class);
    private String group2Id = "group2";
    private String group2Title = "Group 2";
    private Group group2 = mock(Group.class);
    private Long ass1Id = 1L;
    private String ass1Name = "Assignment 1";
    private Assignment ass1 = new Assignment();
    private Long ass2Id = 2L;
    private String ass2Name = "Assignment 1";
    private Assignment ass2 = new Assignment();

    @Before
    public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        reset(gradingService);

        var controller = new GradesController();

        controller.setAuthzGroupService(authzGroupService);
        controller.setEntityManager(entityManager);
        controller.setGradingService(gradingService);
        controller.setPortalService(portalService);
        controller.setSecurityService(securityService);
        controller.setSiteService(siteService);

        var session = mock(Session.class);
        when(session.getUserId()).thenReturn(user1Id);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        controller.setSessionManager(sessionManager);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).apply(configurer).build();

        // Setup some mock data
        when(portalService.getPinnedSites()).thenReturn(List.of(site1Id, site2Id));
        when(portalService.getPinnedSites(user1Id)).thenReturn(List.of(site1Id, site2Id));

        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getOptionalSite(site1Id)).thenReturn(Optional.of(site1));
        try {
            when(siteService.getSite(site1Id)).thenReturn(site1);
        } catch (Exception e) {
        }

        when(site2.getTitle()).thenReturn(site2Title);
        when(siteService.getOptionalSite(site2Id)).thenReturn(Optional.of(site2));
        try {
            when(siteService.getSite(site2Id)).thenReturn(site2);
        } catch (Exception e) {
        }

        when(group1.getId()).thenReturn(group1Id);
        when(group1.getTitle()).thenReturn(group1Title);

        when(group2.getId()).thenReturn(group2Id);
        when(group2.getTitle()).thenReturn(group2Title);

        when(site1.getGroups()).thenReturn(List.of(group1, group2));

        ass1 = new Assignment();
        ass1.setId(ass1Id);
        ass1.setName(ass1Name);
        ass1.setReference("/assignments/" + ass1Id);
        ass1.setPoints(87D);

        ass2 = new Assignment();
        ass2.setId(ass2Id);
        ass2.setName(ass2Name);
        ass2.setReference("/assignments/" + ass2Id);
        ass2.setPoints(44D);
    }

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testStudentGetUserGrades() throws Exception {

        ass1.setContext(site1Id);
        ass1.setReleased(true);

        ass2.setContext(site2Id);
        ass2.setReleased(true);

        when(gradingService.getViewableAssignmentsForCurrentUser(site1Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass1));
        when(gradingService.getViewableAssignmentsForCurrentUser(site2Id, site2Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass2));

        GradeDefinition grade1 = mock(GradeDefinition.class);
        when(grade1.getGrade()).thenReturn("33");
        GradeDefinition grade2 = mock(GradeDefinition.class);
        when(grade2.getGrade()).thenReturn("44");

        Map<Long, List<GradeDefinition>> grades = Map.of(ass1Id, List.of(grade1));
        when(gradingService.getGradesWithoutCommentsForStudentsForItems(site1Id, site1Id, List.of(ass1Id), List.of(user1Id))).thenReturn(grades);
        grades = Map.of(ass2Id, List.of(grade2));
        when(gradingService.getGradesWithoutCommentsForStudentsForItems(site2Id, site2Id, List.of(ass2Id), List.of(user1Id))).thenReturn(grades);

        mockMvc.perform(get("/users/me/grades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sites[0].siteId", is(site1Id)))
            .andExpect(jsonPath("$.sites[0].title", is(site1Title)))
            .andExpect(jsonPath("$.sites[1].siteId", is(site2Id)))
            .andExpect(jsonPath("$.sites[1].title", is(site2Title)))
            .andExpect(jsonPath("$.grades[0].name", is(ass1Name)))
            .andExpect(jsonPath("$.grades[0].siteId", is(site1Id)))
            .andExpect(jsonPath("$.grades[0].siteTitle", is(site1Title)))
            .andExpect(jsonPath("$.grades[0].score", is("33")))
            .andExpect(jsonPath("$.grades[0].notGradedYet", is(false)))
            .andExpect(jsonPath("$.grades[0].canGrade", is(false)))
            .andExpect(jsonPath("$.grades[1].name", is(ass2Name)))
            .andExpect(jsonPath("$.grades[1].siteId", is(site2Id)))
            .andExpect(jsonPath("$.grades[1].siteTitle", is(site2Title)))
            .andExpect(jsonPath("$.grades[1].score", is("44")))
            .andExpect(jsonPath("$.grades[1].notGradedYet", is(false)))
            .andExpect(jsonPath("$.grades[1].canGrade", is(false)))
            .andDo(document("get-user-grades"));
    }

    @Test
    public void testGetSiteGrades() throws Exception {

        ass1.setReleased(true);
        ass1.setContext(site1Id);

        when(gradingService.getViewableAssignmentsForCurrentUser(site1Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass1));

        GradeDefinition grade1 = mock(GradeDefinition.class);
        when(grade1.getGrade()).thenReturn("33");
        GradeDefinition grade2 = mock(GradeDefinition.class);
        when(grade2.getGrade()).thenReturn("44");

        Map<Long, List<GradeDefinition>> grades = Map.of(ass1Id, List.of(grade1, grade2));
        when(gradingService.getGradesWithoutCommentsForStudentsForItems(site1Id, site1Id, List.of(ass1Id), List.of(user1Id))).thenReturn(grades);

        mockMvc.perform(get("/sites/" + site1Id + "/grades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.grades[0].name", is(ass1Name)))
            .andExpect(jsonPath("$.grades[0].siteId", is(site1Id)))
            .andExpect(jsonPath("$.grades[0].siteTitle", is(site1Title)))
            .andExpect(jsonPath("$.grades[0].score", is("33")))
            .andExpect(jsonPath("$.grades[0].notGradedYet", is(false)))
            .andExpect(jsonPath("$.grades[0].canGrade", is(false)))
            .andDo(document("get-site-grades"));
    }

    @Test
    public void testGetItemData() throws Exception {

        Long cat1Id = 1L;
        String cat1Name = "Fruits";

        ass1.setCategoryName(cat1Name);
        ass1.setCategoryId(cat1Id);

        ass2.setCategoryId(cat1Id);
        ass2.setCategoryName(cat1Name);

        Double cat1Weight = 22D;
        CategoryDefinition catDef1 = new CategoryDefinition();
        catDef1.setId(cat1Id);
        catDef1.setName(cat1Name);
        catDef1.setWeight(cat1Weight);
        catDef1.setDropKeepEnabled(false);
        catDef1.setExtraCredit(false);
        catDef1.setEqualWeight(false);
        catDef1.setAssignmentList(List.of(ass1, ass2));

        when(gradingService.getCategoryDefinitions(site1Id, site1Id)).thenReturn(List.of(catDef1));
        when(gradingService.getAssignments(any(), any(), any())).thenReturn(List.of(ass1, ass2));

        mockMvc.perform(get("/sites/" + site1Id + "/grading/item-data"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories[0].id", is(1)))
            .andExpect(jsonPath("$.categories[0].name", is(cat1Name)))
            .andExpect(jsonPath("$.categories[0].weight", is(cat1Weight)))
            .andExpect(jsonPath("$.categories[0].totalPoints", is(131D)))
            .andExpect(jsonPath("$.categories[0].assignmentList[0].id", is(ass1Id.intValue())))
            .andExpect(jsonPath("$.categories[0].assignmentList[0].categoryId", is(cat1Id.intValue())))
            .andExpect(jsonPath("$.categories[0].assignmentList[0].categoryName", is(cat1Name)))
            .andExpect(jsonPath("$.categories[0].assignmentList[1].id", is(ass2Id.intValue())))
            .andExpect(jsonPath("$.categories[0].assignmentList[1].categoryId", is(cat1Id.intValue())))
            .andExpect(jsonPath("$.categories[0].assignmentList[1].categoryName", is(cat1Name)))
            .andDo(document("get-item-data"));
    }

    @Test
    public void testSubmitGrade() throws Exception {

        Long gradingItemId = 1L;
        String user1Id = "user1";
        String grade = "33";
        String comment = "Great";
        String reference = "/assignments/1";

        mockMvc.perform(post("/sites/" + site1Id + "/grades/" + gradingItemId + "/" + user1Id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"grade\": \"" + grade + "\", \"comment\": \"" + comment + "\", \"reference\": \"" + reference + "\"}"))
            .andExpect(status().isOk())
            .andDo(document("grade-user"));

        verify(gradingService).setAssignmentScoreString(site1Id, site1Id, gradingItemId, user1Id, grade, "restapi", reference);
        verify(gradingService).setAssignmentScoreComment(site1Id, gradingItemId, user1Id, comment);
    }

    @Test
    public void testGetGroupCategoriesList()  throws Exception {

        when(site1.getGroupsWithMember(user1Id)).thenReturn(List.of(group1, group2));

        Gradebook gb1 = new Gradebook();
        gb1.setId(1L);
        gb1.setUid(group1Id);

        Gradebook gb2 = new Gradebook();
        gb2.setId(2L);
        gb2.setUid(group2Id);

        when(gradingService.getGradebookGroupInstances(site1Id)).thenReturn(List.of(gb1, gb2));

        when(gradingService.isCategoriesEnabled(gb1.getUid())).thenReturn(true);
        when(gradingService.isCategoriesEnabled(gb2.getUid())).thenReturn(true);

        Long cat1Id = 1L;
        String cat1Name = "Fruits";
        Double cat1Weight = 22D;
        CategoryDefinition catDef1 = new CategoryDefinition();
        catDef1.setId(cat1Id);
        catDef1.setName(cat1Name);
        catDef1.setWeight(cat1Weight);
        catDef1.setDropKeepEnabled(false);
        catDef1.setExtraCredit(false);
        catDef1.setEqualWeight(false);
        when(gradingService.getCategoryDefinitions(gb1.getUid(), site1Id)).thenReturn(List.of(catDef1));

        Long cat2Id = 2L;
        String cat2Name = "Vegetables";
        Double cat2Weight = 44D;
        CategoryDefinition catDef2 = new CategoryDefinition();
        catDef2.setId(cat2Id);
        catDef2.setName(cat2Name);
        catDef2.setWeight(cat2Weight);
        catDef2.setDropKeepEnabled(false);
        catDef2.setExtraCredit(false);
        catDef2.setEqualWeight(false);
        when(gradingService.getCategoryDefinitions(gb2.getUid(), site1Id)).thenReturn(List.of(catDef2));

        when(gradingService.getAssignments(group1Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass1));
        when(gradingService.getAssignments(group2Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass2));

        mockMvc.perform(get("/sites/" + site1Id + "/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].uid", is(group2Id)))
            .andExpect(jsonPath("$[0].name", is(group2Title)))
            .andExpect(jsonPath("$[0].items[0].id", is(cat2Id.toString())))
            .andExpect(jsonPath("$[0].items[0].name", is(cat2Name)))
            .andDo(document("get-group-categories"));
    }

    @Test
    public void testGetSiteItems() throws Exception {

        String appName = "assignments";

        when(site1.getGroupsWithMember(user1Id)).thenReturn(List.of(group1, group2));

        Gradebook gb1 = new Gradebook();
        gb1.setId(1L);
        gb1.setUid(group1Id);

        Gradebook gb2 = new Gradebook();
        gb2.setId(2L);
        gb2.setUid(group2Id);

        when(gradingService.getGradebookGroupInstances(site1Id)).thenReturn(List.of(gb1, gb2));

        when(gradingService.getAssignments(group1Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass1));
        when(gradingService.getAssignments(group2Id, site1Id, SortType.SORT_BY_NONE)).thenReturn(List.of(ass2));

        mockMvc.perform(get("/sites/" + site1Id + "/items/" + appName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].uid", is(group2Id)))
            .andExpect(jsonPath("$[0].name", is(group2Title)))
            .andExpect(jsonPath("$[0].items[0].id", is(ass2Id.toString())))
            .andExpect(jsonPath("$[0].items[0].name", is(ass2Name)))
            .andDo(document("get-site-items-for-app"));

        mockMvc.perform(get("/sites/" + site1Id + "/items/" + appName + "/" + user1Id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].uid", is(group2Id)))
            .andExpect(jsonPath("$[0].name", is(group2Title)))
            .andExpect(jsonPath("$[0].items[0].id", is(ass2Id.toString())))
            .andExpect(jsonPath("$[0].items[0].name", is(ass2Name)))
            .andExpect(jsonPath("$[1].uid", is(group1Id)))
            .andExpect(jsonPath("$[1].name", is(group1Title)))
            .andExpect(jsonPath("$[1].items[0].id", is(ass1Id.toString())))
            .andExpect(jsonPath("$[1].items[0].name", is(ass1Name)))
            .andDo(document("get-site-items-for-app-user"));

        when(gradingService.isGradebookGroupEnabled(site1Id)).thenReturn(true);

        mockMvc.perform(get("/sites/" + site1Id + "/items/" + appName + "/" + user1Id + "/" + group1Id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].uid", is(group1Id)))
            .andExpect(jsonPath("$[0].name", is(group1Title)))
            .andExpect(jsonPath("$[0].items[0].id", is(ass1Id.toString())))
            .andExpect(jsonPath("$[0].items[0].name", is(ass1Name)))
            .andDo(document("get-site-items-for-app-user-group"));
    }
}
