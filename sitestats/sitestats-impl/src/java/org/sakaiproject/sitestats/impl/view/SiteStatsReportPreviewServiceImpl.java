/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;

public class SiteStatsReportPreviewServiceImpl implements SiteStatsReportPreviewService {

	private static final String PREVIEW_CACHE = SiteStatsReportPreviewServiceImpl.class.getName();
	private static final int MAX_PREVIEWS = 1_000;
	private static final int PREVIEW_TTL_SECONDS = 30 * 60;

	@Setter private MemoryService memoryService;

	private Cache<String, ReportDef> previews;

	public void init() {
		previews = memoryService.createCache(PREVIEW_CACHE,
				new SimpleConfiguration<String, ReportDef>(MAX_PREVIEWS, PREVIEW_TTL_SECONDS, 0));
	}

	@Override
	public String register(String siteId, String userId, ReportDef reportDef) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(userId) || reportDef == null) {
			throw new IllegalArgumentException("A site id, user id, and report definition are required");
		}
		String previewId = UUID.randomUUID().toString();
		previews.put(cacheKey(siteId, userId, previewId), new ReportDef(reportDef, siteId));
		return previewId;
	}

	@Override
	public ReportDef get(String siteId, String userId, String previewId) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(userId) || StringUtils.isBlank(previewId)) {
			return null;
		}
		ReportDef preview = previews.get(cacheKey(siteId, userId, previewId));
		return preview == null ? null : new ReportDef(preview, siteId);
	}

	private String cacheKey(String siteId, String userId, String previewId) {
		return encode(siteId) + ':' + encode(userId) + ':' + encode(previewId);
	}

	private String encode(String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

}
