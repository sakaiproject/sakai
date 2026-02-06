package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import static org.junit.Assert.*;

import org.tsugi.lti.objects.Member;
import org.tsugi.lti.objects.Groups;
import org.tsugi.lti.objects.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Member class.
 */
public class MemberTest {
    
    private static final XmlMapper XML_MAPPER;
    
    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_MAPPER.setDefaultUseWrapper(false);
    }
    
    @Test
    public void testSerializeWithAllFields() throws Exception {
        Member member = new Member();
        member.setUserId("user123");
        member.setRole("Learner");
        member.setRoles("Learner");
        member.setPersonNameGiven("John");
        member.setPersonNameFamily("Doe");
        member.setPersonNameFull("John Doe");
        member.setPersonContactEmailPrimary("john.doe@example.com");
        member.setPersonSourcedId("sourced-123");
        member.setLisResultSourcedId("result-123");
        
        Groups groups = new Groups();
        List<Group> groupList = new ArrayList<>();
        Group group = new Group();
        group.setId("group-1");
        group.setTitle("Group 1");
        groupList.add(group);
        groups.setGroup(groupList);
        member.setGroups(groups);
        
        String xml = XML_MAPPER.writeValueAsString(member);
        
        assertNotNull("XML should not be null", xml);
        assertTrue("XML should contain user_id", xml.contains("<user_id>user123</user_id>"));
        assertTrue("XML should contain role", xml.contains("<role>Learner</role>"));
        assertTrue("XML should contain person_name_given", xml.contains("<person_name_given>John</person_name_given>"));
    }
    
    @Test
    public void testDeserializeWithAllFields() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<member>\n" +
                     "<user_id>user123</user_id>\n" +
                     "<role>Learner</role>\n" +
                     "<roles>Learner</roles>\n" +
                     "<person_name_given>John</person_name_given>\n" +
                     "<person_name_family>Doe</person_name_family>\n" +
                     "<person_name_full>John Doe</person_name_full>\n" +
                     "<person_contact_email_primary>john.doe@example.com</person_contact_email_primary>\n" +
                     "<person_sourcedid>sourced-123</person_sourcedid>\n" +
                     "<lis_result_sourcedid>result-123</lis_result_sourcedid>\n" +
                     "</member>";
        
        Member member = XML_MAPPER.readValue(xml, Member.class);
        
        assertNotNull("Member should not be null", member);
        assertEquals("UserId should match", "user123", member.getUserId());
        assertEquals("Role should match", "Learner", member.getRole());
        assertEquals("PersonNameGiven should match", "John", member.getPersonNameGiven());
        assertEquals("PersonNameFamily should match", "Doe", member.getPersonNameFamily());
        assertEquals("PersonNameFull should match", "John Doe", member.getPersonNameFull());
        assertEquals("Email should match", "john.doe@example.com", member.getPersonContactEmailPrimary());
    }
    
    @Test
    public void testRoundTripSerialization() throws Exception {
        Member original = new Member();
        original.setUserId("test-user");
        original.setRole("Instructor");
        original.setPersonNameGiven("Test");
        original.setPersonNameFamily("User");
        
        String xml = XML_MAPPER.writeValueAsString(original);
        Member deserialized = XML_MAPPER.readValue(xml, Member.class);
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertEquals("UserId should match", original.getUserId(), deserialized.getUserId());
        assertEquals("Role should match", original.getRole(), deserialized.getRole());
        assertEquals("PersonNameGiven should match", original.getPersonNameGiven(), deserialized.getPersonNameGiven());
    }
}
