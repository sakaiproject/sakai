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
 * http://www.osedu.org/licenses/ECL-2.0 
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 *
 */
public class RepeatingCalendarItem implements Serializable {
	
	public static final String REPEATS_WEEKLY = "weekly";
	public static final String REPEATS_MONTHLY = "monthly";
	public static final String REPEATS_DAILY = "daily";
	public static final String REPEATS_YEARLY = "yearly";
	
	public static final String EVERY_MONDAY = "MONDAY";
	public static final String EVERY_TUESDAY = "TUESDAY";
	public static final String EVERY_WEDNESDAY = "WEDNESDAY";
	public static final String EVERY_THURSDAY = "THURSDAY";
	public static final String EVERY_FRIDAY = "FRIDAY";
	public static final String EVERY_SATURDAY = "SATURDAY";
	public static final String EVERY_SUNDAY = "SUNDAY";
	
	protected Long id;
	protected String title;
	protected Date firstTime;
	protected String calendarTimeLabelKey;
	protected String entityReference;
	protected Context context;
	protected SourceType sourceType;
	protected String subtype;
	protected String frequency;
	protected int maxCount;
	protected Date lastTime;
	
	protected List<Integer> exclusions = new ArrayList<Integer>();
	protected List<CalendarItem> instances = new ArrayList<CalendarItem>(); 

	public RepeatingCalendarItem() {
		super();
	}

	/**
	 * @param title
	 * @param firstTime
	 * @param lastTime 
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param subtype TODO
	 * @param context
	 * @param sourceType
	 * @param frequency 
	 * @param maxCount 
	 */
	public RepeatingCalendarItem(String title, Date firstTime,
			Date lastTime, String calendarTimeLabelKey,
			String entityReference, String subtype, Context context, SourceType sourceType, String frequency, int maxCount) {
		super();
		this.title = title;
		this.firstTime = firstTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.frequency = frequency;
		this.maxCount = maxCount;
		this.lastTime = lastTime;
		this.subtype = subtype;
	}
	
	/**
	 * @param id
	 * @param title
	 * @param firstTime
	 * @param lastTime 
	 * @param calendarTimeLabelKey
	 * @param entityReference
	 * @param subtype TODO
	 * @param context
	 * @param sourceType
	 * @param frequency 
	 * @param maxCount 
	 */
	public RepeatingCalendarItem(Long id, String title, Date firstTime,
			Date lastTime, String calendarTimeLabelKey,
			String entityReference, String subtype, Context context, SourceType sourceType, String frequency, int maxCount) {
		super();
		this.id = id;
		this.title = title;
		this.firstTime = firstTime;
		this.calendarTimeLabelKey = calendarTimeLabelKey;
		this.entityReference = entityReference;
		this.context = context;
		this.sourceType = sourceType;
		this.frequency = frequency;
		this.maxCount = maxCount;
		this.lastTime = lastTime;
		this.subtype = subtype;
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
	public Date getFirstTime() {
		return firstTime;
	}

	/**
	 * @return the calendarTimeLabelKey
	 */
	public String getCalendarTimeLabelKey() {
		return calendarTimeLabelKey;
	}

	/**
	 * @return the entityReference
	 */
	public String getEntityReference() {
		return entityReference;
	}

	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
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
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * @return the count
	 */
	public int getMaxCount() {
		return maxCount;
	}

	/**
	 * @return the lastTime
	 */
	public Date getLastTime() {
		return lastTime;
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
	 * @param firstTime the calendarTime to set
	 */
	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	/**
	 * @param calendarTimeLabelKey the calendarTimeLabelKey to set
	 */
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
	 * @param subtype the subtype to set
	 */
	public void setSubtype(String subtype) {
		this.subtype = subtype;
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

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @param maxCount the count to set
	 */
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	/**
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RepeatingCalendarItem [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", firstTime=");
		builder.append(firstTime);
		builder.append(", lastTime=");
		builder.append(lastTime);
		builder.append(", frequency=");
		builder.append(frequency);
		builder.append(", maxCount=");
		builder.append(maxCount);
		builder.append(", calendarTimeLabelKey=");
		builder.append(calendarTimeLabelKey);
		builder.append(", entityReference=");
		builder.append(entityReference);
		builder.append(", subtype=");
		builder.append(subtype);
		builder.append(", context=");
		builder.append(context);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}


}
