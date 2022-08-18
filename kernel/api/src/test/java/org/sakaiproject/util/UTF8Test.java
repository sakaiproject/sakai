/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;

/** 
 ** JUnit UTF-8 test cases for the sakai-util package
 * NOTE: this does not actually do anything - Dec 2011 - fix or remove this
 **/
public class UTF8Test {

	private String asciiResourceName = "dilbert.txt";
	private String badAsciiResourceName = "PointyHairedBoss?";
	private String utf8ResourceName  = "サカイ.txt";
	private String percentUtf8ResourceName= "%E3%82%B5%E3%82%AB%E3%82%A4.txt";
	private String mimeUtf8ResourceName = "=?UTF8?B?44K144Kr44KkLnR4dA==?=";

	@Before
	public void setUp() throws Exception {
		byte[] utf8bytes = utf8ResourceName.getBytes();
		utf8ResourceName = new String(utf8bytes, "UTF-8");
	}

	/**
	 ** test Validator.escapeResourceName()
	 **/
	@Test
	public void testEscapeResourceName() {
		/* - platform dependencies sometimes cause this to fail -- need a different approach */
		Assert.assertEquals("ascii string should match orginal",
						 asciiResourceName,
						 Validator.escapeResourceName(asciiResourceName));
		Assert.assertEquals("utf8 string should match original",
						 utf8ResourceName,
						 Validator.escapeResourceName(utf8ResourceName));
		if ( badAsciiResourceName.equals(  Validator.escapeResourceName(badAsciiResourceName)) )
			Assert.fail("invalid ascii string should be changed");
	}

	/**
	 ** test Web.encodeFileName()
	 ** -- currently commented out because MockHttpServletRequest can't be instantiated for some reason
	 **/
	@Test
	public void testEncodeFileName() {
		// test MSIE type browser
		/* - platform dependencies sometimes cause this to fail -- need a different approach */
		MockHttpServletRequest request = new MockHttpServletRequest(); // needs servlet 3.0
		request.addHeader("USER-AGENT", "MSIE");
		Assert.assertEquals("ascii string should match orginal (MSIE)",
						 asciiResourceName,
						 Web.encodeFileName(request, asciiResourceName));
						 
		Assert.assertEquals("utf8 string should be percent encoded (MSIE)",
						 percentUtf8ResourceName,
						 Web.encodeFileName(request, utf8ResourceName));
						 
		// Test Mozilla type browser
		/* - platform dependencies sometimes cause this to fail -- need a different approach */
		request = new MockHttpServletRequest();
		request.addHeader("USER-AGENT", "Mozilla");
		Assert.assertEquals("ascii string should match orginal (mozilla)",
						 asciiResourceName,
						 Web.encodeFileName(request, asciiResourceName));
		Assert.assertEquals("utf8 string should be mime encoded (mozilla)",
						 mimeUtf8ResourceName,
						 Web.encodeFileName(request, utf8ResourceName));
	}
}
