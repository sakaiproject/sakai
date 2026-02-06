package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.objects.POXRequestHeader;
import org.tsugi.lti.objects.POXRequestHeaderInfo;
import org.tsugi.lti.objects.POXRequestBody;
import org.tsugi.lti.objects.ReadMembershipRequest;

/**
 * Unit tests for POXEnvelopeRequest class.
 */
public class POXEnvelopeRequestTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithHeaderAndBody() throws Exception {
        POXEnvelopeRequest envelope = new POXEnvelopeRequest();
        
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        header.setRequestHeaderInfo(headerInfo);
        envelope.setPoxHeader(header);
        
        POXRequestBody body = new POXRequestBody();
        ReadMembershipRequest request = new ReadMembershipRequest();
        request.setSourcedId("123course456");
        body.setReadMembershipRequest(request);
        envelope.setPoxBody(body);
        
        String xml = XML_MAPPER.writeValueAsString(envelope);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain POXHeader", xml.contains("imsx_POXHeader"));
        assertTrue("XML should contain POXBody", xml.contains("imsx_POXBody"));
        assertTrue("XML should contain version", xml.contains("V1.0"));
        assertTrue("XML should contain sourcedId", xml.contains("123course456"));
    }
    
    @Test
    public void testDeserializeWithHeaderAndBody() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXEnvelopeRequest xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_POXHeader>\n" +
                     "<imsx_POXRequestHeaderInfo>\n" +
                     "<imsx_version>V1.0</imsx_version>\n" +
                     "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
                     "</imsx_POXRequestHeaderInfo>\n" +
                     "</imsx_POXHeader>\n" +
                     "<imsx_POXBody>\n" +
                     "<readMembershipRequest>\n" +
                     "<sourcedId>123course456</sourcedId>\n" +
                     "</readMembershipRequest>\n" +
                     "</imsx_POXBody>\n" +
                     "</imsx_POXEnvelopeRequest>";
        
        POXEnvelopeRequest envelope = XML_MAPPER.readValue(xml, POXEnvelopeRequest.class);
        
        assertNotNull("Envelope should not be null", envelope);
        assertNotNull("POXHeader should not be null", envelope.getPoxHeader());
        assertNotNull("POXBody should not be null", envelope.getPoxBody());
        assertEquals("Version should match", "V1.0", envelope.getPoxHeader().getRequestHeaderInfo().getVersion());
        assertNotNull("ReadMembershipRequest should not be null", envelope.getPoxBody().getReadMembershipRequest());
        assertEquals("SourcedId should match", "123course456", envelope.getPoxBody().getReadMembershipRequest().getSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXEnvelopeRequest original = new POXEnvelopeRequest();
        
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("123456789");
        header.setRequestHeaderInfo(headerInfo);
        original.setPoxHeader(header);
        
        POXRequestBody body = new POXRequestBody();
        ReadMembershipRequest request = new ReadMembershipRequest();
        request.setSourcedId("test-course");
        body.setReadMembershipRequest(request);
        original.setPoxBody(body);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXEnvelopeRequest deserialized = XML_MAPPER.readValue(xml, POXEnvelopeRequest.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("POXHeader should not be null", deserialized.getPoxHeader());
        assertNotNull("POXBody should not be null", deserialized.getPoxBody());
        assertEquals("Version should match", original.getPoxHeader().getRequestHeaderInfo().getVersion(),
                     deserialized.getPoxHeader().getRequestHeaderInfo().getVersion());
    }
}
