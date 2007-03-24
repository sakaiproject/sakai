/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.SiteSpecificResourceType;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;

public class DbResourceTypeRegistry extends ResourceTypeRegistryImpl 
{
	/** Our logger. */
	protected static Log M_log = LogFactory.getLog(DbResourceTypeRegistry.class);

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;
	
	/** Table amd field names**/
	
	protected static String m_resourceTableName = "CONTENT_TYPE_REGISTRY";
	protected static String m_contextIDField    = "CONTEXT_ID";
	protected static String m_resourceIDField   = "RESOURCE_TYPE_ID";
	protected static String m_enabledField      = "ENABLED";
	
	/** SQL to get enabled resource ids **/
	protected static String GET_ENABLED_RESOURCES = "select " + m_resourceIDField + 
	                                                " from " + m_resourceTableName + 
	                                                " where " + m_contextIDField + "= ? " + 
	                                                " and " + m_enabledField + "=?";
	
	
    /** SQL to get full map of resources **/
	
	protected static String FIELDLIST = m_contextIDField + ", " + m_resourceIDField + ", " + m_enabledField;
	protected static String GET_RESOURCEID_MAP = "select " + FIELDLIST + " from " + m_resourceTableName + 
				                                 " where " + m_contextIDField + "=?";
	/** SQL to delete and insert triples **/
	
	protected static String DELETE_CURRENT_MAP = "delete from " + m_resourceTableName + 
	                                             " where " + m_contextIDField + "=?";
	protected static String INSERT_RESOURCEID_MAP = "insert into " + m_resourceTableName +
	                                                " (" + FIELDLIST + ") values (?, ?, ?) ";
	
	                                                
	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;
	//protected SqlReader m_sqlReader = null;
	
	/**
	 * @param service
	 */
	public void setSqlService(SqlService service)
	{
		this.m_sqlService = service;
	}

	
	/**
	 * Configuration: to run the ddl on init or not.
	 *
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
	}
	
	/* protected -- delete everything in the db associated with this context
	 * 
	 * @param Connection connection
	 * 	      the sqlservice connection
	 * @param String contextID
	 *        the contextID
	 */
	
	protected void deleteMapofResourceTypesForContext(Connection connection, String contextID)
	{
		Object fields[] = new Object[1];
		fields[0] = contextID;

		boolean ok = m_sqlService.dbWrite(connection, DELETE_CURRENT_MAP, fields);
	}
	
	/* insert enabled status for resource ids in the given map for the provided contextid
	 * 
	 * @param Connection connection
	 *        the sqlservice connection
	 * 
	 * @param String contextID
	 * 
	 * @param Map<String resourceID, Boolean isOn> enabled
	 *        whether or not each resourceID is enabled in this context
	 */
	
	protected void insertMapofResourceTypesforContext(Connection connection, String contextID, Map<String, Boolean> enabled)
	{
		Iterator<String> iter = enabled.keySet().iterator();
		while (iter.hasNext()) 
		{
			String resourceID = iter.next();

			Object fields[] = new Object[3];
			fields[0]= contextID;
			fields[1] = resourceID;
			fields[2]= enabled.get(resourceID) ? "e" : "d";
			
			m_sqlService.dbWrite(connection, INSERT_RESOURCEID_MAP, fields);
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#setResourceTypesForContext(java.lang.String, java.util.Map)
	 */
	public void setMapOfResourceTypesForContext(String context, Map<String, Boolean> enabled) 
	{
		//super.setMapOfResourceTypesForContext(context, enabled);
		//Replace in teh db
		
		Connection conn = null;
		Boolean wasCommit;

		try
		{
			conn = m_sqlService.borrowConnection();
			wasCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			this.deleteMapofResourceTypesForContext(conn, context);
			this.insertMapofResourceTypesforContext(conn, context, enabled);

			conn.commit();
			conn.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(conn);
		}
		catch (SQLException e)
		{
			M_log.warn("setMapOfResourceTypesForContext(" + context + ") " + e);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getResourceTypesForContext(java.lang.String)
	 */
	public Map<String, Boolean> getMapOfResourceTypesForContext(String context) 
	{
		Map<String, Boolean> enabled = new HashMap<String, Boolean>();
		
		try 
		{
			Object fields[] = new Object[1];
			fields[0] = context;
			
			Connection conn = m_sqlService.borrowConnection();
			PreparedStatement psth = conn.prepareStatement(GET_RESOURCEID_MAP);
			psth.setString(1, context);
			psth.execute();
			ResultSet results = psth.getResultSet();
			
			while (results.next()) 
			{
				enabled.put(results.getString(2), new Boolean(results.getString(3).equals("e")));
			}		
			
			m_sqlService.returnConnection(conn);
			
		}
		catch(SQLException sqlException) 
		{
			M_log.warn("getMapOfResourceTypesForContext(" + context + ") " + sqlException);
		}

		if(enabled.isEmpty())
		{
			for(ResourceType type : this.typeIndex.values())
			{
				if(type instanceof SiteSpecificResourceType)
				{
					enabled.put(type.getId(), new Boolean(((SiteSpecificResourceType) type).isEnabledByDefault()));
				}
			}
		}

		return enabled;
	} 


	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content_registry");
				super.init();
			}

		}
		catch (Throwable t)
		{
		}
	}

}
