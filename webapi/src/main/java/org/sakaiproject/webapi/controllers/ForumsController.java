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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ForumsController extends AbstractSakaiApiController {

    @Autowired
    private SynopticMsgcntrManager msgCenterManager;

    private Predicate<SynopticMsgcntrItem> countFilter = i -> i.getNewMessagesCount() > 0 || i.getNewForumCount() > 0;

    private Function<SynopticMsgcntrItem, Map<String, Object>> handler = item -> {

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

    @GetMapping(value = "/users/current/forums/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getUserForums() throws UserNotDefinedException {

        List<String> pinnedSites = portalService.getPinnedSites();

        List<Map<String, Object>> forums = msgCenterManager.getWorkspaceSynopticMsgcntrItems(checkSakaiSession().getUserId())
            .stream()
            .filter(i -> pinnedSites.contains(i.getSiteId()))
            .filter(countFilter)
            .map(handler)
            .collect(Collectors.toList());

        return Map.of("forums", forums, "sites", getPinnedSiteList());
    }

    @GetMapping(value = "/sites/{siteId}/forums/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteForums(@PathVariable String siteId) throws UserNotDefinedException {

        return Map.of("forums", msgCenterManager.getSiteSynopticMsgcntrItems(List.of(checkSakaiSession().getUserId()), siteId)
                .stream().filter(countFilter).map(handler).collect(Collectors.toList()));
    }
}
