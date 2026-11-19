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
	public void classifiesEdgeOnAndroidBeforeChrome() {
		assertClassification(
				"Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 EdgA/120.0.0.0",
				SiteStatsUserAgentClassifier.DEVICE_MOBILE, SiteStatsUserAgentClassifier.OS_ANDROID,
				SiteStatsUserAgentClassifier.BROWSER_EDGE);
	}

	@Test
	public void classifiesEdgeOnIphoneBeforeSafari() {
		assertClassification(
				"Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) EdgiOS/120.0.0.0 Version/17.0 Mobile/15E148 Safari/605.1.15",
				SiteStatsUserAgentClassifier.DEVICE_MOBILE, SiteStatsUserAgentClassifier.OS_IOS,
				SiteStatsUserAgentClassifier.BROWSER_EDGE);
	}

	@Test
	public void classifiesEdgeOnDesktopBeforeChrome() {
		assertClassification(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
				SiteStatsUserAgentClassifier.DEVICE_DESKTOP, SiteStatsUserAgentClassifier.OS_WINDOWS,
				SiteStatsUserAgentClassifier.BROWSER_EDGE);
	}

	@Test
	public void classifiesReducedChromeOnAndroidPhone() {
		assertClassification(
				"Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
				SiteStatsUserAgentClassifier.DEVICE_MOBILE, SiteStatsUserAgentClassifier.OS_ANDROID,
				SiteStatsUserAgentClassifier.BROWSER_CHROME);
	}

	@Test
	public void classifiesReducedChromeOnAndroidTablet() {
		assertClassification(
				"Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
				SiteStatsUserAgentClassifier.DEVICE_TABLET, SiteStatsUserAgentClassifier.OS_ANDROID,
				SiteStatsUserAgentClassifier.BROWSER_CHROME);
	}

	@Test
	public void classifiesChromeOsSeparatelyFromLinux() {
		assertClassification(
				"Mozilla/5.0 (X11; CrOS x86_64 14541.0.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
				SiteStatsUserAgentClassifier.DEVICE_DESKTOP, SiteStatsUserAgentClassifier.OS_CHROMEOS,
				SiteStatsUserAgentClassifier.BROWSER_CHROME);
	}

	@Test
	public void desktopModeAndroidTabletUsesReportedLinuxPlatform() {
		// Chrome desktop mode hides Android and tablet tokens; the UA cannot identify the hardware.
		assertClassification(
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
				SiteStatsUserAgentClassifier.DEVICE_DESKTOP, SiteStatsUserAgentClassifier.OS_LINUX,
				SiteStatsUserAgentClassifier.BROWSER_CHROME);
	}

	@Test
	public void desktopModeIpadUsesReportedMacPlatform() {
		// Safari desktop mode shares its platform tokens with macOS; do not guess iPad hardware.
		assertClassification(
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15",
				SiteStatsUserAgentClassifier.DEVICE_DESKTOP, SiteStatsUserAgentClassifier.OS_MACOS,
				SiteStatsUserAgentClassifier.BROWSER_SAFARI);
	}

	@Test
	public void classifiesBlankAsOther() {
		SiteStatsUserAgentClassifier.Classification c = SiteStatsUserAgentClassifier.classify(null);
		assertEquals(SiteStatsUserAgentClassifier.DEVICE_OTHER, c.getDevice());
		assertEquals(SiteStatsUserAgentClassifier.OS_OTHER, c.getOs());
		assertEquals(SiteStatsUserAgentClassifier.BROWSER_OTHER, c.getBrowser());
	}

	private void assertClassification(String userAgent, String device, String os, String browser) {
		SiteStatsUserAgentClassifier.Classification classification = SiteStatsUserAgentClassifier.classify(userAgent);
		assertEquals(device, classification.getDevice());
		assertEquals(os, classification.getOs());
		assertEquals(browser, classification.getBrowser());
	}
}
