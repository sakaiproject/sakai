/******************************************************************************
* Copyright (c) 2022 Apereo Foundation
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
******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
*/
@Slf4j
@RestController
public class SitesController extends AbstractSakaiApiController {

    @Autowired
    @Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
    private CourseManagementService cmService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ToolManager toolManager;

    @GetMapping(value = "/users/{userId}/sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getSites(@PathVariable String userId)
        throws UserNotDefinedException {

        Session session = checkSakaiSession();

        final List<Map<String, Object>> terms = cmService.getAcademicSessions().stream().map(as -> {

            Map<String, Object> term = new HashMap<>();
            term.put("id", as.getEid());
            term.put("name", as.getTitle());
            return term;
        }).collect(Collectors.toList());

        final List<Map<String, Object>> sites = siteService.getUserSites().stream().map(s -> {

            Map<String, Object> site = new HashMap<>();
            site.put("id", s.getId());
            site.put("title", s.getTitle());
            site.put("url", s.getUrl());

            if (StringUtils.equals(s.getType(), "course")) {
                site.put("course", true);
                site.put("term", s.getProperties().getProperty(Site.PROP_SITE_TERM));
            } else if (StringUtils.equals(s.getType(), "project")) {
                site.put("project", true);
            }
            return site;
        }).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> data = new HashMap<>();

        data.put("terms", terms);
        data.put("sites", sites);

        return data;
    }

    @GetMapping(value = "/users/current/favourites", produces = MediaType.APPLICATION_JSON_VALUE) 
    public Map<String, List<Map<String, Object>>> getFavouriteSitesWithPages() throws UserNotDefinedException {

        String userId = checkSakaiSession().getUserId();

        ResourceProperties resourceProperties = preferencesService.getPreferences(userId)
            .getProperties(PreferencesService.SITENAV_PREFS_KEY);
        List<String> siteIds = resourceProperties.getPropertyList("order");

        if (siteIds == null) return Collections.emptyMap();

        List<Map<String, Object>> sitesList = siteIds.stream().map(siteId -> {
            Map<String, Object> siteMap = new HashMap<>();
            try {
                Site site = siteService.getSite(siteId);
                siteMap.put("id", site.getId());
                siteMap.put("title", site.getTitle());
                siteMap.put("url", site.getUrl());
                siteMap.put("type", site.getType());
                List<Map<String, Object>> pageList = site.getOrderedPages().stream().map(page -> {
                    Map<String, Object> pageMap = new HashMap<>();
                    List<ToolConfiguration> toolList = page.getTools();
                    if (toolList.size() == 1 ) {
                        String toolId = toolList.get(0).getId();
                        String toolUrl = page.getUrl().replaceFirst("page.*", "tool/".concat(toolId));
                        pageMap.put("url", toolUrl);
                        pageMap.put("reset-url", toolUrl.replaceFirst("tool", "tool-reset"));
                        pageMap.put("toolid", toolList.get(0).getToolId());
                    } else {
                        pageMap.put("url", page.getUrl());
                        pageMap.put("reset-url",  page.getUrl().replaceFirst("page", "page-reset"));
                    }
                    if (toolList.size() > 0 && toolManager.isHidden(toolList.get(0))) {
                        pageMap.put("hidden", true);
                    }
                    if (!toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page)) {
                        pageMap.put("locked", true);
                    }
                    if (page.isPopUp()) {
                        pageMap.put("isPopup", true);
                    }
                    pageMap.put("title", page.getTitle());
                    pageMap.put("url", page.getUrl());
                    return pageMap;
                }).collect(Collectors.toList());
                siteMap.put("pages", pageList);
            } catch (IdUnusedException e) {
                log.error(e.getMessage());
            }
            return siteMap;
        }).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        data.put("favourites", sitesList);
        return data;
    }
}
