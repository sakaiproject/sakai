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

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class DurationFormatTest {

	@Test
	public void formatsMinutesOnly() {
		assertEquals("1 minute", DurationFormat.formatMinutes(1, Locale.US));
		assertEquals("30 minutes", DurationFormat.formatMinutes(30, Locale.US));
	}

	@Test
	public void formatsHoursOnly() {
		assertEquals("1 hour", DurationFormat.formatMinutes(60, Locale.US));
		assertEquals("2 hours", DurationFormat.formatMinutes(120, Locale.US));
		assertEquals("24 hours", DurationFormat.formatMinutes(1440, Locale.US));
	}

	@Test
	public void formatsHoursAndMinutes() {
		assertEquals("1 hour and 30 minutes", DurationFormat.formatMinutes(90, Locale.US));
		assertEquals("25 hours and 1 minute", DurationFormat.formatMinutes(1501, Locale.US));
	}

	@Test
	public void treatsNegativeAsZero() {
		assertEquals("0 minutes", DurationFormat.formatMinutes(-5, Locale.US));
	}

	@Test
	public void usesFrenchLocale() {
		assertEquals("1 heure", DurationFormat.formatMinutes(60, Locale.FRANCE));
		assertEquals("1 heure et 30 minutes", DurationFormat.formatMinutes(90, Locale.FRANCE));
	}

	@Test
	public void usesJapaneseLocaleWithoutJoinerSpaces() {
		assertEquals("1時間", DurationFormat.formatMinutes(60, Locale.JAPAN));
		assertEquals("1時間30分", DurationFormat.formatMinutes(90, Locale.JAPAN));
	}

	@Test
	public void nullLocaleFallsBackToDefault() {
		String formatted = DurationFormat.formatMinutes(60, null);
		assertEquals(DurationFormat.formatMinutes(60, Locale.getDefault()), formatted);
	}

	@Test
	public void unsupportedLocaleUsesBaseBundleNotJvmDefault() {
		Locale previousDefault = Locale.getDefault();
		try {
			Locale.setDefault(Locale.FRANCE);
			// No Italian duration bundle: must use base English, not JVM default French.
			assertEquals("1 hour", DurationFormat.formatMinutes(60, Locale.ITALIAN));
			assertEquals("1 hour and 30 minutes", DurationFormat.formatMinutes(90, Locale.ITALIAN));
		} finally {
			Locale.setDefault(previousDefault);
		}
	}
}
