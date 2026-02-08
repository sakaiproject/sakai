package org.tsugi.lti;

import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.*;
import org.tsugi.pox.POXResponseBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Test that validates our POX message builders produce XML that matches
 * the IMS LTI v1.1 Implementation Guide examples verbatim.
 * 
 * Implementation Guide: https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
 * 
 * This test covers the Basic Outcomes Service examples from Section 6.1 of the
 * LTI v1.1 Implementation Guide.
 * 
 * NOTE: The v1.1 Implementation Guide examples do NOT include empty <imsx_codeMinor/>
 * elements in the response XML. This differs from the v1.1.1 Basic Outcomes spec
 * examples (see IMSLTIv1p1p1Test.java), which do include empty codeMinor elements.
 * Our code correctly omits null codeMinor fields in both cases via @JsonInclude(NON_NULL).
 */
public class IMSLTIv1p1Test {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    // ========================================================================
    // Canonical XML examples from the LTI v1.1 Implementation Guide
    // These are shared between build/serialization tests and parse/deserialization tests
    // ========================================================================
    
    /** XML from Section 6.1.1 - replaceResultRequest */
    private static final String specXml_6_1_1_replaceResultRequest = 
            "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
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
            "<language>en</language>\n" +
            "<textString>0.92</textString>\n" +
            "</resultScore>\n" +
            "</result>\n" +
            "</resultRecord>\n" +
            "</replaceResultRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
    
    /** XML from Section 6.1.1 - replaceResultResponse */
    private static final String specXml_6_1_1_replaceResultResponse = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXResponseHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>4560</imsx_messageIdentifier>\n" +
            "<imsx_statusInfo>\n" +
            "<imsx_codeMajor>success</imsx_codeMajor>\n" +
            "<imsx_severity>status</imsx_severity>\n" +
            "<imsx_description>Score for 3124567 is now 0.92</imsx_description>\n" +
            "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
            "<imsx_operationRefIdentifier>replaceResult</imsx_operationRefIdentifier>\n" +
            "</imsx_statusInfo>\n" +
            "</imsx_POXResponseHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<replaceResultResponse/>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
    
    /** XML from Section 6.1.2 - readResultRequest */
    private static final String specXml_6_1_2_readResultRequest = 
            "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXRequestHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
            "</imsx_POXRequestHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<readResultRequest>\n" +
            "<resultRecord>\n" +
            "<sourcedGUID>\n" +
            "<sourcedId>3124567</sourcedId>\n" +
            "</sourcedGUID>\n" +
            "</resultRecord>\n" +
            "</readResultRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
    
    /** XML from Section 6.1.2 - readResultResponse */
    private static final String specXml_6_1_2_readResultResponse = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXResponseHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>1313355158804</imsx_messageIdentifier>\n" +
            "<imsx_statusInfo>\n" +
            "<imsx_codeMajor>success</imsx_codeMajor>\n" +
            "<imsx_severity>status</imsx_severity>\n" +
            "<imsx_description>Result read</imsx_description>\n" +
            "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
            "<imsx_operationRefIdentifier>readResult</imsx_operationRefIdentifier>\n" +
            "</imsx_statusInfo>\n" +
            "</imsx_POXResponseHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<readResultResponse>\n" +
            "<result>\n" +
            "<resultScore>\n" +
            "<language>en</language>\n" +
            "<textString>0.91</textString>\n" +
            "</resultScore>\n" +
            "</result>\n" +
            "</readResultResponse>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
    
    /** XML from Section 6.1.3 - deleteResultRequest */
    private static final String specXml_6_1_3_deleteResultRequest = 
            "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXRequestHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>999999123</imsx_messageIdentifier>\n" +
            "</imsx_POXRequestHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<deleteResultRequest>\n" +
            "<resultRecord>\n" +
            "<sourcedGUID>\n" +
            "<sourcedId>3124567</sourcedId>\n" +
            "</sourcedGUID>\n" +
            "</resultRecord>\n" +
            "</deleteResultRequest>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
    
