package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.Role;
import org.tsugi.lti.objects.TimeFrame;
import org.tsugi.lti.objects.AdminPeriod;

/**
 * Unit tests for Role class.
 */
public class RoleTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        Role role = new Role();
        role.setRoleType("Instructor");
        role.setSubRole("TeachingAssistant");
        role.setStatus("Active");
        role.setDateTime("2024-01-01T00:00:00Z");
        role.setDataSource("SIS");
        
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setBegin("2024-01-01");
        timeFrame.setEnd("2024-12-31");
        role.setTimeFrame(timeFrame);
        
        String xml = XML_MAPPER.writeValueAsString(role);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain roleType", xml.contains("<roleType>Instructor</roleType>"));
        assertTrue("XML should contain subRole", xml.contains("<subRole>TeachingAssistant</subRole>"));
        assertTrue("XML should contain status", xml.contains("<status>Active</status>"));
        assertTrue("XML should contain timeFrame", xml.contains("<timeFrame>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<role>\n" +
                     "<roleType>Instructor</roleType>\n" +
                     "<subRole>TeachingAssistant</subRole>\n" +
                     "<status>Active</status>\n" +
                     "<dateTime>2024-01-01T00:00:00Z</dateTime>\n" +
                     "<dataSource>SIS</dataSource>\n" +
                     "<timeFrame>\n" +
                     "<begin>2024-01-01</begin>\n" +
                     "<end>2024-12-31</end>\n" +
                     "</timeFrame>\n" +
                     "</role>";
        
        Role role = XML_MAPPER.readValue(xml, Role.class);
        
        assertNotNull("Role should not be null", role);
        assertEquals("RoleType should match", "Instructor", role.getRoleType());
        assertEquals("SubRole should match", "TeachingAssistant", role.getSubRole());
        assertEquals("Status should match", "Active", role.getStatus());
        assertEquals("DateTime should match", "2024-01-01T00:00:00Z", role.getDateTime());
        assertEquals("DataSource should match", "SIS", role.getDataSource());
        assertNotNull("TimeFrame should not be null", role.getTimeFrame());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Role original = new Role();
        original.setRoleType("Learner");
        original.setStatus("Active");
        
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setBegin("2024-01-01");
        timeFrame.setEnd("2024-12-31");
        original.setTimeFrame(timeFrame);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Role deserialized = XML_MAPPER.readValue(xml, Role.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("RoleType should match", original.getRoleType(), deserialized.getRoleType());
        assertEquals("Status should match", original.getStatus(), deserialized.getStatus());
        assertNotNull("TimeFrame should not be null", deserialized.getTimeFrame());
    }
}
