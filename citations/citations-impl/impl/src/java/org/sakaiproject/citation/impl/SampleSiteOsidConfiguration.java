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
 	 * Citation Helper XML configuration files (rooted in <sakai.home>)
 	 */
  public static final String  CATEGORIES_XML      = "org.sakaiproject.citation/categories.xml";
  public static final String  CONFIGURATION_XML   = "org.sakaiproject.citation/configuration.xml";

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
   * @return Configuration XML (eg samples/config.xml)
   */
  public String getConfigurationXml() throws OsidConfigurationException
  {
	  return "config01.xml";
    //return CONFIGURATION_XML;
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
   * @return Hierarchy XML (eg /tomcat-home/sakai/database.xml)
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException
  {
	  return "categories01.xml";
    //return CATEGORIES_XML;
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

  /*
   * Helpers
   */

  /**
   * Using a relative path specification, create a full path based
   * on the system level <code>sakai.home</code> property.
   *
   * @param relativePath Relative file specification (eg sakaibrary/config.xml)
   */
  private static String sakaiHome(String relativePath)
  {
    String sakaiHome = System.getProperty("sakai.home", "sakai");
    String separator = System.getProperty("file.separator");

    if ((!sakaiHome.endsWith(separator)) && (!sakaiHome.endsWith("/")))
    {
      sakaiHome += "/";
    }
    return sakaiHome + relativePath;
  }
}
