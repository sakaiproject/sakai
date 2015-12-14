package org.sakaiproject.tool.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

/**
 * This is a factory for a ResourceLoader for the ActiveToolComponent.
 * The code here was re-factored from ActiveToolComponent to make the class more testable.
 *
 */
public class ToolResourceFactory {
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ToolProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.tool.tools";
	private static final String RESOURCECLASS = "resource.class.tool";
	private static final String RESOURCEBUNDLE = "resource.bundle.tool";
	
	
	private ServerConfigurationService serverConfigurationService;
	
	public ResourceLoader createInstance() {
		String resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		String resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);

		// Resource starts up the ComponentManager through the cover.
		return new Resource().getLoader(resourceClass, resourceBundle);
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
}
