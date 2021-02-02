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

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.sakaiproject.exception.IdUnusedException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
@Slf4j
@RestController
public class TasksController extends AbstractSakaiApiController {

    @Resource
    private EntityManager entityManager;

    @Resource
    private TaskService taskService;

    @Resource
    private SiteService siteService;

    @Resource
    private UserDirectoryService userDirectoryService;

    @GetMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserTaskAdapterBean> getTasks() throws UserNotDefinedException {

        checkSakaiSession();

        // Flatten the UserTask objects into a more compact form and return.
        return taskService.getAllTasksForCurrentUser()
            .stream().map(bean -> {
                try {
                    bean.setSiteTitle(siteService.getSite(bean.getSiteId()).getTitle());
                    entityManager.getUrl(bean.getReference(), Entity.UrlType.PORTAL).ifPresent(u -> bean.setUrl(u));
                } catch (IdUnusedException e) {
                    log.warn("No site for id {}", bean.getSiteId());
                }
                return bean;
            }).collect(Collectors.toList());
    }

    @PutMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserTaskAdapterBean createTask(@RequestBody UserTaskAdapterBean taskTransfer) {

        checkSakaiSession();
        return UserTaskAdapterBean.from(taskService.createSingleUserTask(taskTransfer));
    }

    @PutMapping(value = "/tasks/{userTaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserTaskAdapterBean updateTask(@RequestBody UserTaskAdapterBean taskTransfer ) {

        checkSakaiSession();
        return UserTaskAdapterBean.from(taskService.saveUserTask(taskTransfer));
    }

    @DeleteMapping("/tasks/{userTaskId}")
    public void deleteTask(@PathVariable Long userTaskId) {

        checkSakaiSession();
        taskService.removeUserTask(userTaskId);
    }
}
