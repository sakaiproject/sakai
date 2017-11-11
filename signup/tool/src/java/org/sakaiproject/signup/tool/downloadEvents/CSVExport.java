/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

package org.sakaiproject.signup.tool.downloadEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

public class CSVExport {

	List<SignupMeetingWrapper> wrappers;
	SakaiFacade sakaiFacade;
	
	private ResourceLoader rb = new ResourceLoader("messages");
	
	public CSVExport(List<SignupMeetingWrapper> meetingWrappers, SakaiFacade sakaiFacade) {
		this.wrappers = meetingWrappers;
		this.sakaiFacade = sakaiFacade;
	}
	
	/**
	 * Generates the header row for the CSV file
	 * @return
	 */
	public List<String> getHeaderRow() {
		
		List<String> header = new ArrayList<String>();
		
		header.add(rb.getString("wksheet_meeting_name", "Event Name"));
		header.add(rb.getString("wksheet_user_name", "Attendee Name"));
		header.add(rb.getString("wksheet_user_id", "Attendee User Id"));
		header.add(rb.getString("wksheet_user_email", "Email"));
		header.add(rb.getString("wksheet_site_name", "Site Title"));
		header.add(rb.getString("wksheet_appointment_start_time", "Appointment Time"));
		header.add(rb.getString("wksheet_appointment_duration", "Duration (min)"));
		header.add(rb.getString("wksheet_num_of_attendees", "#Num Attendees in Slot"));
		header.add(rb.getString("wksheet_user_comment", "User Comment"));
		header.add(rb.getString("wksheet_organizer", "Organizer"));
		header.add(rb.getString("wksheet_location", "Location"));
		header.add(rb.getString("wksheet_category", "Category"));
		header.add(rb.getString("wksheet_meeting_start_time", "Event Start Time"));
		header.add(rb.getString("wksheet_meeting_duration", "Event Duration (min)"));
		
		return header;
	}
	
	/**
	 * Generates the data rows for the CSV file
	 * @return
	 */
	public List<List<String>> getDataRows() {
		
		ArrayList<List<String>> data = new ArrayList<List<String>>();
		
		for (SignupMeetingWrapper wrp : wrappers) {
			List<SignupTimeslot> tsItems = wrp.getMeeting().getSignupTimeSlots();
			if (tsItems != null) {
				for (SignupTimeslot tsItem : tsItems) {
					/*strange thing happen for hibernate, tsItem can be null for mySql 4.x*/
					List<SignupAttendee> attendees = tsItem == null ? null : getValidAttendees(tsItem.getAttendees());
					if (attendees != null) {
						for (SignupAttendee att : attendees) {
							
							List<String> attendance = new ArrayList<String>();
							
							User attendee = sakaiFacade.getUser(att.getAttendeeUserId());
							
							attendance.add(wrp.getMeeting().getTitle());

							attendance.add(attendee ==null? "--" :attendee.getDisplayName());

							attendance.add(attendee ==null? "--" : attendee.getEid());

							attendance.add(attendee ==null? "--" : attendee.getEmail());

							attendance.add(getSiteTitle(att.getSignupSiteId()));
	
							attendance.add(sakaiFacade.getTimeService().newTime(tsItem.getStartTime().getTime()).toStringLocalFull());

							attendance.add(String.valueOf(getDurationLength(tsItem.getEndTime(), tsItem.getStartTime())));

							attendance.add(String.valueOf(getValidAttendees(tsItem.getAttendees()).size()));

							attendance.add(att.getComments());

							attendance.add(sakaiFacade.getUserDisplayName(wrp.getMeeting().getCreatorUserId()));

							attendance.add(wrp.getMeeting().getLocation());
							
							attendance.add(wrp.getMeeting().getCategory());

							attendance.add(sakaiFacade.getTimeService().newTime(wrp.getMeeting().getStartTime().getTime()).toStringLocalFull());

							attendance.add(String.valueOf(getDurationLength(wrp.getMeeting().getEndTime(), wrp.getMeeting().getStartTime())));
							
							data.add(attendance);
						}
					}
				}

			}
		}

		return data;
	}
	
	/**
	 * Clean the list of attendees by checking that each user is valid. This is a duplicate of the SignupUIBaseBean method.
	 * @param attendees     List of attendees to be cleaned
	 * @return      the cleaned list
	 */
	private List<SignupAttendee> getValidAttendees(List<SignupAttendee> attendees) {
		List<SignupAttendee> cleanedList = new ArrayList<SignupAttendee>();

		for(SignupAttendee attendee: attendees){
			if(sakaiFacade.checkForUser(attendee.getAttendeeUserId())) {
				cleanedList.add(attendee);
			}
		}
		return cleanedList;
	}
	
	/**
	 * Get site title given a site id
	 * @param siteId
	 * @return
	 */
	private String getSiteTitle(String siteId) {
		Site site;
		try {
			site = sakaiFacade.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			return "";
		}
		String title = site.getTitle();
		return title;
	}
	
	/**
	 * Get duration in minutes between two dates
	 * @param endTime
	 * @param startTime
	 * @return
	 */
	private double getDurationLength(Date endTime, Date startTime) {
		double duration = 0;
		duration = endTime.getTime() - startTime.getTime();
		duration = duration / (1000 * 60);// minutes
		return duration;
	}
	
}
