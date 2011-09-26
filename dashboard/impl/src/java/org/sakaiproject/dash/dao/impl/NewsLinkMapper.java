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
public class NewsLinkMapper implements RowMapper {

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		NewsLink link = new NewsLink();
		
		NewsItem newsItem = (NewsItem) (new NewsItemMapper()).mapRow(rs, rowNum);
		link.setNewsItem(newsItem);
		
		link.setContext(newsItem.getContext());
		
		// person
		Person person = (Person) (new PersonMapper()).mapRow(rs, rowNum);
		link.setPerson(person);

		link.setId(rs.getLong("link_id"));
		link.setHidden(rs.getBoolean("link_hidden"));
		link.setSticky(rs.getBoolean("link_sticky"));

		return link;
	}

}
