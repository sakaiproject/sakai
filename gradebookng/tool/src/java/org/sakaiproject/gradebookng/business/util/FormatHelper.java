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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

@Slf4j
public class FormatHelper {

	private static ResourceLoader rl = new ResourceLoader();

	/**
	 * The value is a double (ie 12.34542) that needs to be formatted as a percentage with two decimal places precision. And drop off any .0
	 * if no decimal places.
	 *
	 * @param score as a double
	 * @return double to decimal places
	 */
	public static String formatDoubleToDecimal(final Double score) {
		return formatDoubleToDecimal(score, 2);
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
		return formatGradeForDisplay(score) + "%";
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

		final BigDecimal decimal = new BigDecimal(string).setScale(2, RoundingMode.HALF_UP);

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
	 * @return
	 */
	public static String formatGradeFromUserLocale(final String grade) {
		return formatGradeForLocale(grade, rl.getLocale());
	}

	/**
	 * Format a grade, e.g. 00 => 0 0001 => 1 1.0 => 1 1.25 => 1.25
	 *
	 * @param grade - string representation of a grade
	 * @return
	 */
	public static String formatGradeForDisplay(final Double grade) {
		return formatGradeForDisplay(formatDoubleToDecimal(grade));
	}

	/**
	 * Format a grade from the root locale for display using the user's locale
	 *
	 * @param grade - string representation of a grade
	 * @return
	 */
	public static String formatGradeForDisplay(final String grade) {
		if (StringUtils.isBlank(grade)) {
			return "";
		}

		String s;
		try {
			final DecimalFormat dfParse = (DecimalFormat) NumberFormat.getInstance(Locale.ROOT);
			dfParse.setParseBigDecimal(true);
			final BigDecimal d = (BigDecimal) dfParse.parse(grade);

			final DecimalFormat dfFormat = (DecimalFormat) NumberFormat.getInstance(rl.getLocale());
			dfFormat.setMinimumFractionDigits(0);
			dfFormat.setMaximumFractionDigits(2);
			dfFormat.setGroupingUsed(true);
			s = dfFormat.format(d);
		} catch (final NumberFormatException | ParseException e) {
			log.debug("Bad format, returning original string: {}", grade);
			s = grade;
		}

		return StringUtils.removeEnd(s, ".0");
	}

	/**
	 * Format a grade using the locale
	 *
	 * @param grade - string representation of a grade
	 * @param locale
	 * @return
	 */
	private static String formatGradeForLocale(final String grade, final Locale locale) {
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
			log.debug("Bad format, returning original string: {}", grade);
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
		final String dateFormatString = MessageHelper.getString("format.date");
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
	 * Format a date with a time e.g. MM/dd/yyyy HH:mm
	 *
	 * @param date
	 * @return
	 */
	public static String formatDateTime(final Date date) {
		final String dateTimeFormatString = MessageHelper.getString("format.datetime");
		final SimpleDateFormat df = new SimpleDateFormat(dateTimeFormatString);
		return df.format(date);
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
	 * Validate if a string is a valid Double using the specified Locale.
	 *
	 * @param value - The value validation is being performed on.
	 * @return true if the value is valid
	 */
	public static boolean isValidDouble(final String value) {
		final DoubleValidator dv = new DoubleValidator();
		return dv.isValid(value, rl.getLocale());
	}

	/**
	 * Validate/convert a Double using the user's Locale.
	 *
	 * @param value - The value validation is being performed on.
	 * @return The parsed Double if valid or null if invalid.
	 */
	public static Double validateDouble(final String value) {
		final DoubleValidator dv = new DoubleValidator();
		return dv.validate(value, rl.getLocale());
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
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 not supported");
		}
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
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 not supported");
		}
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
			info.add(MessageHelper.getString("label.category.drophighest", dropHighest));
		}
		if (dropLowest > 0) {
			info.add(MessageHelper.getString("label.category.droplowest", dropLowest));
		}
		if (keepHighest > 0) {
			info.add(MessageHelper.getString("label.category.keephighest", keepHighest));
		}

		return info;
	}
}
