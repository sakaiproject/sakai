/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sakaiproject.sitestats.impl.view.SiteStatsUserAgentClassifier;

public class SiteStatsUserAgentClassifierTest {

	@Test
	public void classifiesChromeOnWindowsAsDesktop() {
		SiteStatsUserAgentClassifier.Classification c = SiteStatsUserAgentClassifier.classify(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
		assertEquals(SiteStatsUserAgentClassifier.DEVICE_DESKTOP, c.getDevice());
		assertEquals(SiteStatsUserAgentClassifier.OS_WINDOWS, c.getOs());
		assertEquals(SiteStatsUserAgentClassifier.BROWSER_CHROME, c.getBrowser());
	}

	@Test
	public void classifiesIphoneSafariAsMobile() {
		SiteStatsUserAgentClassifier.Classification c = SiteStatsUserAgentClassifier.classify(
				"Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1");
		assertEquals(SiteStatsUserAgentClassifier.DEVICE_MOBILE, c.getDevice());
		assertEquals(SiteStatsUserAgentClassifier.OS_IOS, c.getOs());
		assertEquals(SiteStatsUserAgentClassifier.BROWSER_SAFARI, c.getBrowser());
	}

	@Test
	public void classifiesBlankAsOther() {
		SiteStatsUserAgentClassifier.Classification c = SiteStatsUserAgentClassifier.classify(null);
		assertEquals(SiteStatsUserAgentClassifier.DEVICE_OTHER, c.getDevice());
		assertEquals(SiteStatsUserAgentClassifier.OS_OTHER, c.getOs());
		assertEquals(SiteStatsUserAgentClassifier.BROWSER_OTHER, c.getBrowser());
	}
}
