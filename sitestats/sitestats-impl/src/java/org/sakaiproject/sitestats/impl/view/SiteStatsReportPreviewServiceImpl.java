/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;

public class SiteStatsReportPreviewServiceImpl implements SiteStatsReportPreviewService {

	private static final long PREVIEW_TTL_MILLIS = 30L * 60L * 1000L;

	private final Map<String, PreviewReport> previews = new ConcurrentHashMap<String, PreviewReport>();

	@Override
	public String register(String siteId, ReportDef reportDef) {
		if (StringUtils.isBlank(siteId) || reportDef == null) {
			throw new IllegalArgumentException("A site id and report definition are required");
		}
		cleanupExpired();
		String previewId = UUID.randomUUID().toString();
		previews.put(previewId, new PreviewReport(siteId, reportDef, System.currentTimeMillis() + PREVIEW_TTL_MILLIS));
		return previewId;
	}

	@Override
	public ReportDef get(String siteId, String previewId) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(previewId)) {
			return null;
		}
		cleanupExpired();
		PreviewReport preview = previews.get(previewId);
		if (preview == null) {
			return null;
		}
		if (preview.isExpired()) {
			previews.remove(previewId);
			return null;
		}
		if (!StringUtils.equals(siteId, preview.getSiteId())) {
			return null;
		}
		return preview.getReportDef();
	}

	private void cleanupExpired() {
		Iterator<Map.Entry<String, PreviewReport>> iterator = previews.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, PreviewReport> entry = iterator.next();
			if (entry.getValue().isExpired()) {
				iterator.remove();
			}
		}
	}

	private static class PreviewReport {
		private final String siteId;
		private final ReportDef reportDef;
		private final long expiresAt;

		private PreviewReport(String siteId, ReportDef reportDef, long expiresAt) {
			this.siteId = siteId;
			this.reportDef = reportDef;
			this.expiresAt = expiresAt;
		}

		private String getSiteId() {
			return siteId;
		}

		private ReportDef getReportDef() {
			return reportDef;
		}

		private boolean isExpired() {
			return System.currentTimeMillis() > expiresAt;
		}
	}
}
