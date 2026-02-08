package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXResponseHeader;
import org.tsugi.lti.objects.POXResponseHeaderInfo;
import org.tsugi.lti.objects.POXStatusInfo;

/**
 * Unit tests for POXResponseHeader class.
 */
public class POXResponseHeaderTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithHeaderInfo() throws Exception {
        POXResponseHeader header = new POXResponseHeader();
        POXResponseHeaderInfo headerInfo = new POXResponseHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999125");
        
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        statusInfo.setDescription("Operation completed");
        statusInfo.setMessageRefIdentifier("999999123");
        statusInfo.setOperationRefIdentifier("replaceResult");
        headerInfo.setStatusInfo(statusInfo);
        
        header.setResponseHeaderInfo(headerInfo);
        
        String xml = XML_MAPPER.writeValueAsString(header);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain responseHeaderInfo", xml.contains("imsx_POXResponseHeaderInfo"));
        assertTrue("XML should contain version", xml.contains("V1.0"));
        assertTrue("XML should contain statusInfo", xml.contains("imsx_statusInfo"));
    }
    
    @Test
    public void testDeserializeWithHeaderInfo() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXHeader xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_POXResponseHeaderInfo>\n" +
                     "<imsx_version>V1.0</imsx_version>\n" +
                     "<imsx_messageIdentifier>999999125</imsx_messageIdentifier>\n" +
                     "<imsx_statusInfo>\n" +
                     "<imsx_codeMajor>success</imsx_codeMajor>\n" +
                     "<imsx_severity>status</imsx_severity>\n" +
                     "<imsx_description>Operation completed</imsx_description>\n" +
                     "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
                     "<imsx_operationRefIdentifier>replaceResult</imsx_operationRefIdentifier>\n" +
                     "</imsx_statusInfo>\n" +
                     "</imsx_POXResponseHeaderInfo>\n" +
                     "</imsx_POXHeader>";
        
        POXResponseHeader header = XML_MAPPER.readValue(xml, POXResponseHeader.class);
        
        assertNotNull("Header should not be null", header);
        assertNotNull("ResponseHeaderInfo should not be null", header.getResponseHeaderInfo());
        assertEquals("Version should match", "V1.0", header.getResponseHeaderInfo().getVersion());
        assertNotNull("StatusInfo should not be null", header.getResponseHeaderInfo().getStatusInfo());
        assertEquals("CodeMajor should match", "success", header.getResponseHeaderInfo().getStatusInfo().getCodeMajor());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXResponseHeader original = new POXResponseHeader();
        POXResponseHeaderInfo headerInfo = new POXResponseHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("123456789");
        
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        statusInfo.setDescription("Test");
        statusInfo.setMessageRefIdentifier("123");
        statusInfo.setOperationRefIdentifier("testOp");
        headerInfo.setStatusInfo(statusInfo);
        
        original.setResponseHeaderInfo(headerInfo);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXResponseHeader deserialized = XML_MAPPER.readValue(xml, POXResponseHeader.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("ResponseHeaderInfo should not be null", deserialized.getResponseHeaderInfo());
        assertEquals("Version should match", original.getResponseHeaderInfo().getVersion(),
                     deserialized.getResponseHeaderInfo().getVersion());
        assertEquals("MessageIdentifier should match", original.getResponseHeaderInfo().getMessageIdentifier(),
                     deserialized.getResponseHeaderInfo().getMessageIdentifier());
        assertEquals("CodeMajor should match", original.getResponseHeaderInfo().getStatusInfo().getCodeMajor(),
                     deserialized.getResponseHeaderInfo().getStatusInfo().getCodeMajor());
    }
}
