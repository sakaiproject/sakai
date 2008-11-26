/**********************************************************************************
 * $URL:
 * $Id:
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

package org.sakaiproject.citation.api;

import java.util.Collection;
import java.util.List;
import org.sakaiproject.citation.util.api.OsidConfigurationException;

/**
 * Repository OSID configuration
 */
public interface ConfigurationService
{
  /** 
   * Maximum number of databases to be searched at one time.  
   *
   * Eight is the value used in the original implementation - it's compatible 
   * with both the Ex Libris Xserver and the Sirsi Web2 Bridge.
   */
  public static final int SEARCHABLE_DATABASES = 8;
  
  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML (eg file:///tomcat-home/sakai/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException;

  /**
   * Is the configuration XML file provided and readable
   * @return true If the XML file is provided and readable, false otherwise
   */
  public boolean isConfigurationXmlAvailable();

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException;

  /**
   * Fetch the identifiers for all XML database hierarchy documents known
   * to the ConfigurationService.
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
   */
  public Collection<String> getAllCategoryXml();

  /**
   * Is the database hierarchy XML file provided and readable
   * @return true If the XML file is provided and readable, false otherwise
   */
  public boolean isDatabaseHierarchyXmlAvailable();

  /**
   * Fetch the reference string for the folder in ContentHosting containing
   * the config files, or null if access from CHS is not enabled.
   * @return the reference string (eg /content/group/citationsAdmin/config/)
   */
  public String getConfigFolderReference();

  /**
   * Fetch the collection-id for the folder in ContentHosting containing
   * the config files, or null if access from CHS is not enabled.
   * @return the resource-id string (eg /group/citationsAdmin/config/)
   */
  public String getConfigFolderId();

  /**
   * Fetch this user's group affiliations
   * @return A list of group IDs (empty if no IDs exist)
   */
  public List<String> getGroupIds() throws OsidConfigurationException;

  /**
   * Fetch the site specific Repository OSID package name
   * @return Repository Package (eg org.sakaibrary.osid.repository.xserver)
   */
  public String getSiteConfigOsidPackageName();

  /**
   * Fetch the site specific extended Repository ID
   * @return The Repository ID
   */
  public String getSiteConfigExtendedRepositoryId();

  /**
   * Fetch the meta-search username
   * @return the username
   */
  public String getSiteConfigMetasearchUsername();

  /**
   * Fetch the meta-search password
   * @return the username
   */
  public String getSiteConfigMetasearchPassword();

  /**
   * Fetch the meta-search base-URL
   * @return the base URL
   */
  public String getSiteConfigMetasearchBaseUrl();

  /**
   * Fetch the OpenURL label
   * @return the label text
   */
  public String getSiteConfigOpenUrlLabel();

  /**
   * Fetch the OpenURL resolver address
   * @return the address (domain name or IP)
   */
  public String getSiteConfigOpenUrlResolverAddress();

  /**
   * Fetch the Google base-URL
   * @return the URL
   */
  public String getSiteConfigGoogleBaseUrl();

  /**
   * Fetch the Sakai server key
   * @return the key text
   */
  public String getSiteConfigSakaiServerKey();

  /**
   * How should we use "preferred" URLs found by Library Search?
   * @return "false", "related-link", or "title-link"
   */
  public String getSiteConfigUsePreferredUrls();
  
  /**
   * Prefix string for "preferred" URLs (when used as title or related links).  
   *
   * This is likely to be the proxy information for the direct URL.
   *
   * @return The prefix String (null if none)
   */
  public String getSiteConfigPreferredUrlPrefix();

  /**
   * Get the maximum number of databases we can search at one time
   */
  public int getSiteConfigMaximumSearchableDBs();

  /**
   * Enable/disable Citations Helper by default
   * @param state true to set default 'On'
   */
  public void setCitationsEnabledByDefault(boolean state);

  /**
   * Is Citations Helper by default enabled?
   * @return true if so
   */
  public boolean isCitationsEnabledByDefault();

  /**
   * Enable/disable site by site Citations Helper override
   * @param state true to enable site by site Citations Helper
   */
  public void setAllowSiteBySiteOverride(boolean state);

  /**
   * Is site by site Citations Helper enabled?
   * @return true if so
   */
  public boolean isAllowSiteBySiteOverride();

  /**
   * Enable/disable Google support
   * @param state true to enable Google support
   */
  public void setGoogleScholarEnabled(boolean state);

  /**
   * Is Google search enabled?
   * @return true if so
   */
  public boolean isGoogleScholarEnabled();

  /**
   * Enable/disable library search support
   * @param state true to enable support
   */
  public void setLibrarySearchEnabled(boolean state);

  /**
   * Is library search enabled for any users?
   * @return true if so
   */
  public boolean isLibrarySearchEnabled();

  /**
   * Is library search enabled for the current user?
   * @return true if so
   */
  public boolean librarySearchEnabled();

}
