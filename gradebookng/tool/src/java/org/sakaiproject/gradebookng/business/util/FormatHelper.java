/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.springframework.web.util.HtmlUtils;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.MessageHelper;

@Slf4j
public class FormatHelper {

	@Setter
    private static ResourceLoader RL = new ResourceLoader();

    /**
	 * The value is a double (ie 12.34542) that needs to be formatted as a percentage with two decimal places precision. And drop off any .0
	 * if no decimal places.
	 *
	 * @param score as a double
	 * @return double to decimal places
	 */
	public static String formatDoubleToDecimal(final Double score) {
		if (score == null) {
                        return "";
		}
		else {
                        return formatDoubleToDecimal(score, 2);
		}
	}

	/**
	 * The value is a double (ie 12.34542) that needs to be formatted as a percentage with 'n' decimal places precision. And drop off any .0
	 * if no decimal places.
	 *
	 * @param score as a double
	 * @param n as an int
	 * @return double to n decimal places
	 */
	private static String formatDoubleToDecimal(final Double score, final int n) {
		return formatGrade(convertDoubleToBigDecimal(score, n).toString());
	}

	private static BigDecimal convertDoubleToBigDecimal(final Double score, final int decimalPlaces) {
		// Rounding is problematic due to the use of Doubles in
		// Gradebook. A number like 89.065 (which can be produced by
		// weighted categories, for example) is stored as the double
		// 89.06499999999999772626324556767940521240234375. If you
		// naively round this to two decimal places, you get 89.06 when
		// you wanted 89.07
		//
		// Payten devised a clever trick of rounding to some larger
		// decimal places first, which rounds these numbers up to
		// something more manageable. For example, if you round the
		// above to 10 places, you get 89.0650000000, which rounds
		// correctly when rounded up to 2 places.

		return new BigDecimal(score)
				.setScale(10, RoundingMode.HALF_UP)
				.setScale(decimalPlaces, RoundingMode.HALF_UP);
	}

	// Helper method to consistently round numbers as above with doubles
	private static BigDecimal convertStringToBigDecimal(final String score, final int decimalPlaces) {
		return new BigDecimal(score)
				.setScale(10, RoundingMode.HALF_UP)
				.setScale(decimalPlaces, RoundingMode.HALF_UP);
	}

	/**
	 * Convert a double score to match the number of decimal places exhibited in the toMatch string representation of a number
	 *
	 * @param score as a double
	 * @param toMatch the number as a string
	 * @return double to decimal places
	 */
	public static String formatDoubleToMatch(final Double score, final String toMatch) {
		int numberOfDecimalPlaces = 0;

		if (toMatch.contains(".")) {
			numberOfDecimalPlaces = toMatch.split("\\.")[1].length();
		}

		if (toMatch.contains(",")) {
			numberOfDecimalPlaces = toMatch.split("\\,")[1].length();
		}

		return FormatHelper.formatDoubleToDecimal(score, numberOfDecimalPlaces);
	}

	/**
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision.
	 *
	 * @param score as a double
	 * @return percentage to decimal places with a '%' for good measure
	 */
	public static String formatDoubleAsPercentage(final Double score) {
		return formatGradeForDisplay(score, null) + "%";
	}

	/**
	 * Format the given string as a percentage with two decimal precision. String should be something that can be converted to a number.
	 *
	 * @param string string representation of the number
	 * @return percentage to decimal places with a '%' for good measure
	 */
	public static String formatStringAsPercentage(final String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}

		final BigDecimal decimal = convertStringToBigDecimal(string, 2);

