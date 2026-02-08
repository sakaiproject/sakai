package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.Group;
import org.tsugi.lti.objects.GroupSet;

/**
 * Unit tests for Group class.
 */
public class GroupTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        Group group = new Group();
        group.setId("group-123");
        group.setTitle("Test Group");
        
        GroupSet groupSet = new GroupSet();
        groupSet.setId("set-123");
        groupSet.setTitle("Parent Set");
        group.setSet(groupSet);
        
        String xml = XML_MAPPER.writeValueAsString(group);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain id", xml.contains("<id>group-123</id>"));
        assertTrue("XML should contain title", xml.contains("<title>Test Group</title>"));
        assertTrue("XML should contain set", xml.contains("<set>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<group>\n" +
                     "<id>group-123</id>\n" +
                     "<title>Test Group</title>\n" +
                     "<set>\n" +
                     "<id>set-123</id>\n" +
                     "<title>Parent Set</title>\n" +
                     "</set>\n" +
                     "</group>";
        
        Group group = XML_MAPPER.readValue(xml, Group.class);
        
        assertNotNull("Group should not be null", group);
        assertEquals("Id should match", "group-123", group.getId());
        assertEquals("Title should match", "Test Group", group.getTitle());
        assertNotNull("Set should not be null", group.getSet());
        assertEquals("Set id should match", "set-123", group.getSet().getId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Group original = new Group();
        original.setId("test-group");
        original.setTitle("Test Title");
        
        GroupSet groupSet = new GroupSet();
        groupSet.setId("test-set");
        groupSet.setTitle("Set Title");
        original.setSet(groupSet);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Group deserialized = XML_MAPPER.readValue(xml, Group.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Id should match", original.getId(), deserialized.getId());
        assertEquals("Title should match", original.getTitle(), deserialized.getTitle());
        assertNotNull("Set should not be null", deserialized.getSet());
        assertEquals("Set id should match", original.getSet().getId(), deserialized.getSet().getId());
        assertEquals("Set title should match", original.getSet().getTitle(), deserialized.getSet().getTitle());
    }
}
