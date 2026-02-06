package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.MembershipRecord;
import org.tsugi.lti.objects.Membership;
import org.tsugi.lti.objects.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for MembershipRecord class.
 */
public class MembershipRecordTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
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
        
        String xml = XML_MAPPER.writeValueAsString(record);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain sourcedId", xml.contains("<sourcedId>course-123</sourcedId>"));
        assertTrue("XML should contain membership", xml.contains("<membership>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<membershipRecord>\n" +
                     "<sourcedId>course-123</sourcedId>\n" +
                     "<membership>\n" +
                     "<collectionSourcedId>collection-123</collectionSourcedId>\n" +
                     "<member>\n" +
                     "<user_id>user123</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "</member>\n" +
                     "</membership>\n" +
                     "</membershipRecord>";
        
        MembershipRecord record = XML_MAPPER.readValue(xml, MembershipRecord.class);
        
        assertNotNull("MembershipRecord should not be null", record);
        assertEquals("SourcedId should match", "course-123", record.getSourcedId());
        assertNotNull("Membership should not be null", record.getMembership());
        assertEquals("CollectionSourcedId should match", "collection-123", record.getMembership().getCollectionSourcedId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        MembershipRecord original = new MembershipRecord();
        original.setSourcedId("test-course");
        
        Membership membership = new Membership();
        membership.setCollectionSourcedId("test-collection");
        original.setMembership(membership);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        MembershipRecord deserialized = XML_MAPPER.readValue(xml, MembershipRecord.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("SourcedId should match", original.getSourcedId(), deserialized.getSourcedId());
        assertNotNull("Membership should not be null", deserialized.getMembership());
        assertEquals("CollectionSourcedId should match", original.getMembership().getCollectionSourcedId(),
                     deserialized.getMembership().getCollectionSourcedId());
    }
}
