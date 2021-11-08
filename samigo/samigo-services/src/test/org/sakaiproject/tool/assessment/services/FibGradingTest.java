/**
 * Copyright (c) 2005-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.sakaiproject.tool.assessment.services;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class FibGradingTest {
	
	@Test
	public void testValidate() {
		GradingService gradingService = new GradingService();
		
		try {
			gradingService.validate("2.1");
		
		} catch (Exception e) {
			Assert.fail();
		}

		/* FIX me this should work
		try {
			gradingService.validate("2,1");
		
		} catch (Exception e) {
			fail();
		}
	*/
		try {
			gradingService.validate("not a number");
			Assert.fail();
		} catch (Exception e) {
			
		}

		try {
			Map map = gradingService.validate("6.022E23");
			Assert.assertTrue(map.containsKey(GradingService.ANSWER_TYPE_REAL));
		} catch (Exception e) {
			Assert.fail();
		}

		try {
			Map map = gradingService.validate("1+9i");
			Assert.assertTrue(map.containsKey(GradingService.ANSWER_TYPE_COMPLEX));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void fibSimpleUnicodeTest() {
		GradingService gradingService = new GradingService();

		// Ignore case should succeed
		Assert.assertTrue(gradingService.fibmatch("Müsli", "müsli", false, true));
		// Case sensitive should fail
		Assert.assertFalse(gradingService.fibmatch("Müsli", "müsli", true, true));
		// Ignore spaces should pass
		Assert.assertTrue(gradingService.fibmatch("müsli", " müsli ", true, true));
		// Spaces matter should fail
		Assert.assertFalse(gradingService.fibmatch("Müsli", " müsli ", true, false));
		// Spaces example from SAK-34144
		Assert.assertTrue(gradingService.fibmatch("red rust", "redrust", false, true));
		Assert.assertTrue(gradingService.fibmatch("red rust", "RedRust", false, true));
		Assert.assertFalse(gradingService.fibmatch("red rust", "RedRust", true, true));
		// Unicode HTML equivalent mapping SAK-44579
		Assert.assertTrue(gradingService.fibmatch("Müsli", "M&uuml;sli", false, true));
		// Uppercase ummlaut does not match when case sensitive
		Assert.assertFalse(gradingService.fibmatch("Müsli", "M&Uuml;sli", true, true));
		// HTML spaces ignored, this non-breaking space is not a simple space
		Assert.assertTrue(gradingService.fibmatch("Müsli", "&nbsp;M&uuml;sli&nbsp;", true, true));
		// HTML Ampersand
		Assert.assertTrue(gradingService.fibmatch("C &amp; D", "C &amp; D", false, false));
		Assert.assertTrue(gradingService.fibmatch("C &amp; D", "C & D", false, false));
		Assert.assertTrue(gradingService.fibmatch("C & D", "C & D", false, false));
		Assert.assertTrue(gradingService.fibmatch("C & D", "C &amp; D", false, false));
		// HTML Bracket
		Assert.assertTrue(gradingService.fibmatch("A &gt; ~T", "A &gt; ~T", false, false));
		Assert.assertTrue(gradingService.fibmatch("A &gt; ~T", "A > ~T", false, false));
		Assert.assertTrue(gradingService.fibmatch("A > ~T", "A > ~T", false, false));
		Assert.assertTrue(gradingService.fibmatch("A &gt; ~T", "A &gt; ~T", false, false));
	}

	@Test
	public void fibTestUnicodeApostrophes() {
		GradingService gradingService = new GradingService();

		// Italian course FIB with smart quotes or special keyboard
		Assert.assertTrue(gradingService.fibmatch("un'", "un'", false, true));
		Assert.assertTrue(gradingService.fibmatch("un’", "un'", false, true));
		Assert.assertTrue(gradingService.fibmatch("un’", "un'", true, false));
		Assert.assertTrue(gradingService.fibmatch("un'", "un’", false, true));
		Assert.assertTrue(gradingService.fibmatch("un’", "un‘", false, true));
		Assert.assertFalse(gradingService.fibmatch("un`", "un'", false, true));

		// Mac user with default smart quotes on !!
		Assert.assertTrue(gradingService.fibmatch("Sylvester \"Sly Stone\" Stewart", "Sylvester \"Sly Stone\" Stewart", true, true));
		Assert.assertTrue(gradingService.fibmatch("Sylvester “Sly Stone” Stewart", "Sylvester \"Sly Stone\" Stewart", true, true));
		Assert.assertTrue(gradingService.fibmatch("Sylvester “Sly Stone“ Stewart", "Sylvester \"Sly Stone\" Stewart", true, false));
		Assert.assertFalse(gradingService.fibmatch("Sylvester 'Sly Stone' Stewart", "Sylvester \"Sly Stone\" Stewart", true, true));
		Assert.assertFalse(gradingService.fibmatch("Sylvester ’Sly Stone’ Stewart", "Sylvester \"Sly Stone\" Stewart", true, true));
	}

}
