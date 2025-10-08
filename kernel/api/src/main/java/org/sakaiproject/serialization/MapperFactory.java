/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.serialization;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.Module;

import javax.xml.stream.XMLInputFactory;

/**
 * Utility class for creating and configuring Jackson ObjectMapper instances.
 * Uses builder pattern to allow flexible configuration of ObjectMapper properties.
 */
public final class MapperFactory {

    private MapperFactory() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Creates a default ObjectMapper with common settings.
     * - Ignores unknown properties
     * - Excludes null values
     * - Registers JavaTimeModule for handling Java 8 date/time
     * - Disables date timestamp serialization (uses ISO format instead)
     *
     * @return a pre-configured ObjectMapper
     */
    public static ObjectMapper createDefaultJsonMapper() {
        return jsonBuilder()
                .registerJdk8Module()
                .registerJavaTimeModule()
                .ignoreUnknownProperties()
                .excludeNulls()
                .disableDateTimestamps()
                .build();
    }

    /**
     * Creates a default XmlMapper with common settings.
     * - Ignores unknown properties
     * - Excludes null values
     * - Registers JavaTimeModule for handling Java 8 date/time
     * - Disables date timestamp serialization (uses ISO format instead)
     *
     * @return a pre-configured XmlMapper
     */
    public static XmlMapper createDefaultXmlMapper() {
        return xmlBuilder()
                .registerJavaTimeModule()
                .disableDateTimestamps()
                .ignoreUnknownProperties()
                .excludeNulls()
                .enableOutputCDataAsText()
                .disableNamespaceAware()
                .setMaxAttributeSize(32000)
                .enableRepairingNamespaces()
                .enablePrettyPrinting()
                .enableOutputXML11()
                .build();
    }

    /**
     * Creates a builder for configuring and building an ObjectMapper.
     *
     * @return a new ObjectMapperBuilder instance
     */
    public static JsonMapperBuilder jsonBuilder() {
        return new JsonMapperBuilder();
    }

    /**
     * Creates a builder for configuring and building an ObjectMapper.
     *
     * @return a new XmlMapperBuilder instance
     */
    public static XmlMapperBuilder xmlBuilder() {
        return new XmlMapperBuilder();
    }

    /**
     * Builder class for creating customized ObjectMapper instances.
     */
    public static class JsonMapperBuilder {
        private final ObjectMapper mapper;

        /**
         * Creates a new builder with a fresh ObjectMapper instance.
         */
        public JsonMapperBuilder() {
            this.mapper = new ObjectMapper();
        }

        /**
         * Builds the configured ObjectMapper.
         *
         * @return the configured ObjectMapper
         */
        public ObjectMapper build() {
            return mapper;
        }

        /**
         * Configures the mapper to ignore unknown properties during deserialization.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder ignoreUnknownProperties() {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return this;
        }

        /**
         * Configures the mapper to exclude null values during serialization.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder excludeNulls() {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return this;
        }

        /**
         * Configures the mapper to include empty values during serialization.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder includeEmpty() {
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return this;
        }

        /**
         * Registers JavaTimeModule for handling Java 8 date/time types.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder registerJavaTimeModule() {
            mapper.registerModule(new JavaTimeModule());
            return this;
        }

        /**
         * Registers Jdk8Module for handling Optional and other JDK8 types.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder registerJdk8Module() {
            try {
                Class<?> clazz = Class.forName("com.fasterxml.jackson.datatype.jdk8.Jdk8Module");
                Object module = clazz.getDeclaredConstructor().newInstance();
                if (module instanceof Module) {
                    mapper.registerModule((Module) module);
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // Module not on classpath in this test/runtime; skip registration
            } catch (ReflectiveOperationException e) {
                // Could not instantiate module; skip registration
            }
            return this;
        }

        /**
         * Disables serializing dates as timestamps (uses ISO format instead).
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder disableDateTimestamps() {
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return this;
        }

        /**
         * Enables pretty printing for serialized JSON.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder enablePrettyPrinting() {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return this;
        }

        /**
         * Enables failing on empty beans.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder failOnEmptyBeans() {
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
            return this;
        }

        /**
         * Disables failing on empty beans.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder disableFailOnEmptyBeans() {
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return this;
        }

        /**
         * Enables array serialization for single element.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder enableWrapRootValue() {
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            return this;
        }

        /**
         * Enables accepting single value as array.
         *
         * @return this builder for chaining
         */
        public JsonMapperBuilder enableAcceptSingleValueAsArray() {
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            return this;
        }
    }

    /**
     * Builder class for creating customized XmlMapper instances.
     */
    public static class XmlMapperBuilder {
        private XmlMapper xmlMapper;

