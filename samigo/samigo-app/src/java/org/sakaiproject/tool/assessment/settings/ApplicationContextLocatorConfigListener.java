/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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



package org.sakaiproject.tool.assessment.settings;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.framework.ApplicationContextLocator;

/**
 * Intitializes ApplicationContextLocator at startup.
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class ApplicationContextLocatorConfigListener
  implements ServletContextListener
{
  private static Log log = LogFactory.getLog(ApplicationContextLocatorConfigListener.class);

  /**
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent sce)
  {
    log.debug(
      "ApplicationContextLocatorConfigListener.contextInitialized(ServletContextEvent sce)");

    PathInfo path = PathInfo.getInstance();

    Properties p;
    try {
      p = path.getSettingsProperties("spring_contexts.properties");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    ApplicationContextLocator.init(p);

    log.debug("ApplicationContextLocator initialized!");
  }

  /**
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent sce)
  {
    ;
  }
}
