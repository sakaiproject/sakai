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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.impl.SakaiContextLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/29/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class SakaiHomeContextLoader extends SakaiContextLoader {

    /**
     * Our logger.
     */
    private static Log M_log = LogFactory.getLog(SakaiHomeContextLoader.class);

    /**
     * Name of servlet context parameter that can specify the config location for loading into the shared component set.
     * The path will be relative to the sakai.home directory.
     */
    public static final String SAKAI_HOME_LOCATION_PARAM = "sakaiHomeContextLocation";

    public static final String SAKAI_HOME_CONTEXT_SUFFIX = "-context.xml";

    /**
     * Initialize the local ApplicationContext, link it to the shared context, and load shared definitions into the shared context.
     *
     * @param servletContext current servlet context
     * @return the new WebApplicationContext
     * @throws org.springframework.beans.BeansException
     *          if the context couldn't be initialized
     */
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws BeansException {

        WebApplicationContext rv = super.initWebApplicationContext(servletContext);
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) rv;

        if (configurableApplicationContext != null) {
            String sakaiHomeLocation = servletContext.getInitParameter(SAKAI_HOME_LOCATION_PARAM);
            String servletContextName = rv.getServletContext().getServletContextName();
            if (sakaiHomeLocation == null || sakaiHomeLocation.length() == 0) {
                sakaiHomeLocation = servletContextName + SAKAI_HOME_CONTEXT_SUFFIX;
            }
            if (sakaiHomeLocation != null) {
                final String sakaiHomePath = ServerConfigurationService.getSakaiHomePath();

                String[] locations = StringUtils.tokenizeToStringArray(sakaiHomeLocation,
                        ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS);
                if (locations != null) {

                    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) configurableApplicationContext.getBeanFactory());

                    for (int i = 0; i < locations.length; i++) {
                        String resourcePath = sakaiHomePath + locations[i];
                        M_log.debug(servletContextName + " is attempting to load Spring beans from: " + resourcePath);
                        if (new File(resourcePath).exists()) {
                            reader.loadBeanDefinitions(new FileSystemResource(resourcePath));
                        } else {
                            M_log.info(servletContext + " startup is skipping introspection of the resource: " + resourcePath +
                                " because it does not exist.");
                        }
                    }
                }
            }
        }

        return rv;
    }

}
