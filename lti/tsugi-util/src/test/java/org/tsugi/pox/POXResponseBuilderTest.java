package org.tsugi.pox;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import org.tsugi.lti.POXResponseBuilder;
import org.tsugi.lti.POXResponseHelper;
import org.tsugi.lti.objects.POXEnvelopeResponse;
import org.tsugi.lti.objects.POXCodeMinorField;

public class POXResponseBuilderTest {
    
    @Test
    public void testCreateSuccessResponse() {
        // Most POX success responses don't have body content (e.g., replaceResultResponse is empty)
        String response = POXResponseBuilder.create()
            .withDescription("Test success")
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asSuccess()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain status severity", response.contains("status"));
        assertTrue("Response should contain description", response.contains("Test success"));
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
    }
    
    @Test
    public void testCreateFailureResponse() {
        Properties minorCodes = new Properties();
        minorCodes.setProperty("error1", "invaliddata");
        minorCodes.setProperty("error2", "incompletedata");
        
        String response = POXResponseBuilder.create()
            .withDescription("Test failure")
            .withMinorCodes(minorCodes)
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asFailure()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test failure"));
        assertTrue("Response should contain minor codes", response.contains("imsx_codeMinor"));
    }
    
    @Test
    public void testCreateUnsupportedResponse() {
        String response = POXResponseBuilder.create()
            .withDescription("Test unsupported")
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asUnsupported()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain unsupported", response.contains("unsupported"));
        assertTrue("Response should contain error severity", response.contains("error"));
        assertTrue("Response should contain description", response.contains("Test unsupported"));
    }
    
    @Test
    public void testCreateProcessingResponse() {
        String response = POXResponseBuilder.create()
            .withDescription("Test processing")
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asProcessing()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain processing", response.contains("processing"));
        assertTrue("Response should contain status severity", response.contains("status"));
        assertTrue("Response should contain description", response.contains("Test processing"));
    }
    
    @Test
    public void testCreateResponseWithMinorFields() {
        List<POXCodeMinorField> minorFields = new ArrayList<>();
        
        POXCodeMinorField field1 = new POXCodeMinorField();
        field1.setFieldName("error1");
        field1.setFieldValue("invaliddata");
        minorFields.add(field1);
        
        POXCodeMinorField field2 = new POXCodeMinorField();
        field2.setFieldName("error2");
        field2.setFieldValue("incompletedata");
        minorFields.add(field2);
        
        String response = POXResponseBuilder.create()
            .withDescription("Test with minor fields")
            .withMinorCodes(minorFields)
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asFailure()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain minor codes", response.contains("imsx_codeMinor"));
        assertTrue("Response should contain error1", response.contains("error1"));
        assertTrue("Response should contain error2", response.contains("error2"));
    }
    
    @Test
    public void testBuildResponseObject() {
        // Most POX responses don't have body content - replaceResultResponse is empty
        POXEnvelopeResponse response = POXResponseBuilder.create()
            .withDescription("Test response object")
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asSuccess()
            .build();
        
        assertNotNull("Response object should not be null", response);
        assertNotNull("Response header should not be null", response.getPoxHeader());
        assertNotNull("Response body should not be null", response.getPoxBody());
        assertNotNull("Response header info should not be null", response.getPoxHeader().getResponseHeaderInfo());
        assertNotNull("Response status info should not be null", response.getPoxHeader().getResponseHeaderInfo().getStatusInfo());
        
        assertEquals("V1.0", response.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("msg123", response.getPoxHeader().getResponseHeaderInfo().getMessageIdentifier());
        assertEquals("success", response.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor());
        assertEquals("status", response.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getSeverity());
        assertEquals("Test response object", response.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription());
    }
    
    @Test
    public void testCreateSuccessResponseStatic() {
        // Most POX success responses don't have body content
        String response = POXResponseHelper.createSuccessResponse(
            "Test success", "msg123", "replaceResultRequest");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test success"));
    }
    
    @Test
    public void testCreateFailureResponseStatic() {
        Properties minorCodes = new Properties();
        minorCodes.setProperty("error1", "invaliddata");
        
        String response = POXResponseHelper.createFailureResponse(
            "Test failure", minorCodes, "msg123", "replaceResultRequest");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain failure", response.contains("failure"));
        assertTrue("Response should contain description", response.contains("Test failure"));
    }
    
    @Test
    public void testCreateUnsupportedResponseStatic() {
        String response = POXResponseHelper.createUnsupportedResponse(
            "Test unsupported", "msg123", "replaceResultRequest");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain unsupported", response.contains("unsupported"));
        assertTrue("Response should contain description", response.contains("Test unsupported"));
    }
    
    @Test
    public void testCreateProcessingResponseStatic() {
        String response = POXResponseHelper.createProcessingResponse(
            "Test processing", "msg123", "replaceResultRequest");
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain processing", response.contains("processing"));
        assertTrue("Response should contain description", response.contains("Test processing"));
    }
    
    @Test
    public void testResponseWithNullValues() {
        String response = POXResponseBuilder.create()
            .withDescription("Test with nulls")
            .withMessageId(null) 
            .withOperation("replaceResultRequest")
            .asSuccess()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain description", response.contains("Test with nulls"));
        assertTrue("Response should contain message identifier", response.contains("imsx_messageIdentifier"));
    }
    
    @Test
    public void testResponseWithEmptyBody() {
        // Empty body is the normal case for most POX responses
        String response = POXResponseBuilder.create()
            .withDescription("Test empty body")
            .withMessageId("msg123")
            .withOperation("replaceResultRequest")
            .asSuccess()
            .buildAsXml();
        
        assertNotNull("Response should not be null", response);
        assertTrue("Response should contain success", response.contains("success"));
        assertTrue("Response should contain POX body", response.contains("imsx_POXBody"));
    }
}

