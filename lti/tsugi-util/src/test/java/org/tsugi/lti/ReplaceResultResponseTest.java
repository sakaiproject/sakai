package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ReplaceResultResponse;

/**
 * Unit tests for ReplaceResultResponse class.
 * Note: This is an empty response element per the LTI 1.1.1 spec.
 */
public class ReplaceResultResponseTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerialize() throws Exception {
        ReplaceResultResponse response = new ReplaceResultResponse();
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain replaceResultResponse", xml.contains("replaceResultResponse"));
    }
    
    @Test
    public void testDeserialize() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<replaceResultResponse/>";
        
        ReplaceResultResponse response = XML_MAPPER.readValue(xml, ReplaceResultResponse.class);
        
        assertNotNull("Response should not be null", response);
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ReplaceResultResponse original = new ReplaceResultResponse();
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ReplaceResultResponse deserialized = XML_MAPPER.readValue(xml, ReplaceResultResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
    }
}
