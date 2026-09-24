/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** The spreadsheet convention uses semicolons when commas separate decimal places. */
public final class CsvSeparator {

    private CsvSeparator() {
    }

    public static char forLocale(Locale locale) {
        return forDecimalSeparator(String.valueOf(DecimalFormatSymbols.getInstance(locale).getDecimalSeparator()));
    }

    public static char forDecimalSeparator(String decimalSeparator) {
        return ",".equals(decimalSeparator) ? ';' : ',';
    }
}
