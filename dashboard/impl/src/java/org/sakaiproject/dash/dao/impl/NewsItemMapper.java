/**
 * 
 */
package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
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
		
		NewsItem newsItem = null;
		Long id = rs.getLong("ni_id");
		if(id == null) { 
			// this would be an error
		} else {
			newsItem = new NewsItem();
			newsItem.setNewsTimeLabelKey(rs.getString("ni_news_time_label_key"));
			newsItem.setTitle(rs.getString("ni_title"));
			newsItem.setEntityReference(rs.getString("ni_entity_ref"));
			newsItem.setSubtype(rs.getString("ni_subtype"));
			try {
				newsItem.setSticky(rs.getBoolean("nl_sticky"));
			} catch (SQLException e) {
				// this means that "nl_sticky" is not valid, so set sticky to false
				newsItem.setSticky(false);
			}
			try {
				newsItem.setItemCount(rs.getInt("ni_count"));
			} catch(SQLException e) {
				// this means that "ni_count" is not valid, so set itemCount to 1
				newsItem.setItemCount(1);
			}
		}
		
		newsItem.setId(id);
		newsItem.setNewsTime(rs.getTimestamp("ni_news_time"));
		
		// source_type
		SourceType sourceType = (SourceType) (new SourceTypeMapper()).mapRow(rs, rowNum);
		newsItem.setSourceType(sourceType);
		
		// context
		Context context = (Context) (new ContextMapper()).mapRow(rs, rowNum);
		newsItem.setContext(context);
		
		return newsItem;
	}

}
