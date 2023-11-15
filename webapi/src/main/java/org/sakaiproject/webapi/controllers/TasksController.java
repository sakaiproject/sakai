/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.AssignationType;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskAssigned;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.http.MediaType;

import org.sakaiproject.exception.IdUnusedException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sakaiproject.authz.api.SecurityService;

@Slf4j
@RestController
public class TasksController extends AbstractSakaiApiController {

    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String GROUP_REPLACE = "#GROUP#";
    private static final String SITE_REPLACE = "#SITE#";
    private static final String USER_REPLACE = "#USER#";

    @Resource
    private EntityManager entityManager;

    @Resource
    private TaskService taskService;

    @Resource
    private SiteService siteService;

    @Resource
    private UserDirectoryService userDirectoryService;
    
    @Resource
    private SecurityService securityService;

    @GetMapping(value = "/users/me/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getTasks() throws UserNotDefinedException {

        checkSakaiSession();

        Map<String, Object> data = new HashMap<>();

        // Flatten the UserTask objects into a more compact form and return.
        data.put("tasks", taskService.getCurrentTasksForCurrentUser()
            .stream().map(bean -> {
                try {
                    updateUserTaskAdapterBean(bean);
                } catch (IdUnusedException e) {
                    log.warn("No site for id {}", bean.getSiteId());
                }
                return bean;
            }).collect(Collectors.toList()));

        data.put("canAddTask", taskService.canCurrentUserAddTask(null));

        return data;
    }
    
