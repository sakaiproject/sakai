package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.StatusInfo;

/**
 * Unit tests for StatusInfo class.
 */
public class StatusInfoTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setCodeMinor("none");
        statusInfo.setSeverity("status");
        statusInfo.setDescription("Operation completed successfully");
        
        String xml = XML_MAPPER.writeValueAsString(statusInfo);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain codemajor", xml.contains("<codemajor>success</codemajor>"));
        assertTrue("XML should contain codeminor", xml.contains("<codeminor>none</codeminor>"));
        assertTrue("XML should contain severity", xml.contains("<severity>status</severity>"));
        assertTrue("XML should contain description", xml.contains("<description>Operation completed successfully</description>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<statusinfo>\n" +
                     "<codemajor>success</codemajor>\n" +
                     "<codeminor>none</codeminor>\n" +
                     "<severity>status</severity>\n" +
                     "<description>Operation completed successfully</description>\n" +
                     "</statusinfo>";
        
        StatusInfo statusInfo = XML_MAPPER.readValue(xml, StatusInfo.class);
        
        assertNotNull("StatusInfo should not be null", statusInfo);
        assertEquals("CodeMajor should match", "success", statusInfo.getCodeMajor());
        assertEquals("CodeMinor should match", "none", statusInfo.getCodeMinor());
        assertEquals("Severity should match", "status", statusInfo.getSeverity());
        assertEquals("Description should match", "Operation completed successfully", statusInfo.getDescription());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        StatusInfo original = new StatusInfo();
        original.setCodeMajor("failure");
        original.setCodeMinor("error");
        original.setSeverity("error");
        original.setDescription("Test error");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        StatusInfo deserialized = XML_MAPPER.readValue(xml, StatusInfo.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("CodeMajor should match", original.getCodeMajor(), deserialized.getCodeMajor());
        assertEquals("CodeMinor should match", original.getCodeMinor(), deserialized.getCodeMinor());
        assertEquals("Severity should match", original.getSeverity(), deserialized.getSeverity());
        assertEquals("Description should match", original.getDescription(), deserialized.getDescription());
    }
}
