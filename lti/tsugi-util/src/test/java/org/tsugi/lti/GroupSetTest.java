package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.GroupSet;

/**
 * Unit tests for GroupSet class.
 */
public class GroupSetTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        GroupSet groupSet = new GroupSet();
        groupSet.setId("group-set-123");
        groupSet.setTitle("Test Group Set");
        
        String xml = XML_MAPPER.writeValueAsString(groupSet);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain id", xml.contains("<id>group-set-123</id>"));
        assertTrue("XML should contain title", xml.contains("<title>Test Group Set</title>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<set>\n" +
                     "<id>group-set-123</id>\n" +
                     "<title>Test Group Set</title>\n" +
                     "</set>";
        
        GroupSet groupSet = XML_MAPPER.readValue(xml, GroupSet.class);
        
        assertNotNull("GroupSet should not be null", groupSet);
        assertEquals("Id should match", "group-set-123", groupSet.getId());
        assertEquals("Title should match", "Test Group Set", groupSet.getTitle());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        GroupSet original = new GroupSet();
        original.setId("test-id");
        original.setTitle("Test Title");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        GroupSet deserialized = XML_MAPPER.readValue(xml, GroupSet.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Id should match", original.getId(), deserialized.getId());
        assertEquals("Title should match", original.getTitle(), deserialized.getTitle());
    }
}
