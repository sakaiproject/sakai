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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;

public class SiteStatsReportPreviewServiceImpl implements SiteStatsReportPreviewService {

	private static final String PREVIEW_CACHE = SiteStatsReportPreviewServiceImpl.class.getName();

	@Setter private CacheManager cacheManager;

	private Cache previews;

	public void init() {
		previews = cacheManager.getCache(PREVIEW_CACHE);
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
		ReportDef preview = previews.get(cacheKey(siteId, userId, previewId), ReportDef.class);
		return preview == null ? null : new ReportDef(preview, siteId);
	}

	private String cacheKey(String siteId, String userId, String previewId) {
		return encode(siteId) + ':' + encode(userId) + ':' + encode(previewId);
	}

	private String encode(String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

}
