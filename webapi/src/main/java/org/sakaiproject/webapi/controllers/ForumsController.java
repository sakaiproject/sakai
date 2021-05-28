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

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class ForumsController extends AbstractSakaiApiController {

	@Resource
	private SynopticMsgcntrManager msgCenterManager;

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

    private Function<SynopticMsgcntrItem, Map<String, Object>> handler = (item) -> {

        Map<String, Object> map = new HashMap<>();
        map.put("messageCount", item.getNewMessagesCount());
        map.put("forumCount", item.getNewForumCount());
        map.put("siteId", item.getSiteId());
        map.put("siteTitle", item.getSiteTitle());

        try {
            Site site = siteService.getSite(item.getSiteId());

            map.put("siteUrl", site.getUrl());

            ToolConfiguration tc = site.getToolForCommonId("sakai.forums");
            if (tc != null) {
                String forumsUrl = "/portal/directtool/" + tc.getId();
                map.put("forumUrl", forumsUrl);
            }
            tc = site.getToolForCommonId("sakai.messages");
            if (tc != null) {
                String messageUrl = "/portal/directtool/" + tc.getId();
                map.put("messageUrl", messageUrl);
            }
        } catch (Exception e) {
            log.error("Failed to set urls for forums or messages, for site {}", item.getSiteId());
        }
        return map;
    };

    @ApiOperation(value = "Get a particular user's forums data")
	@GetMapping(value = "/users/{userEid}/forums", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getUserForums(@PathVariable String userEid) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        List<String> sites = siteService.getUserSites().stream().map(s -> s.getId()).collect(Collectors.toList());

        return msgCenterManager.getWorkspaceSynopticMsgcntrItems(session.getUserId())
            .stream().filter(si -> sites.contains(si.getSiteId())).map(handler).collect(Collectors.toList());
            //.stream().map(handler).collect(Collectors.toList());
	}

    @ApiOperation(value = "Get a particular site's forums data")
	@GetMapping(value = "/sites/{siteId}/forums", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getSiteForums(@PathVariable String siteId) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        return msgCenterManager.getSiteSynopticMsgcntrItems(
            Arrays.asList(new String[] {session.getUserId()}), siteId)
                .stream().map(handler).collect(Collectors.toList());
	}
}
