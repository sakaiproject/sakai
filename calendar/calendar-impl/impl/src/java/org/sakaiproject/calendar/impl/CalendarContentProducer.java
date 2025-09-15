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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Content producer for calendar search functionality
 * 
 * @author Generated for calendar search support
 */
@Slf4j
public class CalendarContentProducer implements EntityContentProducer {

	@Setter @Getter
	private SearchService searchService = null;
	
	@Setter @Getter
	private SearchIndexBuilder searchIndexBuilder = null;
	
	@Setter @Getter
	private EntityManager entityManager = null;
	
	@Setter @Getter
	private CalendarService calendarService = null;
	
	@Setter @Getter
	private List<String> addEvents = new ArrayList<>();
	
	@Setter @Getter
	private List<String> removeEvents = new ArrayList<>();
	
	@Setter
	private SiteService siteService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;

	protected void init() throws Exception {
		
		if (serverConfigurationService != null && serverConfigurationService.getBoolean("search.enable", false)) {
			for (Iterator<String> i = addEvents.iterator(); i.hasNext();) {
				getSearchService().registerFunction((String) i.next());
			}
			for (Iterator<String> i = removeEvents.iterator(); i.hasNext();) {
				getSearchService().registerFunction((String) i.next());
			}
			getSearchIndexBuilder().registerEntityContentProducer(this);
		}
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

	@Override
	public boolean canRead(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return false;
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return false;
		
		String siteId = event.getSiteId();
		try {
			// Check if user can access the site - this is how calendar tool checks access
			siteService.getSiteVisit(siteId);
			return true;
		} catch (Exception e) {
			// User cannot access the site
			return false;
		}
	}

	@Override
	public Integer getAction(Event event) {
		String evt = event.getEvent();
		if (evt == null) return SearchBuilderItem.ACTION_UNKNOWN;
		
		for (Iterator<String> i = addEvents.iterator(); i.hasNext();) {
			String match = (String) i.next();
			if (evt.equals(match)) {
				return SearchBuilderItem.ACTION_ADD;
			}
		}
		for (Iterator<String> i = removeEvents.iterator(); i.hasNext();) {
			String match = (String) i.next();
			if (evt.equals(match)) {
				return SearchBuilderItem.ACTION_DELETE;
			}
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	@Override
	public String getContainer(String reference) {
		try {
			return getReference(reference).getContainer();
		} catch (Exception ex) {
			return "";
		}
	}

	@Override
	public String getContent(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return "";
		
		StringBuilder sb = new StringBuilder();
		
		// Add the display name
		String displayName = event.getDisplayName();
		if (displayName != null) {
			SearchUtils.appendCleanString(displayName, sb);
			sb.append("\n");
		}
		
		// Add the description
		String description = event.getDescription();
		if (description != null) {
			SearchUtils.appendCleanString(description, sb);
			sb.append("\n");
		}
		
		// Add the location
		String location = event.getLocation();
		if (location != null) {
			SearchUtils.appendCleanString(location, sb);
			sb.append("\n");
		}
		
		// Add the type
		String type = event.getType();
		if (type != null) {
			SearchUtils.appendCleanString(type, sb);
		}
		
		log.debug("Indexed calendar event content for reference: {}", ref.getReference());
		return sb.toString();
	}

	@Override
	public Reader getContentReader(String reference) {
		return new StringReader(getContent(reference));
	}

	@Override
	public String getId(String ref) {
		try {
			Reference reference = getReference(ref);
			if (reference != null) {
				return reference.getId();
			}
		} catch (Exception e) {
			log.debug("Error getting id for reference: {}", ref, e);
		}
		return "";
	}

	public List<String> getSiteContent(String context) {
		List<String> rv = new ArrayList<>();
		
		try {
			// Get the calendar for this site
			String calendarReference = calendarService.calendarReference(context, SiteService.MAIN_CONTAINER);
			Calendar calendar = calendarService.getCalendar(calendarReference);
			
			if (calendar != null && calendar.allowGetEvents()) {
				// Get all events from the calendar
				List<CalendarEvent> events = calendar.getEvents(null, null);
				for (CalendarEvent event : events) {
					rv.add(event.getReference());
				}
			}
		} catch (Exception e) {
			log.warn("Error getting site content for context: {}", context, e);
		}
		
		return rv;
	}

	@Override
	public Iterator<String> getSiteContentIterator(String context) {
		return getSiteContent(context).iterator();
	}

	@Override
	public String getSiteId(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return null;
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return null;
		
		return event.getSiteId();
	}

	@Override
	public String getSubType(String ref) {
		return "";
	}

	@Override
	public String getTitle(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return "";
		
		String displayName = event.getDisplayName();
		return displayName != null ? displayName : "";
	}

	@Override
	public String getTool() {
		return "calendar";
	}

	@Override
	public String getType(String ref) {
		return "calendar";
	}

	@Override
	public String getUrl(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return "";
		
		String siteId = event.getSiteId();
		// Use directtool URL for better linking to specific events
		try {
			// Find the calendar tool in the site
			Site site = siteService.getSite(siteId);
			ToolConfiguration toolConfig = site.getToolForCommonId("sakai.schedule");
			if (toolConfig != null) {
				return "/portal/directtool/" + toolConfig.getId() + 
					   "?eventReference=" + reference + 
					   "&panel=Main&sakai_action=doDescription&sakai.state.reset=true";
			}
		} catch (Exception e) {
			log.debug("Error getting tool configuration for site: {}", siteId, e);
		}
			return "";
	}

	@Override
	public boolean isContentFromReader(String reference) {
		return false;
	}

	@Override
	public boolean isForIndex(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return false;
		
		CalendarEvent event = getCalendarEvent(ref);
		if (event == null) return false;
		
		// Only index events that are accessible (this will be checked again in canRead)
		return true;
	}

	@Override
	public boolean matches(String reference) {
		return reference.startsWith(CalendarService.REFERENCE_ROOT);
	}

	@Override
	public boolean matches(Event event) {
		return matches(event.getResource());
	}

	/**
	 * Helper method to get CalendarEvent from a reference
	 */
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
} 