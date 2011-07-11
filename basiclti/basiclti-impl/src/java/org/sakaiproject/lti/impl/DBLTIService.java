/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.lti.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * DBLTIService extends the BaseLTIService.
 * </p>
 */
public class DBLTIService extends BaseLTIService implements LTIService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DBLTIService.class);

	/** Dependency: SqlService */
	protected SqlService m_sql = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sql = service;
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = Boolean.valueOf(value);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
                if ( m_sql == null ) m_sql = (SqlService) ComponentManager.get("org.sakaiproject.db.api.SqlService");
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sql.ddl(this.getClass().getClassLoader(), "sakai_lti");
			}

			super.init();
		}
		catch (Exception t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/** insertMapping */
	public Object insertMapping(Properties newProps)
	{
		// TODO: Only admins can do this
		HashMap<String, Object> newMapping = new HashMap<String,Object> ();

		// CHeck for user data errors
		String errors = foorm.formExtract(newProps, LTIService.ADMIN_MAPPING_MODEL, rb, newMapping);
                if ( errors != null ) return errors;

		// Run the SQL
		String sql = "INSERT INTO lti_mapping "+foorm.insertForm(newMapping);
System.out.println("sql="+sql);
		Object [] fields = foorm.getObjects(newMapping);
		Long retval = m_sql.dbInsert(null, sql, fields, "id");
		System.out.println("Insert="+retval);
		return retval;
	}
	
	public Object getMapping(int key) {return "oops"; }
	public String deleteMapping(int key) { return null; }
	public String updateMapping(int key, Properties newProps) { return null; }
	public ArrayList<Properties> getMappings(String search, String order, int first, int last) { return null; }

	/** insertTool */
	public Object insertTool(Properties newProps)
	{
		// TODO: Only admins can do this
		HashMap<String, Object> newMapping = new HashMap<String,Object> ();
		String errors = foorm.formExtract(newProps, LTIService.ADMIN_TOOL_MODEL, rb, newMapping);
                if ( errors != null ) return errors;
		String sql = "INSERT INTO lti_tools "+foorm.insertForm(newMapping);
System.out.println("sql="+sql);
		Object [] fields = foorm.getObjects(newMapping);
		Long retval = m_sql.dbInsert(null, sql, fields, "id");
		System.out.println("Insert="+retval);
		return retval;
	}
	
	public Object getTool(int key) {return "oops"; }	
	public String deleteTool(int key) { return null; }
	public String updateTool(int key, Properties newProps) { return null; }
	public ArrayList<Properties> getTools(String search, String order, int first, int last) { return null; }
	

}
