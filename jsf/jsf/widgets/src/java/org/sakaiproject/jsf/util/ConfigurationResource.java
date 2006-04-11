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



package org.sakaiproject.jsf.util;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Thin wrapper for lookup of configuration of resources.
 * @author Ed Smiley
 * @version $Id$
 */
public class ConfigurationResource
{
  private ResourceBundle configurationBundle;
  private final String CONFIG_PACKAGE = "org.sakaiproject.jsf";

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


  public static void main(String[] args)
  {
    ConfigurationResource cr = new ConfigurationResource();
    Enumeration enumeration = cr.configurationBundle.getKeys();
    while (enumeration.hasMoreElements())
    {
      String key = (String) enumeration.nextElement();
      String value = cr.get(key);
      System.out.println(key + "=" + value);
    }
    System.out.println("xxx exists" + "=" + cr.exists("xxx"));
    System.out.println("xxx" + "=" + cr.get("xxx"));
    System.out.println("inputRichText_none exists" + "=" + cr.exists("inputRichText_none"));
    System.out.println("inputRichText_none" + "=" + cr.get("inputRichText_none"));
    System.out.println("inputRichText_small exists" + "=" + cr.exists("inputRichText_small"));
    System.out.println("inputRichText_small" + "=" + cr.get("inputRichText_small"));
  }

}
