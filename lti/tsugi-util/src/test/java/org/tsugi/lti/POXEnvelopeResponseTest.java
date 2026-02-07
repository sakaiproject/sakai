package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXEnvelopeResponse;
import org.tsugi.lti.objects.POXResponseHeader;
import org.tsugi.lti.objects.POXResponseHeaderInfo;
import org.tsugi.lti.objects.POXStatusInfo;
import org.tsugi.lti.objects.POXResponseBody;
import org.tsugi.lti.objects.ReplaceResultResponse;

/**
 * Unit tests for POXEnvelopeResponse class.
 */
public class POXEnvelopeResponseTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithHeaderAndBody() throws Exception {
        POXEnvelopeResponse envelope = new POXEnvelopeResponse();
        
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
        envelope.setPoxHeader(header);
        
        POXResponseBody body = new POXResponseBody();
        body.setReplaceResultResponse(new ReplaceResultResponse());
        envelope.setPoxBody(body);
        
        String xml = XML_MAPPER.writeValueAsString(envelope);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain POXHeader", xml.contains("imsx_POXHeader"));
        assertTrue("XML should contain POXBody", xml.contains("imsx_POXBody"));
        assertTrue("XML should contain success", xml.contains("success"));
        assertTrue("XML should contain replaceResultResponse", xml.contains("replaceResultResponse"));
    }
    
    @Test
    public void testDeserializeWithHeaderAndBody() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_POXHeader>\n" +
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
                     "</imsx_POXHeader>\n" +
                     "<imsx_POXBody>\n" +
                     "<replaceResultResponse/>\n" +
                     "</imsx_POXBody>\n" +
                     "</imsx_POXEnvelopeResponse>";
        
        POXEnvelopeResponse envelope = XML_MAPPER.readValue(xml, POXEnvelopeResponse.class);
        
        assertNotNull("Envelope should not be null", envelope);
        assertNotNull("POXHeader should not be null", envelope.getPoxHeader());
        assertNotNull("POXBody should not be null", envelope.getPoxBody());
        assertEquals("Version should match", "V1.0", envelope.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("CodeMajor should match", "success", envelope.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor());
        assertNotNull("ReplaceResultResponse should not be null", envelope.getPoxBody().getReplaceResultResponse());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXEnvelopeResponse original = new POXEnvelopeResponse();
        
        POXResponseHeader header = new POXResponseHeader();
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
        header.setResponseHeaderInfo(headerInfo);
        original.setPoxHeader(header);
        
        POXResponseBody body = new POXResponseBody();
        body.setReplaceResultResponse(new ReplaceResultResponse());
        original.setPoxBody(body);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXEnvelopeResponse deserialized = XML_MAPPER.readValue(xml, POXEnvelopeResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("POXHeader should not be null", deserialized.getPoxHeader());
        assertNotNull("POXBody should not be null", deserialized.getPoxBody());
        assertEquals("Version should match", original.getPoxHeader().getResponseHeaderInfo().getVersion(),
                     deserialized.getPoxHeader().getResponseHeaderInfo().getVersion());
    }
}
