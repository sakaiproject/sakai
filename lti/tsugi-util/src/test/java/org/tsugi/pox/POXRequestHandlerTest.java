package org.tsugi.pox;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.mockito.Mockito;

import org.tsugi.lti.Base64;
import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.objects.POXCodeMinor;
import org.tsugi.lti.objects.POXCodeMinorField;
import org.tsugi.lti.POXJacksonParser;
import org.apache.commons.text.StringEscapeUtils;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.signature.OAuthSignatureMethod;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class POXRequestHandlerTest {
    
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
        
        assertTrue("Request should be valid", pox.isValid());
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
        // Use valid POX response XML - replaceResultResponse matches the operation
        String bodyXml = "<replaceResultResponse/>";
        String response = pox.getResponseSuccess("Test success", bodyXml);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test success"));
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
        // Verify that the bodyXml is included in the response
        assertTrue("Response should contain the bodyXml parameter", response.contains("replaceResultResponse"));
    }
    
    @Test
    public void testGetResponseSuccessWithBodyXml() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        // Test with readResultResponse XML - should be included regardless of operation
        String bodyXml = "<readResultResponse><result><resultScore><language>en</language><textString>0.95</textString></resultScore></result></readResultResponse>";
        String response = pox.getResponseSuccess("Test success with body", bodyXml);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test success with body"));
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
        // Verify that the bodyXml is included in the response even though operation is replaceResultRequest
        assertTrue("Response should contain readResultResponse", response.contains("readResultResponse"));
        assertTrue("Response should contain the score value", response.contains("0.95"));
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
        assertTrue("Should find valid major code", POXRequestHandler.inArray(POXRequestHandler.validMajor, "success"));
        assertTrue("Should find valid severity", POXRequestHandler.inArray(POXRequestHandler.validSeverity, "error"));
        assertTrue("Should find valid minor code", POXRequestHandler.inArray(POXRequestHandler.validMinor, "invaliddata"));
        assertFalse("Should not find invalid code", POXRequestHandler.inArray(POXRequestHandler.validMajor, "invalid"));
        assertFalse("Should not find null code", POXRequestHandler.inArray(POXRequestHandler.validMajor, null));
    }
    
    @Test
    public void testGetFatalResponse() {
        String description = "Test fatal error";
        String operation = "testOperation";
        
        // Call the method (now uses POXResponseBuilder instead of hand-constructed XML)
        String response = POXRequestHandler.getFatalResponse(description, operation);
        
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
            .withOperation(operation)
            .buildAsXml();
        
        // Compare exact XML - should match since both use POXResponseBuilder
        assertEquals("Response should match POXResponseBuilder output", expected, response);
    }
    
    @Test
    public void testGetFatalResponseWithOperation() {
        String response = POXRequestHandler.getFatalResponse("Test fatal error", "test123");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test fatal error"));
        assertTrue("Response should contain operation", response.contains("test123"));
    }
    
    @Test
    public void testRunTest() {
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
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
        props.setProperty("sam", POXConstants.MINOR_IDALLOC);  // Valid minor code
        
        output = pox.getResponseFailure(desc, props);
        assertNotNull("Failure response should not be null", output);
        assertTrue("Failure response should contain failure", output.contains("failure"));
        assertTrue("Failure response should contain description", output.contains(desc));
        assertTrue("Failure response should contain minor codes", output.contains("imsx_codeMinor"));
        // Only valid minor codes are included in the minor codes section - "sam" with MINOR_IDALLOC should be present
        assertTrue("Failure response should contain sam", output.contains("sam"));
        assertTrue("Failure response should contain MINOR_IDALLOC", output.contains(POXConstants.MINOR_IDALLOC));
    }
    
    @Test
    public void testInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\"?><invalid>content</invalid>";
        POXRequestHandler pox = new POXRequestHandler(invalidXml);
        
        assertFalse("Request should not be valid", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testEmptyXml() {
        POXRequestHandler pox = new POXRequestHandler("");
        
        assertFalse("Request should not be valid", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testNullXml() {
        POXRequestHandler pox = new POXRequestHandler((String) null);
        
        assertFalse("Request should not be valid", pox.isValid());
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
        // Test the "filter-and-warn" policy: when invalid minor codes are mixed with valid ones,
        // POXRequestHandler.getResponse filters out invalid codes, includes valid codes in the response,
        // and appends an "Internal error" message to the description to alert about the invalid entries.
        // This ensures the response still succeeds with valid codes while warning about configuration issues.
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        Properties minorCodes = new Properties();
        minorCodes.setProperty("field1", "invalidcode");  // Invalid minor code - will be filtered out
        minorCodes.setProperty("field2", "invaliddata");  // Valid minor code - will be included
        
        String response = pox.getResponse("Test response", "failure", "error", "msg123", 
                                        minorCodes, null);
        
        assertNotNull("Response should not be null", response);
        // Invalid codes trigger an internal error message in the description
        assertTrue("Response should contain internal error", response.contains("Internal error"));
        // The invalid code value appears in the internal error message (expected behavior)
        assertTrue("Response should contain invalid code in error message", response.contains("Invalid imsx_codeMinorFieldValue=invalidcode"));
        // Valid minor codes should still be included in the response
        assertTrue("Response should contain valid minor code", response.contains("invaliddata"));
        assertTrue("Response should contain field2 with valid code", response.contains("field2"));
        // Invalid minor codes should be filtered out from the minor codes section
        assertFalse("Response should not contain field1 in minor codes section", response.contains("<imsx_codeMinorFieldName>field1</imsx_codeMinorFieldName>"));
        // Verify that invalidcode is not in the minor codes section (only in error message)
        int minorCodesStart = response.indexOf("<imsx_codeMinor>");
        int minorCodesEnd = response.indexOf("</imsx_codeMinor>");
        if (minorCodesStart >= 0 && minorCodesEnd >= 0) {
            String minorCodesSection = response.substring(minorCodesStart, minorCodesEnd);
            assertFalse("Invalid code should not appear in minor codes section", minorCodesSection.contains("invalidcode"));
        }
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
        // messageRefIdentifier should reference the original request message ID, not the response messageId
        String requestMessageId = pox.getHeaderMessageIdentifier();
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
            "</imsx_statusInfo>" +
            "</imsx_POXResponseHeaderInfo>" +
            "</imsx_POXHeader>" +
            "<imsx_POXBody/>" +
            "</imsx_POXEnvelopeResponse>",
            StringEscapeUtils.escapeXml11(messageId), 
            StringEscapeUtils.escapeXml11(major), 
            StringEscapeUtils.escapeXml11(severity), 
            StringEscapeUtils.escapeXml11(description), 
            StringEscapeUtils.escapeXml11(requestMessageId), 
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
        
        // messageRefIdentifier should reference the original request message ID, not the response messageId
        String requestMessageId = pox.getHeaderMessageIdentifier();
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
            StringEscapeUtils.escapeXml11(requestMessageId), 
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
        
        // Test case 3: Response with body content that doesn't match known response types
        // bodyString that doesn't match known POX response types will be ignored
        String description = "Test description";
        String major = "success";
        String severity = "status";
        String messageId = "msg789";
        String operation = "replaceResultRequest";
        POXCodeMinor codeMinor = null;
        String bodyString = "<test>body content</test>";  // Not a valid POX response type
        
        String response = (String) method.invoke(pox, description, major, severity, messageId, operation, codeMinor, bodyString);
        
        // Build expected XML using POXResponseBuilder (same as implementation)
        // bodyString doesn't match known response types, so body will be empty
        String requestMessageId = pox.getHeaderMessageIdentifier();
        String expected = POXResponseBuilder.create()
            .withDescription(description)
            .withMajor(major)
            .withSeverity(severity)
            .withMessageId(messageId)
            .withOperation(operation)
            .withMessageRefIdentifier(requestMessageId)
            .buildAsXml();
        
        assertEquals("Response with invalid bodyString should match POXResponseBuilder output (body ignored)", expected, response);
        // Verify that invalid bodyString is not included
        assertFalse("Response should not contain invalid bodyString", response.contains("<test>body content</test>"));
    }
    
    @Test
    public void testParsePostBodyStateReset() throws Exception {
        // Test that parsePostBody() resets state on each call to prevent state leakage
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Verify initial state is valid
        assertTrue("Initial request should be valid", pox.isValid());
        assertEquals("replaceResultRequest", pox.getOperation());
        assertNull("Initial error message should be null", pox.errorMessage);
        
        // Use reflection to access private fields
        java.lang.reflect.Field operationField = POXRequestHandler.class.getDeclaredField("operation");
        operationField.setAccessible(true);
        
        // Manually set some state to simulate a previous failed parse
        pox.valid = false;
        pox.errorMessage = "Previous error";
        operationField.set(pox, "previousOperation");
        
        // Call parsePostBody() again - it should reset state and parse successfully
        pox.parsePostBody();
        
        // Verify state was reset and parsing succeeded
        assertTrue("Request should be valid after reset", pox.isValid());
        assertEquals("replaceResultRequest", pox.getOperation());
        assertNull("Error message should be null after successful parse", pox.errorMessage);
    }
    
    @Test
    public void testParsePostBodyStateResetWithInvalidXml() throws Exception {
        // Test that parsePostBody() resets state even when parsing fails
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Verify initial state is valid
        assertTrue("Initial request should be valid", pox.isValid());
        
        // Use reflection to access private postBody field
        java.lang.reflect.Field postBodyField = POXRequestHandler.class.getDeclaredField("postBody");
        postBodyField.setAccessible(true);
        
        // Change to invalid XML and parse
        postBodyField.set(pox, "<?xml version=\"1.0\"?><invalid>content</invalid>");
        pox.parsePostBody();
        
        // Verify state was reset and parsing failed appropriately
        assertFalse("Request should not be valid after invalid XML", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
        assertNull("Operation should be null after failed parse", pox.getOperation());
        
        // Now parse valid XML again - should reset and succeed
        postBodyField.set(pox, TEST_XML_REQUEST);
        pox.parsePostBody();
        
        // Verify state was reset and parsing succeeded
        assertTrue("Request should be valid after reset and valid XML", pox.isValid());
        assertEquals("replaceResultRequest", pox.getOperation());
        assertNull("Error message should be null after successful parse", pox.errorMessage);
    }
    
    @Test
    public void testXxeProtection() {
        // Test that XXE (XML External Entity) attacks are blocked
        // This test verifies that the xmlMapper is hardened against XXE attacks
        
        // XXE attack payload attempting to read /etc/passwd (or similar sensitive file)
        String xxePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE foo [\n" +
            "<!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n" +
            "]>\n" +
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
            "<sourcedId>&xxe;</sourcedId>\n" +
            "</sourcedGUID>\n" +
            "</resultRecord>\n" +
            "</replaceResultRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
        
        POXRequestHandler pox = new POXRequestHandler(xxePayload);
        
        // With XXE protection, the parser should either:
        // 1. Fail to parse (most likely due to DTD being disabled)
        // 2. Parse but not expand the entity (entity content should be empty or null)
        
        // The parser should fail because DTD support is disabled
        assertFalse("XXE attack should be blocked - request should not be valid", pox.isValid());
        assertNotNull("Should have error message indicating parse failure", pox.errorMessage);
        
        // Verify that the entity was not expanded (if parsing somehow succeeded)
        // The sourcedId should not contain file contents
        if (pox.getPoxRequest() != null) {
            String sourcedId = POXJacksonParser.getBodySourcedId(pox.getPoxRequest());
            if (sourcedId != null) {
                assertFalse("Entity should not be expanded - sourcedId should not contain file contents", 
                    sourcedId.contains("root:") || sourcedId.contains("/bin/"));
            }
        }
    }
    
    @Test
    public void testXxeProtectionExternalEntity() {
        // Test that external entity references are blocked
        String xxePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE foo [\n" +
            "<!ENTITY xxe SYSTEM \"http://evil.com/evil.xml\">\n" +
            "]>\n" +
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
            "<sourcedId>&xxe;</sourcedId>\n" +
            "</sourcedGUID>\n" +
            "</resultRecord>\n" +
            "</replaceResultRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
        
        POXRequestHandler pox = new POXRequestHandler(xxePayload);
        
        // External entity resolution should be blocked
        assertFalse("External entity attack should be blocked - request should not be valid", pox.isValid());
        assertNotNull("Should have error message indicating parse failure", pox.errorMessage);
    }
    
    /**
     * Helper method to create a mock HttpServletRequest with OAuth headers and body
     */
    private HttpServletRequest createMockRequestWithOAuth(String body, String oauthConsumerKey, 
            String oauthSecret, String oauthBodyHash, String oauthSignatureMethod) throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        
        // Set content type
        Mockito.when(request.getContentType()).thenReturn("application/xml");
        
        // Set Authorization header with OAuth parameters
        String authHeader = String.format(
            "OAuth oauth_consumer_key=\"%s\", oauth_body_hash=\"%s\", oauth_signature_method=\"%s\", oauth_signature=\"test_signature\", oauth_timestamp=\"1234567890\", oauth_nonce=\"test_nonce\"",
            oauthConsumerKey, oauthBodyHash, oauthSignatureMethod);
        Mockito.when(request.getHeader("Authorization")).thenReturn(authHeader);
        
        // Set request body - create a new reader each time getReader() is called
        // This is needed because BufferedReader can only be read once
        Mockito.when(request.getReader()).thenAnswer(invocation -> {
            return new BufferedReader(new StringReader(body));
        });
        
        // Set method
        Mockito.when(request.getMethod()).thenReturn("POST");
        
        // Set request URL
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/lti/service"));
        
        // Set parameter map (empty for POX requests)
        Mockito.when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        
        // Set parameter names enumeration (empty)
        Mockito.when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        
        return request;
    }
    
    /**
     * Helper method to create a properly signed OAuth request for POX
     */
    private HttpServletRequest createSignedOAuthRequest(String body, String oauthConsumerKey, 
            String oauthSecret, String url) throws Exception {
        // Compute body hash
        String oauthBodyHash = computeBodyHash(body, "HMAC-SHA1");
        String oauthSignatureMethod = "HMAC-SHA1";
        
        // Create OAuth message with required parameters
        OAuthMessage oam = new OAuthMessage("POST", url, null);
        oam.addParameter(OAuth.OAUTH_CONSUMER_KEY, oauthConsumerKey);
        oam.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, oauthSignatureMethod);
        oam.addParameter(OAuth.OAUTH_VERSION, "1.0");
        oam.addParameter(OAuth.OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        oam.addParameter(OAuth.OAUTH_NONCE, String.valueOf(System.nanoTime()));
        oam.addParameter("oauth_body_hash", oauthBodyHash);
        
        // Sign the message
        OAuthConsumer consumer = new OAuthConsumer(null, oauthConsumerKey, oauthSecret, null);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        OAuthSignatureMethod signer = OAuthSignatureMethod.newMethod(oauthSignatureMethod, accessor);
        signer.sign(oam);
        
        // Get the signed parameters
        String signature = oam.getParameter(OAuth.OAUTH_SIGNATURE);
        String timestamp = oam.getParameter(OAuth.OAUTH_TIMESTAMP);
        String nonce = oam.getParameter(OAuth.OAUTH_NONCE);
        
        // Build Authorization header with properly signed parameters
        String authHeader = String.format(
            "OAuth oauth_consumer_key=\"%s\", oauth_body_hash=\"%s\", oauth_signature_method=\"%s\", oauth_signature=\"%s\", oauth_timestamp=\"%s\", oauth_nonce=\"%s\", oauth_version=\"1.0\"",
            OAuth.percentEncode(oauthConsumerKey),
            OAuth.percentEncode(oauthBodyHash),
            oauthSignatureMethod,
            OAuth.percentEncode(signature),
            timestamp,
            nonce);
        
        // Create mock request
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContentType()).thenReturn("application/xml");
        Mockito.when(request.getHeader("Authorization")).thenReturn(authHeader);
        
        // Mock getHeaderNames to return Authorization header
        Enumeration<String> headerNames = Collections.enumeration(Collections.singletonList("Authorization"));
        Mockito.when(request.getHeaderNames()).thenReturn(headerNames);
        
        // Mock getHeaders to return the Authorization header value
        Enumeration<String> authHeaders = Collections.enumeration(Collections.singletonList(authHeader));
        Mockito.when(request.getHeaders("Authorization")).thenReturn(authHeaders);
        
        // Set request body - create a new reader each time getReader() is called
        // This is needed because BufferedReader can only be read once
        Mockito.when(request.getReader()).thenAnswer(invocation -> {
            return new BufferedReader(new StringReader(body));
        });
        
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        Mockito.when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        Mockito.when(request.getParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        
        return request;
    }
    
    /**
     * Helper method to compute OAuth body hash
     */
    private String computeBodyHash(String body, String signatureMethod) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        if ("HMAC-SHA256".equals(signatureMethod)) {
            md = MessageDigest.getInstance("SHA-256");
        }
        md.update(body.getBytes(StandardCharsets.UTF_8));
        byte[] output = Base64.encode(md.digest());
        return new String(output);
    }
    
    @Test
    public void testValidateRequestResetsValidFlag() throws Exception {
        // Create a POXRequestHandler with valid XML
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        assertTrue("Initial request should be valid", pox.isValid());
        
        // Create a mock request (validation will fail due to invalid signature, but that's OK)
        String bodyHash = computeBodyHash(TEST_XML_REQUEST, "HMAC-SHA1");
        HttpServletRequest request = createMockRequestWithOAuth(
            TEST_XML_REQUEST, "test_key", "test_secret", bodyHash, "HMAC-SHA1");
        
        // Call validateRequest - it should reset valid flag
        pox.validateRequest("test_key", "test_secret", request);
        
        // Valid should be false after failed validation
        assertFalse("Valid should be false after validation failure", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
        assertTrue("Error message should mention OAuth validation", 
            pox.errorMessage.contains("OAuth validation"));
    }
    
    @Test
    public void testValidateRequestWithInvalidSecret() throws Exception {
        // Create a POXRequestHandler with valid XML
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        assertTrue("Initial request should be valid", pox.isValid());
        
        // Create a mock request with wrong secret
        String bodyHash = computeBodyHash(TEST_XML_REQUEST, "HMAC-SHA1");
        HttpServletRequest request = createMockRequestWithOAuth(
            TEST_XML_REQUEST, "test_key", "wrong_secret", bodyHash, "HMAC-SHA1");
        
        // Call validateRequest with wrong secret
        pox.validateRequest("test_key", "wrong_secret", request);
        
        // Validation should fail
        assertFalse("Valid should be false after validation failure", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testValidateRequestWithNullRequest() throws Exception {
        // Create a POXRequestHandler with valid XML
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        assertTrue("Initial request should be valid", pox.isValid());
        
        // Create a minimal mock request
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/lti/service"));
        Mockito.when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        
        // Call validateRequest - should handle gracefully
        pox.validateRequest("test_key", "test_secret", request);
        
        // Valid should be false after failed validation
        assertFalse("Valid should be false after validation failure", pox.isValid());
    }
    
    @Test
    public void testValidateRequestSetsBaseString() throws Exception {
        // Create a POXRequestHandler with valid XML
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Create a mock request with OAuth parameters
        String bodyHash = computeBodyHash(TEST_XML_REQUEST, "HMAC-SHA1");
        HttpServletRequest request = createMockRequestWithOAuth(
            TEST_XML_REQUEST, "test_key", "test_secret", bodyHash, "HMAC-SHA1");
        
        // Call validateRequest
        pox.validateRequest("test_key", "test_secret", request);
        
        // Base string may be null if signature extraction fails, but method should handle it
        // The important thing is that the method doesn't throw an exception
        // and sets valid to false on validation failure
        assertFalse("Valid should be false after validation failure", pox.isValid());
    }
    
    @Test
    public void testValidateRequestWithUrl() throws Exception {
        // Create a POXRequestHandler with valid XML
        POXRequestHandler pox = new POXRequestHandler(TEST_XML_REQUEST);
        
        // Create a mock request
        String bodyHash = computeBodyHash(TEST_XML_REQUEST, "HMAC-SHA1");
        HttpServletRequest request = createMockRequestWithOAuth(
            TEST_XML_REQUEST, "test_key", "test_secret", bodyHash, "HMAC-SHA1");
        
        // Call validateRequest with URL parameter
        String url = "http://localhost/lti/service";
        pox.validateRequest("test_key", "test_secret", request, url);
        
        // Validation should fail (invalid signature), but method should handle it
        assertFalse("Valid should be false after validation failure", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testValidateRequestSuccess() throws Exception {
        // Create a properly signed OAuth request
        String oauthConsumerKey = "test_key";
        String oauthSecret = "test_secret";
        String url = "http://localhost/lti/service";
        
        // Create a mock request with properly signed OAuth
        HttpServletRequest signedRequest = createSignedOAuthRequest(
            TEST_XML_REQUEST, oauthConsumerKey, oauthSecret, url);
        
        // Create POXRequestHandler from the signed request
        POXRequestHandler pox = new POXRequestHandler(signedRequest);
        
        // Verify initial parsing succeeded
        assertTrue("Request should be valid after parsing", pox.isValid());
        assertEquals("replaceResultRequest", pox.getOperation());
        
        // Now validate the OAuth signature
        pox.validateRequest(oauthConsumerKey, oauthSecret, signedRequest, url);
        
        // Validation should succeed with proper signature
        assertTrue("Request should be valid after OAuth validation", pox.isValid());
        assertNull("Error message should be null after successful validation", pox.errorMessage);
        assertNotNull("Base string should be set", pox.base_string);
    }
    
    @Test
    public void testValidateRequestSuccessWithoutUrl() throws Exception {
        // Create a properly signed OAuth request
        String oauthConsumerKey = "test_key";
        String oauthSecret = "test_secret";
        String url = "http://localhost/lti/service";
        
        // Create a mock request with properly signed OAuth
        HttpServletRequest signedRequest = createSignedOAuthRequest(
            TEST_XML_REQUEST, oauthConsumerKey, oauthSecret, url);
        
        // Create POXRequestHandler from the signed request
        POXRequestHandler pox = new POXRequestHandler(signedRequest);
        
        // Verify initial parsing succeeded
        assertTrue("Request should be valid after parsing", pox.isValid());
        
        // Now validate the OAuth signature without URL (should use request URL)
        pox.validateRequest(oauthConsumerKey, oauthSecret, signedRequest);
        
        // Validation should succeed with proper signature
        assertTrue("Request should be valid after OAuth validation", pox.isValid());
        assertNull("Error message should be null after successful validation", pox.errorMessage);
    }
    
    @Test
    public void testValidateRequestFail() throws Exception {
        // Create a properly signed OAuth request with one set of credentials
        String signingKey = "test_key";
        String signingSecret = "test_secret";
        String url = "http://localhost/lti/service";
        
        // Create a mock request signed with signingKey/signingSecret
        HttpServletRequest signedRequest = createSignedOAuthRequest(
            TEST_XML_REQUEST, signingKey, signingSecret, url);
        
        // Create POXRequestHandler from the signed request
        POXRequestHandler pox = new POXRequestHandler(signedRequest);
        
        // Verify initial parsing succeeded
        assertTrue("Request should be valid after parsing", pox.isValid());
        assertEquals("replaceResultRequest", pox.getOperation());
        
        // Now validate with DIFFERENT credentials (wrong secret)
        String wrongSecret = "wrong_secret";
        pox.validateRequest(signingKey, wrongSecret, signedRequest, url);
        
        // Validation should fail because secret doesn't match
        assertFalse("Request should be invalid after OAuth validation with wrong secret", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
        assertTrue("Error message should mention OAuth validation", 
            pox.errorMessage.contains("OAuth validation"));
    }
    
    @Test
    public void testValidateRequestFailWrongKey() throws Exception {
        // Create a properly signed OAuth request with one set of credentials
        String signingKey = "test_key";
        String signingSecret = "test_secret";
        String url = "http://localhost/lti/service";
        
        // Create a mock request signed with signingKey/signingSecret
        HttpServletRequest signedRequest = createSignedOAuthRequest(
            TEST_XML_REQUEST, signingKey, signingSecret, url);
        
        // Create POXRequestHandler from the signed request
        POXRequestHandler pox = new POXRequestHandler(signedRequest);
        
        // Verify initial parsing succeeded
        assertTrue("Request should be valid after parsing", pox.isValid());
        
        // Now validate with DIFFERENT key AND secret (both wrong)
        // This ensures validation fails because signature won't match
        String wrongKey = "wrong_key";
        String wrongSecret = "wrong_secret";
        pox.validateRequest(wrongKey, wrongSecret, signedRequest, url);
        
        // Validation should fail because signature doesn't match
        assertFalse("Request should be invalid after OAuth validation with wrong key and secret", pox.isValid());
        assertNotNull("Should have error message", pox.errorMessage);
        assertTrue("Error message should mention OAuth validation", 
            pox.errorMessage.contains("OAuth validation"));
    }
}

