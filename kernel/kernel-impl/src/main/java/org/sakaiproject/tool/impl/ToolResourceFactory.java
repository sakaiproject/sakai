/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
