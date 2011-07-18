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

	/** Mapping methods */
	
	public Object insertMapping(Properties newProps)
	{
		return insertThing("lti_mapping",LTIService.MAPPING_MODEL, newProps);
        }
	
	public Map<String,Object> getMapping(Long key) 
	{
	        return getThing("lti_mapping", LTIService.MAPPING_MODEL, key);                
	}

	public boolean deleteMapping(Long key)
	{
	        return deleteThing("lti_mapping", LTIService.MAPPING_MODEL, key);
	}
	
	public Object updateMapping(Long key, Object newProps) 
	{ 
	        return updateThing("lti_mapping", LTIService.MAPPING_MODEL, key, newProps);
	}

	public List<Map<String,Object>> getMappings(String search, String order, int first, int last) 
	{ 
	        return getThings("lti_mapping", LTIService.MAPPING_MODEL, search, order, first, last);
	}

	/** Tool Methods */
	public Object insertTool(Properties newProps)
	{
		return insertThing("lti_tools",LTIService.TOOL_MODEL, newProps);
        }
	
	public Map<String,Object> getTool(Long key) 
	{
	        return getThing("lti_tools", LTIService.TOOL_MODEL, key);                
	}
	
	public Map<String,Object> getTool(String url) {return null; }

	private boolean getTool(Object urlorkey, Map<String,Object> retval) 
	{
	        return false; 
	}

	public boolean deleteTool(Long key)
	{
	        return deleteThing("lti_tools", LTIService.TOOL_MODEL, key);
	}
	
	public Object updateTool(Long key, Object newProps) 
	{ 
	        return updateThing("lti_tools", LTIService.TOOL_MODEL, key, newProps);
	}

	public List<Map<String,Object>> getTools(String search, String order, int first, int last) 
	{ 
	        return getThings("lti_tools", LTIService.TOOL_MODEL, search, order, first, last);
	}
	
	// Returns String (falure) or Long (key on success)
	public Object insertThing(String table, String [] model, Properties newProps)
	{
		if ( ! isMaintain() ) return null; 
		String [] columns = foorm.getFields(model);
		
		HashMap<String, Object> newMapping = new HashMap<String,Object> ();
		
		if ( isMaintain() && ! isAdmin() && ( Arrays.asList(columns).indexOf("SITE_ID") >= 0 ) ) newProps.put("SITE_ID",getContext());
		
		String errors = foorm.formExtract(newProps, model, rb, newMapping);
                if ( errors != null ) return errors;
                
		String sql = "INSERT INTO "+table+foorm.insertForm(newMapping);
		Object [] fields = foorm.getObjects(newMapping);
		Long retval = m_sql.dbInsert(null, sql, fields, "id");
		System.out.println("Insert="+retval);
		return retval;
	}
	
	public Map<String,Object> getThing(String table, String [] model, Long key) 
	{
	        String statement = "SELECT "+foorm.formSelect(model)+" from "+table+" WHERE id = ?";
                Object fields[] = null;           
                String [] columns = foorm.getFields(model);

                if ( isAdmin () )
                {
                        fields = new Object[1];
                        fields[0] = key;               
                } else if ( Arrays.asList(columns).indexOf("SITE_ID") >= 0 ) {
                        statement += " AND WHERE SITE_ID = ?";
                        fields = new Object[2];
                        fields[0] = key;               
                        fields[1] = getContext();
                } else { 
                        return null;
                }
               
                List rv = getResultSet(statement,fields,columns);
                
                if ((rv != null) && (rv.size() > 0))
                {
                        return (Map<String,Object>) rv.get(0);
                }
                return null;
                
	}
	
	public List<Map<String,Object>> getThings(String table, String [] model,  
	        String search, String order, int first, int last) 
	{ 
                String statement = "SELECT "+foorm.formSelect(model)+" FROM " + table;
                String [] columns = foorm.getFields(model);
                
                Object fields[] = null;
                if ( ! isAdmin () )
                {
                        if  ( Arrays.asList(columns).indexOf("SITE_ID") >= 0 ) {
                                statement += " WHERE SITE_ID = ?";
                                fields = new Object[1];
                                fields[0] = getContext();
                        } else {
                                return null;
                        }
                }
                

                return getResultSet(statement,fields,columns);
	}

        public boolean deleteThing(String table, String []model, Long key)
	{
	        String statement = "DELETE FROM "+table+" WHERE id = ?";
                Object fields[] = null;
                String [] columns = foorm.getFields(model);

                if ( isAdmin () )
                {
                        fields = new Object[1];
                        fields[0] = key;               
                } else if ( isMaintain() && ( Arrays.asList(columns).indexOf("SITE_ID") >= 0 ) ) {
                        statement += " AND WHERE SITE_ID = ?";
                        fields = new Object[2];
                        fields[0] = key;               
                        fields[1] = getContext();
                } else {
                        return false;
                }
                return m_sql.dbWrite(statement, fields);
	}
	
	public Object updateThing(String table, String [] model, Long key, Object newProps)
	{
		if ( ! isMaintain() ) return null; 
		String [] columns = foorm.getFields(model);
		
		HashMap<String, Object> newMapping = new HashMap<String,Object> ();
				
		String errors = foorm.formExtract(newProps, model, rb, newMapping);
                if ( errors != null ) return errors;

                if ( isMaintain() && ! isAdmin() && ( Arrays.asList(columns).indexOf("SITE_ID") >= 0 ) ) 
                {
                        foorm.setField(newMapping, "SITE_ID",getContext());
                }

		String sql = "UPDATE "+table+" SET "+foorm.updateForm(newMapping);
		System.out.println("Upate="+sql);
		Object [] fields = foorm.getObjects(newMapping);
		boolean retval = m_sql.dbWrite(sql, fields);
		System.out.println("Update="+retval);
		if ( ! retval ) return "Update failed";
		return Boolean.TRUE;
	}

	
        // Utility to return a resultset
	public List<Map<String,Object>> getResultSet(String statement, Object [] fields, final String [] columns) 
	{
	      
	        System.out.println("statement="+statement);

                List rv = m_sql.dbRead(statement, fields, new SqlReader()
                {
                        public Object readSqlResultRecord(ResultSet result)
                        {
                                try
                                {
                                        Map<String,Object> rv = new HashMap<String,Object> ();                                    
                                        for (String field : columns) {
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
