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

package org.sakaiproject.dash.entity;

import java.util.Date;
import java.util.Map;

/**
 * A RepeatingEventGenerator is an DashboardEntityInfo that can add repeating calendar items 
 * to the calendar.  It provides a method to identify dates on which those repeating
 * calendar items will occur. 
 *
 */
public interface RepeatingEventGenerator extends DashboardEntityInfo {
	
	/**
	 * Returns a list of times at which the repeating event occurs 
	 * between the beginDate and the endDate.  The list is filtered
	 * to eliminate any previously excluded events. 
	 * @param entityReference
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public Map<Integer,Date> generateRepeatingEventDates(String entityReference, Date beginDate, Date endDate);

}
