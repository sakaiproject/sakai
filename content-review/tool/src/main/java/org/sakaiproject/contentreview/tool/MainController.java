/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.contentreview.tool;

import java.security.InvalidParameterException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * MainController
 *
 * This is the controller used by Spring MVC to handle requests
 *
 * @author Bryan Holladay
 *
 *
 */
@Slf4j
@Controller
public class MainController {

	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	@Qualifier("org.sakaiproject.contentreview.service.ContentReviewService")
	private ContentReviewService contentReviewService;
	
	@Autowired
	private AssignmentService assignmentService;

	
	@RequestMapping(value = "/webhooks", method = RequestMethod.POST)
	public void webhooks(HttpServletRequest request, HttpServletResponse response, Model model, 
			@RequestParam Integer providerId, @RequestParam(required=false) String custom) {
		if(providerId == null) {
			throw new InvalidParameterException("Missing providerName");
		}
		log.info("webhook provider and custom: " + custom);
		contentReviewService.webhookEvent(request, providerId, Optional.ofNullable(custom));
	}
	
	@RequestMapping(value = "/viewreport", method = RequestMethod.GET)
	public String viewReport(Model model, @RequestParam String contentId, @RequestParam String assignmentRef) {
		log.info("viewReport(): contentId: " + contentId + ", assignmentRef: " + assignmentRef);
		if(sessionManager != null && sessionManager.getCurrentSession() != null
				&& StringUtils.isNotEmpty(sessionManager.getCurrentSessionUserId())) {
			boolean isInstructor = hasInstructorPermissions(assignmentRef);
			if(!isInstructor && !hasStudentPermission(assignmentRef, contentId)) {
				//this user doesn't have access to view this item
				throw new SecurityException("A valid session ID with access to the content item is required");
			}
			return "redirect:" + contentReviewService.getReviewReportRedirectUrl(contentId, assignmentRef, sessionManager.getCurrentSessionUserId(), isInstructor);
		}
		throw new SecurityException("A valid session ID with access to the content item is required");			
	}
	
	/**
	 * Depending on the assignmentRef, a check will be used to determine whether the user should have instructor access 
	 * @param assignmentRef
	 * @return
	 */
	private boolean hasInstructorPermissions(String assignmentRef) {
		if(StringUtils.isNotEmpty(assignmentRef)) {
			if(assignmentRef.startsWith(AssignmentServiceConstants.REFERENCE_ROOT)) {
				AssignmentReferenceReckoner.AssignmentReference refReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentRef).reckon();
				if("a".equals(refReckoner.getSubtype())) {
					//ASSIGNMENT1 instructor permission check
					return assignmentService.allowGradeSubmission(assignmentRef);
				}
			}
		}
		return false;
	}

	/**
	 * Depending on the assignmentRef, a check will be used to determine whether the user should have student access
	 * @param assignmentRef
	 * @param contentId
	 * @return
	 */
	private boolean hasStudentPermission(String assignmentRef, String contentId) {
		ContentReviewItem item = contentReviewService.getContentReviewItemByContentId(contentId);
		if(item != null && sessionManager.getCurrentSessionUserId().equals(item.getUserId())) {
			return true;
		}else {
			if(assignmentRef.startsWith(AssignmentServiceConstants.REFERENCE_ROOT)) {
				//If assignment, check the current user's submission for this assignment
				try {
					AssignmentReferenceReckoner.AssignmentReference refReckoner = AssignmentReferenceReckoner.reckoner().reference(assignmentRef).reckon();
					if("a".equals(refReckoner.getSubtype())) {
						AssignmentSubmission submission = assignmentService.getSubmission(refReckoner.getId(), sessionManager.getCurrentSessionUserId());
						return submission != null && submission.getAttachments().contains(AssignmentServiceConstants.REF_PREFIX + contentId);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
}
