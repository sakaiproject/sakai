/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SakaiContextLoader;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.WebApplicationContext;

/**
 * <p>
 * Sakai's extension to the Spring ContextLoaderListener - use our ContextLoader, and increment / decrement the child count of the ComponentManager on init / destroy.
 * </p>
 */
@NoArgsConstructor
@Slf4j
public class SakaiContextLoaderListener extends SakaiContextLoader implements ServletContextListener
{
	public SakaiContextLoaderListener(WebApplicationContext context) {
		super(context);
	}

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
		cleanupAttributes(event.getServletContext());

		log.debug("Destroying Components in {}", event.getServletContext().getServletContextName());

		// decrement the count of children for the component manager
		((SpringCompMgr) ComponentManager.getInstance()).removeChildAc();
	}

	/**
	 * Find all ServletContext attributes which implement {@link DisposableBean}
	 * and destroy them, removing all affected ServletContext attributes eventually.
	 * @param sc the ServletContext to check
	 */
	private static void cleanupAttributes(ServletContext sc) {
		Enumeration<String> attrNames = sc.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			if (attrName.startsWith("org.springframework.")) {
				Object attrValue = sc.getAttribute(attrName);
				if (attrValue instanceof DisposableBean) {
					try {
						((DisposableBean) attrValue).destroy();
					}
					catch (Throwable ex) {
						log.error("Couldn't invoke destroy method of attribute with name '" + attrName + "'", ex);
					}
				}
			}
		}
	}
}
