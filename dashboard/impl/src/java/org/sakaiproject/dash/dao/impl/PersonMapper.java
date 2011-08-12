package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Person;
import org.springframework.jdbc.core.RowMapper;

public class PersonMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Person person = new Person();
		person.setId(rs.getLong("id"));
		person.setSakaiId(rs.getString("sakai_id"));
		person.setUserId(rs.getString("user_id"));
		return person;
	}
	
	

}
