package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.tsugi.lti.objects.Members;
import org.tsugi.lti.objects.Member;

/**
 * Unit tests for Members class.
 */
public class MembersTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithMembers() throws Exception {
        Members members = new Members();
        List<Member> memberList = new ArrayList<>();
        
        Member member1 = new Member();
        member1.setUserId("user1");
        member1.setRole("Learner");
        memberList.add(member1);
        
        Member member2 = new Member();
        member2.setUserId("user2");
        member2.setRole("Instructor");
        memberList.add(member2);
        
        members.setMember(memberList);
        
        String xml = XML_MAPPER.writeValueAsString(members);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain member elements", xml.contains("<member>"));
        assertTrue("XML should contain user1", xml.contains("user1"));
        assertTrue("XML should contain user2", xml.contains("user2"));
    }
    
    @Test
    public void testDeserializeWithMembers() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<members>\n" +
                     "<member>\n" +
                     "<user_id>user1</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "</member>\n" +
                     "<member>\n" +
                     "<user_id>user2</user_id>\n" +
                     "<role>Instructor</role>\n" +
                     "</member>\n" +
                     "</members>";
        
        Members members = XML_MAPPER.readValue(xml, Members.class);
        
        assertNotNull("Members should not be null", members);
        assertNotNull("Member list should not be null", members.getMember());
        assertEquals("Should have 2 members", 2, members.getMember().size());
        assertEquals("First member userId should match", "user1", members.getMember().get(0).getUserId());
        assertEquals("Second member userId should match", "user2", members.getMember().get(1).getUserId());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Members original = new Members();
        List<Member> memberList = new ArrayList<>();
        
        Member member = new Member();
        member.setUserId("test-user");
        member.setRole("Learner");
        memberList.add(member);
        
        original.setMember(memberList);
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Members deserialized = XML_MAPPER.readValue(xml, Members.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertNotNull("Member list should not be null", deserialized.getMember());
        assertEquals("Should have 1 member", 1, deserialized.getMember().size());
        assertEquals("Member userId should match", original.getMember().get(0).getUserId(),
                     deserialized.getMember().get(0).getUserId());
    }
}
