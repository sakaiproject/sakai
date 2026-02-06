package org.tsugi.pox;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Properties;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.junit.Test;

import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;
import org.tsugi.lti.POXJacksonParser;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class POXRequestHandlerTest {
    
    private static final Logger log = LoggerFactory.getLogger(POXRequestHandlerTest.class);
    
    private static final String inputTestData = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +  
        "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" + 
        "<imsx_POXHeader>\n" + 
        "<imsx_POXRequestHeaderInfo>\n" + 
        "<imsx_version>V1.0</imsx_version>\n" + 
        "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" + 
        "</imsx_POXRequestHeaderInfo>\n" + 
        "</imsx_POXHeader>\n" + 
        "<imsx_POXBody>\n" + 
        "<replaceResultRequest>\n" + 
        "<resultRecord>\n" + 
        "<sourcedGUID>\n" + 
        "<sourcedId>3124567</sourcedId>\n" + 
        "</sourcedGUID>\n" + 
        "<result>\n" + 
        "<resultScore>\n" + 
        "<language>en-us</language>\n" + 
        "<textString>A</textString>\n" + 
        "</resultScore>\n" + 
        "</result>\n" + 
        "</resultRecord>\n" + 
        "</replaceResultRequest>\n" + 
        "</imsx_POXBody>\n" + 
        "</imsx_POXEnvelopeRequest>";
    
    private static final String TEST_XML_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<imsx_POXEnvelopeRequest xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
        "<imsx_POXHeader>\n" +
        "<imsx_POXRequestHeaderInfo>\n" +
        "<imsx_version>V1.0</imsx_version>\n" +
        "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
        "</imsx_POXRequestHeaderInfo>\n" +
        "</imsx_POXHeader>\n" +
        "<imsx_POXBody>\n" +
        "<replaceResultRequest>\n" +
        "<resultRecord>\n" +
        "<sourcedGUID>\n" +
        "<sourcedId>3124567</sourcedId>\n" +
        "</sourcedGUID>\n" +
        "<result>\n" +
        "<resultScore>\n" +
        "<language>en-us</language>\n" +
        "<textString>A</textString>\n" +
        "</resultScore>\n" +
        "</result>\n" +
        "</resultRecord>\n" +
        "</replaceResultRequest>\n" +
        "</imsx_POXBody>\n" +
        "</imsx_POXEnvelopeRequest>";
    
    @Test
    public void testConstructorWithXmlString() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertTrue("Request should be valid", pox.valid);
        assertEquals("replaceResultRequest", pox.getOperation());
        assertEquals("V1.0", pox.getHeaderVersion());
        assertEquals("999999123", pox.getHeaderMessageIdentifier());
    }
    
    @Test
    public void testGetOperation() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertEquals("replaceResultRequest", pox.getOperation());
    }
    
    @Test
    public void testGetHeaderVersion() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertEquals("V1.0", pox.getHeaderVersion());
    }
    
    @Test
    public void testGetHeaderMessageIdentifier() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertEquals("999999123", pox.getHeaderMessageIdentifier());
    }
    
    @Test
    public void testGetHeaderItem() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertEquals("V1.0", pox.getHeaderItem("/imsx_version"));
        assertEquals("999999123", pox.getHeaderItem("/imsx_messageIdentifier"));
        assertNull(pox.getHeaderItem("/nonexistent"));
    }
    
    @Test
    public void testGetHeaderMap() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        Map<String, String> headerMap = pox.getHeaderMap();
        
        assertNotNull("Header map should not be null", headerMap);
        assertEquals("V1.0", headerMap.get("version"));
        assertEquals("999999123", headerMap.get("messageIdentifier"));
    }
    
    @Test
    public void testGetBodyMap() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        String sourcedId = POXJacksonParser.getBodySourcedId(pox.getPoxRequest());
        String textString = POXJacksonParser.getBodyTextString(pox.getPoxRequest());
        String language = POXJacksonParser.getBodyLanguage(pox.getPoxRequest());
        
        assertNotNull("SourcedId should not be null", sourcedId);
        assertEquals("3124567", sourcedId);
        assertNotNull("TextString should not be null", textString);
        assertEquals("A", textString);
        assertNotNull("Language should not be null", language);
        assertEquals("en-us", language);
    }
    
    @Test
    public void testGetPostBody() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertEquals(TEST_XML_REQUEST, pox.getPostBody());
    }
    
    @Test
    public void testGetPoxRequest() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        POXEnvelopeRequest request = pox.getPoxRequest();
        
        assertNotNull("POX request should not be null", request);
        assertNotNull("POX header should not be null", request.getPoxHeader());
        assertNotNull("POX body should not be null", request.getPoxBody());
    }
    
    @Test
    public void testGetResponseSuccess() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        String response = pox.getResponseSuccess("Test success", "<test>body</test>");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test success"));
        // Note: bodyString parameter is ignored as bodyContent support was removed from POXResponseBuilder
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
    }
    
    @Test
    public void testGetResponseFailure() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        Properties minorCodes = new Properties();
        minorCodes.setProperty("error1", "invaliddata");
        minorCodes.setProperty("error2", "incompletedata");
        
        String response = pox.getResponseFailure("Test failure", minorCodes);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain description", response.contains("Test failure"));
        assertTrue("Response should contain minor codes", response.contains("invaliddata"));
    }
    
    @Test
    public void testGetResponseUnsupported() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        String response = pox.getResponseUnsupported("Test unsupported");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain unsupported", response.contains("unsupported"));
        assertTrue("Response should contain description", response.contains("Test unsupported"));
    }
    
    @Test
    public void testInArray() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        assertTrue("Should find valid major code", pox.inArray(POXRequestHandler.validMajor, "success"));
        assertTrue("Should find valid severity", pox.inArray(POXRequestHandler.validSeverity, "error"));
        assertTrue("Should find valid minor code", pox.inArray(POXRequestHandler.validMinor, "invaliddata"));
        assertFalse("Should not find invalid code", pox.inArray(POXRequestHandler.validMajor, "invalid"));
        assertFalse("Should not find null code", pox.inArray(POXRequestHandler.validMajor, null));
    }
    
    @Test
    public void testGetFatalResponse() {
        String description = "Test fatal error";
        String message_id = "testOperation";
        
        // Call the method (now uses POXResponseBuilder instead of hand-constructed XML)
        String response = POXRequestHandler.getFatalResponse(description, message_id);
        
        // Extract the generated messageId from the response (it's time-based)
        Pattern messageIdPattern = Pattern.compile("<imsx_messageIdentifier>(.*?)</imsx_messageIdentifier>");
        Matcher matcher = messageIdPattern.matcher(response);
        assertTrue("Response should contain messageIdentifier", matcher.find());
        String messageId = matcher.group(1);
        
        // Build expected XML using POXResponseBuilder (same as the implementation now uses)
        String expected = POXResponseBuilder.create()
            .withDescription(description)
            .asFailure()
            .withMessageId(messageId)
            .withOperation(message_id)
            .buildAsXml();
        
        // Compare exact XML - should match since both use POXResponseBuilder
        assertEquals("Response should match POXResponseBuilder output", expected, response);
    }
    
    @Test
    public void testGetFatalResponseWithMessageId() {
        String response = POXRequestHandler.getFatalResponse("Test fatal error", "test123");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test fatal error"));
        assertTrue("Response should contain message ID", response.contains("test123"));
    }
    
    @Test
    public void testRunTest() {
        POXRequestHandler pox = new POXRequestHandler(inputTestData);
        
        assertEquals("V1.0", pox.getHeaderVersion());
        assertEquals("replaceResultRequest", pox.getOperation());
        
        String guid = POXJacksonParser.getBodySourcedId(pox.getPoxRequest());
        assertEquals("3124567", guid);
        
        String grade = POXJacksonParser.getBodyTextString(pox.getPoxRequest());
        assertEquals("A", grade);

        String desc = "Message received and validated operation=" + pox.getOperation() +
            " guid=" + guid + " grade=" + grade;

        String output = pox.getResponseUnsupported(desc);
        assertNotNull("Unsupported response should not be null", output);
        assertTrue("Unsupported response should contain unsupported", output.contains("unsupported"));
        assertTrue("Unsupported response should contain description", output.contains(desc));

        Properties props = new Properties();
        props.setProperty("fred", "zap");  // Invalid minor code - will be filtered out
        props.setProperty("sam", POXRequestHandler.MINOR_IDALLOC);  // Valid minor code
        
        output = pox.getResponseFailure(desc, props);
        assertNotNull("Failure response should not be null", output);
        assertTrue("Failure response should contain failure", output.contains("failure"));
        assertTrue("Failure response should contain description", output.contains(desc));
        assertTrue("Failure response should contain minor codes", output.contains("imsx_codeMinor"));
        // Only valid minor codes are included in the minor codes section - "sam" with MINOR_IDALLOC should be present
        assertTrue("Failure response should contain sam", output.contains("sam"));
        assertTrue("Failure response should contain MINOR_IDALLOC", output.contains(POXRequestHandler.MINOR_IDALLOC));
    }
    
    @Test
    public void testInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\"?><invalid>content</invalid>";
        POXRequestHandler pox = new POXRequestHandler(invalidXml);
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testEmptyXml() {
        POXRequestHandler pox = new POXRequestHandler("");
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testNullXml() {
        POXRequestHandler pox = new POXRequestHandler((String) null);
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testResponseWithMinorCodes() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        Properties minorCodes = new Properties();
        minorCodes.setProperty("field1", "invaliddata");
        minorCodes.setProperty("field2", "incompletedata");
        
        String response = pox.getResponse("Test response", "failure", "error", "msg123", 
                                        minorCodes, "<body>content</body>");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain minor codes", response.contains("imsx_codeMinor"));
        assertTrue("Response should contain field1", response.contains("field1"));
        assertTrue("Response should contain field2", response.contains("field2"));
    }
    
    @Test
    public void testResponseWithInvalidMinorCodes() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        Properties minorCodes = new Properties();
        minorCodes.setProperty("field1", "invalidcode");
        minorCodes.setProperty("field2", "invaliddata");
        
        String response = pox.getResponse("Test response", "failure", "error", "msg123", 
                                        minorCodes, null);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain internal error", response.contains("Internal error"));
    }
    
    @Test
    public void testCreateFallbackResponse() throws Exception {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Use reflection to access the private createFallbackResponse method
        Method method = POXRequestHandler.class.getDeclaredMethod("createFallbackResponse", 
            String.class, String.class, String.class, String.class, String.class, 
            POXCodeMinor.class, Object.class);
        method.setAccessible(true);
        
        // Test case 1: Basic response without minor codes or body
        String description = "Test description";
        String major = "success";
        String severity = "status";
        String messageId = "msg123";
        String operation = "replaceResultRequest";
        POXCodeMinor codeMinor = null;
        String bodyString = null;
        
        String response = (String) method.invoke(pox, description, major, severity, messageId, operation, codeMinor, bodyString);
        
        // Build expected XML using hand-constructed format, adjusted to match Jackson's compact output
        // Jackson produces compact XML with single quotes in declaration and includes empty tags
        String expected = String.format(
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">" +
            "<imsx_POXHeader>" +
            "<imsx_POXResponseHeaderInfo>" +
            "<imsx_version>V1.0</imsx_version>" +
            "<imsx_messageIdentifier>%s</imsx_messageIdentifier>" +
            "<imsx_statusInfo>" +
            "<imsx_codeMajor>%s</imsx_codeMajor>" +
            "<imsx_severity>%s</imsx_severity>" +
            "<imsx_description>%s</imsx_description>" +
            "<imsx_messageRefIdentifier>%s</imsx_messageRefIdentifier>" +
            "<imsx_operationRefIdentifier>%s</imsx_operationRefIdentifier>" +
            "<imsx_codeMinor/>" +
            "</imsx_statusInfo>" +
            "</imsx_POXResponseHeaderInfo>" +
            "</imsx_POXHeader>" +
            "<imsx_POXBody/>" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(major), 
            StringEscapeUtils.escapeXml11(severity), 
            StringEscapeUtils.escapeXml11(description), 
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(operation)
        );
        
        assertEquals("Response should match hand-constructed XML (adjusted for Jackson format)", expected, response);
    }
    
    @Test
    public void testCreateFallbackResponseWithMinorCodes() throws Exception {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Use reflection to access the private createFallbackResponse method
        Method method = POXRequestHandler.class.getDeclaredMethod("createFallbackResponse", 
            String.class, String.class, String.class, String.class, String.class, 
            POXCodeMinor.class, Object.class);
        method.setAccessible(true);
        
        // Test case 2: Response with minor codes
        String description = "Test description";
        String major = "failure";
        String severity = "error";
        String messageId = "msg456";
        String operation = "replaceResultRequest";
        
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
        
        String bodyString = null;
        
        String response = (String) method.invoke(pox, description, major, severity, messageId, operation, codeMinor, bodyString);
        
        // Build expected XML using hand-constructed format, adjusted to match Jackson's compact output
        // Jackson produces compact XML with single quotes in declaration
        StringBuilder minorString = new StringBuilder();
        minorString.append("<imsx_codeMinor>");
        for (POXCodeMinorField field : fields) {
            minorString.append("<imsx_codeMinorField>");
            minorString.append("<imsx_codeMinorFieldName>");
            minorString.append(StringEscapeUtils.escapeXml11(field.getFieldName()));
            minorString.append("</imsx_codeMinorFieldName>");
            minorString.append("<imsx_codeMinorFieldValue>");
            minorString.append(StringEscapeUtils.escapeXml11(field.getFieldValue()));
            minorString.append("</imsx_codeMinorFieldValue>");
            minorString.append("</imsx_codeMinorField>");
        }
        minorString.append("</imsx_codeMinor>");
        
        String expected = String.format(
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">" +
            "<imsx_POXHeader>" +
            "<imsx_POXResponseHeaderInfo>" +
            "<imsx_version>V1.0</imsx_version>" +
            "<imsx_messageIdentifier>%s</imsx_messageIdentifier>" +
            "<imsx_statusInfo>" +
            "<imsx_codeMajor>%s</imsx_codeMajor>" +
            "<imsx_severity>%s</imsx_severity>" +
            "<imsx_description>%s</imsx_description>" +
            "<imsx_messageRefIdentifier>%s</imsx_messageRefIdentifier>" +
            "<imsx_operationRefIdentifier>%s</imsx_operationRefIdentifier>" +
            "%s" +
            "</imsx_statusInfo>" +
            "</imsx_POXResponseHeaderInfo>" +
            "</imsx_POXHeader>" +
            "<imsx_POXBody/>" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(major), 
            StringEscapeUtils.escapeXml11(severity), 
            StringEscapeUtils.escapeXml11(description), 
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(operation),
            minorString.toString()
        );
        
        assertEquals("Response with minor codes should match hand-constructed XML (adjusted for Jackson format)", expected, response);
    }
    
    @Test
    public void testCreateFallbackResponseWithBody() throws Exception {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Use reflection to access the private createFallbackResponse method
        Method method = POXRequestHandler.class.getDeclaredMethod("createFallbackResponse", 
            String.class, String.class, String.class, String.class, String.class, 
            POXCodeMinor.class, Object.class);
        method.setAccessible(true);
        
        // Test case 3: Response with body content (bodyString is ignored by POXResponseBuilder)
        String description = "Test description";
        String major = "success";
        String severity = "status";
        String messageId = "msg789";
        String operation = "replaceResultRequest";
        POXCodeMinor codeMinor = null;
        String bodyString = "<test>body content</test>";
        
        String response = (String) method.invoke(pox, description, major, severity, messageId, operation, codeMinor, bodyString);
        
        // Build expected XML using hand-constructed format, adjusted to match Jackson's compact output
        // Note: bodyString is ignored by POXResponseBuilder, so body will be empty
        String expected = String.format(
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">" +
            "<imsx_POXHeader>" +
            "<imsx_POXResponseHeaderInfo>" +
            "<imsx_version>V1.0</imsx_version>" +
            "<imsx_messageIdentifier>%s</imsx_messageIdentifier>" +
            "<imsx_statusInfo>" +
            "<imsx_codeMajor>%s</imsx_codeMajor>" +
            "<imsx_severity>%s</imsx_severity>" +
            "<imsx_description>%s</imsx_description>" +
            "<imsx_messageRefIdentifier>%s</imsx_messageRefIdentifier>" +
            "<imsx_operationRefIdentifier>%s</imsx_operationRefIdentifier>" +
            "<imsx_codeMinor/>" +
            "</imsx_statusInfo>" +
            "</imsx_POXResponseHeaderInfo>" +
            "</imsx_POXHeader>" +
            "<imsx_POXBody/>" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(major), 
            StringEscapeUtils.escapeXml11(severity), 
            StringEscapeUtils.escapeXml11(description), 
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(operation)
        );
        
        assertEquals("Response with body should match hand-constructed XML (bodyString ignored, adjusted for Jackson format)", expected, response);
    }
}

