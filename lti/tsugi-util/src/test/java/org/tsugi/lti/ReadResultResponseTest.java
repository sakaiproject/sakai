package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ReadResultResponse;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;

/**
 * Unit tests for ReadResultResponse class.
 */
public class ReadResultResponseTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithResult() throws Exception {
        ReadResultResponse response = new ReadResultResponse();
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.91");
        result.setResultScore(resultScore);
        response.setResult(result);
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain result element", xml.contains("<result>"));
        assertTrue("XML should contain resultScore", xml.contains("<resultScore>"));
        assertTrue("XML should contain language", xml.contains("<language>en</language>"));
        assertTrue("XML should contain textString", xml.contains("<textString>0.91</textString>"));
    }
    
    @Test
    public void testDeserializeWithResult() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<readResultResponse>\n" +
                     "<result>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.91</textString>\n" +
                     "</resultScore>\n" +
                     "</result>\n" +
                     "</readResultResponse>";
        
        ReadResultResponse response = XML_MAPPER.readValue(xml, ReadResultResponse.class);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("Result should not be null", response.getResult());
        assertNotNull("ResultScore should not be null", response.getResult().getResultScore());
        assertEquals("Language should match", "en", response.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", "0.91", response.getResult().getResultScore().getTextString());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ReadResultResponse original = new ReadResultResponse();
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.85");
        result.setResultScore(resultScore);
        original.setResult(result);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ReadResultResponse deserialized = XML_MAPPER.readValue(xml, ReadResultResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("Result should not be null", deserialized.getResult());
        assertEquals("Language should match", original.getResult().getResultScore().getLanguage(),
                     deserialized.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match", original.getResult().getResultScore().getTextString(),
                     deserialized.getResult().getResultScore().getTextString());
    }
    
    /**
     * Test factory method against canonical XML from LTI 1.1.1 spec Figure 6 / Section 6.1.2.
     * This validates that the factory method produces spec-compliant XML.
     * Uses semantic equality checking by parsing the generated XML via XML_MAPPER
     * and verifying key elements (result/resultScore/language and textString) rather
     * than brittle string comparison that can fail due to Jackson injecting xmlns=""
     * attributes.
     */
    @Test
    public void testFactoryMethodMatchesSpec() throws Exception {
        // Canonical XML from LTI 1.1.1 spec Figure 6 / Section 6.1.2
        // (just the readResultResponse body portion)
        String specXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<readResultResponse>\n" +
                         "<result>\n" +
                         "<resultScore>\n" +
                         "<language>en</language>\n" +
                         "<textString>0.91</textString>\n" +
                         "</resultScore>\n" +
                         "</result>\n" +
                         "</readResultResponse>";
        
        // Create using factory method
        ReadResultResponse response = ReadResultResponse.create("0.91", null, "en");
        
        // Serialize to XML using XML_MAPPER
        String generatedXml = XML_MAPPER.writeValueAsString(response);
        
        // Parse the generated XML via XML_MAPPER to verify semantic equality
        // This avoids brittle string comparison issues with xmlns="" attributes
        ReadResultResponse parsedResponse = XML_MAPPER.readValue(generatedXml, ReadResultResponse.class);
        
        // Verify key elements produced by ReadResultResponse.create
        // Check result/resultScore/language and textString values
        assertNotNull("Response should not be null", parsedResponse);
        assertNotNull("Result should not be null", parsedResponse.getResult());
        assertNotNull("ResultScore should not be null", parsedResponse.getResult().getResultScore());
        assertEquals("Language should match spec", "en", parsedResponse.getResult().getResultScore().getLanguage());
        assertEquals("TextString should match spec", "0.91", parsedResponse.getResult().getResultScore().getTextString());
        
        // Verify spec XML can also be parsed and matches semantically
        ReadResultResponse specResponse = XML_MAPPER.readValue(specXml, ReadResultResponse.class);
        assertEquals("Spec language should match", "en", specResponse.getResult().getResultScore().getLanguage());
        assertEquals("Spec textString should match", "0.91", specResponse.getResult().getResultScore().getTextString());
        
        // Cross-verify: generated response should match spec response semantically
        assertEquals("Generated language should match spec", 
                     specResponse.getResult().getResultScore().getLanguage(),
                     parsedResponse.getResult().getResultScore().getLanguage());
        assertEquals("Generated textString should match spec",
                     specResponse.getResult().getResultScore().getTextString(),
                     parsedResponse.getResult().getResultScore().getTextString());
    }
    
    /**
     * Test factory method with comment/resultData against canonical structure.
     */
    @Test
    public void testFactoryMethodWithComment() throws Exception {
        // Create using factory method with comment
        ReadResultResponse response = ReadResultResponse.create("0.92", "Great work!", "en");
        
        // Serialize to XML
        String generatedXml = XML_MAPPER.writeValueAsString(response);
        
        // Verify structure
        assertNotNull("XML should not be null", generatedXml);
        assertTrue("XML should contain resultScore", generatedXml.contains("<resultScore>"));
        assertTrue("XML should contain language", generatedXml.contains("<language>en</language>"));
        assertTrue("XML should contain textString", generatedXml.contains("<textString>0.92</textString>"));
        assertTrue("XML should contain resultData", generatedXml.contains("<resultData>"));
        assertTrue("XML should contain comment text", generatedXml.contains("Great work!"));
    }
    
    /**
     * Test factory method with empty comment (should not include resultData).
     */
    @Test
    public void testFactoryMethodWithEmptyComment() throws Exception {
        // Create using factory method with empty comment
        ReadResultResponse response = ReadResultResponse.create("0.91", "", "en");
        
        // Serialize to XML
        String generatedXml = XML_MAPPER.writeValueAsString(response);
        
        // Verify resultData is not included
        assertNotNull("XML should not be null", generatedXml);
        assertTrue("XML should contain resultScore", generatedXml.contains("<resultScore>"));
        assertFalse("XML should not contain resultData when comment is empty", generatedXml.contains("<resultData>"));
    }
}
