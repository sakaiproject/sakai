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
