package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ReplaceResultRequest;
import org.tsugi.lti.objects.ResultRecord;
import org.tsugi.lti.objects.SourcedGUID;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;

/**
 * Unit tests for ReplaceResultRequest class.
 */
public class ReplaceResultRequestTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithResultRecord() throws Exception {
        ReplaceResultRequest request = new ReplaceResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        resultRecord.setResult(result);
        
        request.setResultRecord(resultRecord);
        
        String xml = XML_MAPPER.writeValueAsString(request);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain resultRecord", xml.contains("<resultRecord>"));
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>3124567</sourcedId>"));
        assertTrue("XML should contain result", xml.contains("<result>"));
        assertTrue("XML should contain resultScore", xml.contains("<resultScore>"));
    }
    
    @Test
    public void testDeserializeWithResultRecord() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<replaceResultRequest>\n" +
                     "<resultRecord>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "</sourcedGUID>\n" +
                     "<result>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>\n" +
                     "</result>\n" +
                     "</resultRecord>\n" +
                     "</replaceResultRequest>";
        
        ReplaceResultRequest request = XML_MAPPER.readValue(xml, ReplaceResultRequest.class);
        
        assertNotNull("Request should not be null", request);
        assertNotNull("ResultRecord should not be null", request.getResultRecord());
        assertNotNull("SourcedGUID should not be null", request.getResultRecord().getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", request.getResultRecord().getSourcedGUID().getSourcedId());
        assertNotNull("Result should not be null", request.getResultRecord().getResult());
        assertNotNull("ResultScore should not be null", request.getResultRecord().getResult().getResultScore());
        assertEquals("Language should match", "en", request.getResultRecord().getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", "0.92", request.getResultRecord().getResult().getResultScore().getTextString());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ReplaceResultRequest original = new ReplaceResultRequest();
        ResultRecord resultRecord = new ResultRecord();
        
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("12345");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.85");
        result.setResultScore(resultScore);
        resultRecord.setResult(result);
        
        original.setResultRecord(resultRecord);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ReplaceResultRequest deserialized = XML_MAPPER.readValue(xml, ReplaceResultRequest.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("ResultRecord should not be null", deserialized.getResultRecord());
        assertEquals("SourcedId should match", original.getResultRecord().getSourcedGUID().getSourcedId(), 
                     deserialized.getResultRecord().getSourcedGUID().getSourcedId());
        assertNotNull("Result should not be null", deserialized.getResultRecord().getResult());
        assertEquals("TextString should match", original.getResultRecord().getResult().getResultScore().getTextString(),
                     deserialized.getResultRecord().getResult().getResultScore().getTextString());
    }
}
