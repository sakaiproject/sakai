/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

@Getter
public class SiteStatsSamigoQuiz {

	private final String id;
	private final String title;
	private final Instant dueDate;
	private final Instant closeDate;
	private final boolean acceptLate;
	private final List<String> groupIds;
	private final boolean manualGradingRequired;
	private final List<SiteStatsSamigoDateOverride> dateOverrides;

	public SiteStatsSamigoQuiz(String id, String title, Instant dueDate) {
		this(id, title, dueDate, dueDate, Collections.emptyList(), false);
	}

	public SiteStatsSamigoQuiz(String id, String title, Instant dueDate, List<String> groupIds) {
		this(id, title, dueDate, dueDate, groupIds, false);
	}

	public SiteStatsSamigoQuiz(String id, String title, Instant dueDate, List<String> groupIds,
			boolean manualGradingRequired) {
		this(id, title, dueDate, dueDate, groupIds, manualGradingRequired);
	}

	public SiteStatsSamigoQuiz(String id, String title, Instant dueDate, Instant closeDate, List<String> groupIds,
			boolean manualGradingRequired) {
		this(id, title, dueDate, closeDate, groupIds, manualGradingRequired, false, Collections.emptyList());
	}

	public SiteStatsSamigoQuiz(String id, String title, Instant dueDate, Instant closeDate, List<String> groupIds,
			boolean manualGradingRequired, boolean acceptLate, List<SiteStatsSamigoDateOverride> dateOverrides) {
		this.id = id;
		this.title = StringEscapeUtils.unescapeHtml4(StringUtils.defaultString(title));
		this.dueDate = dueDate;
		this.closeDate = closeDate != null ? closeDate : dueDate;
		this.acceptLate = acceptLate;
		this.groupIds = groupIds == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<String>(groupIds));
		this.manualGradingRequired = manualGradingRequired;
		this.dateOverrides = dateOverrides == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<SiteStatsSamigoDateOverride>(dateOverrides));
	}

	SiteStatsSamigoWindow windowFor(String userId, Set<String> userGroupIds) {
		SiteStatsSamigoDateOverride override = overrideFor(userId, userGroupIds);
		if (override == null) {
			return new SiteStatsSamigoWindow(dueDate, closeDate);
		}
		return new SiteStatsSamigoWindow(override.getDueDate(),
				closeDate(override.getDueDate(), override.getRetractDate(), acceptLate));
	}

	static Instant closeDate(Instant due, Instant retract, boolean acceptLate) {
		if (acceptLate && retract != null) {
			return retract;
		}
		return due != null ? due : retract;
	}

	private SiteStatsSamigoDateOverride overrideFor(String userId, Set<String> userGroupIds) {
		if (dateOverrides.isEmpty()) {
			return null;
		}
		Set<String> groups = userGroupIds == null ? Collections.emptySet() : userGroupIds;
		List<SiteStatsSamigoDateOverride> ordered = new ArrayList<SiteStatsSamigoDateOverride>(dateOverrides);
		ordered.sort(Comparator
				.comparing((SiteStatsSamigoDateOverride override) -> StringUtils.isBlank(override.getUserId()) ? 0 : 1)
				.thenComparing(SiteStatsSamigoDateOverride::getGroupId, Comparator.nullsFirst(String::compareTo))
				.thenComparing(SiteStatsSamigoDateOverride::getUserId, Comparator.nullsFirst(String::compareTo)));
		SiteStatsSamigoDateOverride groupOverride = null;
		SiteStatsSamigoDateOverride userOverride = null;
		for (SiteStatsSamigoDateOverride override : ordered) {
			if (StringUtils.isNotBlank(userId) && userId.equals(override.getUserId())) {
				userOverride = override;
			} else if (StringUtils.isNotBlank(override.getGroupId()) && groups.contains(override.getGroupId())) {
				groupOverride = override;
			}
		}
		return userOverride != null ? userOverride : groupOverride;
	}
}
