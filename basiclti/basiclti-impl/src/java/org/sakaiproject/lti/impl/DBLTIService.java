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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Properties;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlReader;
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
		String errors = foorm.formExtract(newProps, LTIService.MAPPING_MODEL, rb, newMapping);
                if ( errors != null ) return errors;

		// Run the SQL
		String sql = "INSERT INTO lti_mapping "+foorm.insertForm(newMapping);
System.out.println("sql="+sql);
		Object [] fields = foorm.getObjects(newMapping);
		Long retval = m_sql.dbInsert(null, sql, fields, "id");
		System.out.println("Insert="+retval);
		return retval;
	}
	
	public Object getMapping(Long key) {return "oops"; }
	public String deleteMapping(Long key) { return null; }
	public String updateMapping(Long key, Map<String,Object> retval) { return null; }
	public List<Map<String,Object>> getMappings(String search, String order, int first, int last) { return null; }

	/** insertTool */
	public Object insertTool(Properties newProps)
	{
		if ( ! isMaintain() ) return null; 
		HashMap<String, Object> newMapping = new HashMap<String,Object> ();
		if ( isMaintain() && ! isAdmin() ) newProps.put("SITE_ID",getContext());
		String errors = foorm.formExtract(newProps, LTIService.TOOL_MODEL, rb, newMapping);
                if ( errors != null ) return errors;
		String sql = "INSERT INTO lti_tools "+foorm.insertForm(newMapping);
System.out.println("sql="+sql);
		Object [] fields = foorm.getObjects(newMapping);
		Long retval = m_sql.dbInsert(null, sql, fields, "id");
		System.out.println("Insert="+retval);
		return retval;
	}
	
	public Map<String,Object> getTool(Long key) 
	{
	        String statement = "SELECT "+foorm.formSelect(LTIService.TOOL_MODEL)+" from lti_tools WHERE id = ?";
                Object fields[] = null;
                if ( isAdmin () )
                {
                        fields = new Object[1];
                        fields[0] = key;               
                } else {
                        statement += " AND WHERE SITE_ID = ?";
                        fields = new Object[2];
                        fields[0] = key;               
                        fields[1] = getContext();
                }
                
                System.out.println("statement="+statement);

                List rv = m_sql.dbRead(statement, fields, new SqlReader()
                {
                        public Object readSqlResultRecord(ResultSet result)
                        {
                                try
                                {
                                        Map<String,Object> rv = new HashMap<String,Object> ();                                    
                                        for (String field : TOOL_FIELDS) {
                                                rv.put(field,result.getObject(field));
                                        }
                                        return rv;
                                }
                                catch (SQLException e)
                                {
                                        M_log.warn("getTools" + e);
                                        return null;
                                }
                        }
                });
                
                if ((rv != null) && (rv.size() > 0))
                {
                        return (Map<String,Object>) rv.get(0);
                }
                return null;
                
	}
	
	public Map<String,Object> getTool(String url) {return null; }

	private boolean getTool(Object urlorkey, Map<String,Object> retval) 
	{
	        return false; 
	}

	public String deleteTool(Long key) { return null; }
	public String updateTool(Long key, Map<String,Object> newProps) { return null; }


	public List<Map<String,Object>> getTools(String search, String order, int first, int last) 
	{ 
                String statement = "SELECT "+foorm.formSelect(LTIService.TOOL_MODEL)+" from lti_tools";
                Object fields[] = null;
                if ( ! isAdmin () )
                {
                        statement += " WHERE SITE_ID = ?";
                        fields = new Object[1];
                        fields[0] = getContext();
                }
                
                System.out.println("statement="+statement);

                List rv = m_sql.dbRead(statement, fields, new SqlReader()
                {
                        public Object readSqlResultRecord(ResultSet result)
                        {
                                try
                                {
                                        Map<String,Object> rv = new HashMap<String,Object> ();                                    
                                        for (String field : TOOL_FIELDS) {
                                                rv.put(field,result.getObject(field));
                                        }
                                        return rv;
                                }
                                catch (SQLException e)
                                {
                                        M_log.warn("getTools" + e);
                                        return null;
                                }
                        }
                });

                return (List<Map<String,Object>>) rv;
	}

}
