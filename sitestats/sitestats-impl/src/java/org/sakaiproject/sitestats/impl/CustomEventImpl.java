package org.sakaiproject.sitestats.impl;

import java.util.Date;

import org.sakaiproject.event.api.Event;


public class CustomEventImpl implements Event {
	private Date	date;
	private String	event;
	private String	ref;
	private String	sessionUser;
	private String	sessionId;
	private boolean	modify;

	public CustomEventImpl(Date date, String event, String ref, String sessionUser, String sessionId) {
		this(date, event, ref, sessionUser, sessionId, 'm');
	}

	public CustomEventImpl(Date date, String event, String ref, String sessionUser, String sessionId, char eventCode) {
		this.date = date;
		this.event = event;
		this.ref = ref;
		this.sessionUser = sessionUser;
		this.sessionId = sessionId;
		this.modify = ('m' == eventCode);
	}
	
	public Date getDate() {
		return date;
	}

	public String getEvent() {
		return event;
	}

	public String getResource() {
		return ref;
	}

	public String getUserId() {
		return sessionUser;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public boolean getModify() {
		return modify;
	}

	public int getPriority() {
		return 0;
	}
}
