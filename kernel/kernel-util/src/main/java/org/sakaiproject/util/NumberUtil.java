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

import lombok.Setter;

/**
 * Utilities based on double and integers such as validation formats.
 */
public class NumberUtil {

    @Setter
    private static ResourceLoader resourceLoader = new ResourceLoader();

    /**
     * @param origin origin number that is needed to validate on the default user's locale
     * @return true if number format is valid for user's locale
     */
    public static boolean isValidLocaleDouble(final String origin) {
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(resourceLoader.getLocale());
        final DecimalFormatSymbols fs = df.getDecimalFormatSymbols();
        final String doublePattern =
                new StringBuilder()
                        .append("\\d{1,3}(\\")
                        .append(fs.getGroupingSeparator())
                        .append("\\d{3})+")
                        .append(fs.getDecimalSeparator())
                        .append("\\d+|\\d*\\")
                        .append(fs.getDecimalSeparator())
                        .append("\\d+|\\d{1,3}(\\")
                        .append(fs.getGroupingSeparator())
                        .append("\\d{3})+|\\d+")
                        .toString();
        return origin.matches(doublePattern);
    }
}
