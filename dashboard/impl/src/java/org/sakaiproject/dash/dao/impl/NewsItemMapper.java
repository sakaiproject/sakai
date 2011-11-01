/**
 * 
 */
package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.NewsItem;
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
public class NewsItemMapper implements RowMapper {

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		NewsItem newsItem = new NewsItem();
		newsItem.setId(rs.getLong("ni_id"));
		newsItem.setNewsTime(rs.getTimestamp("ni_news_time"));
		newsItem.setNewsTimeLabelKey(rs.getString("ni_news_time_label_key"));
		newsItem.setTitle(rs.getString("ni_title"));
		newsItem.setEntityReference(rs.getString("ni_entity_ref"));
		newsItem.setSubtype(rs.getString("ni_subtype"));
		
		// source_type
		SourceType sourceType = (SourceType) (new SourceTypeMapper()).mapRow(rs, rowNum);
		newsItem.setSourceType(sourceType);
		
		// context
		Context context = (Context) (new ContextMapper()).mapRow(rs, rowNum);
		newsItem.setContext(context);
		
		return newsItem;
	}

}
