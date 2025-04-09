export const i18nUrl = /getI18nProperties.*grader$/;
export const i18n = `
at=at
attachment=Attachment
confirm_remove_feedback_comment=Are you sure you want to remove the feedback comment?
confirm_remove_private_notes=Are you sure you want to remove the private notes?
done=Done
grade=Grade:
no_submission=No submission
no_submission_for=No submission for
peer_reviews=Peer Reviews
reviewer_comments=Reviewer Comments
reviewer_attachments=Reviewer Attachments
grading_rubric_tooltip=Grade this submission using a rubric
rubric=Rubric
autoevaluation=Autoevaluation:
openAutoevaluation=Check Autoevaluation
studentrubric=This is a self-report assignment. You can check the self-report of submitter before grading.
add_feedback_tooltip=Write, or record, some feedback for this student
written_feedback_label=Write some feedback
recorded_feedback_label=Record some feedback
add_attachments_tooltip=Add some attachments to your grading
grade_this=Grade this
private_notes_tooltip=Add notes attached to this student submission, for yourself and other \
instructors and click Done when you've finished. Students will NOT be able to see these.
private_notes_label=Enter your private notes
settings=Settings
graded_status_label=Graded Status:
all_submissions=Show all submissions
only_ungraded=Only show ungraded submissions
only_graded=Only show graded submissions
only_submitted=Only show actual submissions
returned_tooltip=This grade has been returned to the student
submitted=Submitted
grader_on_left=Dock the grader on the left
confirm_remove_attachment=This will remove the feedback attachment permanently. Do you want to continue?
saved_successfully=The grade was saved successfully.
feedback_attachment_tooltip=Click to download this feedback attachment.
student_selector_label=Student selector
attempt_selector_label=Number of attempts selector
lettergrade_selector_label=Letter grade selector
passfail_selector_label=Pass/Fail grade selector
number_grade_label=Numeric grade input field
checkgrade_label=Checkmark grade input box
comment_present=There is a comment on this submission
unsaved_comment_present=The comment on this submission is not saved
notes_present=There are private notes on this submission
unsaved_notes_present=The private notes on this submission is not saved
unsaved_text_warning=Changes are not going to be saved. Click on 'Cancel' again to confirm.
profile_image='s profile image
inline_feedback_instruction=Below is the submission from a student. You can insert comments in this \
text by clicking 'Add Feedback' at the bottom of the submission - comments surrounded by double \
curly braces {{<span class="highlight">like this</span>}} will appear red to the student. Click \
'Done' when you're finished. <strong>Your changes won't be saved until you click one of the save \
buttons in the grader.</strong>
confirm_exceed_max_grade=The grade you entered is greater than the max of {}. Is that okay?
unlimited=Unlimited
hide_history=Hide History
show_history=Show History
hide_history_tooltip=Hide the history for this submission
show_history_tooltip=Show the feedback comment and grade history for this submission
previous_submissions=Previous Submissions
previous_grades=Previous Grades
instructor_feedback=Instructor comments to previous submissions
saving=Saving grade ...
successful_save=Comments and/or grade have been saved.
failed_save=Grade saving failed
grade_submission=Grade Submission
draft_submission=DRAFT
draft_not_submitted=Still a draft - not submitted.
lti_grade_launch_button=Go to External Tool
lti_grade_launch_instructions=The submissions for this assignment are stored in an External Tool (LTI).
lti_grade_not_automatic=<b>Note:</b> When the LTI tool sends a grade to the server, it is not automatically reflected in this user interface until you refresh the grading interface.
destroy_rubric_panel_log=Failed to destroy rubric panel. Maybe it wasn't showing in the first place.
submission_inline=Inline Submission
assign_grade_overrides=Assign Grade Overrides
feedback_comment_label=Enter your feedback
previous_submission_label=View the previous submission
next_submission_label=View the next submission
override_grade_with=Override with:
ungraded=Ungraded
filter_settings_warning=You've applied some settings. Click on the cog icon below to view them.
grader=Grader
rubric_done_tooltip=Set the grade points and close the rubric
add_feedback_comment=Add Feedback Comment
edit_feedback_comment=Edit Feedback Comment
add_private_notes=Add Private Notes
edit_private_notes=Edit Private Notes
points=Points:
pick_category=Pick a category for the grading item
associate_with_existing=Associate this {} with an existing Grading item
select_existing=Select a grading item to associate:
create_new_item=Create a new Grading item
show_less=Show less
show_all=Show all
removed=Removed
loading_1=Loading the grading data ...
loading_2=This may take a few seconds.
loading_submission=Loading submission ...
group_label=Select a group
peer_info_label=Display the peer review instructions
gen.checked=Checked
nav.view.subsOnly=Navigate between students with submissions only
grad3=Graded
nav.list=Return to List
gen.subm=Assignment Submission
addfeedback=Add Feedback
gen.don=Done
non.submission.grade.select=Please select default grade:
grade.max=max
peerassessment.peerGradeInfo=You are able to accept or override the averaged peer review grade in this section.  Once this grade is released, this is the grade that will appear in the gradebook.
ungra=Ungraded
pass=Pass
fail=Fail
gen.checked=Checked
gen.gra2=Grade:
review.report=Report
content_review.score_display.grader=%
content_review.delimiter=-
content_review.disclosure.pending=Pending
content_review.notYetSubmitted.grader=This item has not yet been submitted to
content_review.disclosure.error=Error
gen.assign.gra=Grade
grading_rubric=Grading Rubric
gen.remove=Remove  
download.feedback.attachment=Feedback Attachment(s)
gen.addatt=Add Attachments
allowResubmit=Allow Resubmission
allow.resubmit.number=Number of resubmissions allowed
allow.resubmit.closeTime=Accept Resubmission Until
allowExtension=Allow Extension
allowExtensionCaptionGrader=If this student has not submitted yet, you can set a custom extended due date for this student only. 
submission_history=Submission History
gen.acesubunt=Accept Until
gen.sav=Save
gen.retustud=Save and Release to Student
gen.can=Cancel
grades.lateness.late=Late
gen.assign.spent=Time spent:
select_category_label=Select a category
`;

export const filePickerI18nUrl = /getI18nProperties.*file-picker/;

export const filePickerI18n = `
remove=Remove
to_be_added=To be added:
`;
