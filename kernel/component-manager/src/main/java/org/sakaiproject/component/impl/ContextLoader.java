/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.component.impl;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * <p>
 * Sakai's extension to Spring's ContextLoader - adds the location of the ComponentManager shared AC, linking the local AC to it as parent, and loading localy hosted components into shared.
 * </p>
 */
public class ContextLoader extends org.springframework.web.context.ContextLoader
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ContextLoader.class);

	/** Name of servlet context parameter that can specify the config location for loading into the shared component set. */
	public static final String SHARED_LOCATION_PARAM = "contextSharedLocation";

	/**
	 * Initialize the local ApplicationContext, link it to the shared context, and load shared definitions into the shared context.
	 * 
	 * @param servletContext
	 *        current servlet context
	 * @return the new WebApplicationContext
	 * @throws BeansException
	 *         if the context couldn't be initialized
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws BeansException
	{
		WebApplicationContext rv = super.initWebApplicationContext(servletContext);

		// if we have a parent and any shared bean definitions, load them into the parent
		ConfigurableApplicationContext parent = (ConfigurableApplicationContext) rv.getParent();
		if (parent != null)
		{
			String sharedConfig = servletContext.getInitParameter(SHARED_LOCATION_PARAM);
			if (sharedConfig != null)
			{
				String[] locations = StringUtils.tokenizeToStringArray(sharedConfig,
						ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS);
				if (locations != null)
				{
					XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) parent.getBeanFactory());

					for (int i = 0; i < locations.length; i++)
					{
						try
						{
							reader.loadBeanDefinitions(rv.getResources(locations[i]));
						}
						catch (IOException e)
						{
							M_log.warn("exception loading into parent: " + e);
						}
					}
				}
			}
		}

		return rv;
	}

	/**
	 * Access the shared ApplicationContext
	 * 
	 * @param servletContext
	 *        (not used)
	 * @return The shared application context
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException
	{
		// get the component manager (we know it's a SpringCompMgr) and from that the shared AC
		ConfigurableApplicationContext sharedAc = ((SpringCompMgr) ComponentManager.getInstance()).getApplicationContext();

		return sharedAc;
	}
}
