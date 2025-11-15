This feature forces the submission of saved assignment drafts for students who have NO previous submissions for that particular assignment.

This feature is disabled by default.
assignment.autoSubmit.enabled=true to enable it in sakai.properties. Also, administrators need to set up a quartz job to auto-submit the draft assignments.

LOGIC:

- Only submit saved assignment drafts for students who have no previous submissions for that particular assignment
- If the assignment is past due date or close date, draft submissions with content will be auto-submitted
- If assignment has no due date or close date, the feature will not process that assignment
- The auto-submitted assignment will be marked as submitted with the draft saved time
- A property "auto_submitted" will be added to indicate this was automatically submitted
- Events will be posted for auditing purposes

NOTES:

- Draft submissions are identified as submissions that are not submitted but have either submitted text or attachments
- This only affects assignments that are not in draft status themselves
- Group assignments are supported - each group's draft submission is processed independently
- The job logs all actions for auditing and troubleshooting

CONFIGURATION:

1. Enable the feature in sakai.properties:
   assignment.autoSubmit.enabled=true

2. Set up a scheduled job in the Admin Workspace > Job Scheduler:
   - Job Name: Auto Submit Assignments Job
   - Job Class: org.sakaiproject.api.app.scheduler.JobBeanWrapper.AutoSubmitAssignments
   - Schedule as needed (typically daily or a few times per day)

EVENTS:

The following events are posted for monitoring:
- asn.auto.submit.job - Job execution start/end
- asn.auto.submit.job.error - Job errors
- asn.auto.submit.submission - Individual submission auto-submitted
