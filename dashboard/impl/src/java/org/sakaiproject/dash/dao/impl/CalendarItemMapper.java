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
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		CalendarItem calendarItem = new CalendarItem();
		calendarItem.setId(rs.getLong("ci_id"));
		calendarItem.setCalendarTime(rs.getDate("ci_calendar_time"));
		calendarItem.setTitle(rs.getString("ci_title"));
		calendarItem.setEntityUrl(rs.getString("ci_access_url"));
		calendarItem.setEntityReference(rs.getString("ci_entity_ref"));
		
		// source_type
		SourceType sourceType = new SourceType();
		sourceType.setId(rs.getLong("type_id"));
		sourceType.setName(rs.getString("type_name"));
		calendarItem.setSourceType(sourceType);
		
		// context
		Context context = new Context();
		context.setId(rs.getLong("site_id"));
		context.setContextId(rs.getString("site_context_id"));
		context.setContextTitle(rs.getString("site_context_title"));
		context.setContextUrl(rs.getString("site_context_url"));
		calendarItem.setContext(context);
		
		// realm
		Realm realm = new Realm();
		realm.setId(rs.getLong("r_id"));
		realm.setRealmId(rs.getString("r_realm_id"));
		calendarItem.setRealm(realm);
		
		return calendarItem;
	}

}
