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

import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 *
 */
public class RepeatingCalendarItemMapper implements RowMapper {

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		// rep_id, rep_first_time, rep_last_time, rep_frequency, rep_count, rep_calendar_time_label_key, rep_title,
		
		RepeatingCalendarItem calendarItem = new RepeatingCalendarItem();
		calendarItem.setId(rs.getLong("rep_id"));
		calendarItem.setFirstTime(rs.getTimestamp("rep_first_time"));
		calendarItem.setLastTime(rs.getTimestamp("rep_last_time"));
		calendarItem.setFrequency(rs.getString("rep_frequency"));
		calendarItem.setMaxCount(rs.getInt("rep_count"));
		calendarItem.setCalendarTimeLabelKey(rs.getString("rep_calendar_time_label_key"));
		calendarItem.setTitle(rs.getString("rep_title"));
		calendarItem.setEntityReference(rs.getString("rep_entity_ref"));
		calendarItem.setSubtype(rs.getString("rep_subtype"));
		
		
		// source_type
		SourceType sourceType = (SourceType) (new SourceTypeMapper()).mapRow(rs, rowNum);
		calendarItem.setSourceType(sourceType);
		
		// context
		Context context = (Context) (new ContextMapper()).mapRow(rs, rowNum);
		calendarItem.setContext(context);
		
		return calendarItem;
	}


}
