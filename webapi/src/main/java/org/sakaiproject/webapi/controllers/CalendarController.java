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

import org.sakaiproject.webapi.beans.CalendarEventRestBean;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class CalendarController extends AbstractSakaiApiController {

	@Resource
	private CalendarService calendarService;

	@Resource
	private ContentHostingService contentHostingService;

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

    private Function<CalendarEvent, CalendarEventRestBean> convert = (ce) -> {

        CalendarEventRestBean bean = new CalendarEventRestBean(ce, contentHostingService);

        try {
            bean.setCreatorDisplayName(userDirectoryService.getUser(bean.getCreator()).getDisplayName());

            if (!StringUtils.isBlank(bean.getAssignmentId())) {
                String ref = AssignmentReferenceReckoner.reckoner().context(bean.getSiteId()).subtype("a").id(bean.getAssignmentId()).reckon().getReference();
                if (ref != null) {
                    Optional<String> url = entityManager.getUrl(ref, Entity.UrlType.PORTAL);
                    if (url.isPresent()) {
                        bean.setUrl(url.get());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to create bean for calendar event");
        }
        return bean;
    };

    @ApiOperation(value = "Get a particular user's calendar data")
	@GetMapping(value = "/users/{userId}/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CalendarEventRestBean> getUserCalendar(@PathVariable String userId) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        return siteService.getUserSites().stream().map(s -> {

            try {
                return ((List<CalendarEvent>) calendarService.getCalendar(calendarService.calendarReference(s.getId(), "main"))
                    .getEvents(null, null))
                    .stream().map(convert).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Failed to get calendar events for site {}. An empty list will be generated", s.getId());
                return Collections.<CalendarEventRestBean>emptyList();
            }
        }).flatMap(Collection::stream).collect(Collectors.toList());
	}

    @ApiOperation(value = "Get a particular site's calendar data")
	@GetMapping(value = "/sites/{siteId}/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CalendarEventRestBean> getSiteCalendar(@PathVariable String siteId) throws UserNotDefinedException {

		Session session = checkSakaiSession();
        try {
            return ((List<CalendarEvent>) calendarService.getCalendar(calendarService.calendarReference(siteId, "main"))
                .getEvents(null, null))
                .stream().map(convert).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get calendar events for site {}. An empty list will be generated", siteId);
            return Collections.<CalendarEventRestBean>emptyList();
        }
	}
}
