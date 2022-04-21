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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.authz.api.AuthzGroupReferenceBuilder;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.AssignationType;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskAssigned;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.tasks.api.repository.TaskAssignedRepository;
import org.sakaiproject.tasks.api.repository.TaskRepository;
import org.sakaiproject.tasks.api.repository.UserTaskRepository;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService, Observer {

    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private EntityManager entityManager;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserTaskRepository userTaskRepository;
    @Autowired private TaskAssignedRepository taskAssignedRepository;

    @Setter private TransactionTemplate transactionTemplate;

    public void init() {

        eventTrackingService.addObserver(this);
    }

    public void update(Observable o, Object arg) {

        if (arg instanceof Event) {
            Event event = (Event) arg;
            if (event.getEvent().equals(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP)) {
                try {
                    Set<String> siteUsers = siteService.getSite(event.getContext()).getUsers();
                    transactionTemplate.executeWithoutResult(status -> {

                        userTaskRepository.findByTask_SiteId(event.getContext()).forEach(userTask -> {

                            if (!siteUsers.contains(userTask.getUserId())) {
                                // This user task's user has been removed from the site
                                userTaskRepository.deleteById(userTask.getId());
                            }
                        });
                    });
                } catch (Exception e) {
                    log.error("Failed to update user tasks for site {}: {}", event.getContext(), e.toString());
                }
            } else if (event.getEvent().equals(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP)) {
                String groupId = event.getResource();
                try {
                    String groupRef = AuthzGroupReferenceBuilder.builder().site(event.getContext()).group(groupId).build();
                    transactionTemplate.executeWithoutResult(status -> {

                        // Find any task containing this group
                        taskRepository.findByGroupsContaining(groupRef).forEach(t -> {

                            // Get the set of users in all this tasks's groups
                            Set<String> users = t.getGroups().stream().map(group -> {

                                    try {
                                        return authzGroupService.getAuthzGroup(group).getUsers();
                                    } catch (GroupNotDefinedException gnde) {
                                        return Collections.<String>emptySet();
                                    }

                                }).flatMap(Collection::stream).collect(Collectors.toSet());

                            userTaskRepository.deleteByTaskAndUserIdNotIn(t, users);
                        });
                    });
                } catch (Exception e) {
                    log.error("Failed to update user tasks for group {}: {}", groupId, e.toString());
                }
            }
        }
    }

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

        Optional<UserTask> optionalUserTask = userTaskRepository.findById(transfer.getUserTaskId());

        if (!optionalUserTask.isPresent()) {
            log.error("No user task for id {}. Returning null ...", transfer.getUserTaskId());
            return null;
        }

        UserTask userTask = optionalUserTask.get();

        // Trigger the load of the Task entity
        Task task = userTask.getTask();
        if (!task.getSystem()) {
            BeanUtils.copyProperties(transfer, task);
            taskRepository.save(task);
        }

        // Update the user task and merge it back in
        BeanUtils.copyProperties(transfer, userTask);
        return userTaskRepository.save(userTask);
    }
    
    @Transactional
    public UserTask createUserTask(Task task, UserTaskAdapterBean transfer) {
        UserTask userTask = new UserTask();
        userTask.setTask(task);
        BeanUtils.copyProperties(transfer, userTask);
        return userTaskRepository.save(userTask);
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
                    bean.setDue(ut.getTask().getDue());
                    return bean;
                }).collect(Collectors.toList());
    }
    
    public List<UserTaskAdapterBean> getAllTasksForCurrentUserOnSite(String siteId) {
    	
        String userId = sessionManager.getCurrentSessionUserId();

        return userTaskRepository.findByUserIdAndSiteId(userId, siteId)
                .stream()
                .map(ut -> {
                    UserTaskAdapterBean bean = new UserTaskAdapterBean();
                    BeanUtils.copyProperties(ut, bean);
                    BeanUtils.copyProperties(ut.getTask(), bean);
                    bean.setUserTaskId(ut.getId());
                    bean.setTaskId(ut.getTask().getId());
                    bean.setDue(ut.getTask().getDue());
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
        taskAssignedRepository.deleteByTask(task);
        userTaskRepository.deleteByTask(task);
        taskRepository.delete(task);
    }
    
    @Transactional
    public void assignTask(Task task, AssignationType type, String objectId) {
        assignTask(task, type, Arrays.asList(objectId));
    }
    
    @Transactional
    public void assignTask(Task task, AssignationType type, List<String> objectIds) {
        for (String objectId : objectIds) {
            TaskAssigned taskAssigned = new TaskAssigned();
            taskAssigned.setTask(task);
            taskAssigned.setType(type);
            taskAssigned.setObjectId(objectId);
            taskAssignedRepository.save(taskAssigned);
        }
    }
    
    @Transactional
    public List<TaskAssigned> getTaskAssignments(Long taskId) {
        return taskAssignedRepository.findByTaskId(taskId);
    }
}
