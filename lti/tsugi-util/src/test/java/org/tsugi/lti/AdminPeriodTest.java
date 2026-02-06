package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.AdminPeriod;

/**
 * Unit tests for AdminPeriod class.
 */
public class AdminPeriodTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        AdminPeriod adminPeriod = new AdminPeriod();
        adminPeriod.setLanguage("en");
        adminPeriod.setTextString("Fall 2024");
        
        String xml = XML_MAPPER.writeValueAsString(adminPeriod);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain language", xml.contains("<language>en</language>"));
        assertTrue("XML should contain textString", xml.contains("<textString>Fall 2024</textString>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<adminPeriod>\n" +
                     "<language>en</language>\n" +
                     "<textString>Fall 2024</textString>\n" +
                     "</adminPeriod>";
        
        AdminPeriod adminPeriod = XML_MAPPER.readValue(xml, AdminPeriod.class);
        
        assertNotNull("AdminPeriod should not be null", adminPeriod);
        assertEquals("Language should match", "en", adminPeriod.getLanguage());
        assertEquals("TextString should match", "Fall 2024", adminPeriod.getTextString());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        AdminPeriod original = new AdminPeriod();
        original.setLanguage("en");
        original.setTextString("Spring 2025");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        AdminPeriod deserialized = XML_MAPPER.readValue(xml, AdminPeriod.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Language should match", original.getLanguage(), deserialized.getLanguage());
        assertEquals("TextString should match", original.getTextString(), deserialized.getTextString());
    }
}
