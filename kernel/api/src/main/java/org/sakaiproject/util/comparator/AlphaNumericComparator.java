/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.util.comparator;

import org.apache.commons.lang3.StringUtils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class AlphaNumericComparator implements Comparator<String> {
	private final Comparator<String> stringComparator;
	private final Comparator<String> fallbackComparator;

	public AlphaNumericComparator() {
		this(String.CASE_INSENSITIVE_ORDER, Comparator.naturalOrder());
	}

	public AlphaNumericComparator(Locale locale) {
		Collator collator = Collator.getInstance(Objects.requireNonNull(locale));
		collator.setStrength(Collator.SECONDARY);
		Comparator<String> localeComparator = collator::compare;
		this.stringComparator = buildComparator(localeComparator);
		this.fallbackComparator = localeComparator;
	}

	private AlphaNumericComparator(Comparator<String> textComparator, Comparator<String> fallbackComparator) {
		this.stringComparator = buildComparator(textComparator);
		this.fallbackComparator = fallbackComparator;
	}

	@Override
	public int compare(String o1, String o2) {
		try {
			return stringComparator.compare(o1, o2);
		} catch (NumberFormatException nfe) {
			return fallbackComparator.compare(String.valueOf(o1), String.valueOf(o2));
		}
	}

	private static Comparator<String> buildComparator(Comparator<String> textComparator) {
		return Comparator
				.comparing(AlphaNumericComparator::getTextPart, textComparator)
				.thenComparingLong(AlphaNumericComparator::getNumberPart);
	}

	private static String getTextPart(String value) {
		return String.valueOf(value).replaceAll("\\d", "");
	}

	private static long getNumberPart(String value) {
		String numberPart = String.valueOf(value).replaceAll("\\D", "");
		return StringUtils.isNumeric(numberPart) ? Long.parseLong(numberPart) : 0;
	}
}
