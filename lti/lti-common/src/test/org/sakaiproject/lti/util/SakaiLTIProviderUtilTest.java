/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lti.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SakaiLTIProviderUtilTest {

	private static final String CONSUMER = "lmsng.school.edu";

	@Test
	public void ignoresReturnUrlUntilLaunchIsValidated() {
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://attacker.example/landing", false, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "/return", false, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "/return", true, null));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "/return", true, ""));
	}

	@Test
	public void allowsValidatedReturnUrlOnConsumerHost() {
		String https = "https://" + CONSUMER + "/lti/return";
		String http = "http://" + CONSUMER + ":8443/lti/return?launch=1#done";
		String upper = "HTTPS://LMSNG.SCHOOL.EDU/lti/return";
		assertEquals(https, SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(https, true, CONSUMER));
		assertEquals(http, SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(http, true, CONSUMER));
		assertEquals(upper, SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(upper, true, CONSUMER));
		assertEquals("//" + CONSUMER + "/return",
				SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("//" + CONSUMER + "/return", true, CONSUMER));
	}

	@Test
	public void allowsValidatedRelativePath() {
		String path = "/portal/site/mercury?panel=Main";
		assertEquals(path, SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(path, true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(path, false, CONSUMER));
	}

	@Test
	public void rejectsOtherHostsAndProtocolRelativeAttackers() {
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://attacker.example/landing", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"//attacker.example/pr", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + ".attacker.example/landing", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://attacker.example/" + CONSUMER, true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "@attacker.example/landing", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://attacker.example@" + CONSUMER + "/landing", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"javascript:alert(1)", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "%2eattacker.example/landing", true, CONSUMER));
	}

	@Test
	public void rejectsResponseSplittingAndSchemeRelativePaths() {
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://" + CONSUMER + "/x\r\nX-Injected: 1", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl(
				"https://attacker.example/x%0D%0AX-Injected: 1", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("/\\attacker.example", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("/%2f/attacker.example", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("/%5Cattacker.example", true, CONSUMER));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("/", true, CONSUMER));
	}

	@Test
	public void treatsConsumerKeyAsLiteralHost() {
		String key = "school.edu+extra";
		assertEquals("https://school.edu+extra/return",
				SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("https://school.edu+extra/return", true, key));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("https://school.edu/return", true, key));
		assertNull(SakaiLTIProviderUtil.allowedLaunchErrorReturnUrl("https://school.eduextra/return", true, key));
	}
}
