package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXResponseHeaderInfo;
import org.tsugi.lti.objects.POXStatusInfo;

/**
 * Unit tests for POXResponseHeaderInfo class.
 */
public class POXResponseHeaderInfoTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
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
        
        String xml = XML_MAPPER.writeValueAsString(headerInfo);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain version", xml.contains("imsx_version"));
        assertTrue("XML should contain V1.0", xml.contains("V1.0"));
        assertTrue("XML should contain messageIdentifier", xml.contains("imsx_messageIdentifier"));
        assertTrue("XML should contain statusInfo", xml.contains("imsx_statusInfo"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXResponseHeaderInfo xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_version>V1.0</imsx_version>\n" +
                     "<imsx_messageIdentifier>999999125</imsx_messageIdentifier>\n" +
                     "<imsx_statusInfo>\n" +
                     "<imsx_codeMajor>success</imsx_codeMajor>\n" +
                     "<imsx_severity>status</imsx_severity>\n" +
                     "<imsx_description>Operation completed</imsx_description>\n" +
                     "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
                     "<imsx_operationRefIdentifier>replaceResult</imsx_operationRefIdentifier>\n" +
                     "</imsx_statusInfo>\n" +
                     "</imsx_POXResponseHeaderInfo>";
        
        POXResponseHeaderInfo headerInfo = XML_MAPPER.readValue(xml, POXResponseHeaderInfo.class);
        
        assertNotNull("HeaderInfo should not be null", headerInfo);
        assertEquals("Version should match", "V1.0", headerInfo.getVersion());
        assertEquals("MessageIdentifier should match", "999999125", headerInfo.getMessageIdentifier());
        assertNotNull("StatusInfo should not be null", headerInfo.getStatusInfo());
        assertEquals("CodeMajor should match", "success", headerInfo.getStatusInfo().getCodeMajor());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXResponseHeaderInfo original = new POXResponseHeaderInfo();
        original.setVersion("V1.0");
        original.setMessageIdentifier("123456789");
        
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        statusInfo.setDescription("Test");
        statusInfo.setMessageRefIdentifier("123");
        statusInfo.setOperationRefIdentifier("testOp");
        original.setStatusInfo(statusInfo);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXResponseHeaderInfo deserialized = XML_MAPPER.readValue(xml, POXResponseHeaderInfo.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Version should match", original.getVersion(), deserialized.getVersion());
        assertEquals("MessageIdentifier should match", original.getMessageIdentifier(), deserialized.getMessageIdentifier());
        assertNotNull("StatusInfo should not be null", deserialized.getStatusInfo());
        assertEquals("CodeMajor should match", original.getStatusInfo().getCodeMajor(),
                     deserialized.getStatusInfo().getCodeMajor());
    }
}
