package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.ReadMembershipResponse;
import org.tsugi.lti.objects.MembershipRecord;
import org.tsugi.lti.objects.Membership;
import org.tsugi.lti.objects.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ReadMembershipResponse class.
 */
public class ReadMembershipResponseTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithMembershipRecord() throws Exception {
        ReadMembershipResponse response = new ReadMembershipResponse();
        MembershipRecord record = new MembershipRecord();
        record.setSourcedId("course-123");
        
        Membership membership = new Membership();
        membership.setCollectionSourcedId("collection-123");
        
        List<Member> members = new ArrayList<>();
        Member member = new Member();
        member.setUserId("user123");
        member.setRole("Learner");
        members.add(member);
        membership.setMembers(members);
        
        record.setMembership(membership);
        response.setMembershipRecord(record);
        
        String xml = XML_MAPPER.writeValueAsString(response);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain membershipRecord", xml.contains("<membershipRecord>"));
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>course-123</sourcedId>"));
    }
    
    @Test
    public void testDeserializeWithMembershipRecord() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<readMembershipResponse>\n" +
                     "<membershipRecord>\n" +
                     "<sourcedId>course-123</sourcedId>\n" +
                     "<membership>\n" +
                     "<collectionSourcedId>collection-123</collectionSourcedId>\n" +
                     "<member>\n" +
                     "<user_id>user123</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "</member>\n" +
                     "</membership>\n" +
                     "</membershipRecord>\n" +
                     "</readMembershipResponse>";
        
        ReadMembershipResponse response = XML_MAPPER.readValue(xml, ReadMembershipResponse.class);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("MembershipRecord should not be null", response.getMembershipRecord());
        assertEquals("SourcedId should match", "course-123", response.getMembershipRecord().getSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        ReadMembershipResponse original = new ReadMembershipResponse();
        MembershipRecord record = new MembershipRecord();
        record.setSourcedId("test-course");
        
        Membership membership = new Membership();
        membership.setCollectionSourcedId("test-collection");
        record.setMembership(membership);
        original.setMembershipRecord(record);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        ReadMembershipResponse deserialized = XML_MAPPER.readValue(xml, ReadMembershipResponse.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("MembershipRecord should not be null", deserialized.getMembershipRecord());
        assertEquals("SourcedId should match", original.getMembershipRecord().getSourcedId(),
                     deserialized.getMembershipRecord().getSourcedId());
    }
}
