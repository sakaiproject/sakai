package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.tsugi.lti.objects.POXStatusInfo;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;

/**
 * Unit tests for POXStatusInfo class.
 */
public class POXStatusInfoTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeSuccessStatus() throws Exception {
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        statusInfo.setDescription("Operation completed successfully");
        statusInfo.setMessageRefIdentifier("999999123");
        statusInfo.setOperationRefIdentifier("replaceResult");
        
        String xml = XML_MAPPER.writeValueAsString(statusInfo);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain codeMajor", xml.contains("imsx_codeMajor"));
        assertTrue("XML should contain success", xml.contains("success"));
        assertTrue("XML should contain severity", xml.contains("imsx_severity"));
        assertTrue("XML should contain description", xml.contains("Operation completed successfully"));
        assertFalse("XML should not contain codeMinor when null", xml.contains("imsx_codeMinor"));
    }
    
    @Test
    public void testSerializeWithCodeMinor() throws Exception {
        POXStatusInfo statusInfo = new POXStatusInfo();
        statusInfo.setCodeMajor("failure");
        statusInfo.setSeverity("error");
        statusInfo.setDescription("Operation failed");
        statusInfo.setMessageRefIdentifier("999999123");
        statusInfo.setOperationRefIdentifier("replaceResult");
        
        POXCodeMinor codeMinor = new POXCodeMinor();
        List<POXCodeMinorField> fields = new ArrayList<>();
        POXCodeMinorField field = new POXCodeMinorField();
        field.setFieldName("field1");
        field.setFieldValue("invaliddata");
        fields.add(field);
        codeMinor.setCodeMinorFields(fields);
        statusInfo.setCodeMinor(codeMinor);
        
        String xml = XML_MAPPER.writeValueAsString(statusInfo);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain codeMinor", xml.contains("imsx_codeMinor"));
        assertTrue("XML should contain field name", xml.contains("field1"));
        assertTrue("XML should contain field value", xml.contains("invaliddata"));
    }
    
    @Test
    public void testDeserializeSuccessStatus() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_statusInfo xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_codeMajor>success</imsx_codeMajor>\n" +
                     "<imsx_severity>status</imsx_severity>\n" +
                     "<imsx_description>Operation completed successfully</imsx_description>\n" +
                     "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
                     "<imsx_operationRefIdentifier>replaceResult</imsx_operationRefIdentifier>\n" +
                     "</imsx_statusInfo>";
        
        POXStatusInfo statusInfo = XML_MAPPER.readValue(xml, POXStatusInfo.class);
        
        assertNotNull("StatusInfo should not be null", statusInfo);
        assertEquals("CodeMajor should match", "success", statusInfo.getCodeMajor());
        assertEquals("Severity should match", "status", statusInfo.getSeverity());
        assertEquals("Description should match", "Operation completed successfully", statusInfo.getDescription());
        assertEquals("MessageRefIdentifier should match", "999999123", statusInfo.getMessageRefIdentifier());
        assertEquals("OperationRefIdentifier should match", "replaceResult", statusInfo.getOperationRefIdentifier());
        assertNull("CodeMinor should be null", statusInfo.getCodeMinor());
    }
    
    @Test
    public void testDeserializeWithCodeMinor() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<imsx_statusInfo xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                     "<imsx_codeMajor>failure</imsx_codeMajor>\n" +
                     "<imsx_severity>error</imsx_severity>\n" +
                     "<imsx_description>Operation failed</imsx_description>\n" +
                     "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
                     "<imsx_operationRefIdentifier>replaceResult</imsx_operationRefIdentifier>\n" +
                     "<imsx_codeMinor>\n" +
                     "<imsx_codeMinorField>\n" +
                     "<imsx_codeMinorFieldName>field1</imsx_codeMinorFieldName>\n" +
                     "<imsx_codeMinorFieldValue>invaliddata</imsx_codeMinorFieldValue>\n" +
                     "</imsx_codeMinorField>\n" +
                     "</imsx_codeMinor>\n" +
                     "</imsx_statusInfo>";
        
        POXStatusInfo statusInfo = XML_MAPPER.readValue(xml, POXStatusInfo.class);
        
        assertNotNull("StatusInfo should not be null", statusInfo);
        assertEquals("CodeMajor should match", "failure", statusInfo.getCodeMajor());
        assertNotNull("CodeMinor should not be null", statusInfo.getCodeMinor());
        assertNotNull("CodeMinorFields should not be null", statusInfo.getCodeMinor().getCodeMinorFields());
        assertEquals("Should have one code minor field", 1, statusInfo.getCodeMinor().getCodeMinorFields().size());
        assertEquals("Field name should match", "field1", statusInfo.getCodeMinor().getCodeMinorFields().get(0).getFieldName());
        assertEquals("Field value should match", "invaliddata", statusInfo.getCodeMinor().getCodeMinorFields().get(0).getFieldValue());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        POXStatusInfo original = new POXStatusInfo();
        original.setCodeMajor("success");
        original.setSeverity("status");
        original.setDescription("Test description");
        original.setMessageRefIdentifier("123");
        original.setOperationRefIdentifier("testOp");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        POXStatusInfo deserialized = XML_MAPPER.readValue(xml, POXStatusInfo.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("CodeMajor should match", original.getCodeMajor(), deserialized.getCodeMajor());
        assertEquals("Severity should match", original.getSeverity(), deserialized.getSeverity());
        assertEquals("Description should match", original.getDescription(), deserialized.getDescription());
        assertEquals("MessageRefIdentifier should match", original.getMessageRefIdentifier(), deserialized.getMessageRefIdentifier());
        assertEquals("OperationRefIdentifier should match", original.getOperationRefIdentifier(), deserialized.getOperationRefIdentifier());
    }
}
