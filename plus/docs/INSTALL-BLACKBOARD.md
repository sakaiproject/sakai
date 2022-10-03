Blackboard
----------

Blackboard is planning on supporting IMS Dynamic Configuration, but until they do,
you need to do a bit of cutting and pasting of URLs between the systems.

To use this process, create a Tenant in Sakai Plus with a title and the following
information:

    Issuer: https://blackboard.com
    OIDC Auth: https://developer.blackboard.com/api/v1/gateway/oidcauth
    OIDC Token: https://developer.blackboard.com/api/v1/gateway/oauth2/jwttoken

Then go into the Sakai Plus Registration for the tenant and grab the "Manual Configuration"
URLs so you can create an LTI 1.3 clientID in the Blackboard Developer Portal.  Here
are some sample Sakai Plus URLs you will need for the Blackboard Developer portal:

    OIDC Login: https://dev1.sakaicloud.com/plus/sakai/oidc_login/654321
    OIDC Redirect: https://dev1.sakaicloud.com/plus/sakai/oidc_launch
    OIDC KeySet: https://dev1.sakaicloud.com/imsblis/lti13/keyset

Note that the `OIDC Login` value for Sakai Plus includes the Tenant ID for your
newly created Sakai Plus Tenant so it is unique for each Sakai Plus Tenant.  The
Redirect and Keyset values are the same for all tenants.

Use these Sakai Plus values in the Blackboard Developer portal to create an
LTI 1.3 integration.  The developer portal will give you a Client Id and
per-client KeySet URL similar to the following:

    OIDC KeySet: https://developer.blackboard.com/api/vl/management/applications/fe3ebd13-39a4-42c4-8b83-194f08e77f8a/jwks.json
    Client Id: fe3ebd13-39a4-42c4-8b83-194f08e77f8a

The value in the KeySet is the same as the Client Id.  You will need to update these values
in your Sakai Plus Tenant.

Once you place Sakai Plus into a Blackboard instance you will be given
a Deployment Id for that integration.

    Deployment Id: ea4e4459-2363-348e-bd38-048993689aa0

Once you have updated your Sakai Plus tenant with the `Client ID`,
`Keyset URL`, and `Deployment ID` your security arrangement should be
set up.

Once the Tenant has all the necessary security set up, there a number
of `target_link_uri` values that you can use.  You can send a Deep Link
Canvas
------

Canvas does not support IMS Dynamic Registration but has their own JSON-based
automatic Registration process that is supported by Sakai Plus.

    https://canvas.instructure.com/doc/api/file.lti_dev_key_config.html

To use this process, create a Tenant in Sakai Plus with a title and the following
information:

    Issuer: https://canvas.instructure.com
    OIDC Auth: https://canvas.instructure.com/api/lti/authorize_redirect
    OIDC KeySet: https://canvas.instructure.com/api/lti/security/jwks
    OIDC Token: https://canvas.instructure.com/login/oauth2/token

Make sure to check "Trust Email" - this needs to be set in the SakaiPlus Tenant
from the beginning.

This is a partially complete tenant, to get the remaining data, go into
the Tenant detail page and find the Canvas URL that looks like:

     https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=1234567

Use this URL in the Canvas Admin -> Developer Keys -> + Developer Key -> + LTI Key.
Set Key Name, Title, and your email address.  Then Choose "Enter URL" from the drop-down
and paste the URL for your Tenant in Sakai.  Make sure not to have any spaces in
the URL.  Then press "Save".  The go back in to edit the key and make sure the
key is marked as "Public" in "Additional Settings", changing and saving if necessary.

to create an integration.  This integration
creates a Client Id similar to the following:

    Client Id: 85730000000000147

Then to install Sakai Plus into a course or set of courses, you must use the
Client Id to add the tool and it then gives you a Deployment ID.
For a single course, go to Settings -> View App Configurations -> + App.  Then
choose "By Client ID" from the drop down and enter the ClientID from the previous
step and press "Submit".

    Deployment Id: 327:a17deed8f179b120bdd14743e67ca7916eaea622

Come back to Sakai Plus and update the Tenant to include both values and
your integration should start working.

For Canvas, sometimes it generates *lots* of Deployment Id values, so you
can make authorization of SakaiPlus based only on Client Id by leaving
the Deployment Id blank/empty in the Tenant.  SakaiPlus will track
Deployment Id on a per-context basis for AccessToken calls to the the LMSs.

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

