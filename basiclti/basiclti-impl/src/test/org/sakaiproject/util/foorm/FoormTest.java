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

import java.lang.Class;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.sakaiproject.util.foorm.Foorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class FoormTest {
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

	public void testBasics() {
		System.out.println("Hello, World");
		Foorm foorm = new Foorm();
		System.out.println(foorm.parseFormString("title:text:required=true:maxlength=25"));
		System.out.println(foorm.parseFormString("description:textarea:required=true:rows=2:cols=25"));
		System.out.println(foorm.parseFormString("sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));

		HashMap row = new HashMap(); row.put("title", "Fred"); row.put("description","Desc");
		row.put("sendemail", new Integer(1)); 
		row.put("acceptgrades", new Integer(1));
		row.put("preferheight", new Integer(100));

		System.out.println(foorm.getField(row,"title"));

		System.out.println(foorm.formInput(row,"title:text:required=true:maxlength=25"));
		System.out.println(foorm.formInput(row,"description:textarea:required=true:rows=2:cols=25"));
		System.out.println(foorm.formInput(row,"sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));

		System.out.println(foorm.formInput(row, test_form));

		System.out.println(foorm.formOutput(row, test_form, null));

		Properties pro = new Properties(); 
		pro.setProperty("title","blah");
		pro.setProperty("acceptgrades","blah"); 
		pro.setProperty("preferheight","1"); 
		pro.setProperty("homepage","blah");
		pro.setProperty("webpage","http://www.cnn.com/");

		// Properties parms, String[] formDefinition, boolean forInsert, Object loader, SortedMap<String,String> errors
		System.out.println(foorm.formValidate(pro, test_form, true, null, null));

		HashMap<String, Object> rm = new HashMap<String,Object> ();

		// Object parms, String[] formDefinition, Object loader, boolean forInsert, Map<String, Object> dataMap, 
		//    SortedMap<String,String> errors
		System.out.println(foorm.formExtract(pro, test_form, null, false, rm, null));
		System.out.println("--- Result Map ---"); 
		System.out.println(rm);

		HashMap crow = new HashMap(); 
		crow.put("allowtitle", new Integer(0)); 
		// Should suppress 
		crow.put("allowpreferheight",new Integer(1)); 
		crow.put("allowwebpage",new Integer(0)); 
		// Should suppress 
		crow.put("sendname", new Integer(1)); 
		// Should suppress 
		crow.put("acceptgrades", new Integer(2)); 
		// crow.put("preferheight", new Integer(100)); (Leave alone - should be allowed)

		String [] ff = foorm.filterForm(crow, test_form); 
		System.out.println(Arrays.toString(ff));

		System.out.println("--- Required I18N Strings ---"); 
		ArrayList<String> strings = foorm.utilI18NStrings(test_form); 
		System.out.println(strings);
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
			System.out.println("db error : " + expression);
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
				System.out.print(o.toString() + " ");
			}

			System.out.println(" ");
		}
	}

	public static void dumpMeta(ResultSet rs) throws SQLException {
		ResultSetMetaData md   = rs.getMetaData();
		// Print the column labels
		for( int i = 1; i <= md.getColumnCount(); i++ ) {
			System.out.println( md.getColumnLabel(i) + " (" + md.getColumnDisplaySize(i) + ") auto=" + md.isAutoIncrement(i)) ;
			System.out.println(" type="+md.getColumnClassName(i));
		}
		System.out.println() ;

		// Loop through the result set
		while( rs.next() ) {
			for( int i = 1; i <= md.getColumnCount(); i++ ) System.out.print( rs.getString(i) + " " ) ;
			System.out.println() ;
		}
	}

	public Connection getHSqlDatabase() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:file:testdb", "sa", "");
			System.out.println("Got Connection="+conn);
			return conn;
		} catch (Exception e) {
			System.out.println("Unable to connect to hsql database");
			e.printStackTrace();
			assert false;
		}
		return null;
	}

	public void testDatabase() {
		conn = getHSqlDatabase();

		try {
			update(conn,"DROP TABLE sample_table");
			System.out.println("Dropped Existing table");
		} catch (SQLException ex2) {
			System.out.println("Creating fresh table");
		}

		try {
			update(conn,"CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)");
		} catch (SQLException ex2) {
			ex2.printStackTrace();
			assert false;
		}

		try {
			update(conn,"INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)");
			update(conn,"INSERT INTO sample_table(str_col,num_col) VALUES('Toyota', 200)");
			update(conn,"INSERT INTO sample_table(str_col,num_col) VALUES('Honda', 300)");
			update(conn,"INSERT INTO sample_table(str_col,num_col) VALUES('GM', 400)");

			// do a query
			query(conn,"SELECT * FROM sample_table WHERE num_col < 250");
		} catch (SQLException ex3) {
			ex3.printStackTrace();
			assert false;
		}

		try {
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
			conn.close();
		} catch (SQLException ex3) {
			ex3.printStackTrace();
		}

	}

	public void testCreateSchema() {
		boolean doReset = true;
		Foorm foorm = new Foorm();
		String[] sqls = foorm.formSqlTable("lti_content", test_form, "hsqldb", doReset);
		conn = getHSqlDatabase();

		for (String sql : sqls) {
			System.out.println("SQL="+sql);
			try {
				update(conn, sql);
			} catch (SQLException e) {
				// Ignore
			}
		}

		try {
			query(conn,"SELECT * FROM lti_content");
		} catch (SQLException ex3) {
			ex3.printStackTrace();
			assert false;
		}

		try {
			System.out.println("Second time...");
			doReset = false;
			Statement st = conn.createStatement(); 
			ResultSet rs =  st.executeQuery("SELECT * FROM lti_content");
			ResultSetMetaData md   = rs.getMetaData();
			sqls = foorm.formAdjustTable("lti_content", test_form_2, "hsqldb", md);
			for (String sql : sqls) {
				System.out.println("SQL="+sql);
				try {
					update(conn, sql);
				} catch (SQLException e) {
					e.printStackTrace();
					assert false;
				}
			}
			query(conn,"SELECT * FROM lti_content");
		} catch (SQLException ex3) {
			ex3.printStackTrace();
			assert false;
		}


	}

}
