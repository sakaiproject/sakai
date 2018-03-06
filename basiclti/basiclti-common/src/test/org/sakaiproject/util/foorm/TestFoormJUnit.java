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

import org.junit.Test;

import java.lang.Class;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

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
 * fully unit test the basic lti code.
 *
 */
@Slf4j
public class TestFoormJUnit {
    static Connection conn = null;

     static String [] test_form = { 
     	"id:key",
     	"title:text:maxlength=80",
     	"preferheight:integer:label=bl_preferheight:maxlength=80",
     	"sendname:radio:label=bl_sendname:choices=off,on,content",
     	"acceptgrades:radio:label=bl_acceptgrades:choices=off,on", 
     	"homepage:url:maxlength=100",
     	"webpage:url:maxlength=100",
     	"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
     	"created_at:autodate",
     	"updated_at:autodate"  
     };

     // Add two fields
     static String [] test_form_2 = { 
     	"id:text:maxlength=80", // Trigger severe error
     	"title:text:maxlength=80",
     	"stuff:text:maxlength=80",
     	"preferheight:integer:label=bl_preferheight:maxlength=80",
     	"sendname:radio:label=bl_sendname:choices=off,on,content",
     	"acceptgrades:radio:label=bl_acceptgrades:choices=off,on", 
     	"webpage:url:maxlength=256",
     	"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
     	"created_at:autodate",
     	"updated_at:autodate"  
     };

	
    // For integer field test
    static String [] test_integer = { 
	"preferheight:integer:label=bl_preferheight:maxlength=80"
    };

	
    @Test
	public void testBasicFormExtraction() {

	Foorm foorm = new Foorm();
	Properties pro = new Properties(); 

	pro.setProperty("title","blah");
	//pro.setProperty("acceptgrades","blah"); 
	pro.setProperty("acceptgrades","1"); 
	pro.setProperty("preferheight","1"); 
	pro.setProperty("homepage","http://www.cnn.com/");
	pro.setProperty("webpage","http://www.cnn.com/");

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	String foormExtract = foorm.formExtract(pro, test_form, null, false, rm, null);
	assertNull("got result from foormExtract",foormExtract);
	assertEquals("foormExtract has no errors",null,foormExtract);

    }

