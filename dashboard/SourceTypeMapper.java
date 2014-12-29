/**
 * 
 */
package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author jimeng
 *
 */
public class SourceTypeMapper implements RowMapper {

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		SourceType sourceType = new SourceType();
		sourceType.setId(rs.getLong("type_id"));
		sourceType.setName(rs.getString("type_name"));
		sourceType.setAccessPermission(rs.getString("type_access_permission"));
		return sourceType;
	}

}
