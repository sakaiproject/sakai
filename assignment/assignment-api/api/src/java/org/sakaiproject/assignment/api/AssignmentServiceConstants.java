package org.sakaiproject.assignment.api;

/**
 * Created by enietzel on 2/20/17.
 */
public final class AssignmentServiceConstants {

    private AssignmentServiceConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }

    /** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
    public static final String APPLICATION_ID = "sakai:assignment";

    /** This string starts the references to resources in this service. */
    public static final String REFERENCE_ROOT = "/assignment";

    /** Security function giving the user permission to receive assignment submission email */
    public static final String SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS = "asn.receive.notifications";

    /** Security lock for adding an assignment. */
    public static final String SECURE_ADD_ASSIGNMENT = "asn.new";

    /** Security lock for adding an assignment. */
    public static final String SECURE_ADD_ASSIGNMENT_CONTENT = "asn.new";

    /** Security lock for adding an assignment submission. */
    public static final String SECURE_ADD_ASSIGNMENT_SUBMISSION = "asn.submit";

    /** Security lock for removing an assignment. */
    public static final String SECURE_REMOVE_ASSIGNMENT = "asn.delete";

    /** Security lock for removing an assignment content. */
    public static final String SECURE_REMOVE_ASSIGNMENT_CONTENT = "asn.delete";

    /** Security lock for removing an assignment submission. */
    public static final String SECURE_REMOVE_ASSIGNMENT_SUBMISSION = "asn.delete";

    /** Security lock for accessing an assignment. */
    public static final String SECURE_ACCESS_ASSIGNMENT = "asn.read";

    /** Security lock for accessing an assignment content. */
    public static final String SECURE_ACCESS_ASSIGNMENT_CONTENT = "asn.read";

    /** Security lock for accessing an assignment submission. */
    public static final String SECURE_ACCESS_ASSIGNMENT_SUBMISSION = "asn.submit";

    /** Security lock for updating an assignment. */
    public static final String SECURE_UPDATE_ASSIGNMENT = "asn.revise";

    /** Security lock for updating an assignment content. */
    public static final String SECURE_UPDATE_ASSIGNMENT_CONTENT = "asn.revise";

    /** Security lock for updating an assignment submission. */
    public static final String SECURE_UPDATE_ASSIGNMENT_SUBMISSION = "asn.submit";

    /** Security lock for grading submission */
    public static final String SECURE_GRADE_ASSIGNMENT_SUBMISSION = "asn.grade";

    /** Security function giving the user permission to all groups, if granted to at the site level. */
    public static final String SECURE_ALL_GROUPS = "asn.all.groups";

    /** Security function giving the user permission to share drafts within his/her role for a given site */
    public static final String SECURE_SHARE_DRAFTS = "asn.share.drafts";

    /** The Reference type for a site where site groups are to be considered in security computation. */
    public static final String REF_TYPE_SITE_GROUPS = "site-groups";

    /** The Reference type for an assignment. */
    public static final String REF_TYPE_ASSIGNMENT = "a";

    /** The Reference type for an assignment where site groups are to be considered in security computation. */
    public static final String REF_TYPE_ASSIGNMENT_GROUPS = "a-groups";

    /** The Reference type for a submission. */
    public static final String REF_TYPE_SUBMISSION = "s";

    /** The Reference type for a content. */
    public static final String REF_TYPE_CONTENT = "c";

    /** The Reference type for a grade spreadsheet. */
    public static final String REF_TYPE_GRADES = "grades";

    /** The Reference type for a submissions zip. */
    public static final String REF_TYPE_SUBMISSIONS = "submissions";

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
}
