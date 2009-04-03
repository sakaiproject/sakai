/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.gradebook.ui;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Provides a way to override UI bean definitions from the Sakai component
 * framework while still visibly injecting them in UI configuration files.
 * 
 * (If our UI logic was based on Spring beans rather than JSF backing beans,
 * there might be a more straightforward way to deliver this capability.)
 */
public class ConfigurationBean implements ApplicationContextAware{
	private static final Log log = LogFactory.getLog(ConfigurationBean.class);
    
    private ApplicationContext applicationContext;
	
	private Map<String, Object> pluginDefaults;
	private ServerConfigurationService serverConfigurationService;
	
	/**
	 * @param name
	 * @return the bean configured to match the given plug-in key, or null if none were found
	 */
	public Object getPlugin(String key) {
		Object target = null;
		if (log.isDebugEnabled()) log.debug("key=" + key + ", serverConfigurationService=" + serverConfigurationService + ", default=" + pluginDefaults.get(key));
		if (serverConfigurationService != null) {
			// As of Sakai 2.4, the framework's configuration service
			// returns the empty string instead of null if a property isn't found.
			target = StringUtils.stripToNull(serverConfigurationService.getString(key));
		}
		if (target == null) {
			target = pluginDefaults.get(key);
		}
		if (target != null) {
			if (target instanceof String) {
				// Assume that we got a bean name instead of the
				// bean itself.
				target = applicationContext.getBean((String)target);
			}
		}
		if (log.isDebugEnabled()) log.debug("for " + key + " returning " + target);
		return target;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setPluginDefaults(Map<String, Object> pluginDefaults) {
		this.pluginDefaults = pluginDefaults;
	}

	/**
	 * If this method is never called (as in the case of standalone builds), the
	 * default map of property values will be used. 
	 * @param serverConfigurationService
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
}
