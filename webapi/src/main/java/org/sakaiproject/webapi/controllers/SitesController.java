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

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

	@Resource(name = "org.sakaiproject.coursemanagement.api.CourseManagementService")
	private CourseManagementService cmService;

	@Resource
	private SiteService siteService;

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

    @Resource
    private SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);

    @Resource
    private PreferencesService preferencesService = (PreferencesService) ComponentManager.get(PreferencesService.class);

    @Resource
    private ActiveToolManager activeToolManager = (ActiveToolManager) ComponentManager.get(ActiveToolManager.class);

    @Resource
    private ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());

	@GetMapping(value = "/users/{userId}/favs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getFavsWithPages(@PathVariable String userId)
        throws UserNotDefinedException {

        Session session = checkSakaiSession();
        String reqUser = session.getUserId();
        //If non admin user requests data of another user, return empty map and warn
        if(!securityService.isSuperUser() && !userId.equals(reqUser)) {
            log.warn("User " + reqUser + " requested favorites from user " + userId);
            return new HashMap<>();
        }
        return getUserFavoriteSitesWithPages(userId);
	}

	@GetMapping(value = "/user/favs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getFavsWithPages()
        throws UserNotDefinedException {

        Session session = checkSakaiSession();
        String reqUser = session.getUserId();
        return getUserFavoriteSitesWithPages(reqUser);
	}

    private Map<String, List<Map<String, Object>>> getUserFavoriteSitesWithPages(String userId)
        throws UserNotDefinedException  {

        ResourceProperties props = preferencesService.getPreferences(userId).getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
        List<Map<String, Object>> sites = props.getPropertyList("order").stream().map(f -> {
            Map<String, Object> site = new HashMap<>();
            try {
                Site s = siteService.getSite(f);
                site.put("id", s.getId());
                site.put("title", s.getTitle());
                site.put("url", s.getUrl());
                site.put("type", s.getType());
                List<Map<String, Object>> pages = s.getOrderedPages().stream().map(p -> {
                    Map<String, Object> page = new HashMap<>();
                    List<ToolConfiguration> toolList = p.getTools();
                    if(toolList != null && toolList.size() == 1 ) {
                        String toolId = toolList.get(0).getId();
                        String toolUrl = p.getUrl().replaceFirst("page.*", "tool/".concat(toolId));
                        page.put("url", toolUrl);
                        page.put("reset-url", toolUrl.replaceFirst("tool", "tool-reset"));
                        page.put("toolid", toolList.get(0).getToolId());
                    } else {
                        page.put("url", p.getUrl());
                        page.put("reset-url",  p.getUrl().replaceFirst("page", "page-reset"));
                    }
                    if(toolList.size() > 0 && toolManager.isHidden(toolList.get(0))) {
                        page.put("hidden", true);
                    }
                    if(!toolManager.isFirstToolVisibleToAnyNonMaintainerRole(p)) {
                        page.put("locked", true);
                    }
                    //page.put("stealthed", activeToolManager.isStealthed(toolList.get(0).getToolId()));
                    page.put("title", p.getTitle());
                    page.put("url", p.getUrl());
                    page.put("isPopup", p.isPopUp());
                    return page;
                }).collect(Collectors.toList());
                site.put("pages", pages);
            } catch (IdUnusedException e) {
                log.error(e.getMessage());
            }
            return site;
        }).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        data.put("favorites", sites);
        return data;
	}
}
