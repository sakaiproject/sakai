/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
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
import org.sakaiproject.rights.util.RightsException;

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

	public static class BasicCreativeCommonsLicense implements CreativeCommonsLicense
	{
		protected String m_id;
		protected Set<Permission> m_permissions = new TreeSet<Permission>();
		protected Set<Prohibition> m_prohibitions = new TreeSet<Prohibition>();
		protected Set<Requirement> m_requirements = new TreeSet<Requirement>();

		public void addPermission(Permission permission) 
		{
			if(m_permissions == null)
			{
				m_permissions = new TreeSet<Permission>();
			}
			m_permissions.add(permission);
		}

		public void addPermission(String permission)  throws RightsException
		{
			Permission p = Permission.fromString(permission);
			if(p == null)
			{
				throw new RightsException();
			}
			addPermission(p);
		}

		public void addProhibition(Prohibition prohibition) 
		{
			if(m_prohibitions == null)
			{
				m_prohibitions = new TreeSet<Prohibition>();
			}
			m_prohibitions.add(prohibition);
		}

		public void addProhibition(String prohibition) throws RightsException
		{
			Prohibition p = Prohibition.fromString(prohibition);
			if(p == null)
			{
				throw new RightsException();
			}
			addProhibition(p);
		}

		public void addRequirement(Requirement requirement) 
		{
			if(m_requirements == null)
			{
				m_requirements = new TreeSet<Requirement>();
			}
			m_requirements.add(requirement);
		}

		public void addRequirement(String requirement) throws RightsException 
		{
			Requirement r = Requirement.fromString(requirement);
			if(r == null)
			{
				throw new RightsException();
			}
			addRequirement(r);
		}

		public String getIdentifier() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Collection<Permission> getPermissions() 
		{
			return m_permissions;
		}

		public Collection<Prohibition> getProhibitions() 
		{
			return m_prohibitions;
		}

		public Collection<Requirement> getRequirements() 
		{
			return m_requirements;
		}

		public String getUri() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasPermissions() 
		{
			return m_permissions != null && ! m_permissions.isEmpty();
		}

		public boolean hasProhibitions() 
		{
			return m_prohibitions != null && ! m_prohibitions.isEmpty();
		}

		public boolean hasRequirements() 
		{
			return m_requirements != null && ! m_requirements.isEmpty();
		}

		public void removePermission(String permission) 
		{
			Permission p = Permission.fromString(permission);
			if(p != null)
			{
				this.m_permissions.remove(p);
			}
		}

		public void removeProhibitions(Collection<Object> prohibitions) 
		{
			if(prohibitions != null)
			{
				for (Object obj : prohibitions)
				{
					Prohibition p = null;
					if(obj instanceof Prohibition)
					{
						p = (Prohibition) obj;
					}
					else if(obj instanceof String)
					{
						p = Prohibition.fromString((String) obj);
					}
					if(p != null)
					{
						this.m_prohibitions.remove(p);
					}
				}
			}
		}

		public void removeRequirements(Collection<Object> requirements) 
		{
			if(this.m_requirements == null)
			{
				this.m_requirements = new TreeSet<Requirement>();
			}
			this.m_requirements.clear();
			
			if(requirements != null)
			{
				for (Object obj : requirements)
				{
					Requirement r = null;
					if(obj instanceof Requirement)
					{
						r = (Requirement) obj;
					}
					else if(obj instanceof String)
					{
						r = Requirement.fromString((String) obj);
					}
					if(r != null)
					{
						this.m_requirements.remove(r);
					}
				}
			}
		}
		
		public void setPermissions(Collection<Object> permissions) 
		{
			if(this.m_permissions == null)
			{
				this.m_permissions = new TreeSet<Permission>();
			}
			this.m_permissions.clear();
			
			if(permissions != null)
			{
				for (Object obj : permissions)
				{
					Permission p = null;
					if(obj instanceof Permission)
					{
						p = (Permission) obj;
					}
					else if(obj instanceof String)
					{
						p = Permission.fromString((String) obj);
					}
					if(p != null)
					{
						this.m_permissions.add(p);
					}
				}
			}
		}

		public void setProhibitions(Collection<Object> prohibitions) 
		{
			if(this.m_prohibitions == null)
			{
				this.m_prohibitions = new TreeSet<Prohibition>();
			}
			this.m_prohibitions.clear();
			
			if(prohibitions != null)
			{
				for (Object obj : prohibitions)
				{
					Prohibition p = null;
					if(obj instanceof Prohibition)
					{
						p = (Prohibition) obj;
					}
					else if(obj instanceof String)
					{
						p = Prohibition.fromString((String) obj);
					}
					if(p != null)
					{
						this.m_prohibitions.add(p);
					}
				}
			}
		}

		public void setRequirements(Collection<Object> requirements) 
		{
			// TODO Auto-generated method stub
			
		}

		public Element toXml(Document doc, Stack<Object> stack) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}	// class BasicCreativeCommonsLicense
		
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
