/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.util;

import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.util.ResourceLoader;

public class TestFormatHelper {

	private Map<String, Double> schema;
	private Locale spanishLocale;
	private ResourceLoader resourceLoader;

	@Before
	public void setup() {
		spanishLocale = new Locale("es", "ES");

		resourceLoader = Mockito.mock(ResourceLoader.class);
		FormatHelper.setRL(resourceLoader);

		schema = Map.of(
				"F", 0.0,
				"D", 60.0,
				"C", 70.0,
				"B", 80.0,
				"A", 90.0
		);
	}
	
	@Test
	public void testCalculateGradeFromNumber() {

		String resultScale;
		String gradeNumber = "85";
		String gradeScale = "B";

		resultScale = FormatHelper.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(gradeScale, resultScale);

		gradeNumber = "30";
		gradeScale = "F";

		resultScale = FormatHelper.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(gradeScale, resultScale);

		gradeNumber = "60";
		gradeScale = "D";

		resultScale = FormatHelper.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(gradeScale, resultScale);
	}

	@Test
	public void testCalculateNumberFromGrade() {

		String resultScale;
		String gradeNumber = "80";
		String gradeScale = "B";

		resultScale = FormatHelper.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(gradeNumber, resultScale);

		gradeNumber = "0";
		gradeScale = "F";

		resultScale = FormatHelper.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(gradeNumber, resultScale);

		gradeNumber = "60";
		gradeScale = "D";

		resultScale = FormatHelper.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(gradeNumber, resultScale);
	}
	@Test
	public void normalizeGradeRemovesTrailingZero() {
		String grade = "89.0";
		String expected = "89";

		String actual = FormatHelper.normalizeGrade(grade);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void normalizeGradeRemovesTrailingZeroWithComma() {
		String grade = "89,0";
		String expected = "89";

		String actual = FormatHelper.normalizeGrade(grade);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void normalizeGradeReturnsNullForEmptyString() {
		String grade = "";
		String expected = null;

		String actual = FormatHelper.normalizeGrade(grade);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void normalizeGradeReturnsNullForNullInput() {
		String grade = null;
		String expected = null;

		String actual = FormatHelper.normalizeGrade(grade);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testFormatDoubleToDecimal() {
		Assert.assertEquals("", FormatHelper.formatDoubleToDecimal(null));
		Assert.assertEquals("89.07", FormatHelper.formatDoubleToDecimal(89.065));
		Assert.assertEquals("90", FormatHelper.formatDoubleToDecimal(90.0));
	}

	@Test
	public void testFormatDoubleToMatch() {
		Assert.assertEquals("89.07", FormatHelper.formatDoubleToMatch(89.065, "90.07"));
		Assert.assertEquals("90", FormatHelper.formatDoubleToMatch(90.0, "80"));
	}

	@Test
	public void testFormatDoubleAsPercentage() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals("89.07%", FormatHelper.formatDoubleAsPercentage(89.065));
		Assert.assertEquals("90%", FormatHelper.formatDoubleAsPercentage(90.0));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("89,07%", FormatHelper.formatDoubleAsPercentage(89.065));
		Assert.assertEquals("90%", FormatHelper.formatDoubleAsPercentage(90.0));
	}

	@Test
	public void testFormatStringAsPercentage() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertNull(FormatHelper.formatStringAsPercentage(""));
		Assert.assertNull(FormatHelper.formatStringAsPercentage(null));
		Assert.assertEquals("89.07%", FormatHelper.formatStringAsPercentage("89.065"));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("89,07%", FormatHelper.formatStringAsPercentage("89.065"));
	}

	@Test
	public void testFormatGrade() {
		Assert.assertEquals("", FormatHelper.formatGrade(""));
		Assert.assertEquals("89.065", FormatHelper.formatGrade("89.065"));
		Assert.assertEquals("90", FormatHelper.formatGrade("90.0"));
	}

	@Test
	public void testConvertEmptyGradeToDash() {
		Assert.assertEquals(" - ", FormatHelper.convertEmptyGradeToDash(""));
		Assert.assertEquals(" - ", FormatHelper.convertEmptyGradeToDash(null));
		Assert.assertEquals("90", FormatHelper.convertEmptyGradeToDash("90"));
	}

	@Test
	public void testStripLineBreaks() {
		Assert.assertEquals("abc", FormatHelper.stripLineBreaks("abc\r\n"));
		Assert.assertEquals("abc", FormatHelper.stripLineBreaks("abc\n"));
		Assert.assertEquals("abc", FormatHelper.stripLineBreaks("abc\r"));
	}

	@Test
	public void testAbbreviateMiddle() {
		Assert.assertEquals("short", FormatHelper.abbreviateMiddle("short"));
		Assert.assertEquals("verylongstringthatsho...atshouldbeabbreviated", FormatHelper.abbreviateMiddle("verylongstringthatshouldbeabbreviated verylongstringthatshouldbeabbreviated"));
	}

	@Test
	public void testValidateDouble() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals(Double.valueOf(90.5), FormatHelper.validateDouble("90.5"));
		Assert.assertEquals(Double.valueOf(905.0), FormatHelper.validateDouble("90,5")); // In the US, comma is a thousand's separator
		Assert.assertNull(FormatHelper.validateDouble("invalid"));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals(Double.valueOf(90.5), FormatHelper.validateDouble("90,5"));
		Assert.assertEquals(Double.valueOf(905.0), FormatHelper.validateDouble("90.5")); // In Spain, a dot is a thousand's separator
	}

	@Test
	public void testEncodeDecode() {
		String original = "test string with spaces & special chars!";
		String encoded = FormatHelper.encode(original);
		String decoded = FormatHelper.decode(encoded);
		Assert.assertEquals(original, decoded);
		Assert.assertEquals("", FormatHelper.encode(""));
		Assert.assertEquals("", FormatHelper.decode(""));
	}

	@Test
	public void testHtmlEscapeUnescape() {
		String original = "<p>Test & example</p>";
		String escaped = FormatHelper.htmlEscape(original);
		String unescaped = FormatHelper.htmlUnescape(escaped);
		Assert.assertEquals(original, unescaped);
	}

	@Test
	public void testTransformNewGrade() {
		Assert.assertEquals("90.5", FormatHelper.transformNewGrade("90.5", Locale.US));
		Assert.assertEquals("90.5", FormatHelper.transformNewGrade("90,5", Locale.GERMANY));
	}

	@Test
	public void testFormatGradeFromUserLocale() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals("", FormatHelper.formatGradeFromUserLocale(""));
		Assert.assertEquals("89.065", FormatHelper.formatGradeFromUserLocale("89.065"));
		Assert.assertEquals("90", FormatHelper.formatGradeFromUserLocale("90.0"));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("89,065", FormatHelper.formatGradeFromUserLocale("89,065"));
	}

