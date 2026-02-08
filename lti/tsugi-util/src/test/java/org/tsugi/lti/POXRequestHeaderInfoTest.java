package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXRequestHeaderInfo;

/**
 * Unit tests for POXRequestHeaderInfo class.
 */
public class POXRequestHeaderInfoTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        
        String xml = XML_MAPPER.writeValueAsString(headerInfo);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain version", xml.contains("imsx_version"));
        assertTrue("XML should contain V1.0", xml.contains("V1.0"));
        assertTrue("XML should contain messageIdentifier", xml.contains("imsx_messageIdentifier"));
        assertTrue("XML should contain message ID", xml.contains("999999123"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXRequestHeaderInfo xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_version>V1.0</imsx_version>\n" +
                     "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
                     "</imsx_POXRequestHeaderInfo>";
        
        POXRequestHeaderInfo headerInfo = XML_MAPPER.readValue(xml, POXRequestHeaderInfo.class);
        
        assertNotNull("HeaderInfo should not be null", headerInfo);
        assertEquals("Version should match", "V1.0", headerInfo.getVersion());
        assertEquals("MessageIdentifier should match", "999999123", headerInfo.getMessageIdentifier());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXRequestHeaderInfo original = new POXRequestHeaderInfo();
        original.setVersion("V1.0");
        original.setMessageIdentifier("123456789");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXRequestHeaderInfo deserialized = XML_MAPPER.readValue(xml, POXRequestHeaderInfo.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Version should match", original.getVersion(), deserialized.getVersion());
        assertEquals("MessageIdentifier should match", original.getMessageIdentifier(), deserialized.getMessageIdentifier());
    }
}
