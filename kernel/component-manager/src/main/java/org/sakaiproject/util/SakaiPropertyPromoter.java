/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.util;

import java.util.Map;
import java.util.Properties;

/**
 * Some Sakai properties have traditionally also been exposed as Java system properties.
 * This is a simple utility class to handle that chore.
 */
public class SakaiPropertyPromoter {
	private SakaiProperties sakaiProperties;
	private Map<String, String> sakaiPropertyToSystemPropertyMap;
	
	public void init() {
		Properties props = sakaiProperties.getProperties();
		for (String sakaiPropertyName : sakaiPropertyToSystemPropertyMap.keySet()) {
			String sakaiPropertyValue = props.getProperty(sakaiPropertyName);
			if (sakaiPropertyValue != null) {
				System.setProperty(sakaiPropertyToSystemPropertyMap.get(sakaiPropertyName), sakaiPropertyValue);
			}
		}
	}
	
	public void setSakaiProperties(SakaiProperties sakaiProperties) {
		this.sakaiProperties = sakaiProperties;
	}
	public void setSakaiPropertyToSystemPropertyMap(Map<String, String> sakaiPropertyToSystemPropertyMap) {
		this.sakaiPropertyToSystemPropertyMap = sakaiPropertyToSystemPropertyMap;
	}
}
