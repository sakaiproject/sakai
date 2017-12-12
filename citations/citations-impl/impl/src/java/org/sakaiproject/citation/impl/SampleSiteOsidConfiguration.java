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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.sakaiproject.citation.impl;

import java.util.List;
import java.util.ArrayList;

import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.citation.api.SiteOsidConfiguration;

/**
 * Sample Repository OSID configuration
 */
public class SampleSiteOsidConfiguration implements SiteOsidConfiguration
{
 	/*
 	 * Citation Helper XML database and configuration file names
 	 *
 	 * Set to null to force the use of values from components.xml
 	 */
  public static final String  CATEGORIES_XML      = "categories.xml";
  public static final String  CONFIGURATION_XML   = "config.xml";

 	/*
 	 * Group membership
 	 */
  public static final String  FULL_ACCESS_GROUP   = "all";
  public static final String  GUEST_ACCESS_GROUP  = "free";

  /**
   * Simple public constructor
   */
  public SampleSiteOsidConfiguration()
  {
  }

  /*
   * Interface methods
   */

  /**
   * Initialization - do whatever is appropriate here
   */
  public void init() throws OsidConfigurationException
  {
  }

  /**
   * Fetch the appropriate XML configuration document for this user.  Typically,
   * this will be a path relative to the root folder for citations configurations
   * ( e.g. samples/config.xml)
   *<p>
   * Return null to force the use of the siteConfigXml property from
   * components.xml
   *
   * @return Configuration XML (eg config.xml or samples/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException
  {
    return CONFIGURATION_XML;
  }

  /**
  /**
   * Fetch the appropriate XML database document for this user.  Typically,
   * this will be a path relative to the root folder for citations configurations
   * ( e.g. samples/categories.xml)
   *<p>
   * Return null to force the use of the databaseXml property from
   * components.xml
   *
   * @return Hierarchy XML (eg categories.xml or samples/categories.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException
  {
    return CATEGORIES_XML;
  }

  /**
   * Fetch this user's group affiliations (ALL in this example)
   * @return A list of group ID strings (empty if no IDs exist)
   */
  public List<String> getGroupIds() throws OsidConfigurationException
  {
    ArrayList<String> groupList = new ArrayList();

    groupList.add(FULL_ACCESS_GROUP);
    groupList.add(GUEST_ACCESS_GROUP);

    return groupList;
  }
}
