/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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



package org.sakaiproject.tool.assessment.settings;

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
 * @version $Id$
 */
public class PathInfo
    //extends AbstractCompositeObject
{
      private static Log log = LogFactory.getLog(OjbConfigListener.class);
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
    props.load(new FileInputStream(pathToSecurity + "/" + fileName));

    return props;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param fileName DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws IOException DOCUMENTATION PENDING
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
    props.load(new FileInputStream(pathToSettings + "/" + fileName));

    return props;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getBasePathToSecurity()
  {
    log.debug("getBasePathToSecurity()");

    return basePathToSecurity;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param basePathToSecurity DOCUMENTATION PENDING
   *
   * @throws IllegalArgumentException DOCUMENTATION PENDING
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
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getBasePathToSettings()
  {
    log.debug("getBasePathToSettings()");

    return basePathToSettings;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param basePathToSettings DOCUMENTATION PENDING
   *
   * @throws IllegalArgumentException DOCUMENTATION PENDING
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
   * DOCUMENT ME!
   *
   * @return
   */
  public String getPathToSecurity()
  {
    return pathToSecurity;
  }

  /**
   * DOCUMENT ME!
   *
   * @return
   */
  public String getPathToSettings()
  {
    return pathToSettings;
  }
}
