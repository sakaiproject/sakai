/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.foorm;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.lang.Class;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Arrays;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lti.api.LTIService;

/*
 * This is originally based on the FoormTest.java file but modified to use JUnit
 * code and conventions. It is targeted for a change to address the issue tested
 * testIntegerField.  Most code that did not address the parsing issue has been deleted.
 *
 * Sample code for testing hsql and oracle databases has been kept.  It is not yet
 * suitable for actual junit testing since it creates direct output rather than
 * allowing testing assertions but it seems a shame to have to recreate the functionality
 * later from scratch.
 *
 * There will need to be some code refactoring to make it possible to
 * fully unit test the LTI code.
 *
 */
@Slf4j
public class FoormSakaiTest {
	String[] vendors = {"mysql", "oracle", "hsqldb"};

	/* This is a weird unit test.  It's goal is to generate the DDL and optionall dump it. 
	 * when a Shell Environment variable is set.
	 *
	 * export SAKAI_FOORM_DDL_DUMP=yes
	 * mvn test
	 *
	 */
	@Test
	public void testCreateDDL() {
		boolean doDump = "yes".equals(System.getenv("SAKAI_FOORM_DDL_DUMP"));
		String [] sqls;
		Foorm foorm = new Foorm();
		for ( String vendorName: vendors) {
			if ( doDump ) System.out.println("\n=========== "+vendorName);
			sqls = foorm.formSqlTable("lti_content", LTIService.CONTENT_MODEL, vendorName, false);
			assertEquals(sqls.length, "oracle".equals(vendorName) ? 2 : 1);
			if ( doDump ) {
				System.out.println(sqls[0]);
				if ( "oracle".equals(vendorName) ) System.out.println(sqls[1]);
			}

			sqls = foorm.formSqlTable("lti_tool", LTIService.TOOL_MODEL, vendorName, false);
			assertEquals(sqls.length, "oracle".equals(vendorName) ? 2 : 1);
			if ( doDump ) {
				System.out.println(sqls[0]);
				if ( "oracle".equals(vendorName) ) System.out.println(sqls[1]);
			}

			sqls = foorm.formSqlTable("lti_tool_site", LTIService.TOOL_SITE_MODEL, vendorName, false);
			assertEquals(sqls.length, "oracle".equals(vendorName) ? 2 : 1);
			if ( doDump ) {
				System.out.println(sqls[0]);
				if ( "oracle".equals(vendorName) ) System.out.println(sqls[1]);
			}

			sqls = foorm.formSqlTable("lti_membership_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL, vendorName, false);
			// No primary key
			assertEquals(sqls.length, "oracle".equals(vendorName) ? 1 : 1);
			if ( doDump ) {
				System.out.println(sqls[0]);
			}
		}
	}

	/************* database tests **********************/

	@Test
	public void testCreateHsqlSchema() {
		createAndTestVendorSchema(getHSqlDatabase(),"hsqldb", "lti_content", LTIService.CONTENT_MODEL);
		createAndTestVendorSchema(getHSqlDatabase(),"hsqldb", "lti_tool", LTIService.TOOL_MODEL);
		createAndTestVendorSchema(getHSqlDatabase(),"hsqldb", "lti_tool_site", LTIService.TOOL_SITE_MODEL);
		createAndTestVendorSchema(getHSqlDatabase(),"hsqldb", "lti_membership_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL);
	}

	// Code inspiration from http://hsqldb.org/doc/guide/apb.html
	public synchronized void query(Connection conn, String expression) throws SQLException {
		Statement st = null;
		ResultSet rs = null;

		st = conn.createStatement();         // statement objects can be reused with
		rs = st.executeQuery(expression);    // run the query

		// do something with the result set.
		dump(rs);
		dumpMeta(rs);
		st.close();
	}

	//use for SQL commands CREATE, DROP, INSERT and UPDATE
	public synchronized void update(Connection conn, String expression) throws SQLException {
		Statement st = null;
		st = conn.createStatement();    // statements
		int i = st.executeUpdate(expression);    // run the query
		if (i == -1) {
			log.debug("db error : {}", expression);
		}
		st.close();
	}

	public static void dump(ResultSet rs) throws SQLException {
		ResultSetMetaData meta   = rs.getMetaData();
		int               colmax = meta.getColumnCount();
		int               i;
		Object            o = null;
		for (; rs.next(); ) {
			for (i = 0; i < colmax; ++i) {
				o = rs.getObject(i + 1);
				log.debug("{}", o.toString());
			}
		}
	}

	public static void dumpMeta(ResultSet rs) throws SQLException {
		ResultSetMetaData md   = rs.getMetaData();
		// Print the column labels
		for( int i = 1; i <= md.getColumnCount(); i++ ) {
			log.debug("{} ({}) auto={}", md.getColumnLabel(i), md.getColumnDisplaySize(i), md.isAutoIncrement(i));
			log.debug("type={}", md.getColumnClassName(i));
		}

		// Loop through the result set
		while( rs.next() ) {
			for( int i = 1; i <= md.getColumnCount(); i++ ) log.debug("{} ", rs.getString(i)) ;
		}
	}

	public Connection getHSqlDatabase() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "sa", "");
			log.debug("Got Connection={}", conn);
			return conn;
		} catch (Exception e) {
			log.error("Unable to connect to hsql database error: {}, {}", e.getMessage(), e);
			assert false;
		}
		return null;
	}

	public void createAndTestVendorSchema(Connection conn, String vendorName, String tableName, String[] model) {

		boolean doReset = true;
		Foorm foorm = new Foorm();
		String[] sqls = foorm.formSqlTable(tableName, model,vendorName, doReset);

		log.debug("First time ...");
		for (String sql : sqls) {
			log.debug("time1: SQL={}", sql);
			try {
				update(conn, sql);
			} catch (SQLException e) {
				log.error("create Schema issue: {}", e.getMessage());
				fail("FAILED: time1: [sql]"+e);
			}
		}

		try {
			query(conn,"SELECT * FROM " + tableName);
		} catch (SQLException ex3) {
			log.error(ex3.getMessage(), ex3);
			fail("FAILED: time1: SELECT * FROM "+tableName+" "+ex3);
		}

		log.debug("Second time...");
		try {
			Statement st = conn.createStatement();
			ResultSet rs =  st.executeQuery("SELECT * FROM "+tableName);
			ResultSetMetaData md   = rs.getMetaData();
			sqls = foorm.formAdjustTable(tableName, model, vendorName, md);
			for (String sql : sqls) {
				fail("FAILED: formAdjustTable: "+tableName+" "+sql);
			}
			query(conn,"SELECT * FROM "+tableName);
		} catch (SQLException ex3) {
			log.error(ex3.getMessage(), ex3);
			fail("FAILED: formAdjustTable "+tableName+" "+ex3);
		}


	}

}
