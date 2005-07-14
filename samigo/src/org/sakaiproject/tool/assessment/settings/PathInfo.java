/**********************************************************************************
* $URL$
* $Id$
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

package org.sakaiproject.tool.assessment.settings;

//import org.navigoproject.AbstractCompositeObject;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

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
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(PathInfo.class);
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
    LOG.debug("getInstance()");

    return _INSTANCE;
  }

  /**
   * Constructor should never be exposed.  Singleton pattern.
   */
  private PathInfo()
  {
    LOG.debug("new PathInfo()");
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
    if(LOG.isDebugEnabled())
    {
      LOG.debug("getSecurityProperties(String " + fileName + ")");
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
    if(LOG.isDebugEnabled())
    {
      LOG.debug("getSettingsProperties(String " + fileName + ")");
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
    LOG.debug("getBasePathToSecurity()");

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
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setBasePathToSecurity(String " + basePathToSecurity + ")");
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
    LOG.debug("getBasePathToSettings()");

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
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setBasePathToSettings(String " + basePathToSettings + ")");
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
