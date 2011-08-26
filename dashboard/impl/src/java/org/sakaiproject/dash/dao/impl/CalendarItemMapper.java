/**
 * 
 */
package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;

import org.springframework.jdbc.core.RowMapper;

/**
 * 
 *
 */
public class CalendarItemMapper implements RowMapper {

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CalendarItem calendarItem = new CalendarItem();
		calendarItem.setId(rs.getLong("ci_id"));
		calendarItem.setCalendarTime(rs.getTimestamp("ci_calendar_time"));
		calendarItem.setTitle(rs.getString("ci_title"));
		calendarItem.setEntityUrl(rs.getString("ci_access_url"));
		calendarItem.setEntityReference(rs.getString("ci_entity_ref"));
		
		// source_type
		SourceType sourceType = (SourceType) (new SourceTypeMapper()).mapRow(rs, rowNum);
		calendarItem.setSourceType(sourceType);
		
		// context
		Context context = (Context) (new ContextMapper()).mapRow(rs, rowNum);
		calendarItem.setContext(context);
		
		return calendarItem;
	}

}
