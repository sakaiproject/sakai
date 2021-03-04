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

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.TopicRestBean;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class ConversationsController extends AbstractSakaiApiController {

	@Resource
	private ConversationsService conversationsService;

	@Resource
	private EntityManager entityManager;

	@Resource
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Resource
	private SiteService siteService;

	@Resource
	private UserDirectoryService userDirectoryService;

    @ApiOperation(value = "Get the conversation topics for a site")
	@GetMapping(value = "/sites/{siteId}/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TopicRestBean> getSiteTopics(@PathVariable String siteId) throws UserNotDefinedException {

		checkSakaiSession();
        try {
            String ref = siteService.getSite(siteId).getReference();
            return conversationsService.getTopicsAboutRef(ref).stream()
                .map(TopicRestBean::new).collect(Collectors.toList());
        } catch (IdUnusedException idue) {
            log.error("No site for {}", siteId);
        }
        return Collections.emptyList();
	}
}