	@Test
	public void testFormatGradeForDisplayDouble() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals("", FormatHelper.formatGradeForDisplay((Double) null, GradeType.POINTS));
		Assert.assertEquals("90", FormatHelper.formatGradeForDisplay(90.0, GradeType.POINTS));
		Assert.assertEquals("89.07", FormatHelper.formatGradeForDisplay(89.065, GradeType.POINTS));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("89,07", FormatHelper.formatGradeForDisplay(89.065, GradeType.POINTS));
	}

	@Test
	public void testFormatGradeForDisplayString() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals("", FormatHelper.formatGradeForDisplay("", GradeType.POINTS));
		Assert.assertEquals("90", FormatHelper.formatGradeForDisplay("90.0", GradeType.POINTS));
		Assert.assertEquals("89.07", FormatHelper.formatGradeForDisplay("89.065", GradeType.POINTS));
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("89,07", FormatHelper.formatGradeForDisplay("89.065", GradeType.POINTS));
	}

	@Test
	public void testFormatDateWithNull() {
		Assert.assertEquals("not available", FormatHelper.formatDate(null, "not available"));
	}

	@Test
	public void testFormatCategoryDropInfo() {
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals(0, FormatHelper.formatCategoryDropInfo(null).size());
		
		CategoryDefinition category = new CategoryDefinition();
		Assert.assertEquals(0, FormatHelper.formatCategoryDropInfo(category).size());
		
		category.setDropHighest(1);
		Assert.assertEquals(1, FormatHelper.formatCategoryDropInfo(category).size());
		
		category.setDropLowest(1);
		Assert.assertEquals(2, FormatHelper.formatCategoryDropInfo(category).size());
		
		category.setKeepHighest(1);
		Assert.assertEquals(3, FormatHelper.formatCategoryDropInfo(category).size());
	}

	@Test
	public void testCalculateGradeFromNumberWithSpanishLocale() {
		String resultScale;
		String gradeNumber = "85,5"; // spanishLocale format
		String gradeScale = "B";

		resultScale = FormatHelper.getGradeFromNumber(gradeNumber, schema, spanishLocale);
		Assert.assertEquals(gradeScale, resultScale);

		gradeNumber = "30,7";
		gradeScale = "F";

		resultScale = FormatHelper.getGradeFromNumber(gradeNumber, schema, spanishLocale);
		Assert.assertEquals(gradeScale, resultScale);
	}

	@Test
	public void testCalculateNumberFromGradeWithSpanishLocale() {
		String resultScale;
		String gradeScale = "B";

		resultScale = FormatHelper.getNumberFromGrade(gradeScale, schema, spanishLocale);
		Assert.assertEquals("80", resultScale);  // Should be "80" not "80,0" as it's an integer

		gradeScale = "F";
		resultScale = FormatHelper.getNumberFromGrade(gradeScale, schema, spanishLocale);
		Assert.assertEquals("0", resultScale);
	}

	@Test
	public void testTransformNewGradeWithLocales() {
		// US locale tests
		Assert.assertEquals("90.5", FormatHelper.transformNewGrade("90.5", Locale.US));
		Assert.assertEquals("905.0", FormatHelper.transformNewGrade("90,5", Locale.US));
		
		// spanishLocale locale tests
		Assert.assertEquals("90.5", FormatHelper.transformNewGrade("90,5", spanishLocale));
		Assert.assertEquals("905.0", FormatHelper.transformNewGrade("90.5", spanishLocale));
		
		// Test with more complex decimals
		Assert.assertEquals("90.567", FormatHelper.transformNewGrade("90,567", spanishLocale));
		Assert.assertEquals("1234.567", FormatHelper.transformNewGrade("1.234,567", spanishLocale));
	}

	@Test(expected = NumberFormatException.class)
	public void testTransformNewGradeWithInvalidSpanishFormat() {
		FormatHelper.transformNewGrade("1,234.567", spanishLocale); // Invalid spanishLocale format
	}

	@Test
	public void testValidateDoubleWithLocales() {
		// Test with US locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals(Double.valueOf(90.5), FormatHelper.validateDouble("90.5"));
		Assert.assertEquals(Double.valueOf(905.0), FormatHelper.validateDouble("90,5")); // In the US, comma is a thousand's separator
		
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals(Double.valueOf(90.5), FormatHelper.validateDouble("90,5"));
		Assert.assertEquals(Double.valueOf(905.0), FormatHelper.validateDouble("90.5")); // In Spain, a dot is a thousand's separator
	}

	@Test
	public void testFormatGradeForDisplayWithLocales() {
		// Test with spanishLocale locale
		Mockito.when(resourceLoader.getLocale()).thenReturn(spanishLocale);
		Assert.assertEquals("90,57", FormatHelper.formatGradeForDisplay("90.567", GradeType.POINTS));
		Assert.assertEquals("1.234,57", FormatHelper.formatGradeForDisplay("1234.567", GradeType.POINTS));
		
		// Test with the US locale (reset to default)
		Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.US);
		Assert.assertEquals("90.57", FormatHelper.formatGradeForDisplay("90.567", GradeType.POINTS));
		Assert.assertEquals("1,234.57", FormatHelper.formatGradeForDisplay("1234.567", GradeType.POINTS));
		// No need to restore locale as @After will do it
	}

	@Test
	public void testFormatGradeForDisplayWithLetterGrade() {
		Assert.assertEquals("A+", FormatHelper.formatGradeForDisplay("A+", GradeType.LETTER));
	}
}
