/**
 * Copyright (c) 2009-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
