package org.tsugi.pox;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.Map;

import org.junit.Test;

import org.tsugi.lti.objects.POXEnvelopeRequest;
import org.tsugi.lti.POXJacksonParser;

public class IMSPOXRequestJacksonTest {
    
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
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertTrue("Request should be valid", pox.valid);
        assertEquals("replaceResultRequest", pox.getOperation());
        assertEquals("V1.0", pox.getHeaderVersion());
        assertEquals("999999123", pox.getHeaderMessageIdentifier());
    }
    
    @Test
    public void testGetOperation() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertEquals("replaceResultRequest", pox.getOperation());
    }
    
    @Test
    public void testGetHeaderVersion() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertEquals("V1.0", pox.getHeaderVersion());
    }
    
    @Test
    public void testGetHeaderMessageIdentifier() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertEquals("999999123", pox.getHeaderMessageIdentifier());
    }
    
    @Test
    public void testGetHeaderItem() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertEquals("V1.0", pox.getHeaderItem("/imsx_version"));
        assertEquals("999999123", pox.getHeaderItem("/imsx_messageIdentifier"));
        assertNull(pox.getHeaderItem("/nonexistent"));
    }
    
    @Test
    public void testGetHeaderMap() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        Map<String, String> headerMap = pox.getHeaderMap();
        
        assertNotNull("Header map should not be null", headerMap);
        assertEquals("V1.0", headerMap.get("version"));
        assertEquals("999999123", headerMap.get("messageIdentifier"));
    }
    
    @Test
    public void testGetBodyMap() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
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
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertEquals(TEST_XML_REQUEST, pox.getPostBody());
    }
    
    @Test
    public void testGetPoxRequest() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        POXEnvelopeRequest request = pox.getPoxRequest();
        
        assertNotNull("POX request should not be null", request);
        assertNotNull("POX header should not be null", request.getPoxHeader());
        assertNotNull("POX body should not be null", request.getPoxBody());
    }
    
    @Test
    public void testGetResponseSuccess() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        String response = pox.getResponseSuccess("Test success", "<test>body</test>");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test success"));
        assertTrue("Response should contain body", response.contains("<test>body</test>"));
    }
    
    @Test
    public void testGetResponseFailure() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
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
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        String response = pox.getResponseUnsupported("Test unsupported");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain unsupported", response.contains("unsupported"));
        assertTrue("Response should contain description", response.contains("Test unsupported"));
    }
    
    @Test
    public void testInArray() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        
        assertTrue("Should find valid major code", pox.inArray(IMSPOXRequestJackson.validMajor, "success"));
        assertTrue("Should find valid severity", pox.inArray(IMSPOXRequestJackson.validSeverity, "error"));
        assertTrue("Should find valid minor code", pox.inArray(IMSPOXRequestJackson.validMinor, "invaliddata"));
        assertFalse("Should not find invalid code", pox.inArray(IMSPOXRequestJackson.validMajor, "invalid"));
        assertFalse("Should not find null code", pox.inArray(IMSPOXRequestJackson.validMajor, null));
    }
    
    @Test
    public void testGetFatalResponse() {
        String response = IMSPOXRequestJackson.getFatalResponse("Test fatal error");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test fatal error"));
    }
    
    @Test
    public void testGetFatalResponseWithMessageId() {
        String response = IMSPOXRequestJackson.getFatalResponse("Test fatal error", "test123");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test fatal error"));
        assertTrue("Response should contain message ID", response.contains("test123"));
    }
    
    @Test
    public void testRunTest() {
        IMSPOXRequestJackson.runTest();
    }
    
    @Test
    public void testInvalidXml() {
        String invalidXml = "<?xml version=\"1.0\"?><invalid>content</invalid>";
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(invalidXml);
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testEmptyXml() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson("");
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testNullXml() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson((String) null);
        
        assertFalse("Request should not be valid", pox.valid);
        assertNotNull("Should have error message", pox.errorMessage);
    }
    
    @Test
    public void testResponseWithMinorCodes() {
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
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
        IMSPOXRequestJackson pox = new IMSPOXRequestJackson(TEST_XML_REQUEST);
        Properties minorCodes = new Properties();
        minorCodes.setProperty("field1", "invalidcode");
        minorCodes.setProperty("field2", "invaliddata");
        
        String response = pox.getResponse("Test response", "failure", "error", "msg123", 
                                        minorCodes, null);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain internal error", response.contains("Internal error"));
    }
}

