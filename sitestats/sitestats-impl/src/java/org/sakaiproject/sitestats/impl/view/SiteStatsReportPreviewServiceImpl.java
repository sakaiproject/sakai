/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;

public class SiteStatsReportPreviewServiceImpl implements SiteStatsReportPreviewService {

	private static final long PREVIEW_TTL_MILLIS = 30L * 60L * 1000L;

	private final Map<PreviewKey, PreviewReport> previews = new ConcurrentHashMap<PreviewKey, PreviewReport>();

	@Override
	public String register(String siteId, String userId, ReportDef reportDef) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(userId) || reportDef == null) {
			throw new IllegalArgumentException("A site id, user id, and report definition are required");
		}
		cleanupExpired();
		String previewId = UUID.randomUUID().toString();
		PreviewKey key = new PreviewKey(siteId, userId, previewId);
		previews.put(key, new PreviewReport(new ReportDef(reportDef, siteId), System.currentTimeMillis() + PREVIEW_TTL_MILLIS));
		return previewId;
	}

	@Override
	public ReportDef get(String siteId, String userId, String previewId) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(userId) || StringUtils.isBlank(previewId)) {
			return null;
		}
		cleanupExpired();
		PreviewKey key = new PreviewKey(siteId, userId, previewId);
		PreviewReport preview = previews.get(key);
		if (preview == null) {
			return null;
		}
		if (preview.isExpired()) {
			previews.remove(key);
			return null;
		}
		return new ReportDef(preview.getReportDef(), siteId);
	}

	private void cleanupExpired() {
		Iterator<Map.Entry<PreviewKey, PreviewReport>> iterator = previews.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<PreviewKey, PreviewReport> entry = iterator.next();
			if (entry.getValue().isExpired()) {
				iterator.remove();
			}
		}
	}

	private static class PreviewKey {
		private final String siteId;
		private final String ownerId;
		private final String previewId;

		private PreviewKey(String siteId, String ownerId, String previewId) {
			this.siteId = siteId;
			this.ownerId = ownerId;
			this.previewId = previewId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof PreviewKey)) {
				return false;
			}
			PreviewKey other = (PreviewKey) obj;
			return StringUtils.equals(siteId, other.siteId)
					&& StringUtils.equals(ownerId, other.ownerId)
					&& StringUtils.equals(previewId, other.previewId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(siteId, ownerId, previewId);
		}
	}

	private static class PreviewReport {
		private final ReportDef reportDef;
		private final long expiresAt;

		private PreviewReport(ReportDef reportDef, long expiresAt) {
			this.reportDef = reportDef;
			this.expiresAt = expiresAt;
		}

		private ReportDef getReportDef() {
			return reportDef;
		}

		private boolean isExpired() {
			return System.currentTimeMillis() > expiresAt;
		}
	}
}
