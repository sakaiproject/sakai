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

package org.sakaiproject.dash.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Person;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 *
 */
public class CalendarLinkMapper implements RowMapper {

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CalendarLink link = new CalendarLink();
		
		CalendarItem calendarItem = (CalendarItem) (new CalendarItemMapper()).mapRow(rs, rowNum);
		link.setCalendarItem(calendarItem);
		
		link.setContext(calendarItem.getContext());
		
		// person
		Person person = (Person) (new PersonMapper()).mapRow(rs, rowNum);
		link.setPerson(person);

		link.setId(rs.getLong("link_id"));
		link.setHidden(rs.getBoolean("link_hidden"));
		link.setSticky(rs.getBoolean("link_sticky"));

		return link;
	}

}
