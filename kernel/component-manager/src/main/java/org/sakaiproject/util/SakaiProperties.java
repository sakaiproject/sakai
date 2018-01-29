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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

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
@Slf4j
public class SakaiProperties implements BeanFactoryPostProcessorCreator, InitializingBean {
    private SakaiPropertiesFactoryBean propertiesFactoryBean = new SakaiPropertiesFactoryBean();
    //private PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
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

    /**
     * Gets the individual properties from each properties file which is read in
     * 
     * @return a map of filename -> Properties
     */
    public Map<String, Properties> getSeparateProperties() {
        LinkedHashMap<String, Properties> m = new LinkedHashMap<String, Properties>();
        /* This doesn't work because spring always returns only the first of the properties files -AZ
         * very disappointing because it means we can't tell which file a property came from
        try {
            // have to use reflection to get the fields here because Spring does not expose them directly
            Field localPropertiesField = PropertiesLoaderSupport.class.getDeclaredField("localProperties");
            Field locationsField = PropertiesLoaderSupport.class.getDeclaredField("locations");
            localPropertiesField.setAccessible(true);
            locationsField.setAccessible(true);
            Properties[] localProperties = (Properties[]) localPropertiesField.get(propertiesFactoryBean);
            Resource[] locations = (Resource[]) locationsField.get(propertiesFactoryBean);
            log.info("found "+locations.length+" locations and "+localProperties.length+" props files");
            for (int i = 0; i < localProperties.length; i++) {
                Properties p = localProperties[i];
                Properties props = dereferenceProperties(p);
                Resource r = locations[i];
                log.info("found "+p.size()+" props ("+props.size()+") in "+r.getFilename());
                if (m.put(r.getFilename(), props) != null) {
                    log.warn("SeparateProperties: Found use of 2 sakai properties files with the same name (probable data loss): "+r.getFilename());
                }
            }
        } catch (Exception e) {
            log.warn("SeparateProperties: Failure trying to get the separate properties: "+e);
            m.clear();
            m.put("ALL", getProperties());
        }
        */
        /*
        m.put("ALL", getProperties());
        */
        for (Entry<String, Properties> entry : propertiesFactoryBean.getLoadedProperties().entrySet()) {
            m.put(entry.getKey(), dereferenceProperties(entry.getValue()));
        }
        return m;
    }

    /**
     * INTERNAL
     * @return the set of properties after processing
     */
    public Properties getProperties() {
        Properties rawProperties = getRawProperties();
        Properties parsedProperties = dereferenceProperties(rawProperties);
        return parsedProperties; 
    }

    /**
     * INTERNAL
     * @return the complete set of properties exactly as read from the files
     */
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
    private Properties dereferenceProperties(Properties srcProperties) throws RuntimeException {
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
                String parsedPropValue = (String)parseStringValue.invoke(resolver, (String)propEntry.getValue(), srcProperties, new HashSet<Object>());
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

    /**
     * Blatantly stolen from the Spring classes in order to get access to the properties files as they are read in,
     * this could not be done by overrides because the stupid finals and private vars, this is why frameworks should
     * never use final and private in their code.... sigh
     * 
     * @author Spring Framework
     * @author Aaron Zeckoski (azeckoski @ vt.edu)
     */
    public class SakaiPropertiesFactoryBean implements FactoryBean, InitializingBean {
        public static final String XML_FILE_EXTENSION = ".xml";
        
        private Map<String, Properties> loadedProperties = new LinkedHashMap<String, Properties>();
        /**
         * @return a map of file -> properties for everything loaded here
         */
        public Map<String, Properties> getLoadedProperties() {
            return loadedProperties;
        }

        private Properties[] localProperties;
        private Resource[] locations;
        private boolean localOverride = false;
        private boolean ignoreResourceNotFound = false;
        private String fileEncoding;
        private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

        private boolean singleton = true;
        private Object singletonInstance;
        public final void setSingleton(boolean singleton) {
            // ignore this
        }
        public final boolean isSingleton() {
            return this.singleton;
        }

        public final void afterPropertiesSet() throws IOException {
            if (this.singleton) {
                this.singletonInstance = createInstance();
            }
        }

        public final Object getObject() throws IOException {
            if (this.singleton) {
                return this.singletonInstance;
            } else {
                return createInstance();
            }
        }

        @SuppressWarnings("rawtypes")
        public Class getObjectType() {
            return Properties.class;
        }

        protected Object createInstance() throws IOException {
            return mergeProperties();
        }

        public void setProperties(Properties properties) {
            this.localProperties = new Properties[] {properties};
        }

        public void setPropertiesArray(Properties[] propertiesArray) { // unused
            this.localProperties = propertiesArray;
        }

        public void setLocation(Resource location) { // unused
            this.locations = new Resource[] {location};
        }

        public void setLocations(Resource[] locations) {
            this.locations = locations;
        }

        public void setLocalOverride(boolean localOverride) {
            this.localOverride = localOverride;
        }

        public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
            this.ignoreResourceNotFound = ignoreResourceNotFound;
        }

        public void setFileEncoding(String encoding) {
            this.fileEncoding = encoding;
        }

        public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
            this.propertiesPersister =
                    (propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister());
        }

        /**
         * Return a merged Properties instance containing both the loaded properties 
         * and properties set on this FactoryBean.
         */
        protected Properties mergeProperties() throws IOException {
            Properties result = new Properties();

            if (this.localOverride) {
                // Load properties from file upfront, to let local properties override.
                loadProperties(result);
            }

            if (this.localProperties != null) {
                for (int i = 0; i < this.localProperties.length; i++) {
                    loadedProperties.put("local"+i, this.localProperties[i]);
                    CollectionUtils.mergePropertiesIntoMap(this.localProperties[i], result);
                }
            }

            if (!this.localOverride) {
                // Load properties from file afterwards, to let those properties override.
                loadProperties(result);
            }

            if (log.isInfoEnabled()) log.info("Loaded a total of "+result.size()+" properties");
            return result;
        }

        /**
         * Load properties into the given instance.
         * 
         * @param props the Properties instance to load into
         * @throws java.io.IOException in case of I/O errors
         * @see #setLocations
         */
        protected void loadProperties(Properties props) throws IOException {
            if (this.locations != null) {
                for (int i = 0; i < this.locations.length; i++) {
                    Resource location = this.locations[i];
                    if (log.isDebugEnabled()) {
                        log.debug("Loading properties file from " + location);
                    }
                    InputStream is = null;
                    try {
                        Properties p = new Properties();
                        is = location.getInputStream();
                        if (location.getFilename().endsWith(XML_FILE_EXTENSION)) {
                            this.propertiesPersister.loadFromXml(p, is);
                        } else {
                            if (this.fileEncoding != null) {
                                this.propertiesPersister.load(p, new InputStreamReader(is, this.fileEncoding));
                            } else {
                                this.propertiesPersister.load(p, is);
                            }
                        }
                        if (log.isInfoEnabled()) {
                            log.info("Loaded "+p.size()+" properties from file " + location);
                        }
                        loadedProperties.put(location.getFilename(), p);
                        props.putAll(p); // merge the properties
                    } catch (IOException ex) {
                        if (this.ignoreResourceNotFound) {
                            if (log.isWarnEnabled()) {
                                log.warn("Could not load properties from " + location + ": " + ex.getMessage());
                            }
                        } else {
                            throw ex;
                        }
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        }
    }
}
