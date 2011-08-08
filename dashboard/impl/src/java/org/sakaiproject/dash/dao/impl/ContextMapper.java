package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Context;
import org.springframework.jdbc.core.RowMapper;

public class ContextMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Context context = new Context();
		context.setId(rs.getLong("site_id"));
		context.setContextId(rs.getString("site_context_id"));
		context.setContextTitle(rs.getString("site_context_title"));
		context.setContextUrl(rs.getString("site_context_url"));
		return context;
	}
	
	

}
