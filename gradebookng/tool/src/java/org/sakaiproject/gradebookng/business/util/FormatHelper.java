package org.sakaiproject.gradebookng.business.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class FormatHelper {

	/**
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision.
	 * @param score as a double
	 * @return percentage to decimal places with a '%' for good measure
	 */
	public static String formatDoubleAsPercentage(Double score) {
		NumberFormat df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(2);
		df.setRoundingMode(RoundingMode.DOWN);

		//TODO does the % need to be internationalised?
		return df.format(score) + "%";
	}


	public static String formatStringAsPercentage(String string) {
		if(StringUtils.isBlank(string)) {
			return null;
		}

		BigDecimal decimal = new BigDecimal(string);
		decimal.setScale(2, RoundingMode.HALF_DOWN); //same as GradebookService

		return formatDoubleAsPercentage(decimal.doubleValue());
	}


	/**
	 * Format a grade, e.g.
	 * 00 => 0
	 * 0001 => 1
	 * 1.0 => 1
	 * 1.25 => 1.25
	 * @param grade
	 * @return
	 */
	public static String formatGrade(String grade) {
		if (StringUtils.isBlank(grade)) {
			return "";
		}
		return StringUtils.removeEnd(String.valueOf(Double.parseDouble(grade)), ".0");
	}


	/**
	 * Format a date e.g. MM/dd/yyyy
	 *
	 * @param date
	 * @return
	 */
	private static String formatDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy"); //TODO needs to come from i18n
		return df.format(date);
	}


	/**
	 * Format a date but return ifNull if null
	 *
	 * @param date
	 * @param ifNull
	 * @return
	 */
	public static String formatDate(Date date, String ifNull) {
		if(date == null) {
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
	public static String formatDateTime(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //TODO needs to come from i18n
		return df.format(date);
	}
}
