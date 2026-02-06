package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.DeleteResultResponse;

/**
 * Unit tests for DeleteResultResponse class.
 * Note: This is an empty response element per the LTI 1.1.1 spec.
 */
public class DeleteResultResponseTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerialize() throws Exception {
        DeleteResultResponse response = new DeleteResultResponse();
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain deleteResultResponse", xml.contains("deleteResultResponse"));
    }
    
    @Test
    public void testDeserialize() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<deleteResultResponse/>";
        
        DeleteResultResponse response = XML_MAPPER.readValue(xml, DeleteResultResponse.class);
        
        assertNotNull("Response should not be null", response);
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        DeleteResultResponse original = new DeleteResultResponse();
        
        String xml = XML_MAPPER.writeValueAsString(original);
        DeleteResultResponse deserialized = XML_MAPPER.readValue(xml, DeleteResultResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
    }
}
