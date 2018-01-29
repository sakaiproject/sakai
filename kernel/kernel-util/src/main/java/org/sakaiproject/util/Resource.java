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

package org.sakaiproject.util;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class allows a resource bundle to be made accessible though a spring bean. 
 * This allows properties to be shared across multiple artifacts without each project having their own
 * copy of the bundle.
 *
 */
@Slf4j
public class Resource {
	public Resource() {
		// constructor
	}
	
	/**
	 * Get localized resource bundle via classLoader.
	 * @param className Java class name providing access to the *.properties file
	 * @param bundleName default name of bundle.
	 * @return ResourceLoader or <code>null</code> if the ResourceLoader failed to load.
	 */
	public ResourceLoader getLoader(String resourceClass, String resourceBundle) {
		ResourceLoader loader = null;
		try {
			loader = new ResourceLoader(resourceBundle, ComponentManager.get(resourceClass).getClass().getClassLoader());
			}
		catch (Exception e) {
			log.warn("WARN: Localized bundle not found: " + e.toString());
		}
		
		return loader;
	}
}
