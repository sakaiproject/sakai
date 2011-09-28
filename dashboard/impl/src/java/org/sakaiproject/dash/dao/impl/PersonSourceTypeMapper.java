package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.ItemType;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonSourceType;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

public class PersonSourceTypeMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ItemType itemType = ItemType.fromInteger(rs.getInt("pc_itemtype"));
		Person person = (Person) new PersonMapper().mapRow(rs, rowNum);
		SourceType sourceType = (SourceType) new SourceTypeMapper().mapRow(rs, rowNum);
		PersonSourceType personSourceType = new PersonSourceType(itemType, person, sourceType);
		return personSourceType;
	}
	
}
