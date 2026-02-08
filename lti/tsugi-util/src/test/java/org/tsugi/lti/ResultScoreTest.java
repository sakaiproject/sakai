package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ResultScore;

/**
 * Unit tests for ResultScore class.
 * 
 * Tests serialization and deserialization of ResultScore fields:
 * - language (optional but commonly present)
 * - textString (optional but commonly present)
 * 
 * Both fields are optional per the XSD.
 */
public class ResultScoreTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithBothFields() throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain language element", xml.contains("<language>en</language>"));
        assertTrue("XML should contain textString element", xml.contains("<textString>0.92</textString>"));
    }
    
    @Test
    public void testSerializeWithLanguageOnly() throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain language element", xml.contains("<language>en</language>"));
        // TextString is null and not serialized (Jackson XML omits null fields by default)
        assertFalse("XML should not contain textString element when null", xml.contains("<textString>"));
    }
    
    @Test
    public void testSerializeWithTextStringOnly() throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setTextString("0.92");
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain textString element", xml.contains("<textString>0.92</textString>"));
        // Language is null and not serialized (Jackson XML omits null fields by default)
        assertFalse("XML should not contain language element when null", xml.contains("<language>"));
    }
    
    @Test
    public void testSerializeEmpty() throws Exception {
        ResultScore resultScore = new ResultScore();
        // Both fields are null
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        
        assertNotNull("XML should not be null", xml);
        // Null fields are not serialized (Jackson XML omits null fields by default)
        assertFalse("XML should not contain language element when null", xml.contains("<language>"));
        assertFalse("XML should not contain textString element when null", xml.contains("<textString>"));
    }
    
    @Test
    public void testDeserializeWithBothFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>";
        
        ResultScore resultScore = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertNotNull("ResultScore should not be null", resultScore);
        assertEquals("Language should match", "en", resultScore.getLanguage());
        assertEquals("TextString should match", "0.92", resultScore.getTextString());
    }
    
    @Test
    public void testDeserializeWithLanguageOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "</resultScore>";
        
        ResultScore resultScore = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertNotNull("ResultScore should not be null", resultScore);
        assertEquals("Language should match", "en", resultScore.getLanguage());
        assertNull("TextString should be null", resultScore.getTextString());
    }
    
    @Test
    public void testDeserializeWithTextStringOnly() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultScore>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>";
        
        ResultScore resultScore = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertNotNull("ResultScore should not be null", resultScore);
        assertNull("Language should be null", resultScore.getLanguage());
        assertEquals("TextString should match", "0.92", resultScore.getTextString());
    }
    
    @Test
    public void testDeserializeEmptyElement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<resultScore/>";
        
        ResultScore resultScore = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertNotNull("ResultScore should not be null", resultScore);
        assertNull("Language should be null", resultScore.getLanguage());
        assertNull("TextString should be null", resultScore.getTextString());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ResultScore original = new ResultScore();
        original.setLanguage("en");
        original.setTextString("0.85");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ResultScore deserialized = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("Language should match", original.getLanguage(), deserialized.getLanguage());
        assertEquals("TextString should match", original.getTextString(), deserialized.getTextString());
    }
    
    @Test
    public void testNumericTextString() throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        ResultScore deserialized = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertEquals("Numeric textString should be preserved", "0.92", deserialized.getTextString());
    }
    
    @Test
    public void testLetterGradeTextString() throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("A");
        
        String xml = XML_MAPPER.writeValueAsString(resultScore);
        ResultScore deserialized = XML_MAPPER.readValue(xml, ResultScore.class);
        
        assertEquals("Letter grade should be preserved", "A", deserialized.getTextString());
    }
}
