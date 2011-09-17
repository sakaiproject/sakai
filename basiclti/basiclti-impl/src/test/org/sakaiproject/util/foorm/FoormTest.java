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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.foorm;

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

/*
	public static void main(String[] args) {
		System.out.println("YO");
		try {
	        	Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:file:testdb", "sa", "");
		} catch (Exception e) {
			System.out.println("Unable to connect to hsql database");
		}
	}
*/
	public void testBasics() {
		System.out.println("Hello, World");
		Foorm foorm = new Foorm();
		System.out.println(foorm.parseFormString("title:text:required=true:size=25"));
		System.out.println(foorm.parseFormString("description:textarea:required=true:rows=2:cols=25"));
		System.out.println(foorm.parseFormString("sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));
		
		HashMap row = new HashMap(); row.put("title", "Fred"); row.put("description","Desc");
		row.put("sendemail", new Integer(1)); 
		row.put("acceptgrades", new Integer(1));
		row.put("preferheight", new Integer(100));
		
		System.out.println(foorm.getField(row,"title"));
		
		System.out.println(foorm.formInput(row,"title:text:required=true:size=25"));
		System.out.println(foorm.formInput(row,"description:textarea:required=true:rows=2:cols=25"));
		System.out.println(foorm.formInput(row,"sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));
		
		String [] test_form = { 
			"title:text:size=80",
			"preferheight:integer:label=bl_preferheight:size=80",
			"sendname:radio:label=bl_sendname:choices=off,on,content",
			"acceptgrades:radio:label=bl_acceptgrades:choices=off,on", 
			"homepage:url:size=100",
			"webpage:url:size=100",
			"customparameters:textarea:required=true:label=bl_customparameters:rows=5:cols=25" 
		};
		
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
}
