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

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.CalendarEventRestBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CalendarController extends AbstractSakaiApiController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    private Function<CalendarEvent, CalendarEventRestBean> convert = ce -> {

        CalendarEventRestBean bean = new CalendarEventRestBean(ce, contentHostingService);

        try {
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
            log.warn("Failed to create bean for calendar event: {}", e.toString());
        }
        return bean;
    };

    @GetMapping(value = "/users/current/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCurrentUserCalendar() throws UserNotDefinedException {

        checkSakaiSession();

        List<String> calendarRefs = portalService.getPinnedSites()
            .stream()
            .map(siteId -> calendarService.calendarReference(siteId, "main"))
            .collect(Collectors.toList());

        return Map.of(
            "events", calendarService.getEvents(calendarRefs, null, false).stream().map(convert).collect(Collectors.toList()),
            "sites", getPinnedSiteList()
        );
    }

    @GetMapping(value = "/sites/{siteId}/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSiteCalendar(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        List<String> refs = List.of(calendarService.calendarReference(siteId, "main"));

        return Map.of(
            "events", calendarService.getEvents(refs, null, false).stream().map(convert).collect(Collectors.toList())
        );
    }
}
