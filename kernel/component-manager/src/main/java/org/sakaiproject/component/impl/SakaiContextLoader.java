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

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>
 * Sakai's extension to Spring's ContextLoader - adds the location of the ComponentManager shared AC, linking the local AC to it as parent, and loading localy hosted components into shared.
 * </p>
 */
@Slf4j
public class SakaiContextLoader extends ContextLoader
{
	public static final String SPRING_CONTEXT_SUFFIX = "-context.xml";

	/**
     * Allows loading/override of custom bean definitions from sakai.home
     *
     * <p>The pattern is the 'servlet_name-context.xml'</p>
     *
     * @param servletContext current servlet context
     * @return the new WebApplicationContext
     * @throws org.springframework.beans.BeansException
     *          if the context couldn't be initialized
     */
    @Override
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws BeansException {

        ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) super.initWebApplicationContext(servletContext);
        // optionally look in sakai home for additional bean deifinitions to load
		if (cwac != null) {
			final String servletName = servletContext.getServletContextName(); 
			String location = getHomeBeanDefinitionIfExists(servletName);
			if (StringUtils.isNotBlank(location)) {
				log.debug("Servlet " + servletName + " is attempting to load bean definition [" + location + "]");
				XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) cwac.getBeanFactory());
				try {
					int loaded = reader.loadBeanDefinitions(new FileSystemResource(location));
					log.info("Servlet " + servletName + " loaded " + loaded + " beans from [" + location + "]");
					AnnotationConfigUtils.registerAnnotationConfigProcessors(reader.getRegistry());
					cwac.getBeanFactory().preInstantiateSingletons();
				} catch (BeanDefinitionStoreException bdse) {
					log.warn("Failure loading beans from [" + location + "]", bdse);
				} catch (BeanCreationException bce) {
					log.warn("Failure instantiating beans from [" + location + "]", bce);
				}
			}
		}
        return cwac;
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