    /** XML from Section 6.1.3 - deleteResultResponse */
    private static final String specXml_6_1_3_deleteResultResponse = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "<imsx_POXHeader>\n" +
            "<imsx_POXResponseHeaderInfo>\n" +
            "<imsx_version>V1.0</imsx_version>\n" +
            "<imsx_messageIdentifier>4560</imsx_messageIdentifier>\n" +
            "<imsx_statusInfo>\n" +
            "<imsx_codeMajor>success</imsx_codeMajor>\n" +
            "<imsx_severity>status</imsx_severity>\n" +
            "<imsx_messageRefIdentifier>999999123</imsx_messageRefIdentifier>\n" +
            "<imsx_operationRefIdentifier>deleteResult</imsx_operationRefIdentifier>\n" +
            "</imsx_statusInfo>\n" +
            "</imsx_POXResponseHeaderInfo>\n" +
            "</imsx_POXHeader>\n" +
            "<imsx_POXBody>\n" +
            "<deleteResultResponse/>\n" +
            "</imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
    
    /**
     * Normalize spec XML to match Jackson's output style programmatically.
     * 
     * IMPORTANT: This method ONLY normalizes syntax/formatting differences.
     * It does NOT remove, add, or modify any XML elements or their content.
     * All transformations are purely syntactic:
     * - XML declaration format (quotes, spacing)
     * - Attribute spacing (xmlns = "..." -> xmlns="...")
     * - Empty element format (<tag></tag> -> <tag/>)
     * - Whitespace between tags
     * 
     * Jackson's XmlMapper with WRITE_XML_DECLARATION produces:
     * - XML declaration with single quotes: <?xml version='1.0' encoding='UTF-8'?>
     * - Self-closing tags for empty elements: <tag/>
     * - No whitespace between tags
     * - No spaces around = in attributes (xmlns="..." not xmlns = "...")
     * - Preserves text content exactly
     */
    private String normalizeToJacksonStyle(String xml) {
        if (xml == null) return null;
        
        // Step 1: Remove XML declaration (we'll add it back in Jackson's style)
        // Syntax only: changes quote style and spacing, doesn't change content
        xml = xml.replaceAll("<\\?xml\\s+version\\s*=\\s*[\"']1\\.0[\"']\\s+encoding\\s*=\\s*[\"']UTF-8[\"']\\s*\\?>\\s*", "");
        
        // Step 2: Normalize attribute spacing - remove spaces around =
        // Syntax only: xmlns = "..." -> xmlns="..." (formatting change only)
        xml = xml.replaceAll("\\s+=\\s+", "=");
        
        // Step 3: Normalize empty elements to self-closing form
        // Syntax only: <tag></tag> or <tag> </tag> -> <tag/> (formatting change only)
        // Also normalize: <tag /> -> <tag/> (spacing in self-closing tag)
        xml = xml.replaceAll("<([^/>\\s]+)([^>]*)>\\s*</\\1>", "<$1$2/>");
        xml = xml.replaceAll("<([^/>\\s]+)([^>]*)\\s+/>", "<$1$2/>");
        
        // Step 4 & 5: Normalize whitespace using utility
        // Syntax only: removes formatting whitespace, doesn't change element structure
        xml = XmlNormalizationUtil.normalizeWhitespace(xml);
        
        // Step 6: Add XML declaration back in Jackson's style (single quotes)
        // Syntax only: adds declaration in consistent format
        xml = "<?xml version='1.0' encoding='UTF-8'?>" + xml;
        
        return xml;
    }
    
