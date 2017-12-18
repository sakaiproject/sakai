/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
**********************************************************************************/



package org.sakaiproject.jsf.util;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * Thin wrapper for lookup of configuration of resources.
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
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
      log.debug("{}={}", key, value);
    }
    log.debug("xxx exists={}", cr.exists("xxx"));
    log.debug("xxx={}", cr.get("xxx"));
    log.debug("inputRichText_none exists={}", cr.exists("inputRichText_none"));
    log.debug("inputRichText_none={}", cr.get("inputRichText_none"));
    log.debug("inputRichText_small exists={}", cr.exists("inputRichText_small"));
    log.debug("inputRichText_small={}", cr.get("inputRichText_small"));
  }
}
