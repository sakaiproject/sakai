package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.Realm;
import org.springframework.jdbc.core.RowMapper;

public class RealmMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Realm realm = new Realm();
		realm.setId(rs.getLong("r_id"));
		realm.setRealmId(rs.getString("r_realm_id"));
		return realm;
	}
	
	

}