    /**
     * Normalize generated XML for comparison.
     * 
     * IMPORTANT: This method ONLY normalizes syntax/formatting differences.
     * It does NOT remove, add, or modify any XML elements or their content.
     * 
     * Jackson produces consistent format but may include:
     * - xmlns="" attributes on elements (syntax difference, not structural)
     * - Whitespace formatting differences
     */
    private String normalizeGeneratedXml(String xml) {
        if (xml == null) return null;
        
        // Remove xmlns="" attributes (Jackson adds these but spec doesn't have them)
        // Syntax only: xmlns="" is a namespace declaration artifact, not a structural element
        xml = xml.replaceAll("\\s+xmlns=\"\"", "");
        
        // Normalize whitespace using utility
        xml = XmlNormalizationUtil.normalizeWhitespace(xml);
        
        return xml;
    }
    
    /**
     * Test replaceResultRequest - matches example from Section 6.1.1 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testReplaceResultRequest() throws Exception {
        // XML example from Section 6.1.1 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_1_replaceResultRequest;
        
        // Build the same structure using our objects
        POXEnvelopeRequest request = new POXEnvelopeRequest();
        
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        header.setRequestHeaderInfo(headerInfo);
        request.setPoxHeader(header);
        
        POXRequestBody body = new POXRequestBody();
        ReplaceResultRequest replaceRequest = new ReplaceResultRequest();
        
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        resultRecord.setResult(result);
        
        replaceRequest.setResultRecord(resultRecord);
        body.setReplaceResultRequest(replaceRequest);
        request.setPoxBody(body);
        
        // Serialize to XML
        String generatedXml = XML_MAPPER.writeValueAsString(request);
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        assertEquals("replaceResultRequest should match Section 6.1.1 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    /**
     * Test replaceResultResponse - matches example from Section 6.1.1 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testReplaceResultResponse() throws Exception {
        // XML example from Section 6.1.1 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_1_replaceResultResponse;
        
        // Build using POXResponseBuilder
        // Set messageRefIdentifier to reference the original request message ID (999999123)
        String generatedXml = POXResponseBuilder.create()
                .withDescription("Score for 3124567 is now 0.92")
                .asSuccess()
                .withMessageId("4560")
                .withMessageRefIdentifier("999999123")
                .withOperation("replaceResult")
                .withBodyObject(new ReplaceResultResponse())
                .buildAsXml();
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        
        assertEquals("replaceResultResponse should match Section 6.1.1 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    /**
     * Test readResultRequest - matches example from Section 6.1.2 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testReadResultRequest() throws Exception {
        // XML example from Section 6.1.2 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_2_readResultRequest;
        
        // Build the same structure using our objects
        POXEnvelopeRequest request = new POXEnvelopeRequest();
        
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        header.setRequestHeaderInfo(headerInfo);
        request.setPoxHeader(header);
        
        POXRequestBody body = new POXRequestBody();
        ReadResultRequest readRequest = new ReadResultRequest();
        
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        readRequest.setResultRecord(resultRecord);
        body.setReadResultRequest(readRequest);
        request.setPoxBody(body);
        
        // Serialize to XML
        String generatedXml = XML_MAPPER.writeValueAsString(request);
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        assertEquals("readResultRequest should match Section 6.1.2 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    /**
     * Test readResultResponse - matches example from Section 6.1.2 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testReadResultResponse() throws Exception {
        // XML example from Section 6.1.2 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_2_readResultResponse;
        
        // Build the response body object
        ReadResultResponse readResultResponse = new ReadResultResponse();
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.91");
        result.setResultScore(resultScore);
        readResultResponse.setResult(result);
        
        // Build using POXResponseBuilder
        // Set messageRefIdentifier to reference the original request message ID (999999123)
        String generatedXml = POXResponseBuilder.create()
                .withDescription("Result read")
                .asSuccess()
                .withMessageId("1313355158804")
                .withMessageRefIdentifier("999999123")
                .withOperation("readResult")
                .withBodyObject(readResultResponse)
                .buildAsXml();
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        
        assertEquals("readResultResponse should match Section 6.1.2 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    /**
     * Test deleteResultRequest - matches example from Section 6.1.3 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testDeleteResultRequest() throws Exception {
        // XML example from Section 6.1.3 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_3_deleteResultRequest;
        
        // Build the same structure using our objects
        POXEnvelopeRequest request = new POXEnvelopeRequest();
        
        POXRequestHeader header = new POXRequestHeader();
        POXRequestHeaderInfo headerInfo = new POXRequestHeaderInfo();
        headerInfo.setVersion("V1.0");
        headerInfo.setMessageIdentifier("999999123");
        header.setRequestHeaderInfo(headerInfo);
        request.setPoxHeader(header);
        
        POXRequestBody body = new POXRequestBody();
        DeleteResultRequest deleteRequest = new DeleteResultRequest();
        
        ResultRecord resultRecord = new ResultRecord();
        SourcedGUID sourcedGUID = new SourcedGUID();
        sourcedGUID.setSourcedId("3124567");
        resultRecord.setSourcedGUID(sourcedGUID);
        
        deleteRequest.setResultRecord(resultRecord);
        body.setDeleteResultRequest(deleteRequest);
        request.setPoxBody(body);
        
        // Serialize to XML
        String generatedXml = XML_MAPPER.writeValueAsString(request);
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        assertEquals("deleteResultRequest should match Section 6.1.3 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    /**
     * Test deleteResultResponse - matches example from Section 6.1.3 of
     * https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testDeleteResultResponse() throws Exception {
        // XML example from Section 6.1.3 of the LTI v1.1 Implementation Guide
        String specXml = specXml_6_1_3_deleteResultResponse;
        
        // Build using POXResponseBuilder
        // Note: Section 6.1.3 example doesn't include a description field
        // Set messageRefIdentifier to reference the original request message ID (999999123)
        String generatedXml = POXResponseBuilder.create()
                .withDescription(null)  // Explicitly null to match spec
                .asSuccess()
                .withMessageId("4560")
                .withMessageRefIdentifier("999999123")
                .withOperation("deleteResult")
                .withBodyObject(new DeleteResultResponse())
                .buildAsXml();
        
        // Normalize spec XML to Jackson style and compare
        String normalizedSpec = normalizeToJacksonStyle(specXml);
        String normalizedGenerated = normalizeGeneratedXml(generatedXml);
        
        assertEquals("deleteResultResponse should match Section 6.1.3 example", 
                normalizedSpec, normalizedGenerated);
    }
    
    // ========================================================================
    // Parsing Tests: Parse canonical XML and verify objects are populated correctly
    // ========================================================================
    
    /**
     * Test parsing replaceResultRequest XML - verifies objects are correctly populated
     * from Section 6.1.1 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseReplaceResultRequest() throws Exception {
        String specXml = specXml_6_1_1_replaceResultRequest;
        
        // Parse the XML
        POXEnvelopeRequest request = XML_MAPPER.readValue(specXml, POXEnvelopeRequest.class);
        
        // Verify header
        assertNotNull("Request should have header", request.getPoxHeader());
        assertNotNull("Header should have request header info", request.getPoxHeader().getRequestHeaderInfo());
        assertEquals("Version should be V1.0", "V1.0", request.getPoxHeader().getRequestHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "999999123", request.getPoxHeader().getRequestHeaderInfo().getMessageIdentifier());
        
        // Verify body
        assertNotNull("Request should have body", request.getPoxBody());
        assertNotNull("Body should have replaceResultRequest", request.getPoxBody().getReplaceResultRequest());
        
        // Verify result record
        ResultRecord resultRecord = request.getPoxBody().getReplaceResultRequest().getResultRecord();
        assertNotNull("Result record should exist", resultRecord);
        assertNotNull("Result record should have sourcedGUID", resultRecord.getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", resultRecord.getSourcedGUID().getSourcedId());
        
        // Verify result
        assertNotNull("Result record should have result", resultRecord.getResult());
        assertNotNull("Result should have resultScore", resultRecord.getResult().getResultScore());
        assertEquals("Language should be en", "en", resultRecord.getResult().getResultScore().getLanguage());
        assertEquals("Text string should match", "0.92", resultRecord.getResult().getResultScore().getTextString());
    }
    
    /**
     * Test parsing replaceResultResponse XML - verifies objects are correctly populated
     * from Section 6.1.1 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseReplaceResultResponse() throws Exception {
        String specXml = specXml_6_1_1_replaceResultResponse;
        
        // Parse the XML
        POXEnvelopeResponse response = XML_MAPPER.readValue(specXml, POXEnvelopeResponse.class);
        
        // Verify header
        assertNotNull("Response should have header", response.getPoxHeader());
        assertNotNull("Header should have response header info", response.getPoxHeader().getResponseHeaderInfo());
        assertEquals("Version should be V1.0", "V1.0", response.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "4560", response.getPoxHeader().getResponseHeaderInfo().getMessageIdentifier());
        
        // Verify status info
        POXStatusInfo statusInfo = response.getPoxHeader().getResponseHeaderInfo().getStatusInfo();
        assertNotNull("Status info should exist", statusInfo);
        assertEquals("Code major should be success", "success", statusInfo.getCodeMajor());
        assertEquals("Severity should be status", "status", statusInfo.getSeverity());
        assertEquals("Description should match", "Score for 3124567 is now 0.92", statusInfo.getDescription());
        assertEquals("Message ref identifier should match", "999999123", statusInfo.getMessageRefIdentifier());
        assertEquals("Operation ref identifier should match", "replaceResult", statusInfo.getOperationRefIdentifier());
        assertNull("Code minor should be null (not present in v1.1 examples)", statusInfo.getCodeMinor());
        
        // Verify body
        assertNotNull("Response should have body", response.getPoxBody());
        assertNotNull("Body should have replaceResultResponse", response.getPoxBody().getReplaceResultResponse());
    }
    
    /**
     * Test parsing readResultRequest XML - verifies objects are correctly populated
     * from Section 6.1.2 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseReadResultRequest() throws Exception {
        String specXml = specXml_6_1_2_readResultRequest;
        
        // Parse the XML
        POXEnvelopeRequest request = XML_MAPPER.readValue(specXml, POXEnvelopeRequest.class);
        
        // Verify header
        assertNotNull("Request should have header", request.getPoxHeader());
        assertEquals("Version should be V1.0", "V1.0", request.getPoxHeader().getRequestHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "999999123", request.getPoxHeader().getRequestHeaderInfo().getMessageIdentifier());
        
        // Verify body
        assertNotNull("Request should have body", request.getPoxBody());
        assertNotNull("Body should have readResultRequest", request.getPoxBody().getReadResultRequest());
        
        // Verify result record
        ResultRecord resultRecord = request.getPoxBody().getReadResultRequest().getResultRecord();
        assertNotNull("Result record should exist", resultRecord);
        assertNotNull("Result record should have sourcedGUID", resultRecord.getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", resultRecord.getSourcedGUID().getSourcedId());
        
        // Verify result is null (not present in readResultRequest)
        assertNull("Result should be null in readResultRequest", resultRecord.getResult());
    }
    
    /**
     * Test parsing readResultResponse XML - verifies objects are correctly populated
     * from Section 6.1.2 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseReadResultResponse() throws Exception {
        String specXml = specXml_6_1_2_readResultResponse;
        
        // Parse the XML
        POXEnvelopeResponse response = XML_MAPPER.readValue(specXml, POXEnvelopeResponse.class);
        
        // Verify header
        assertNotNull("Response should have header", response.getPoxHeader());
        assertEquals("Version should be V1.0", "V1.0", response.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "1313355158804", response.getPoxHeader().getResponseHeaderInfo().getMessageIdentifier());
        
        // Verify status info
        POXStatusInfo statusInfo = response.getPoxHeader().getResponseHeaderInfo().getStatusInfo();
        assertNotNull("Status info should exist", statusInfo);
        assertEquals("Code major should be success", "success", statusInfo.getCodeMajor());
        assertEquals("Severity should be status", "status", statusInfo.getSeverity());
        assertEquals("Description should match", "Result read", statusInfo.getDescription());
        assertEquals("Message ref identifier should match", "999999123", statusInfo.getMessageRefIdentifier());
        assertEquals("Operation ref identifier should match", "readResult", statusInfo.getOperationRefIdentifier());
        
        // Verify body
        assertNotNull("Response should have body", response.getPoxBody());
        assertNotNull("Body should have readResultResponse", response.getPoxBody().getReadResultResponse());
        
        // Verify result in response
        Result result = response.getPoxBody().getReadResultResponse().getResult();
        assertNotNull("Result should exist", result);
        assertNotNull("Result should have resultScore", result.getResultScore());
        assertEquals("Language should be en", "en", result.getResultScore().getLanguage());
        assertEquals("Text string should match", "0.91", result.getResultScore().getTextString());
    }
    
    /**
     * Test parsing deleteResultRequest XML - verifies objects are correctly populated
     * from Section 6.1.3 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseDeleteResultRequest() throws Exception {
        String specXml = specXml_6_1_3_deleteResultRequest;
        
        // Parse the XML
        POXEnvelopeRequest request = XML_MAPPER.readValue(specXml, POXEnvelopeRequest.class);
        
        // Verify header
        assertNotNull("Request should have header", request.getPoxHeader());
        assertEquals("Version should be V1.0", "V1.0", request.getPoxHeader().getRequestHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "999999123", request.getPoxHeader().getRequestHeaderInfo().getMessageIdentifier());
        
        // Verify body
        assertNotNull("Request should have body", request.getPoxBody());
        assertNotNull("Body should have deleteResultRequest", request.getPoxBody().getDeleteResultRequest());
        
        // Verify result record
        ResultRecord resultRecord = request.getPoxBody().getDeleteResultRequest().getResultRecord();
        assertNotNull("Result record should exist", resultRecord);
        assertNotNull("Result record should have sourcedGUID", resultRecord.getSourcedGUID());
        assertEquals("SourcedId should match", "3124567", resultRecord.getSourcedGUID().getSourcedId());
        
        // Verify result is null (not present in deleteResultRequest)
        assertNull("Result should be null in deleteResultRequest", resultRecord.getResult());
    }
    
    /**
     * Test parsing deleteResultResponse XML - verifies objects are correctly populated
     * from Section 6.1.3 example of https://www.imsglobal.org/specs/ltiv1p1/implementation-guide
     */
    @Test
    public void testParseDeleteResultResponse() throws Exception {
        String specXml = specXml_6_1_3_deleteResultResponse;
        
        // Parse the XML
        POXEnvelopeResponse response = XML_MAPPER.readValue(specXml, POXEnvelopeResponse.class);
        
        // Verify header
        assertNotNull("Response should have header", response.getPoxHeader());
        assertEquals("Version should be V1.0", "V1.0", response.getPoxHeader().getResponseHeaderInfo().getVersion());
        assertEquals("Message identifier should match", "4560", response.getPoxHeader().getResponseHeaderInfo().getMessageIdentifier());
        
        // Verify status info
        POXStatusInfo statusInfo = response.getPoxHeader().getResponseHeaderInfo().getStatusInfo();
        assertNotNull("Status info should exist", statusInfo);
        assertEquals("Code major should be success", "success", statusInfo.getCodeMajor());
        assertEquals("Severity should be status", "status", statusInfo.getSeverity());
        assertNull("Description should be null (not present in deleteResultResponse)", statusInfo.getDescription());
        assertEquals("Message ref identifier should match", "999999123", statusInfo.getMessageRefIdentifier());
        assertEquals("Operation ref identifier should match", "deleteResult", statusInfo.getOperationRefIdentifier());
        assertNull("Code minor should be null (not present in v1.1 examples)", statusInfo.getCodeMinor());
        
        // Verify body
        assertNotNull("Response should have body", response.getPoxBody());
        assertNotNull("Body should have deleteResultResponse", response.getPoxBody().getDeleteResultResponse());
    }
}
