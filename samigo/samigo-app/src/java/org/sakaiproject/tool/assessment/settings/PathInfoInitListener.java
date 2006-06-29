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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Initializes PathInfo at startup.
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class PathInfoInitListener
  implements ServletContextListener
{
  private static Log log = LogFactory.getLog(OjbConfigListener.class);

  /**
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent sce)
  {
    log.debug(
      "PathInfoInitListener.contextInitialized(ServletContextEvent " + sce +
      ")");
    String pathToSecurity = null;
    String pathToSettings = null;
    try
    {
      PathInfo pathInfo = PathInfo.getInstance();
      pathToSecurity =
        sce.getServletContext().getInitParameter("PathToSecurity");
      log.debug("PathToSecurity=" + pathToSecurity);
      if((pathToSecurity == null) || (pathToSecurity.length() < 1))
      {
        throw new IllegalStateException("PathToSecurity is invalid!");
      }

      pathToSettings =
        sce.getServletContext().getInitParameter("PathToSettings");
      log.debug("PathToSettings=" + pathToSettings);
      if((pathToSettings == null) || (pathToSettings.length() < 1))
      {
        throw new IllegalStateException("PathToSettings is invalid!");
      }

      if((pathToSecurity != null) && (pathToSettings != null))
      {
        pathInfo.setBasePathToSecurity(pathToSecurity);
        pathInfo.setBasePathToSettings(pathToSettings);
      }

      log.debug("PathInfoInitListener initialized successfully!");
    }
    catch(Exception ex)
    {
      log.error(ex);
      throw new RuntimeException(ex);
    }
  }

  /**
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent sce)
  {
    ;
  }
}
