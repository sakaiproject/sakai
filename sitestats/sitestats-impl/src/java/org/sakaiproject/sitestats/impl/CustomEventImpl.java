/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;


public class CustomEventImpl implements Event {
	private Date	date;
	private String	event;
	private String	ref;
	private String	context;
	private String	sessionUser;
	private String	sessionId;
	private boolean	modify;
	private LRS_Statement lrsStatement;

	public CustomEventImpl(Date date, String event, String ref, String sessionUser, String sessionId) {
		this(date, event, ref, null, sessionUser, sessionId, 'm');
	}

	public CustomEventImpl(Date date, String event, String ref, String context, String sessionUser, String sessionId) {
		this(date, event, ref, context, sessionUser, sessionId, 'm');
	}

	public CustomEventImpl(Date date, String event, String ref, String context, String sessionUser, String sessionId, char eventCode) {
		this(date, event, ref, context, sessionUser, sessionId, 'm',null);
	}
	public CustomEventImpl(Date date, String event, String ref, String context, String sessionUser, String sessionId, char eventCode, LRS_Statement lrsStatement) {
		this.date = date;
		this.event = event;
		this.ref = ref;
		this.context = context;
		this.sessionUser = sessionUser;
		this.sessionId = sessionId;
		this.modify = ('m' == eventCode);
		this.lrsStatement = lrsStatement;
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
	
	public String getContext() {
		return context;
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

	public Date getEventTime() {
		return date;
	}
	
	public String toString(){
		return new ToStringBuilder(this).
	       append("date", date).
	       append("event", event).
	       append("ref", ref).
	       append("context", context).
	       append("sessionUser", sessionUser).
	       append("sessionId", sessionId).
	       append("modify", modify).
	       toString();
	}

	@Override
	public LRS_Statement getLrsStatement() {
		// TODO Auto-generated method stub
		return null;
	}
}
