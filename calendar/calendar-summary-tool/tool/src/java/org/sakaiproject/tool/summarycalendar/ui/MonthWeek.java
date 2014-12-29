/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;

public class MonthWeek implements Serializable {
	private static final long	serialVersionUID	= 8229021180637202724L;
	
	private Day[] days		= new Day[7];

	public MonthWeek() {

	}

	public Day getDay(int weekPos) {
		return days[weekPos];
	}
	
	public Day[] getDays(){
		return this.days;
	}

	public void setDay(int weekPos, Day day) {
		days[weekPos] = day;
	}
}
