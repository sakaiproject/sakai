/**********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2013 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tasks.impl.test;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TaskServiceTestConfiguration.class})
@FixMethodOrder(NAME_ASCENDING)
public class TaskServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private TaskService taskService;

    private Task createTask(String reference) {

        String siteId = "python101";
        String description = "This is my task";
        Set<String> userIds = new HashSet<>();
        userIds.add("abcde");
        userIds.add("earle");

        Task task = new Task();
        task.setSiteId(siteId);
        task.setReference(reference);
        task.setSystem(false);
        task.setDescription(description);
        return taskService.createTask(task, userIds, Priorities.HIGH);
    }

    @Test
    public void testCanCreateSingleUserTask() {

        String description = "Description about my task";
        String notes = "Notes about my task";
        int priority = Priorities.QUITE_HIGH;
        UserTaskAdapterBean bean = new UserTaskAdapterBean();
        bean.setDescription(description);
        bean.setNotes(notes);
        bean.setPriority(priority);

        taskService.createSingleUserTask(bean);

        Optional<UserTaskAdapterBean> optionalUserTask
            = taskService.getAllTasksForCurrentUser().stream().findFirst();

        Assert.isTrue(optionalUserTask.isPresent(), "getAllTasksForCurrentUser did not return our new task");

        UserTaskAdapterBean userTask = optionalUserTask.get();

        assertEquals("task.description were not the same", description, userTask.getDescription());
        assertEquals("usertask.notes were not the same", notes, userTask.getNotes());
        Assert.isTrue(userTask.getPriority() == priority, "task.priority were not the same");
    }

    @Test
    public void testCanCreateTask() {

        String reference = "/a/xyz";
        Task task = createTask(reference);
        Assert.isTrue(task.getId() != null, "The saved task should now have an id");

        Assert.isTrue(taskService.getAllTasksForCurrentUser().stream().findFirst().isPresent(), "getAllTasksForCurrentUser did not return our new task");
    }

    @Test
    public void testCanSaveUserTask() {

        String reference = "/a/xyz";

        createTask(reference);

        List<UserTaskAdapterBean> userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 1, "There should be one user task after creating");
        UserTaskAdapterBean userTask = userTasks.get(0);

        String newNotes = "New notes";
        String newDescription = "New description";
        userTask.setNotes(newNotes);
        userTask.setPriority(3);
        userTask.setComplete(true);
        userTask.setDescription(newDescription);

        taskService.saveUserTask(userTask);

        userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 1, "There should be one user task after updating");

        userTask = userTasks.get(0);

        assertEquals("Newly saved notes doesn't match", newNotes, userTask.getNotes());
        assertEquals("Newly saved description doesn't match", newDescription, userTask.getDescription());
        Assert.isTrue(userTask.getPriority() == 3, "Newly saved priority doesn't match");
        Assert.isTrue(userTask.getComplete(), "Newly saved task should be complete");
    }

    @Test
    public void testCanRemoveUserTask() {

        String reference = "/a/xyz";

        createTask(reference);

        List<UserTaskAdapterBean> userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 1, "There should be one user task after creating");
        UserTaskAdapterBean userTask = userTasks.get(0);
        taskService.removeUserTask(userTask.getUserTaskId());

        userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 0, "There should be zero user task after removing");
    }

    @Test
    public void testCanDeleteTask() {

        String reference = "/a/xyz";
        Task task = createTask(reference);
        Assert.isTrue(task.getId() != null, "The saved task should now have an id");
        taskService.removeTask(task);

        List<UserTaskAdapterBean> userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 0, "Deleting a Task should delete the user tasks");
    }
 
    @Test
    public void testCanSoftDeleteTask() {

        String reference = "/a/xyz";
        Task task = createTask(reference);

        List<UserTaskAdapterBean> userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 1, "There should only be one task");

        UserTaskAdapterBean userTask = userTasks.get(0);

        userTask.setSoftDeleted(true);
        taskService.saveUserTask(userTask);

        userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 1, "There should only be one task");

        userTask = userTasks.get(0);

        Assert.isTrue(userTask.getSoftDeleted(), "The task should be soft deleted");
    }

    @Test
    public void testCanDeleteTaskByReference() {

        String reference = "/a/xyz";
        Task task = createTask(reference);
        Assert.isTrue(task.getId() != null, "The saved task should now have an id");
        taskService.removeTaskByReference(reference);

        List<UserTaskAdapterBean> userTasks = taskService.getAllTasksForCurrentUser();
        Assert.isTrue(userTasks.size() == 0, "Deleting a Task by reference should delete the user tasks");
    }

}
