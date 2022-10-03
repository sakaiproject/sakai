Sakai
-----

For 21 and later versions of Sakai you can use IMS Dynamic Configuration.  Create a Tenant with a title
(often SakaiPlus), issuer, and registration unlock code.   Then go to the Plus -> Tenant detail
page and find the IMS Dynamic Registration url.

Sometimes this is truly two independent servers but more commonly we are just setting this
up in a "loop back" configuration for testing.

The issuer for a Sakai system is the base URL of the system without a trailing slash:

    https://sakai.school.edu

For testing you might use and issuer like:

    https://localhost:8080

In both cases do not include a trailing slash.

If you want to test both deep link and site launch make sure to add at least one tool plus
'sakai.site' to the allowed tools (i.e. like sakai.resources:sakai.site)

Make sure to "trust email" or launches in the "loop back" use case site will log you out of the
browser you are launching from.  This is only weird when we run both the main site and the plus
site on the same server (i.e. loop back testing).  If these are different servers and URLs,
the logout at launch will not be a problem.

For Dynamic Registration to work, Sakai Plus demands that the issuer in Sakai Plus
match the issuer provided by the LMS during the IMS Dynamic Configuration process.
The registration lock is single use and must be reset in Sakai Plus to re-run the Dynamic
Registration process.

Once you have the Dynamic Registration URL like:

    http://localhost:8080/plus/sakai/dynamic/8efcdee4-96c3-44bf-92fd-1d901ad593a3?unlock_token=42

Go into Administration Workspace -> External Tools -> LTI Advantage Auto Provision,
give the new tool  a title like "LMS End of Sakai Plus" and press "Auto-Provision".  Then press
"Use LTI Advantage Auto Configuration" and paste in the Dynamic Registration URL, and run
the process.  Make sure to enable:

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

You can place the SakaiPlus site launch in the left navigation of a site using
Site Info -> Manage Tools -> External Tools (near the bottom of the tool list)

