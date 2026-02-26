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

import java.util.HashMap;
import java.util.List;
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

	@Test
	public void fibContainsIllegalsTest() {
		GradingService gradingService = new GradingService();
		
		//The forbidden characters "'.,&<>\s|*
		Assert.assertEquals(3, gradingService.checkMarkersFIB("\"}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB("'}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB(".}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB(",}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB("&}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB("<}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB(">}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB(" }"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB("|}"));
		Assert.assertEquals(3, gradingService.checkMarkersFIB("*}"));
		
		//Same char as marker
		Assert.assertEquals(2, gradingService.checkMarkersFIB("xx"));
		Assert.assertEquals(2, gradingService.checkMarkersFIB("{{"));
		Assert.assertEquals(2, gradingService.checkMarkersFIB("}}"));
		Assert.assertEquals(2, gradingService.checkMarkersFIB("[["));
		Assert.assertEquals(2, gradingService.checkMarkersFIB("]]"));
		Assert.assertEquals(2, gradingService.checkMarkersFIB("  "));
		
		//Invalid lentgh
		Assert.assertEquals(1, gradingService.checkMarkersFIB(""));
		Assert.assertEquals(1, gradingService.checkMarkersFIB("{"));
		Assert.assertEquals(1, gradingService.checkMarkersFIB("{}+"));
		Assert.assertEquals(1, gradingService.checkMarkersFIB("abcd"));
		Assert.assertEquals(1, gradingService.checkMarkersFIB("{ }"));
		Assert.assertEquals(1, gradingService.checkMarkersFIB(" {}"));
		Assert.assertEquals(1, gradingService.checkMarkersFIB("{} "));
		
		//Valid markers
		Assert.assertEquals(0, gradingService.checkMarkersFIB("{}"));
		Assert.assertEquals(0, gradingService.checkMarkersFIB("ab"));
		Assert.assertEquals(0, gradingService.checkMarkersFIB("xw"));
		Assert.assertEquals(0, gradingService.checkMarkersFIB("[]"));
		Assert.assertEquals(0, gradingService.checkMarkersFIB("-+"));
		Assert.assertEquals(0, gradingService.checkMarkersFIB("$%"));
	}
	
	@Test
	public void checkPairErrorsFIBTest() {
		GradingService gradingService = new GradingService();
		
		//Basic correct FIB
		Assert.assertFalse(gradingService.checkPairErrorsFIB("Roses are {red}", "{}"));
		//Empty text
		Assert.assertTrue(gradingService.checkPairErrorsFIB("", "{}"));
		//Empty text, just open mark
		Assert.assertTrue(gradingService.checkPairErrorsFIB("{", "{}"));
		//Empty text, just close mark
		Assert.assertTrue(gradingService.checkPairErrorsFIB("}", "{}"));
		//Empty text, valid marks
		Assert.assertTrue(gradingService.checkPairErrorsFIB("{}", "{}"));
		//Missing close mark
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are {red", "{}"));
		//Missing open mark
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are red}", "{}"));
		//two open, one close marks
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are {{red}", "{}"));
		//One open, two close marks
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are {{red}", "{}"));
		//Swap markers
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are }red{", "{}"));
		
		//Two words correct FIB
		Assert.assertFalse(gradingService.checkPairErrorsFIB("Roses are +red-, violets are +blue-", "+-"));
		//Many words in one, correct FIB
		Assert.assertFalse(gradingService.checkPairErrorsFIB("Roses are +red, violets are blue-", "+-"));
		//One empty pair
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red-, violets are +-", "+-"));
		//One empty pair
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +-, violets are +blue-", "+-"));
		//both empty pairs
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +-, violets are +-", "+-"));
		//Missing one marker at the end
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red-, violets are +blue", "+-"));
		//Missing one open marker
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red-, violets are blue-", "+-"));
		//Missing all close markers
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red, violets are +blue", "+-"));
		//Missing all open markers
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are red-, violets are blue-", "+-"));
		//Swap markers
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red+, violets are -blue-", "+-"));
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are +red-, violets are -blue+", "+-"));
		Assert.assertTrue(gradingService.checkPairErrorsFIB("Roses are -red+, violets are -blue+", "+-"));

		// SAK-52033: slash and backslash markers must work even with rich text HTML wrappers
		Assert.assertFalse(gradingService.checkPairErrorsFIB("\\word/", "\\/"));
		Assert.assertFalse(gradingService.checkPairErrorsFIB("/word\\", "/\\"));
		Assert.assertFalse(gradingService.checkPairErrorsFIB("<p>\\word/</p>", "\\/"));
		Assert.assertFalse(gradingService.checkPairErrorsFIB("<p>/word\\</p>", "/\\"));
		Assert.assertFalse(gradingService.checkPairErrorsFIB("<p>\\red/ and \\blue/</p>", "\\/"));
		Assert.assertFalse(gradingService.checkPairErrorsFIB("<p>/red\\ and /blue\\</p>", "/\\"));
		Assert.assertTrue(gradingService.checkPairErrorsFIB("<p>\\word</p>", "\\/"));
		
	}
	
	@Test
	public void fibParseTest() {
		//Get some examples of how fib parsing is working.
		//The tested method just try to extract words between
		// markers, no errors or conditions are checked here
		GradingService gradingService = new GradingService();
		
		List answers = gradingService.parseFillInBlank("Roses are {red}, violets are {blue}", "{}");
		Assert.assertEquals(2, answers.size());
		Assert.assertEquals("red", ((HashMap) answers.get(0)).get("ans"));
		Assert.assertEquals("blue", ((HashMap) answers.get(1)).get("ans"));
		
		answers = gradingService.parseFillInBlank("Roses are +red-, the violet's +blue-", "+-");
		Assert.assertEquals(2, answers.size());
		Assert.assertEquals("red", ((HashMap) answers.get(0)).get("ans"));
		Assert.assertEquals("blue", ((HashMap) answers.get(1)).get("ans"));
		
		answers = gradingService.parseFillInBlank(
				"Roses are xredn, the violet's xbluen, sugar is xsweetn", "xn");
		Assert.assertEquals(3, answers.size());
		Assert.assertEquals("red", ((HashMap) answers.get(0)).get("ans"));
		Assert.assertEquals("blue", ((HashMap) answers.get(1)).get("ans"));
		Assert.assertEquals("sweet", ((HashMap) answers.get(2)).get("ans"));
	}
}
