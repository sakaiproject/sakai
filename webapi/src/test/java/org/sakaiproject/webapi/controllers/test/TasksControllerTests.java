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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.is;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.AssignationType;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskAssigned;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.controllers.TasksController;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class })
public class TasksControllerTests extends BaseControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PortalService portalService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private UserDirectoryService userDirectoryService;

    private AutoCloseable mocks;

    private String site1Id = "site1";

    @Before
    public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        reset(taskService);

        TasksController controller = new TasksController();

        controller.setUserDirectoryService(userDirectoryService);
        controller.setTaskService(taskService);
        controller.setPortalService(portalService);
        controller.setEntityManager(entityManager);
        controller.setSiteService(siteService);

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        controller.setSessionManager(sessionManager);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).apply(configurer).build();
    }

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetUserTasks() throws Exception {

        List<UserTaskAdapterBean> tasks = getTasks();
        when(taskService.getCurrentTasksForCurrentUser()).thenReturn(tasks);
        UserTaskAdapterBean task1 = tasks.get(0);
        UserTaskAdapterBean task2 = tasks.get(1);

        mockMvc.perform(get("/users/me/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tasks[0].userTaskId", is(task1.getUserTaskId().intValue())))
            .andExpect(jsonPath("$.tasks[0].taskId", is(task1.getTaskId().intValue())))
            .andExpect(jsonPath("$.tasks[0].userId", is(task1.getUserId())))
            .andExpect(jsonPath("$.tasks[0].siteId", is(task1.getSiteId())))
            .andExpect(jsonPath("$.tasks[0].siteTitle", is(task1.getSiteTitle())))
            .andExpect(jsonPath("$.tasks[0].description", is(task1.getDescription())))
            .andExpect(jsonPath("$.tasks[0].reference", is(task1.getReference())))
            .andExpect(jsonPath("$.tasks[0].system", is(task1.getSystem())))
            .andExpect(jsonPath("$.tasks[0].complete", is(task1.getComplete())))
            .andExpect(jsonPath("$.tasks[0].owner", is(task1.getOwner())))
            .andExpect(jsonPath("$.tasks[1].userTaskId", is(task2.getUserTaskId().intValue())))
            .andExpect(jsonPath("$.tasks[1].taskId", is(task2.getTaskId().intValue())))
            .andExpect(jsonPath("$.tasks[1].userId", is(task2.getUserId())))
            .andExpect(jsonPath("$.tasks[1].siteId", is(task2.getSiteId())))
            .andExpect(jsonPath("$.tasks[1].siteTitle", is(task2.getSiteTitle())))
            .andExpect(jsonPath("$.tasks[1].description", is(task2.getDescription())))
            .andExpect(jsonPath("$.tasks[1].reference", is(task2.getReference())))
            .andExpect(jsonPath("$.tasks[1].system", is(task2.getSystem())))
            .andExpect(jsonPath("$.tasks[1].complete", is(task2.getComplete())))
            .andExpect(jsonPath("$.tasks[1].owner", is(task2.getOwner())))
            .andDo(document("get-user-tasks"));
    }

    @Test
    public void testGetSiteTasks() throws Exception {

        UserTaskAdapterBean task = getTasks().get(0);
        when(taskService.getAllTasksForCurrentUserOnSite(site1Id)).thenReturn(List.of(task));

        mockMvc.perform(get("/sites/" + site1Id + "/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tasks[0].siteId", is(task.getSiteId())))
            .andDo(document("get-site-tasks"));
    }

    @Test
    public void testCreateStudentTask() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("description", "Space Stations");
        map.put("priority", 3);
        map.put("notes", "Do the assignment");
        Instant twoDays = Instant.now().plus(2, ChronoUnit.DAYS);
        map.put("due", twoDays.toEpochMilli());
        map.put("assignationType", AssignationType.site.name());
        map.put("siteId", site1Id);
        map.put("complete", false);
        map.put("userId", "user1");
        map.put("owner", "adrian");

        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(map);

        Task task = new Task();
        task.setId(1L);
        task.setDue(twoDays);
        task.setDescription((String) map.get("description"));
        task.setSiteId((String) map.get("siteId"));
        task.setReference("/assignment/1");
        task.setSystem(true);
        task.setOwner((String) map.get("owner"));
        UserTask userTask = new UserTask();
        userTask.setId(1L);
        userTask.setUserId((String) map.get("userId"));
        userTask.setTask(task);
        userTask.setComplete((Boolean) map.get("complete"));
        userTask.setPriority((Integer) map.get("priority"));
        userTask.setSoftDeleted(false);

        String site1Title = "Site 1";
        Site site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getSite(site1Id)).thenReturn(site1);
        when(site1.getUsersIsAllowed("section.role.student")).thenReturn(Set.of((String) map.get("userId")));

        when(taskService.createTask(any(), any(), any())).thenReturn(task);
        when(taskService.createUserTask(any(), any())).thenReturn(userTask);

        TaskAssigned assigns = new TaskAssigned();
        assigns.setTask(task);
        assigns.setType(AssignationType.site);
        when(taskService.getTaskAssignments(task.getId())).thenReturn(List.of(assigns));

        List<TaskAssigned> taskAssignedList = taskService.getTaskAssignments(task.getId());

        when(taskService.createSingleUserTask(any())).thenReturn(userTask);

        mockMvc.perform(post("/tasks").contentType("application/json").content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId", is(task.getSiteId())))
            .andExpect(jsonPath("$.userId", is(userTask.getUserId())))
            .andExpect(jsonPath("$.description", is(task.getDescription())))
            .andDo(document("create-student-task"));
    }

    @Test
    public void testCreateSingleUserTask() throws Exception {

        Map<String, Object> map = getUserTaskMap();

        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(map);

        Task task = new Task();
        task.setId(1L);
        task.setDescription((String) map.get("description"));
        task.setSiteId((String) map.get("siteId"));
        task.setSystem(false);
        task.setOwner((String) map.get("owner"));
        UserTask userTask = new UserTask();
        userTask.setId(1L);
        userTask.setUserId((String) map.get("userId"));
        userTask.setTask(task);
        userTask.setComplete((Boolean) map.get("complete"));
        userTask.setPriority((Integer) map.get("priority"));
        userTask.setSoftDeleted(false);

        String site1Title = "Site 1";
        Site site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getSite(site1Id)).thenReturn(site1);

        TaskAssigned assigns = new TaskAssigned();
        assigns.setTask(task);
        assigns.setType(AssignationType.user);
        when(taskService.getTaskAssignments(task.getId())).thenReturn(List.of(assigns));

        when(taskService.createSingleUserTask(any())).thenReturn(userTask);

        mockMvc.perform(post("/tasks").contentType("application/json").content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId", is(task.getSiteId())))
            .andExpect(jsonPath("$.userId", is(userTask.getUserId())))
            .andExpect(jsonPath("$.description", is(task.getDescription())))
            .andDo(document("create-personal-task"));
    }

    @Test
    public void testUpdateTask() throws Exception {

        Map<String, Object> map = getUserTaskMap();

        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(map);

        Task task = new Task();
        task.setId(1L);
        task.setDescription((String) map.get("description"));
        task.setSiteId((String) map.get("siteId"));
        task.setSystem(false);
        task.setOwner((String) map.get("owner"));
        UserTask userTask = new UserTask();
        userTask.setId(1L);
        userTask.setUserId((String) map.get("userId"));
        userTask.setTask(task);
        userTask.setComplete((Boolean) map.get("complete"));
        userTask.setPriority((Integer) map.get("priority"));
        userTask.setSoftDeleted(false);

        String site1Title = "Site 1";
        Site site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getSite(site1Id)).thenReturn(site1);

        when(taskService.saveUserTask(any())).thenReturn(userTask);

        mockMvc.perform(put("/tasks/" + userTask.getId()).contentType("application/json").content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.siteId", is(task.getSiteId())))
            .andDo(document("update-task"));
    }

    @Test
    public void testDeleteTask() throws Exception {

        mockMvc.perform(delete("/tasks/1"))
            .andExpect(status().isOk())
            .andDo(document("delete-task"));

        verify(taskService).removeUserTask(1L);
    }

    private Map<String, Object> getUserTaskMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("description", "Go to Aldi");
        map.put("priority", 1);
        map.put("assignationType", AssignationType.user.name());
        map.put("taskAssignedTo", "");
        map.put("complete", false);
        map.put("userId", "user1");
        map.put("siteId", site1Id);
        map.put("owner", "user1");
        return map;
    }

    private List<UserTaskAdapterBean> getTasks() throws Exception {

        String site1Title = "Site 1";
        Site site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.getSite(site1Id)).thenReturn(site1);

        UserTaskAdapterBean task1 = new UserTaskAdapterBean();
        task1.setUserTaskId(1L);
        task1.setTaskId(1L);
        task1.setUserId("user1");
        task1.setSiteId(site1Id);
        task1.setSiteTitle(site1Title);
        task1.setDescription("Everbody needs to take out the laundry");
        task1.setReference("/task/1");
        task1.setSystem(true);
        task1.setComplete(false);
        task1.setOwner("admin");
        task1.setPriority(2);
        task1.setStarts(Instant.now());
        task1.setDue(Instant.now().plus(2, ChronoUnit.DAYS));
        task1.setNotes("Take the laundry out and dry it on the line");
        task1.setUrl("http://path-to-tool-task-item.com");
        task1.setSoftDeleted(false);

        String site2Title = "Site 2";
        String site2Id = "site2";
        Site site2 = mock(Site.class);
        when(site2.getTitle()).thenReturn(site2Title);
        when(siteService.getSite(site2Id)).thenReturn(site2);

        UserTaskAdapterBean task2 = new UserTaskAdapterBean();
        task2.setUserTaskId(2L);
        task2.setTaskId(2L);
        task2.setUserId("user1");
        task2.setSiteId(site2Id);
        task2.setSiteTitle(site2Title);
        task2.setDescription("Do the Venus assignment");
        task2.setReference("/task/2");
        task2.setSystem(true);
        task2.setComplete(false);
        task2.setOwner("admin");
        task2.setPriority(1);
        task2.setStarts(Instant.now());
        task2.setDue(Instant.now().plus(2, ChronoUnit.DAYS));
        task2.setUrl("http://path-to-tool-task-item.com");
        task2.setSoftDeleted(false);

        return List.of(task1, task2);
    }
}
