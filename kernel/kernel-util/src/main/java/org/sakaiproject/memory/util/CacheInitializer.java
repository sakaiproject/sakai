/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.memory.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.config.CacheConfiguration;

/**
 * Utility class to configure a cache. Could have used common beanutils but
 * didn't want another library.
 * 
 * @author buckett
 * @deprecated since Sakai 2.9, do not use this anymore (use the sakai config settings instead), this will be removed in 11
 */
@Slf4j
public class CacheInitializer {
	private Map<String, String> configMap;

	public CacheInitializer() {

	}

	/**
	 * Set the configuration that needs to be set on the cache.
	 * 
	 * @param config
	 *            The unsplit configuration. Eg:
	 *            "timeToLiveSeconds=400,timeToIdleSeconds=100"
	 * @return This object.
	 */
	public CacheInitializer configure(String config) {
		configMap = new HashMap<String, String>();
		String[] configParts = config.split(",");
		for (String part : configParts) {
			String[] splitParts = part.split("=", 2);
			if (splitParts.length == 2) {
				String key = splitParts[0];
				String value = splitParts[1];
				configMap.put(key, value);
			} else {
				log.warn("Couldn't parse cache config of: " + part);
			}
		}
		return this;
	}

	/**
	 * Configure
	 * 
	 * @param cacheConfig
	 * @return
	 */
	public CacheInitializer initialize(CacheConfiguration cacheConfig) {
		if (configMap == null) {
			throw new IllegalStateException(
					"You must configure the initializer first.");
		}
		Method[] methods = cacheConfig.getClass().getMethods();
		for (Method method : methods) {
			if (Modifier.isPublic(method.getModifiers())
					&& method.getName().startsWith("set")
					&& method.getParameterTypes().length == 1) {
				// Ok we can handle this method.
				String key = Character.toLowerCase(method.getName().charAt(
						"set".length()))
						+ method.getName().substring("set".length() + 1);
				log.debug("Looking in config map for: {}", key);
				String value = configMap.get(key);
				if (value != null) {
					Class clazz = method.getParameterTypes()[0];
					log.debug("Need to convert to :" + clazz);
					Object obj = covertValue(value, clazz);

					if (obj != null) {
						invokeMethod(method, cacheConfig, obj);
						log.debug("Setting {}#{} to {}", clazz, key, value);
					}

				}

			}
		}
		return this;
	}

	private Object covertValue(String value, Class clazz) {
		Object obj = null;
		try {
			if (String.class.equals(clazz)) {
				obj = value;
			} else if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
				obj = Integer.valueOf(value);
			} else if (Boolean.class.equals(clazz)
					|| boolean.class.equals(clazz)) {
				obj = Boolean.valueOf(value);
			} else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
				obj = Long.valueOf(value);
			} else if (Float.class.equals(clazz) || float.class.equals(clazz)) {
				obj = Float.valueOf(value);
			} else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
				obj = Double.valueOf(value);
			} else if (Character.class.equals(clazz)
					|| char.class.equals(clazz)) {
				obj = Character.valueOf(value.charAt(0));
			} else {
				log.debug("Can't convert to :{}", clazz);
			}
		} catch (NumberFormatException nfe) {
			log.debug("Ignored bad number: {}", value);
		}
		return obj;
	}

	private void invokeMethod(Method method, Object obj, Object value) {
		try {
			method.invoke(obj, value);
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

}
