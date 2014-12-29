/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
package org.sakaiproject.tool.podcasts.util;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs date validation respecting i18n.<br>
 * <b>Note:</b> This class does not support "hi_IN", "ja_JP_JP" and "th_TH" locales.
 */
public final class DateUtil {

	private DateUtil() {
	}

	/**
	 * Performs date validation checking like Feb 30, etc.
	 *
	 * @param date
	 *            The candidate String date.
	 * @param format
	 *            The given date-time format.
	 * @param locale
	 *            The given locale.
	 * @return TRUE - Conforms to a valid input date format string.<br>
	 *         FALSE - Does not conform.
	 */
	public static boolean isValidDate(final String date, final String format, final Locale locale) {
		if (date == null || format == null) {
			return false;
		}

		List<String> replaced = new ArrayList<String>();
		String regex = createRegexFromDateFormat(format.trim(), replaced);

		DateFormatSymbols dateFormatSymbols;
		if (locale == null) {
			dateFormatSymbols = DateFormatSymbols.getInstance();
		} else {
			dateFormatSymbols = DateFormatSymbols.getInstance(locale);
		}

		// Check date
		int year = 0;
		int month = 1;
		int day = 1;
		Matcher matcher = Pattern.compile(regex).matcher(Matcher.quoteReplacement(date.trim()));
		if (!matcher.find()) {
			// Invalid date
			return false;
		}
		for (int group = 1; group <= matcher.groupCount(); group++) {
			boolean isValid;
			String[] strs;

			switch (replaced.get(group - 1).charAt(0)) {
			case 'y':	// Year
			case 'Y':	// Week year
				year = Integer.parseInt(matcher.group(group));
				if (year < 1582) {
					// Allow only 4 digits and Gregorian
					return false;
				}
				break;
			case 'M':	// Month in year
				try {
					month = Integer.parseInt(matcher.group(group));
				} catch (NumberFormatException e) {
					// Maybe Jan, January, ...
					isValid = false;
					for (int i = 0; i < 4 && !isValid; i++) {
						switch (i) {
						case 0:
						default:
							strs = DateFormatSymbols.getInstance(Locale.ENGLISH).getMonths();
							break;
						case 1:
							strs = DateFormatSymbols.getInstance(Locale.ENGLISH).getShortMonths();
							break;
						case 2:
							strs = dateFormatSymbols.getMonths();
							break;
						case 3:
							strs = dateFormatSymbols.getShortMonths();
							break;
						}
						for (int num = Calendar.JANUARY; num <= Calendar.DECEMBER; num++) {
							if (strs[num].equalsIgnoreCase(matcher.group(group))) {
								isValid = true;
								month = num + 1;
								break;
							}
						}
					}
					if (!isValid) {
						return false;
					}
				}
				if (month < 1 || month > 12) {
					return false;
				}
				break;
			case 'w':	// Week in year
				int num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 53) {
					return false;
				}
				break;
			case 'W':	// Week in month
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 6) {
					return false;
				}
				break;
			case 'D':	// Day in year
				num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 366) {
					return false;
				}
				break;
			case 'd':	// Day in month
				day = Integer.parseInt(matcher.group(group));
				if (day < 1 || day > 31) {
					return false;
				}
				break;
			case 'F':	// Day of week in month
				num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 5) {
					return false;
				}
				break;
			case 'E':	// Day name in week
				isValid = false;
				for (int i = 0; i < 4 && !isValid; i++) {
					switch (i) {
					case 0:
					default:
						strs = DateFormatSymbols.getInstance(Locale.ENGLISH).getWeekdays();
						break;
					case 1:
						strs = DateFormatSymbols.getInstance(Locale.ENGLISH).getShortWeekdays();
						break;
					case 2:
						strs = dateFormatSymbols.getWeekdays();
						break;
					case 3:
						strs = dateFormatSymbols.getShortWeekdays();
						break;
					}
					for (num = Calendar.SUNDAY; num <= Calendar.SATURDAY; num++) {
						if (strs[num].equalsIgnoreCase(matcher.group(group))) {
							isValid = true;
							break;
						}
					}
				}
				if (!isValid) {
					return false;
				}
				break;
			case 'u':	// Day number of week (1 = Monday, ..., 7 = Sunday)
				num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 7) {
					return false;
				}
				break;
			case 'a':	// Am/pm marker
				isValid = false;
				for (int i = 0; i < 2 && !isValid; i++) {
					switch (i) {
					case 0:
					default:
						strs = DateFormatSymbols.getInstance(Locale.ENGLISH).getAmPmStrings();
						break;
					case 1:
						strs = dateFormatSymbols.getAmPmStrings();
						break;
					}
					for (num = Calendar.AM; num <= Calendar.PM; num++) {
						if (strs[num].equalsIgnoreCase(matcher.group(group))) {
							isValid = true;
							break;
						}
					}
				}
				if (!isValid) {
					return false;
				}
				break;
			case 'H':	// Hour in day (0-23)
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 23) {
					return false;
				}
				break;
			case 'k':	// Hour in day (1-24)
				num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 24) {
					return false;
				}
				break;
			case 'K':	// Hour in am/pm (0-11)
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 11) {
					return false;
				}
				break;
			case 'h':	// Hour in am/pm (1-12)
				num = Integer.parseInt(matcher.group(group));
				if (num < 1 || num > 12) {
					return false;
				}
				break;
			case 'm':	// Minute in hour
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 59) {
					return false;
				}
				break;
			case 's':	// Second in minute
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 60) {
					// Include leap sec
					return false;
				}
				break;
			case 'S':	// Millisecond
				num = Integer.parseInt(matcher.group(group));
				if (num < 0 || num > 999) {
					return false;
				}
				break;
			default:
				break;
			}
		}

		return checkDate(day, month, year);
	}

	/**
	 * Validate whether the date input is valid.
	 */
	private static boolean checkDate(final int day, final int month, final int year) {
		// Is date valid for month?
		if (month == 2) {
			// Check for leap year
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				// leap year
				if (day > 29) {
					return false;
				}
			} else {
				// normal year
				if (day > 28) {
					return false;
				}
			}
		} else if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
			return false;
		}

		return true;
	}

	/**
	 * Create a regular expression replacing the given date-time format.
	 * The created regular expression does not allow digit number over.<br><br>
	 * e.g., "dd/MM/yyyy hh:mm:ss a" -> "^(-?\d{1,2})/(-?\d{1,2})/(-?\d{1,4}) (-?\d{1,2}):(-?\d{1,2}):(-?\d{1,2}) (.+)$"
	 *
	 * @param format
	 *            The date-time format to be replaced.
	 * @param replaced
	 *            The list to keep replaced date-time patterns.
	 * @return The regular expression.
	 */
	private static String createRegexFromDateFormat(final String format, final List<String> replaced) {
		if (format == null) {
			return null;
		} else if (replaced != null) {
			replaced.clear();
		}

		// Create regular expression to match date-time patterns
		StringBuffer sb = new StringBuffer("'{1,2}");
		char[] patternChars = "GyMdkHmsSEDFwWahKzZYuX".toCharArray();
		for (char patternChar : patternChars) {
			sb.append('|');
			sb.append(patternChar);
			sb.append('+');
		}

		// Replace date-time pattern with regular expression in format
		Matcher matcher = Pattern.compile(sb.toString()).matcher('^' + Matcher.quoteReplacement(format) + '$');
		boolean quoted = false;
		StringBuffer replacement = new StringBuffer();
		sb = new StringBuffer();
		while (matcher.find()) {
			char patternChar = matcher.group().charAt(0);
			if (patternChar == '\'') {
				if (matcher.group().length() == 1) {
					// Replace ' with empty string
					matcher.appendReplacement(sb, "");
					quoted ^= true;
				} else if (matcher.group().length() == 2) {
					// Replace '' with '
					matcher.appendReplacement(sb, "'");
				}
				continue;
			}
			if (quoted) {
				continue;
			}

			replacement.setLength(0);
			replacement.append('(');
			switch (patternChar) {
			case 'y':	// Year
			case 'Y':	// Week year
				replacement.append("-?\\\\d{1,");
				if (matcher.group().length() <= 4) {
					// Replace y, yy, yyy, yyyy with \d{1,4}
					replacement.append(4);
				} else {
					// Replace yyyyy, yyyyyy, ... with \d{1,x}
					replacement.append(matcher.group().length());
				}
				replacement.append('}');
				break;
			case 'M':	// Month in year
				if (matcher.group().length() <= 2) {
					// Replace M or MM with \d{1,2}
					replacement.append("-?\\\\d{1,2}");
				} else {
					// Replace MMM, MMMM, ... with .+
					replacement.append(".+");
				}
				break;
			case 'D':	// Day in year
			case 'S':	// Millisecond
				replacement.append("-?\\\\d{1,");
				if (matcher.group().length() <= 3) {
					// Replace D or DD or DDD with \d{1,3}
					replacement.append(3);
				} else {
					// Replace DDDD, DDDDD, ... with \d{1,x}
					replacement.append(matcher.group().length());
				}
				replacement.append('}');
				break;
			case 'w':	// Week in year
			case 'd':	// Day in month
			case 'H':	// Hour in day (0-23)
			case 'k':	// Hour in day (1-24)
			case 'K':	// Hour in am/pm (0-11)
			case 'h':	// Hour in am/pm (1-12)
			case 'm':	// Minute in hour
			case 's':	// Second in minute
				replacement.append("-?\\\\d{1,");
				if (matcher.group().length() <= 2) {
					// Replace d or dd with \d{1,2}
					replacement.append(2);
				} else {
					// Replace ddd, dddd, ... with \d{1,x}
					replacement.append(matcher.group().length());
				}
				replacement.append('}');
				break;
			case 'W':	// Week in month
			case 'F':	// Day of week in month
			case 'u':	// Day number of week (1 = Monday, ..., 7 = Sunday)
				replacement.append("-?\\\\d");
				if (matcher.group().length() > 1) {
					replacement.append("{1," + matcher.group().length() + "}");
				}
				break;
			default:
				replacement.append(".+");
				break;
			}
			replacement.append(')');
			matcher.appendReplacement(sb, replacement.toString());

			if (replaced != null) {
				// Keep replaced date-time pattern
				replaced.add(matcher.group());
			}
		}
		return matcher.appendTail(sb).toString();
	}
}
