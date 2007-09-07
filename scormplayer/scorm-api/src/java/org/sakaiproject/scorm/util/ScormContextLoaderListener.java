/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
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
package org.sakaiproject.scorm.util;

import javax.servlet.ServletContextEvent;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.scorm.service.api.ScormEntityProvider;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

public class ScormContextLoaderListener extends ContextLoaderListener {

	protected org.springframework.web.context.ContextLoader createContextLoader()
	{
		return new ContextLoader();
	}
	
	public void contextInitialized(ServletContextEvent event)
	{
		super.contextInitialized(event);
		
		System.out.println("Registering access provider!");
		
		HttpServletAccessProviderManager manager = 
			(HttpServletAccessProviderManager)((SpringCompMgr)ComponentManager.getInstance()).get("org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager");
		
		HttpServletAccessProvider provider = (HttpServletAccessProvider)((SpringCompMgr)ComponentManager.getInstance()).get("org.sakaiproject.scorm.client.ScormAccessProvider");
		
		manager.registerProvider(ScormEntityProvider.ENTITY_PREFIX, provider);
	}
	
	public void contextDestroyed(ServletContextEvent event)
	{
		super.contextDestroyed(event);
		
		HttpServletAccessProviderManager manager = 
			(HttpServletAccessProviderManager)((SpringCompMgr)ComponentManager.getInstance()).get("org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager");
		
		HttpServletAccessProvider provider = (HttpServletAccessProvider)((SpringCompMgr)ComponentManager.getInstance()).get("org.sakaiproject.scorm.client.ScormAccessProvider");
		
		manager.unregisterProvider(ScormEntityProvider.ENTITY_PREFIX, provider);
	}
	
}
