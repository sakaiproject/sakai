/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.tasks.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaskService {

    UserTask createSingleUserTask(UserTaskAdapterBean transfer);
    UserTask saveUserTask(UserTaskAdapterBean transfer);
    UserTask createUserTask(Task task, UserTaskAdapterBean transfer);
    void removeUserTask(Long userTaskId);
    Task createTask(Task task, Set<String> users, Integer priority);
    Task saveTask(Task task);
    Optional<Task> getTask(String reference);
    List<UserTaskAdapterBean> getAllTasksForCurrentUser();
    List<UserTaskAdapterBean> getAllTasksForCurrentUserOnSite(String siteId);
    List<UserTask> getCurrentUserTasks(String userId);
    void removeTask(Task task);
    void removeTaskByReference(String reference);
    void completeUserTaskByReference(String reference, List<String> userIds);
    void assignTask(Task task, AssignationType type, String objectId);
    void assignTask(Task task, AssignationType type, List<String> objectIds);
    List<TaskAssigned> getTaskAssignments(Long taskId);
}
