/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.cover;

import java.io.OutputStream;
import java.util.Set;
import java.util.Vector;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * AssignmentService is a static Cover for the
 * {@link org.sakaiproject.assignment.api.AssignmentService AssignmentService};
 * see that interface for usage details.
 * </p>
 */
public class AssignmentService {
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.assignment.api.AssignmentService getInstance() {
		if (ComponentManager.CACHE_COMPONENTS) {
			if (m_instance == null)
				m_instance = (org.sakaiproject.assignment.api.AssignmentService) ComponentManager
						.get(org.sakaiproject.assignment.api.AssignmentService.class);
			return m_instance;
		} else {
			return (org.sakaiproject.assignment.api.AssignmentService) ComponentManager
					.get(org.sakaiproject.assignment.api.AssignmentService.class);
		}
	}

	private static org.sakaiproject.assignment.api.AssignmentService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.assignment.api.AssignmentService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.assignment.api.AssignmentService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ALL_GROUPS = org.sakaiproject.assignment.api.AssignmentService.SECURE_ALL_GROUPS;
	
	public static java.lang.String SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS = org.sakaiproject.assignment.api.AssignmentService.SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS;
	
	public static java.lang.String SECURE_ADD_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_ADD_ASSIGNMENT;

	public static java.lang.String SECURE_ADD_ASSIGNMENT_CONTENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_ADD_ASSIGNMENT_CONTENT;

	public static java.lang.String SECURE_ADD_ASSIGNMENT_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.SECURE_ADD_ASSIGNMENT_SUBMISSION;

	public static java.lang.String SECURE_REMOVE_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_REMOVE_ASSIGNMENT;

	public static java.lang.String SECURE_REMOVE_ASSIGNMENT_CONTENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_REMOVE_ASSIGNMENT_CONTENT;

	public static java.lang.String SECURE_REMOVE_ASSIGNMENT_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.SECURE_REMOVE_ASSIGNMENT_SUBMISSION;

	public static java.lang.String SECURE_ACCESS_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_ACCESS_ASSIGNMENT;

	public static java.lang.String SECURE_ACCESS_ASSIGNMENT_CONTENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_ACCESS_ASSIGNMENT_CONTENT;

	public static java.lang.String SECURE_ACCESS_ASSIGNMENT_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.SECURE_ACCESS_ASSIGNMENT_SUBMISSION;

	public static java.lang.String SECURE_UPDATE_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_UPDATE_ASSIGNMENT;

	public static java.lang.String SECURE_UPDATE_ASSIGNMENT_CONTENT = org.sakaiproject.assignment.api.AssignmentService.SECURE_UPDATE_ASSIGNMENT_CONTENT;

	public static java.lang.String SECURE_UPDATE_ASSIGNMENT_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.SECURE_UPDATE_ASSIGNMENT_SUBMISSION;

	public static java.lang.String SECURE_GRADE_ASSIGNMENT_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION;

	public static java.lang.String REF_TYPE_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_ASSIGNMENT;

	public static java.lang.String REF_TYPE_SUBMISSION = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_SUBMISSION;

	public static java.lang.String REF_TYPE_CONTENT = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_CONTENT;

	public static java.lang.String REF_TYPE_GRADES = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_GRADES;

	public static java.lang.String REF_TYPE_SUBMISSIONS = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_SUBMISSIONS;

	public static java.lang.String REF_TYPE_SITE_GROUPS = org.sakaiproject.assignment.api.AssignmentService.REF_TYPE_SITE_GROUPS;

	public static java.lang.String GRADEBOOK_INTEGRATION_NO = org.sakaiproject.assignment.api.AssignmentService.GRADEBOOK_INTEGRATION_NO;

	public static java.lang.String GRADEBOOK_INTEGRATION_ADD = org.sakaiproject.assignment.api.AssignmentService.GRADEBOOK_INTEGRATION_ADD;

	public static java.lang.String GRADEBOOK_INTEGRATION_ASSOCIATE = org.sakaiproject.assignment.api.AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE;

	public static java.lang.String PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT = org.sakaiproject.assignment.api.AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT;

	public static java.lang.String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE= org.sakaiproject.assignment.api.Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE;
	
	public static java.lang.String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE = org.sakaiproject.assignment.api.Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE;
	
	public static java.lang.String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH = org.sakaiproject.assignment.api.Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH;
	
	public static java.lang.String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST = org.sakaiproject.assignment.api.Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST;
	
