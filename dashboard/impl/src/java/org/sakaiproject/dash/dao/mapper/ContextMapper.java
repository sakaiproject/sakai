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
import org.springframework.jdbc.core.RowMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Context context = new Context();
		try {
			context.setId(rs.getLong("site_id"));
			context.setContextId(rs.getString("site_context_id"));
			context.setContextTitle(rs.getString("site_context_title"));
			context.setContextUrl(rs.getString("site_context_url"));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if(e instanceof SQLException) {
				throw (SQLException) e;
			}
		}
		return context;
	}
	
	

}
