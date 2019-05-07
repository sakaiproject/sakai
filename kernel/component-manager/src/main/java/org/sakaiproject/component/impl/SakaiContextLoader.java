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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.impl;

import java.io.File;
import java.util.Arrays;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;

/**
 * <p>
 * Sakai's extension to Spring's {@link ContextLoader} - adds the location of the ComponentManager shared AC,
 * linking the local AC to it as parent, and loading localy hosted components into shared.
 * </p>
 */
@NoArgsConstructor
@Slf4j
public class SakaiContextLoader extends ContextLoader
{
	public static final String SPRING_CONTEXT_SUFFIX = "-context.xml";

	/**
	 * {@inheritDoc}
	 */
	public SakaiContextLoader(WebApplicationContext context) {
		super(context);
	}

	/**
	 * Allows loading/override of custom bean definitions from sakai.home
	 *
	 * <p>The pattern is the 'servlet_name-context.xml'</p>
	 *
	 * @param sc current servlet context
	 * @param wac the new WebApplicationContext
	 */
	@Override
	protected void customizeContext(ServletContext sc, ConfigurableWebApplicationContext wac) {
		super.customizeContext(sc, wac);
		if (wac != null) {
			final String servletName = sc.getServletContextName();
			String location = getHomeBeanDefinitionIfExists(servletName);
			if (StringUtils.isNotBlank(location)) {
				String[] configLocations = wac.getConfigLocations();
				String[] newLocations = Arrays.copyOf(configLocations, configLocations.length + 1);
				newLocations[configLocations.length] = "file:" + location;
				wac.setConfigLocations(newLocations);
				log.info("Servlet {} added an additional bean config location [{}]", servletName, location);
			}
		}
	}

	/**
	 * Spring allows a parent ApplicationContext to be set during the creation of a new ApplicationContext
	 *
	 * Sakai sets the SakaiApplicationContext as the parent which managed by the ComponentManager
	 * 
	 * @param servletContext (not used)
	 * @return the shared SakaiApplicationContext
	 */
	@Override
	protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException
	{
		// get the component manager (we know it's a SpringCompMgr) and from that the shared AC
		ConfigurableApplicationContext sharedAc = ((SpringCompMgr) ComponentManager.getInstance()).getApplicationContext();

		return sharedAc;
	}

	private String getHomeBeanDefinitionIfExists(String servletName) {
		if (StringUtils.isBlank(servletName)) {
			return null;
		}

		if (StringUtils.isNotBlank(servletName)) {
			String name = servletName + SPRING_CONTEXT_SUFFIX;
			String path = ServerConfigurationService.getSakaiHomePath();
			String location = path + name;

			log.debug("Servlet " + servletName + " is checking for a bean definition at sakai.home/" + name);
			if (new File(location).canRead()) {
				log.info("Servlet " + servletName + " located an additional bean definition at sakai.home/" + name);
				return location;
			} else {
				log.debug("Servlet " + servletName + " did not find a bean definition at sakai.home/" + name);
			}
		}
		return null;
	}
}
