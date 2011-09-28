package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.ItemType;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonContext;
import org.springframework.jdbc.core.RowMapper;

public class PersonContextMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ItemType itemType = ItemType.fromInteger(rs.getInt("pc_itemtype"));
		Person person = (Person) new PersonMapper().mapRow(rs, rowNum);
		Context context = (Context) new ContextMapper().mapRow(rs, rowNum);
		PersonContext personContext = new PersonContext(itemType, person, context);
		return personContext;
	}

}
