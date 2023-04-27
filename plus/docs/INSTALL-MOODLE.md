Moodle
------

For recent versions of Moodle you can use LTI Dynamic Registration.

In Administration Workspace -> Plus Admin, create a Tenant with a title,
issuer, and registration unlock code.   Then go to the Tenant detail page and find the LTI
Dynamic Registration URL and use that in the auto-provisioning screen of Moodle.

The issuer for a Moodle system is the base URL of the system without a trailing slash:

    https://moodle.school.edu

For testing you might use and issuer like:

    http://localhost:8888/moodle

In both cases do not include a trailing slash.

For Dynamic Registration to work, Sakai Plus demands that the issuer in Sakai Plus
match the issuer provided by the LMS during the LTI Dynamic Registration process.
The registration lock is single use and must be reset in Sakai Plus to re-run the Dynamic
Registration process.

