package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.TimeFrame;
import org.tsugi.lti.objects.AdminPeriod;

/**
 * Unit tests for TimeFrame class.
 */
public class TimeFrameTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setBegin("2024-01-01T00:00:00Z");
        timeFrame.setEnd("2024-12-31T23:59:59Z");
        timeFrame.setRestrict(true);
        
        AdminPeriod adminPeriod = new AdminPeriod();
        adminPeriod.setLanguage("en");
        adminPeriod.setTextString("Fall 2024");
        timeFrame.setAdminPeriod(adminPeriod);
        
        String xml = XML_MAPPER.writeValueAsString(timeFrame);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain begin", xml.contains("<begin>2024-01-01T00:00:00Z</begin>"));
        assertTrue("XML should contain end", xml.contains("<end>2024-12-31T23:59:59Z</end>"));
        assertTrue("XML should contain restrict", xml.contains("<restrict>true</restrict>"));
        assertTrue("XML should contain adminPeriod", xml.contains("<adminPeriod>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<timeFrame>\n" +
                     "<begin>2024-01-01T00:00:00Z</begin>\n" +
                     "<end>2024-12-31T23:59:59Z</end>\n" +
                     "<restrict>true</restrict>\n" +
                     "<adminPeriod>\n" +
                     "<language>en</language>\n" +
                     "<textString>Fall 2024</textString>\n" +
                     "</adminPeriod>\n" +
                     "</timeFrame>";
        
        TimeFrame timeFrame = XML_MAPPER.readValue(xml, TimeFrame.class);
        
        assertNotNull("TimeFrame should not be null", timeFrame);
        assertEquals("Begin should match", "2024-01-01T00:00:00Z", timeFrame.getBegin());
        assertEquals("End should match", "2024-12-31T23:59:59Z", timeFrame.getEnd());
        assertEquals("Restrict should match", Boolean.TRUE, timeFrame.getRestrict());
        assertNotNull("AdminPeriod should not be null", timeFrame.getAdminPeriod());
        assertEquals("AdminPeriod language should match", "en", timeFrame.getAdminPeriod().getLanguage());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        TimeFrame original = new TimeFrame();
        original.setBegin("2024-01-01");
        original.setEnd("2024-12-31");
        original.setRestrict(false);
        
        AdminPeriod adminPeriod = new AdminPeriod();
        adminPeriod.setLanguage("en");
        adminPeriod.setTextString("Test Period");
        original.setAdminPeriod(adminPeriod);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        TimeFrame deserialized = XML_MAPPER.readValue(xml, TimeFrame.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Begin should match", original.getBegin(), deserialized.getBegin());
        assertEquals("End should match", original.getEnd(), deserialized.getEnd());
        assertEquals("Restrict should match", original.getRestrict(), deserialized.getRestrict());
        assertNotNull("AdminPeriod should not be null", deserialized.getAdminPeriod());
    }
}
