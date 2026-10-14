/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.internal;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.descriptors.portlet.PortletDD;

/**
 * The portlet preferences validator registry. This class caches the portlet
 * preferences validator instances for portlet definitions, and ensure that
 * only one validator instance is created per portlet definition.
 *
 * @since 2006-02-10
 */
public class PreferencesValidatorRegistry {

	/** Logger. */
	private static final Log LOG = LogFactory.getLog(PreferencesValidatorRegistry.class);

	/** The static singleton registry instance. */
	private static final PreferencesValidatorRegistry REGISTRY =
			new PreferencesValidatorRegistry();


	// Private Member Variables ------------------------------------------------

	/**
	 * The preferences validator cache: key is the portlet definition, value is
	 * the portlet preferences validator instance.
	 */
	private final Map cache = new HashMap();


	// Constructor -------------------------------------------------------------

	/**
	 * Private constructor that prevents external instantiation.
	 */
	private PreferencesValidatorRegistry() {
		// Do nothing.
	}

	public static PreferencesValidatorRegistry getRegistry() {
		return REGISTRY;
	}


	// Public Methods ----------------------------------------------------------

	/**
	 * Returns the preferences validator instance for the given portlet
	 * definition. If no preferences validator class is defined for the portlet
	 * definition, null is returned. This method caches the validator instances
	 * in the cache to ensure that only one validator instance is created per
	 * portlet definition.
	 * @param portletDD  the portlet definition.
	 * @return the preferences validator if defined for the portlet definition.
	 * @throw ValidatorException  if fail to instantiate validator instance.
	 */
	public PreferencesValidator getPreferencesValidator(PortletDD portletDD)
	throws ValidatorException {

		// Try to retrieve the validator from cache.
		PreferencesValidator validator = (PreferencesValidator)
				cache.get(portletDD);
		if (validator != null) {
			return validator;
		}

		// Try to construct the validator instance for the portlet definition.
		String className = portletDD.getPortletPreferences()
				.getPreferencesValidator();
		if (className != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating preferences validator: " + className);
			}
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try {
				Class clazz = loader.loadClass(className);
			    validator = (PreferencesValidator) clazz.newInstance();
			    cache.put(portletDD, validator);
			} catch (InstantiationException ex) {
				LOG.error("Error instantiating validator.", ex);
			    throw new ValidatorException(ex, null);
			} catch (IllegalAccessException ex) {
				LOG.error("Error instantiating validator.", ex);
			    throw new ValidatorException(ex, null);
			} catch (ClassNotFoundException ex) {
				LOG.error("Error instantiating validator.", ex);
			    throw new ValidatorException(ex, null);
			} catch (ClassCastException ex) {
				LOG.error("Error casting instance to PreferencesValidator.", ex);
				throw new ValidatorException(ex, null);
			}
		}
		return validator;
	}

}
