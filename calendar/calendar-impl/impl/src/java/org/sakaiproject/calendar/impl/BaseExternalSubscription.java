package org.sakaiproject.calendar.impl;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.ExternalCalendarSubscription;
import org.sakaiproject.entity.api.Entity;

public class BaseExternalSubscription implements ExternalSubscription {
	private String subscriptionName;

	private String subscriptionUrl;

	private String context;

	private Calendar calendar;

	private boolean isInstitutional;

	public BaseExternalSubscription() {
	}

	public BaseExternalSubscription(String subscriptionName,
			String subscriptionUrl, String context, Calendar calendar,
			boolean isInstitutional) {
		setSubscriptionName(subscriptionName);
		setSubscriptionUrl(subscriptionUrl);
		setCalendar(calendar);
		setContext(context);
		setInstitutional(isInstitutional);
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
			((ExternalCalendarSubscription) calendar).setContext(context);
	}

	public String getReference() {
		if (calendar != null)
			return calendar.getReference();
		else
			return calendarSubscriptionReference(context,
					getIdFromSubscriptionUrl(subscriptionUrl));
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public boolean isInstitutional() {
		return isInstitutional;
	}

	public void setInstitutional(boolean isInstitutional) {
		this.isInstitutional = isInstitutional;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseExternalSubscription)
			return getReference().equals(
					((BaseExternalSubscription) o).getReference());
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if (getReference() != null) {
			hashCode += getReference().hashCode();
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
		byte[] encoded = Base64.encodeBase64(url.getBytes());
		// '/' cannot be used in Reference => use '.' instead (not part of
		// Base64 alphabet)
		String encStr = new String(encoded).replaceAll("/", "\\.");
		return encStr;
	}

	public static String getSubscriptionUrlFromId(String id) {
		// use Base64
		byte[] decoded = Base64.decodeBase64(id.replaceAll("\\.", "/")
				.getBytes());
		return new String(decoded);
	}

	public static String calendarSubscriptionReference(String context, String id)
	{
		return CalendarService.REFERENCE_ROOT + Entity.SEPARATOR
				+ CalendarService.REF_TYPE_CALENDAR_SUBSCRIPTION + Entity.SEPARATOR
				+ context + Entity.SEPARATOR + id;
	}
}