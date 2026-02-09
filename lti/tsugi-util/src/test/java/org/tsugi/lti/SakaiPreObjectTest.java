package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.*;
import org.tsugi.pox.POXRequestHandler;
import org.tsugi.pox.POXResponseBuilder;
import org.tsugi.lti.POXJacksonParser;

/**
 * Test class to verify backward compatibility with previous Sakai version XML.
 * 
 * This test takes XML input/output produced by the previous version of Sakai when using the 
 * https://www.tsugi.org/lti-test/
 * test tool - this is the XML sent by the tool and received by the tool using Sakai before
 * removal of the XML Map. It ensures:
 * 1. Sakai can receive and parse the "WE SENT" XML (requests from the test tool)
 * 2. Sakai's new builders can generate the "POST RETURNS" XML (responses sent the test tool)
 */
public class SakaiPreObjectTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    /**
     * Normalize text content for comparison by trimming and collapsing whitespace.
     * This handles differences in whitespace formatting from previous Sakai versions.
     */
    private String normalizeText(String text) {
        if (text == null) return null;
        return text.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Test 1: basic-lis-readmembershipsforcontext response
     * 
     * POST RETURNS: MessageResponse with members
     */
    @Test
    public void testReadMembershipsResponse() throws Exception {
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message_response>\n" +
            "  <lti_message_type>basic-lis-readmembershipsforcontext</lti_message_type>\n" +
            "  <members>\n" +
            "    <member>\n" +
            "      <lis_result_sourcedid>9b106027c94ffe1a43dd662245125173616e5ef29f15d6866bcb4fc19428e9ac:::00fc9208-c718-4918-b9e3-4176c27dcfc5:::content:6</lis_result_sourcedid>\n" +
            "      <person_contact_email_primary>p@p.com</person_contact_email_primary>\n" +
            "      <person_name_family>p</person_name_family>\n" +
            "      <person_name_full>p p</person_name_full>\n" +
            "      <person_name_given>p</person_name_given>\n" +
            "      <person_sourcedid>csev</person_sourcedid>\n" +
            "      <role>Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor</role>\n" +
            "      <roles>Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor</roles>\n" +
            "      <user_id>00fc9208-c718-4918-b9e3-4176c27dcfc5</user_id>\n" +
            "    </member>\n" +
            "    <member>\n" +
            "      <lis_result_sourcedid>d7ddb275930a9688ad5fa9be75f58048ca0307ea5f5f96f4d86fc5e24b0fa5a8:::f2c5d415-e060-4757-83f2-002a853042f9:::content:6</lis_result_sourcedid>\n" +
            "      <person_contact_email_primary>h@p.com</person_contact_email_primary>\n" +
            "      <person_name_family>h</person_name_family>\n" +
            "      <person_name_full>h h</person_name_full>\n" +
            "      <person_name_given>h</person_name_given>\n" +
            "      <person_sourcedid>hirouki</person_sourcedid>\n" +
            "      <role>Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner</role>\n" +
            "      <roles>Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner</roles>\n" +
            "      <user_id>f2c5d415-e060-4757-83f2-002a853042f9</user_id>\n" +
            "    </member>\n" +
            "  </members>\n" +
            "  <statusinfo>\n" +
            "    <codemajor>Success</codemajor>\n" +
            "    <codeminor>fullsuccess</codeminor>\n" +
            "    <severity>Status</severity>\n" +
            "  </statusinfo>\n" +
            "</message_response>";
        
        // Build using MessageResponseBuilder
        Member member1 = new Member();
        member1.setLisResultSourcedId("9b106027c94ffe1a43dd662245125173616e5ef29f15d6866bcb4fc19428e9ac:::00fc9208-c718-4918-b9e3-4176c27dcfc5:::content:6");
        member1.setPersonContactEmailPrimary("p@p.com");
        member1.setPersonNameFamily("p");
        member1.setPersonNameFull("p p");
        member1.setPersonNameGiven("p");
        member1.setPersonSourcedId("csev");
        member1.setRole("Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
        member1.setRoles("Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
        member1.setUserId("00fc9208-c718-4918-b9e3-4176c27dcfc5");
        
        Member member2 = new Member();
        member2.setLisResultSourcedId("d7ddb275930a9688ad5fa9be75f58048ca0307ea5f5f96f4d86fc5e24b0fa5a8:::f2c5d415-e060-4757-83f2-002a853042f9:::content:6");
        member2.setPersonContactEmailPrimary("h@p.com");
        member2.setPersonNameFamily("h");
        member2.setPersonNameFull("h h");
        member2.setPersonNameGiven("h");
        member2.setPersonSourcedId("hirouki");
        member2.setRole("Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
        member2.setRoles("Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
        member2.setUserId("f2c5d415-e060-4757-83f2-002a853042f9");
        
        MessageResponseBuilder builder = MessageResponseBuilder.success()
            .withLtiMessageType("basic-lis-readmembershipsforcontext")
            .addMember(member1)
            .addMember(member2);
        
        String generatedXml = builder.buildAsXml(false);
        assertNotNull("Generated XML should not be null", generatedXml);
        
        // Parse both to verify semantic equality
        MessageResponse expected = XML_MAPPER.readValue(expectedXml, MessageResponse.class);
        MessageResponse generated = XML_MAPPER.readValue(generatedXml, MessageResponse.class);
        assertEquals("LtiMessageType should match", expected.getLtiMessageType(), generated.getLtiMessageType());
        assertNotNull("Members should not be null", generated.getMembers());
        assertEquals("Member count should match", 2, generated.getMembers().getMember().size());
        assertNotNull("StatusInfo should not be null", generated.getStatusInfo());
        // Compare case-insensitively as old Sakai may have used capitalized values
        assertEquals("CodeMajor should match", 
            expected.getStatusInfo().getCodeMajor().toLowerCase(), 
            generated.getStatusInfo().getCodeMajor().toLowerCase());
    }
    
    /**
     * Test 2: replaceResultRequest/Response
     * 
     * WE SENT: replaceResultRequest with resultScore and resultData
     * POST RETURNS: replaceResultResponse (empty element)
     */
    @Test
    public void testReplaceResultRequestResponse() throws Exception {
        String weSentXml = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXRequestHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>6988bcdd1b4b3</imsx_messageIdentifier>\n" +
            "    </imsx_POXRequestHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "    <replaceResultRequest>\n" +
            "      <resultRecord>\n" +
            "        <sourcedGUID>\n" +
            "          <sourcedId>d7ddb275930a9688ad5fa9be75f58048ca0307ea5f5f96f4d86fc5e24b0fa5a8:::f2c5d415-e060-4757-83f2-002a853042f9:::content:6</sourcedId>\n" +
            "        </sourcedGUID>\n" +
            "        <result>\n" +
            "          <resultScore>\n" +
            "            <language>en-us</language>\n" +
            "            <textString>0.05</textString>\n" +
            "          </resultScore>\n" +
            "<resultData>\n" +
            "<text>\n" +
            "Five percent\n" +
            "</text>\n" +
            "</resultData>\n" +
            "        </result>\n" +
            "      </resultRecord>\n" +
            "    </replaceResultRequest>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
        
        String postReturnsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXResponseHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>1770568925405</imsx_messageIdentifier>\n" +
            "      <imsx_statusInfo>\n" +
            "        <imsx_codeMajor>success</imsx_codeMajor>\n" +
            "        <imsx_severity>status</imsx_severity>\n" +
            "        <imsx_description>Result replaced</imsx_description>\n" +
            "        <imsx_messageRefIdentifier>6988bcdd1b4b3</imsx_messageRefIdentifier>\n" +
            "        <imsx_operationRefIdentifier>replaceResultRequest</imsx_operationRefIdentifier>\n" +
            "      </imsx_statusInfo>\n" +
            "    </imsx_POXResponseHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "<replaceResultResponse/>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
        
        // Parse the "WE SENT" request
        POXRequestHandler requestHandler = new POXRequestHandler(weSentXml);
        assertTrue("Request should be valid", requestHandler.valid);
        assertEquals("Operation should be replaceResultRequest", "replaceResultRequest", requestHandler.getOperation());
        
        // Extract message identifier from request
        String requestMessageId = requestHandler.getHeaderMessageIdentifier();
        assertEquals("Request message ID should match", "6988bcdd1b4b3", requestMessageId);
        
        // Build the "POST RETURNS" response using POXResponseBuilder
        ReplaceResultResponse replaceResultResponse = new ReplaceResultResponse();
        String generatedXml = POXResponseBuilder.create()
            .withDescription("Result replaced")
            .asSuccess()
            .withMessageId("1770568925405")
            .withMessageRefIdentifier(requestMessageId)
            .withOperation("replaceResultRequest")
            .withBodyObject(replaceResultResponse)
            .buildAsXml();
        assertNotNull("Generated XML should not be null", generatedXml);
        
        // Parse both to verify semantic equality
        POXEnvelopeResponse expected = POXJacksonParser.parseResponse(postReturnsXml);
        POXEnvelopeResponse generated = POXJacksonParser.parseResponse(generatedXml);
        assertNotNull("Expected response should not be null", expected);
        assertNotNull("Generated response should not be null", generated);
        
        // Verify key fields
        assertEquals("CodeMajor should match", 
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor());
        assertEquals("Description should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription());
        assertEquals("MessageRefIdentifier should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier());
        assertEquals("OperationRefIdentifier should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getOperationRefIdentifier(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getOperationRefIdentifier());
    }
    
    /**
     * Test 3: readResultRequest/Response
     * 
     * WE SENT: readResultRequest
     * POST RETURNS: readResultResponse with resultScore and resultData
     */
    @Test
    public void testReadResultRequestResponse() throws Exception {
        String weSentXml = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXRequestHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>6988bcf2aeb7a</imsx_messageIdentifier>\n" +
            "    </imsx_POXRequestHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "    <readResultRequest>\n" +
            "      <resultRecord>\n" +
            "        <sourcedGUID>\n" +
            "          <sourcedId>d7ddb275930a9688ad5fa9be75f58048ca0307ea5f5f96f4d86fc5e24b0fa5a8:::f2c5d415-e060-4757-83f2-002a853042f9:::content:6</sourcedId>\n" +
            "        </sourcedGUID>\n" +
            "      </resultRecord>\n" +
            "    </readResultRequest>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
        
        String postReturnsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXResponseHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>1770568946846</imsx_messageIdentifier>\n" +
            "      <imsx_statusInfo>\n" +
            "        <imsx_codeMajor>success</imsx_codeMajor>\n" +
            "        <imsx_severity>status</imsx_severity>\n" +
            "        <imsx_description>Result read</imsx_description>\n" +
            "        <imsx_messageRefIdentifier>6988bcf2aeb7a</imsx_messageRefIdentifier>\n" +
            "        <imsx_operationRefIdentifier>readResultRequest</imsx_operationRefIdentifier>\n" +
            "      </imsx_statusInfo>\n" +
            "    </imsx_POXResponseHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "<readResultResponse>\n" +
            "  <result>\n" +
            "    <resultData>\n" +
            "      <text>\n" +
            "        Five percent\n" +
            "      </text>\n" +
            "    </resultData>\n" +
            "    <resultScore>\n" +
            "      <language>en</language>\n" +
            "      <textString>0.05</textString>\n" +
            "    </resultScore>\n" +
            "  </result>\n" +
            "</readResultResponse>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
        
        // Parse the "WE SENT" request
        POXRequestHandler requestHandler = new POXRequestHandler(weSentXml);
        assertTrue("Request should be valid", requestHandler.valid);
        assertEquals("Operation should be readResultRequest", "readResultRequest", requestHandler.getOperation());
        
        // Extract message identifier from request
        String requestMessageId = requestHandler.getHeaderMessageIdentifier();
        assertEquals("Request message ID should match", "6988bcf2aeb7a", requestMessageId);
        
        // Build the "POST RETURNS" response using POXResponseBuilder with ReadResultResponse
        ReadResultResponse readResultResponse = ReadResultResponse.create("0.05", "Five percent", "en");
        String generatedXml = POXResponseBuilder.create()
            .withDescription("Result read")
            .asSuccess()
            .withMessageId("1770568946846")
            .withMessageRefIdentifier(requestMessageId)
            .withOperation("readResultRequest")
            .withBodyObject(readResultResponse)
            .buildAsXml();
        assertNotNull("Generated XML should not be null", generatedXml);
        
        // Parse both to verify semantic equality
        POXEnvelopeResponse expected = POXJacksonParser.parseResponse(postReturnsXml);
        POXEnvelopeResponse generated = POXJacksonParser.parseResponse(generatedXml);
        assertNotNull("Expected response should not be null", expected);
        assertNotNull("Generated response should not be null", generated);
        
        // Verify key fields
        assertEquals("CodeMajor should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor());
        assertEquals("Description should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription());
        assertEquals("MessageRefIdentifier should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier());
        
        // Verify result content
        ReadResultResponse expectedBody = expected.getPoxBody().getReadResultResponse();
        ReadResultResponse generatedBody = generated.getPoxBody().getReadResultResponse();
        assertNotNull("Expected body should not be null", expectedBody);
        assertNotNull("Generated body should not be null", generatedBody);
        assertEquals("ResultScore textString should match",
            expectedBody.getResult().getResultScore().getTextString(),
            generatedBody.getResult().getResultScore().getTextString());
        // Normalize whitespace for comparison (old Sakai may have had formatting whitespace)
        assertEquals("ResultData text should match", 
            normalizeText(expectedBody.getResult().getResultData().getText()),
            normalizeText(generatedBody.getResult().getResultData().getText()));
    }
    
    /**
     * Test 4: deleteResultRequest/Response
     * 
     * WE SENT: deleteResultRequest
     * POST RETURNS: deleteResultResponse (empty element)
     */
    @Test
    public void testDeleteResultRequestResponse() throws Exception {
        String weSentXml = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeRequest xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXRequestHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>6988bd13c6395</imsx_messageIdentifier>\n" +
            "    </imsx_POXRequestHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "    <deleteResultRequest>\n" +
            "      <resultRecord>\n" +
            "        <sourcedGUID>\n" +
            "          <sourcedId>d7ddb275930a9688ad5fa9be75f58048ca0307ea5f5f96f4d86fc5e24b0fa5a8:::f2c5d415-e060-4757-83f2-002a853042f9:::content:6</sourcedId>\n" +
            "        </sourcedGUID>\n" +
            "      </resultRecord>\n" +
            "    </deleteResultRequest>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeRequest>";
        
        String postReturnsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<imsx_POXEnvelopeResponse xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n" +
            "  <imsx_POXHeader>\n" +
            "    <imsx_POXResponseHeaderInfo>\n" +
            "      <imsx_version>V1.0</imsx_version>\n" +
            "      <imsx_messageIdentifier>1770568979968</imsx_messageIdentifier>\n" +
            "      <imsx_statusInfo>\n" +
            "        <imsx_codeMajor>success</imsx_codeMajor>\n" +
            "        <imsx_severity>status</imsx_severity>\n" +
            "        <imsx_description>Result deleted</imsx_description>\n" +
            "        <imsx_messageRefIdentifier>6988bd13c6395</imsx_messageRefIdentifier>\n" +
            "        <imsx_operationRefIdentifier>deleteResultRequest</imsx_operationRefIdentifier>\n" +
            "      </imsx_statusInfo>\n" +
            "    </imsx_POXResponseHeaderInfo>\n" +
            "  </imsx_POXHeader>\n" +
            "  <imsx_POXBody>\n" +
            "<deleteResultResponse/>\n" +
            "  </imsx_POXBody>\n" +
            "</imsx_POXEnvelopeResponse>";
        
        // Parse the "WE SENT" request
        POXRequestHandler requestHandler = new POXRequestHandler(weSentXml);
        assertTrue("Request should be valid", requestHandler.valid);
        assertEquals("Operation should be deleteResultRequest", "deleteResultRequest", requestHandler.getOperation());
        
        // Extract message identifier from request
        String requestMessageId = requestHandler.getHeaderMessageIdentifier();
        assertEquals("Request message ID should match", "6988bd13c6395", requestMessageId);
        
        // Build the "POST RETURNS" response using POXResponseBuilder
        DeleteResultResponse deleteResultResponse = new DeleteResultResponse();
        String generatedXml = POXResponseBuilder.create()
            .withDescription("Result deleted")
            .asSuccess()
            .withMessageId("1770568979968")
            .withMessageRefIdentifier(requestMessageId)
            .withOperation("deleteResultRequest")
            .withBodyObject(deleteResultResponse)
            .buildAsXml();
        assertNotNull("Generated XML should not be null", generatedXml);
        
        // Parse both to verify semantic equality
        POXEnvelopeResponse expected = POXJacksonParser.parseResponse(postReturnsXml);
        POXEnvelopeResponse generated = POXJacksonParser.parseResponse(generatedXml);
        assertNotNull("Expected response should not be null", expected);
        assertNotNull("Generated response should not be null", generated);
        
        // Verify key fields
        assertEquals("CodeMajor should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getCodeMajor());
        assertEquals("Description should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getDescription());
        assertEquals("MessageRefIdentifier should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getMessageRefIdentifier());
        assertEquals("OperationRefIdentifier should match",
            expected.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getOperationRefIdentifier(),
            generated.getPoxHeader().getResponseHeaderInfo().getStatusInfo().getOperationRefIdentifier());
    }
}
