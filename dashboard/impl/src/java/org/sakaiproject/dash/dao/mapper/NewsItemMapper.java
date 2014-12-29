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
