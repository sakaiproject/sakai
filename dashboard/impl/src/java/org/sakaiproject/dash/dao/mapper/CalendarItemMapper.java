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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 *
 */
public class CalendarItemMapper implements RowMapper {
	
	private static Logger logger = LoggerFactory.getLogger(CalendarItemMapper.class);

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CalendarItem calendarItem = new CalendarItem();
		calendarItem.setId(rs.getLong("ci_id"));
		calendarItem.setCalendarTime(rs.getTimestamp("ci_calendar_time"));
		calendarItem.setCalendarTimeLabelKey(rs.getString("ci_calendar_time_label_key"));
		calendarItem.setTitle(rs.getString("ci_title"));
		calendarItem.setEntityReference(rs.getString("ci_entity_ref"));
		calendarItem.setSubtype(rs.getString("ci_subtype"));
		calendarItem.setSequenceNumber(rs.getInt("ci_sequence_num"));
		
		// repeating_event_id
		RepeatingCalendarItem repeatingCalendarItem = (RepeatingCalendarItem) (new RepeatingCalendarItemMapper()).mapRow(rs, rowNum);
		calendarItem.setRepeatingCalendarItem(repeatingCalendarItem);
		
		// source_type
		SourceType sourceType = (SourceType) (new SourceTypeMapper()).mapRow(rs, rowNum);
		calendarItem.setSourceType(sourceType);
		
		// context
		Context context = (Context) (new ContextMapper()).mapRow(rs, rowNum);
		calendarItem.setContext(context);
		
		//logger.info(calendarItem);
		
		return calendarItem;
	}

}
