/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.tsugi.lti.ContentItem;

/**
 * Regression coverage for sanitizing CI/DL description text on assignment return jobs.
 */
public class LTIAdminToolContentDescriptionTest {

	@Test
	public void applySanitizedDescriptionToJob_omitsTextWhenOnlyDisallowedHtml() {
		// AntiSamy/FormattedText leaves nothing usable for a script-only payload → null
		JSONObject job = new JSONObject();
		job.put("title", "Quiz");
		job.put(ContentItem.TEXT, "<script>alert(1)</script>");

		String applied = LTIAdminTool.applySanitizedDescriptionToJob(job, null);

		assertNull(applied);
		assertFalse(job.containsKey(ContentItem.TEXT));
		String serialized = job.toJSONString();
		assertFalse(serialized.contains("\"text\""));
		assertFalse(serialized.contains("script"));
		assertFalse(serialized.contains("alert"));
	}

	@Test
	public void applySanitizedDescriptionToJob_putsSanitizedTextWhenPresent() {
		JSONObject job = new JSONObject();
		job.put(ContentItem.TEXT, "<script>alert(1)</script><p>ok</p>");
		String sanitized = "<p>ok</p>";

		String applied = LTIAdminTool.applySanitizedDescriptionToJob(job, sanitized);

		assertEquals(sanitized, applied);
		assertEquals(sanitized, job.get(ContentItem.TEXT));
		assertTrue(job.toJSONString().contains("ok"));
		assertFalse(job.toJSONString().contains("script"));
	}
}
