package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;
import org.tsugi.lti.objects.ResultData;

/**
 * Unit tests for Result class.
 * 
 * Tests serialization and deserialization of all Result fields:
 * - sourcedId (optional, used in some contexts)
 * - resultScore (optional but commonly present)
 * - resultData (optional, commonly used for grade comments/feedback)
 * 
 * All fields are optional per the XSD and should be omitted from XML when null.
 */
public class ResultTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithResultScoreOnly() throws Exception {
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        
        String xml = XML_MAPPER.writeValueAsString(result);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain resultScore element", xml.contains("<resultScore>"));
        assertTrue("XML should contain language", xml.contains("<language>en</language>"));
        assertTrue("XML should contain textString", xml.contains("<textString>0.92</textString>"));
        assertFalse("XML should not contain sourcedId when null", xml.contains("<sourcedId>"));
        assertFalse("XML should not contain resultData when null", xml.contains("<resultData>"));
    }
    
    @Test
    public void testSerializeWithResultDataOnly() throws Exception {
        Result result = new Result();
        ResultData resultData = new ResultData();
        resultData.setText("Great work!");
        result.setResultData(resultData);
        
        String xml = XML_MAPPER.writeValueAsString(result);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain resultData element", xml.contains("<resultData>"));
        assertTrue("XML should contain text", xml.contains("Great work!"));
        assertFalse("XML should not contain sourcedId when null", xml.contains("<sourcedId>"));
        assertFalse("XML should not contain resultScore when null", xml.contains("<resultScore>"));
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        Result result = new Result();
        result.setSourcedId("3124567");
        
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        
        ResultData resultData = new ResultData();
        resultData.setText("Excellent work!");
        result.setResultData(resultData);
        
        String xml = XML_MAPPER.writeValueAsString(result);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>3124567</sourcedId>"));
        assertTrue("XML should contain resultScore", xml.contains("<resultScore>"));
        assertTrue("XML should contain resultData", xml.contains("<resultData>"));
    }
    
    @Test
    public void testSerializeNullFieldsOmitted() throws Exception {
        Result result = new Result();
        // All fields are null
        
        String xml = XML_MAPPER.writeValueAsString(result);
        
        assertNotNull("XML should not be null", xml);
        assertFalse("XML should not contain sourcedId when null", xml.contains("<sourcedId>"));
        assertFalse("XML should not contain resultScore when null", xml.contains("<resultScore>"));
        assertFalse("XML should not contain resultData when null", xml.contains("<resultData>"));
    }
    
    @Test
    public void testDeserializeWithResultScoreOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<result>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>\n" +
                     "</result>";
        
        Result result = XML_MAPPER.readValue(xml, Result.class);
        
        assertNotNull("Result should not be null", result);
        assertNull("SourcedId should be null", result.getSourcedId());
        assertNotNull("ResultScore should not be null", result.getResultScore());
        assertEquals("Language should match", "en", result.getResultScore().getLanguage());
        assertEquals("TextString should match", "0.92", result.getResultScore().getTextString());
        assertNull("ResultData should be null", result.getResultData());
    }
    
    @Test
    public void testDeserializeWithResultDataOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<result>\n" +
                     "<resultData>\n" +
                     "<text>Good job!</text>\n" +
                     "</resultData>\n" +
                     "</result>";
        
        Result result = XML_MAPPER.readValue(xml, Result.class);
        
        assertNotNull("Result should not be null", result);
        assertNull("SourcedId should be null", result.getSourcedId());
        assertNull("ResultScore should be null", result.getResultScore());
        assertNotNull("ResultData should not be null", result.getResultData());
        assertEquals("Text should match", "Good job!", result.getResultData().getText());
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<result>\n" +
                     "<sourcedId>3124567</sourcedId>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>\n" +
                     "<resultData>\n" +
                     "<text>Well done!</text>\n" +
                     "</resultData>\n" +
                     "</result>";
        
        Result result = XML_MAPPER.readValue(xml, Result.class);
        
        assertNotNull("Result should not be null", result);
        assertEquals("SourcedId should match", "3124567", result.getSourcedId());
        assertNotNull("ResultScore should not be null", result.getResultScore());
        assertEquals("Language should match", "en", result.getResultScore().getLanguage());
        assertEquals("TextString should match", "0.92", result.getResultScore().getTextString());
        assertNotNull("ResultData should not be null", result.getResultData());
        assertEquals("Text should match", "Well done!", result.getResultData().getText());
    }
    
    @Test
    public void testDeserializeEmptyElement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<result/>";
        
        Result result = XML_MAPPER.readValue(xml, Result.class);
        
        assertNotNull("Result should not be null", result);
        assertNull("SourcedId should be null", result.getSourcedId());
        assertNull("ResultScore should be null", result.getResultScore());
        assertNull("ResultData should be null", result.getResultData());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Result original = new Result();
        original.setSourcedId("12345");
        
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.85");
        original.setResultScore(resultScore);
        
        ResultData resultData = new ResultData();
        resultData.setText("Round trip test");
        original.setResultData(resultData);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Result deserialized = XML_MAPPER.readValue(xml, Result.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("SourcedId should match", original.getSourcedId(), deserialized.getSourcedId());
        assertNotNull("ResultScore should not be null", deserialized.getResultScore());
        assertEquals("Language should match", original.getResultScore().getLanguage(), deserialized.getResultScore().getLanguage());
        assertEquals("TextString should match", original.getResultScore().getTextString(), deserialized.getResultScore().getTextString());
        assertNotNull("ResultData should not be null", deserialized.getResultData());
        assertEquals("Text should match", original.getResultData().getText(), deserialized.getResultData().getText());
    }
}
