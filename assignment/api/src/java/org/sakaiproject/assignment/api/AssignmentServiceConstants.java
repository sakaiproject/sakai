/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Created by enietzel on 2/20/17.
 */

public final class AssignmentServiceConstants {

    public static final String ASSIGNMENT_TOOL_ID = "sakai.assignment.grades";
    /**
     * The type string for this application: should not change over time as it may be stored in various parts of persistent entities.
     */
    public static final String SAKAI_ASSIGNMENT = "sakai:assignment";
    /**
     * This string starts the references to resources in this service.
     */
    public static final String REFERENCE_ROOT = "/assignment";
    /**
     * Security function giving the user permission to receive assignment submission email
     */
    public static final String SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS = "asn.receive.notifications";
    /**
     * Security lock for adding an assignment.
     */
    public static final String SECURE_ADD_ASSIGNMENT = "asn.new";
    /**
     * Security lock for adding an assignment submission.
     */
    public static final String SECURE_ADD_ASSIGNMENT_SUBMISSION = "asn.submit";
    /**
     * Security lock for removing an assignment.
     */
    public static final String SECURE_REMOVE_ASSIGNMENT = "asn.delete";
    /**
     * Security lock for removing an assignment submission.
     */
    public static final String SECURE_REMOVE_ASSIGNMENT_SUBMISSION = "asn.delete";
    /**
     * Security lock for accessing an assignment.
     */
    public static final String SECURE_ACCESS_ASSIGNMENT = "asn.read";
    /**
     * Security lock for accessing an assignment submission.
     */
    public static final String SECURE_ACCESS_ASSIGNMENT_SUBMISSION = "asn.submit";
    /**
     * Security lock for updating an assignment.
     */
    public static final String SECURE_UPDATE_ASSIGNMENT = "asn.revise";
    /**
     * Security lock for updating an assignment submission.
     */
    public static final String SECURE_UPDATE_ASSIGNMENT_SUBMISSION = "asn.submit";
    /**
     * Security lock for grading submission
     */
    public static final String SECURE_GRADE_ASSIGNMENT_SUBMISSION = "asn.grade";
    /**
     * Security function giving the user permission to all groups, if granted to at the site level.
     */
    public static final String SECURE_ALL_GROUPS = "asn.all.groups";
    /**
     * Security function giving the user permission to share drafts within his/her role for a given site
     */
    public static final String SECURE_SHARE_DRAFTS = "asn.share.drafts";
    /**
     * The Reference type for an assignment.
     */
    public static final String REF_TYPE_ASSIGNMENT = "a";
    /**
     * The Reference type for a submission.
     */
    public static final String REF_TYPE_SUBMISSION = "s";
    /**
     * The Reference type for a content.
     */
    public static final String REF_TYPE_CONTENT = "c";
    /**
     * The prefix for a Reference ID.
     */
    public static final String REF_PREFIX = "/content";

    // the three choices for Gradebook Integration
    public static final String GRADEBOOK_INTEGRATION_NO = "no";
    public static final String GRADEBOOK_INTEGRATION_ADD = "add";
    public static final String GRADEBOOK_INTEGRATION_ASSOCIATE = "associate";
    public static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";
    public static final String GRADEBOOK_PERMISSION_GRADE_ALL = "gradebook.gradeAll";
    public static final String GRADEBOOK_PERMISSION_EDIT_ASSIGNMENTS = "gradebook.editAssignments";
    // and the prop name
    public static final String PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT = "prop_new_assignment_add_to_gradebook";
    public static final String NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING = "new_assignment_check_anonymous_grading";

    public static final Set<String> PROPERTIES_EXCLUDED_FROM_DUPLICATE_ASSIGNMENTS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    AssignmentConstants.NEW_ASSIGNMENT_DUE_DATE_SCHEDULED,
                    AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED,
                    ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID,
                    ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID,
                    ResourceProperties.PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID,
                    AssignmentServiceConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK,
                    AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT)));

    private AssignmentServiceConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }
}
