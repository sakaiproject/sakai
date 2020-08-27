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

import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


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

    @ApiOperation(value = "Get all the Sakai sites a user can access")
	@GetMapping(value = "/users/{userId}/sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getSites(
            @ApiParam(value = "The user whose sites we want to retrieve", required = true)
            @PathVariable
            String userId
        ) throws UserNotDefinedException {

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
}
