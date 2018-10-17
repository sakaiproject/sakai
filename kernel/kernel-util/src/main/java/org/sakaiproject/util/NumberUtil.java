/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2018 Sakai Foundation
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilities based on double and integers such as validation formats.
 */
public class NumberUtil {
	
	/**
	 * @param origin number that is needed to validate
	 * @param locale specific geographic location to validate format on origin param  
	 * @return true if number format is valid for that locale
	 */
	public static boolean isValidLocaleDouble(final String origin, Locale locale) {
		final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
		final DecimalFormatSymbols fs = df.getDecimalFormatSymbols();
		final String doublePattern =
			"\\d+\\" + fs.getGroupingSeparator() 
			+ "\\d\\d\\d\\" + fs.getDecimalSeparator()
			+ "\\d+|\\d+\\" + fs.getDecimalSeparator()
			+ "\\d+|\\d+\\" + fs.getGroupingSeparator() 
			+ "\\d\\d\\d|\\d+";
		return origin.matches(doublePattern);
	}

	/**
	 * @param origin origin number that is needed to validate on the default user's locale
	 * @return true if number format is valid for user's locale
	 */
	public static boolean isValidLocaleDouble(final String origin) {
		return isValidLocaleDouble(origin, new ResourceLoader().getLocale());
	}

}
