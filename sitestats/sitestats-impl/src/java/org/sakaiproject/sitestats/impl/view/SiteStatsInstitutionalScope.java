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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

@Slf4j
public class SiteStatsInstitutionalScope {

	@Setter private SiteService siteService;

	public ResolvedScope resolve(SiteStatsReportRequest request) {
		SiteStatsReportRequest safe = SiteStatsReportRequest.normalized(request);
		String type = StringUtils.trimToNull(safe.getSiteType());
		if ("all".equalsIgnoreCase(type)) {
			type = null;
		}
		String prefix = StringUtils.trimToNull(safe.getSitePrefix());
		boolean publishedOnly = safe.getPublishedOnly() == null || Boolean.TRUE.equals(safe.getPublishedOnly());

		SiteService.SelectionType selection = publishedOnly
				? new SiteService.SelectionType("institutionalPublished", false, true, true, true)
				: SiteService.SelectionType.NON_USER;

		List<String> ids = siteService.getSiteIds(selection, type, prefix, null, null, null);
		Set<String> siteIds = new HashSet<String>();
		if (ids != null) {
			siteIds.addAll(ids);
		}
		log.debug("Institutional scope resolved to {} sites (type={}, prefix={}, publishedOnly={})",
				siteIds.size(), type, prefix, publishedOnly);
		return new ResolvedScope(Collections.unmodifiableSet(siteIds));
	}

	@Getter
	public static final class ResolvedScope {
		private final Set<String> siteIds;

		public ResolvedScope(Set<String> siteIds) {
			this.siteIds = siteIds;
		}

		public int size() {
			return siteIds.size();
		}

		public boolean contains(String siteId) {
			return siteId != null && siteIds.contains(siteId);
		}
	}
}
