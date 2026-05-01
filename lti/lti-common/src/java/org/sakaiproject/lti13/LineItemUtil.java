/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2012 The Sakai Foundation, 2013- The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti13;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import org.json.simple.JSONObject;

import org.tsugi.lti.ContentItem;
import org.tsugi.lti13.DeepLinkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI13_PATH;
import static org.sakaiproject.lti.util.SakaiLTIUtil.getOurServerUrl;
import org.sakaiproject.lti.util.SakaiLTIUtil;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.foorm.Foorm;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.component.cover.ServerConfigurationService;

import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.AssignmentHasIllegalPointsException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.lti13.util.SakaiLineItem;

import org.tsugi.lti.LTIUtil;
import org.tsugi.lti13.LTI13Util;

/**
 * Some Sakai Utility code for IMS LTI This is mostly code to support the
 * Sakai conventions for making and launching LTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LineItemUtil {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static final String GB_EXTERNAL_APP_NAME = "IMS-AGS";
	public static final String ASSIGNMENTS_EXTERNAL_APP_NAME = "Assignments"; // Avoid circular references
	/** Tool id stored in {@code GB_GRADABLE_OBJECT_T.EXTERNAL_APP_NAME} for gradebook columns owned by Assignments. */
	public static final String ASSIGNMENT_GRADES_TOOL_ID = "sakai.assignment.grades";
	public static final String ASSIGNMENT_REFERENCE_PREFIX = "/assignment/a";

	public final static String ID_SEPARATOR = "|";
	public final static String ID_SEPARATOR_REGEX = "\\|";

	// TODO: SAK-44137 - In Sakai-22 or later switch this default
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM = "lti.advantage.construct.lineitem";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE = "true";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_FALSE = "false";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_DEFAULT = LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE;

	/**
	 * Result of classifying a gradebook column for LTI Advantage line items: whether it is a primary
	 * LTI line item row, a legacy assignment-ref row discoverable via the site assignment→LTI map,
	 * tool ownership, the resolved {@code tool_id|content_id} key, and (when loaded) the Sakai Assignment.
	 */
	public static final class LtiLineItemRowResolution {
		private final boolean primaryLtiLineItemRow;
		private final boolean fallbackAssignmentRefRow;
		private final boolean ownedByTool;
		private final String toolContentKey;
		private final org.sakaiproject.assignment.api.model.Assignment sakaiAssignment;

		private LtiLineItemRowResolution(boolean primaryLtiLineItemRow, boolean fallbackAssignmentRefRow,
				boolean ownedByTool, String toolContentKey,
				org.sakaiproject.assignment.api.model.Assignment sakaiAssignment) {
			this.primaryLtiLineItemRow = primaryLtiLineItemRow;
			this.fallbackAssignmentRefRow = fallbackAssignmentRefRow;
			this.ownedByTool = ownedByTool;
			this.toolContentKey = toolContentKey;
			this.sakaiAssignment = sakaiAssignment;
		}

		private static LtiLineItemRowResolution none() {
			return new LtiLineItemRowResolution(false, false, false, null, null);
		}

		public boolean isPrimaryLtiLineItemRow() {
			return primaryLtiLineItemRow;
		}

		public boolean isFallbackAssignmentRefRow() {
			return fallbackAssignmentRefRow;
		}

		public boolean isOwnedByTool() {
			return ownedByTool;
		}

		public String getToolContentKey() {
			return toolContentKey;
		}

		/**
		 * Present when {@link #isPrimaryLtiLineItemRow()} is true and the row is backed by Assignments
		 * (external-tool submission). Callers may use this to avoid a second assignment load.
		 */
		public org.sakaiproject.assignment.api.model.Assignment getSakaiAssignment() {
			return sakaiAssignment;
		}

		/**
		 * Whether this row should appear in AGS line item listings for the requested tool (matches prior
		 * combinations of {@code isGradebookColumnLTI} / assignment-ref fallback / tool id match).
		 */
		public boolean isIncludedInToolLineItemList() {
			return ownedByTool && (primaryLtiLineItemRow || fallbackAssignmentRefRow);
		}
	}

	public static String URLEncode(String inp) {
		if ( inp == null ) return null;
		try {
			return java.net.URLEncoder.encode(inp.trim(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}
	// tool_id|content_id|resourceId|tag|
	/**
	 * Construct the GB_EXTERNAL_ID for LTI AGS entries
	 * @param content - The content item cannot be null
	 * @param lineItem - The lineItem to insert
	 * @return The properly formatted external id
	 */
	public static String constructExternalId(Map<String, Object> content, SakaiLineItem lineItem)
	{
		Long tool_id = LTIUtil.toLongKey(content.get(LTIService.LTI_TOOL_ID));
		return constructExternalId(tool_id, content, lineItem);
	}

	/**
	 * Construct the GB_EXTERNAL_ID for LTI AGS entries
	 * @param tool_id - The key of the owning tool
	 * @param content - The content item associated with this - likely null.
	 * @param lineItem - The lineItem to insert
	 * @return The properly formatted external id
	 *
	 * The format of the external_id is:
	 *
	 *     tool_id|content_id|resourceId|tag|
	 *
	 * The tool_id is requires and functions as an AUTHZ field to keep tools from
	 * seeing / accessing each other's line items.  The content_id will likely by empty
	 * because these are owned by the tool rather than a content item.  The resourceId
	 * and tag are chosen by the tool and used as filters when the tool retrieves its
	 * LineItems.  Since we use vertical bars as separator - they are not allowed in the
	 * tag or resourceId.
	 *
	 * Someday if we had a longer field I would store this as JSON.
	 */
	// TODO: Remember to dream that someday this will be JSON :)
	public static String constructExternalId(Long tool_id, Map<String, Object> content, SakaiLineItem lineItem)
	{
		Long content_id = (content == null) ? null : LTIUtil.toLongNull(content.get(LTIService.LTI_ID));
		log.debug("content_id={}", content_id);
		return constructExternalIdImpl(tool_id, content_id, lineItem);
	}

	private static String constructExternalIdImpl(Long tool_id, Long content_id, SakaiLineItem lineItem)
	{
		String retval = tool_id.toString();
		retval += ID_SEPARATOR + ((content_id == null) ? "0" : content_id.toString()) + ID_SEPARATOR;
		if ( lineItem != null ) {
			if ( lineItem.resourceId == null ) {
				retval += ID_SEPARATOR;
			} else {
				retval += URLEncode(lineItem.resourceId.replace("|", "")) + ID_SEPARATOR;
			}
			if ( lineItem.tag == null ) {
				retval += ID_SEPARATOR;
			} else {
				retval += URLEncode(lineItem.tag.replace("|", "")) + ID_SEPARATOR;
			}
		}
		log.debug("retval={}", retval);
		return retval;
	}

	private static boolean hasValidExternalIdFormat(String externalId) {
		if (StringUtils.isBlank(externalId)) {
			return false;
		}
		String[] parts = externalId.split(ID_SEPARATOR_REGEX, -1);
		if (parts == null || parts.length < 2) {
			return false;
		}
		return StringUtils.isNumeric(parts[0]) && StringUtils.isNumeric(parts[1]);
	}

	/**
	 * Stable key for LTI line items: {@code tool_id|content_id}. Stored in {@code EXTERNAL_ID}
	 * and not modified on line item updates (only {@code LINEITEM_METADATA} changes).
	 */
	private static String constructToolContentExternalId(Long toolId, Long contentId) {
		return toolId + ID_SEPARATOR + ((contentId == null) ? "0" : contentId.toString());
	}

	private static String constructLineItemMetadata(SakaiLineItem lineItem) {
		Map<String, String> metadata = new LinkedHashMap<>();
		metadata.put("resourceId", StringUtils.trimToNull(lineItem.resourceId));
		metadata.put("tag", StringUtils.trimToNull(lineItem.tag));
		try {
			return OBJECT_MAPPER.writeValueAsString(metadata);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new RuntimeException("Could not serialize line item metadata", e);
		}
	}

	private static boolean hasValidExternalDataFormat(String externalData) {
		if (StringUtils.isBlank(externalData)) {
			return false;
		}
		String[] parts = externalData.split(ID_SEPARATOR_REGEX, -1);
		if (parts == null || parts.length < 2) {
			return false;
		}
		return StringUtils.isNumeric(parts[0]) && StringUtils.isNumeric(parts[1]);
	}

	/**
	 * Ownership / filtering key: {@code tool_id|content_id}. Prefer {@code EXTERNAL_ID}; fall back to
	 * legacy {@code EXTERNAL_DATA} from interim deployments.
	 */
	private static String getPreferredToolContentKey(Assignment gradebookColumn) {
		if (gradebookColumn == null) {
			return null;
		}

		String externalId = StringUtils.trimToNull(gradebookColumn.getExternalId());
		if (hasValidExternalIdFormat(externalId)) {
			String[] parts = externalId.split(ID_SEPARATOR_REGEX, -1);
			return parts[0] + ID_SEPARATOR + parts[1];
		}

		String externalData = StringUtils.trimToNull(gradebookColumn.getExternalData());
		if (hasValidExternalDataFormat(externalData)) {
			return externalData;
		}

		return null;
	}

	private static Map<String, String> parseLineItemMetadata(String lineItemMetadata, Assignment gradebookColumn) {
		if (StringUtils.isBlank(lineItemMetadata)) {
			return null;
		}
		try {
			Map<?, ?> raw = OBJECT_MAPPER.readValue(lineItemMetadata, Map.class);
			Map<String, String> parsed = new HashMap<>();
			Object resourceId = raw.get("resourceId");
			Object tag = raw.get("tag");
			if (resourceId instanceof String) {
				parsed.put("resourceId", StringUtils.trimToNull((String) resourceId));
			}
			if (tag instanceof String) {
				parsed.put("tag", StringUtils.trimToNull((String) tag));
			}
			return parsed;
		} catch (Exception e) {
			log.warn(
					"Failed to parse LINEITEM_METADATA as JSON; gradebookAssignmentId={} name={} externalId={} reference={} gradebookUid={}; rawLineItemMetadata={}",
					gradebookColumn != null ? gradebookColumn.getId() : null,
					gradebookColumn != null ? gradebookColumn.getName() : null,
					gradebookColumn != null ? gradebookColumn.getExternalId() : null,
					gradebookColumn != null ? gradebookColumn.getReference() : null,
					gradebookColumn != null ? gradebookColumn.getGradebookUid() : null,
					lineItemMetadata,
					e);
			return null;
		}
	}

	private static Map<String, String> getPreferredLineItemMetadata(Assignment gradebookColumn) {
		if (gradebookColumn == null) {
			return null;
		}

		Map<String, String> parsedMetadata = parseLineItemMetadata(gradebookColumn.getLineItemMetadata(), gradebookColumn);
		if (parsedMetadata != null) {
			return parsedMetadata;
		}

		String legacyExternalId = StringUtils.trimToNull(gradebookColumn.getExternalId());
		if (!hasValidExternalIdFormat(legacyExternalId)) {
			return null;
		}
		String[] parts = legacyExternalId.split(ID_SEPARATOR_REGEX, -1);
		Map<String, String> legacy = new HashMap<>();
		legacy.put("resourceId", (parts.length > 2) ? StringUtils.trimToNull(parts[2]) : null);
		legacy.put("tag", (parts.length > 3) ? StringUtils.trimToNull(parts[3]) : null);
		return legacy;
	}

	private static boolean shouldMigrateLegacyOnUpdate(Assignment gradebookColumn) {
		if (gradebookColumn == null) {
			return false;
		}
		if (StringUtils.isNotBlank(gradebookColumn.getLineItemMetadata())) {
			return false;
		}
		// Interim rows: tool|content only in EXTERNAL_DATA
		if (hasValidExternalDataFormat(StringUtils.trimToNull(gradebookColumn.getExternalData()))) {
			return true;
		}
		String externalId = StringUtils.trimToNull(gradebookColumn.getExternalId());
		if (!hasValidExternalIdFormat(externalId)) {
			return false;
		}
		String[] parts = externalId.split(ID_SEPARATOR_REGEX, -1);
		// Legacy full line: tool|content|resourceId|tag|...
		return parts.length > 2;
	}

	static boolean shouldMigrateLegacyExternalId(Assignment gradebookColumn) {
		return shouldMigrateLegacyOnUpdate(gradebookColumn);
	}

	private static Long deriveContentIdFromGradebookExternalId(Assignment gradebookColumn) {
		String toolContentKey = getPreferredToolContentKey(gradebookColumn);
		if (toolContentKey == null) {
			return null;
		}
		String[] parts = StringUtils.split(toolContentKey, ID_SEPARATOR_REGEX);
		return (parts == null || parts.length < 2) ? null : Long.valueOf(parts[1]);
	}

	public static Assignment createLineItem(Site site, Long tool_id, Map<String, Object> content, SakaiLineItem lineItem) {
		String context_id = site.getId();
		return createLineItem(context_id, tool_id, content, lineItem);
	}

	public static Assignment createLineItem(String context_id, Long tool_id, Map<String, Object> content, SakaiLineItem lineItem) {
		// Look up the assignment so we can find the max points
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

		if (lineItem.scoreMaximum == null) {
			lineItem.scoreMaximum = 100.0;
		}

		if ( lineItem.label == null ) {
			throw new RuntimeException("lineitem.label is required");
		}

		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}

		Long content_id = (content == null) ? null : LTIUtil.toLongNull(content.get(LTIService.LTI_ID));
		String stableExternalId = constructToolContentExternalId(tool_id, content_id);
		String lineitem_metadata = constructLineItemMetadata(lineItem);

		Assignment gradebookColumn = null;
		Long gradebookColumnId = null;

		// Check for duplicate labels
		List<Assignment> assignments = getColumnsForContextDAO(context_id);
		if ( assignments == null ) {
			throw new RuntimeException("Could not list columns for "+context_id+" tool="+tool_id+" (is the gradebook in the site?)");
		}

		boolean createNew = true;
		for (Iterator i = assignments.iterator(); i.hasNext();) {
			Assignment gbColumn = (Assignment) i.next();

			if (lineItem.label.equals(gbColumn.getName())) {
				gradebookColumn = gbColumn;
				gradebookColumnId = gbColumn.getId();
				createNew = false;
				break;
			}
		}

		if ( gradebookColumn == null ) gradebookColumn = new Assignment();

		pushAdvisor();
		String failure = null;
		try {
			if (lineItem.scoreMaximum != null ) {
				gradebookColumn.setPoints(Double.valueOf(lineItem.scoreMaximum));
			} else {
				gradebookColumn.setPoints(100.0D);
			}
			// We are using the actual grade and points possible in the GB
			gradebookColumn.setExternallyMaintained(false);
			gradebookColumn.setExternalId(stableExternalId);
			gradebookColumn.setLineItemMetadata(lineitem_metadata);
			gradebookColumn.setExternalAppName(GB_EXTERNAL_APP_NAME);
			gradebookColumn.setName(lineItem.label);
			Boolean releaseToStudent = lineItem.releaseToStudent == null ? Boolean.TRUE : lineItem.releaseToStudent; // Default to true
			Boolean includeInComputation = lineItem.includeInComputation == null ? Boolean.TRUE : lineItem.includeInComputation; // Default true
			gradebookColumn.setReleased(releaseToStudent); // default true
			gradebookColumn.setCounted(includeInComputation); // default true
			gradebookColumn.setUngraded(false);
			Date endDateTime = LTIUtil.parseIMS8601(lineItem.endDateTime);
			if ( endDateTime != null ) gradebookColumn.setDueDate(endDateTime);

			if ( createNew ) {
				gradebookColumnId = gradingService.addAssignment(context_id, context_id, gradebookColumn);
				gradebookColumn.setId(gradebookColumnId);
				log.info("Added assignment: {} with Id: {}", lineItem.label, gradebookColumnId);
			} else {
				gradingService.updateAssignment(context_id, context_id, gradebookColumnId, gradebookColumn);
				log.info("Updated assignment: {} with Id: {}", lineItem.label, gradebookColumnId);
			}
		} catch (ConflictingAssignmentNameException e) {
			failure = "ConflictingAssignmentNameException while adding assignment " + e.toString();
			gradebookColumn = null; // Just to make sure
		} catch (Exception e) {
			failure = "Exception (may be because GradeBook has not yet been added to the Site) "+ e.toString();
			gradebookColumn = null; // Just to make double sure
		}  finally {
			popAdvisor();
		}

		if (gradebookColumn == null || gradebookColumn.getId() == null) {
			if ( failure == null ) failure = "gradebookColumn or Id is null.";
			gradebookColumn = null;
		}

		if ( failure != null ) {
			throw new RuntimeException(failure);
		}

		return gradebookColumn;
	}

	/**
	 * Applies LTI line item fields to a Sakai Assignments activity (points use the assignment scale factor).
	 * {@code startDateTime} maps to {@link org.sakaiproject.assignment.api.model.Assignment#getOpenDate() openDate}.
	 * {@code endDateTime} maps to both {@link org.sakaiproject.assignment.api.model.Assignment#getDueDate() due date}
	 * and {@link org.sakaiproject.assignment.api.model.Assignment#getCloseDate() close date} (accept until).
	 */
	private static void applyLineItemToSakaiAssignment(SakaiLineItem lineItem,
			org.sakaiproject.assignment.api.model.Assignment asn,
			org.sakaiproject.assignment.api.AssignmentService assignmentService) {
		if (lineItem == null || asn == null || assignmentService == null) {
			return;
		}
		if (lineItem.label != null) {
			asn.setTitle(lineItem.label.trim());
		}
		if (lineItem.scoreMaximum != null
				&& asn.getTypeOfGrade() == org.sakaiproject.assignment.api.model.Assignment.GradeType.SCORE_GRADE_TYPE) {
			int scaleFactor = asn.getScaleFactor() != null ? asn.getScaleFactor() : assignmentService.getScaleFactor();
			int maxGradePoint = (int) Math.round(lineItem.scoreMaximum * scaleFactor);
			asn.setMaxGradePoint(maxGradePoint);
		}
		if (lineItem.startDateTime != null) {
			Date startDate = LTIUtil.parseIMS8601(lineItem.startDateTime);
			if (startDate != null) {
				asn.setOpenDate(startDate.toInstant());
			}
		}
		if (lineItem.endDateTime != null) {
			Date endDate = LTIUtil.parseIMS8601(lineItem.endDateTime);
			if (endDate != null) {
				Instant endInstant = endDate.toInstant();
				asn.setDueDate(endInstant);
			}
		}
	}

	/**
	 * Copies title and due date from a Sakai assignment onto the linked gradebook column.
	 * Score maximum is applied separately so the gradebook row and assignment both receive explicit updates
	 * from the line item (see {@link #updateLineItem}).
	 */
	private static void syncGradebookColumnTitleAndDueFromSakaiAssignment(Assignment gbColumn,
			org.sakaiproject.assignment.api.model.Assignment asn) {
		if (gbColumn == null || asn == null) {
			return;
		}
		gbColumn.setName(asn.getTitle());
		if (asn.getDueDate() != null) {
			gbColumn.setDueDate(Date.from(asn.getDueDate()));
		}
	}

	/**
	 * Persists title, points, and due date for externally maintained gradebook columns.
	 * {@link GradingService#updateAssignment} does not apply those fields when
	 * {@link Assignment#getExternallyMaintained()} is true; {@link GradingService#updateExternalAssessment}
	 * must be used (same pattern as {@code AssignmentToolUtils#integrateGradebook} for {@code update}).
	 */
	private static void syncExternalAssessmentDefinition(GradingService gradingService, String gradebookUid,
			Assignment gradebookColumn) {
		if (gradingService == null || gradebookUid == null || gradebookColumn == null) {
			return;
		}
		if (!Boolean.TRUE.equals(gradebookColumn.getExternallyMaintained())) {
			return;
		}
		String externalId = StringUtils.trimToNull(gradebookColumn.getExternalId());
		if (externalId == null) {
			return;
		}
		String title = StringUtils.trimToNull(gradebookColumn.getName());
		if (title == null) {
			return;
		}
		try {
			gradingService.updateExternalAssessment(gradebookUid, externalId, null, gradebookColumn.getExternalData(),
					title, null, gradebookColumn.getPoints(), gradebookColumn.getDueDate(),
					gradebookColumn.getUngraded());
		} catch (AssessmentNotFoundException | ConflictingAssignmentNameException
				| AssignmentHasIllegalPointsException e) {
			throw new RuntimeException(e);
		}
	}

	public static Assignment updateLineItem(Site site, Long tool_id, Long column_id, SakaiLineItem lineItem)
			throws PermissionException {
		log.debug("updateLineItem site={} tool_id={} column_id={} lineItem={}", site.getId(), tool_id, column_id, lineItem);
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");
		org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager
				.get(org.sakaiproject.assignment.api.AssignmentService.class);
		String context_id = site.getId();

		if ( column_id == null ) {
			throw new RuntimeException("column_id is required");
		}

		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}

		Assignment gradebookColumn = getColumnByKeyDAO(context_id, tool_id, column_id);
		if ( gradebookColumn == null ) return null;

		/*
			{
			  "scoreMaximum": 0,
			  "label": "string",
			  "tag": "string",
			  "resourceId": "string"
			}
		*/
		log.debug("gradebookColumn={}", gradebookColumn);

		boolean shouldMigrateLegacy = shouldMigrateLegacyOnUpdate(gradebookColumn);
		if (shouldMigrateLegacy) {
			String interimData = StringUtils.trimToNull(gradebookColumn.getExternalData());
			if (hasValidExternalDataFormat(interimData)) {
				gradebookColumn.setExternalId(interimData);
				gradebookColumn.setExternalData(null);
			} else {
				String legacyExternalId = StringUtils.trimToNull(gradebookColumn.getExternalId());
				String[] parts = legacyExternalId.split(ID_SEPARATOR_REGEX, -1);
				gradebookColumn.setExternalId(parts[0] + ID_SEPARATOR + parts[1]);
				Map<String, String> legacyMetadata = new HashMap<>();
				legacyMetadata.put("resourceId", (parts.length > 2) ? StringUtils.trimToNull(parts[2]) : null);
				legacyMetadata.put("tag", (parts.length > 3) ? StringUtils.trimToNull(parts[3]) : null);
				try {
					gradebookColumn.setLineItemMetadata(OBJECT_MAPPER.writeValueAsString(legacyMetadata));
				} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
					throw new RuntimeException("Could not serialize legacy line item metadata", e);
				}
			}
		}

		String lineitem_metadata = constructLineItemMetadata(lineItem);

		log.debug("gb item id={}; gb item title={}; lineitem_metadata={}; external_id={}", gradebookColumn.getId(),
			  gradebookColumn.getName(), lineitem_metadata, gradebookColumn.getExternalId());

		// Do not modify EXTERNAL_ID after create; only LINEITEM_METADATA (and normal GB fields) change here.
		gradebookColumn.setLineItemMetadata(lineitem_metadata);

		Boolean releaseToStudent = lineItem.releaseToStudent == null ? Boolean.TRUE : lineItem.releaseToStudent; // Default to true
		Boolean includeInComputation = lineItem.includeInComputation == null ? Boolean.TRUE : lineItem.includeInComputation; // Default true
		gradebookColumn.setReleased(releaseToStudent); // default true
		gradebookColumn.setCounted(includeInComputation); // default true
		gradebookColumn.setUngraded(false);

		Map<String, String> assignmentRefToToolKey = getExternalIdsForToolAssignments(context_id);
		LtiLineItemRowResolution resolution = resolveLtiLineItemRow(context_id, gradebookColumn, tool_id, assignmentRefToToolKey);
		org.sakaiproject.assignment.api.model.Assignment sakaiAsn = resolution.getSakaiAssignment();

		pushAdvisor();
		try {
			if (sakaiAsn != null && resolution.isPrimaryLtiLineItemRow() && assignmentService != null) {
				try {
					applyLineItemToSakaiAssignment(lineItem, sakaiAsn, assignmentService);
					assignmentService.updateAssignment(sakaiAsn);
					syncGradebookColumnTitleAndDueFromSakaiAssignment(gradebookColumn, sakaiAsn);
					// scoreMaximum is stored on the assignment (scaled maxGradePoint) and on the gradebook row (points)
					if (lineItem.scoreMaximum != null) {
						gradebookColumn.setPoints(Double.valueOf(lineItem.scoreMaximum));
					}
				} catch (PermissionException e) {
					log.warn("Could not update linked Sakai assignment from LTI line item: {}", e.toString());
					throw e;
				}
			} else {
				if ( lineItem.scoreMaximum != null ) {
					gradebookColumn.setPoints(Double.valueOf(lineItem.scoreMaximum));
				}
				if ( lineItem.label != null ) {
					gradebookColumn.setName(lineItem.label);
				}
				Date dueDate = LTIUtil.parseIMS8601(lineItem.endDateTime);
				if ( dueDate != null ) {
					gradebookColumn.setDueDate(dueDate);
				}
			}
			syncExternalAssessmentDefinition(gradingService, context_id, gradebookColumn);
			gradingService.updateAssignment(context_id, context_id, column_id, gradebookColumn);
		} finally {
			popAdvisor();
		}

		return gradebookColumn;
	}

	/**
	 * Return a map of external_id values for LTI assignments in a site
	 *
	 * @param context_id
	 *
	 * The format of the external_id is:
	 *
	 *     tool_id|content_id|resourceLink|tag|
	 *
	 * This should be called with the appropriate security advisor in place
	 *
	 * @return A map of assignment references to their external_id values
	 */
	public static Map<String, String> getExternalIdsForToolAssignments(String context_id) {
		Map<String, String> retval = new HashMap<>();
		org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
		LTIService ltiService = ComponentManager.get(LTIService.class);

		try {
			Collection<org.sakaiproject.assignment.api.model.Assignment> assignments = assignmentService.getAssignmentsForContext(context_id);
			for (org.sakaiproject.assignment.api.model.Assignment a : assignments) {
				String assignmentReference = org.sakaiproject.assignment.api.AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
				Integer assignmentContentId = a.getContentId();
				if ( assignmentContentId == null ) continue;
				Map<String, Object> content = ltiService.getContent(assignmentContentId.longValue(), context_id);
				if ( content == null ) continue;
				retval.put(assignmentReference, constructExternalId(content, null));
			}
		} catch (Exception e) {
			log.error("Unexpected Throwable", e.toString());
			log.debug("Stacktrace", e);
		}
		return retval;
	}

	private static boolean toolIdMatchesKey(Long toolId, String toolContentKey) {
		if (toolId == null || StringUtils.isBlank(toolContentKey)) {
			return false;
		}
		String[] parts = toolContentKey.split(ID_SEPARATOR_REGEX);
		return parts.length >= 1 && toolId.toString().equals(parts[0]);
	}

	private static org.sakaiproject.assignment.api.model.Assignment loadSakaiAssignment(String assignmentRef) {
		if (!isAssignmentColumn(assignmentRef)) {
			return null;
		}
		org.sakaiproject.assignment.api.AssignmentService assignmentService = null;
		try {
			assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
		} catch (Throwable t) {
			log.debug("AssignmentService not available: {}", t.toString());
			return null;
		}
		if (assignmentService == null) {
			return null;
		}
		try {
			String assignmentId = org.sakaiproject.assignment.api.AssignmentReferenceReckoner.reckoner()
					.reference(assignmentRef).reckon().getId();
			if (StringUtils.isBlank(assignmentId)) {
				assignmentId = assignmentRef;
			}
			return assignmentService.getAssignment(assignmentId);
		} catch (Exception e) {
			log.debug("Could not load assignment: {}", assignmentRef, e);
			return null;
		}
	}

	private static String constructToolKeyFromAssignmentContent(String contextId,
			org.sakaiproject.assignment.api.model.Assignment asn) {
		if (asn == null || asn.getContentId() == null || StringUtils.isBlank(contextId)) {
			return null;
		}
		try {
			LTIService ltiService = ComponentManager.get(LTIService.class);
			if (ltiService == null) {
				return null;
			}
			Map<String, Object> content = ltiService.getContent(asn.getContentId().longValue(), contextId);
			if (content == null) {
				return null;
			}
			return constructExternalId(content, null);
		} catch (Throwable t) {
			log.debug("Could not build tool key from assignment content: {}", t.toString());
			return null;
		}
	}

	/**
	 * Single classification pass for a gradebook column: primary LTI line item vs legacy assignment-ref
	 * row, tool ownership, resolved {@code tool_id|content_id} key, and (for external-tool assignments)
	 * the loaded {@link org.sakaiproject.assignment.api.model.Assignment} so callers do not fetch twice.
	 *
	 * @param contextId site / gradebook uid
	 * @param gbColumn gradebook column
	 * @param toolId requesting LTI tool id; if null, ownership is not evaluated (always passes)
	 * @param assignmentRefToToolKey map from assignment reference to {@code tool_id|content_id}; if null,
	 *        a map is loaded when needed (prefer passing a pre-built map when iterating all columns)
	 */
	public static LtiLineItemRowResolution resolveLtiLineItemRow(String contextId, Assignment gbColumn, Long toolId,
			Map<String, String> assignmentRefToToolKey) {
		if (gbColumn == null) {
			return LtiLineItemRowResolution.none();
		}

		String appName = gbColumn.getExternalAppName();
		String assignmentExternalId = StringUtils.trimToNull(gbColumn.getExternalId());
		Map<String, String> refMap = assignmentRefToToolKey;

		if (GB_EXTERNAL_APP_NAME.equals(appName)) {
			String key = getPreferredToolContentKey(gbColumn);
			if (StringUtils.isBlank(key) && isAssignmentColumn(assignmentExternalId)) {
				if (refMap == null) {
					refMap = getExternalIdsForToolAssignments(contextId);
				}
				key = refMap != null ? refMap.get(assignmentExternalId) : null;
			}
			boolean owned = toolId == null || toolIdMatchesKey(toolId, key);
			return new LtiLineItemRowResolution(true, false, owned, key, null);
		}

		boolean assignmentApp = ASSIGNMENTS_EXTERNAL_APP_NAME.equals(appName)
				|| ASSIGNMENT_GRADES_TOOL_ID.equals(appName);

		if (assignmentApp) {
			String assignmentRef = assignmentExternalId;
			if (assignmentRef == null) {
				assignmentRef = StringUtils.trimToNull(gbColumn.getReference());
			}
			if (!isAssignmentColumn(assignmentRef)) {
				return LtiLineItemRowResolution.none();
			}
			org.sakaiproject.assignment.api.model.Assignment asn = loadSakaiAssignment(assignmentRef);
			if (asn == null) {
				return LtiLineItemRowResolution.none();
			}
			if (contextId != null && asn.getContext() != null && !contextId.equals(asn.getContext())) {
				return LtiLineItemRowResolution.none();
			}
			if (org.sakaiproject.assignment.api.model.Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION
					.equals(asn.getTypeOfSubmission())) {
				String key = null;
				if (refMap != null) {
					key = refMap.get(assignmentRef);
				}
				if (StringUtils.isBlank(key)) {
					key = constructToolKeyFromAssignmentContent(contextId, asn);
				}
				boolean owned = toolId == null || toolIdMatchesKey(toolId, key);
				return new LtiLineItemRowResolution(true, false, owned, key, asn);
			}
			if (refMap == null) {
				refMap = getExternalIdsForToolAssignments(contextId);
			}
			String key = refMap != null ? refMap.get(assignmentRef) : null;
			if (StringUtils.isBlank(key)) {
				return LtiLineItemRowResolution.none();
			}
			boolean owned = toolId == null || toolIdMatchesKey(toolId, key);
			return new LtiLineItemRowResolution(false, true, owned, key, null);
		}

		if (isAssignmentColumn(assignmentExternalId)) {
			if (refMap == null) {
				refMap = getExternalIdsForToolAssignments(contextId);
			}
			String key = refMap != null ? refMap.get(assignmentExternalId) : null;
			if (StringUtils.isBlank(key)) {
				return LtiLineItemRowResolution.none();
			}
			boolean owned = toolId == null || toolIdMatchesKey(toolId, key);
			return new LtiLineItemRowResolution(false, true, owned, key, null);
		}

		return LtiLineItemRowResolution.none();
	}

	/**
	 * Return a list of assignments associated with this tool in a site
	 * @param context_id - The site id
	 * @param tool_id - The tool id
	 * @return A list of Assignment objects (perhaps empty) or null on failure
	 */
	protected static List<Assignment> getColumnsForToolDAO(String context_id, Long tool_id) {
		List<Assignment> retval = new ArrayList<>();
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

		pushAdvisor();
		try {
			Map<String, String> assignmentRefToToolKey = getExternalIdsForToolAssignments(context_id);
			List<Assignment> gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				LtiLineItemRowResolution r = resolveLtiLineItemRow(context_id, gbColumn, tool_id, assignmentRefToToolKey);
				if (r.isIncludedInToolLineItemList()) {
					retval.add(gbColumn);
				}
			}
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.toString());
			log.debug("Stacktrace:", e);
			retval = null;
		} finally {
			popAdvisor();
		}
		return retval;
	}

	/**
	 * Return a list of assignments in a site
	 * @param context_id - The site id
	 * @return A list of Assignment objects (perhaps empty) or null on failure
	 */
	protected static List<Assignment> getColumnsForContextDAO(String context_id) {
		List retval = new ArrayList();
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

		pushAdvisor();
		try {
			List<Assignment> gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			return gradebookColumns;
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.toString());
			log.debug("Stacktrace:", e);
			retval = null;
		} finally {
			popAdvisor();
		}
		return retval;
	}

	/**
	 * Load a particular assignment by its internal Sakai GB key
	 * @param context_id
	 * @param tool_id
	 * @param column_id
	 * @return
	 */
	public static Assignment getColumnByKeyDAO(String context_id, Long tool_id, Long column_id)
	{
		List<Assignment> assignments = getColumnsForToolDAO(context_id, tool_id);
		for (Iterator i = assignments.iterator(); i.hasNext();) {
			Assignment gbColumn = (Assignment) i.next();
			if (column_id.equals(gbColumn.getId())) return gbColumn;
		}
		return null;
	}

	/**
	 * Load a particular assignment by its internal Sakai GB key
	 * @param context_id
	 * @param tool_id
	 * @param column_id
	 * @return
	 */
	protected static Assignment getColumnByLabelDAO(String context_id, Long tool_id, String column_label)
	{
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");
		Assignment retval = null;

		pushAdvisor();
		try {
			Map<String, String> assignmentRefToToolKey = getExternalIdsForToolAssignments(context_id);
			List gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				LtiLineItemRowResolution r = resolveLtiLineItemRow(context_id, gbColumn, tool_id, assignmentRefToToolKey);
				if (!r.isIncludedInToolLineItemList()) {
					continue;
				}
				if (column_label.equals(gbColumn.getName())) {
					retval = gbColumn;
					break;
				}
			}
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.toString());
			log.debug("Stacktrace:", e);
			retval = null;
		} finally {
			popAdvisor();
		}
		return retval;
	}

	/**
	 * Load a particular assignment by its internal Sakai GB key
	 * @param context_id
	 * @param tool_id
	 * @param column_id
	 * @return
	 */
	protected static boolean deleteAssignmentByKeyDAO(String context_id, Long tool_id, Long column_id)
	{
		// Make sure it belongs to us
		Assignment a = getColumnByKeyDAO(context_id, tool_id, column_id);
		if ( a == null ) return false;
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

		pushAdvisor();
		try {
			// Provides us no return value
			gradingService.removeAssignment(column_id);
		} finally {
			popAdvisor();
		}

		return true;
	}

	/**
	 * Determine if a grade book column is relevant to LTI Advantage line items / AGS.
	 * <p>
	 * IMS-AGS columns are always treated as LTI. Columns owned by the Assignments tool
	 * ({@link #ASSIGNMENT_GRADES_TOOL_ID} or legacy {@link #ASSIGNMENTS_EXTERNAL_APP_NAME})
	 * are LTI only when the linked Sakai assignment uses external (LTI) submission.
	 * </p>
	 *
	 * @param contextId site id (gradebook uid); used to verify the assignment belongs to the site. May be null.
	 * @param gradebookColumn gradebook assignment row
	 * @see #resolveLtiLineItemRow(String, Assignment, Long, java.util.Map) for tool ownership and loaded assignment
	 */
	public static boolean isGradebookColumnLTI(String contextId, Assignment gradebookColumn) {
		return resolveLtiLineItemRow(contextId, gradebookColumn, null, null).isPrimaryLtiLineItemRow();
	}

	/**
	 * @deprecated use {@link #isGradebookColumnLTI(String, Assignment)}
	 */
	@Deprecated
	public static boolean isGradebookColumnLTI(Assignment gradebookColumn) {
		return isGradebookColumnLTI(null, gradebookColumn);
	}

	public static boolean isAssignmentColumn(String external_id) {
		return external_id != null && external_id.startsWith(ASSIGNMENT_REFERENCE_PREFIX);
	}

	/**
	 * Get the line items from the gradebook for a tool
	 * @param site The site we are looking at
	 * @param tool_id The tool we are scanning for
	 * @param filter Optional line item with resourceId or tag used to filter returned results
	 * @return A List of LineItems - an empty list is returned if none exist
	 */
	public static List<SakaiLineItem> getLineItemsForTool(String signed_placement, Site site, Long tool_id, SakaiLineItem filter) {

		log.debug("signed_placement={}; site id={}; tool_id={}", signed_placement, site.getId(), tool_id);

		String context_id = site.getId();
		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

		List<SakaiLineItem> retval = new ArrayList<>();

		pushAdvisor();
		try {
			Map<String, String> assignmentRefToToolKey = getExternalIdsForToolAssignments(context_id);
			List gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				LtiLineItemRowResolution r = resolveLtiLineItemRow(context_id, gbColumn, tool_id, assignmentRefToToolKey);
				if (!r.isIncludedInToolLineItemList()) {
					continue;
				}
				String external_id = r.getToolContentKey();
				log.debug("gbColumn: {} resolved key={}", gbColumn.getName(), external_id);
				if ( external_id == null || external_id.length() < 1 ) continue;

				log.debug("gb column id={}; title={}; external_id={}", gbColumn.getId(), gbColumn.getName(), external_id);

				String[] parts = external_id.split(ID_SEPARATOR_REGEX);

				org.sakaiproject.assignment.api.model.Assignment sakaiAsn = r.getSakaiAssignment();
				SakaiLineItem item = getLineItem(signed_placement, gbColumn, sakaiAsn);
				if ( parts.length > 1 && ! StringUtils.equals("0", parts[1]) ) {
					item.resourceLinkId = "content:" + parts[1];
				}

				if ( filter != null ) {
					if ( filter.resourceLinkId != null && ! filter.resourceLinkId.equals(item.resourceLinkId)) continue;
					if ( filter.resourceId != null && ! filter.resourceId.equals(item.resourceId)) continue;
					if ( filter.tag != null && ! filter.tag.equals(item.tag)) continue;
				}
				retval.add(item);
			}
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.getMessage());
			log.debug("Stacktrace:", e);
		} finally {
			popAdvisor();
		}

		return retval;
	}

	public static SakaiLineItem getLineItem(String signed_placement, Assignment gbColumn) {
		return getLineItem(signed_placement, gbColumn, null);
	}

	/**
	 * Build a line item for AGS. When {@code sakaiAssignment} is non-null (external-tool assignment row),
	 * label, score cap, dates follow the Assignments object; resourceId/tag still come from the
	 * gradebook column metadata. {@code startDateTime} reflects {@code openDate}; {@code endDateTime}
	 * reflects due date (or accept-until if due is unset).
	 */
	public static SakaiLineItem getLineItem(String signed_placement, Assignment gbColumn,
			org.sakaiproject.assignment.api.model.Assignment sakaiAssignment) {
		SakaiLineItem li = new SakaiLineItem();
		if (sakaiAssignment != null) {
			li.label = sakaiAssignment.getTitle();
			if (sakaiAssignment.getTypeOfGrade() == org.sakaiproject.assignment.api.model.Assignment.GradeType.SCORE_GRADE_TYPE
					&& sakaiAssignment.getMaxGradePoint() != null) {
				int scaleFactor = sakaiAssignment.getScaleFactor() != null ? sakaiAssignment.getScaleFactor() : 100;
				li.scoreMaximum = sakaiAssignment.getMaxGradePoint() / (double) scaleFactor;
			} else if (gbColumn.getPoints() != null) {
				li.scoreMaximum = gbColumn.getPoints();
			}
			Instant openInstant = sakaiAssignment.getOpenDate();
			if (openInstant != null) {
				li.startDateTime = org.tsugi.lti.LTIUtil.getISO8601(Date.from(openInstant));
			}
			Instant endInstant = sakaiAssignment.getDueDate();
			if (endInstant == null) {
				endInstant = sakaiAssignment.getCloseDate();
			}
			if (endInstant != null) {
				li.endDateTime = org.tsugi.lti.LTIUtil.getISO8601(Date.from(endInstant));
			} else if (gbColumn.getDueDate() != null) {
				li.endDateTime = org.tsugi.lti.LTIUtil.getISO8601(gbColumn.getDueDate());
			}
		} else {
			li.label = gbColumn.getName();
			li.scoreMaximum = gbColumn.getPoints();
			Date dueDate = gbColumn.getDueDate();
			if (dueDate != null) {
				li.endDateTime = org.tsugi.lti.LTIUtil.getISO8601(dueDate);
			}
		}

		// EXTERNAL_ID holds tool_id|content_id (stable); LINEITEM_METADATA JSON holds resourceId/tag.
		String toolContentKey = getPreferredToolContentKey(gbColumn);
		if (toolContentKey != null && toolContentKey.length() > 0) {
			String[] parts = toolContentKey.split(ID_SEPARATOR_REGEX);
			li.resourceLinkId = (parts.length > 1 && parts[1].trim().length() > 1) ? parts[1].trim() : null;
		}

		Map<String, String> metadata = getPreferredLineItemMetadata(gbColumn);
		if (metadata != null) {
			li.resourceId = metadata.get("resourceId");
			li.tag = metadata.get("tag");
		}

		if (signed_placement != null) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + gbColumn.getId();
		}

		return li;
	}

	/**
	 * Builds the {@link SakaiLineItem} for one gradebook column the same way as {@link #getLineItemsForTool}
	 * (assignment-sourced label/points/due when applicable, resourceId/tag from gradebook metadata, resourceLinkId
	 * from the resolved tool key).
	 *
	 * @return the line item for this tool, or {@code null} if the column is not included for this tool (same
	 *         {@link LtiLineItemRowResolution#isIncludedInToolLineItemList()} gate as {@link #getLineItemsForTool})
	 */
	public static SakaiLineItem getLineItemForToolColumn(String signed_placement, String contextId, Long toolId,
			Assignment gbColumn) {
		if (gbColumn == null) {
			return null;
		}
		Map<String, String> assignmentRefToToolKey = getExternalIdsForToolAssignments(contextId);
		LtiLineItemRowResolution r = resolveLtiLineItemRow(contextId, gbColumn, toolId, assignmentRefToToolKey);
		if (!r.isIncludedInToolLineItemList()) {
			return null;
		}
		String external_id = r.getToolContentKey();
		SakaiLineItem li = getLineItem(signed_placement, gbColumn, r.getSakaiAssignment());
		if (StringUtils.isNotBlank(external_id)) {
			String[] parts = external_id.split(ID_SEPARATOR_REGEX);
			if (parts.length > 1 && !StringUtils.equals("0", parts[1])) {
				li.resourceLinkId = "content:" + parts[1];
			}
		}
		return li;
	}

	/*
	 * This is the statically constructed line item for a content item - if this is used, it will create
	 * a line item when a score is first received
	 */
	public static SakaiLineItem constructLineItem(Map<String, Object> content) {
		String signed_placement = SakaiLTIUtil.getSignedPlacement(content);
		SakaiLineItem li = new SakaiLineItem();
		li.label = (String) content.get(LTIService.LTI_TITLE);
		li.resourceLinkId = SakaiLTIUtil.getResourceLinkId(content);
		if ( signed_placement != null ) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement;
		}
		li.scoreMaximum = 100.0;
		return li;
	}

	/*
	 * Gets the default lineItem for a content launch - first check if a line item exists
	 * for the tool that matches the title - if not, check a property and optionally give
	 * the "generic" endpoint that will create the lineitem upon first receipt of a score.
	 */
	public static SakaiLineItem getDefaultLineItem(Site site, Map<String, Object> content) {
		String signed_placement = SakaiLTIUtil.getSignedPlacement(content);
		Long tool_id = LTIUtil.toLongKey(content.get(LTIService.LTI_TOOL_ID));
		log.debug("signed_placement={}; site id={}; tool_id={}", signed_placement, site.getId(), tool_id);
		List<SakaiLineItem> toolItems = LineItemUtil.getLineItemsForTool(signed_placement, site, tool_id, null /* filter */);
		String title = (String) content.get(LTIService.LTI_TITLE);
		for (SakaiLineItem item : toolItems) {
			if ( item.label == null) continue;
			if ( item.label.equals(title) ) {
				return item;
			}
		}

		// Construct the line item if we are asked to do so
		String autoConstruct = ServerConfigurationService.getString(LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM, LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_DEFAULT);
		if ( LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE.equals(autoConstruct) ) {
			return constructLineItem(content);
		}
		return null;
	}

	/**
	 * Gets the default lineItem for a content launch with content bean
	 * @param site the site
	 * @param content the content bean
	 * @return the default line item
	 */
	public static SakaiLineItem getDefaultLineItem(Site site, org.sakaiproject.lti.beans.LtiContentBean content) {
		return getDefaultLineItem(site, content != null ? content.asMap() : null);
	}

	/**
	 * Pull a lineitem out of a Deep Link Response, construct a lineitem from a ContentItem response
	 */
	public static SakaiLineItem extractLineItem(String response_str) {


		JSONObject response = org.tsugi.lti.LTIUtil.parseJSONObject(response_str);
		if ( response == null ) return null;

		// Check if this a DeepLinkResponse
		JSONObject lineItem = LTIUtil.getObject(response, DeepLinkResponse.LINEITEM);
		if ( lineItem == null ) lineItem = LTIUtil.getObject(response, ContentItem.LINEITEM);

		// Nothing to parse here...
		if ( lineItem == null ) return null;

		String lineItemStr = lineItem.toString();
		SakaiLineItem sakaiLineItem = parseLineItem(lineItemStr);
		if ( sakaiLineItem == null ) return null;

		// See if we can find the scoreMaximum the old way
		Double scoreMaximum = ContentItem.getScoreMaximum(lineItem);
		if ( scoreMaximum != null ) sakaiLineItem.scoreMaximum = scoreMaximum;

		return sakaiLineItem;
	}

	/**
	 * Parse a LineItem from a string
	 */
	public static SakaiLineItem parseLineItem(String lineItemStr) {

		if ( lineItemStr == null || StringUtils.isEmpty(lineItemStr) ) return null;

		SakaiLineItem sakaiLineItem = null;

		try {
			sakaiLineItem = (SakaiLineItem) new ObjectMapper().readValue(lineItemStr, SakaiLineItem.class);
		} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
			log.warn("Could not parse input as SakaiLineItem {}",lineItemStr);
			return null;
		}
		return sakaiLineItem;
	}

	/**
	 * Setup a security advisor.
	 */
	public static void pushAdvisor() {
		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvisor.SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	public static void popAdvisor() {
		SecurityService.popAdvisor();
	}
}
