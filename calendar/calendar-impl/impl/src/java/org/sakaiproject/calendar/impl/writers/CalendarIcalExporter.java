/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.calendar.impl.writers;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Comment;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.impl.readers.ICalRecurrence;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Builds the VEVENTs for a calendar's iCal export.
 *
 * Recurring events are exported as a single VEVENT carrying an RRULE (plus EXDATEs for any
 * excluded occurrences) instead of one VEVENT per occurrence, so subscribers receive the
 * series definition rather than an expanded copy of it.
 */
@Slf4j
public class CalendarIcalExporter
{
	/** Resolves a calendar reference the same way {@code CalendarService#getCalendar(String)} does. */
	@FunctionalInterface
	public interface CalendarLookup
	{
		Calendar getCalendar(String calRef) throws IdUnusedException, PermissionException;
	}

	/** Returns a calendar's events, optionally restricted to a range. */
	@FunctionalInterface
	public interface EventsLookup
	{
		List getEvents(Calendar calendar, TimeRange range) throws PermissionException;
	}

	private final TimeService timeService;
	private final UserDirectoryService userDirectoryService;
	private final CalendarLookup calendarLookup;
	private final EventsLookup eventsLookup;

	public CalendarIcalExporter(TimeService timeService, UserDirectoryService userDirectoryService,
			CalendarLookup calendarLookup, EventsLookup eventsLookup)
	{
		this.timeService = timeService;
		this.userDirectoryService = userDirectoryService;
		this.calendarLookup = calendarLookup;
		this.eventsLookup = eventsLookup;
	}

	/**
	 * Adds a VEVENT to {@code ical} for each exportable event referenced by {@code calRefs}
	 * and returns how many were added.
	 *
	 * One-time events outside {@code exportRange} are skipped. Recurring events are always
	 * included, since subscribers need the full series regardless of the export window.
	 */
	public int exportEvents(net.fortuna.ical4j.model.Calendar ical, List<String> calRefs, TimeRange exportRange)
	{
		int numEvents = 0;

		for (String calRef : calRefs)
		{
			Calendar calendar;
			try
			{
				calendar = calendarLookup.getCalendar(calRef);
			}
			catch (IdUnusedException | PermissionException e)
			{
				continue;
			}

			List events;
			try
			{
				// A null range returns base events without expanding recurring occurrences,
				// so each series can be emitted as a single VEVENT+RRULE.
				events = eventsLookup.getEvents(calendar, null);
			}
			catch (PermissionException e)
			{
				continue;
			}

			for (Object obj : events)
			{
				CalendarEvent event = (CalendarEvent) obj;
				RecurrenceRule rule = event.getRecurrenceRule();

				if (rule == null && !exportRange.overlaps(event.getRange()))
				{
					continue;
				}

				ical.getComponents().add(buildVEvent(event, rule));
				numEvents++;
			}
		}

		return numEvents;
	}

	private VEvent buildVEvent(CalendarEvent event, RecurrenceRule rule)
	{
		DateTime icalStartDate = new DateTime(event.getRange().firstTime().getTime());
		long seconds = event.getRange().duration() / 1000;
		VEvent icalEvent = new VEvent(icalStartDate, Duration.ofSeconds(seconds), event.getDisplayName());

		TzId tzId = new TzId(timeService.getLocalTimeZone().getID());
		icalEvent.getProperty(Property.DTSTART).getParameters().add(tzId);
		icalEvent.getProperty(Property.DTSTART).getParameters().add(Value.DATE_TIME);
		icalEvent.getProperties().add(new Uid(event.getId()));

		// Build the description, appending attachment URLs if present.
		StringBuffer description = new StringBuffer("");
		if (event.getDescription() != null && !event.getDescription().equals(""))
			description.append(event.getDescription());

		List attachments = event.getAttachments();
		if (attachments != null)
		{
			for (Iterator iter = attachments.iterator(); iter.hasNext();)
			{
				Reference attachment = (Reference) iter.next();
				description.append("\n");
				description.append(attachment.getUrl());
				description.append("\n");
			}
		}
		if (!description.isEmpty())
		{
			icalEvent.getProperties().add(new Description(description.toString().replace('\r', '\n')));
		}

		if (StringUtils.isNotBlank(event.getLocation()))
		{
			icalEvent.getProperties().add(new Location(event.getLocation().replace('\r', '\n')));
		}

		try
		{
			String organizer = userDirectoryService.getUser(event.getCreator()).getDisplayName();
			organizer = organizer.replaceAll(" ", "%20");
			icalEvent.getProperties().add(new Organizer(new URI("CN=" + organizer)));
		}
		catch (UserNotDefinedException e) { log.warn("Creator not found for calendar event [{}]", event.getId()); }
		catch (URISyntaxException e) { log.warn("Could not build organizer URI for calendar event [{}]", event.getId()); }

		StringBuffer comment = new StringBuffer(event.getType());
		comment.append(" (");
		comment.append(event.getSiteName());
		comment.append(")");
		icalEvent.getProperties().add(new Comment(comment.toString()));

		if (rule != null)
		{
			RRule rrule = ICalRecurrence.toRRule(rule, timeService.getLocalTimeZone());
			if (rrule != null)
			{
				icalEvent.getProperties().add(rrule);
			}

			for (ExDate exDate : ICalRecurrence.toExDates(event, timeService))
			{
				icalEvent.getProperties().add(exDate);
			}
		}

		return icalEvent;
	}
}