/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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

package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * <p>
 * This is a abstract base class for Signup Email. It provides some must-have or
 * common used methods like getFooter()
 * </P>
 */
abstract public class SignupEmailBase implements SignupEmailNotification, MeetingTypes {

	@Getter @Setter
	private SakaiFacade sakaiFacade;
	
	@Getter
	protected SignupMeeting meeting;

	protected static ResourceLoader rb = new ResourceLoader("emailMessage");

	public static final String newline = "<BR>\r\n"; // System.getProperty("line.separator");\r\n

	public static final String space = " ";
	
	private static final int SITE_DESCRIPTION_DISPLAY_LENGTH=20;

	/** Indicates whether the email represents a cancellation - to be overwritten by subclasses */
	protected boolean cancellation = false;
	
	protected boolean modifyComment = false;

	/* footer for the email */
	protected String getFooter(String newline) {
		/* tag the message - HTML version */
		if(this.meeting.getCurrentSiteId()==null)
			return getFooterWithAccessUrl(newline);
		else
			return getFooterWithNoAccessUrl(newline);
	}

	/* footer for the email */
	protected String getFooter(String newline, String targetSiteId) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				"<a href=\"" + getSiteAccessUrl(targetSiteId) + "\">" + getSiteAccessUrl(targetSiteId) + "</a>",
				getSiteTitle(targetSiteId), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text"), params) + newline;

		return rv;
	}
	
	/* footer for the email */
	private String getFooterWithAccessUrl(String newline) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				"<a href=\"" + getSiteAccessUrl() + "\">" + getSiteAccessUrl() + "</a>", getSiteTitle(), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text"), params) + newline;

		return rv;
	}
	
	/* footer for the email */
	private String getFooterWithNoAccessUrl(String newline) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				getSiteTitle(), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text.no.access.link"), params) + newline;

		return rv;
	}

	/**
	 * get the email Header, which contains destination email address, subject
	 * etc.
	 */
	abstract public List<String> getHeader();

	/**
	 * get the main message for this email
	 */
	abstract public String getMessage();
	
	/**
	 * get the from address for this email
	 */
	abstract public String getFromAddress();
	
	/**
	 * get the subject for this email
	 */
	abstract public String getSubject();
	
	/**
	 * get current site Id
	 * 
	 * @return the current site Id
	 */
	protected String getSiteId() {
		String siteId = getSakaiFacade().getCurrentLocationId();
		if(SakaiFacade.NO_LOCATION.equals(siteId)){
			siteId =meeting.getCurrentSiteId()!=null? this.meeting.getCurrentSiteId() : SakaiFacade.NO_LOCATION;			
		}
		
		return siteId;
	}

	/* get the site name */
	protected String getSiteTitle() {
		return getSakaiFacade().getLocationTitle(getSiteId());
	}

	/* get the site name */
	protected String getSiteTitle(String targetSiteId) {
		return getSakaiFacade().getLocationTitle(targetSiteId);
	}
	
	/* get the site name */
	protected String getShortSiteTitle(String targetSiteId) {
		return getSakaiFacade().getLocationTitle(targetSiteId);
	}

	/* get the site name with a quotation mark */
	protected String getSiteTitleWithQuote() {
		return "\"" + getSiteTitle() + "\"";
	}

	/* get the site name with a quotation mark */
	protected String getSiteTitleWithQuote(String targetSiteId) {
		return "\"" + getSiteTitle(targetSiteId) + "\"";
	}
	
	/* get the site name with a quotation mark */
	protected String getShortSiteTitleWithQuote(String targetSiteId) {
		return "\"" + getShortSiteTitle(targetSiteId) + "\"";
	}

	/* get the link to access the current-site signup tool page in a site */
	protected String getSiteAccessUrl() {
		// TODO May have efficiency issue with getPageId
		String siteUrl = getSakaiFacade().getServerConfigurationService().getPortalUrl() + "/site/" + getSiteId()
				+ "/page/" + getSakaiFacade().getCurrentPageId();
		return siteUrl;
	}

	/* get the link to access corresponding site - signup tool page in a site */
	protected String getSiteAccessUrl(String targetSiteId) {
		// TODO May have efficiency issue with getPageId
		String siteUrl = getSakaiFacade().getServerConfigurationService().getPortalUrl() + "/site/" + targetSiteId
				+ "/page/" + getSakaiFacade().getSiteSignupPageId(targetSiteId);
		return siteUrl;
	}
	
	/* Get the meeting title, max length of 30 chars (with ellipsis where required) */
	protected String getAbbreviatedMeetingTitle(){
		return StringUtils.abbreviate(meeting.getTitle(), 30);
	}

	/**
	 * This will convert the Java date object to a Sakai's Time object, which
	 * provides all the useful methods for output.
	 * 
	 * @param date
	 *            a Java Date object.
	 * @return a Sakai's Time object.
	 */
	protected Time getTime(Date date) {
		Time time = getSakaiFacade().getTimeService().newTime(date.getTime());
		return time;
	}

	/**
	 * Make first letter of the string to Capital letter
	 * 
	 * @param st
	 *            a string value
	 * @return a string with a first capital letter
	 */
	protected String makeFirstCapLetter(String st) {
		return StringUtils.capitalize(st);
	}

	static private String myServiceName = null;

	protected String getServiceName() {
		/* first look at email bundle and then sakai.properties. 
		 * it will allow user to define different 'ui.service' value */
		if (myServiceName == null) {
			try {
				if(rb.keySet().contains("ui.service"))
					myServiceName = rb.getString("ui.service");
				else
					myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service",
							"Sakai Service");
			} catch (Exception e) {
				myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service",
						"Sakai Service");
			}
		}

		return myServiceName;

	}
	
	
	protected String getRepeatTypeMessage(SignupMeeting meeting){
		String recurFrqs ="";
		if (DAILY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatDaily");
		else if (WEEKDAYS.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekdays");
		else if (WEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekly");
		else if (BIWEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatBiWeekly");
		else
			recurFrqs = rb.getString("body.meeting.unknown.repeatType");
		
		return recurFrqs;
	}
	
	protected String getServerFromAddress() {
		return  getServiceName() +" <" + getSakaiFacade().getServerConfigurationService().getString("setup.request", "no-reply@"  + getSakaiFacade().getServerConfigurationService().getServerName())+ ">";
	}

	protected boolean userIsAttendingTimeslot(User user, SignupTimeslot timeslot) {
		return timeslot.getAttendee(user.getId()) != null;
	}

	protected List<VEvent> eventsWhichUserIsAttending(User user) {
		final List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots();
		List<VEvent> events = new ArrayList<VEvent>();
		for (SignupTimeslot timeslot : timeslots) {
			if (userIsAttendingTimeslot(user, timeslot)) {
				final VEvent event = timeslot.getVevent();
				if (event != null) {
					events.add(event);
				}
			}
		}
		return events;
	}

	public boolean isCancellation() {
		return cancellation;
	}

	public boolean isModifyComment() {
		return modifyComment;
	}

	public void setModifyComment(boolean modifyComment) {
		this.modifyComment = modifyComment;
	}
}
