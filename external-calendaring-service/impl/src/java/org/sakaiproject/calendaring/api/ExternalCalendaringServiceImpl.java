/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.calendaring.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.validate.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendaring.logic.SakaiProxy;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;

/**
 * Implementation of {@link ExternalCalendaringService}
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ExternalCalendaringServiceImpl implements ExternalCalendaringService {
	
	@Setter
	private TimeService timeService;

	/**
	 * {@inheritDoc}
	 */
	public VEvent createEvent(CalendarEvent event) {
		return createEvent(event, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public VEvent createEvent(CalendarEvent event, Set<User> attendees) {
		//Default to time in GMT
		return createEvent(event, null, false);
	}

	public VTimeZone getTimeZone(boolean timeIsLocal) {
		//timezone. All dates are in GMT so we need to explicitly set that
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		
		//To prevent NPE on timezone
		TimeZone timezone = null;
		if (timeIsLocal == true) {
			timezone = registry.getTimeZone(timeService.getLocalTimeZone().getID());
		}
		if (timezone == null) {
			//This is guaranteed to return timezone if timeIsLocal == false or it fails and returns null
			timezone = registry.getTimeZone("GMT");
		}
		return timezone.getVTimeZone();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public VEvent createEvent(CalendarEvent event, Set<User> attendees, boolean timeIsLocal) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		VTimeZone tz = getTimeZone(timeIsLocal);

		//start and end date
		DateTime start = new DateTime(getStartDate(event.getRange()).getTime());
		DateTime end = new DateTime(getEndDate(event.getRange()).getTime());
		
		//create event incl title/summary
		VEvent vevent = new VEvent(start, end, event.getDisplayName());
			
		//add timezone
		vevent.getProperties().add(tz.getTimeZoneId());
		
		//add uid to event
		//could come from the vevent_uuid field in the calendar event, otherwise from event ID.
		String uuid = null;
		if(StringUtils.isNotBlank(event.getField("vevent_uuid"))) {
			uuid = event.getField("vevent_uuid");
		} else {
			uuid = event.getId();
		}		
		vevent.getProperties().add(new Uid(uuid));
		
		//add sequence to event
		//will come from the vevent_sequnece field in the calendar event, otherwise skip it
		String sequence = null;
		if(StringUtils.isNotBlank(event.getField("vevent_sequence"))) {
			sequence = event.getField("vevent_sequence");
			vevent.getProperties().add(new Sequence(sequence));
		}
			
		//add description to event
		vevent.getProperties().add(new Description(event.getDescription()));
		
		//add location to event
		vevent.getProperties().add(new Location(event.getLocation()));
		
		//add organiser to event
		if(StringUtils.isNotBlank(event.getCreator())) {

			String creatorEmail = sakaiProxy.getUserEmail(event.getCreator());

			URI mailURI = createMailURI(creatorEmail);
			Cn commonName = new Cn(sakaiProxy.getUserDisplayName(event.getCreator()));

			Organizer organizer = new Organizer(mailURI);
			organizer.getParameters().add(commonName);
			vevent.getProperties().add(organizer);
		}
		
		//add attendees to event with 'required participant' role
		vevent = addAttendeesToEvent(vevent, attendees);
		
		//add URL to event, if present
		String url = null;
		if(StringUtils.isNotBlank(event.getField("vevent_url"))) {
			url = event.getField("vevent_url");
			Url u = new Url();
			try {
				u.setValue(url);
				vevent.getProperties().add(u);
			} catch (URISyntaxException e) {
				//it doesnt matter, ignore it
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("VEvent:" + vevent);
		}
		
		return vevent;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public VEvent addAttendeesToEvent(VEvent vevent, Set<User> attendees) {
		return addAttendeesToEventWithRole(vevent, attendees, Role.REQ_PARTICIPANT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VEvent addChairAttendeesToEvent(VEvent vevent, Set<User> attendees) {
		return addAttendeesToEventWithRole(vevent, attendees, Role.CHAIR);
	}

	/**
	 * Adds attendees to an existing event with a given role
	 * Common logic for addAttendeesToEvent and addChairAttendeestoEvent
	 *
	 * @param vevent  the VEvent to add the attendess too
	 * @param attendees list of Users that have been invited to the event
	 * @param role      the role with which to add each user
	 * @return          the VEvent for the given event or null if there was an error
	 */
	protected VEvent addAttendeesToEventWithRole(VEvent vevent, Set<User> attendees, Role role) {

		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//add attendees to event with 'required participant' role
		if(attendees != null){
			for(User u: attendees) {
				Attendee a = new Attendee(createMailURI(u.getEmail()));
				a.getParameters().add(role);
				a.getParameters().add(new Cn(u.getDisplayName()));
				a.getParameters().add(PartStat.ACCEPTED);
				a.getParameters().add(Rsvp.FALSE);
			
				vevent.getProperties().add(a);
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("VEvent with attendees:" + vevent);
		}
		
		return vevent;
	}

	/**
	 * {@inheritDoc}
	 */
	public VEvent cancelEvent(VEvent vevent) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		// You can only have one status so make sure we remove any previous ones.
		vevent.getProperties().removeAll(vevent.getProperties(Property.STATUS));
		vevent.getProperties().add(Status.VEVENT_CANCELLED);

		// Must define a sequence for cancellations. If one was not defined when the event was created use 1
		if (vevent.getProperties().getProperty(Property.SEQUENCE) == null) {
			vevent.getProperties().add(new Sequence("1"));
		}

		if(log.isDebugEnabled()){
			log.debug("VEvent cancelled:" + vevent);
		}
		
		return vevent;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Calendar createCalendar(List<VEvent> events) {
		return createCalendar(events, null, true);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Calendar createCalendar(List<VEvent> events, String method, boolean timeIsLocal) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//setup calendar
		Calendar calendar = setupCalendar(method);
		
		//null check
		if(CollectionUtils.isEmpty(events)) {
			log.error("List of VEvents was null or empty, no calendar will be created.");
			return null;
		}
		
		//add vevents to calendar
		calendar.getComponents().addAll(events);
		
		//add vtimezone
		VTimeZone tz = getTimeZone(timeIsLocal);

		calendar.getComponents().add(tz);
		
		//validate
		try {
			calendar.validate(true);
		} catch (ValidationException e) {
			log.error("createCalendar failed validation", e);
			return null;
		}
		
		if(log.isDebugEnabled()){
			log.debug("Calendar:" + calendar);
		}
		
		return calendar;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toFile(Calendar calendar) {
		
		if(!isIcsEnabled()) {
			log.debug("ExternalCalendaringService is disabled. Enable via calendar.ics.generation.enabled=true in sakai.properties");
			return null;
		}
		
		//null check
		if(calendar == null) {
			log.error("Calendar is null, cannot generate ICS file.");
			return null;
		}
		
		String path = generateFilePath(UUID.randomUUID().toString());
		
		//test file
		File file = new File(path);
		try {
			if(!file.createNewFile()) {
				log.error("Couldn't write file to: " + path);
				return null;	
			}
		} catch (IOException e) {
			log.error("An error occurred trying to write file to: " + path + " : " + e.getClass() + " : " + e.getMessage());
			return null;
		}
		
		//if cleanup enabled, mark for deletion when the JVM exits.
		if(sakaiProxy.isCleanupEnabled()) {
			file.deleteOnExit();
		}
		
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(file);
		
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(calendar, fout);
		
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (ValidationException e) {
			log.error(e.getMessage(), e);
		}
 
		return path;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isIcsEnabled() {
		return sakaiProxy.isIcsEnabled();
	}
	
	/**
	 * Helper method to setup the standard parts of the calendar
	 * @return
	 */
	private Calendar setupCalendar(String method) {
		
		String serverName = sakaiProxy.getServerName();
		
		//setup calendar
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//"+serverName+"//Sakai External Calendaring Service//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		if (method != null) {
			calendar.getProperties().add(new Method(method));
		}
		return calendar;
	}
	
	
	
	
	/**
	 * Helper to extract the startDate of a TimeRange into a java.util.Calendar object. 
	 * @param range 
	 * @return
	 */
	private java.util.Calendar getStartDate(TimeRange range) {
		java.util.Calendar c = new GregorianCalendar();
		c.setTimeInMillis(range.firstTime().getTime());
		return c;
	}
	
	/**
	 * Helper to extract the endDate of a TimeRange into a java.util.Calendar object. 
	 * @param range 
	 * @return
	 */
	private java.util.Calendar getEndDate(TimeRange range) {
		java.util.Calendar c = new GregorianCalendar();
		c.setTimeInMillis(range.lastTime().getTime());
		return c;
	}
	
	/**
	 * Helper to create the name of the ICS file we are to write
	 * @param filename
	 * @return
	 */
	private String generateFilePath(String filename) {
		StringBuilder sb = new StringBuilder();
		
		String base = sakaiProxy.getCalendarFilePath();
		sb.append(base);
		
		//add slash if reqd
		if(!StringUtils.endsWith(base, File.separator)) {
			sb.append(File.separator);
		}
		
		sb.append(filename);
		sb.append(".ics");
		return sb.toString();
	}

	/**
	 * Create a URI to be used for a person's email address that degrades nicely if one is not defined
	 * @param email The email address as a string, can be empty or even <code>null</code>
	 * @return the URI object
	 */
	private URI createMailURI(String email) {
		if (email == null || email.isEmpty()) {
			return URI.create("noemail");
		} else {
			return URI.create("mailto:" + email);
		}
	}

	/**
	 * init
	 */
	public void init() {
		log.info("init");
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
}
