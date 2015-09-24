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
	protected Context context;
	protected SourceType sourceType;
	protected String subtype;
	protected RepeatingCalendarItem repeatingCalendarItem = null;
	protected Integer sequenceNumber = null;
	protected String infoLinkURL;
	
	
	/**
	 * 
	 */
	public CalendarItem() {
		super();
	}

	/**
	 * 
	 * @param title
	 * @param calendarTime
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param subtype TODO
	 * @param repeatingCalendarItem
	 * @param sequenceNumber TODO
	 */
	public CalendarItem(String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, Context context,
			SourceType sourceType, String subtype, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber) {
		super();
		this.title = title;
		this.calendarTime = calendarTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.subtype = subtype;
		this.repeatingCalendarItem = repeatingCalendarItem;
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @param id
	 * @param title
	 * @param calendarTime
	 * @param calendarTimeLabelKey 
	 * @param entityReference
	 * @param context
	 * @param sourceType
	 * @param subtype TODO
	 * @param repeatingCalendarItem 
	 * @param sequenceNumber TODO
	 * @param realm
	 */
	public CalendarItem(Long id, String title, Date calendarTime,
			String calendarTimeLabelKey, String entityReference, Context context, SourceType sourceType, 
			String subtype, RepeatingCalendarItem repeatingCalendarItem, Integer sequenceNumber) {
		super();
		this.id = id;
		this.title = title;
		this.calendarTime = calendarTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.subtype = subtype;
		this.repeatingCalendarItem = repeatingCalendarItem;
		this.sequenceNumber = sequenceNumber;
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
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the repeatingCalendarItem
	 */
	public RepeatingCalendarItem getRepeatingCalendarItem() {
		return repeatingCalendarItem;
	}

	/**
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @return the sourceType
	 */
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
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
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @param repeatingCalendarItem the repeatingCalendarItem to set
	 */
	public void setRepeatingCalendarItem(RepeatingCalendarItem repeatingCalendarItem) {
		this.repeatingCalendarItem = repeatingCalendarItem;
	}

	/**
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * @param subtype the subtype to set
	 */
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getInfoLinkURL() {
		return infoLinkURL;
	}

	public void setInfoLinkURL(String infoLinkURL) {
		this.infoLinkURL = infoLinkURL;
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
		builder.append(", context=");
		builder.append(context);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append(", subtype=");
		builder.append(subtype);
		builder.append(", repeatingCalendarItem=");
		builder.append(repeatingCalendarItem);
		builder.append(", sequenceNumber=");
		builder.append(sequenceNumber);
		builder.append("]");
		return builder.toString();
	}

}
