/**********************************************************************************
 * $URL:
 * $Id:
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.citation.api;

import java.util.List;
import org.sakaiproject.citation.util.api.OsidConfigurationException;

/**
 * Repository OSID configuration
 */
public interface ConfigurationService
{
  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML (eg file:///tomcat-home/sakai/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException;

  /**
   * Is the configuration XML available (was the URI provided)?
   * @return true If the XML file is available
   */
  public boolean isConfigurationXmlAvailable();

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException;

  /**
   * Is the database hierarchy XML available (was the URI provided)?
   * @return true If the XML file is available
   */
  public boolean isDatabaseHierarchyXmlAvailable();

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
   * Is library search enabled?
   * @return true if so
   */
  public boolean isLibrarySearchEnabled();

}