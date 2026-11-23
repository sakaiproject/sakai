/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.org.ponder.messageutil.MessageLocator;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LessonBuilderServiceTestConfiguration.class, LessonsGradebookTitleTest.UiBoundaries.class})
@Transactional
public class LessonsGradebookTitleTest {
    @Autowired private SimplePageToolDao dao;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private GradebookIfc gradebookIfc;
    @Autowired private GradingService gradebook;
    @Autowired private SecurityService security;
    @Autowired private SessionManager sessions;
    @Autowired private SiteService sites;
    @Autowired private ActiveToolManager tools;
    @Autowired private MessageLocator messages;
    @Autowired private TaskService tasks;
    @Autowired private ConditionService conditions;
    private SimplePageItem question;
    private SimplePageBean bean;

    @Before
    public void setup() throws Exception {
        reset(gradebook, security, sessions, sites, tools, messages, tasks, conditions);
        when(messages.getMessage(anyString())).thenAnswer(call -> call.getArgument(0));
        when(gradebook.isGradebookGroupEnabled("site")).thenReturn(true);
        when(security.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/site")).thenReturn(true);
        when(sessions.getCurrentSessionUserId()).thenReturn("editor");
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("site");
        when(tools.getCurrentPlacement()).thenReturn(placement);
        Session session = mock(Session.class);
        when(sessions.getCurrentSession()).thenReturn(session);
        when(session.getAttribute("sakai.csrf.token")).thenReturn("token");
        ToolSession toolSession = mock(ToolSession.class);
        Map<String, Object> attributes = new HashMap<>();
        when(sessions.getCurrentToolSession()).thenReturn(toolSession);
        when(toolSession.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> { attributes.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(toolSession).setAttribute(anyString(), any());
        Site site = mock(Site.class);
        when(sites.getSite("site")).thenReturn(site);
        when(tasks.getTask(anyString())).thenReturn(Optional.empty());
        when(conditions.getConditionsForItem(anyString(), anyString(), anyString())).thenReturn(List.of());

        SimplePage page = dao.makePage("tool", "site", "Lesson", null, null);
        assertTrue(dao.quickSaveItem(page));
        question = dao.makeItem(page.getPageId(), 1, SimplePageItem.QUESTION, "", "Question");
        question.setGroups("group-a");
        question.setGradebookTitle("Old title");
        question.setGradebookId("lesson-builder:question:existing");
        assertTrue(dao.quickSaveItem(question));
        // RSF constructs the request bean; use the real DAO and Gradebook adapter from the tool context.
        bean = new SimplePageBean();
        bean.setSimplePageToolDao(dao);
        bean.setGradebookIfc(gradebookIfc);
        bean.setSecurityService(security);
        bean.setSessionManager(sessions);
        bean.setSiteService(sites);
        bean.setMessageLocator(messages);
        bean.setTaskService(tasks);
        bean.setConditionService(conditions);
        bean.setCurrentSiteId("site");
        bean.setCurrentPage(page);
        bean.setCurrentPageId(page.getPageId());
        bean.setCsrfToken("token");
        bean.itemId = question.getId();
        bean.questionType = "shortanswer";
        bean.questionAnswer = "answer";
        bean.questionText = "Question";
        bean.graded = true;
        bean.maxPoints = "10";
        bean.gradebookTitle = "New title";
        bean.selectedGroups = new String[] {"group-a"};
    }

    @Test
    public void failedRenameWithNoAddedGroupsPreservesTheTitle() {
        rejectRename();
        assertEquals("success", bean.updateQuestion());
        assertStoredTitle("Old title");
        verify(gradebook, never()).addExternalAssessment(anyString(), anyString(), anyString(), any(), anyString(),
                any(), any(), anyString(), any(), any(), any(), any());
        assertEquals(List.of("simplepage.gradebook-update-failed"), bean.errMessages());
    }

    @Test
    public void failedRenameAfterAddingAGroupPreservesTheLocalTitle() {
        bean.selectedGroups = new String[] {"group-a", "group-b"};
        rejectRename();
        assertEquals("success", bean.updateQuestion());
        assertStoredTitle("Old title");
        verify(gradebook).addExternalAssessment(eq("group-b"), eq("site"), anyString(), any(), eq("New title"),
                any(), any(), anyString(), any(), any(), any(), any());
    }

    @Test
    public void failedCreationDoesNotCommitTheLocalTitleAfterAnUpdate() {
        bean.selectedGroups = new String[] {"group-a", "group-b"};
        doThrow(new ConflictingAssignmentNameException("duplicate")).when(gradebook)
                .addExternalAssessment(eq("group-b"), eq("site"), anyString(), any(), eq("New title"),
                        any(), any(), anyString(), any(), any(), any(), any());
        assertEquals("success", bean.updateQuestion());
        assertStoredTitle("Old title");
        verify(gradebook).updateExternalAssessment(eq("group-a"), anyString(), any(), any(), eq("New title"), any(), any(), any(), any());
        assertEquals(List.of("simplepage.existing-gradebook"), bean.errMessages());
    }

    @Test
    public void successfulGroupChangesCommitTheTitleAfterTheUpdates() {
        bean.selectedGroups = new String[] {"group-a", "group-b"};
        doAnswer(call -> { assertEquals("Old title", question.getGradebookTitle()); return null; }).when(gradebook)
                .updateExternalAssessment(eq("group-a"), anyString(), any(), any(), eq("New title"), any(), any(), any(), any());
        assertEquals("success", bean.updateQuestion());
        assertStoredTitle("New title");
    }

    private void rejectRename() {
        doThrow(new ConflictingAssignmentNameException("duplicate")).when(gradebook)
                .updateExternalAssessment(eq("group-a"), anyString(), any(), any(), eq("New title"), any(), any(), any(), any());
    }

    private void assertStoredTitle(String expected) {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertEquals(expected, dao.findItem(question.getId()).getGradebookTitle());
    }

    @Configuration
    public static class UiBoundaries {
        @Bean public MessageLocator messages() { return mock(MessageLocator.class); }
        @Bean public TaskService tasks() { return mock(TaskService.class); }
        @Bean public ConditionService conditions() { return mock(ConditionService.class); }
    }
}
