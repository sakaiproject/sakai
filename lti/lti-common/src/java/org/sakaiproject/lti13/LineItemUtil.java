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
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import org.json.simple.JSONObject;

import org.tsugi.lti.ContentItem;
import org.tsugi.lti13.DeepLinkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI13_PATH;
import static org.sakaiproject.lti.util.SakaiLTIUtil.getOurServerUrl;
import org.sakaiproject.lti.util.SakaiLTIUtil;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.foorm.Foorm;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.component.cover.ServerConfigurationService;

import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
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

	public static final String GB_EXTERNAL_APP_NAME = "IMS-AGS";
	public static final String ASSIGNMENTS_EXTERNAL_APP_NAME = "Assignments"; // Avoid circular references
	public static final String ASSIGNMENT_REFERENCE_PREFIX = "/assignment/a";

	public final static String ID_SEPARATOR = "|";
	public final static String ID_SEPARATOR_REGEX = "\\|";

	// TODO: SAK-44137 - In Sakai-22 or later switch this default
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM = "lti.advantage.construct.lineitem";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE = "true";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_FALSE = "false";
	public static final String LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_DEFAULT = LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE;

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
	 * @param content - The content item (can be null)
	 * @param lineItem - The lineItem to insert
	 * @return The properly formatted external id, or null if content is null or has no tool id
	 */
	public static String constructExternalId(LtiContentBean content, SakaiLineItem lineItem) {
		if (content == null) return null;
		Long toolId = content.getToolId();
		if (toolId == null) return null;
		return constructExternalIdImpl(toolId, content.getId(), lineItem);
	}

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

	private static Long deriveContentIdFromGradebookExternalId(String external_id) {
		if (external_id == null) {
			return null;
		}
		String[] parts = StringUtils.split(external_id, ID_SEPARATOR_REGEX);
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

		String external_id = constructExternalId(tool_id, content, lineItem);

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
			gradebookColumn.setExternalId(external_id);
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

	public static Assignment updateLineItem(Site site, Long tool_id, Long column_id, SakaiLineItem lineItem) {
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

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

		if ( lineItem.scoreMaximum != null ) {
			gradebookColumn.setPoints(Double.valueOf(lineItem.scoreMaximum));
		}

		Long content_id = deriveContentIdFromGradebookExternalId(gradebookColumn.getExternalId());
		String external_id = (content_id != null) ? constructExternalIdImpl(tool_id, content_id, lineItem) : constructExternalId(tool_id, null, lineItem);

		log.debug("gb item id={}; gb item title={}; external_id={}; prior external_id={}; derived content id={}", gradebookColumn.getId(),
			  gradebookColumn.getName(), external_id, gradebookColumn.getExternalId(), content_id);

		gradebookColumn.setExternalId(external_id);
		if ( lineItem.label != null ) {
			gradebookColumn.setName(lineItem.label);
		}

		Boolean releaseToStudent = lineItem.releaseToStudent == null ? Boolean.TRUE : lineItem.releaseToStudent; // Default to true
		Boolean includeInComputation = lineItem.includeInComputation == null ? Boolean.TRUE : lineItem.includeInComputation; // Default true
		gradebookColumn.setReleased(releaseToStudent); // default true
		gradebookColumn.setCounted(includeInComputation); // default true
		gradebookColumn.setUngraded(false);
		Date dueDate = LTIUtil.parseIMS8601(lineItem.endDateTime);
		if ( dueDate != null ) gradebookColumn.setDueDate(dueDate);

		pushAdvisor();
		try {
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
				LtiContentBean content = ltiService.getContentAsBean(assignmentContentId.longValue(), context_id);
				if ( content == null ) continue;
				retval.put(assignmentReference, constructExternalId(content, null));
			}
		} catch (Exception e) {
			log.error("Unexpected Throwable", e.toString());
			log.debug("Stacktrace", e);
		}
		return retval;
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
		Map<String, String> externalIds = null;

		pushAdvisor();
		try {
			List<Assignment> gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				String external_id = gbColumn.getExternalId();
				if ( isGradebookColumnLTI(gbColumn) ) {
					// We are good to go
				} else if ( isAssignmentColumn(external_id) ) {
					if ( externalIds == null ) {
						externalIds = getExternalIdsForToolAssignments(context_id);
					}
					external_id = externalIds.get(external_id);
					if ( external_id == null ) continue;
				}

				// Parse the external_id
				// tool_id|content_id|resourceLink|tag|
				if ( external_id == null || external_id.length() < 1 ) continue;

				String[] parts = external_id.split(ID_SEPARATOR_REGEX);
				if ( parts.length < 1 || ! parts[0].equals(tool_id.toString()) ) continue;

				retval.add(gbColumn);
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
			List gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				if ( ! isGradebookColumnLTI(gbColumn) ) continue;

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
	 * Determine if a grade book column is relevant to LTI
	 */
	public static boolean isGradebookColumnLTI(Assignment gradebookColumn) {
		// if (gradebookColumn.isExternallyMaintained()) return false;
		if ( GB_EXTERNAL_APP_NAME.equals(gradebookColumn.getExternalAppName()) ) return true;
		if ( ASSIGNMENTS_EXTERNAL_APP_NAME.equals(gradebookColumn.getExternalAppName())) return true;
		return false;
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
		Map<String, String> externalIds = null;

		pushAdvisor();
		try {

			List gradebookColumns = gradingService.getAssignments(context_id, context_id, SortType.SORT_BY_NONE);
			for (Iterator i = gradebookColumns.iterator(); i.hasNext();) {
				Assignment gbColumn = (Assignment) i.next();
				String external_id = gbColumn.getExternalId();
				log.debug("gbColumn: {} {}", gbColumn.getName(), external_id);
				if ( isGradebookColumnLTI(gbColumn) ) {
					// We are good to go
				} else if ( StringUtils.isNotEmpty(external_id) && isAssignmentColumn(external_id) ) {
					if ( externalIds == null ) {
						externalIds = getExternalIdsForToolAssignments(context_id);
					}
					external_id = externalIds.get(external_id);
					log.debug("derived assignment based on external_id: {} {}", external_id, externalIds);
					if ( external_id == null ) continue;
				}

				// Parse the external_id
				// tool_id|content_id|resourceLink|tag|assignmentRef (optional)
				if ( external_id == null || external_id.length() < 1 ) continue;

				log.debug("gb column id={}; title={}; external_id={}", gbColumn.getId(), gbColumn.getName(), external_id);

				String[] parts = external_id.split(ID_SEPARATOR_REGEX);
				if ( parts.length < 1 || ! parts[0].equals(tool_id.toString()) ) continue;

				SakaiLineItem item = getLineItem(signed_placement, gbColumn);
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

	public static SakaiLineItem getLineItem(String signed_placement, Assignment assignment) {
		SakaiLineItem li = new SakaiLineItem();
		li.label = assignment.getName();
		li.scoreMaximum = assignment.getPoints();
		Date dueDate = assignment.getDueDate();
		if ( dueDate != null ) {
			li.endDateTime = org.tsugi.lti.LTIUtil.getISO8601(dueDate);
		}

		// Parse the external_id
		// tool_id|content_id|resourceLink|tag|
		String external_id = assignment.getExternalId();
		if ( external_id != null && external_id.length() > 0 ) {
			String[] parts = external_id.split(ID_SEPARATOR_REGEX);
			li.resourceLinkId = (parts.length > 1 && parts[1].trim().length() > 1) ? parts[1].trim() : null;
			li.resourceId  = (parts.length > 2 && parts[2].trim().length() > 1) ? parts[2].trim() : null;
			li.tag = (parts.length > 3 && parts[3].trim().length() > 1) ? parts[3].trim() : null;
		}

		if ( signed_placement != null ) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + assignment.getId();
		}

		return li;
	}

	/*
	 * This is the statically constructed line item for a content item - if this is used, it will create
	 * a line item when a score is first received. Bean overload using bean-based SakaiLTIUtil methods.
	 */
	public static SakaiLineItem constructLineItem(LtiContentBean content) {
		if (content == null) return null;
		String signed_placement = SakaiLTIUtil.getSignedPlacement(content);
		SakaiLineItem li = new SakaiLineItem();
		li.label = content.getTitle();
		li.resourceLinkId = SakaiLTIUtil.getResourceLinkId(content);
		if ( signed_placement != null ) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement;
		}
		li.scoreMaximum = 100.0;
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
	 * Gets the default lineItem for a content launch with content bean. Uses bean-based SakaiLTIUtil methods.
	 * @param site the site
	 * @param content the content bean
	 * @return the default line item
	 */
	public static SakaiLineItem getDefaultLineItem(Site site, LtiContentBean content) {
		if (content == null) return null;
		String signed_placement = SakaiLTIUtil.getSignedPlacement(content);
		Long tool_id = content.getToolId();
		log.debug("signed_placement={}; site id={}; tool_id={}", signed_placement, site.getId(), tool_id);
		List<SakaiLineItem> toolItems = LineItemUtil.getLineItemsForTool(signed_placement, site, tool_id, null /* filter */);
		String title = content.getTitle();
		for (SakaiLineItem item : toolItems) {
			if ( item.label == null) continue;
			if ( item.label.equals(title) ) {
				return item;
			}
		}
		String autoConstruct = ServerConfigurationService.getString(LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM, LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_DEFAULT);
		if ( LTI_ADVANTAGE_CONSTRUCT_LINE_ITEM_TRUE.equals(autoConstruct) ) {
			return constructLineItem(content);
		}
		return null;
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
