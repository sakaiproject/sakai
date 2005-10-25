/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/branches/oncourse/jsf/widgets/src/java/org/sakaiproject/jsf/util/ConfigurationResource.java $
* $Id: ConfigurationResource.java 597 2005-07-13 20:26:17Z janderse@umich.edu $
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



package org.sakaiproject.tool.messageforums.jsf;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Thin wrapper for lookup of configuration of resources.
 * @author Ed Smiley
 * @version $Id: ConfigurationResource.java 597 2005-07-13 20:26:17Z janderse@umich.edu $
 */
public class ConfigurationResource
{
  private ResourceBundle configurationBundle;
  private final String CONFIG_PACKAGE = "org.sakaiproject.tool.messageforums.jsf";

  /**
   * Get resources for default locale.
   */
  public ConfigurationResource()
  {
    configurationBundle = ResourceBundle.getBundle(CONFIG_PACKAGE + "." + "Configuration");
  }

  /**
   * Get resources for specific locale.
   * @param locale Locale
   */
  public ConfigurationResource(Locale locale)
  {
    configurationBundle = ResourceBundle.getBundle(CONFIG_PACKAGE + "." +
      "Configuration", locale);
  }

  /**
   * Look up key/value
   * @param key String
   * @return String value for key, or empty string if not found
   */
  public String get(String key)
  {
    try
    {
      return configurationBundle.getString(key);
    }
    catch (Exception ex)
    {
      return "";
    }
  }

  /**
   * Return true only if this key exists.
   * @param key String
   * @return boolean
   */
  public boolean exists(String key)
  {
    try {
      configurationBundle.getString(key);
      return true;
    }
    catch (Exception ex) {
      return false;
    }
  }

}
