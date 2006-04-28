ENABLING ROSTER

By default, Roster is included in the release but hidden from the list of available tools in the Worksite Setup. Roster will be available to adminsitrators using the "Sites" adminsitration interface as one of the tools Roster (sakai.site.roster). You have one of two choices in terms of enabling Roster.

If you want to let a few selected users use Roster to test it out, you can have the adminstrator selectively add it by hand to the sites for those users who you want to use Roster.

If you want to make it so that any user can add Roster to their site using WorkSite Setup, edit webapps/sakai-roster-tool/tools/sakai.site.roster.xml in a deployed instance, or roster/roster-app/src/webapp/tools/sakai.site.roster.xml in a source tree. Uncomment the <category ...> entries in the <tool> section.


KNOWN ISSUES

There is no standard way to provision the pictures in the Roster tool in this release, but there is sample code you can take as a model for creating your own photo load job, and you can find it on subversion:

https://source.sakaiproject.org/svn/trunk/oncourse/src/scheduler/scheduler-component-shared/src/java/org/sakaiproject/component/app/scheduler/jobs/UpdateProfilePhotoJob.java

If you would like further information on how to provision the pictures, contact Lance Speelmon (lance@indiana.edu).