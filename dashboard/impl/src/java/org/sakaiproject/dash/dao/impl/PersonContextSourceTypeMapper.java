package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.ItemType;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonContextSourceType;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

public class PersonContextSourceTypeMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ItemType itemType = ItemType.fromInteger(rs.getInt("pc_itemtype"));
		Person person = (Person) new PersonMapper().mapRow(rs, rowNum);
		Context context = (Context) new ContextMapper().mapRow(rs, rowNum);
		SourceType sourceType = (SourceType) new SourceTypeMapper().mapRow(rs, rowNum);
		PersonContextSourceType personContextSourceType = new PersonContextSourceType(itemType, person, context, sourceType);
		return personContextSourceType;
	}

}
