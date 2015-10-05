package org.imsglobal.lti2;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.imsglobal.lti2.LTI2Util;

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

}
