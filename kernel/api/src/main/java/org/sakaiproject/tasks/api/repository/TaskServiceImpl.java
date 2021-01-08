/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tasks.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.tasks.api.repository.TaskRepository;
import org.sakaiproject.tasks.api.repository.UserTaskRepository;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    @Autowired private SessionManager sessionManager;

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private UserTaskRepository userTaskRepository;

    @Transactional
    public UserTask createSingleUserTask(UserTaskAdapterBean transfer) {

        String userId = sessionManager.getCurrentSessionUserId();

        Task task = new Task();
        BeanUtils.copyProperties(transfer, task);
        task.setReference("/user/" + userId);
        task.setSystem(false);
        task = taskRepository.save(task);

        UserTask userTask = new UserTask();
        BeanUtils.copyProperties(transfer, userTask);
        userTask.setUserId(userId);
        userTask.setTask(task);
        return userTaskRepository.save(userTask);
    }

    @Transactional
    public Task createTask(Task task, Set<String> users, Integer priority) {

        Optional<Task> optionalCurrentTask = getTask(task.getReference());

        if (optionalCurrentTask.isPresent()) {
            task.setId(optionalCurrentTask.get().getId());
        }

        final Task mergedTask = taskRepository.save(task);

        if (!optionalCurrentTask.isPresent()) {
            users.forEach(userId -> {

                UserTask userTask = new UserTask();
                userTask.setUserId(userId);
                userTask.setTask(mergedTask);
                userTask.setPriority(priority);
                userTaskRepository.save(userTask);
            });
        }

        return mergedTask;
    }

    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public Optional<Task> getTask(String reference) {
        return taskRepository.findByReference(reference);
    }

    @Transactional
    public UserTask saveUserTask(UserTaskAdapterBean transfer) {

        UserTask userTask = userTaskRepository.findById(transfer.getUserTaskId());

        // Trigger the load of the Task entity
        Task task = userTask.getTask();
        if (!task.getSystem()) {
            BeanUtils.copyProperties(transfer, task);
            taskRepository.save(task);
        }

        // Update the user task and merge it back in
        BeanUtils.copyProperties(transfer, userTask);
        userTaskRepository.save(userTask);

        return userTask;
    }

    @Transactional
    public void removeUserTask(Long userTaskId) {
        userTaskRepository.deleteById(userTaskId);
    }

    @Transactional
    private void completeTaskForUsers(Long taskId, List<String> userIds) {

        userTaskRepository.findByTaskIdAndUserIdIn(taskId, userIds).forEach(ut -> {
            ut.setComplete(true);
            userTaskRepository.save(ut);
        });
    }

    public List<UserTaskAdapterBean> getAllTasksForCurrentUser() {

        String userId = sessionManager.getCurrentSessionUserId();

        return userTaskRepository.findByUserId(userId)
                .stream()
                .map(ut -> {
                    UserTaskAdapterBean bean = new UserTaskAdapterBean();
                    BeanUtils.copyProperties(ut, bean);
                    BeanUtils.copyProperties(ut.getTask(), bean);
                    bean.setUserTaskId(ut.getId());
                    bean.setTaskId(ut.getTask().getId());
                    return bean;
                }).collect(Collectors.toList());
    }

    public List<UserTask> getCurrentUserTasks(String userId) {
        return userTaskRepository.findByUserIdAndTask_StartsLessThanEqual(userId, Instant.now());
    }

    @Transactional
    public void removeTaskByReference(String reference) {

        Optional<Task> optionalTask = getTask(reference);
        if (optionalTask.isPresent()) {
            removeTask(optionalTask.get());
        } else {
            log.info("No task for reference {}. Nothing removed.", reference);
        }
    }

    @Transactional
    public void completeUserTaskByReference(String reference, List<String> userIds) {
        getTask(reference).ifPresent(t -> completeTaskForUsers(t.getId(), userIds));
    }


    @Transactional
    public void removeTask(Task task) {

        userTaskRepository.deleteByTask(task);
        taskRepository.delete(task);
    }
}
