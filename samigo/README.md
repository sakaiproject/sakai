Starting a README.md for Samigo

# Auto Submit
This feature forces the submission of saved assessments for students who have NO previous submissions (Please also refer to SAK-14474).

This is not a OOTB feature. To turn this on, add
samigo.autoSubmit.enabled=true to sakai.properties. Also, administrators need to set up a quartz job to auto-submit the assessments.

If the autoSubmit is enabled in sakai.properties, there will be a configurable option in the Submissions portion of Settings, deselected by default.

LOGIC:

- only submit saved assessments for students who have no previous submissions for that particular assessment
- if late submissions are allowed, submit and mark AUTO-SUBMIT where LATE would normally be (as long as attempt date is before due date)
- if late submissions are not allowed, skip over that check and submit anyway, and mark AUTO-SUBMIT where LATE would normally be (as long as attempt date is before due date)
- If attempt date is after due date, mark LATE as normal 


After this feature enabled, please run [auto_submit_oracle.sql or auto_submit_mysql.sql](https://github.com/sakaiproject/sakai/tree/master/samigo/docs/auto_submit) to make it show up in the pre-populated assessment types.
