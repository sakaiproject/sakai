package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.MessageResponse;
import org.tsugi.lti.objects.Members;
import org.tsugi.lti.objects.Member;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultScore;
import org.tsugi.lti.objects.StatusInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for MessageResponse class.
 */
public class MessageResponseTest {
    
    private static final XmlMapper XML_MAPPER = TestXmlMapperFactory.createXmlMapper();
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        MessageResponse response = new MessageResponse();
        response.setLtiMessageType("basic-lti-launch-request");
        
        Members members = new Members();
        List<Member> memberList = new ArrayList<>();
        Member member = new Member();
        member.setUserId("user123");
        member.setRole("Learner");
        memberList.add(member);
        members.setMember(memberList);
        response.setMembers(members);
        
        Result result = new Result();
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage("en");
        resultScore.setTextString("0.92");
        result.setResultScore(resultScore);
        response.setResult(result);
        
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        response.setStatusInfo(statusInfo);
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain lti_message_type", xml.contains("<lti_message_type>basic-lti-launch-request</lti_message_type>"));
        assertTrue("XML should contain members", xml.contains("<members>"));
        assertTrue("XML should contain result", xml.contains("<result>"));
        assertTrue("XML should contain statusinfo", xml.contains("<statusinfo>"));
    }
    
    @Test
    public void testSerializeWithNullOptionalFields() throws Exception {
        MessageResponse response = new MessageResponse();
        response.setLtiMessageType("basic-lti-launch-request");
        
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setCodeMajor("success");
        response.setStatusInfo(statusInfo);
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain lti_message_type", xml.contains("<lti_message_type>"));
        assertFalse("XML should not contain members when null", xml.contains("<members>"));
        assertFalse("XML should not contain result when null", xml.contains("<result>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<message_response>\n" +
                     "<lti_message_type>basic-lti-launch-request</lti_message_type>\n" +
                     "<members>\n" +
                     "<member>\n" +
                     "<user_id>user123</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "</member>\n" +
                     "</members>\n" +
                     "<result>\n" +
                     "<resultScore>\n" +
                     "<language>en</language>\n" +
                     "<textString>0.92</textString>\n" +
                     "</resultScore>\n" +
                     "</result>\n" +
                     "<statusinfo>\n" +
                     "<codemajor>success</codemajor>\n" +
                     "<severity>status</severity>\n" +
                     "</statusinfo>\n" +
                     "</message_response>";
        
        MessageResponse response = XML_MAPPER.readValue(xml, MessageResponse.class);
        
        assertNotNull("Response should not be null", response);
        assertEquals("LtiMessageType should match", "basic-lti-launch-request", response.getLtiMessageType());
        assertNotNull("Members should not be null", response.getMembers());
        assertNotNull("Result should not be null", response.getResult());
        assertNotNull("StatusInfo should not be null", response.getStatusInfo());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        MessageResponse original = new MessageResponse();
        original.setLtiMessageType("test-message");
        
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setCodeMajor("success");
        statusInfo.setSeverity("status");
        original.setStatusInfo(statusInfo);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        MessageResponse deserialized = XML_MAPPER.readValue(xml, MessageResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("LtiMessageType should match", original.getLtiMessageType(), deserialized.getLtiMessageType());
        assertNotNull("StatusInfo should not be null", deserialized.getStatusInfo());
        assertEquals("CodeMajor should match", original.getStatusInfo().getCodeMajor(),
                     deserialized.getStatusInfo().getCodeMajor());
    }
}
