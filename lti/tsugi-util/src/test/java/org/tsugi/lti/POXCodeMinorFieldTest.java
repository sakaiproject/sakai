package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.POXCodeMinorField;

/**
 * Unit tests for POXCodeMinorField class.
 */
public class POXCodeMinorFieldTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithBothFields() throws Exception {
        POXCodeMinorField field = new POXCodeMinorField();
        field.setFieldName("field1");
        field.setFieldValue("invaliddata");
        
        String xml = XML_MAPPER.writeValueAsString(field);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain fieldName", xml.contains("imsx_codeMinorFieldName"));
        assertTrue("XML should contain field1", xml.contains("field1"));
        assertTrue("XML should contain fieldValue", xml.contains("imsx_codeMinorFieldValue"));
        assertTrue("XML should contain invaliddata", xml.contains("invaliddata"));
    }
    
    @Test
    public void testDeserializeWithBothFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_codeMinorField xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_codeMinorFieldName>field1</imsx_codeMinorFieldName>\n" +
                     "<imsx_codeMinorFieldValue>invaliddata</imsx_codeMinorFieldValue>\n" +
                     "</imsx_codeMinorField>";
        
        POXCodeMinorField field = XML_MAPPER.readValue(xml, POXCodeMinorField.class);
        
        assertNotNull("Field should not be null", field);
        assertEquals("FieldName should match", "field1", field.getFieldName());
        assertEquals("FieldValue should match", "invaliddata", field.getFieldValue());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXCodeMinorField original = new POXCodeMinorField();
        original.setFieldName("testField");
        original.setFieldValue("testValue");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXCodeMinorField deserialized = XML_MAPPER.readValue(xml, POXCodeMinorField.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("FieldName should match", original.getFieldName(), deserialized.getFieldName());
        assertEquals("FieldValue should match", original.getFieldValue(), deserialized.getFieldValue());
    }
}
