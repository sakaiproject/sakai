package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXRequestHeader;
import org.tsugi.lti.objects.POXRequestHeaderInfo;

/**
 * Unit tests for POXRequestHeader class.
 */
public class POXRequestHeaderTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithHeaderInfo() throws Exception {
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        header.setRequestHeaderInfo(headerInfo);
        
        String xml = XML_MAPPER.writeValueAsString(header);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain requestHeaderInfo", xml.contains("imsx_POXRequestHeaderInfo"));
        assertTrue("XML should contain version", xml.contains("V1.0"));
        assertTrue("XML should contain messageIdentifier", xml.contains("999999123"));
    }
    
    @Test
    public void testDeserializeWithHeaderInfo() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXHeader xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_POXRequestHeaderInfo>\n" +
                     "<imsx_version>V1.0</imsx_version>\n" +
                     "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
                     "</imsx_POXRequestHeaderInfo>\n" +
                     "</imsx_POXHeader>";
        
        POXRequestHeader header = XML_MAPPER.readValue(xml, POXRequestHeader.class);
        
        assertNotNull("Header should not be null", header);
        assertNotNull("RequestHeaderInfo should not be null", header.getRequestHeaderInfo());
        assertEquals("Version should match", "V1.0", header.getRequestHeaderInfo().getVersion());
        assertEquals("MessageIdentifier should match", "999999123", header.getRequestHeaderInfo().getMessageIdentifier());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXRequestHeader original = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("123456789");
        original.setRequestHeaderInfo(headerInfo);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXRequestHeader deserialized = XML_MAPPER.readValue(xml, POXRequestHeader.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("RequestHeaderInfo should not be null", deserialized.getRequestHeaderInfo());
        assertEquals("Version should match", original.getRequestHeaderInfo().getVersion(),
                     deserialized.getRequestHeaderInfo().getVersion());
        assertEquals("MessageIdentifier should match", original.getRequestHeaderInfo().getMessageIdentifier(),
                     deserialized.getRequestHeaderInfo().getMessageIdentifier());
    }
}
