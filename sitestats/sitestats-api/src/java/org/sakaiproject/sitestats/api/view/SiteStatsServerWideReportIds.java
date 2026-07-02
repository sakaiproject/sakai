/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.sakaiproject.sitestats.api.StatsManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SiteStatsServerWideReportIds {

	public static final String MONTHLY_LOGIN = StatsManager.MONTHLY_LOGIN_REPORT;
	public static final String WEEKLY_LOGIN = StatsManager.WEEKLY_LOGIN_REPORT;
	public static final String DAILY_LOGIN = StatsManager.DAILY_LOGIN_REPORT;
	public static final String REGULAR_USERS = StatsManager.REGULAR_USERS_REPORT;
	public static final String HOURLY_USAGE = StatsManager.HOURLY_USAGE_REPORT;
	public static final String TOP_ACTIVITIES = StatsManager.TOP_ACTIVITIES_REPORT;
	public static final String TOOL = StatsManager.TOOL_REPORT;

	public static final List<String> ORDERED_IDS = Collections.unmodifiableList(Arrays.asList(
			MONTHLY_LOGIN,
			WEEKLY_LOGIN,
			DAILY_LOGIN,
			REGULAR_USERS,
			HOURLY_USAGE,
			TOP_ACTIVITIES,
			TOOL));
	private static final Map<String, String> KEY_SUFFIXES;

	static {
		LinkedHashMap<String, String> keySuffixes = new LinkedHashMap<String, String>();
		keySuffixes.put(MONTHLY_LOGIN, "monthly_login_report");
		keySuffixes.put(WEEKLY_LOGIN, "weekly_login_report");
		keySuffixes.put(DAILY_LOGIN, "daily_login_report");
		keySuffixes.put(REGULAR_USERS, "regular_users_report");
		keySuffixes.put(HOURLY_USAGE, "hourly_usage_report");
		keySuffixes.put(TOP_ACTIVITIES, "top_activities_report");
		keySuffixes.put(TOOL, "tool_report");
		KEY_SUFFIXES = Collections.unmodifiableMap(keySuffixes);
	}

	public static boolean isSupported(String reportType) {
		return ORDERED_IDS.contains(reportType);
	}

	public static String keySuffix(String reportType) {
		String keySuffix = KEY_SUFFIXES.get(reportType);
		if (keySuffix == null) {
			throw new IllegalArgumentException("Unknown server-wide SiteStats report: " + reportType);
		}
		return keySuffix;
	}
}
