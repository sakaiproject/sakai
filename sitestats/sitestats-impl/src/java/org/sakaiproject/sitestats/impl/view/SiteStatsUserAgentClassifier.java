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
package org.sakaiproject.sitestats.impl.view;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public final class SiteStatsUserAgentClassifier {

	public static final String DEVICE_DESKTOP = "Desktop";
	public static final String DEVICE_MOBILE = "Mobile";
	public static final String DEVICE_TABLET = "Tablet";
	public static final String DEVICE_OTHER = "Other";

	public static final String OS_WINDOWS = "Windows";
	public static final String OS_MACOS = "macOS";
	public static final String OS_IOS = "iOS";
	public static final String OS_ANDROID = "Android";
	public static final String OS_LINUX = "Linux";
	public static final String OS_OTHER = "Other";

	public static final String BROWSER_CHROME = "Chrome";
	public static final String BROWSER_SAFARI = "Safari";
	public static final String BROWSER_FIREFOX = "Firefox";
	public static final String BROWSER_EDGE = "Edge";
	public static final String BROWSER_OTHER = "Other";

	private SiteStatsUserAgentClassifier() {
	}

	public static Classification classify(String userAgent) {
		String ua = StringUtils.defaultString(userAgent);
		if (StringUtils.isBlank(ua)) {
			return new Classification(DEVICE_OTHER, OS_OTHER, BROWSER_OTHER);
		}
		String lower = ua.toLowerCase(Locale.ROOT);
		return new Classification(deviceClass(lower), operatingSystem(lower), browserFamily(lower));
	}

	private static String deviceClass(String lower) {
		if (lower.contains("ipad") || lower.contains("tablet") || (lower.contains("android") && !lower.contains("mobile"))) {
			return DEVICE_TABLET;
		}
		if (lower.contains("mobi") || lower.contains("iphone") || lower.contains("ipod")
				|| lower.contains("android") || lower.contains("windows phone")) {
			return DEVICE_MOBILE;
		}
		if (lower.contains("windows") || lower.contains("macintosh") || lower.contains("linux")
				|| lower.contains("x11") || lower.contains("cros")) {
			return DEVICE_DESKTOP;
		}
		return DEVICE_OTHER;
	}

	private static String operatingSystem(String lower) {
		if (lower.contains("android")) {
			return OS_ANDROID;
		}
		if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ipod") || lower.contains("ios")) {
			return OS_IOS;
		}
		if (lower.contains("windows")) {
			return OS_WINDOWS;
		}
		if (lower.contains("mac os") || lower.contains("macintosh")) {
			return OS_MACOS;
		}
		if (lower.contains("linux") || lower.contains("cros") || lower.contains("x11")) {
			return OS_LINUX;
		}
		return OS_OTHER;
	}

	private static String browserFamily(String lower) {
		if (lower.contains("edg/") || lower.contains("edge/")) {
			return BROWSER_EDGE;
		}
		if (lower.contains("firefox/") || lower.contains("fxios/")) {
			return BROWSER_FIREFOX;
		}
		if (lower.contains("chrome/") || lower.contains("crios/")) {
			return BROWSER_CHROME;
		}
		if (lower.contains("safari/") && !lower.contains("chrome/") && !lower.contains("crios/")) {
			return BROWSER_SAFARI;
		}
		return BROWSER_OTHER;
	}

	public static final class Classification {
		private final String device;
		private final String os;
		private final String browser;

		public Classification(String device, String os, String browser) {
			this.device = device;
			this.os = os;
			this.browser = browser;
		}

		public String getDevice() {
			return device;
		}

		public String getOs() {
			return os;
		}

		public String getBrowser() {
			return browser;
		}
	}
}
