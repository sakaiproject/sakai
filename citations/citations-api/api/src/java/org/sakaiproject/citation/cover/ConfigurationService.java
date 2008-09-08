/**********************************************************************************
 * $URL:  $
 * $Id:   $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.citation.cover;

import java.util.List;

import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.component.cover.ComponentManager;

/*
 * Static covers for ConfigurationService API methods; adheres to
 * the ConfigurationService API interface definition
 */
public class ConfigurationService
{
	private static org.sakaiproject.citation.api.ConfigurationService m_instance;

	/**
	 * @return An instance of the ConfigurationService
	 */
	public static org.sakaiproject.citation.api.ConfigurationService getInstance()
	{ /*
	   * Caching?
	   */
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
			{
				m_instance = (org.sakaiproject.citation.api.ConfigurationService)
				        ComponentManager.get(
				              org.sakaiproject.citation.api.ConfigurationService.class);
			}
			return m_instance;
		}
		/*
		 * No cache
		 */
		return (org.sakaiproject.citation.api.ConfigurationService)
		            ComponentManager.get(
		                  org.sakaiproject.citation.api.ConfigurationService.class);
	}

  /**
   * Fetch the appropriate XML configuration document for this user
   */
	public static String getConfigurationXml() throws OsidConfigurationException
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getConfigurationXml();
	}

	  /**
	   * Is the configuration XML file provided and readable
	   * @return true If the XML file is provided and readable, false otherwise
	   */
	public static boolean isConfigurationXmlAvailable()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isConfigurationXmlAvailable();
	}

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   */
	public static String getDatabaseHierarchyXml() throws OsidConfigurationException
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getDatabaseHierarchyXml();
	}

	  /**
	   * Is the database hierarchy XML file provided and readable
	   * @return true If the XML file is provided and readable, false otherwise
	   */
	public static boolean isDatabaseHierarchyXmlAvailable()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isDatabaseHierarchyXmlAvailable();
	}

  /**
   * Fetch this user's group affiliations
   */
	public static List<String> getGroupIds() throws OsidConfigurationException
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getGroupIds();
	}

  /**
   * Fetch the site specific Repository OSID package name
   */
	public static String getSiteConfigOsidPackageName()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigOsidPackageName();
	}

  /**
   * Fetch the site specific extended Repository ID
   */
	public static String getSiteConfigExtendedRepositoryId()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigExtendedRepositoryId();
	}

  /**
   * Fetch the meta-search username
   */
	public static String getSiteConfigMetasearchUsername()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigMetasearchUsername();
	}

  /**
   * Fetch the meta-search password
   */
	public static String getSiteConfigMetasearchPassword()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigMetasearchPassword();
	}

  /**
   * Fetch the meta-search base-URL
   */
	public static String getSiteConfigMetasearchBaseUrl()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigMetasearchBaseUrl();
	}

  /**
   * Fetch the OpenURL label
   */
	public static String getSiteConfigOpenUrlLabel()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigOpenUrlLabel();
	}

  /**
   * Fetch the OpenURL resolver address
   */
	public static String getSiteConfigOpenUrlResolverAddress()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigOpenUrlResolverAddress();
	}

  /**
   * Fetch the Google base-URL
   */
	public static String getSiteConfigGoogleBaseUrl()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigGoogleBaseUrl();
	}

  /**
   * Fetch the Sakai server key
   */
	public static String getSiteConfigSakaiServerKey()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return null;
		}
		return instance.getSiteConfigSakaiServerKey();
	}

  /**
   * Should we use "preferred" URLs found by Library Search as the title link?
   * @return true if so
   */
	public static boolean getSiteConfigUsePreferredUrls()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.getSiteConfigUsePreferredUrls();
	}

  /**
   * Get the maximum number of databases we can search at one time
   */
  public static int getSiteConfigMaximumSearchableDBs()
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return org.sakaiproject.citation.api.ConfigurationService.SEARCHABLE_DATABASES;
		}
		return instance.getSiteConfigMaximumSearchableDBs();
	}

  /**
   * Enable/disable Google support
   */
  public static void setGoogleScholarEnabled(boolean state)
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return;
		}
		instance.setGoogleScholarEnabled(state);
	}

  /**
   * Is Google search enabled?
   */
  public static boolean isGoogleScholarEnabled()
  {
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isGoogleScholarEnabled();
	}

  /**
   * Enable/disable library support
   */
 public static void setLibrarySearchEnabled(boolean state)
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return;
		}
		instance.setLibrarySearchEnabled(state);
	}

  /**
   * Is library search enabled?
   */
  public static boolean isLibrarySearchEnabled()
  {
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isLibrarySearchEnabled();
	}

  /**
   * Enable/disable default Citations support
   */
  public static void setCitationsEnabledByDefault(boolean state)
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return;
		}
		instance.setCitationsEnabledByDefault(state);
	}

  /**
   * Is Citations support enabled by default?
   */
  public static boolean isCitationsEnabledByDefault()
  {
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isCitationsEnabledByDefault();
	}

  /**
   * Enable/disable site-by-site Citations support
   */
  public static void setAllowSiteBySiteOverride(boolean state)
	{
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return;
		}
		instance.setAllowSiteBySiteOverride(state);
	}

  /**
   * Is site-by-site Citations support enabled?
   */
  public static boolean isAllowSiteBySiteOverride()
  {
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.isAllowSiteBySiteOverride();
	}

  public static boolean librarySearchEnabled()
  {
		org.sakaiproject.citation.api.ConfigurationService instance = getInstance();
		if (instance == null)
		{
			return false;
		}
		return instance.librarySearchEnabled();
  }
}