/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.coursemanagement.api;

import java.sql.Time;

/**
 * A time and a place for a Section to meet.  Meetings are completely controlled by
 * their sections.  To add a Meeting to a Section, call section.getMeetings() and operate
 * on the List of meetings.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface Meeting {
	public String getLocation();
	public void setLocation(String location);
	public Time getStartTime();
	public void setStartTime(Time startTime);
	public Time getFinishTime();
	public void setFinishTime(Time finishTime);
	public String getNotes();
	public void setNotes(String notes);
	public boolean isFriday();
	public void setFriday(boolean friday);
	public boolean isMonday();
	public void setMonday(boolean monday);
	public boolean isSaturday();
	public void setSaturday(boolean saturday);
	public boolean isSunday();
	public void setSunday(boolean sunday);
	public boolean isThursday();
	public void setThursday(boolean thursday);
	public boolean isTuesday();
	public void setTuesday(boolean tuesday);
	public boolean isWednesday();
	public void setWednesday(boolean wednesday);
}
