/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.SiteSpecificResourceType;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

@Slf4j
public class DbResourceTypeRegistry extends ResourceTypeRegistryImpl 
{
	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;
	
	/** Table amd field names**/
	
	protected static final String m_resourceTableName = "CONTENT_TYPE_REGISTRY";
	protected static final String m_contextIDField    = "CONTEXT_ID";
	protected static final String m_resourceIDField   = "RESOURCE_TYPE_ID";
	protected static final String m_enabledField      = "ENABLED";
	
	/** SQL to get enabled resource ids **/
	protected static String GET_ENABLED_RESOURCES = "select " + m_resourceIDField + 
	                                                " from " + m_resourceTableName + 
	                                                " where " + m_contextIDField + "= ? " + 
	                                                " and " + m_enabledField + "=?";
	
	
    /** SQL to get full map of resources **/
	
	protected static final String FIELDLIST = m_contextIDField + ", " + m_resourceIDField + ", " + m_enabledField;
	protected static final String GET_RESOURCEID_MAP = "select " + FIELDLIST + " from " + m_resourceTableName + 
				                                 " where " + m_contextIDField + "=?";
	/** SQL to delete and insert triples **/
	
	protected static final String DELETE_CURRENT_MAP = "delete from " + m_resourceTableName + 
	                                             " where " + m_contextIDField + "=?";
	protected static final String INSERT_RESOURCEID_MAP = "insert into " + m_resourceTableName +
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

	
	private ThreadLocalManager threadLocalManager;
	
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * Configuration: to run the ddl on init or not.
	 *
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = Boolean.valueOf(value).booleanValue();
	}
	
	/* protected -- delete everything in the db associated with this context
	 * 
	 * @param Connection connection
	 * 	      the sqlservice connection
	 * @param String contextID
	 *        the contextID
	 */
	
	protected void deleteMapofResourceTypesForContext(String contextID)
	{
		Object fields[] = new Object[1];
		fields[0] = contextID;

		m_sqlService.dbWrite(DELETE_CURRENT_MAP, fields);
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
	
	protected void insertMapofResourceTypesforContext(String contextID, Map<String, Boolean> enabled)
	{
		for (Map.Entry<String, Boolean> entry : enabled.entrySet())
		{
			String resourceID = entry.getKey();

			Object fields[] = new Object[3];
			fields[0]= contextID;
			fields[1] = resourceID;
			fields[2]= (entry.getValue().booleanValue() ? "e" : "d");
			
			m_sqlService.dbWrite(INSERT_RESOURCEID_MAP, fields);
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#setResourceTypesForContext(java.lang.String, java.util.Map)
	 */
	public void setMapOfResourceTypesForContext(final String context, final Map<String, Boolean> enabled) 
	{
		//super.setMapOfResourceTypesForContext(context, enabled);
		//Replace in teh db
		

		m_sqlService.transact(new Runnable()
		{
			public void run()
			{
				saveMap(context, enabled);					
			}
		}, "DbResourceTypeRegistry.setMapOfResourceTypesForContext: " + context);
		
		threadLocalManager.set("getMapOfResourceTypesForContext@" + context, new HashMap<String, Boolean>(enabled));
	}
	
	protected void saveMap(String context, Map<String, Boolean> enabled) 
	{
		this.deleteMapofResourceTypesForContext(context);
		this.insertMapofResourceTypesforContext(context, enabled);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getResourceTypesForContext(java.lang.String)
	 */
	public Map<String, Boolean> getMapOfResourceTypesForContext(String context) 
	{
		Map<String, Boolean> enabled = (Map<String, Boolean>) threadLocalManager.get("getMapOfResourceTypesForContext@" + context);
			
		if(enabled == null)
		{
			enabled = new HashMap<String, Boolean>();
			Object fields[] = new Object[1];
			fields[0] = context;
			
			List results = m_sqlService.dbRead(GET_RESOURCEID_MAP, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						return new Entry(result.getString(2), "e".equals(result.getString(3)));
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
				
			for(Object result : results)
			{
				if(result instanceof Entry)
				{
					Entry entry = (Entry) result;
					
					enabled.put(entry.getTypeId(), Boolean.valueOf(entry.isEnabled()));
				}
			}
			
			if(enabled.isEmpty())
			{
				for(ResourceType type : this.typeIndex.values())
				{
					if(type instanceof SiteSpecificResourceType)
					{
						enabled.put(type.getId(), Boolean.valueOf(((SiteSpecificResourceType) type).isEnabledByDefault()));
					}
				}
			}
			
			threadLocalManager.set("getMapOfResourceTypesForContext@" + context, enabled);
		}

		return new HashMap<String, Boolean>(enabled);
	} 


	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			log.info("init()");
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content_registry");
				super.init();
			}

		}
		catch (Exception t)
		{
		}
	}
	
	public class Entry
	{
		protected String typeId;
		protected boolean enabled;
		public Entry(String typeId, boolean enabled)
		{
			this.typeId = typeId;
			this.enabled = enabled;
		}
		public boolean isEnabled() 
		{
			return enabled;
		}
		public void setEnabled(boolean enabled) 
		{
			this.enabled = enabled;
		}
		public String getTypeId() 
		{
			return typeId;
		}
		public void setTypeId(String typeId) 
		{
			this.typeId = typeId;
		}
	}

}
