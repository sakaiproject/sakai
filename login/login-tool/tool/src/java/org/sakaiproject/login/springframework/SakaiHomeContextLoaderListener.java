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
package org.sakaiproject.login.springframework;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/29/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class SakaiHomeContextLoaderListener extends SakaiHomeContextLoader implements ServletContextListener {
    private static final Log log = LogFactory.getLog(SakaiHomeContextLoaderListener.class);

	/**
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		initWebApplicationContext(event.getServletContext());

		// increment the count of children for the component manager
		((SpringCompMgr) ComponentManager.getInstance()).addChildAc();
	}

	/**
	 * Close the root web application context.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		closeWebApplicationContext(event.getServletContext());
		//ContextCleanupListener.cleanupAttributes(event.getServletContext());
		
		log.info("Destroying Components in "+event.getServletContext().getServletContextName());

		// decrement the count of children for the component manager
		((SpringCompMgr) ComponentManager.getInstance()).removeChildAc();
	}
}