	@Test
	public void testSearchCheckSql() {
		Foorm foorm = new Foorm();
		// SAK-32704 - Want to review all this can perhaps adjust how the decision is made
		// about which search approach is in use
		String whereStyle = "COLUMN = 'value'";
		assertEquals(whereStyle, foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = "COLUMN='value'";
		assertEquals(whereStyle, foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = "COLUMN='value' OR OTHER=42";
		assertEquals(whereStyle,foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = "COLUMN='value' OR ( OTHER=42 and zap=21)";
		assertEquals(whereStyle, foorm.searchCheck(whereStyle, "table", new String[]{}));

		// Workaround for the Null ones below - not pretty but works
		whereStyle = "1=1 and (table.COLUMN LIKE '%zap%' OR ( othertable.OTHER=42 and zap=21))";
		assertEquals(whereStyle, foorm.searchCheck(whereStyle, "table", new String[]{}));

		// At some point we might want these to pass
		whereStyle = "(1=1) and (table.COLUMN LIKE '%zap%' OR ( othertable.OTHER=42 and zap=21))";
		assertNull(foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = "table.COLUMN LIKE '%zap%' OR ( othertable.OTHER=42 and zap=21)";
		assertNull(foorm.searchCheck(whereStyle, "table", new String[]{}));

		// A parenthesis first should be OK
		whereStyle = "(COLUMN='value') OR ( OTHER=42 and zap=21)";
		assertNull(foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = "( COLUMN = 'value' ) OR ( OTHER=42 and zap=21)";
		assertNull(foorm.searchCheck(whereStyle, "table", new String[]{}));
		whereStyle = " ( COLUMN = 'value' ) OR ( OTHER=42 and zap=21)";
		assertNull(foorm.searchCheck(whereStyle, "table", new String[]{}));
	}

	@Test
	public void testSearchCheckSingle() {
		Foorm foorm = new Foorm();
		assertEquals("table.FIELD:VALUE", foorm.searchCheck("FIELD:VALUE", "table", new String[]{"FIELD"}));
	}

    /* These tests focus on integer parsing */

    @Test
	public void testIntegerFieldBigDecimal() {
	Foorm foorm = new Foorm();
	Map<String,Object> parms = new HashMap<String,Object>();
	parms.put("preferheight", BigDecimal.valueOf(3));

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	String foormExtract = foorm.formExtract(parms, test_integer, null, false, rm, null);
	assertEquals("result from foormExtract should be null",null,foormExtract);
	assertNull("got result from foormExtract",foormExtract);

    }
    @Test
	public void testIntegerFieldBigDecimalViaNumber() {
	Foorm foorm = new Foorm();
	Map<String,Object> parms = new HashMap<String,Object>();
	parms.put("preferheight", BigDecimal.valueOf(3));

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	String foormExtract = foorm.formExtract(parms, test_integer, null, false, rm, null);
	assertEquals("result from foormExtract should be null",null,foormExtract);
	assertNull("got result from foormExtract",foormExtract);

    }


   @Test
	public void testIntegerFieldIntegerViaNumber() {
	Foorm foorm = new Foorm();
	Map<String,Object> parms = new HashMap<String,Object>();
	parms.put("preferheight", Integer.valueOf(3));

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	String foormExtract = foorm.formExtract(parms, test_integer, null, false, rm, null);
	assertEquals("result from foormExtract should be null",null,foormExtract);
	assertNull("got result from foormExtract",foormExtract);

    }
    @Test
	public void testIntegerFieldInteger() {
	Foorm foorm = new Foorm();
	Map<String,Object> parms = new HashMap<String,Object>();
	parms.put("preferheight", Integer.valueOf(3));

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	String foormExtract = foorm.formExtract(parms, test_integer, null, false, rm, null);
	assertEquals("result from foormExtract should be null",null,foormExtract);
	assertNull("got result from foormExtract",foormExtract);

    }

    @Test
	public void testIntegerFieldWord() {
	Foorm foorm = new Foorm();
	Map<String,Object> parms = new HashMap<String,Object>();
	parms.put("preferheight", "RUFF");

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	// An error is expected from this configuration.
	String foormExtract = foorm.formExtract(parms, test_integer, null, false, rm, null);
	assertNotNull("got result from foormExtract",foormExtract);

    }

    /************* database tests **********************/
    /* 
     *  These database tests are currently commented out as:

     * - they require having the db drivers available in the pom and having the 
     * user / password in the java file.
     *
     * - The tests print a lot but do very little junit assertion testing.
     * It should be the opposite.
     */

    // Note that the hsql test will currently produce an error.

    /*
     @Test
     public void testCreateHsqlSchema() {
     
         createAndTestVendorSchema(getHSqlDatabase(),"hsqldb");
     }
     */


    /* 
    @Test
    	public void testCreateOracleSchema() {
    	createAndTestVendorSchema(getOracleDatabase(),"oracle");
    }
    */


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
	
    public Connection getOracleDatabase() {
	try {

	    Class.forName("oracle.jdbc.driver.OracleDriver");

	    String jdbcUrl;
	    String user;
	    String pw;

	    jdbcUrl = "";
	    user = "";
	    pw = "";

	    conn = DriverManager.getConnection(jdbcUrl,user,pw);
	    log.debug("Got Connection={}", conn);
	    return conn;
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    assert false;
	}
	return null;
    }
	
    public Connection getHSqlDatabase() {
	try {
	    Class.forName("org.hsqldb.jdbcDriver");
	    conn = DriverManager.getConnection("jdbc:hsqldb:file:testdb", "sa", "");
	    log.debug("Got Connection={}", conn);
	    return conn;
	} catch (Exception e) {
	    log.error("Unable to connect to hsql database error: {}, {}", e.getMessage(), e);
	    assert false;
	}
	return null;
    }




    public void createAndTestVendorSchema(Connection vendorConnection, String vendorName) {
	    
	boolean doReset = true;
	Foorm foorm = new Foorm();
	String[] sqls = foorm.formSqlTable("lti_content", test_form,vendorName, doReset);
	//	conn = getHSqlDatabase();
	conn = vendorConnection;

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
	    query(conn,"SELECT * FROM lti_content");
	} catch (SQLException ex3) {
	    log.error(ex3.getMessage(), ex3);
	    fail("FAILED: time1: SELECT * FROM lti_content"+ex3);
	}

	log.debug("Second time...");
	try {
	    doReset = false;
	    Statement st = conn.createStatement(); 
	    ResultSet rs =  st.executeQuery("SELECT * FROM lti_content");
	    ResultSetMetaData md   = rs.getMetaData();
	    sqls = foorm.formAdjustTable("lti_content", test_form_2, vendorName, md);
	    for (String sql : sqls) {
		log.debug("time2: SQL={}", sql);
		try {
		    update(conn, sql);
		} catch (SQLException e) {
		    log.error(e.getMessage(), e);
		    fail("FAILED: time2: sql loop: [sql]"+e);
		}
	    }
	    query(conn,"SELECT * FROM lti_content");
	} catch (SQLException ex3) {
	    log.error(ex3.getMessage(), ex3);
	    fail("FAILED: time2: SELECT * FROM lti_content"+ex3);
	}


    }

}
