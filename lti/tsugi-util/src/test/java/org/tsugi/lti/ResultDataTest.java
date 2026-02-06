package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ResultData;

/**
 * Unit tests for ResultData class.
 * 
 * Tests serialization and deserialization of all ResultData fields:
 * - text (commonly used for grade comments/feedback)
 * - url (optional URL field)
 * - ltiLaunchUrl (optional LTI launch URL field)
 * 
 * All fields are optional per the XSD and should be omitted from XML when null.
 */
public class ResultDataTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeTextOnly() throws Exception {
        ResultData resultData = new ResultData();
        resultData.setText("Great work on this assignment!");
        
        String xml = XML_MAPPER.writeValueAsString(resultData);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain text element", xml.contains("<text>"));
        assertTrue("XML should contain text value", xml.contains("Great work on this assignment!"));
        assertFalse("XML should not contain url element", xml.contains("<url>"));
        assertFalse("XML should not contain ltiLaunchUrl element", xml.contains("<ltiLaunchUrl>"));
    }
    
    @Test
    public void testSerializeUrlOnly() throws Exception {
        ResultData resultData = new ResultData();
        resultData.setUrl("https://example.com/feedback");
        
        String xml = XML_MAPPER.writeValueAsString(resultData);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain url element", xml.contains("<url>"));
        assertTrue("XML should contain url value", xml.contains("https://example.com/feedback"));
        assertFalse("XML should not contain text element", xml.contains("<text>"));
        assertFalse("XML should not contain ltiLaunchUrl element", xml.contains("<ltiLaunchUrl>"));
    }
    
    @Test
    public void testSerializeLtiLaunchUrlOnly() throws Exception {
        ResultData resultData = new ResultData();
        resultData.setLtiLaunchUrl("https://tool.example.com/launch?param=value");
        
        String xml = XML_MAPPER.writeValueAsString(resultData);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain ltiLaunchUrl element", xml.contains("<ltiLaunchUrl>"));
        assertTrue("XML should contain ltiLaunchUrl value", xml.contains("https://tool.example.com/launch?param=value"));
        assertFalse("XML should not contain text element", xml.contains("<text>"));
        assertFalse("XML should not contain url element", xml.contains("<url>"));
    }
    
    @Test
    public void testSerializeAllFields() throws Exception {
        ResultData resultData = new ResultData();
        resultData.setText("Excellent work!");
        resultData.setUrl("https://example.com/details");
        resultData.setLtiLaunchUrl("https://tool.example.com/launch");
        
        String xml = XML_MAPPER.writeValueAsString(resultData);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain text element", xml.contains("<text>"));
        assertTrue("XML should contain text value", xml.contains("Excellent work!"));
        assertTrue("XML should contain url element", xml.contains("<url>"));
        assertTrue("XML should contain url value", xml.contains("https://example.com/details"));
        assertTrue("XML should contain ltiLaunchUrl element", xml.contains("<ltiLaunchUrl>"));
        assertTrue("XML should contain ltiLaunchUrl value", xml.contains("https://tool.example.com/launch"));
    }
    
    @Test
    public void testSerializeNullFieldsOmitted() throws Exception {
        ResultData resultData = new ResultData();
        // All fields are null
        
        String xml = XML_MAPPER.writeValueAsString(resultData);
        
        assertNotNull("XML should not be null", xml);
        // With @JsonInclude(NON_NULL), null fields should be omitted
        // The XML should just be the root element (empty or self-closing)
        assertFalse("XML should not contain text element when null", xml.contains("<text>"));
        assertFalse("XML should not contain url element when null", xml.contains("<url>"));
        assertFalse("XML should not contain ltiLaunchUrl element when null", xml.contains("<ltiLaunchUrl>"));
    }
    
    @Test
    public void testDeserializeTextOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultData>\n" +
                     "<text>Good job!</text>\n" +
                     "</resultData>";
        
        ResultData resultData = XML_MAPPER.readValue(xml, ResultData.class);
        
        assertNotNull("ResultData should not be null", resultData);
        assertEquals("Text should match", "Good job!", resultData.getText());
        assertNull("URL should be null", resultData.getUrl());
        assertNull("LtiLaunchUrl should be null", resultData.getLtiLaunchUrl());
    }
    
    @Test
    public void testDeserializeUrlOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultData>\n" +
                     "<url>https://example.com/resource</url>\n" +
                     "</resultData>";
        
        ResultData resultData = XML_MAPPER.readValue(xml, ResultData.class);
        
        assertNotNull("ResultData should not be null", resultData);
        assertNull("Text should be null", resultData.getText());
        assertEquals("URL should match", "https://example.com/resource", resultData.getUrl());
        assertNull("LtiLaunchUrl should be null", resultData.getLtiLaunchUrl());
    }
    
    @Test
    public void testDeserializeLtiLaunchUrlOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultData>\n" +
                     "<ltiLaunchUrl>https://tool.example.com/launch?key=value</ltiLaunchUrl>\n" +
                     "</resultData>";
        
        ResultData resultData = XML_MAPPER.readValue(xml, ResultData.class);
        
        assertNotNull("ResultData should not be null", resultData);
        assertNull("Text should be null", resultData.getText());
        assertNull("URL should be null", resultData.getUrl());
        assertEquals("LtiLaunchUrl should match", "https://tool.example.com/launch?key=value", resultData.getLtiLaunchUrl());
    }
    
    @Test
    public void testDeserializeAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultData>\n" +
                     "<text>Well done!</text>\n" +
                     "<url>https://example.com/feedback</url>\n" +
                     "<ltiLaunchUrl>https://tool.example.com/launch</ltiLaunchUrl>\n" +
                     "</resultData>";
        
        ResultData resultData = XML_MAPPER.readValue(xml, ResultData.class);
        
        assertNotNull("ResultData should not be null", resultData);
        assertEquals("Text should match", "Well done!", resultData.getText());
        assertEquals("URL should match", "https://example.com/feedback", resultData.getUrl());
        assertEquals("LtiLaunchUrl should match", "https://tool.example.com/launch", resultData.getLtiLaunchUrl());
    }
    
    @Test
    public void testDeserializeEmptyElement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultData/>";
        
        ResultData resultData = XML_MAPPER.readValue(xml, ResultData.class);
        
        assertNotNull("ResultData should not be null", resultData);
        assertNull("Text should be null", resultData.getText());
        assertNull("URL should be null", resultData.getUrl());
        assertNull("LtiLaunchUrl should be null", resultData.getLtiLaunchUrl());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        // Create object with all fields
        ResultData original = new ResultData();
        original.setText("Round trip test");
        original.setUrl("https://example.com/test");
        original.setLtiLaunchUrl("https://tool.example.com/test");
        
        // Serialize to XML
        String xml = XML_MAPPER.writeValueAsString(original);
        
        // Deserialize back to object
        ResultData deserialized = XML_MAPPER.readValue(xml, ResultData.class);
        
        // Verify all fields match
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Text should match after round trip", original.getText(), deserialized.getText());
        assertEquals("URL should match after round trip", original.getUrl(), deserialized.getUrl());
        assertEquals("LtiLaunchUrl should match after round trip", original.getLtiLaunchUrl(), deserialized.getLtiLaunchUrl());
    }
    
    @Test
    public void testRoundTripWithPartialFields() throws Exception {
        // Create object with only text field
        ResultData original = new ResultData();
        original.setText("Partial fields test");
        
        // Serialize to XML
        String xml = XML_MAPPER.writeValueAsString(original);
        
        // Deserialize back to object
        ResultData deserialized = XML_MAPPER.readValue(xml, ResultData.class);
        
        // Verify text matches and other fields are null
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Text should match after round trip", original.getText(), deserialized.getText());
        assertNull("URL should be null", deserialized.getUrl());
        assertNull("LtiLaunchUrl should be null", deserialized.getLtiLaunchUrl());
    }
}