		return formatDoubleAsPercentage(decimal.doubleValue());
	}

	/**
	 * Format a grade, e.g. 00 => 0 0001 => 1 1.0 => 1 1.25 => 1.25 based on the root locale
	 *
	 * @param grade
	 * @return
	 */
	public static String formatGrade(final String grade) {
		return formatGradeForLocale(grade, Locale.ROOT);
	}

	/**
	 * Format a grade, e.g. 00 => 0 0001 => 1 1.0 => 1 1.25 => 1.25 based on the user's locale
	 *
	 * @param grade - string representation of a grade
	 * @return - string formatted per the user's preferred locale
	 */
	public static String formatGradeFromUserLocale(final String grade) {
		return formatGradeForLocale(grade, RL.getLocale());
	}

	/**
	 * Format a grade, e.g. 00 => 0 0001 => 1 1.0 => 1 1.25 => 1.25
	 *
	 * @param grade - string representation of a grade
	 * @return
	 */
	public static String formatGradeForDisplay(Double grade, GradeType gradeType) {
		return formatGradeForDisplay(formatDoubleToDecimal(grade), gradeType);
	}

	/**
	 * Format a grade from the root locale for display using the user's locale
	 *
	 * @param grade - string representation of a grade
	 * @return
	 */
	public static String formatGradeForDisplay(final String grade, GradeType gradeType) {
		if (StringUtils.isBlank(grade)) {
			return "";
		}

		if (gradeType != null && gradeType == GradeType.LETTER) {
			return grade;
		}

		String s;
		try {
			final BigDecimal d = convertStringToBigDecimal(grade, 2);

			final DecimalFormat dfFormat = (DecimalFormat) NumberFormat.getInstance(RL.getLocale());
			dfFormat.setMinimumFractionDigits(0);
			dfFormat.setMaximumFractionDigits(2);
			dfFormat.setGroupingUsed(true);
			s = dfFormat.format(d);
		} catch (final NumberFormatException e) {
			log.warn("Bad format, returning original string: {}", grade);
			s = grade;
		}

		return StringUtils.removeEnd(s, ".0");
	}

	/**
	 * Convert an empty grade to a dash for display purposes
	 *
	 * @param grade
	 * @return a dash if the grade is empty, the original grade if not
	 */
	public static String convertEmptyGradeToDash(final String grade) {
		return StringUtils.defaultIfBlank(grade, " - ");
	}

	/**
	 * Format a grade using the locale
	 *
	 * @param grade - string representation of a grade
	 * @param locale
	 * @return
	 */
	public static String formatGradeForLocale(final String grade, final Locale locale) {
		if (StringUtils.isBlank(grade)) {
			return "";
		}

		String s;
		try {
			final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
			final Double d = df.parse(grade).doubleValue();

			df.setMinimumFractionDigits(0);
			df.setGroupingUsed(false);

			s = df.format(d);
		} catch (final NumberFormatException | ParseException e) {
			log.warn("Bad format, returning original string: {}", grade);
			s = grade;
		}

		return StringUtils.removeEnd(s, ".0");
	}

	/**
	 * Format a date e.g. MM/dd/yyyy
	 *
	 * @param date
	 * @return
	 */
	private static String formatDate(final Date date) {
		final String dateFormatString = MessageHelper.getString("format.date", RL.getLocale());
		final SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
		return df.format(date);
	}

	/**
	 * Format a date but return ifNull if null
	 *
	 * @param date
	 * @param ifNull
	 * @return
	 */
	public static String formatDate(final Date date, final String ifNull) {
		if (date == null) {
			return ifNull;
		}

		return formatDate(date);
	}

	/**
	 * Strips out line breaks
	 *
	 * @param s String to abbreviate
	 * @return string without line breaks
	 */
	public static String stripLineBreaks(final String s) {
		return s.replaceAll("\\r\\n|\\r|\\n", "");
	}

	/**
	 * Abbreviate a string via {@link StringUtils#abbreviateMiddle(String, String, int)}
	 *
	 * Set at 45 chars
	 *
	 * @param s String to abbreviate
	 * @return abbreviated string or full string if it was shorter than the setting
	 */
	public static String abbreviateMiddle(final String s) {
		return StringUtils.abbreviateMiddle(s, "...", 45);
	}

	/**
	 * Validate/convert a Double using the user's Locale.
	 *
	 * @param value - The value validation is being performed on.
	 * @return The parsed Double if valid or null if invalid.
	 */
	public static Double validateDouble(final String value) {
		final DoubleValidator dv = new DoubleValidator();
		return dv.validate(value, RL.getLocale());
	}

	/**
	 * Helper to encode a string and avoid the ridiculous exception that is never thrown
	 *
	 * @param s
	 * @return encoded s
	 */
	public static String encode(final String s) {
		if (StringUtils.isBlank(s)) {
			return s;
		}
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

	/**
	 * Helper to decode a string and avoid the ridiculous exception that is never thrown
	 *
	 * @param s
	 * @return decoded s
	 */
	public static String decode(final String s) {
		if (StringUtils.isBlank(s)) {
			return s;
		}
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

	/**
	 * Returns a list of drop highest/lowest labels based on the settings of the given category.
	 * @param category the category
	 * @return a list of 1 or 2 labels indicating that drop highest/lowest is in use, or an empty list if not in use.
	 */
	public static List<String> formatCategoryDropInfo(CategoryDefinition category) {

		if (category == null) {
			return Collections.emptyList();
		}

		int dropHighest = category.getDropHighest() == null ? 0 : category.getDropHighest();
		int dropLowest = category.getDropLowest() == null ? 0 : category.getDropLowest();
		int keepHighest = category.getKeepHighest() == null ? 0 : category.getKeepHighest();

		if (dropHighest == 0 && dropLowest == 0 && keepHighest == 0) {
			return Collections.emptyList();
		}

		List<String> info = new ArrayList<>(2);
		if (dropHighest > 0) {
			info.add(MessageHelper.getString("label.category.drophighest", RL.getLocale(), dropHighest));
		}
		if (dropLowest > 0) {
			info.add(MessageHelper.getString("label.category.droplowest", RL.getLocale(), dropLowest));
		}
		if (keepHighest > 0) {
			info.add(MessageHelper.getString("label.category.keephighest", RL.getLocale(), keepHighest));
		}

		return info;
	}

	/**
	* Turn special characters into HTML character references. Handles complete character set defined in HTML 4.01 recommendation.
	* Escapes all special characters to their corresponding entity reference (e.g. &lt;) at least as required by the specified encoding. In other words, if a special character does not have to be escaped for the given encoding, it may not be.
	* Reference: http://www.w3.org/TR/html4/sgml/entities.html
	 */
	public static String htmlEscape(String input){
		return HtmlUtils.htmlEscape(input, StandardCharsets.UTF_8.name());
	}
	
	public static String htmlUnescape(String input){
		return HtmlUtils.htmlUnescape(input);
	}

	/**
	 * Helper to accept numerical grades and get the scale value.
	 * Returns the scale whose value equals to the numeric value received, or if it doesn't exists, the highest value lower.
	 *
	 * @param newGrade the grade to convert
	 * @param schema the current schema of Gradebook
	 * @param currentUserLocale the locale to format the grade with the right decimal separator
	 * @return fully formatted string ready for display
	 */
	public static String getGradeFromNumber(String newGrade, Map<String, Double> schema, Locale currentUserLocale) {
		Double currentGradeValue = 0.0;
		Double maxValue = 0.0;
		String returnGrade = newGrade;
		try	{
			NumberFormat nf = NumberFormat.getInstance(currentUserLocale);
			ParsePosition parsePosition = new ParsePosition(0);
			Number n = nf.parse(newGrade,parsePosition);
			if (parsePosition.getIndex() != newGrade.length())
				throw new NumberFormatException("Grade has a bad format.");
			Double dValue = n.doubleValue();

			for (Entry<String, Double> entry : schema.entrySet()) {
				Double tempValue = entry.getValue();
				if (dValue.equals(tempValue)) {
					return entry.getKey();
				}
				else {
					if (maxValue.compareTo(tempValue) < 0) maxValue=tempValue;
					if ((dValue.compareTo(tempValue) > 0 ) && (tempValue.compareTo(currentGradeValue) >= 0 )) {
						currentGradeValue = tempValue;
						returnGrade=entry.getKey();
					}
				}
				if (dValue < 0) throw new NumberFormatException("Grade cannot be lower than 0.");
				if (dValue.compareTo(maxValue) > 0 && dValue > 100) throw new NumberFormatException("Grade exceeds the maximum number allowed in current scale.");
			}
			return returnGrade;
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Grade is not a number, neither a scale value.");
		}
	}

	/**
	 *
	 * @param gradeScale the scale value to convert
	 * @param schema the current schema of Gradebook
	 * @param currentUserLocale the locale to format the grade with the right decimal separator
	 * @return the grade
	 */
	public static String getNumberFromGrade(String gradeScale, Map<String, Double> schema, Locale currentUserLocale) {
		Double newGrade = schema.get(gradeScale);
		NumberFormat nf = NumberFormat.getInstance(currentUserLocale);
		return nf.format(newGrade);
	}

	/**
	 *
	 * @param newGrade the grade to transform
	 * @param locale the locale to format the grade with the right decimal separator
	 * @return the new grade
	 */
	public static String transformNewGrade(String newGrade, Locale locale) {
		try	{
			NumberFormat nf = NumberFormat.getInstance(locale);
			ParsePosition parsePosition = new ParsePosition(0);
			Number n = nf.parse(newGrade,parsePosition);
			if (parsePosition.getIndex() != newGrade.length()) {
				throw new NumberFormatException("Grade has a bad format.");
			}
			double dValue = n.doubleValue();

			return (Double.toString(dValue));

		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Grade is not a number.");
		}
	}


	/*
	 *
	 * Method to normalize a grade by removing trailing ".0" or ",0" and trimming it to null if it becomes empty
	 * @param grade String to transform
	 * @return normalized grade stripped of trailing .0 or ,0
	 */
	public static String normalizeGrade(String grade) {
		if (grade == null) {
			return null;
		}

		// Remove ".0" and ",0" suffixes, then trim the result to null if it's empty
		String adjustedGrade = StringUtils.removeEnd(grade, ".0");
		adjustedGrade = StringUtils.removeEnd(adjustedGrade, ",0");
		return StringUtils.trimToNull(adjustedGrade);
	}

}
