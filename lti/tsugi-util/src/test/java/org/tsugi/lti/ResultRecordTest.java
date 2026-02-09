package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ResultRecord;
import org.tsugi.lti.objects.SourcedGUID;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;

/**
 * Unit tests for ResultRecord class.
 * 
 * Tests serialization and deserialization of ResultRecord fields:
 * - sourcedGUID (optional, contains sourcedId)
 * - result (optional, contains resultScore and/or resultData)
 * 
 * Both fields are optional per the XSD and should be omitted from XML when null.
 */
public class ResultRecordTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithSourcedGUIDOnly() throws Exception {
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        String xml = XML_MAPPER.writeValueAsString(resultRecord);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedGUID element", xml.contains("<sourcedGUID>"));
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>3124567</sourcedId>"));
        assertFalse("XML should not contain result when null", xml.contains("<result>"));
    }
    
    @Test
    public void testSerializeWithResultOnly() throws Exception {
        ResultRecord resultRecord = new ResultRecord();
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        resultRecord.setResult(result);
        
        String xml = XML_MAPPER.writeValueAsString(resultRecord);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain result element", xml.contains("<result>"));
        assertTrue("XML should contain resultScore", xml.contains("<resultScore>"));
        assertFalse("XML should not contain sourcedGUID when null", xml.contains("<sourcedGUID>"));
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
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
        
        String xml = XML_MAPPER.writeValueAsString(resultRecord);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedGUID", xml.contains("<sourcedGUID>"));
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>3124567</sourcedId>"));
        assertTrue("XML should contain result", xml.contains("<result>"));
        assertTrue("XML should contain resultScore", xml.contains("<resultScore>"));
    }
    
    @Test
    public void testSerializeNullFieldsOmitted() throws Exception {
        ResultRecord resultRecord = new ResultRecord();
        // All fields are null
        
        String xml = XML_MAPPER.writeValueAsString(resultRecord);
        
        assertNotNull("XML should not be null", xml);
        assertFalse("XML should not contain sourcedGUID when null", xml.contains("<sourcedGUID>"));
        assertFalse("XML should not contain result when null", xml.contains("<result>"));
    }
    
    @Test
    public void testDeserializeWithSourcedGUIDOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultRecord>\n" +
                     "<sourcedGUID>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "</sourcedGUID>\n" +
                     "</resultRecord>";
        
        ResultRecord resultRecord = XML_MAPPER.readValue(xml, ResultRecord.class);
        
        assertNotNull("ResultRecord should not be null", resultRecord);
        assertNotNull("SourcedGUID should not be null", resultRecord.getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", resultRecord.getSourcedGUID().getSourcedId());
        assertNull("Result should be null", resultRecord.getResult());
    }
    
    @Test
    public void testDeserializeWithResultOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultRecord>\n" +
                     "<result>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>\n" +
                     "</result>\n" +
                     "</resultRecord>";
        
        ResultRecord resultRecord = XML_MAPPER.readValue(xml, ResultRecord.class);
        
        assertNotNull("ResultRecord should not be null", resultRecord);
        assertNull("SourcedGUID should be null", resultRecord.getSourcedGUID());
        assertNotNull("Result should not be null", resultRecord.getResult());
        assertNotNull("ResultScore should not be null", resultRecord.getResult().getResultScore());
        assertEquals("Language should match", "en", resultRecord.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", "0.92", resultRecord.getResult().getResultScore().getTextString());
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
                     "</resultRecord>";
        
        ResultRecord resultRecord = XML_MAPPER.readValue(xml, ResultRecord.class);
        
        assertNotNull("ResultRecord should not be null", resultRecord);
        assertNotNull("SourcedGUID should not be null", resultRecord.getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", resultRecord.getSourcedGUID().getSourcedId());
        assertNotNull("Result should not be null", resultRecord.getResult());
        assertNotNull("ResultScore should not be null", resultRecord.getResult().getResultScore());
        assertEquals("Language should match", "en", resultRecord.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", "0.92", resultRecord.getResult().getResultScore().getTextString());
    }
    
    @Test
    public void testDeserializeEmptyElement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultRecord/>";
        
        ResultRecord resultRecord = XML_MAPPER.readValue(xml, ResultRecord.class);
        
        assertNotNull("ResultRecord should not be null", resultRecord);
        assertNull("SourcedGUID should be null", resultRecord.getSourcedGUID());
        assertNull("Result should be null", resultRecord.getResult());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ResultRecord original = new ResultRecord();
        
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("12345");
        original.setSourcedGUID(sourcedGUID);
        
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.85");
        result.setResultScore(resultScore);
        original.setResult(result);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ResultRecord deserialized = XML_MAPPER.readValue(xml, ResultRecord.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("SourcedGUID should not be null", deserialized.getSourcedGUID());
        assertEquals("SourcedId should match", original.getSourcedGUID().getSourcedId(), deserialized.getSourcedGUID().getSourcedId());
        assertNotNull("Result should not be null", deserialized.getResult());
        assertNotNull("ResultScore should not be null", deserialized.getResult().getResultScore());
        assertEquals("Language should match", original.getResult().getResultScore().getLanguage(), deserialized.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", original.getResult().getResultScore().getTextString(), deserialized.getResult().getResultScore().getTextString());
    }
}
