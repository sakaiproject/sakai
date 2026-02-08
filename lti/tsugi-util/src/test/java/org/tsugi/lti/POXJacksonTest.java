package org.tsugi.lti;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;

import org.tsugi.lti.objects.*;
import org.tsugi.pox.POXResponseFactory;

public class POXJacksonTest {

    private static final String SAMPLE_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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

    private static final String SAMPLE_MEMBERSHIP_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXRequestHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>999999124</imsx_messageIdentifier>\n" +
            "</imsx_POXRequestHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<readMembershipRequest>\n" +
            "<sourcedId>123course456</sourcedId>\n" +
            "</readMembershipRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";

    private static final String SAMPLE_RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXResponseHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>999999125</imsx_messageIdentifier>\n" +
            "<imsx_statusInfo>\n" +
            "<imsx_codeMajor>success</imsx_codeMajor>\n" +
            "<imsx_severity>status</imsx_severity>\n" +
            "<imsx_description>Operation completed successfully</imsx_description>\n" +
            "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
            "<imsx_operationRefIdentifier>replaceResultRequest</imsx_operationRefIdentifier>\n" +
            "</imsx_statusInfo>\n" +
            "</imsx_POXResponseHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";

    @Test
    public void testParseRequest_Success() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_REQUEST_XML);
        
        assertNotNull("Request should not be null", request);
        assertNotNull("POX Header should not be null", request.getPoxHeader());
        assertNotNull("POX Body should not be null", request.getPoxBody());
        assertNotNull("Request Header Info should not be null", 
                     request.getPoxHeader().getRequestHeaderInfo());
        
        assertEquals("V1.0", request.getPoxHeader().getRequestHeaderInfo().getVersion());
        assertEquals("999999123", request.getPoxHeader().getRequestHeaderInfo().getMessageIdentifier());
    }

    @Test
    public void testParseRequest_ReplaceResultOperation() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_REQUEST_XML);
        
        assertNotNull("Request should not be null", request);
        assertEquals("replaceResultRequest", POXJacksonParser.getOperation(request));
        
        POXRequestBody body = request.getPoxBody();
        assertNotNull("Replace result request should not be null", body.getReplaceResultRequest());
        
        ReplaceResultRequest replaceRequest = body.getReplaceResultRequest();
        assertNotNull("Result record should not be null", replaceRequest.getResultRecord());
        
        ResultRecord record = replaceRequest.getResultRecord();
        assertNotNull("Sourced GUID should not be null", record.getSourcedGUID());
        assertEquals("3124567", record.getSourcedGUID().getSourcedId());
        
        assertNotNull("Result should not be null", record.getResult());
        assertNotNull("Result score should not be null", record.getResult().getResultScore());
        
        ResultScore score = record.getResult().getResultScore();
        assertEquals("en-us", score.getLanguage());
        assertEquals("A", score.getTextString());
    }

    @Test
    public void testParseRequest_ReadMembershipOperation() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_MEMBERSHIP_REQUEST_XML);
        
        assertNotNull("Request should not be null", request);
        assertEquals("readMembershipRequest", POXJacksonParser.getOperation(request));
        
        POXRequestBody body = request.getPoxBody();
        assertNotNull("Read membership request should not be null", body.getReadMembershipRequest());
        assertEquals("123course456", body.getReadMembershipRequest().getSourcedId());
    }

    @Test
    public void testParseRequest_NullInput() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(null);
        assertNull("Request should be null for null input", request);
    }

    @Test
    public void testParseRequest_EmptyInput() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest("");
        assertNull("Request should be null for empty input", request);
    }

    @Test
    public void testParseRequest_InvalidXML() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest("<invalid>xml</invalid>");
        assertNull("Request should be null for invalid XML", request);
    }

    @Test
    public void testParseResponse_Success() {
        POXEnvelopeResponse response = POXJacksonParser.parseResponse(SAMPLE_RESPONSE_XML);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("POX Header should not be null", response.getPoxHeader());
        assertNotNull("POX Body should not be null", response.getPoxBody());
        assertNotNull("Response Header Info should not be null", 
                     response.getPoxHeader().getResponseHeaderInfo());
        
        assertEquals("V1.0", response.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("999999125", response.getPoxHeader().getResponseHeaderInfo().getMessageIdentifier());
        
        POXStatusInfo statusInfo = response.getPoxHeader().getResponseHeaderInfo().getStatusInfo();
        assertNotNull("Status info should not be null", statusInfo);
        assertEquals("success", statusInfo.getCodeMajor());
        assertEquals("status", statusInfo.getSeverity());
        assertEquals("Operation completed successfully", statusInfo.getDescription());
    }

    @Test
    public void testParseResponse_NullInput() {
        POXEnvelopeResponse response = POXJacksonParser.parseResponse(null);
        assertNull("Response should be null for null input", response);
    }

    @Test
    public void testGetHeaderInfo() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_REQUEST_XML);
        Map<String, String> headerInfo = POXJacksonParser.getHeaderInfo(request);
        
        assertNotNull("Header info should not be null", headerInfo);
        assertEquals("V1.0", headerInfo.get("version"));
        assertEquals("999999123", headerInfo.get("messageIdentifier"));
    }

    @Test
    public void testGetBodyInfo_ReplaceResult() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_REQUEST_XML);
        
        String sourcedId = POXJacksonParser.getBodySourcedId(request);
        String language = POXJacksonParser.getBodyLanguage(request);
        String textString = POXJacksonParser.getBodyTextString(request);
        
        assertNotNull("SourcedId should not be null", sourcedId);
        assertEquals("3124567", sourcedId);
        assertNotNull("Language should not be null", language);
        assertEquals("en-us", language);
        assertNotNull("TextString should not be null", textString);
        assertEquals("A", textString);
    }

    @Test
    public void testGetBodyInfo_ReadMembership() {
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(SAMPLE_MEMBERSHIP_REQUEST_XML);
        
        String sourcedId = POXJacksonParser.getBodySourcedId(request);
        
        assertNotNull("SourcedId should not be null", sourcedId);
        assertEquals("123course456", sourcedId);
    }

    @Test
    public void testCreateSuccessResponse() {
        String response = POXResponseFactory.createSuccessResponse("Success", "123", "testOp");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain status severity", response.contains("status"));
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
    }

    @Test
    public void testCreateFailureResponse() {
        Properties minorCodes = new Properties();
        minorCodes.setProperty("error1", "invaliddata");
        minorCodes.setProperty("error2", "incompletedata");
        
        String response = POXResponseFactory.createFailureResponse("Failure", minorCodes, "123", "testOp");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain minor codes", response.contains("error1"));
    }

    @Test
    public void testCreateUnsupportedResponse() {
        String response = POXResponseFactory.createUnsupportedResponse("Unsupported", "123", "testOp");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain unsupported", response.contains("unsupported"));
        assertTrue("Response should contain error severity", response.contains("error"));
    }

    @Test
    public void testParseRequest_EmptyBody() {
        String emptyBodyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<imsx_POXEnvelopeRequest xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                "<imsx_POXHeader>\n" +
                "<imsx_POXRequestHeaderInfo>\n" +
                "<imsx_version>V1.0</imsx_version>\n" +
                "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
                "</imsx_POXRequestHeaderInfo>\n" +
                "</imsx_POXHeader>\n" +
                "<imsx_POXBody>\n" +
                "</imsx_POXBody>\n" +
                "</imsx_POXEnvelopeRequest>";
        
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(emptyBodyXml);
        assertNotNull("Request should not be null", request);
        assertNull("Operation should be null for empty body", POXJacksonParser.getOperation(request));
    }

    @Test
    public void testParseRequest_UnknownOperation() {
        String unknownOpXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<imsx_POXEnvelopeRequest xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
                "<imsx_POXHeader>\n" +
                "<imsx_POXRequestHeaderInfo>\n" +
                "<imsx_version>V1.0</imsx_version>\n" +
                "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
                "</imsx_POXRequestHeaderInfo>\n" +
                "</imsx_POXHeader>\n" +
                "<imsx_POXBody>\n" +
                "<unknownOperation>\n" +
                "<data>test</data>\n" +
                "</unknownOperation>\n" +
                "</imsx_POXBody>\n" +
                "</imsx_POXEnvelopeRequest>";
        
        POXEnvelopeRequest request = POXJacksonParser.parseRequest(unknownOpXml);
        assertNotNull("Request should not be null", request);
        assertNull("Operation should be null for unknown operation", POXJacksonParser.getOperation(request));
    }

    @Test
    public void testCreateResponse_WithMinorCodes() {
        Properties minorCodes = new Properties();
        minorCodes.setProperty("field1", "invaliddata");
        minorCodes.setProperty("field2", "incompletedata");
        
        String response = POXResponseFactory.createCustomResponse("Test", "failure", "error", "123", "testOp", 
                                                        minorCodes);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain minor codes", response.contains("field1"));
        assertTrue("Response should contain minor codes", response.contains("invaliddata"));
    }

    @Test
    public void testCreateResponse_WithSpecialCharacters() {
        String description = "Test with <special> & \"characters\"";
        String response = POXResponseFactory.createCustomResponse(description, "failure", "error", "123", "testOp", 
                                                        null);
        
        assertNotNull("Response should not be null", response);
        // Jackson's XmlMapper automatically escapes XML special characters
        // Verify the response is valid XML by parsing it back
        POXEnvelopeResponse parsedResponse = POXJacksonParser.parseResponse(response);
        assertNotNull("Response should be valid XML and parseable", parsedResponse);
        assertNotNull("Status info should not be null", parsedResponse.getPoxHeader().getResponseHeaderInfo().getStatusInfo());
        
        // Verify the description was preserved correctly (Jackson will have escaped it)
        String parsedDescription = parsedResponse.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription();
        assertNotNull("Description should not be null", parsedDescription);
        assertEquals("Description should match original", description, parsedDescription);
        
        // Verify the response contains the description element
        assertTrue("Response should contain description element", response.contains("imsx_description"));
        // Verify special characters are escaped in the XML (check for escaped forms)
        assertTrue("Response should contain escaped ampersand", response.contains("&amp;"));
        // Extract description content to check for XML escaping of < and >
        int start = response.indexOf("<imsx_description>");
        int end = response.indexOf("</imsx_description>");
        assertTrue(start >= 0 && end > start);
        
        String serializedDesc = response.substring(start, end);
        
        // Must escape '<' in text content
        assertTrue("Expected '&lt;' in serialized description", serializedDesc.contains("&lt;"));
        
        // Verify raw unescaped markup does NOT appear
        assertFalse("Raw '<special>' must not appear in serialized description", 
                serializedDesc.contains("<special>"));
        assertFalse("Raw unescaped description must not appear", 
                serializedDesc.contains("Test with <special> & \"characters\""));
        
        // Verify escaped forms ARE present
        // Jackson escapes '<' to '&lt;' but may not escape '>' in text content
        assertTrue("Expected '&lt;special' in serialized description", 
                serializedDesc.contains("&lt;special"));
        assertTrue("Expected '&amp;' in serialized description", 
                serializedDesc.contains("&amp;"));
        // Quotes don't need to be escaped in XML element text content (only in attributes)
        // Verify quotes appear as literal quotes (not escaped)
        assertTrue("Expected literal quotes in serialized description", 
                serializedDesc.contains("\""));
    }

    @Test
    public void testPerformance_Parsing() {
        int iterations = 100;
        
        long jacksonStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            POXEnvelopeRequest jacksonRequest = POXJacksonParser.parseRequest(SAMPLE_REQUEST_XML);
            String operation = POXJacksonParser.getOperation(jacksonRequest);
            Map<String, String> headerInfo = POXJacksonParser.getHeaderInfo(jacksonRequest);
            assertNotNull("Header info should contain version", headerInfo.get("version"));
        }
        long jacksonTime = System.nanoTime() - jacksonStart;
        
        System.out.println("Jackson parsing time: " + jacksonTime + " ns");
        
        assertTrue("Jackson method should complete", jacksonTime > 0);
    }
}
