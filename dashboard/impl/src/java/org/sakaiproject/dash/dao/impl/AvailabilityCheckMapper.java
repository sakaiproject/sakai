/**
 * 
 */
package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.AvailabilityCheck;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 *
 */
public class AvailabilityCheckMapper implements RowMapper {

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		AvailabilityCheck availabilityCheck = new AvailabilityCheck();
		
		availabilityCheck.setId(rs.getLong("id"));
		availabilityCheck.setEntityReference(rs.getString("entity_ref"));
		availabilityCheck.setEntityTypeId(rs.getString("entity_type_id"));
		availabilityCheck.setScheduledTime(rs.getDate("scheduled_time"));
		
		return availabilityCheck;
	}

}
