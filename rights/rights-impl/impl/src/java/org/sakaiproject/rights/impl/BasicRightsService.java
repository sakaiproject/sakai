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

import java.util.Hashtable;
import java.util.Map;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.rights.impl.BaseRightsService;
import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.CreativeCommonsLicense;
import org.sakaiproject.rights.api.RightsAssignment;
import org.sakaiproject.rights.api.RightsPolicy;

public class BasicRightsService extends BaseRightsService 
{
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected static class BasicStorage implements Storage
	{
		protected Map<String, Copyright> m_copyrights;
		protected Map<String, CreativeCommonsLicense> m_licenses;
		protected Map<String, RightsAssignment> m_rightsAssignments;
		
		public BasicStorage()
		{
			m_copyrights = new Hashtable<String, Copyright>();
			m_licenses = new Hashtable<String, CreativeCommonsLicense>();
			m_rightsAssignments = new Hashtable<String, RightsAssignment>();
		}

		public void close() 
		{
			if(m_copyrights != null)
			{
				m_copyrights.clear();
				m_copyrights = null;
			}
			
			if(m_licenses != null)
			{
				m_licenses.clear();
				m_licenses = null;
			}
			
			if(m_rightsAssignments != null)
			{
				m_rightsAssignments.clear();
				m_rightsAssignments = null;
			}
		}

		public Copyright getCopyright(String copyrightId) throws IdUnusedException
		{
			Copyright copyright = m_copyrights.get(copyrightId);
			if(copyright == null)
			{
				throw new IdUnusedException(copyrightId);
			}
			return copyright;
		}

		public CreativeCommonsLicense getLicense(String licenseId) throws IdUnusedException
		{
			CreativeCommonsLicense license = m_licenses.get(licenseId);
			if(license == null)
			{
				throw new IdUnusedException(licenseId);
			}
			return license;
		}

		public RightsAssignment getRightsAssignment(String entityRef) throws IdUnusedException
		{
			RightsAssignment rights = m_rightsAssignments.get(entityRef);
			if(rights == null)
			{
				throw new IdUnusedException(entityRef);
			}
			return rights;
		}

		public RightsPolicy getRightsPolicy(String context, String userId)  throws IdUnusedException
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Copyright newCopyright(String rightsId) 
		{
			return new BasicCopyright(rightsId);
		}

		public CreativeCommonsLicense newLicense(String rightsId) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public RightsAssignment newRightsAssignment(String entityRef) 
		{
			return new BasicRightsAssignment(entityRef);
		}

		public RightsPolicy newRightsPolicy(String context, String userId) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public void open() 
		{
		}

		public void remove(Copyright copyright) 
		{
			// TODO Auto-generated method stub
			
		}

		public void remove(CreativeCommonsLicense license) 
		{
			// TODO Auto-generated method stub
			
		}

		public void remove(RightsAssignment rights) 
		{
			// TODO Auto-generated method stub
			
		}

		public void remove(RightsPolicy policy) 
		{
			// TODO Auto-generated method stub
			
		}

		public String save(Copyright copyright) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String save(CreativeCommonsLicense license) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String save(RightsAssignment rights) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String save(RightsPolicy policy) 
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseRightsService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Shutdown cleanly
	 */
	public void destroy()
	{
		
	}
	
	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
	}

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new BasicStorage();

	} // newStorage

}
