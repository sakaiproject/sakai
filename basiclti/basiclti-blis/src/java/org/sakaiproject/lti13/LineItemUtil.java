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
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.LTI13_PATH;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getOurServerUrl;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getSignedPlacement;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lti.api.LTIService;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.tsugi.ags2.objects.LineItem;
import org.tsugi.ags2.objects.Result;

/**
 * Some Sakai Utility code for IMS Basic LTI This is mostly code to support the
 * Sakai conventions for making and launching BLTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LineItemUtil {

	public static final String GB_EXTERNAL_APP_NAME = "IMS-AGS";

	public final static String ID_SEPARATOR = "|";
	public final static String ID_SEPARATOR_REGEX = "\\|";

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
	public static String constructExternalId(Long tool_id, Map<String, Object> content, LineItem lineItem)
	{
		String retval = tool_id.toString();
		if ( content == null ) {
			retval += ID_SEPARATOR + "0" + ID_SEPARATOR;
		} else {
			retval += ID_SEPARATOR + content.get(LTIService.LTI_ID) + "|";
		}
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
		return retval;
	}

	public static Assignment createLineItem(Site site, Long tool_id, Map<String, Object> content, LineItem lineItem) {
		// Look up the assignment so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		String context_id = site.getId();
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

		Assignment assignmentObject = null;

		// Check for duplicate labels
		List<Assignment> assignments = getAssignmentsForToolDAO(context_id, tool_id);
		if ( assignments == null ) {
			throw new RuntimeException("Could not list assignments for "+context_id+" tool="+tool_id);
		}

		for (Iterator i = assignments.iterator(); i.hasNext();) {
			Assignment gAssignment = (Assignment) i.next();

			if (lineItem.label.equals(gAssignment.getName())) {
				throw new RuntimeException("Duplicate label while adding line item {}" + lineItem.label);
			}
		}

		pushAdvisor();
		String failure = null;
		try {
			Long assignmentId = null;
			// Attempt to add assignment to grade book
			if (assignmentObject == null && g.isGradebookDefined(context_id)) {
				try {
					assignmentObject = new Assignment();
					assignmentObject.setPoints(Double.valueOf(lineItem.scoreMaximum));
					// We are using the sctual grade and points possible in the GB
					assignmentObject.setExternallyMaintained(false);
					assignmentObject.setExternalId(external_id);
					assignmentObject.setExternalAppName(GB_EXTERNAL_APP_NAME);
					assignmentObject.setName(lineItem.label);
					assignmentObject.setReleased(true);
					assignmentObject.setUngraded(true);
					assignmentId = g.addAssignment(context_id, assignmentObject);
					assignmentObject.setId(assignmentId);
					// Update sets the external values while add does not.
					g.updateAssignment(context_id, assignmentId, assignmentObject);
					log.info("Added assignment: {} with Id: {}", lineItem.label, assignmentId);
				} catch (ConflictingAssignmentNameException e) {
					failure = "ConflictingAssignmentNameException while adding assignment " + e.getMessage();
					assignmentObject = null; // Just to make sure
				} catch (Exception e) {
					failure = "GradebookNotFoundException (may be because GradeBook has not yet been added to the Site) "+ e.getMessage();
					assignmentObject = null; // Just to make double sure
				}
			}

			if (assignmentObject == null || assignmentObject.getId() == null) {
				if ( failure == null ) failure = "assignmentObject or Id is null.";
				assignmentObject = null;
			}
		} catch (GradebookNotFoundException e) {
			failure = "GradebookNotFoundException";
			log.error("Gradebook not found", e);
		} catch (Throwable e) {
			failure = "Unexpected Throwable " + e.getMessage();
			log.error("Unexpected Throwable", e);
		}  finally {
			popAdvisor();
		}

		if ( failure != null ) {
			throw new RuntimeException(failure);
		}

		return assignmentObject;
	}

	public static Assignment updateLineItem(Site site, Long tool_id, Long assignment_id, LineItem lineItem) {
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		String context_id = site.getId();

		if ( assignment_id == null ) {
			throw new RuntimeException("tool_id is required");
		}

		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}

		Assignment assignmentObject = getAssignmentByKeyDAO(context_id, tool_id, assignment_id);
		if ( assignmentObject == null ) return null;

		/*
			{
			  "scoreMaximum": 0,
			  "label": "string",
			  "tag": "string",
			  "resourceId": "string"
			}
		*/

		if ( lineItem.scoreMaximum != null ) {
			assignmentObject.setPoints(Double.valueOf(lineItem.scoreMaximum));
		}

		String external_id = constructExternalId(tool_id, null, lineItem);
		assignmentObject.setExternalId(external_id);
		if ( lineItem.label != null ) {
			assignmentObject.setName(lineItem.label);
		}

		pushAdvisor();
		try {
			g.updateAssignment(context_id, assignment_id, assignmentObject);
		} finally {
			popAdvisor();
		}

		return assignmentObject;
	}

	/**
	 * Return a list of assignments associated with this tool in a site
	 * @param context_id - The site id
	 * @param tool_id - The tool id
	 * @return A list of Assignment objects (perhaps empty) or null on failure
	 */
	protected static List<Assignment> getAssignmentsForToolDAO(String context_id, Long tool_id) {
		List retval = new ArrayList();
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		pushAdvisor();
		try {
			List gradebookAssignments = g.getAssignments(context_id);
			for (Iterator i = gradebookAssignments.iterator(); i.hasNext();) {
				Assignment gAssignment = (Assignment) i.next();
				if (gAssignment.isExternallyMaintained()) {
					continue;
				}
				if ( ! GB_EXTERNAL_APP_NAME.equals(gAssignment.getExternalAppName()) ) {
					continue;
				}

				// Parse the external_id
				// tool_id|content_id|resourceLink|tag|
				String external_id = gAssignment.getExternalId();
				if ( external_id == null || external_id.length() < 1 ) continue;

				String[] parts = external_id.split(ID_SEPARATOR_REGEX);
				if ( parts.length < 1 || ! parts[0].equals(tool_id.toString()) ) continue;

				retval.add(gAssignment);
			}
		} catch (GradebookNotFoundException e) {
			log.error("Gradebook not found context_id={}", context_id);
			retval = null;
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.getMessage());
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
	 * @param assignment_id
	 * @return
	 */
	protected static Assignment getAssignmentByKeyDAO(String context_id, Long tool_id, Long assignment_id)
	{
		List<Assignment> assignments = getAssignmentsForToolDAO(context_id, tool_id);
		for (Iterator i = assignments.iterator(); i.hasNext();) {
			Assignment gAssignment = (Assignment) i.next();
			if (assignment_id.equals(gAssignment.getId())) return gAssignment;
		}
		return null;
	}

	/**
	 * Load a particular assignment by its internal Sakai GB key
	 * @param context_id
	 * @param tool_id
	 * @param assignment_id
	 * @return
	 */
	protected static Assignment getAssignmentByLabelDAO(String context_id, Long tool_id, String assignment_label)
	{
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");
		Assignment retval = null;

		pushAdvisor();
		try {
			List gradebookAssignments = g.getAssignments(context_id);
			for (Iterator i = gradebookAssignments.iterator(); i.hasNext();) {
				Assignment gAssignment = (Assignment) i.next();
				if (gAssignment.isExternallyMaintained()) {
					continue;
				}
				if (assignment_label.equals(gAssignment.getName())) {
					retval = gAssignment;
					break;
				}
			}
		} catch (GradebookNotFoundException e) {
			log.error("Gradebook not found context_id={}", context_id);
			retval = null;
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.getMessage());
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
	 * @param assignment_id
	 * @return
	 */
	protected static boolean deleteAssignmentByKeyDAO(String context_id, Long tool_id, Long assignment_id)
	{
		// Make sure it belongs to us
		Assignment a = getAssignmentByKeyDAO(context_id, tool_id, assignment_id);
		if ( a == null ) return false;
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		pushAdvisor();
		try {
			// Provides us no return value
			g.removeAssignment(assignment_id);
		} finally {
			popAdvisor();
		}

		return true;
	}
	/**
	 * Get the line items from the gradebook for a tool
	 * @param site The site we are looking at
	 * @param tool_id The tool we are scanning for
	 * @param filter Optional line item with resourceId or tag used to filter returned results
	 * @return A List of LineItems - an empty list is returned if none exist
	 */
	public static List<LineItem> getLineItemsForTool(String signed_placement, Site site, Long tool_id, LineItem filter) {

		String context_id = site.getId();
		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		List<LineItem> retval = new ArrayList<>();

		pushAdvisor();
		try {
			List gradebookAssignments = g.getAssignments(context_id);
			for (Iterator i = gradebookAssignments.iterator(); i.hasNext();) {
				Assignment gAssignment = (Assignment) i.next();
				if (gAssignment.isExternallyMaintained()) {
					continue;
				}
				if ( ! GB_EXTERNAL_APP_NAME.equals(gAssignment.getExternalAppName()) ) {
					continue;
				}

				// Parse the external_id
				// tool_id|content_id|resourceLink|tag|
				String external_id = gAssignment.getExternalId();
				if ( external_id == null || external_id.length() < 1 ) continue;

				String[] parts = external_id.split(ID_SEPARATOR_REGEX);
				if ( parts.length < 1 || ! parts[0].equals(tool_id.toString()) ) continue;

				LineItem item = getLineItem(signed_placement, gAssignment);

				if ( filter != null ) {
					if ( filter.resourceLinkId != null && ! filter.resourceLinkId.equals(item.resourceLinkId)) continue;
					if ( filter.resourceId != null && ! filter.resourceId.equals(item.resourceId)) continue;
					if ( filter.tag != null && ! filter.tag.equals(item.tag)) continue;
				}
				retval.add(item);
			}
		} catch (GradebookNotFoundException e) {
			log.error("Gradebook not found context_id={}", context_id);
		} catch (Throwable e) {
			log.error("Unexpected Throwable", e.getMessage());
		} finally {
			popAdvisor();
		}

		return retval;
	}

	public static LineItem getLineItem(String signed_placement, Assignment assignment) {
		LineItem li = new LineItem();
		li.label = assignment.getName();
		li.scoreMaximum = assignment.getPoints();

		// Parse the external_id
		// tool_id|content_id|resourceLink|tag|
		String external_id = assignment.getExternalId();
		if ( external_id != null && external_id.length() > 0 ) {
			String[] parts = external_id.split(ID_SEPARATOR_REGEX);
			li.resourceId  = (parts.length > 2 && parts[2].trim().length() > 1) ? parts[2].trim() : null;
			li.tag = (parts.length > 3 && parts[3].trim().length() > 1) ? parts[3].trim() : null;
		}

		if ( signed_placement != null ) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + assignment.getId();
		}

		return li;
	}

	/**
	 * Get the pre-created line items associated with content items in a site
	 * @param site The site we are looking at
	 * @param tool_id The tool we are scanning for
	 * @param filter Optional line item with resourceId or tag used to filter returned results
	 * @return A List of LineItems - an empty list is returned if none exist
	 */
	public static List<LineItem> getPreCreatedLineItems(Site site, Long tool_id, LineItem filter) {
		// Look up the assignment so we can find the max points
		LTIService l = (LTIService) ComponentManager
				.get("org.sakaiproject.lti.api.LTIService");

		String context_id = site.getId();

		List<Map<String,Object>> contents = null;
		pushAdvisor();
		try {
			contents = l.getContents("lti_content.tool_id = "+tool_id, null,0,5000, context_id);
		} catch (Exception e) {
			log.error("Unexpected Exception", e.getMessage());
		} finally {
			popAdvisor();
		}

		List<LineItem> retval = new ArrayList<> ();
		if ( contents == null ) return retval;  // Unlikely

		for (Iterator i = contents.iterator(); i.hasNext();) {
			Map<String, Object> content = (Map) i.next();
			LineItem item = getLineItem(content);
			if ( filter != null ) {
				if ( filter.resourceLinkId != null && ! filter.resourceLinkId.equals(item.resourceLinkId)) continue;
				if ( filter.resourceId != null && ! filter.resourceId.equals(item.resourceId)) continue;
				if ( filter.tag != null && ! filter.tag.equals(item.tag)) continue;
			}
			retval.add(item);
		}

		return retval;
	}

	public static LineItem getLineItem(Map<String, Object> content) {
		String context_id = (String) content.get(LTIService.LTI_SITE_ID);
		String resource_link_id = "content:" + content.get(LTIService.LTI_ID);
		String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
		String signed_placement = null;
		if ( placement_secret != null ) {
			signed_placement = getSignedPlacement(context_id, resource_link_id, placement_secret);
		}
		LineItem li = new LineItem();
		li.label = (String) content.get(LTIService.LTI_TITLE);
		li.resourceLinkId = resource_link_id;
		if ( context_id != null && signed_placement != null ) {
			li.id = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement;
		}
		li.scoreMaximum = 100.0;
		return li;
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
