/*******************************************************************************
 * $URL:
 * $Id:
 * **********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 ******************************************************************************/
package org.sakaiproject.citation.impl;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.citation.api.SiteOsidConfiguration;

/**
 * Sample Repository OSID configuration
 */
public class SampleSiteOsidConfiguration implements SiteOsidConfiguration
{
 	private static Log _log = LogFactory.getLog(SampleSiteOsidConfiguration.class);

 	/*
 	 * Sakaibrary XML configuration files (rooted in <tomcat-home>)
 	 */
  public static final String  CATEGORIES_XML      = "sakai/org.sakaiproject.citation/categories.xml";
  public static final String  CONFIGURATION_XML   = "sakai/org.sakaiproject.citation/configuration.xml";

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
    _log.debug("init()");
  }

  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML (eg file:///tomcat-home/sakai/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException
  {
    return CONFIGURATION_XML;
  }

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML (eg file:///tomcat-home/sakai/database.xml)
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

    groupList.add("all");
    groupList.add("free");
    return groupList;
  }
}
