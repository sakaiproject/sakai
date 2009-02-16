/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.rights.impl;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.RightsPolicy;
import org.sakaiproject.rights.api.RightsService;
import org.sakaiproject.rights.api.CreativeCommonsLicense;
import org.sakaiproject.rights.api.RightsAssignment;
import org.sakaiproject.rights.api.SiteRightsPolicy;
import org.sakaiproject.rights.api.UserRightsPolicy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public abstract class BaseRightsService implements RightsService 
{
	public static class BasicCopyright implements Copyright 
	{

		protected String m_id;
		protected String m_entityRef;
		protected String m_year;
		protected String m_owner;
		
		public BasicCopyright(String entityRef)
		{
			m_entityRef = entityRef;
		}
		
		public BasicCopyright(String entityRef, String year, String owner)
		{
			m_entityRef = entityRef;
			m_year = year;
			m_owner = owner;
		}
		
		public String getCopyrightId() 
		{
			return m_id;
		}

		public String getEntityRef() 
		{
			return m_entityRef;
		}

		public String getOwner() 
		{
			return m_owner;
		}

		public String getYear() 
		{
			return m_year;
		}

		public void setOwner(String owner) 
		{
			m_owner = owner;
		}

		public void setYear(String year) 
		{
			m_year = year;
		}

		public Element toXml(Document doc, Stack<Object> stack) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}	// class BasicCopyright

		
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	public static class BasicRightsAssignment implements RightsAssignment
	{
		protected Map<String, CreativeCommonsLicense> m_licenses = new Hashtable<String, CreativeCommonsLicense>();
		protected Copyright m_copyright;
		protected String m_entityRef = null;
		protected String m_id;
		protected boolean m_copyrightAlert = false;

		public BasicRightsAssignment(String entityRef) 
		{
			m_id = IdManager.createUuid();
			m_entityRef = entityRef;
			
		}

		public void addLicense(CreativeCommonsLicense license) 
		{
			if(m_licenses == null)
			{
				m_licenses = new Hashtable<String, CreativeCommonsLicense>();
			}
			m_licenses.put(license.getIdentifier(), license);
		}

		public int countLicenses() 
		{
			return m_licenses.size();
		}

		public Copyright getCopyright() 
		{
			return m_copyright;
		}

		public String getEntityRef() 
		{
			return m_entityRef;
		}

		public Collection<CreativeCommonsLicense> getLicenses() 
		{
			return m_licenses.values();
		}

		public String getRightsId() 
		{
			return m_id;
		}

		public boolean hasCopyright() 
		{
			return m_copyright != null;
		}

		public boolean hasCopyrightAlert() 
		{
			return m_copyrightAlert;
		}

		public boolean hasLicense() 
		{
			return m_licenses != null && ! m_licenses.isEmpty();
		}
		
		public void setCopyright(Copyright copyright) 
		{
			m_copyright = copyright;
		}

		public void setLicenses(Collection<CreativeCommonsLicense> licenses) 
		{
			
		}

		public Element toXml(Document doc, Stack<Object> stack) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}

	public interface Storage
	{
		public void close();
		public Copyright getCopyright(String copyrightId) throws IdUnusedException;
		
		public CreativeCommonsLicense getLicense(String licenseId) throws IdUnusedException;
		public RightsAssignment getRightsAssignment(String entityRef) throws IdUnusedException;
		public RightsPolicy getRightsPolicy(String context, String userId) throws IdUnusedException;
		public Copyright newCopyright(String rightsId);
		
		public CreativeCommonsLicense newLicense(String rightsId);
		public RightsAssignment newRightsAssignment(String entityRef);
		public RightsPolicy newRightsPolicy(String context, String userId);
		public void open();
		
		public void remove(Copyright copyright);
		public void remove(CreativeCommonsLicense license);
		public void remove(RightsAssignment rights);
		public void remove(RightsPolicy policy);
		
		public String save(Copyright copyright);
		public String save(CreativeCommonsLicense license);
		public String save(RightsAssignment rights);
		public String save(RightsPolicy policy);
		
	}

		
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseRightsService.class);
	
	protected Storage m_storage = null;

	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment addRightsAssignment(String entityRef)
	{
		return m_storage.newRightsAssignment(entityRef);
	}

	public SiteRightsPolicy addSiteRightsPolicy(String context) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public UserRightsPolicy addUserRightsPolicy(String context, String userId) 
	{
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		M_log.info("destroy()");

	}
	
	public RightsAssignment getRightsAssignment(String entityRef) throws IdUnusedException 
	{
		return m_storage.getRightsAssignment(entityRef);
	}
	
	public SiteRightsPolicy getSiteRightsPolicy(String context) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public UserRightsPolicy getUserRightsPolicy(String context, String userId) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// construct a storage helper and read
			m_storage = newStorage();
			m_storage.open();

			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

	} // init

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected abstract Storage newStorage();

	/**
	 * @param rights
	 */
	public void save(RightsAssignment rights)
	{
		m_storage.save(rights);
	}

	public void save(RightsPolicy policy) 
	{
		m_storage.save(policy);
	}

	public void setRightsAssignment(String entityRef, RightsAssignment rights) 
	{
		// m_storage
	}

}	// class BaseCopyrightService
