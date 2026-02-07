package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.tsugi.lti.objects.Membership;
import org.tsugi.lti.objects.Member;

/**
 * Unit tests for Membership class.
 */
public class MembershipTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        Membership membership = new Membership();
        membership.setCollectionSourcedId("collection-123");
        membership.setMembershipIdType("SourcedId");
        membership.setCreditHours(3);
        membership.setDataSource("SIS");
        
        List<Member> members = new ArrayList<>();
        Member member = new Member();
        member.setUserId("user123");
        member.setRole("Learner");
        members.add(member);
        membership.setMembers(members);
        
        String xml = XML_MAPPER.writeValueAsString(membership);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain collectionSourcedId", xml.contains("<collectionSourcedId>collection-123</collectionSourcedId>"));
        assertTrue("XML should contain member", xml.contains("<member>"));
        assertTrue("XML should contain creditHours", xml.contains("<creditHours>3</creditHours>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<membership>\n" +
                     "<collectionSourcedId>collection-123</collectionSourcedId>\n" +
                     "<membershipIdType>SourcedId</membershipIdType>\n" +
                     "<creditHours>3</creditHours>\n" +
                     "<dataSource>SIS</dataSource>\n" +
                     "<member>\n" +
                     "<user_id>user123</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "</member>\n" +
                     "</membership>";
        
        Membership membership = XML_MAPPER.readValue(xml, Membership.class);
        
        assertNotNull("Membership should not be null", membership);
        assertEquals("CollectionSourcedId should match", "collection-123", membership.getCollectionSourcedId());
        assertEquals("MembershipIdType should match", "SourcedId", membership.getMembershipIdType());
        assertEquals("CreditHours should match", Integer.valueOf(3), membership.getCreditHours());
        assertEquals("DataSource should match", "SIS", membership.getDataSource());
        assertNotNull("Members should not be null", membership.getMembers());
        assertEquals("Should have 1 member", 1, membership.getMembers().size());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Membership original = new Membership();
        original.setCollectionSourcedId("test-collection");
        original.setMembershipIdType("SourcedId");
        original.setCreditHours(4);
        
        List<Member> members = new ArrayList<>();
        Member member = new Member();
        member.setUserId("test-user");
        member.setRole("Learner");
        members.add(member);
        original.setMembers(members);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Membership deserialized = XML_MAPPER.readValue(xml, Membership.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("CollectionSourcedId should match", original.getCollectionSourcedId(), deserialized.getCollectionSourcedId());
        assertEquals("CreditHours should match", original.getCreditHours(), deserialized.getCreditHours());
        assertNotNull("Members should not be null", deserialized.getMembers());
        assertEquals("Should have 1 member", 1, deserialized.getMembers().size());
    }
}
