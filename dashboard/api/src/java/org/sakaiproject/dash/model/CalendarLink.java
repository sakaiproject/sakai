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

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.Person;

/**
 * CalendarLink encapsulates links between calendar items and users.
 *
 */
public class CalendarLink implements Serializable {
	
	protected Long id;
	protected Person person;
	protected CalendarItem calendarItem;
	protected Context context;
	protected boolean hidden = false;
	protected boolean sticky = false;

	/**
	 * 
	 */
	public CalendarLink() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param person
	 * @param calendarItem
	 * @param context
	 * @param hidden
	 * @param sticky
	 */
	public CalendarLink(Person person, CalendarItem calendarItem,
			Context context, boolean hidden, boolean sticky) {
		super();
		this.person = person;
		this.calendarItem = calendarItem;
		this.context = context;
		this.hidden = hidden;
		this.sticky = sticky;
}

	/**
	 * @param id
	 * @param person
	 * @param calendarItem
	 * @param context
	 * @param realm
	 * @param hidden
	 * @param sticky
	 */
	public CalendarLink(Long id, Person person, CalendarItem calendarItem,
			Context context, boolean hidden, boolean sticky) {
		super();
		this.id = id;
		this.person = person;
		this.calendarItem = calendarItem;
		this.context = context;
		this.hidden = hidden;
		this.sticky = sticky;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @return the calendarItem
	 */
	public CalendarItem getCalendarItem() {
		return calendarItem;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return the sticky
	 */
	public boolean isSticky() {
		return sticky;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @param calendarItem the calendarItem to set
	 */
	public void setCalendarItem(CalendarItem calendarItem) {
		this.calendarItem = calendarItem;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param sticky the sticky to set
	 */
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CalendarLink [id=");
		builder.append(id);
		builder.append(", person=");
		builder.append(person);
		builder.append(", calendarItem=");
		builder.append(calendarItem);
		builder.append(", context=");
		builder.append(context);
		builder.append(", hidden=");
		builder.append(hidden);
		builder.append(", sticky=");
		builder.append(sticky);
		builder.append("]");
		return builder.toString();
	}

}
