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
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;


import javax.annotation.Resource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    @Autowired private SessionManager sessionManager;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Transactional
    public UserTask createSingleUserTask(UserTaskAdapterBean transfer) {

        String userId = sessionManager.getCurrentSessionUserId();

        Session session = sessionFactory.getCurrentSession();

        Task task = new Task();
        BeanUtils.copyProperties(transfer, task);
        task.setReference("/user/" + userId);
        task.setSystem(false);
        session.save(task);

        UserTask userTask = new UserTask();
        BeanUtils.copyProperties(transfer, userTask);
        userTask.setUserId(userId);
        userTask.setTask(task);
        session.save(userTask);
        return userTask;
    }

    @Transactional
    public Task createTask(Task task, Set<String> users, Integer priority) {

        Session session = sessionFactory.getCurrentSession();

        Optional<Task> optionalCurrentTask = getTask(task.getReference());

        if (optionalCurrentTask.isPresent()) {
            task.setId(optionalCurrentTask.get().getId());
        }

        session.persist(task);

        if (!optionalCurrentTask.isPresent()) {
            users.forEach(userId -> {

                UserTask userTask = new UserTask();
                userTask.setUserId(userId);
                userTask.setTask(task);
                userTask.setPriority(priority);
                session.persist(userTask);
            });
        }

        return task;
    }

    @Transactional
    public Task saveTask(Task task) {

        Session session = sessionFactory.getCurrentSession();
        return (Task) session.merge(task);
    }

    public Optional<Task> getTask(String reference) {

        Session session = sessionFactory.getCurrentSession();

        List<Task> tasks = (List<Task>) session.createCriteria(Task.class)
            .add(Restrictions.eq("reference", reference)).list();

        if (tasks.size() >= 1) {
            return Optional.of(tasks.get(0));
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public UserTask saveUserTask(UserTaskAdapterBean transfer) {

        Session session = sessionFactory.getCurrentSession();

        UserTask userTask = (UserTask) session.load(UserTask.class, transfer.getUserTaskId());

        // Trigger the load of the Task entity
        Task task = userTask.getTask();
        BeanUtils.copyProperties(transfer, task);

        // Update the user task and merge it back in
        BeanUtils.copyProperties(transfer, userTask);
        session.merge(userTask);

        return userTask;
    }

    @Transactional
    public void removeUserTask(Long userTaskId) {

        Session session = sessionFactory.getCurrentSession();

        UserTask userTask = (UserTask) session.load(UserTask.class, userTaskId);
        session.delete(userTask);
    }


    public List<UserTaskAdapterBean> getAllTasksForCurrentUser() {

        String userId = sessionManager.getCurrentSessionUserId();

        Session session = sessionFactory.getCurrentSession();

        return ((List<UserTask>) session.createCriteria(UserTask.class)
            .add(Restrictions.eq("userId", userId)).list())
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

        Session session = sessionFactory.getCurrentSession();
        return (List<UserTask>) session.createCriteria(UserTask.class)
            .add(Restrictions.eq("userId", userId))
            .add(Restrictions.le("task.starts", Instant.now())).list();
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
    public void removeTask(Task task) {

        Session session = sessionFactory.getCurrentSession();
        session.createQuery("delete from UserTask where task = :task")
            .setParameter("task", task).executeUpdate();
        session.delete(task);
    }
}
