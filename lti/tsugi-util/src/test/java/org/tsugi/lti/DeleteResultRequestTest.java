package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.DeleteResultRequest;
import org.tsugi.lti.objects.ResultRecord;
import org.tsugi.lti.objects.SourcedGUID;

/**
 * Unit tests for DeleteResultRequest class.
 */
public class DeleteResultRequestTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithResultRecord() throws Exception {
        DeleteResultRequest request = new DeleteResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        request.setResultRecord(resultRecord);
        
        String xml = XML_MAPPER.writeValueAsString(request);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain resultRecord", xml.contains("<resultRecord>"));
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>3124567</sourcedId>"));
    }
    
    @Test
    public void testDeserializeWithResultRecord() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<deleteResultRequest>\n" +
                     "<resultRecord>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "</sourcedGUID>\n" +
                     "</resultRecord>\n" +
                     "</deleteResultRequest>";
        
        DeleteResultRequest request = XML_MAPPER.readValue(xml, DeleteResultRequest.class);
        
        assertNotNull("Request should not be null", request);
        assertNotNull("ResultRecord should not be null", request.getResultRecord());
        assertNotNull("SourcedGUID should not be null", request.getResultRecord().getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", request.getResultRecord().getSourcedGUID().getSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        DeleteResultRequest original = new DeleteResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("12345");
        resultRecord.setSourcedGUID(sourcedGUID);
        original.setResultRecord(resultRecord);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        DeleteResultRequest deserialized = XML_MAPPER.readValue(xml, DeleteResultRequest.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("ResultRecord should not be null", deserialized.getResultRecord());
        assertEquals("SourcedId should match", original.getResultRecord().getSourcedGUID().getSourcedId(), 
                     deserialized.getResultRecord().getSourcedGUID().getSourcedId());
    }
}
