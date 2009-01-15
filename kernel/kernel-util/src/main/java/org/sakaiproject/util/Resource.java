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

package org.sakaiproject.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;

public class Resource {
	private ResourceLoader loader = null;
	private static Log log = LogFactory.getLog(Resource.class);
	
	public Resource() {
		// constructor
	}
	
	/**
	 * Get localized resource bundle via classLoader
	 * @param className Java class name providing access to the *.properties file
	 * @param bundleName default name of bundle.
	 * @return ResourceLoader
	 */
	public ResourceLoader getLoader(String resourceClass, String resourceBundle) {
		try {
			if ( loader == null ) {
				loader = new ResourceLoader(resourceBundle, ComponentManager.get(resourceClass).getClass().getClassLoader());
			}
		}
		catch (Exception e) {
			log.warn("WARN: Localized bundle not found: " + e.toString());
		}
		
		return loader;
	}
}