    @GetMapping(value = "/sites/{siteId}/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSiteTasks(@PathVariable String siteId) throws UserNotDefinedException, IdUnusedException {

        Session session = checkSakaiSession();
        
        final Site site = siteService.getSite(siteId);

        Map<String, Object> data = new HashMap<>();

        // Flatten the UserTask objects into a more compact form and return.
        data.put("tasks", taskService.getAllTasksForCurrentUserOnSite(siteId)
            .stream().map(bean -> {

                if (site != null) {
                	bean.setSiteTitle(site.getTitle());
                	bean.setTaskAssignedTo(getTaskAssignedDescription(bean.getTaskId(), site));
                }
                if (StringUtils.isNotBlank(bean.getReference()) && !bean.getReference().startsWith("/user/")) {
                    entityManager.getUrl(bean.getReference(), Entity.UrlType.PORTAL).ifPresent(u -> bean.setUrl(u));
                }
                return bean;
            }).collect(Collectors.toList()));

        data.put("canAddTask", taskService.canCurrentUserAddTask(siteId));

        try {
            // Returns a boolean value which depends if an user is an instructor or not
            if (securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                data.put("canUpdateSite", true);

                Collection<Group> groups = site.getGroups();
                if (!groups.isEmpty()) {
                    Collection<Map<String, String>> groupsAsMaps
                        = groups.stream().map(g -> Map.of("reference", g.getReference(), "title", g.getTitle()))
                            .collect(Collectors.toList());
                    data.put("groups", groupsAsMaps);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving role on site {} for user {} : {}", siteId, session.getUserId(), e.toString());
        }

        return data;
    }
    
    @PostMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserTaskAdapterBean createTask(@RequestBody UserTaskAdapterBean taskTransfer) {

        checkSakaiSession();

        UserTaskAdapterBean result = taskTransfer;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String time = sdf.format(new Date());

        Task task = new Task();
        BeanUtils.copyProperties(taskTransfer, task);
        task.setReference("/user/" + taskTransfer.getUserId() + "/" + time);
        task.setDue(taskTransfer.getDue());
        task.setSystem(false);
        String assignationType = taskTransfer.getAssignationType();
        if (AssignationType.site.name().equals(assignationType)) {
            try {
                Site site = siteService.getSite(taskTransfer.getSiteId());
                Set<String> users = site.getUsersIsAllowed("section.role.student");
                task = taskService.createTask(task, users, taskTransfer.getPriority());
                result = UserTaskAdapterBean.from(taskService.createUserTask(task, taskTransfer));
                taskService.assignTask(task, AssignationType.site, taskTransfer.getSiteId());
                result.setTaskAssignedTo(getTaskAssignedDescription(task.getId(), site));
                result.setSiteTitle(site.getTitle());
            } catch (BeansException | IdUnusedException e) {
                log.error(e.getMessage(), e);
            }
        } else if (AssignationType.group.name().equals(assignationType)) {
            try {
                Site site = siteService.getSite(taskTransfer.getSiteId());
                Set<String> users = new HashSet();
                List<String> groups = new ArrayList();
                for (String groupId : taskTransfer.getSelectedGroups()) {
                    groups.add(groupId);
                    Group group = site.getGroup(groupId);
                    Set<Member> members = group.getMembers();
                    for (Iterator membersIter = members.iterator(); membersIter.hasNext();) {
                        Member member = (Member) membersIter.next();
                        users.add(member.getUserId());
                    }
                }
                task = taskService.createTask(task, users, taskTransfer.getPriority());
                result = UserTaskAdapterBean.from(taskService.createUserTask(task, taskTransfer));
                taskService.assignTask(task, AssignationType.group, groups);
                result.setTaskAssignedTo(getTaskAssignedDescription(task.getId(), site));
                result.setSiteTitle(site.getTitle());
            } catch (BeansException | IdUnusedException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            result = UserTaskAdapterBean.from(taskService.createSingleUserTask(taskTransfer));
        	if (!StringUtils.isEmpty(taskTransfer.getSiteId())) {
                try {
                    Site site = siteService.getSite(taskTransfer.getSiteId());
                    result.setTaskAssignedTo(getTaskAssignedDescription(result.getTaskId(), site));
                    result.setSiteTitle(site.getTitle());
                } catch (IdUnusedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        result.setOwner(taskTransfer.getUserId());
 
        return result;
    }

    @PutMapping(value = "/tasks/{userTaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserTaskAdapterBean updateTask(@RequestBody UserTaskAdapterBean taskTransfer ) {

        checkSakaiSession();

        UserTaskAdapterBean bean = UserTaskAdapterBean.from(taskService.saveUserTask(taskTransfer));
        if (!StringUtils.isEmpty(bean.getSiteId())) {
            try {
                updateUserTaskAdapterBean(bean);
            } catch (IdUnusedException e) {
                log.error(e.getMessage(), e);
            }
        }        
        return bean;
    }

    @DeleteMapping("/tasks/{userTaskId}")
    public void deleteTask(@PathVariable Long userTaskId) {

        checkSakaiSession();
        taskService.removeUserTask(userTaskId);
    }

    private String getTaskAssignedDescription(Long taskId, Site site) {
        List<TaskAssigned> taskAssignedList = taskService.getTaskAssignments(taskId);
        String result = USER_REPLACE;
        for (TaskAssigned taskAssigned : taskAssignedList) {
            if (AssignationType.site.equals(taskAssigned.getType())) {
                return SITE_REPLACE;
            } else if (AssignationType.user.equals(taskAssigned.getType())) {
                return USER_REPLACE;
            } else if (AssignationType.group.equals(taskAssigned.getType())) {
                String groupId = taskAssigned.getObjectId();
                Group group = site.getGroup(groupId);
                if (USER_REPLACE.equals(result)) {
                    result = GROUP_REPLACE + SPACE + group.getTitle();
                } else {
                    result = result + COMMA + SPACE + group.getTitle();
                }
            }
        }
        return result;
    }

    /**
     * This method updates a UserTaskAdapterBean object.
     *
     * @param bean the UserTaskAdapterBean object to update
     * @throws IdUnusedException if the specified site ID is invalid
     */
    private void updateUserTaskAdapterBean(UserTaskAdapterBean bean) throws IdUnusedException {

        Site site = siteService.getSite(bean.getSiteId());

        bean.setSiteTitle(site.getTitle());
        if (StringUtils.isNotBlank(bean.getReference()) && !bean.getReference().startsWith("/user/")) {
            entityManager.getUrl(bean.getReference(), Entity.UrlType.PORTAL).ifPresent(u -> bean.setUrl(u));
        }
        bean.setTaskAssignedTo(getTaskAssignedDescription(bean.getTaskId(), site));
    }
}