	public static java.lang.String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = org.sakaiproject.assignment.api.AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK;
	 
	public static boolean allowReceiveSubmissionNotification(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowReceiveSubmissionNotification(param0);
	}
	
	public static java.util.List allowReceiveSubmissionNotificationUsers(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.allowReceiveSubmissionNotificationUsers(param0);
	}
	
	public static boolean allowAddAssignment(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddAssignment(param0);
	}

	public static boolean allowAddSiteAssignment(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddSiteAssignment(param0);
	}
	
	public static boolean allowAllGroups(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
		    return false;
		
		return service.allowAllGroups(param0);
	}

	public static java.util.Collection getGroupsAllowAddAssignment(
			java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return new Vector();

		return service.getGroupsAllowAddAssignment(param0);
	}
	
	public static java.util.Collection getGroupsAllowGradeAssignment(
			java.lang.String param0, java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return new Vector();

		return service.getGroupsAllowGradeAssignment(param0, param1);
	}

	public static boolean allowGetAssignment(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowGetAssignment(param0);
	}

	public static boolean allowUpdateAssignment(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowUpdateAssignment(param0);
	}

	public static boolean allowRemoveAssignment(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowRemoveAssignment(param0);
	}

	public static boolean allowAddAssignmentContent(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddAssignmentContent(param0);
	}

	public static boolean allowGetAssignmentContent(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowGetAssignmentContent(param0);
	}

	public static boolean allowUpdateAssignmentContent(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowUpdateAssignmentContent(param0);
	}

	public static boolean allowRemoveAssignmentContent(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowRemoveAssignmentContent(param0);
	}

	public static boolean allowAddSubmission(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowAddSubmission(param0);
	}

	public static java.util.List allowAddSubmissionUsers(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.allowAddSubmissionUsers(param0);
	}
	
	public static java.util.List allowGradeAssignmentUsers(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.allowGradeAssignmentUsers(param0);
	}
	
	public static java.util.List allowAddAnySubmissionUsers(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.allowAddAnySubmissionUsers(param0);
	}

	public static java.util.List allowAddAssignmentUsers(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.allowAddAssignmentUsers(param0);
	}

	public static boolean allowGetSubmission(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowGetSubmission(param0);
	}

	public static boolean allowUpdateSubmission(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowUpdateSubmission(param0);
	}

	public static boolean allowRemoveSubmission(java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowRemoveSubmission(param0);
	}

