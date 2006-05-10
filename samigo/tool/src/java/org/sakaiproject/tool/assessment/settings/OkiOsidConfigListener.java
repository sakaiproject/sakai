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
import osid.OsidException;
import osid.OsidLoader;
import osid.OsidOwner;

import org.sakaiproject.framework.Constants;
import org.sakaiproject.framework.ThreadLocalMapProvider;

/**
 * Configures OKI OSID impl at startup.
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class OkiOsidConfigListener
  implements ServletContextListener
{
  private static Log log = LogFactory.getLog(OkiOsidConfigListener.class);

  /**
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent sce)
  {
//    if(log.isDebugEnabled())
//    {
//      log.debug("contextInitialized(ServletContextEvent " + sce + ")");
//    }

		log.debug(
		"OkiOsidConfigListener.contextInitialized(ServletContextEvent " + sce + ")");

    try
    {
			PathInfo pathInfo = PathInfo.getInstance();
      OsidOwner owner = new OsidOwner();
      owner.addContext("PATH_TO_SECURITY", pathInfo.getBasePathToSecurity());
      owner.addContext("PATH_TO_SETTINGS", pathInfo.getBasePathToSettings());

      log.debug("PATH_TO_SETTINGS = " + owner.getContext("PATH_TO_SETTINGS"));
      log.debug(owner);
      //SharedManager sm = OsidManagerFactory.createSharedManager(owner);

      // this code initializes the PathInfo IN THE OKI LAYER!! in an indirect manner (through the manager)
			OsidLoader.getManager("osid.shared.SharedManager", "org.sakai.osid.shared.impl", owner);


      log.info("OkiOsidConfigListener initialized successfully!");

			ThreadLocalMapProvider.getMap().put(Constants.OSID_OWNER, owner);
    }
    catch(OsidException e)
    {
      log.fatal(e.getMessage(), e);
      throw new Error(e);
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
