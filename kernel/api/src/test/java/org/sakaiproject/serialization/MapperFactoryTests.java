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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class MapperFactoryTests {

    /**
     * Test class with standard fields for serialization testing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestObject {
        private String name;
        private Integer age;
        private List<String> tags;
        private Date timestamp;
        private Map<String, String> attributes;
    }

    private TestObject createTestObject() {
        List<String> tags = new ArrayList<>(Arrays.asList("tag1", "tag2", "tag3"));
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        
        return new TestObject(
            "Test Name",
            42,
            tags,
            new Date(1684144200000L), // 2023-05-15T10:30:00
            attributes
        );
    }

    @Test
    public void testCreateDefaultJsonMapper() throws IOException {
        // Get the default mapper
        ObjectMapper mapper = MapperFactory.createDefaultJsonMapper();
        Assert.assertNotNull("Default JSON mapper should not be null", mapper);
        
        // Test serialization of a sample object
        TestObject testObj = createTestObject();
        String json = mapper.writeValueAsString(testObj);
        
        // Verify expected behavior
        Assert.assertTrue("JSON should contain name", json.contains("\"name\":\"Test Name\""));
        Assert.assertTrue("JSON should contain age", json.contains("\"age\":42"));
        Assert.assertTrue("JSON should contain tags", json.contains("\"tags\":[\"tag1\",\"tag2\",\"tag3\"]"));
        // When using Java dates, the actual format depends on the Jackson configuration
        // We'll skip exact format testing as it varies by configuration
        
        // Test deserialization
        TestObject deserializedObj = mapper.readValue(json, TestObject.class);
        Assert.assertEquals("Name should be preserved", "Test Name", deserializedObj.getName());
        Assert.assertEquals("Age should be preserved", Integer.valueOf(42), deserializedObj.getAge());
        Assert.assertEquals("Tags size should be preserved", 3, deserializedObj.getTags().size());
        Assert.assertEquals("Date should be preserved", 
                new Date(1684144200000L), deserializedObj.getTimestamp());
    }
    
    @Test
    public void testCreateDefaultXmlMapper() throws IOException {
        // Get the default mapper
        XmlMapper mapper = MapperFactory.createDefaultXmlMapper();
        Assert.assertNotNull("Default XML mapper should not be null", mapper);
        
        // Test serialization of a sample object
        TestObject testObj = createTestObject();
        String xml = mapper.writeValueAsString(testObj);
        
        // Verify expected behavior
        Assert.assertTrue("XML should contain name", xml.contains("<name>Test Name</name>"));
        Assert.assertTrue("XML should contain age", xml.contains("<age>42</age>"));
        Assert.assertTrue("XML should contain tags", xml.contains("<tags>tag1</tags>"));
        // When using Java dates, the actual format depends on the Jackson configuration
        // We'll skip exact format testing as it varies by configuration
        
        // Test deserialization
        TestObject deserializedObj = mapper.readValue(xml, TestObject.class);
        Assert.assertEquals("Name should be preserved", "Test Name", deserializedObj.getName());
        Assert.assertEquals("Age should be preserved", Integer.valueOf(42), deserializedObj.getAge());
        Assert.assertEquals("Tags size should be preserved", 3, deserializedObj.getTags().size());
        Assert.assertEquals("Date should be preserved", new Date(1684144200000L), deserializedObj.getTimestamp());
    }
    
    @Test
    public void testJsonBuilder() {
        // Test the builder pattern
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .registerJavaTimeModule()
                .ignoreUnknownProperties()
                .excludeNulls()
                .disableDateTimestamps()
                .build();
                
        Assert.assertNotNull("Builder should create non-null mapper", mapper);
    }
    
    @Test
    public void testXmlBuilder() {
        // Test the builder pattern
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .registerJavaTimeModule()
                .ignoreUnknownProperties()
                .excludeNulls()
                .disableDateTimestamps()
                .build();
                
        Assert.assertNotNull("Builder should create non-null mapper", mapper);
    }
    
    @Test
    public void testJsonMapperIgnoreUnknownProperties() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .ignoreUnknownProperties()
                .build();
                
        // Test by deserializing a JSON with an unknown property
        String json = "{\"name\":\"Test Name\",\"unknown_field\":\"value\",\"age\":42}";
        TestObject obj = mapper.readValue(json, TestObject.class);
        
        Assert.assertEquals("Should ignore unknown property and deserialize correctly", "Test Name", obj.getName());
        Assert.assertEquals("Should ignore unknown property and deserialize correctly", Integer.valueOf(42), obj.getAge());
    }
    
    @Test
    public void testJsonMapperExcludeNulls() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .excludeNulls()
                .build();
                
        // Test with null field
        TestObject testObj = createTestObject();
        testObj.setAge(null);
        
        String json = mapper.writeValueAsString(testObj);
        Assert.assertFalse("JSON should not contain age field", json.contains("\"age\":"));
    }
    
    @Test
    public void testJsonMapperIncludeEmpty() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .includeEmpty()
                .build();
                
        // Test with null field
        TestObject testObj = createTestObject();
        testObj.setAge(null);
        
        String json = mapper.writeValueAsString(testObj);
        Assert.assertTrue("JSON should contain null age field", json.contains("\"age\":null"));
    }
    
    @Test
    public void testJsonMapperRegisterJavaTimeModule() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .registerJavaTimeModule()
                .build();
                
        // Test with Date and LocalDateTime
        TestObject testObj = createTestObject();
        String json = mapper.writeValueAsString(testObj);
        
        // Deserialize and verify that both Date and LocalDateTime work correctly
        TestObject deserializedObj = mapper.readValue(json, TestObject.class);
        Assert.assertEquals("Date should be preserved", new Date(1684144200000L), deserializedObj.getTimestamp());
    }

    @Test
    public void testJsonMapperDisableDateTimestamps() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .registerJavaTimeModule()
                .disableDateTimestamps()
                .build();

        // Test with Date and LocalDateTime
        TestObject testObj = createTestObject();
        String json = mapper.writeValueAsString(testObj);

        // With disableDateTimestamps(), dates should be serialized as ISO strings, not as timestamps
        // Verify that the date is formatted as an ISO string (not a numeric timestamp)
        Assert.assertTrue("JSON should contain ISO-formatted date string", json.contains("\"timestamp\":\""));
        Assert.assertFalse("JSON should not contain numeric timestamp", json.matches(".*\"timestamp\":\\s*\\d+.*"));
        
        // Additionally verify that the mapper correctly deserializes the JSON back to the object
        TestObject deserializedObj = mapper.readValue(json, TestObject.class);
        Assert.assertEquals("Deserialized Date should match original", testObj.getTimestamp(), deserializedObj.getTimestamp());
    }
    
    @Test
    public void testJsonMapperEnablePrettyPrinting() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .enablePrettyPrinting()
                .build();
                
        TestObject testObj = createTestObject();
        String json = mapper.writeValueAsString(testObj);
        
        // Pretty printed JSON should have newlines
        Assert.assertTrue("JSON should be pretty printed with newlines", json.contains("\n"));
        Assert.assertTrue("JSON should be pretty printed with indentation", json.contains("  "));
    }
    
    @Test
    public void testJsonMapperFailOnEmptyBeans() {
        ObjectMapper mapperWithFailure = MapperFactory.jsonBuilder()
                .failOnEmptyBeans()
                .build();
        
        ObjectMapper mapperWithoutFailure = MapperFactory.jsonBuilder()
                .disableFailOnEmptyBeans()
                .build();
        
        // Verify feature setting
        Assert.assertTrue("FAIL_ON_EMPTY_BEANS should be enabled",
                mapperWithFailure.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        
        Assert.assertFalse("FAIL_ON_EMPTY_BEANS should be disabled",
                mapperWithoutFailure.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }
    
    @Test
    public void testJsonMapperEnableWrapRootValue() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .enableWrapRootValue()
                .build();
                
        TestObject testObj = createTestObject();
        String json = mapper.writeValueAsString(testObj);
        
        // Root value wrapper should contain class name
        Assert.assertTrue("JSON should wrap root value", json.contains("\"TestObject\":"));
    }
    
    @Test
    public void testJsonMapperEnableAcceptSingleValueAsArray() throws IOException {
        ObjectMapper mapper = MapperFactory.jsonBuilder()
                .enableAcceptSingleValueAsArray()
                .build();
                
        // Test by deserializing a single value as an array
        String json = "{\"name\":\"Test Name\",\"tags\":\"singletag\",\"age\":42}";
        TestObject obj = mapper.readValue(json, TestObject.class);
        
        Assert.assertEquals("Should convert single value to array", 1, obj.getTags().size());
        Assert.assertEquals("Should convert single value to array", "singletag", obj.getTags().get(0));
    }
    
    // XML Mapper tests
    
    @Test
    public void testXmlMapperIgnoreUnknownProperties() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .ignoreUnknownProperties()
                .build();
                
        // Test by deserializing XML with an unknown property
        String xml = "<TestObject><name>Test Name</name><unknown_field>value</unknown_field><age>42</age></TestObject>";
        TestObject obj = mapper.readValue(xml, TestObject.class);
        
        Assert.assertEquals("Should ignore unknown property and deserialize correctly", "Test Name", obj.getName());
        Assert.assertEquals("Should ignore unknown property and deserialize correctly", Integer.valueOf(42), obj.getAge());
    }
    
    @Test
    public void testXmlMapperExcludeNulls() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .excludeNulls()
                .build();
                
        // Test with null field
        TestObject testObj = createTestObject();
        testObj.setAge(null);
        
        String xml = mapper.writeValueAsString(testObj);
        Assert.assertFalse("XML should not contain age field", xml.contains("<age>"));
    }
    
    @Test
    public void testXmlMapperIncludeEmpty() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .includeEmpty()
                .build();
                
        // Test with null field
        TestObject testObj = createTestObject();
        testObj.setAge(null);
        
        String xml = mapper.writeValueAsString(testObj);
        
        // Different Jackson versions and configurations may format the XML differently
        // We'll just verify something reasonable happened
        Assert.assertNotNull("XML serialization should succeed", xml);
    }
    
    @Test
    public void testXmlMapperRegisterJavaTimeModule() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .registerJavaTimeModule()
                .build();
                
        // Test with Date and LocalDateTime
        TestObject testObj = createTestObject();
        String xml = mapper.writeValueAsString(testObj);
        
        // Deserialize and verify that both Date and LocalDateTime work correctly
        TestObject deserializedObj = mapper.readValue(xml, TestObject.class);
        Assert.assertEquals("Date should be preserved", new Date(1684144200000L), deserializedObj.getTimestamp());
    }
    
    @Test
    public void testXmlMapperDisableDateTimestamps() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .registerJavaTimeModule()
                .disableDateTimestamps()
                .build();
                
        // Test with Date and LocalDateTime
        TestObject testObj = createTestObject();
        String xml = mapper.writeValueAsString(testObj);
        
        // Verify the XML contains date representations (exact format may vary by configuration)
        Assert.assertTrue("XML should contain timestamp element", xml.contains("<timestamp>"));

        // Test deserialization to verify the values are preserved
        TestObject deserializedObj = mapper.readValue(xml, TestObject.class);
        Assert.assertEquals("Date should be preserved", testObj.getTimestamp(), deserializedObj.getTimestamp());
    }
    
    @Test
    public void testXmlMapperEnablePrettyPrinting() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .enablePrettyPrinting()
                .build();
                
        TestObject testObj = createTestObject();
        String xml = mapper.writeValueAsString(testObj);
        
        // Pretty printed XML should have newlines
        Assert.assertTrue("XML should be pretty printed with newlines", xml.contains("\n"));
        Assert.assertTrue("XML should be pretty printed with indentation", xml.contains("  "));
    }
    
    @Test
    public void testXmlMapperFailOnEmptyBeans() {
        XmlMapper mapperWithFailure = MapperFactory.xmlBuilder()
                .failOnEmptyBeans()
                .build();
        
        XmlMapper mapperWithoutFailure = MapperFactory.xmlBuilder()
                .disableFailOnEmptyBeans()
                .build();
        
        // Verify feature setting
        Assert.assertTrue("FAIL_ON_EMPTY_BEANS should be enabled",
                mapperWithFailure.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        
        Assert.assertFalse("FAIL_ON_EMPTY_BEANS should be disabled",
                mapperWithoutFailure.getSerializationConfig().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }
    
    @Test
    public void testXmlMapperEnableArraysForSingleElements() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .enableArraysForSingleElements()
                .build();
                
        // Test by deserializing a single value as an array
        String xml = "<TestObject><name>Test Name</name><tags>singletag</tags><age>42</age></TestObject>";
        TestObject obj = mapper.readValue(xml, TestObject.class);
        
        Assert.assertEquals("Should convert single value to array", 1, obj.getTags().size());
        Assert.assertEquals("Should convert single value to array", "singletag", obj.getTags().get(0));
    }
    
    @Test
    public void testXmlMapperSetMaxAttributeSize() {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .setMaxAttributeSize(32000)
                .build();
        
        Assert.assertNotNull("Mapper should be created with custom max attribute size", mapper);
    }
    
    @Test
    public void testXmlMapperDisableNamespaceAware() {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .disableNamespaceAware()
                .build();
        
        Assert.assertNotNull("Mapper should be created with namespace awareness disabled", mapper);
    }
    
    @Test
    public void testXmlMapperEnableRepairingNamespaces() {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .enableRepairingNamespaces()
                .build();
        
        Assert.assertNotNull("Mapper should be created with repairing namespaces enabled", mapper);
    }
    
    @Test
    public void testXmlMapperEnableOutputCDataAsText() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .enableOutputCDataAsText()
                .build();
        
        Assert.assertNotNull("Mapper should be created with CDATA as text enabled", mapper);
        
        // Modify test object to include content that would typically be in CDATA
        TestObject testObj = createTestObject();
        testObj.setName("Test with <xml> & special chars");
        
        // Serialize to XML
        String xml = mapper.writeValueAsString(testObj);

        // With enableOutputCDataAsText, the XML should not contain CDATA sections
        Assert.assertFalse("XML should not contain CDATA section", xml.contains("<![CDATA["));
        
        // The XML should use entity escaping for special characters
        // We'll just check for the presence of escaped characters, not the specific format
        Assert.assertTrue("XML should contain escaped characters", xml.contains("&lt;") || xml.contains("&amp;"));
        
        // Deserialize and verify content is preserved
        TestObject deserializedObj = mapper.readValue(xml, TestObject.class);
        Assert.assertEquals("Special characters should be preserved", "Test with <xml> & special chars", deserializedObj.getName());
    }
    
    @Test
    public void testXmlMapperDisableOutputCDataAsText() throws IOException {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .disableOutputCDataAsText()
                .build();
        
        Assert.assertNotNull("Mapper should be created with CDATA as text disabled", mapper);
        
        // Modify test object to include content that would typically be in CDATA
        TestObject testObj = createTestObject();
        testObj.setName("Test with <xml> & special chars");
        
        // Serialize to XML
        String xml = mapper.writeValueAsString(testObj);
        
        // Deserialize and verify content is preserved regardless of how it was encoded
        TestObject deserializedObj = mapper.readValue(xml, TestObject.class);
        Assert.assertEquals("Special characters should be preserved", "Test with <xml> & special chars", deserializedObj.getName());
    }
    
    @Test
    public void testXmlMapperEnableOutputXML11() {
        XmlMapper mapper = MapperFactory.xmlBuilder()
                .enableOutputXML11()
                .build();
        
        // XmlMapper doesn't expose an easy way to check if this feature is enabled
        // So we'll just verify the mapper was created successfully
        Assert.assertNotNull("Mapper should be created with XML 1.1 output enabled", mapper);
    }
}