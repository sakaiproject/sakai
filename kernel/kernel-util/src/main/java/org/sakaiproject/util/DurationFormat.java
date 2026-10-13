/**********************************************************************************
 *
 * Copyright (c) 2026 The Sakai Foundation
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

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Locale-aware formatting of minute-based durations using {@link java.time.Duration}.
 * Matches the common hour/minute word style previously provided by Joda-Time's PeriodFormat.
 */
public final class DurationFormat {

	private static final String BUNDLE = "org.sakaiproject.util.duration";

	private DurationFormat() {
	}

	/**
	 * Formats a positive duration expressed in whole minutes.
	 *
	 * @param totalMinutes minutes to format (negative values are treated as zero)
	 * @param locale locale for unit words and joining; {@code null} uses {@link Locale#getDefault()}
	 * @return localized duration text such as {@code 1 hour and 30 minutes}
	 */
	public static String formatMinutes(long totalMinutes, Locale locale) {
		Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
		long minutesTotal = Math.max(0L, totalMinutes);
		Duration duration = Duration.ofMinutes(minutesTotal);
		long hours = duration.toHours();
		long minutes = duration.toMinutesPart();

		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, effectiveLocale);
		List<String> parts = new ArrayList<>(2);
		if (hours > 0) {
			parts.add(MessageFormat.format(bundle.getString(hours == 1L ? "hour" : "hours"), hours));
		}
		if (minutes > 0 || parts.isEmpty()) {
			parts.add(MessageFormat.format(bundle.getString(minutes == 1L ? "minute" : "minutes"), minutes));
		}
		if (parts.size() == 1) {
			return parts.get(0);
		}
		return MessageFormat.format(bundle.getString("joiner"), parts.get(0), parts.get(1));
	}
}
