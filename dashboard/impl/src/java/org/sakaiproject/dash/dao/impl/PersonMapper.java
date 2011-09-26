package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Person;
import org.springframework.jdbc.core.RowMapper;

public class PersonMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Person person = new Person();
		person.setId(rs.getLong("p_id"));
		person.setSakaiId(rs.getString("p_sakai_id"));
		person.setUserId(rs.getString("p_user_id"));
		return person;
	}
	
	

}
