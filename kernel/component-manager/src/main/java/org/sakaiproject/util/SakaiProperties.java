/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

/**
 * A configurer for "sakai.properties" files. These differ from the usual Spring default properties
 * files by mixing together lines which define property-value pairs and lines which define
 * bean property overrides. The two can be distinguished because Sakai conventionally uses
 * the bean name separator "@" instead of the default "."
 * 
 * This class creates separate PropertyPlaceholderConfigurer and PropertyOverrideConfigurer
 * objects to handle bean configuration, and loads them with the input properties.
 * 
 * SakaiProperties configuration supports most of the properties documented for 
 * PropertiesFactoryBean, PropertyPlaceholderConfigurer, and PropertyOverrideConfigurer.
 */
public class SakaiProperties implements BeanFactoryPostProcessorCreator, InitializingBean {
	private static Log log = LogFactory.getLog(SakaiProperties.class);
	private PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
	private ReversiblePropertyOverrideConfigurer propertyOverrideConfigurer = new ReversiblePropertyOverrideConfigurer();
	private PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
	
	public SakaiProperties() {
		// Set defaults.
		propertiesFactoryBean.setIgnoreResourceNotFound(true);
		propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
		propertyPlaceholderConfigurer.setOrder(0);
		propertyOverrideConfigurer.setBeanNameAtEnd(true);
		propertyOverrideConfigurer.setBeanNameSeparator("@");
		propertyOverrideConfigurer.setIgnoreInvalidKeys(true);
	}

	public void afterPropertiesSet() throws Exception {
		// Connect properties to configurers.
		propertiesFactoryBean.afterPropertiesSet();
		propertyPlaceholderConfigurer.setProperties((Properties)propertiesFactoryBean.getObject());
		propertyOverrideConfigurer.setProperties((Properties)propertiesFactoryBean.getObject());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.BeanFactoryPostProcessorCreator#getBeanFactoryPostProcessors()
	 */
	public Collection<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return (Arrays.asList(new BeanFactoryPostProcessor[] {propertyOverrideConfigurer, propertyPlaceholderConfigurer}));
	}
	
	public Properties getProperties() {
		Properties rawProperties = getRawProperties();
		Properties parsedProperties = dereferenceProperties(rawProperties);
		return parsedProperties; 
	}
	
	public Properties getRawProperties() {
		try {
			return (Properties)propertiesFactoryBean.getObject();
		} catch (IOException e) {
			if (log.isWarnEnabled()) log.warn("Error collecting Sakai properties", e);
			return new Properties();
		}
	}
	
	/**
	 * Dereferences property placeholders in the given {@link Properties}
	 * in exactly the same way the {@link BeanFactoryPostProcessor}s in this
	 * object perform their placeholder dereferencing. Unfortunately, this
	 * process is not readily decoupled from the act of processing a
	 * bean factory in the Spring libraries. Hence the reflection.
	 * 
	 * @param srcProperties a collection of name-value pairs
	 * @return a new collection of properties. If <code>srcProperties</code>
	 *   is <code>null</code>, returns null. If <code>srcProperties</code>
	 *   is empty, returns a reference to same object.
	 * @throws RuntimeException if any aspect of processing fails
	 */
	private Properties dereferenceProperties(Properties srcProperties) 
	throws RuntimeException {
		if ( srcProperties == null ) {
			return null;
		}
		if ( srcProperties.isEmpty() ) {
			return srcProperties;
		}
		try {
			Properties parsedProperties = new Properties();
			PropertyPlaceholderConfigurer resolver = new PropertyPlaceholderConfigurer();
			resolver.setIgnoreUnresolvablePlaceholders(true);
			Method parseStringValue = 
				resolver.getClass().getDeclaredMethod("parseStringValue", String.class, Properties.class, Set.class);
			parseStringValue.setAccessible(true);
			for ( Map.Entry<Object, Object> propEntry : srcProperties.entrySet() ) {
				String parsedPropValue = (String)parseStringValue.invoke(resolver, (String)propEntry.getValue(), srcProperties, new HashSet());
				parsedProperties.setProperty((String)propEntry.getKey(), parsedPropValue);
			}
			return parsedProperties;
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException("Failed to dereference properties", e);
		}
	}
	
	// Delegate properties loading.
	public void setProperties(Properties properties) {
		propertiesFactoryBean.setProperties(properties);
	}
	public void setPropertiesArray(Properties[] propertiesArray) {
		propertiesFactoryBean.setPropertiesArray(propertiesArray);
	}
	public void setLocation(Resource location) {
		propertiesFactoryBean.setLocation(location);
	}
	public void setLocations(Resource[] locations) {
		propertiesFactoryBean.setLocations(locations);
	}
	public void setFileEncoding(String encoding) {
		propertiesFactoryBean.setFileEncoding(encoding);
	}
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		propertiesFactoryBean.setIgnoreResourceNotFound(ignoreResourceNotFound);
	}
	public void setLocalOverride(boolean localOverride) {
		propertiesFactoryBean.setLocalOverride(localOverride);
	}

	// Delegate PropertyPlaceholderConfigurer.
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(ignoreUnresolvablePlaceholders);
	}
	public void setOrder(int order) {
		propertyPlaceholderConfigurer.setOrder(order);
	}
	public void setPlaceholderPrefix(String placeholderPrefix) {
		propertyPlaceholderConfigurer.setPlaceholderPrefix(placeholderPrefix);
	}
	public void setPlaceholderSuffix(String placeholderSuffix) {
		propertyPlaceholderConfigurer.setPlaceholderSuffix(placeholderSuffix);
	}
	public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
		propertyPlaceholderConfigurer.setSearchSystemEnvironment(searchSystemEnvironment);
	}
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		propertyPlaceholderConfigurer.setSystemPropertiesMode(systemPropertiesMode);
	}
	public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
		propertyPlaceholderConfigurer.setSystemPropertiesModeName(constantName);
	}

	// Delegate PropertyOverrideConfigurer.
	public void setBeanNameAtEnd(boolean beanNameAtEnd) {
		propertyOverrideConfigurer.setBeanNameAtEnd(beanNameAtEnd);
	}
	public void setBeanNameSeparator(String beanNameSeparator) {
		propertyOverrideConfigurer.setBeanNameSeparator(beanNameSeparator);
	}
	public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
		propertyOverrideConfigurer.setIgnoreInvalidKeys(ignoreInvalidKeys);
	}
}
