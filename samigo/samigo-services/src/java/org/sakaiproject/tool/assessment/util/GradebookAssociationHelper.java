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
		INVALID_OR_MISSING
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
	 *
	 * @return remapped CSV of ids that had a map entry; empty string if none could be remapped
	 */
	public static String remapItemIds(String gradebookItemIds, Map<String, String> transversalMap) {
		if (transversalMap == null || transversalMap.isEmpty()) {
			return "";
		}
		List<String> remapped = new ArrayList<>();
		for (String oldId : parseItemIds(gradebookItemIds)) {
			String mapped = transversalMap.get(GB_TRANSVERSAL_PREFIX + oldId);
			if (StringUtils.isNotBlank(mapped)) {
				String newId = mapped.startsWith(GB_TRANSVERSAL_PREFIX)
						? mapped.substring(GB_TRANSVERSAL_PREFIX.length())
						: mapped;
				if (StringUtils.isNotBlank(newId)) {
					remapped.add(newId);
				}
			}
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
			return SelectedGradebookValidation.INVALID_OR_MISSING;
		}
		for (String itemId : itemIds) {
			if (!isGradebookItemPresent(siteId, itemId, gradingService)) {
				return SelectedGradebookValidation.INVALID_OR_MISSING;
			}
		}
		return SelectedGradebookValidation.OK;
	}

	/**
	 * @return true if the id is numeric and a non-removed assignment exists for the site
	 */
	public static boolean isGradebookItemPresent(String siteId, String itemId, GradingService gradingService) {
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(itemId) || gradingService == null) {
			return false;
		}
		try {
			Long assignmentId = Long.valueOf(itemId);
			Assignment assignment = gradingService.getAssignment(siteId, siteId, assignmentId);
			return assignment != null && !Boolean.TRUE.equals(assignment.getExternallyMaintained());
		} catch (NumberFormatException | AssessmentNotFoundException e) {
			return false;
		} catch (Exception e) {
			log.warn("Unable to verify gradebook item {} in site {}: {}", itemId, siteId, e.toString());
			return false;
		}
	}

	/**
	 * Drop any ids from the CSV that are blank, non-numeric, or no longer present in Gradebook.
	 *
	 * @return remaining valid ids, or empty string
	 */
	public static String filterPresentItemIds(String gradebookItemIds, String siteId, GradingService gradingService) {
		List<String> valid = parseItemIds(gradebookItemIds).stream()
				.filter(id -> isGradebookItemPresent(siteId, id, gradingService))
				.collect(Collectors.toList());
		return joinItemIds(valid);
	}
}
