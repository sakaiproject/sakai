package org.tsugi.nrps;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.nrps.objects.Container;
import org.tsugi.nrps.objects.Member;
import org.tsugi.nrps.objects.MemberMessage;

// https://www.imsglobal.org/spec/lti-nrps/v2p0

@Slf4j
public class NRPSTest {

	private ObjectMapper mapper;
	private String sampleMemberMessage;
	private String sampleMember;
	private String sampleContainer;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_member_message.json")) {
			assertNotNull("sample_member_message.json resource must exist", resourceAsStream);
			sampleMemberMessage = IOUtils.toString(resourceAsStream, "UTF-8");
		}
		
		try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_member.json")) {
			assertNotNull("sample_member.json resource must exist", resourceAsStream);
			sampleMember = IOUtils.toString(resourceAsStream, "UTF-8");
		}
		
		try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_container.json")) {
			assertNotNull("sample_container.json resource must exist", resourceAsStream);
			sampleContainer = IOUtils.toString(resourceAsStream, "UTF-8");
		}
	}

	@Test
	public void testMemberMessageDeserialization() throws JsonProcessingException {
		MemberMessage memberMessage = mapper.readValue(sampleMemberMessage, MemberMessage.class);
		
		assertNotNull("MemberMessage should not be null", memberMessage);
		assertNotNull("Custom properties should not be null", memberMessage.custom);
		assertEquals("Should have 2 custom properties", 2, memberMessage.custom.size());
		assertNotNull("Basic outcome should not be null", memberMessage.basicoutcome);
		assertEquals("Should have 2 basic outcomes", 2, memberMessage.basicoutcome.size());
	}

	@Test
	public void testMemberDeserialization() throws JsonProcessingException {
		Member member = mapper.readValue(sampleMember, Member.class);
		
		assertNotNull("Member should not be null", member);
		assertNotNull("Member name should not be null", member.name);
		assertNotNull("Member email should not be null", member.email);
		assertEquals("Email should match expected value", "privacy@sakaiger.com", member.email);
		
		assertNotNull("Member messages should not be null", member.message);
		assertFalse("Member messages should not be empty", member.message.isEmpty());
		assertEquals("Should have correct custom property value", "123-456-7890", 
			member.message.get(0).custom.get("user_mobile"));
		assertEquals("Should have correct number of basic outcomes", 2, 
			member.message.get(0).basicoutcome.size());
	}

	@Test
	public void testContainerDeserialization() throws JsonProcessingException {
		Container container = mapper.readValue(sampleContainer, Container.class);
		
		assertNotNull("Container should not be null", container);
		assertNotNull("Container members should not be null", container.members);
		assertFalse("Container members should not be empty", container.members.isEmpty());
		
		Member firstMember = container.members.get(0);
		assertNotNull("First member should not be null", firstMember);
		assertEquals("Member email should match", "privacy@sakaiger.com", firstMember.email);
		assertEquals("Should have correct custom property", "123-456-7890",
			firstMember.message.get(0).custom.get("user_mobile"));
	}

	@Test
	public void testInvalidJson() throws JsonProcessingException {
		String invalidJson = "{invalid:json}";
		thrown.expect(JsonProcessingException.class);
		mapper.readValue(invalidJson, MemberMessage.class);
	}

	@Test
	public void testMissingRequiredFields() throws JsonProcessingException {
		String incompleteJson = "{}";
		// This should not throw an exception as fields are optional
		try {
			MemberMessage message = mapper.readValue(incompleteJson, MemberMessage.class);
			assertNotNull("Should create empty object", message);
		} catch (JsonProcessingException e) {
			fail("Should not throw exception for empty JSON");
		}
	}
}

