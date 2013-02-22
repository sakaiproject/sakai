package org.sakaiproject.util.foorm;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.db.api.SqlService;

import org.apache.commons.logging.Log;

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
				boolean doReset, Log M_log)
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
			boolean failed = false;
			try {
				conn = m_sql.borrowConnection();
				Statement st = conn.createStatement(); 
				ResultSet rs =  st.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
				sqls = formAdjustTable(table,model, m_sql.getVendor(), md);
			} catch (SQLException e) {
				failed = true;
			} finally {
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
