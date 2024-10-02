package org.tsugi.nrps;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.jackson.JacksonUtil;

import org.tsugi.nrps.objects.Container;
import org.tsugi.nrps.objects.Member;
import org.tsugi.nrps.objects.MemberMessage;

// https://www.imsglobal.org/spec/lti-nrps/v2p0

@Slf4j
public class NRPSTest {

	String sampleMemberMessage = null;
	String sampleMember = null;
	String sampleContainer = null;

	@Before
	public void setUp() throws Exception {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_member_message.json");
		sampleMemberMessage = IOUtils.toString(resourceAsStream, "UTF-8");
		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_member.json");
		sampleMember = IOUtils.toString(resourceAsStream, "UTF-8");
		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("nrps/sample_container.json");
		sampleContainer = IOUtils.toString(resourceAsStream, "UTF-8");
	}

	@Test
	public void testLoad() throws JsonProcessingException {
		assertNotNull(sampleMemberMessage);
		// Get a picky ObjectMapper
		ObjectMapper mapper = new ObjectMapper();
		MemberMessage memberMessage = mapper.readValue(sampleMemberMessage, MemberMessage.class);
		assertNotNull(memberMessage);
		assertNotNull(memberMessage.custom);
		assertEquals(memberMessage.custom.size(), 2);
		assertNotNull(memberMessage.basicoutcome);
		assertEquals(memberMessage.basicoutcome.size(), 2);

		assertNotNull(sampleMember);
		Member member = mapper.readValue(sampleMember, Member.class);
		assertNotNull(member);
		assertNotNull(member.name);
		assertNotNull(member.email);
		assertEquals(member.email, "privacy@sakaiger.com");
		assertEquals(member.message.get(0).custom.size(), 2);
		assertEquals(member.message.get(0).custom.get("user_mobile"), "123-456-7890");
		assertEquals(member.message.get(0).basicoutcome.size(), 2);

		assertNotNull(sampleContainer);
		Container container = mapper.readValue(sampleContainer, Container.class);
		assertNotNull(container);
		assertNotNull(container.members);
		assertNotNull(container.members.get(0));
		assertNotNull(container.members.get(0).name);
		assertNotNull(container.members.get(0).email);
		assertEquals(container.members.get(0).email, "privacy@sakaiger.com");
		assertEquals(container.members.get(0).message.get(0).custom.size(), 2);
		assertEquals(container.members.get(0).message.get(0).custom.get("user_mobile"), "123-456-7890");
		assertEquals(container.members.get(0).message.get(0).basicoutcome.size(), 2);
	}

}

