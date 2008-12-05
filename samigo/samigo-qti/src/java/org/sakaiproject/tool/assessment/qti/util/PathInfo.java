/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 Sakai Foundation
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



package org.sakaiproject.tool.assessment.qti.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class used for accessing settings and security directories
 * (and associated Properties).
 *
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: PathInfo.java 1288 2005-08-19 02:58:02Z esmiley@stanford.edu $
 */
public class PathInfo
{
  private static Log log = LogFactory.getLog(PathInfo.class);
  private static final String SETTINGS_DIR = "/org/sakaiproject/settings/sam";
  private static final String SECURITY_DIR = "/org/sakaiproject/security/sam";
  private static PathInfo _INSTANCE = new PathInfo();
  private String basePathToSecurity = "";
  private String basePathToSettings = "";
  private String pathToSecurity = "";
  private String pathToSettings = "";

  /**
   * Returns an instance of PathInfo.
   *
   * @return
   */
  public static PathInfo getInstance()
  {
    log.debug("getInstance()");

    return _INSTANCE;
  }

  /**
   * Constructor should never be exposed.  Singleton pattern.
   */
  private PathInfo()
  {
    log.debug("new PathInfo()");
  }

  /**
   * Returns a Properties from the security directory.
   *
   * @param fileName
   *
   * @return
   *
   * @throws IOException
   */
  public Properties getSecurityProperties(String fileName)
    throws IOException
  {
    if(log.isDebugEnabled())
    {
      log.debug("getSecurityProperties(String " + fileName + ")");
    }

    Properties props = null;
    props = new Properties();
    FileInputStream fileInputStream = new FileInputStream(pathToSecurity + "/" + fileName);
    try {
    	props.load(fileInputStream);
    }
    finally {
    	fileInputStream.close();
    }
    return props;
  }

  /**
   * Load properties from a file.
   *
   * @param fileName file name
   *
   * @return Properties object
   *
   * @throws IOException in reading file
   */
  public Properties getSettingsProperties(String fileName)
    throws IOException
  {
    if(log.isDebugEnabled())
    {
      log.debug("getSettingsProperties(String " + fileName + ")");
    }

    Properties props = null;
    props = new Properties();
    FileInputStream fileInputStream = new FileInputStream(pathToSettings + "/" + fileName);
    try {
    	props.load(fileInputStream);
    }
    finally {
    	fileInputStream.close();
    }
    return props;
  }

  /**
   * Get base path string to security.
   *
   * @return the path
   */
  public String getBasePathToSecurity()
  {
    log.debug("getBasePathToSecurity()");

    return basePathToSecurity;
  }

  /**
   * Set base path to security.
   *
   * @param basePathToSettings DOCUMENTATION PENDING
   *
   * @throws IllegalArgumentException if this is invalid path.
   */
  public void setBasePathToSecurity(String basePathToSecurity)
    throws IllegalArgumentException
  {
    if(log.isDebugEnabled())
    {
      log.debug("setBasePathToSecurity(String " + basePathToSecurity + ")");
    }

    if(basePathToSecurity == null)
    {
      throw new IllegalArgumentException(
        "illegal String basePathToSecurity argument: " + basePathToSecurity);
    }

    synchronized(this.basePathToSecurity)
    {
      this.basePathToSecurity = basePathToSecurity;
    }

    synchronized(this.pathToSecurity)
    {
      this.pathToSecurity = this.basePathToSecurity + SECURITY_DIR;
    }
  }

  /**
   * Get base path string to settings.
   *
   * @return the path
   */
  public String getBasePathToSettings()
  {
    log.debug("getBasePathToSettings()");

    return basePathToSettings;
  }

  /**
   * Set base path to settings.
   *
   * @param basePathToSettings DOCUMENTATION PENDING
   *
   * @throws IllegalArgumentException if this is invalid path.
   */
  public void setBasePathToSettings(String basePathToSettings)
    throws IllegalArgumentException
  {
    if(log.isDebugEnabled())
    {
      log.debug("setBasePathToSettings(String " + basePathToSettings + ")");
    }

    if(basePathToSettings == null)
    {
      throw new IllegalArgumentException(
        "illegal String basePathToSettings argument: " + basePathToSettings);
    }

    synchronized(this.basePathToSettings)
    {
      this.basePathToSettings = basePathToSettings;
    }

    synchronized(this.pathToSettings)
    {
      this.pathToSettings = this.basePathToSettings + SETTINGS_DIR;
    }
  }

  /**
   * Get base path string to security.
   *
   * @return the path
   */
  public String getPathToSecurity()
  {
    return pathToSecurity;
  }

  /**
   * Get base path string to settings.
   *
   * @return the path
   */
  public String getPathToSettings()
  {
    return pathToSettings;
  }
}
