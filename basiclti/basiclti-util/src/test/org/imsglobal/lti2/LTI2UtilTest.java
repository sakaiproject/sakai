package org.imsglobal.lti2;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import org.imsglobal.lti2.LTI2Util;

import org.json.simple.JSONArray;


public class LTI2UtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testServiceCompare() {
		assertTrue(LTI2Util.compareServiceIds("tcp:Result.item", "tcp:Result.item"));
		assertTrue(LTI2Util.compareServiceIds("tcp:Result.item", "#Result.item"));
		assertTrue(LTI2Util.compareServiceIds("tcp:Result.item", "http://sakai.ngrok.com/imsblis/lti2/#Result.item"));
	}

	@Test
	public void testMapProperties() {
		assertEquals(LTI2Util.property2Capability("context_id"), "Context.id");
		assertEquals(LTI2Util.property2Capability("user_id"), "User.id");
	}


        @Test
        public void testFilterProperties() {

		Properties ltiProps = new Properties();
		ltiProps.setProperty("resource_link_id", "42");
		ltiProps.setProperty("user_id", "142");
		ltiProps.setProperty("context_id", "142");

		JSONArray caps = new JSONArray();
		caps.add("User.id");

		LTI2Util.filterLTI1LaunchProperties(ltiProps, caps);

		assertEquals(ltiProps.getProperty("resource_link_id", null), "42");
		assertEquals(ltiProps.getProperty("user_id", null), "142");
		assertNull(ltiProps.getProperty("context_id", null));
	}

}

