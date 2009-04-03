/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package edu.amc.sakai.user;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertyResolver extends PropertyPlaceholderConfigurer {

	/**
	 * Essentially a public version of {@link #parseStringValue(String, java.util.Properties, String)}
	 * which takes a property name (without placeholder prefix or suffix) and returns the value
	 * which would be injected into the <code>BeanFactory</code> at startup. Useful for
	 * resolving configuration properties with recursive definitions.
	 * 
	 * <p>Somewhat fragile b/c we do not have access to the actual placeholder 
	 * prefix/suffix values. Also not the fastest operation in the world... each call 
	 * involves a call to {@link #mergeProperties()}.</p>
	 * 
	 * @param propertyName
	 * @return
	 */
	public String getStringValue(String propertyName) {
		try {
			return parseStringValue(DEFAULT_PLACEHOLDER_PREFIX + 
					propertyName + DEFAULT_PLACEHOLDER_SUFFIX, 
					mergeProperties(), 
					new HashSet());
		} catch ( IOException e ) {
			throw new RuntimeException("Failed to resolve property name [" + propertyName + "]");
		}
	}

	/** 
	 * Access all property keys known to this {@link PropertyResolver}.
	 * 
	 * @return a disconnected collection of property keys
	 */
	public Collection<String> getPropertyKeys() {
		try {
			return new HashSet(mergeProperties().keySet());
		} catch ( IOException e ) {
			throw new RuntimeException("Failed to generate property keyset", e);
		}
	}

}
