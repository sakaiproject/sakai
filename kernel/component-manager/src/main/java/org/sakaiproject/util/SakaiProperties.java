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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.PropertyPlaceholderHelper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A configurer for "sakai.properties" files. These differ from the usual Spring default properties
 * files by mixing lines which define property-value pairs and lines which define
 * bean property overrides. The two can be distinguished because Sakai conventionally uses
 * the bean name separator "@" instead of the default "."
 * 
 * This class creates separate PropertyPlaceholderConfigurer and PropertyOverrideConfigurer
 * objects to handle bean configuration and loads them with the input properties.
 * 
 * SakaiProperties configuration supports most of the properties documented for 
 * PropertiesFactoryBean, PropertyPlaceholderConfigurer, and PropertyOverrideConfigurer.
 */
@Slf4j
public class SakaiProperties implements BeanFactoryPostProcessorCreator, InitializingBean {

    private final SakaiPropertiesFactoryBean propertiesFactoryBean;
    private final ReversiblePropertyOverrideConfigurer propertyOverrideConfigurer;
    private final PropertyPlaceholderConfigurer propertyPlaceholderConfigurer;

    public SakaiProperties() {
        propertiesFactoryBean = new SakaiPropertiesFactoryBean();
        propertiesFactoryBean.setIgnoreResourceNotFound(true);

        propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertyPlaceholderConfigurer.setOrder(0);

        propertyOverrideConfigurer = new ReversiblePropertyOverrideConfigurer();
        propertyOverrideConfigurer.setBeanNameAtEnd(true);
        propertyOverrideConfigurer.setBeanNameSeparator("@");
        propertyOverrideConfigurer.setIgnoreInvalidKeys(true);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Load demo properties when sakai.demo=true
        if ("true".equalsIgnoreCase(System.getProperty("sakai.demo"))) {
            Resource demoProperties = new ClassPathResource("org/sakaiproject/config/bundle/demo.sakai.properties");
            if (demoProperties.exists()) {
                log.info("Loading demo properties from {}", demoProperties.getFilename());
                propertiesFactoryBean.addLocation(demoProperties, "kernel.properties");
            }
        }

        // Connect properties to configurers.
        propertiesFactoryBean.afterPropertiesSet();
        propertyPlaceholderConfigurer.setProperties(propertiesFactoryBean.getObject());
        propertyOverrideConfigurer.setProperties(propertiesFactoryBean.getObject());
    }

    @Override
    public Collection<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
        return (Arrays.asList(new BeanFactoryPostProcessor[] {propertyOverrideConfigurer, propertyPlaceholderConfigurer}));
    }

    /**
     * Gets the individual properties from each properties file that is read in
     * 
     * @return a map of filename -> Properties
     */
    public Map<String, Properties> getSeparateProperties() {
        return propertiesFactoryBean.getLoadedProperties().entrySet().stream()
                .collect(LinkedHashMap::new,
                        (m, entry) -> m.put(entry.getKey(), dereferenceProperties(entry.getValue())),
                        LinkedHashMap::putAll);
    }

    /**
     * @return the map of properties after processing
     */
    public Properties getProperties() {
        Properties rawProperties = getRawProperties();
        return dereferenceProperties(rawProperties);
    }

    /**
     * @return the complete set of properties exactly as read from the files
     */
    public Properties getRawProperties() {
        return propertiesFactoryBean.getObject();
    }

    /**
     * Dereferences property placeholders in the given {@link Properties}
     * using a {@link PropertyPlaceholderHelper}.
     *
     * @param srcProperties a collection of name-value pairs
     * @return a new collection of properties. If {@code srcProperties}
     *   is {@code null}, returns null. If {@code srcProperties}
     *   is empty, returns a reference to the same object.
     */
    private Properties dereferenceProperties(Properties srcProperties) throws RuntimeException {
        if ( srcProperties == null ) {
            return null;
        }
        if ( srcProperties.isEmpty() ) {
            return srcProperties;
        }
        Properties parsedProperties = new Properties();
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        for (Map.Entry<Object, Object> propEntry : srcProperties.entrySet()) {
            String parsedPropValue = helper.replacePlaceholders((String) propEntry.getValue(), srcProperties);
            parsedProperties.setProperty((String) propEntry.getKey(), parsedPropValue);
        }
        return parsedProperties;
    }

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
     * Custom properties factory bean that tracks the properties loaded from each file separately,
     * allowing callers to determine which file a property originated from.
     */
    public static class SakaiPropertiesFactoryBean implements FactoryBean<Properties>, InitializingBean {
        public static final String XML_FILE_EXTENSION = ".xml";

        @Getter private Map<String, Properties> loadedProperties = new LinkedHashMap<>();
        @Setter private Resource[] locations;
        @Setter private boolean localOverride = false;
        @Setter private boolean ignoreResourceNotFound = false;
        @Setter private String fileEncoding;

        private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
        private Properties[] localProperties;
        private Properties singletonInstance;

        @Override
        public final boolean isSingleton() {
            return true;
        }

        @Override
        public final void afterPropertiesSet() throws IOException {
            this.singletonInstance = createInstance();
        }

        @Override
        public final Properties getObject() {
            return this.singletonInstance;
        }

        @Override
        public Class<Properties> getObjectType() {
            return Properties.class;
        }

        protected Properties createInstance() throws IOException {
            return mergeProperties();
        }

        public void setProperties(Properties properties) {
            this.localProperties = new Properties[] {properties};
        }

        public void setPropertiesArray(Properties[] propertiesArray) { // unused
            this.localProperties = propertiesArray;
        }

        public void addLocation(Resource location, String afterFilename) {
            int index = -1;
            if (afterFilename != null && this.locations != null) {
                for (int i = 0; i < this.locations.length; i++) {
                    if (afterFilename.equals(this.locations[i].getFilename())) {
                        index = i;
                        break;
                    }
                }
            }
            if (index >= 0) {
                this.locations = ArrayUtils.insert(index + 1, this.locations, location);
            } else {
                this.locations = ArrayUtils.add(this.locations, location);
            }
        }

        public void setLocation(Resource location) { // unused
            this.locations = new Resource[] {location};
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
                // Load properties from file afterward, to let those properties override.
                loadProperties(result);
            }

            log.info("Loaded a total of {} properties", result.size());
            return result;
        }

        /**
         * Load properties into the given instance.
         * 
         * @param props the Properties instance to load into
         * @throws java.io.IOException in the case of I/O errors
         * @see #setLocations
         */
        protected void loadProperties(Properties props) throws IOException {
            if (this.locations != null) {
                for (Resource location : this.locations) {
                    log.debug("Loading properties file from {}", location);
                    try (InputStream is = location.getInputStream()) {
                        Properties p = new Properties();
                        String fileName = location.getFilename();
                        if (fileName != null && fileName.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
                            this.propertiesPersister.loadFromXml(p, is);
                        } else {
                            if (this.fileEncoding != null) {
                                this.propertiesPersister.load(p, new InputStreamReader(is, this.fileEncoding));
                            } else {
                                this.propertiesPersister.load(p, is);
                            }
                        }
                        log.info("Loaded {} properties from file {}", p.size(), location);
                        loadedProperties.put(fileName, p);
                        props.putAll(p); // merge the properties
                    } catch (IOException ioe) {
                        if (this.ignoreResourceNotFound) {
                            log.warn("Could not load properties from {}, {}", location, ioe.toString());
                        } else {
                            throw ioe;
                        }
                    }
                }
            }
        }
    }
}
