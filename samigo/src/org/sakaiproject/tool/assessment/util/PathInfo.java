/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

package org.sakaiproject.tool.assessment.util;

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
