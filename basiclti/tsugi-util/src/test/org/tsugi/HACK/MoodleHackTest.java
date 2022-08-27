package org.tsugi.HACK;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;

import org.tsugi.HACK.HackMoodle;

@Slf4j
public class MoodleHackTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHackMoodle() {
		String one = "{\"https://purl.imsglobal.org/spec/lti-platform-configuration\":{\"messages_supported\":[\"LtiResourceLinkRequest\",\"LtiDeepLinkingRequest\"]}}";
		String two = "{\"https://purl.imsglobal.org/spec/lti-platform-configuration\":{\"messages_supported\":[{\"type\":\"LtiResourceLinkRequest\"},{\"type\":\"LtiDeepLinkingRequest\"}]}}";
		String hack1 = HackMoodle.hackOpenIdConfiguration(one);
		hack1 = hack1.replaceAll("\\\\","");
		String hack2 = HackMoodle.hackOpenIdConfiguration(two);
		assertEquals(hack1, hack2);
	}

}
