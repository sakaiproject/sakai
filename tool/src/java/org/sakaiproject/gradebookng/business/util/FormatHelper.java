package org.sakaiproject.gradebookng.business.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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


	/**
	 * Format a grade to remove the .0 if present.
	 * @param grade
	 * @return
	 */
	public static String formatGrade(String grade) {
		return StringUtils.removeEnd(grade, ".0");
	}
}
