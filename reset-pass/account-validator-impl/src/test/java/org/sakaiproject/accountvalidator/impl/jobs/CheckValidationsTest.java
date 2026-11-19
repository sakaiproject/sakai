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
package org.sakaiproject.accountvalidator.impl.jobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class CheckValidationsTest {

	@Test
	public void formatCreatedDateUsesLocalizedLongDate() {
		CheckValidations job = new CheckValidations();
		Date created = Date.from(LocalDate.of(2026, 3, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());

		String us = job.formatCreatedDate(created, Locale.US);
		assertNotNull(us);
		assertTrue(us.contains("2026"));
		assertTrue(us.toLowerCase(Locale.US).contains("march") || us.contains("Mar"));

		String fr = job.formatCreatedDate(created, Locale.FRANCE);
		assertNotNull(fr);
		assertTrue(fr.contains("2026"));
		assertFalse(fr.equals(us));
	}
}
