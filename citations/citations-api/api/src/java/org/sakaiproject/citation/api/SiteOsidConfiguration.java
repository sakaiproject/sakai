/*******************************************************************************
 * $URL:
 * $Id:
 * **********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
 ******************************************************************************/
package org.sakaiproject.citation.api;

import java.util.List;
import org.sakaiproject.citation.util.api.OsidConfigurationException;

/**
 * Repository OSID configuration
 */
public interface SiteOsidConfiguration
{
  /**
   * Initialize
   */
  public void init() throws OsidConfigurationException;

  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML (eg file:///tomcat-home/sakai/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException;

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException;

  /**
   * Fetch this user's group affiliations
   * @return A list of group IDs (empty if no IDs exist)
   */
  public List<String> getGroupIds() throws OsidConfigurationException;
}
