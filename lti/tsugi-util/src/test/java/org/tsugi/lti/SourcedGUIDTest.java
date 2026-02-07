package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.SourcedGUID;

/**
 * Unit tests for SourcedGUID class.
 * 
 * Tests serialization and deserialization of SourcedGUID field:
 * - sourcedId (required identifier)
 */
public class SourcedGUIDTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithSourcedId() throws Exception {
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        
        String xml = XML_MAPPER.writeValueAsString(sourcedGUID);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedId element", xml.contains("<sourcedId>3124567</sourcedId>"));
    }
    
    @Test
    public void testSerializeWithNullSourcedId() throws Exception {
        SourcedGUID sourcedGUID = new SourcedGUID();
        // sourcedId is null
        
        String xml = XML_MAPPER.writeValueAsString(sourcedGUID);
        
        assertNotNull("XML should not be null", xml);
        // sourcedId without @JsonInclude won't be serialized when null
        assertFalse("XML should not contain sourcedId element when null", xml.contains("<sourcedId>"));
    }
    
    @Test
    public void testDeserializeWithSourcedId() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "</sourcedGUID>";
        
        SourcedGUID sourcedGUID = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertNotNull("SourcedGUID should not be null", sourcedGUID);
        assertEquals("SourcedId should match", "3124567", sourcedGUID.getSourcedId());
    }
    
    @Test
    public void testDeserializeEmptyElement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<sourcedGUID/>";
        
        SourcedGUID sourcedGUID = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertNotNull("SourcedGUID should not be null", sourcedGUID);
        assertNull("SourcedId should be null", sourcedGUID.getSourcedId());
    }
    
    @Test
    public void testDeserializeWithEmptySourcedId() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId></sourcedId>\n" +
                     "</sourcedGUID>";
        
        SourcedGUID sourcedGUID = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertNotNull("SourcedGUID should not be null", sourcedGUID);
        // Empty string should be preserved
        assertEquals("SourcedId should be empty string", "", sourcedGUID.getSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        SourcedGUID original = new SourcedGUID();
        original.setSourcedId("12345");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        SourcedGUID deserialized = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("SourcedId should match", original.getSourcedId(), deserialized.getSourcedId());
    }
    
    @Test
    public void testNumericSourcedId() throws Exception {
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        
        String xml = XML_MAPPER.writeValueAsString(sourcedGUID);
        SourcedGUID deserialized = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertEquals("Numeric sourcedId should be preserved", "3124567", deserialized.getSourcedId());
    }
    
    @Test
    public void testAlphanumericSourcedId() throws Exception {
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("user-123-abc");
        
        String xml = XML_MAPPER.writeValueAsString(sourcedGUID);
        SourcedGUID deserialized = XML_MAPPER.readValue(xml, SourcedGUID.class);
        
        assertEquals("Alphanumeric sourcedId should be preserved", "user-123-abc", deserialized.getSourcedId());
    }
}
