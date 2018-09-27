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
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * Some Sakai Utility code for IMS Basic LTI This is mostly code to support the
 * Sakai conventions for making and launching BLTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LineItemUtil {
	
	public final static String ID_SEPARATOR = "|";

	public static String URLEncode(String inp) {
		if ( inp == null ) return null;
		try {
			return java.net.URLEncoder.encode(inp.trim(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}
	
	public static String getExternalId(Long tool_id, Map<String, Object> content, LineItem lineItem)
	{
		String retval = tool_id.toString();
		if ( content == null ) {
			retval += ID_SEPARATOR + "0" + ID_SEPARATOR;
		} else {
			retval += ID_SEPARATOR + content.get(LTIService.LTI_ID) + "|";
		}
		String resourceId = URLEncode(lineItem.resourceId);
		if ( resourceId == null ) {
			retval += ID_SEPARATOR;
		} else {
			retval += resourceId + ID_SEPARATOR;
		}
		String tag = URLEncode(lineItem.tag);
		if ( tag == null ) {
			retval += ID_SEPARATOR;
		} else {
			retval += tag + ID_SEPARATOR;
		}
		
		return retval;
	}
			
	public static Assignment createLineItem(Site site, Long tool_id, Map<String, Object> content, LineItem lineItem) {
		// Look up the assignment so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		String siteId = site.getId();
		if (lineItem.scoreMaximum == null) {
			lineItem.scoreMaximum = 100.0;
		}
		
		if ( lineItem.label == null ) {
			throw new RuntimeException("lineitem.label is required");
		}

		if ( tool_id == null ) {
			throw new RuntimeException("tool_id is required");
		}
		
		String external_id = getExternalId(tool_id, content, lineItem);
System.out.println("external_id="+external_id);
		
		Assignment assignmentObject = null;

		pushAdvisor();
		try {
			List gradebookAssignments = g.getAssignments(siteId);
			for (Iterator i = gradebookAssignments.iterator(); i.hasNext();) {
				Assignment gAssignment = (Assignment) i.next();
				if (gAssignment.isExternallyMaintained()) {
					continue;
				}
				if (lineItem.label.equals(gAssignment.getName())) {
					log.warn("Duplicate label while adding line item {}", lineItem.label);
					break;
				}
			}
		} catch (GradebookNotFoundException e) {
			assignmentObject = null;
		}

		// Attempt to add assignment to grade book
		if (assignmentObject == null && g.isGradebookDefined(siteId)) {
			try {
				assignmentObject = new Assignment();
				assignmentObject.setPoints(Double.valueOf(lineItem.scoreMaximum));
				assignmentObject.setExternallyMaintained(false);
				assignmentObject.setExternalId(external_id);
				assignmentObject.setExternalAppName("IMS-AGS");
				assignmentObject.setName(lineItem.label);
				assignmentObject.setReleased(true);
				assignmentObject.setUngraded(true);
				Long assignmentId = g.addAssignment(siteId, assignmentObject);
				assignmentObject.setId(assignmentId);
				log.info("Added assignment: {} with Id: {}", lineItem.label, assignmentId);
			} catch (ConflictingAssignmentNameException e) {
				log.warn("ConflictingAssignmentNameException while adding assignment {}", e.getMessage());
				assignmentObject = null; // Just to make sure
			} catch (Exception e) {
				log.warn("GradebookNotFoundException (may be because GradeBook has not yet been added to the Site) {}", e.getMessage());
				assignmentObject = null; // Just to make double sure
			}
		}
		if (assignmentObject == null || assignmentObject.getId() == null) {
			log.warn("assignmentObject or Id is null.");
			assignmentObject = null;
		}

		// TODO: Figure this out
		// sess.invalidate(); // Make sure to leave no traces
		popAdvisor();
		return assignmentObject;
	}
	
	
	public static List<LineItem> getLineItemsForTool(Site site, Long tool_id) {
		// Look up the assignment so we can find the max points
		LTIService l = (LTIService) ComponentManager
				.get("org.sakaiproject.lti.api.LTIService");

		String context_id = site.getId();
		
		pushAdvisor();
		List<Map<String,Object>> contents = l.getContents("lti_content.tool_id = "+tool_id, null,0,5000, context_id);
		popAdvisor();
		
		List<LineItem> retval = new ArrayList<> ();
		for (Iterator i = contents.iterator(); i.hasNext();) {
			Map<String, Object> content = (Map) i.next();
			retval.add(getLineItem(content));
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
		li.ltiLinkId = resource_link_id;
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
