/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.model;

import java.io.Serializable;
import java.util.Date;


/**
 * CalendarItem encapsulates all information about dashboard items to 
 * appear in the "Calendar" section of users' dashboards.
 *
 */
public class CalendarItem implements Serializable {
	
	protected Long id;
	protected String title;
	protected Date calendarTime;
	protected String calendarTimeLabelKey;
	protected String entityReference;
	protected String entityUrl;
	protected Context context;
	protected SourceType sourceType;
	
	/**
	 * 
	 */
	public CalendarItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, String entityUrl,
			Context context, SourceType sourceType) {
		super();
		this.title = title;
		this.calendarTime = calendarTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.entityUrl = entityUrl;
		this.context = context;
		this.sourceType = sourceType;
	}

	/**
	 * @param id
	 * @param title
	 * @param calendarTime
	 * @param calendarTimeLabelKey TODO
	 * @param entityReference
	 * @param entityUrl
	 * @param context
	 * @param sourceType
	 * @param realm
	 */
	public CalendarItem(Long id, String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, String entityUrl,
			Context context, SourceType sourceType) {
		super();
		this.id = id;
		this.title = title;
		this.calendarTime = calendarTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.entityUrl = entityUrl;
		this.context = context;
		this.sourceType = sourceType;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the calendarTime
	 */
	public Date getCalendarTime() {
		return calendarTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCalendarTimeLabelKey() {
		return this.calendarTimeLabelKey;
	}

	/**
	 * @return the entityReference
	 */
	public String getEntityReference() {
		return entityReference;
	}

	/**
	 * @return the entityUrl
	 */
	public String getEntityUrl() {
		return entityUrl;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the sourceType
	 */
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param calendarTime the calendarTime to set
	 */
	public void setCalendarTime(Date calendarTime) {
		this.calendarTime = calendarTime;
	}
	
	public void setCalendarTimeLabelKey(String calendarTimeLabelKey) {
		this.calendarTimeLabelKey = calendarTimeLabelKey;
	}

	/**
	 * @param entityReference the entityReference to set
	 */
	public void setEntityReference(String entityReference) {
		this.entityReference = entityReference;
	}

	/**
	 * @param entityUrl the entityUrl to set
	 */
	public void setEntityUrl(String entityUrl) {
		this.entityUrl = entityUrl;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CalendarItem [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", calendarTime=");
		builder.append(calendarTime);
		builder.append(", calendarTimeLabelKey=");
		builder.append(calendarTimeLabelKey);
		builder.append(", entityReference=");
		builder.append(entityReference);
		builder.append(", entityUrl=");
		builder.append(entityUrl);
		builder.append(", context=");
		builder.append(context);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

}