	public static boolean allowGradeSubmission(String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.allowGradeSubmission(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentEdit addAssignment(
			java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.addAssignment(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentEdit mergeAssignment(
			org.w3c.dom.Element param0)
			throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.mergeAssignment(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentEdit addDuplicateAssignment(
			java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.IdUnusedException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.addDuplicateAssignment(param0, param1);
	}

	public static void removeAssignment(
			org.sakaiproject.assignment.api.AssignmentEdit param0)
			throws org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.removeAssignment(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentEdit editAssignment(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.editAssignment(param0);
	}

	public static void commitEdit(
			org.sakaiproject.assignment.api.AssignmentContentEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.commitEdit(param0);
	}

	public static void commitEdit(
			org.sakaiproject.assignment.api.AssignmentEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.commitEdit(param0);
	}

	public static void commitEdit(
			org.sakaiproject.assignment.api.AssignmentSubmissionEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.commitEdit(param0);
	}

	public static void cancelEdit(
			org.sakaiproject.assignment.api.AssignmentSubmissionEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.cancelEdit(param0);
	}

	public static void cancelEdit(
			org.sakaiproject.assignment.api.AssignmentContentEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.cancelEdit(param0);
	}

	public static void cancelEdit(
			org.sakaiproject.assignment.api.AssignmentEdit param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.cancelEdit(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentContentEdit addAssignmentContent(
			java.lang.String param0)
			throws org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.addAssignmentContent(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentContentEdit mergeAssignmentContent(
			org.w3c.dom.Element param0)
			throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.mergeAssignmentContent(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentContentEdit addDuplicateAssignmentContent(
			java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.IdUnusedException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.addDuplicateAssignmentContent(param0, param1);
	}

	public static void removeAssignmentContent(
			org.sakaiproject.assignment.api.AssignmentContentEdit param0)
			throws org.sakaiproject.assignment.api.AssignmentContentNotEmptyException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.removeAssignmentContent(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentContentEdit editAssignmentContent(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.editAssignmentContent(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentSubmissionEdit addSubmission(
			java.lang.String param0, java.lang.String param1, java.lang.String param2)
			throws org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.addSubmission(param0, param1, param2);
	}

	public static org.sakaiproject.assignment.api.AssignmentSubmissionEdit mergeSubmission(
			org.w3c.dom.Element param0)
			throws org.sakaiproject.exception.IdInvalidException,
			org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.mergeSubmission(param0);
	}

	public static void removeSubmission(
			org.sakaiproject.assignment.api.AssignmentSubmissionEdit param0)
			throws org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return;

		service.removeSubmission(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentSubmissionEdit editSubmission(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException,
			org.sakaiproject.exception.InUseException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.editSubmission(param0);
	}

	public static java.util.Iterator getAssignmentContents(
			org.sakaiproject.user.api.User param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignmentContents(param0);
	}

	public static org.sakaiproject.assignment.api.Assignment getAssignment(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignment(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentContent getAssignmentContent(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignmentContent(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentSubmission getSubmission(
			java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getSubmission(param0);
	}

	public static org.sakaiproject.assignment.api.AssignmentSubmission getSubmission(
			java.lang.String param0, org.sakaiproject.user.api.User param1)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getSubmission(param0, param1);
	}
	
	public static org.sakaiproject.assignment.api.AssignmentSubmission getSubmission(
			java.util.List param0, org.sakaiproject.user.api.User param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getSubmission(param0, param1);
	}

	public static java.util.Iterator getAssignments(
			org.sakaiproject.assignment.api.AssignmentContent param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignments(param0);
	}

	public static java.util.Iterator getAssignmentsForContext(
			java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignmentsForContext(param0);
	}

	public static java.util.Iterator getAssignmentsForContext(
			java.lang.String param0, java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getAssignmentsForContext(param0, param1);
	}

	public static java.util.List getListAssignmentsForContext(
			java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getListAssignmentsForContext(param0);
	}

	public static java.util.List getSubmissions(
			org.sakaiproject.assignment.api.Assignment param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getSubmissions(param0);
	}
	
	public static int getSubmittedSubmissionsCount(
			java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return 0;

		return service.getSubmittedSubmissionsCount(param0);
	}
	
	public static int getUngradedSubmissionsCount(
			java.lang.String param0) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return 0;

		return service.getUngradedSubmissionsCount(param0);
	}

	public static byte[] getGradesSpreadsheet(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getGradesSpreadsheet(param0);
	}
	public static void getSubmissionsZip(OutputStream param0, java.lang.String param1) throws org.sakaiproject.exception.IdUnusedException,
		org.sakaiproject.exception.PermissionException{
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service != null)
		service.getSubmissionsZip(param0, param1);
	}

	public static java.lang.String assignmentReference(java.lang.String param0,
			java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.assignmentReference(param0, param1);
	}

	public static java.lang.String contentReference(java.lang.String param0,
			java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.contentReference(param0, param1);
	}

	public static java.lang.String submissionReference(java.lang.String param0,
			java.lang.String param1, java.lang.String param2) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.submissionReference(param0, param1, param2);
	}

	public static java.lang.String gradesSpreadsheetReference(
			java.lang.String param0, java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.gradesSpreadsheetReference(param0, param1);
	}

	public static java.lang.String submissionsZipReference(
			java.lang.String param0, java.lang.String param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.submissionsZipReference(param0, param1);
	}

	public static java.lang.String merge(java.lang.String param0,
			org.w3c.dom.Element param1, java.lang.String param2,
			java.lang.String param3, java.util.Map param4,
			java.util.HashMap param5, Set param6) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.merge(param0, param1, param2, param3, param4, param5,
				param6);
	}

	public static java.lang.String getLabel() {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0,
			org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3, java.util.List param4) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return null;

		return service.archive(param0, param1, param2, param3, param4);
	}

	public static boolean getAllowGroupAssignments() {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.getAllowGroupAssignments();
	}

	public static boolean getAllowGroupAssignmentsInGradebook() {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.getAllowGroupAssignmentsInGradebook();
	}
	
	public static boolean canSubmit(String param0, org.sakaiproject.assignment.api.Assignment param1) {
		org.sakaiproject.assignment.api.AssignmentService service = getInstance();
		if (service == null)
			return false;

		return service.canSubmit(param0, param1);
	}
}
