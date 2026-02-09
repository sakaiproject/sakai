package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.tsugi.lti.objects.Groups;
import org.tsugi.lti.objects.Group;

/**
 * Unit tests for Groups class.
 */
public class GroupsTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithGroups() throws Exception {
        Groups groups = new Groups();
        List<Group> groupList = new ArrayList<>();
        
        Group group1 = new Group();
        group1.setId("group-1");
        group1.setTitle("Group 1");
        groupList.add(group1);
        
        Group group2 = new Group();
        group2.setId("group-2");
        group2.setTitle("Group 2");
        groupList.add(group2);
        
        groups.setGroup(groupList);
        
        String xml = XML_MAPPER.writeValueAsString(groups);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain group elements", xml.contains("<group>"));
        assertTrue("XML should contain group-1", xml.contains("group-1"));
        assertTrue("XML should contain group-2", xml.contains("group-2"));
    }
    
    @Test
    public void testDeserializeWithGroups() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<groups>\n" +
                     "<group>\n" +
                     "<id>group-1</id>\n" +
                     "<title>Group 1</title>\n" +
                     "</group>\n" +
                     "<group>\n" +
                     "<id>group-2</id>\n" +
                     "<title>Group 2</title>\n" +
                     "</group>\n" +
                     "</groups>";
        
        Groups groups = XML_MAPPER.readValue(xml, Groups.class);
        
        assertNotNull("Groups should not be null", groups);
        assertNotNull("Group list should not be null", groups.getGroup());
        assertEquals("Should have 2 groups", 2, groups.getGroup().size());
        assertEquals("First group id should match", "group-1", groups.getGroup().get(0).getId());
        assertEquals("Second group id should match", "group-2", groups.getGroup().get(1).getId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Groups original = new Groups();
        List<Group> groupList = new ArrayList<>();
        
        Group group = new Group();
        group.setId("test-group");
        group.setTitle("Test Title");
        groupList.add(group);
        
        original.setGroup(groupList);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Groups deserialized = XML_MAPPER.readValue(xml, Groups.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("Group list should not be null", deserialized.getGroup());
        assertEquals("Should have 1 group", 1, deserialized.getGroup().size());
        assertEquals("Group id should match", original.getGroup().get(0).getId(),
                     deserialized.getGroup().get(0).getId());
    }
}
