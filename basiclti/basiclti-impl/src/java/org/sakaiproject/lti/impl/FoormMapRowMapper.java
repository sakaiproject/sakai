
package org.sakaiproject.lti.impl;

import java.util.Map;
import java.util.HashMap;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * Override some methods in the ColumnMapRowMapper so the columns end
 * up with the case we expect.
 */
public class FoormMapRowMapper extends ColumnMapRowMapper {

	String [] columns = null;

	public FoormMapRowMapper(String [] columns ) {
		this.columns = columns;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> createColumnMap(int columnCount) {
		return new HashMap<String,Object>(columnCount);
	}

	/**
	 * Determine the key to use for the given column in the column Map.
	 * @param columnName the column name as returned by the ResultSet
	 * @return the column key to use
	 * @see java.sql.ResultSetMetaData#getColumnName
	 */
	protected String getColumnKey(String columnName) {
		for (String s : columns ) {
			if ( s.equalsIgnoreCase(columnName) ) return s;
		}
		// Probably something ancilarry like RNUM
		return columnName;
	}
}
