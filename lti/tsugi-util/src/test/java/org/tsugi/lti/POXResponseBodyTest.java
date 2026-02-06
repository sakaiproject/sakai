package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXResponseBody;
import org.tsugi.lti.objects.ReplaceResultResponse;
import org.tsugi.lti.objects.ReadResultResponse;
import org.tsugi.lti.objects.DeleteResultResponse;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;

/**
 * Unit tests for POXResponseBody class.
 */
public class POXResponseBodyTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithReplaceResultResponse() throws Exception {
        POXResponseBody body = new POXResponseBody();
        body.setReplaceResultResponse(new ReplaceResultResponse());
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        // Empty response classes serialize as self-closing tags
        assertTrue("XML should contain replaceResultResponse", xml.contains("replaceResultResponse"));
        assertFalse("XML should not contain other response types", xml.contains("<readResultResponse>"));
    }
    
    @Test
    public void testSerializeWithReadResultResponse() throws Exception {
        POXResponseBody body = new POXResponseBody();
        ReadResultResponse response = new ReadResultResponse();
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.91");
        result.setResultScore(resultScore);
        response.setResult(result);
        body.setReadResultResponse(response);
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain readResultResponse", xml.contains("<readResultResponse>"));
    }
    
    @Test
    public void testSerializeWithDeleteResultResponse() throws Exception {
        POXResponseBody body = new POXResponseBody();
        body.setDeleteResultResponse(new DeleteResultResponse());
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        // Empty response classes serialize as self-closing tags
        assertTrue("XML should contain deleteResultResponse", xml.contains("deleteResultResponse"));
    }
    
    @Test
    public void testSerializeNullFieldsOmitted() throws Exception {
        POXResponseBody body = new POXResponseBody();
        // All fields are null
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertFalse("XML should not contain replaceResultResponse when null", xml.contains("<replaceResultResponse>"));
        assertFalse("XML should not contain readResultResponse when null", xml.contains("<readResultResponse>"));
        assertFalse("XML should not contain deleteResultResponse when null", xml.contains("<deleteResultResponse>"));
    }
    
    @Test
    public void testDeserializeWithReplaceResultResponse() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXBody xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<replaceResultResponse/>\n" +
                     "</imsx_POXBody>";
        
        POXResponseBody body = XML_MAPPER.readValue(xml, POXResponseBody.class);
        
        assertNotNull("Body should not be null", body);
        assertNotNull("ReplaceResultResponse should not be null", body.getReplaceResultResponse());
        assertNull("ReadResultResponse should be null", body.getReadResultResponse());
        assertNull("DeleteResultResponse should be null", body.getDeleteResultResponse());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXResponseBody original = new POXResponseBody();
        original.setReplaceResultResponse(new ReplaceResultResponse());
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXResponseBody deserialized = XML_MAPPER.readValue(xml, POXResponseBody.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("ReplaceResultResponse should not be null", deserialized.getReplaceResultResponse());
    }
}
