/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;

/**
 * Helpers for Samigo TO_SELECTED_GRADEBOOK associations (manual Gradebook item links).
 */
@Slf4j
public final class GradebookAssociationHelper {

	public static final String GB_TRANSVERSAL_PREFIX = "gb/";

	public enum SelectedGradebookValidation {
		OK,
		BLANK,
		INVALID_OR_MISSING,
		LOOKUP_FAILED
	}

	public enum GradebookItemPresence {
		PRESENT,
		ABSENT,
		UNKNOWN
	}

	private GradebookAssociationHelper() {
	}

	/**
	 * Parse a comma-separated list of gradebook item ids, trimming blanks.
	 */
	public static List<String> parseItemIds(String gradebookItemIds) {
		if (StringUtils.isBlank(gradebookItemIds)) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		for (String part : gradebookItemIds.split(",")) {
			String trimmed = StringUtils.trimToEmpty(part);
			if (StringUtils.isNotBlank(trimmed)) {
				result.add(trimmed);
			}
		}
		return result;
	}

	public static String joinItemIds(List<String> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) {
			return "";
		}
		return itemIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
	}

	/**
	 * Remove one assignment id from a CSV list of gradebook item ids.
	 *
	 * @return remaining ids joined, or empty string if none remain
	 */
	public static String removeItemId(String gradebookItemIds, String assignmentIdToRemove) {
		if (StringUtils.isBlank(assignmentIdToRemove)) {
			return StringUtils.defaultString(gradebookItemIds);
		}
		List<String> remaining = parseItemIds(gradebookItemIds).stream()
				.filter(id -> !assignmentIdToRemove.equals(id))
				.collect(Collectors.toList());
		return joinItemIds(remaining);
	}

	public static boolean containsItemId(String gradebookItemIds, String assignmentId) {
		if (StringUtils.isBlank(assignmentId)) {
			return false;
		}
		return parseItemIds(gradebookItemIds).contains(assignmentId);
	}

	/**
	 * Remap gradebook item ids using the site-import transversal map keys {@code gb/{oldId} → gb/{newId}}.
	 * When {@code siteId} and {@code gradingService} are provided, both mapped and unmapped ids
	 * are dropped when confirmed absent in the target site. Existing target-site ids and ids whose
	 * lookup fails temporarily are preserved.
	 *
	 * @return remapped CSV, or empty string if no ids remain
	 */
	public static String remapItemIds(String gradebookItemIds, Map<String, String> transversalMap) {
		return remapItemIds(gradebookItemIds, transversalMap, null, null);
	}

	public static String remapItemIds(String gradebookItemIds, Map<String, String> transversalMap,
			String siteId, GradingService gradingService) {
		if (StringUtils.isBlank(gradebookItemIds)) {
			return "";
		}
		List<String> remapped = new ArrayList<>();
		for (String oldId : parseItemIds(gradebookItemIds)) {
			String mapped = transversalMap == null ? null : transversalMap.get(GB_TRANSVERSAL_PREFIX + oldId);
			if (StringUtils.isBlank(mapped)) {
				mapped = oldId;
			}
			String newId = mapped.startsWith(GB_TRANSVERSAL_PREFIX)
					? mapped.substring(GB_TRANSVERSAL_PREFIX.length())
					: mapped;
			if (StringUtils.isBlank(newId)) {
				continue;
			}
			if (gradingService != null && StringUtils.isNotBlank(siteId)
					&& checkGradebookItemPresence(siteId, newId, gradingService) == GradebookItemPresence.ABSENT) {
				log.info("Dropping gradebook item {} (from {}) confirmed absent in site {}",
						newId, oldId, siteId);
				continue;
			}
			remapped.add(newId);
		}
		return joinItemIds(remapped);
	}

	/**
	 * Validate that TO_SELECTED_GRADEBOOK has at least one parseable item id that still exists in Gradebook.
	 */
	public static SelectedGradebookValidation validateSelectedGradebookItems(String toGradebook,
			String gradebookItemIds, String siteId, GradingService gradingService) {
		if (!EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(toGradebook)) {
			return SelectedGradebookValidation.OK;
		}
		List<String> itemIds = parseItemIds(gradebookItemIds);
		if (itemIds.isEmpty()) {
			return SelectedGradebookValidation.BLANK;
		}
		if (gradingService == null || StringUtils.isBlank(siteId)) {
			return SelectedGradebookValidation.LOOKUP_FAILED;
		}
		boolean lookupFailed = false;
		for (String itemId : itemIds) {
			GradebookItemPresence presence = checkGradebookItemPresence(siteId, itemId, gradingService);
			if (presence == GradebookItemPresence.ABSENT) {
				return SelectedGradebookValidation.INVALID_OR_MISSING;
			}
			if (presence == GradebookItemPresence.UNKNOWN) {
				lookupFailed = true;
			}
		}
		return lookupFailed ? SelectedGradebookValidation.LOOKUP_FAILED : SelectedGradebookValidation.OK;
	}

	/**
	 * @return true if the id is numeric and a non-removed assignment exists for the site
	 */
	public static boolean isGradebookItemPresent(String siteId, String itemId, GradingService gradingService) {
		return checkGradebookItemPresence(siteId, itemId, gradingService) == GradebookItemPresence.PRESENT;
	}

	/**
	 * Resolve the item's gradebook UID (including group gradebooks), verify it belongs to the site,
	 * and distinguish confirmed absence from temporary lookup failure.
	 */
	public static GradebookItemPresence checkGradebookItemPresence(String siteId, String itemId,
			GradingService gradingService) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(itemId) || gradingService == null) {
			return GradebookItemPresence.UNKNOWN;
		}
		Long assignmentId;
		try {
			assignmentId = Long.valueOf(itemId);
		} catch (NumberFormatException e) {
			return GradebookItemPresence.ABSENT;
		}

		String gradebookUid;
		try {
			gradebookUid = gradingService.getGradebookUidByAssignmentById(siteId, assignmentId);
		} catch (AssessmentNotFoundException e) {
			return GradebookItemPresence.ABSENT;
		} catch (Exception e) {
			log.warn("Unable to resolve gradebook uid for item {} in site {}: {}", itemId, siteId, e.toString());
			return GradebookItemPresence.UNKNOWN;
		}
		if (StringUtils.isBlank(gradebookUid)) {
			return GradebookItemPresence.ABSENT;
		}

		try {
			if (gradingService.isGradebookGroupEnabled(siteId)) {
				List<String> siteGradebookUids = gradingService.getGradebookGroupInstancesIds(siteId);
				if (siteGradebookUids == null || !siteGradebookUids.contains(gradebookUid)) {
					return GradebookItemPresence.ABSENT;
				}
			} else if (!siteId.equals(gradebookUid)) {
				return GradebookItemPresence.ABSENT;
			}

			Assignment assignment = gradingService.getAssignment(gradebookUid, siteId, assignmentId);
			if (assignment == null || Boolean.TRUE.equals(assignment.getExternallyMaintained())) {
				return GradebookItemPresence.ABSENT;
			}
			return GradebookItemPresence.PRESENT;
		} catch (AssessmentNotFoundException e) {
			return GradebookItemPresence.ABSENT;
		} catch (Exception e) {
			log.warn("Unable to verify gradebook item {} in site {}: {}", itemId, siteId, e.toString());
			return GradebookItemPresence.UNKNOWN;
		}
	}

	/**
	 * Drop ids that are blank, non-numeric, or confirmed absent in Gradebook.
	 * Ids that cannot be verified (temporary lookup failure) are preserved.
	 *
	 * @return remaining ids, or empty string
	 */
	public static String filterPresentItemIds(String gradebookItemIds, String siteId, GradingService gradingService) {
		List<String> valid = parseItemIds(gradebookItemIds).stream()
				.filter(id -> {
					GradebookItemPresence presence = checkGradebookItemPresence(siteId, id, gradingService);
					return presence == GradebookItemPresence.PRESENT || presence == GradebookItemPresence.UNKNOWN;
				})
				.collect(Collectors.toList());
		return joinItemIds(valid);
	}
}
