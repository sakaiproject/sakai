/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.impl;

import java.io.Reader;
import java.util.*;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class CalendarContentProducer implements EntityContentProducer {

	private CalendarService calendarService;
	private EntityManager entityManager;
	private SearchIndexBuilder searchIndexBuilder;
	private SearchService searchService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;

	private List<String> addingEvents = new ArrayList<>();
	private List<String> removingEvents = new ArrayList<>();

	public void init() {
		if (serverConfigurationService.getBoolean("search.enable", false)) {
			addingEvents.add("calendar.event.add");
			addingEvents.add("calendar.event.modify");
			removingEvents.add("calendar.event.remove");
			addingEvents.forEach(e -> searchService.registerFunction(e));
			removingEvents.forEach(e -> searchService.registerFunction(e));
			searchIndexBuilder.registerEntityContentProducer(this);
		}
	}


	public boolean isContentFromReader(String reference) {
		return false;
	}

	public Reader getContentReader(String reference) {
		return null;
	}

	private Reference getReference(String reference) {
		try {
			return entityManager.newReference(reference);
		} catch (Exception ex) {
			return null;
		}
	}

	private EntityProducer getProducer(Reference ref) {
		try {
			return ref.getEntityProducer();
		} catch (Exception ex) {
			return null;
		}
	}

	private CalendarEvent getCalendarEvent(Reference ref) {
		try {
			EntityProducer ep = getProducer(ref);
			if (ep instanceof CalendarService) {
				CalendarService calService = (CalendarService) ep;
				return (CalendarEvent) calService.getEntity(ref);
			}
		} catch (Exception e) {
			log.debug("Error getting CalendarEvent for reference: {}", ref.getReference(), e);
		}
		return null;
	}

	public String getContent(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";

		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return "";

		StringBuilder sb = new StringBuilder();
		SearchUtils.appendCleanString(event.getDisplayName(), sb);
		sb.append(" ");
		SearchUtils.appendCleanString(event.getDescription(), sb);
		sb.append(" ");
		SearchUtils.appendCleanString(event.getLocation(), sb);
		sb.append(" ");
		SearchUtils.appendCleanString(event.getType(), sb);
		return sb.toString();
	}

	public String getTitle(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";

		CalendarEvent event = getCalendarEvent(ref);
		return (event != null) ? event.getDisplayName() : "";
	}

	public String getUrl(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";

		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return "";

		String siteId = event.getSiteId();
		try {
			Site site = siteService.getSite(siteId);
			ToolConfiguration toolConfig = site.getToolForCommonId("sakai.schedule");
			if (toolConfig != null) {
				return serverConfigurationService.getPortalUrl()
						+ "/directtool/"
						+ toolConfig.getId()
						+ "?eventReference="
						+ reference
						+ "&panel=Main&sakai_action=doDescription&sakai.state.reset=true";
			}
		} catch (Exception e) {
			log.error("Failed to get deep link for context {} and event {}. Returning empty string.", siteId, reference, e);
		}
		return "";
	}

	public boolean matches(String reference) {
		return reference.startsWith(CalendarService.REFERENCE_ROOT);
	}

	public Integer getAction(Event event) {
		String evt = event.getEvent();
		if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
		if (removingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public boolean matches(Event event) {
		String evt = event.getEvent();
		return addingEvents.contains(evt) || removingEvents.contains(evt);
	}

	public String getTool() {
		return "calendar";
	}

	public String getSiteId(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return null;

		CalendarEvent event = getCalendarEvent(ref);
		return (event != null) ? event.getSiteId() : null;
	}

	public Iterator<String> getSiteContentIterator(String context) {
		List<String> rv = new ArrayList<>();
		try {
			String calendarReference = calendarService.calendarReference(context, SiteService.MAIN_CONTAINER);
			Calendar calendar = calendarService.getCalendar(calendarReference);

			if (calendar != null && calendar.allowGetEvents()) {
				List<CalendarEvent> events = calendar.getEvents(null, null);
				for (CalendarEvent event : events) {
					rv.add(event.getReference());
				}
			}
		} catch (Exception e) {
			log.warn("Error getting site content for context: {}", context, e);
		}
		return rv.iterator();
	}

	public boolean isForIndex(String reference) {
		return reference.startsWith(CalendarService.REFERENCE_ROOT);
	}

	public boolean canRead(String reference) {
		if (!isForIndex(reference)) {
			return false;
		}

		Reference ref = getReference(reference);
		if (ref == null) return false;

		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return false;

		String siteId = event.getSiteId();
		try {
			siteService.getSiteVisit(siteId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Map<String, ?> getCustomProperties(String reference) {
		return null;
	}

	public String getCustomRDF(String reference) {
		return null;
	}

	public String getId(String reference) {
		Reference ref = getReference(reference);
		return (ref != null) ? ref.getId() : null;
	}

	public String getType(String reference) {
		return "calendar";
	}

	public String getSubType(String reference) {
		return null;
	}

	public String getContainer(String reference) {
		Reference ref = getReference(reference);
		return (ref != null) ? ref.getContainer() : null;
	}

} 