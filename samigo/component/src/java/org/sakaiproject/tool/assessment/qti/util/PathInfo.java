/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/util/PathInfo.java $
* $Id: PathInfo.java 1288 2005-08-19 02:58:02Z esmiley@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    props.load(new FileInputStream(pathToSecurity + "/" + fileName));

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
    props.load(new FileInputStream(pathToSettings + "/" + fileName));

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
