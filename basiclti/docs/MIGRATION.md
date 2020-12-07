
LTI 1.1 to LTI Advantage Migration
==================================

The LTI Advantage specification defined a pretty elegant way to seamlessly transition LTI 1.1
links, courses, users, and their data to LTI Advantage.

https://www.imsglobal.org/spec/lti/v1p3/migr

Sadly very few LMS's have chosen to implement this.  As of December 2020, Sakai is the only LMS
that fully supports the migration and Tsugi is probably the only tools that fully supports
the migration claim.

We can describe how it works with Sakai and Tsugi and hope that it serves as an example to inspire
other LMS's and tools to support this specification.

The starting situation is that you have an LTI 1.1 key in Tsugi and have been using it in Sakai
for a while with lots of courses, users, and data.

You should *not* make a new key in Tsugi.  Look in the details for your Tsugi key.  There will
be an autoconfiguration URL that will add LTI Advantage to the Tsugi key.

Go into Sakai -> Admininstration Workspace -> External Tools and *edit* the existing
LTI 1.1 entry.  It will have a button titled "LTI Advantage Auto Provision" - press 
it and enter the Tsugi URL and follow the normal registration process.

It will convert that tool to LTI Advantage, and when launching users include the migration
claim.  Tsugi will see the migration claim and use the existing user account rather than
making a new account.

Actually the Sakai/Tsugi migration code is so robust that you can switch back and forth
from LTI 1.1 to LTI Advantage simply by Editing the tool in the Administration Workspace.








