/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Indexes site calendar events for Sakai search. References have the form
 * {@code /calendar/event/{siteId}/{calendarId}/{eventId}}. Who may see a result is decided by the calendar itself,
 * so events restricted to groups are only found by members of those groups.
 */
@Slf4j
@Setter
public class CalendarContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    private static final String EVENT_REFERENCE_PREFIX = CalendarService.REFERENCE_ROOT + Entity.SEPARATOR
            + CalendarService.REF_TYPE_EVENT + Entity.SEPARATOR;

    // calendar.revise is also posted for changes to a calendar itself, so events are matched on their reference too
    private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
            CalendarService.EVENT_ADD_CALENDAR, SearchBuilderItem.ACTION_ADD,
            CalendarService.EVENT_MODIFY_CALENDAR, SearchBuilderItem.ACTION_ADD,
            CalendarService.EVENT_REMOVE_CALENDAR_EVENT, SearchBuilderItem.ACTION_DELETE);

    private CalendarService calendarService;
    private SearchIndexBuilder searchIndexBuilder;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;

    public void init() {
        if (serverConfigurationService.getBoolean("search.enable", false)) {
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return EVENT_ACTIONS.keySet();
    }

    @Override
    public boolean matches(String reference) {
        return parse(reference).isPresent();
    }

    @Override
    public boolean matches(Event event) {
        return EVENT_ACTIONS.containsKey(event.getEvent()) && matches(event.getResource());
    }

    @Override
    public Integer getAction(Event event) {
        return matches(event) ? EVENT_ACTIONS.get(event.getEvent()) : SearchBuilderItem.ACTION_UNKNOWN;
    }

    @Override
    public boolean canRead(String reference) {
        // the calendar's own check, which honours events restricted to groups
        return parse(reference).map(ref -> {
            try {
                return calendar(ref).allowGetEvent(ref.eventId());
            } catch (Exception e) {
                return false;
            }
        }).orElse(false);
    }

    @Override
    public String getTitle(String reference) {
        return withEvent(reference, CalendarEvent::getDisplayName).orElse("");
    }

    @Override
    public String getContent(String reference) {
        return withEvent(reference, event -> StringUtils.joinWith(" ", event.getDisplayName(), event.getDescription(),
                event.getLocation(), event.getType())).orElse("");
    }

    @Override
    public String getUrl(String reference) {
        return parse(reference).map(ref -> {
            try {
                ToolConfiguration tool = siteService.getSite(ref.siteId()).getToolForCommonId("sakai.schedule");
                if (tool != null) {
                    // opens the event the way Date Manager links to it
                    return serverConfigurationService.getPortalUrl() + "/site/" + ref.siteId() + "/tool/" + tool.getId()
                            + "?eventReference=" + URLEncoder.encode(reference, StandardCharsets.UTF_8)
                            + "&panel=Main&sakai_action=doDescription&sakai.state.reset=true";
                }
            } catch (Exception e) {
                log.debug("Could not build url for calendar event {}: {}", reference, e.toString());
            }
            return "";
        }).orElse("");
    }

    @Override
    public String getTool() {
        return "calendar";
    }

    @Override
    public String getId(String reference) {
        return parse(reference).map(EventRef::eventId).orElse("");
    }

    @Override
    public String getSiteId(String reference) {
        return parse(reference).map(EventRef::siteId).orElse(null);
    }

    @Override
    public Iterator<String> getSiteContentIterator(String siteId) {
        Set<String> references = new LinkedHashSet<>();
        try {
            Calendar calendar = calendarService.getCalendar(calendarService.calendarReference(siteId, SiteService.MAIN_CONTAINER));
            for (CalendarEvent event : (List<CalendarEvent>) calendar.getEvents(null, null)) {
                // a recurring event is listed once per occurrence, as !range!sequence!eventId; index the event once
                String eventId = StringUtils.substringAfterLast("!" + event.getId(), "!");
                references.add(calendarService.eventReference(siteId, calendar.getId(), eventId));
            }
        } catch (Exception e) {
            log.debug("No calendar events to index for site {}: {}", siteId, e.toString());
        }
        return references.iterator();
    }

    private <T> Optional<T> withEvent(String reference, Function<CalendarEvent, T> function) {
        return parse(reference).flatMap(ref -> {
            try {
                return Optional.ofNullable(calendar(ref).getEvent(ref.eventId())).map(function);
            } catch (Exception e) {
                log.debug("Could not read calendar event {}: {}", reference, e.toString());
                return Optional.empty();
            }
        });
    }

    private Calendar calendar(EventRef ref) throws Exception {
        return calendarService.getCalendar(calendarService.calendarReference(ref.siteId(), ref.calendarId()));
    }

    private static Optional<EventRef> parse(String reference) {
        if (reference == null || !reference.startsWith(EVENT_REFERENCE_PREFIX)) {
            return Optional.empty();
        }
        String[] parts = reference.substring(EVENT_REFERENCE_PREFIX.length()).split(Entity.SEPARATOR);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new EventRef(parts[0], parts[1], parts[2]));
    }

    private record EventRef(String siteId, String calendarId, String eventId) { }
}
