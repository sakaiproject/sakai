/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.reporting.util;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.text.MeasureFormat.FormatWidth;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;

/**
 * Utility for rendering SCORM ISO-8601 durations (e.g. {@code PT1M4.79S}) as localized strings.
 */
public final class ScormDurationFormatter
{
    private ScormDurationFormatter()
    {
    }

    public static String format(String iso8601, Locale locale)
    {
        return format(iso8601, locale, FormatWidth.NARROW, 2);
    }

    public static String format(String iso8601, Locale locale, FormatWidth width, int maxFractionDigits)
    {
        Duration duration = Duration.parse(iso8601);

        long days = duration.toDays();
        Duration remainder = duration.minusDays(days);
        long hours = remainder.toHours();
        remainder = remainder.minusHours(hours);
        long minutes = remainder.toMinutes();
        remainder = remainder.minusMinutes(minutes);

        double seconds = remainder.toNanos() / 1_000_000_000d;
        double scale = Math.pow(10, Math.max(0, maxFractionDigits));
        seconds = Math.round(seconds * scale) / scale;

        List<Measure> measures = new ArrayList<>();
        if (days > 0)
        {
            measures.add(new Measure(days, MeasureUnit.DAY));
        }
        if (hours > 0)
        {
            measures.add(new Measure(hours, MeasureUnit.HOUR));
        }
        if (minutes > 0 || measures.isEmpty())
        {
            measures.add(new Measure(minutes, MeasureUnit.MINUTE));
        }
        measures.add(new Measure(seconds, MeasureUnit.SECOND));

        MeasureFormat formatter = MeasureFormat.getInstance(locale != null ? locale : Locale.getDefault(), width);
        return formatter.formatMeasures(measures.toArray(new Measure[0]));
    }

    public static String formatOrNull(String iso8601, Locale locale)
    {
        if (iso8601 == null)
        {
            return null;
        }

        String trimmed = iso8601.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }

        try
        {
            return format(trimmed, locale);
        }
        catch (DateTimeParseException e)
        {
            return trimmed;
        }
    }
}
