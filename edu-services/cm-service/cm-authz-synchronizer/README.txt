WHAT'S HERE

This directory contains source code which can be used to synchronize course
site memberships with Course Management enrollments and roles, either for a
given academic session (also known as term), or for all terms that are
considered current.

For background, see: http://jira.sakaiproject.org/jira/browse/SAK-14259


WARNING

This project relies on undocumented aspects of the "site-manage" project
(which includes the standard Site Info and Worksite Setup applications).
It therefore should be viewed as unsupported sample code which may
not work if you use some other method of creating course sites.


SYNCHRONIZING COURSE SITE MEMBERSHIPS THROUGH THE JOB SCHEDULER

1. Build and deploy the "cm-authz-synchronizer-job" component from
this directory:

  mvn clean install sakai:deploy

2. Log into Sakai as an administrator and go to the "Administration Workspace".

3. Select the "Job Scheduler" tool and go to the "Jobs" and then the "New Job"
page.

4. From the "Type" menu, select "Synchronize course site memberships for all
current terms, or specify Job Name as 'term=THE_TERM_EID'".

5. Use the "Job Name" field to specify which terms to synchronize.

To synchronize all course sites associated with a particular term, end
the job name with "term=" followed by the academic session EID for that
term. For example:

  Sync Rosters term=Spring 2009

To synchronize all course sites associated with any term that the Course
Management service reports as "current", use any job name that does
*not* include the string "term=". For example:

  Sync Current Rosters
