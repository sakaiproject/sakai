package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;

/**
 * Unit tests for POXCodeMinor class.
 */
public class POXCodeMinorTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithFields() throws Exception {
        POXCodeMinor codeMinor = new POXCodeMinor();
        List<POXCodeMinorField> fields = new ArrayList<>();
        
        POXCodeMinorField field1 = new POXCodeMinorField();
        field1.setFieldName("field1");
        field1.setFieldValue("invaliddata");
        fields.add(field1);
        
        POXCodeMinorField field2 = new POXCodeMinorField();
        field2.setFieldName("field2");
        field2.setFieldValue("incompletedata");
        fields.add(field2);
        
        codeMinor.setCodeMinorFields(fields);
        
        String xml = XML_MAPPER.writeValueAsString(codeMinor);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain codeMinorField", xml.contains("imsx_codeMinorField"));
        assertTrue("XML should contain field1", xml.contains("field1"));
        assertTrue("XML should contain invaliddata", xml.contains("invaliddata"));
        assertTrue("XML should contain field2", xml.contains("field2"));
        assertTrue("XML should contain incompletedata", xml.contains("incompletedata"));
    }
    
    @Test
    public void testDeserializeWithFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_codeMinor xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_codeMinorField>\n" +
                     "<imsx_codeMinorFieldName>field1</imsx_codeMinorFieldName>\n" +
                     "<imsx_codeMinorFieldValue>invaliddata</imsx_codeMinorFieldValue>\n" +
                     "</imsx_codeMinorField>\n" +
                     "<imsx_codeMinorField>\n" +
                     "<imsx_codeMinorFieldName>field2</imsx_codeMinorFieldName>\n" +
                     "<imsx_codeMinorFieldValue>incompletedata</imsx_codeMinorFieldValue>\n" +
                     "</imsx_codeMinorField>\n" +
                     "</imsx_codeMinor>";
        
        POXCodeMinor codeMinor = XML_MAPPER.readValue(xml, POXCodeMinor.class);
        
        assertNotNull("CodeMinor should not be null", codeMinor);
        assertNotNull("CodeMinorFields should not be null", codeMinor.getCodeMinorFields());
        assertEquals("Should have 2 fields", 2, codeMinor.getCodeMinorFields().size());
        assertEquals("First field name should match", "field1", codeMinor.getCodeMinorFields().get(0).getFieldName());
        assertEquals("First field value should match", "invaliddata", codeMinor.getCodeMinorFields().get(0).getFieldValue());
        assertEquals("Second field name should match", "field2", codeMinor.getCodeMinorFields().get(1).getFieldName());
        assertEquals("Second field value should match", "incompletedata", codeMinor.getCodeMinorFields().get(1).getFieldValue());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXCodeMinor original = new POXCodeMinor();
        List<POXCodeMinorField> fields = new ArrayList<>();
        
        POXCodeMinorField field = new POXCodeMinorField();
        field.setFieldName("testField");
        field.setFieldValue("testValue");
        fields.add(field);
        
        original.setCodeMinorFields(fields);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXCodeMinor deserialized = XML_MAPPER.readValue(xml, POXCodeMinor.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("CodeMinorFields should not be null", deserialized.getCodeMinorFields());
        assertEquals("Should have 1 field", 1, deserialized.getCodeMinorFields().size());
        assertEquals("Field name should match", original.getCodeMinorFields().get(0).getFieldName(),
                     deserialized.getCodeMinorFields().get(0).getFieldName());
        assertEquals("Field value should match", original.getCodeMinorFields().get(0).getFieldValue(),
                     deserialized.getCodeMinorFields().get(0).getFieldValue());
    }
}
