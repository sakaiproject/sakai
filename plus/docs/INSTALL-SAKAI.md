Installing SakaiPlus In Sakai (Inception)
=========================================

This documentation will show how to install and use SakaiPlus on the same server.  This is a
loop-back scenario - often used for testing.   You can do this with different servers if you like.
In this document, I will use a nightly server and loop back to itself.

The issuer for a Sakai system is the base URL of the system without a trailing slash:

For testing you might use and issuer like:

    https://trunk-mysql.nightly.sakaiproject.org

    http://localhost:8080    (for a local instance of Sakai)

In both cases do not include a trailing slash.

Adding A Tenant to Sakai
------------------------

Log in to the admin account then go to Administration Workspace -> Plus Admin.

For fresh installs, Plus Admin is automatically added to Administration Workspace.
If this is an upgraded server, you may need to add the Plus Admin (sakai.plus) tool to
the Administration Workspace using the Sites tool.

Add a tenant, give it a title and set the issuer, set "Trust Email", set "Verbose Logging", set
Allowed Tools to `sakai.resources:sakai.site`, and Registration Lock to 42.

Save the Tenant - it is "draft" because it is missing a lot of fields that will be set when
LTI Dynamic Provisioning runs.

If you don't set "trust email", each plus launch will log you out of the window you launched from.
This is only weird when we run both the main site and the plus
site on the same server (i.e. loop back testing).  If these are different servers and URLs,
the logout at launch will not be a problem.

Once the draft tenant is saved, view the tenant and grab the Dynamic Registration URL like:

    http://localhost:8080/plus/sakai/dynamic/8efcdee4-96c3-44bf-92fd-1d901ad593a3?unlock_token=42

Adding A SakaiPlus Placement to Sakai
-------------------------------------

Go into Administration Workspace -> External Tools -> LTI Advantage Auto Provision,
give the new tool  a title like "LMS End of Sakai Plus" and press "Auto-Provision".  Then press
"Use LTI Advantage Auto Configuration" and paste in the Dynamic Registration URL from the Tenant,
and run the process.  Make sure to enable:

* Send email
* Send name
* Give access to services
* Choose the various placements (Lessons, etc.)
* Tool Supports LTI 1.3
* Allow popup to be changed

before saving the external tool.

You can select both of the types of launches (and even the privacy placement) as long as the tool
url is something like "http../plus/sakai/" with no suffix like sakai.site or sakai.resources.

* The tool URL can receive an LTI launch
* The tool can receive a Content-Item or Deep-Link launch

Once the tool (or tools) are configured, save the tool.

Testing SakaiPlus
-----------------

We have a simple outline of how to [testing SakaiPlus from Sakai](TESTING.md).  It is a little weird
because you end up with two tabs - one tab from the "main site" and another tab for the "plus site".
The easiest way to keep them separate it has different tools in the sites or edit the Overview message.

