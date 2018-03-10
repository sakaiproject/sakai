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
package org.sakaiproject.calendar.impl;

import lombok.Data;
import lombok.Value;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalSubscriptionDetails;
import org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.ExternalCalendarSubscription;
import org.sakaiproject.entity.api.Entity;

import java.time.Instant;
import java.util.Base64;

/**
 * Equaly just matches on the reference any nothing else.
 */
public class BaseExternalSubscriptionDetails implements ExternalSubscriptionDetails {
	private String subscriptionName;

	private String subscriptionUrl;

	private String context;

	private ExternalCalendarSubscription calendar;

	private boolean isInstitutional;

	private Status status;
	
	private String userId;
	
	private String tzid;

	public BaseExternalSubscriptionDetails() {
	}

	/**
	 * Copy constructor.
	 * @param other
	 */
	public BaseExternalSubscriptionDetails(BaseExternalSubscriptionDetails other) {
		this.subscriptionName = other.subscriptionName;
		this.subscriptionUrl = other.subscriptionUrl;
		this.context = other.context;
		this.calendar = other.calendar;
		this.isInstitutional = other.isInstitutional;
		this.status = other.status;
		this.userId = other.userId;
		this.tzid = other.tzid;
	}

	/**
	 * Creates an instance that hasn't gone out to the network so we don't know
	 * the status of it.
	 */
	public BaseExternalSubscriptionDetails(String subscriptionName,
										   String subscriptionUrl, String context, ExternalCalendarSubscription calendar,
										   boolean isInstitutional) {
		setSubscriptionName(subscriptionName);
		setSubscriptionUrl(subscriptionUrl);
		setCalendar(calendar);
		setContext(context);
		setInstitutional(isInstitutional);
	}
	
	public BaseExternalSubscriptionDetails(String subscriptionName,
			   String subscriptionUrl, String context, ExternalCalendarSubscription calendar,
			   boolean isInstitutional, String userId, String tzid) {
	    this(subscriptionName, subscriptionUrl, context, calendar, isInstitutional);
		this.userId = userId;
		this.tzid = tzid;
	}
	
	public BaseExternalSubscriptionDetails(String subscriptionName,
			   String subscriptionUrl, String context, ExternalCalendarSubscription calendar,
			   boolean isInstitutional, String userId, String tzid, boolean ok, String error, Instant instant) {
		this(subscriptionName, subscriptionUrl, context, calendar, isInstitutional, userId, tzid);
		status = new Status(ok, error, instant);
	}

	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	public String getSubscriptionUrl() {
		return subscriptionUrl;
	}

	public void setSubscriptionUrl(String subscriptionUrl) {
		this.subscriptionUrl = subscriptionUrl;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
		if (calendar != null)
			calendar.setContext(context);
	}

	public String getReference() {
		if (calendar != null)
			return calendar.getReference();
		else
			return calendarSubscriptionReference(context,
					getIdFromSubscriptionUrl(subscriptionUrl));
	}

	public ExternalCalendarSubscription getCalendar() {
		return calendar;
	}

	public void setCalendar(ExternalCalendarSubscription calendar) {
		this.calendar = calendar;
	}

	public boolean isInstitutional() {
		return isInstitutional;
	}

	public void setInstitutional(boolean isInstitutional) {
		this.isInstitutional = isInstitutional;
	}

	public State getState() {
		return status != null?
				status.ok?State.LOADED:State.FAILED:
				State.UNKNOWN;
	}

	public Instant getRefreshed() {
		return status != null? status.getRefreshed(): null;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTzid() {
		return tzid;
	}
	
	public void setTzid(String tzid) {
		this.tzid = tzid;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseExternalSubscriptionDetails)
			return getReference().equals(
					((BaseExternalSubscriptionDetails) o).getReference());
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if (getReference() != null) {
			hashCode = getReference().hashCode();
		}
		;
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		buff.append(getSubscriptionName() != null ? getSubscriptionName() : "");
		buff.append('|');
		buff.append(getSubscriptionUrl());
		buff.append('|');
		buff.append(getReference());
		return buff.toString();
	}

	public static String getIdFromSubscriptionUrl(String url)
	{
		// use Base64
		byte[] encoded = Base64.getEncoder().encode(url.getBytes());
		// '/' cannot be used in Reference => use '.' instead (not part of
		// Base64 alphabet)
		String encStr = new String(encoded).replaceAll("/", "\\.");
		return encStr;
	}

	public static String getSubscriptionUrlFromId(String id) {
		// use Base64
		byte[] decoded = Base64.getDecoder().decode(id.replaceAll("\\.", "/").getBytes());
		return new String(decoded);
	}

	public static String calendarSubscriptionReference(String context, String id)
	{
		return CalendarService.REFERENCE_ROOT + Entity.SEPARATOR
				+ CalendarService.REF_TYPE_CALENDAR_SUBSCRIPTION + Entity.SEPARATOR
				+ context + Entity.SEPARATOR + id;
	}

	/**
	 * When a calendar is attempted to be loaded this holds details about the load.
	 */
	@Value
	public class Status {
		private boolean ok;
		private String error;
		private Instant refreshed;
	}
}