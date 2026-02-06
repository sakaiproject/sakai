package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ReadMembershipRequest;

/**
 * Unit tests for ReadMembershipRequest class.
 */
public class ReadMembershipRequestTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithSourcedId() throws Exception {
        ReadMembershipRequest request = new ReadMembershipRequest();
        request.setSourcedId("123course456");
        
        String xml = XML_MAPPER.writeValueAsString(request);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>123course456</sourcedId>"));
    }
    
    @Test
    public void testDeserializeWithSourcedId() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<readMembershipRequest>\n" +
                     "<sourcedId>123course456</sourcedId>\n" +
                     "</readMembershipRequest>";
        
        ReadMembershipRequest request = XML_MAPPER.readValue(xml, ReadMembershipRequest.class);
        
        assertNotNull("Request should not be null", request);
        assertEquals("SourcedId should match", "123course456", request.getSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ReadMembershipRequest original = new ReadMembershipRequest();
        original.setSourcedId("test-course-123");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ReadMembershipRequest deserialized = XML_MAPPER.readValue(xml, ReadMembershipRequest.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("SourcedId should match", original.getSourcedId(), deserialized.getSourcedId());
    }
}