        /**
         * Creates a new builder with a fresh XmlMapper instance using Woodstox.
         */
        public XmlMapperBuilder() {
            xmlMapper = new XmlMapper(WstxInputFactory.newInstance(), WstxOutputFactory.newInstance());
            XMLInputFactory xmlInputFactory = xmlMapper.getFactory().getXMLInputFactory();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            xmlMapper.getFactory().setXMLInputFactory(xmlInputFactory);
            xmlMapper.getFactory().rebuild().build();
        }

        /**
         * Builds the configured XmlMapper.
         *
         * @return the configured XmlMapper
         */
        public XmlMapper build() {
            return xmlMapper;
        }

        /**
         * Configures the mapper to ignore unknown properties during deserialization.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder ignoreUnknownProperties() {
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return this;
        }

        /**
         * Configures the mapper to exclude null values during serialization.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder excludeNulls() {
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return this;
        }

        /**
         * Configures the mapper to include empty values during serialization.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder includeEmpty() {
            xmlMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return this;
        }

        /**
         * Registers JavaTimeModule for handling Java 8 date/time types.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder registerJavaTimeModule() {
            xmlMapper.registerModule(new JavaTimeModule());
            return this;
        }

        /**
         * Disables serializing dates as timestamps (uses ISO format instead).
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder disableDateTimestamps() {
            xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return this;
        }

        /**
         * Enables pretty printing for serialized XML.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder enablePrettyPrinting() {
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return this;
        }

        /**
         * Enables failing on empty beans.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder failOnEmptyBeans() {
            xmlMapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return this;
        }

        /**
         * Disables failing on empty beans.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder disableFailOnEmptyBeans() {
            xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return this;
        }

        /**
         * Enables array serialization for single element.
         *
         * @return this builder for chaining
         */
        public XmlMapperBuilder enableArraysForSingleElements() {
            xmlMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            return this;
        }

        /**
         * Sets the maximum allowed size for XML attributes in bytes.
         * This setting is used to prevent potential denial of service attacks
         * through extremely large attribute values.
         *
         * @param maxAttributeSize The maximum attribute size in bytes
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder setMaxAttributeSize(int maxAttributeSize) {
            xmlMapper.getFactory().getXMLInputFactory().setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, maxAttributeSize);
            // Ensure XXE defenses remain in effect prior to rebuild
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlMapper.getFactory().rebuild().build();
            return this;
        }

        /**
         * Disables namespace awareness in the XML parser.
         * When namespace awareness is disabled, the parser will not process namespace
         * information in XML documents. This can improve performance but may cause issues
         * with documents that rely on namespace processing.
         *
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder disableNamespaceAware() {
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            // Ensure XXE defenses remain in effect prior to rebuild
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlMapper.getFactory().rebuild().build();
            return this;
        }

        /**
         * Enables namespace repairing in the XML writer.
         * When enabled, the XML writer will automatically generate namespace declarations
         * as needed and ensure proper namespace usage in the output XML document.
         * This is useful for ensuring well-formed XML output, especially when working
         * with namespace-aware XML documents.
         *
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder enableRepairingNamespaces() {
            xmlMapper.getFactory().getXMLOutputFactory().setProperty(WstxOutputFactory.IS_REPAIRING_NAMESPACES, true);
            xmlMapper.getFactory().rebuild().build();
            return this;
        }

        /**
         * Disables the automatic conversion of CDATA sections to regular text content.
         * When disabled, CDATA sections in the output XML will be preserved as proper
         * CDATA blocks rather than being output as regular text nodes.
         * This is useful when the exact format of the XML output is important.
         *
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder disableOutputCDataAsText() {
            xmlMapper.getFactory().getXMLOutputFactory().setProperty(WstxOutputProperties.P_OUTPUT_CDATA_AS_TEXT, false);
            xmlMapper.getFactory().rebuild().build();
            return this;
        }

        /**
         * Enables the automatic conversion of CDATA sections to regular text content.
         * When enabled, CDATA sections in the output XML will be written as regular text
         * nodes without CDATA markers. This is the default behavior in many XML parsers
         * and can simplify XML processing in some contexts.
         *
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder enableOutputCDataAsText() {
            xmlMapper.getFactory().getXMLOutputFactory().setProperty(WstxOutputProperties.P_OUTPUT_CDATA_AS_TEXT, true);
            xmlMapper.getFactory().rebuild().build();
            return this;
        }

        /**
         * Enables XML 1.1 output format.
         * When enabled, the XML writer will produce documents conforming to XML 1.1 specifications
         * rather than XML 1.0. XML 1.1 adds support for additional characters and has different
         * handling of control characters. This is useful when working with documents that contain
         * characters not allowed in XML 1.0.
         *
         * @return The builder instance for method chaining
         */
        public XmlMapperBuilder enableOutputXML11() {
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
            return this;
        }
    }
}
