/**
 * Copyright (c) 2009-2017 The Apereo Foundation
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
package org.sakaiproject.util.foorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.db.api.SqlService;

import org.slf4j.Logger;

public class SakaiFoorm extends Foorm {

	// Abstract to be overridden
	@Override
		public String htmlSpecialChars(String str)
		{
			return FormattedText.escapeHtml(str, false);
		}

	// Abstract this away for testing purposes - return null if non existant
	@Override
		public String loadI18N(String str, Object loader)
		{
			if ( loader == null ) return null;
			if ( loader instanceof ResourceLoader) { 
				String retval = ((ResourceLoader) loader).getString(str,null);
				return retval;
				// return ((ResourceLoader) loader).getString(str,null);
			}
			return super.loadI18N(str, loader);
		}

	public void autoDDL(String table, String[] model, SqlService m_sql, boolean m_autoDdl, 
				boolean doReset, Logger M_log)
	{
		// Use very carefully - for testing table creation
		if (doReset)
			M_log.error("DO NOT RUN IN PRODUCTION WITH doReset TRUE");

		String[] sqls = null;
		if ( doReset ) {
			sqls = formSqlTable(table,model, m_sql.getVendor(), doReset);
		} else { 
			String query = "SELECT * FROM " + table;
			query = getPagedSelect(query, 0, 1, m_sql.getVendor());
			Connection conn = null;
			Statement st = null;
			ResultSet rs = null;
			boolean failed = false;
			try {
				conn = m_sql.borrowConnection();
				st = conn.createStatement(); 
				rs =  st.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
				sqls = formAdjustTable(table,model, m_sql.getVendor(), md);
			} catch (SQLException e) {
				failed = true;
			} finally {
				try {
					if ( st != null ) st.close();
					if ( rs != null ) rs.close();
				} catch (SQLException sqlex) {
					M_log.error("Error attempt to close Statement or ResultSet", sqlex);
				}
				if ( conn != null ) m_sql.returnConnection(conn);
			}
			if ( failed ) { // table must not exist
				sqls = formSqlTable(table,model, m_sql.getVendor(), doReset);
			}

		}
		for (String sql : sqls) { 
			M_log.debug(sql);  
			if ( m_autoDdl ) {
				if (m_sql.dbWriteFailQuiet(null, sql, null)) {
					// Schema modifications are more interesting
					if ( sql.trim().toLowerCase().startsWith("alter") ) {
						M_log.info("SQL Success:\n"+sql);
					} else {
						M_log.debug("SQL Success:\n"+sql);
					}
				} else {
					M_log.error("SQL Failure:\n"+sql);
				}
			} else {
				M_log.error("SQL Needed:\n"+sql);
			}
		}
	}

}
