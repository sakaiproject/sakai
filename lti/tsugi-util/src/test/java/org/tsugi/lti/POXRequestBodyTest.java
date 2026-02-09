package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXRequestBody;
import org.tsugi.lti.objects.ReplaceResultRequest;
import org.tsugi.lti.objects.ReadResultRequest;
import org.tsugi.lti.objects.DeleteResultRequest;
import org.tsugi.lti.objects.ResultRecord;
import org.tsugi.lti.objects.SourcedGUID;

/**
 * Unit tests for POXRequestBody class.
 */
public class POXRequestBodyTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithReplaceResultRequest() throws Exception {
        POXRequestBody body = new POXRequestBody();
        ReplaceResultRequest request = new ReplaceResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        request.setResultRecord(resultRecord);
        body.setReplaceResultRequest(request);
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain replaceResultRequest", xml.contains("<replaceResultRequest>"));
        assertFalse("XML should not contain other request types", xml.contains("<readResultRequest>"));
    }
    
    @Test
    public void testSerializeWithReadResultRequest() throws Exception {
        POXRequestBody body = new POXRequestBody();
        ReadResultRequest request = new ReadResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        request.setResultRecord(resultRecord);
        body.setReadResultRequest(request);
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain readResultRequest", xml.contains("<readResultRequest>"));
    }
    
    @Test
    public void testSerializeWithDeleteResultRequest() throws Exception {
        POXRequestBody body = new POXRequestBody();
        DeleteResultRequest request = new DeleteResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        request.setResultRecord(resultRecord);
        body.setDeleteResultRequest(request);
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain deleteResultRequest", xml.contains("<deleteResultRequest>"));
    }
    
    @Test
    public void testSerializeNullFieldsOmitted() throws Exception {
        POXRequestBody body = new POXRequestBody();
        // All fields are null
        
        String xml = XML_MAPPER.writeValueAsString(body);
        
        assertNotNull("XML should not be null", xml);
        assertFalse("XML should not contain replaceResultRequest when null", xml.contains("<replaceResultRequest>"));
        assertFalse("XML should not contain readResultRequest when null", xml.contains("<readResultRequest>"));
        assertFalse("XML should not contain deleteResultRequest when null", xml.contains("<deleteResultRequest>"));
    }
    
    @Test
    public void testDeserializeWithReplaceResultRequest() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_POXBody xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<replaceResultRequest>\n" +
                     "<resultRecord>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "</sourcedGUID>\n" +
                     "</resultRecord>\n" +
                     "</replaceResultRequest>\n" +
                     "</imsx_POXBody>";
        
        POXRequestBody body = XML_MAPPER.readValue(xml, POXRequestBody.class);
        
        assertNotNull("Body should not be null", body);
        assertNotNull("ReplaceResultRequest should not be null", body.getReplaceResultRequest());
        assertNull("ReadResultRequest should be null", body.getReadResultRequest());
        assertNull("DeleteResultRequest should be null", body.getDeleteResultRequest());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXRequestBody original = new POXRequestBody();
        ReadResultRequest request = new ReadResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("test-course-123");
        resultRecord.setSourcedGUID(sourcedGUID);
        request.setResultRecord(resultRecord);
        original.setReadResultRequest(request);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXRequestBody deserialized = XML_MAPPER.readValue(xml, POXRequestBody.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("ReadResultRequest should not be null", deserialized.getReadResultRequest());
        assertEquals("SourcedId should match", original.getReadResultRequest().getResultRecord().getSourcedGUID().getSourcedId(),
                     deserialized.getReadResultRequest().getResultRecord().getSourcedGUID().getSourcedId());
    }
}
