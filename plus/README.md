Sakai Plus
==========

"Sakai Plus" is adding an LTI 1.3 / LTI Advantage "tool provider" to Sakai 23.  Sakai
already has an LTI 1.1 provider (i.e. you can launch a Sakai tool from another
LMS like Canvas, Moodle, Blackboard, etc.).

The LTI 1.1 provider will be kept separate from the LTI Advantage provider to allow
each to evolve to best meet their respective use cases.

Some Properties To Make It Work
===============================

    # Needed for launches to work inside iframes
    sakai.cookieSameSite=none

    lti.role.mapping.Instructor=maintain
    lti.role.mapping.Student=access

    # Not enabled by default
    plus.provider.enabled=true
    plus.tools.allowed=sakai.resources:sakai.site

Overall Strategy
----------------

There are several use cases that SakaiPlus is designed to support - these can be used in combination
depending on the capability of the upstream LMS.

* A simple left navigation launch from the enterprise LMS to "SakaiPlus" to make a Sakai site, enroll all
the students in the site, and launch students into the site. This is either a `LtiResourceLinkRequest` to
the endpoint base or by adding `sakai.site` to the endpoint.  If you send a `LtiContextLaunchRequest` (emerging spec)
to the base endpoint regardless of tool id, it is treated as a `sakai.site` request.

        https://dev1.sakaicloud.com/plus/sakai/
        https://dev1.sakaicloud.com/plus/sakai/sakai.site

* A `LtiDeepLinkingRequest` sent to the the base endpoint or the `sakai.deeplink` endpoint.   This will
check the `plus.allowedtools` list and go through a deep linking flow to choose and install an
individual Sakai tool.  You can install a link to `sakai.site` through deep linking if `sakai.site`
is included in the `plus.allowedtools` property

        https://dev1.sakaicloud.com/plus/sakai/
        https://dev1.sakaicloud.com/plus/sakai/sakai.deeplink

* You can launch to a single Sakai tool without any portal mark-up by hard-coding the `target_uri` to
include the Sakai tool id like `sakai.resources` and sending that end point an `LtiResourceLinkRequest`.

        https://dev1.sakaicloud.com/plus/sakai/sakai.resources

If you have exactly one tool enabled in the allowed tools list (i.e. like sakai.conversations) and do not
even have sakai.site enabled, a `LtiResourceLinkRequest` sent to the base URL will be sent to that tool.
This feature would allow you to put up a server like conversations.sakaicloud.org and serve one and
only one tool.

* You can also send a `DataPrivacyLaunchRequest`, SakaiPlus checks the following properties (in order)
and redirects the user to the correct URL:

        plus.server.policy.uri
        plus.server.tos.uri

At a high level some effort has been made to make it so that the `target_link_uri` is the base launch point

   https://dev1.sakaicloud.com/plus/sakai/

And the launch `message_type` determines that happens - except of course for launches directly to a single
tool.  This reflects the (good) general trend in LTI Advantage to use message type rather than URL patterns
for different kinds of launches.

It might be necessary to install SakaiPlus more than once so that it shows up in all the right placements
in a particular LMS.

D2L BrightSpace
---------------

BrightSpace supports IMS Dynamic Configuration.  Create a Tenant with a title,
issuer, and registration unlock code.   Then go to the SakaiPlus Tenant detail page and find the IMS
Dynamic configuration URL and use that in the auto-provisioning screen of BrightSpace.

The issuer for a D2L system is the base URL of the system without a trailing slash:

    https://school.brightspacedemo.com

While Dynamic Registration is the easiest approach, you can create a draft Tenant
in Sakai Plus, then paste all the Sakai Plus URLs into Brightspace manually, save the tool
in Brightspace, then get copy the Brightspace URLs and edit your Sakai Plus
Tenant.  Here are what typical values look like for Brightspace:

    Client ID:           04a7d304-477d-401a-b701-5a58f54772d6
    Deployment ID:       7862b2ce-79a0-77da-b2dd-7c77c4bb6e39
    LMS Authorization:   https://school.brightspacedemo.com/d2l/lti/authenticate
    LMS KeySet:          https://school.brightspacedemo.com/d2l/.well-known/jwks
    LMS Token:           https://auth.brightspace.com/core/connect/token
    LMS Token Audience:  https://api.brightspace.com/auth/token

Some of the values are local to the Brightspace school's URL and others
are global for all schools.

The basic outline in Brightspace is to

* Install an LTI Advantage Tool
* Create a Deployment for the tool
* Create a Link for the tool (this is what most LMS's call "Placement")

Make sure to enable the security settings for `Org Unit Information`,
`User Information`,`Link Information`.  If you do not send `Org Unit Information`
Sakai Pus will not know anything about the course it is being launched from.
And sending email is important because otherwise all the SakaiPlus accounts will
use the "subject" as the logical key for user accounts.   SakaiPlus can function
without email - but it makes it a lot harder to re-connect user accounts later.

For Dynamic Registration to work, Sakai Plus demands that the issuer in Sakai Plus
match the issuer provided by the LMS during the IMS Dynamic Configuration process.
The registration lock is single use and must be reset in Sakai Plus to re-run the Dynamic
Registration process.

Here are some helpful URLs:

    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/intro_to_LTI.htm
    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/LTI_register_external_learning_tool.htm
    https://success.vitalsource.com/hc/en-gb/articles/360052454313-Brightspace-D2L-LTI-1-3-Tool-Setup
    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/deploy_external_learning_tool_for_LTI_A.htm

Moodle
------

For later versions of Moodle you can use IMS Dynamic Configuration.  Create a Tenant with a title,
issuer, and registration unlock code.   Then go to the Tenant detail page and find the IMS
Dynamic Registration URL and use that in the auto-provisioning screen of Moodle.

The issuer for a Moodle system is the base URL of the system without a trailing slash:

    https://moodle.school.edu

For testing you might use and issuer like:

    https://localhost:8888/moodle

In both cases do not include a trailing slash.

For Dynamic Registration to work, Sakai Plus demands that the issuer in Sakai Plus
match the issuer provided by the LMS during the IMS Dynamic Configuration process.
The registration lock is single use and must be reset in Sakai Plus to re-run the Dynamic
Registration process.

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
request to a URL of the form:

    Deep Link: https://dev1.sakaicloud.com/plus/sakai/deep.link

To launch an entire Sakai site, you can send an LTI Resource Link
request to a target URI of:

    Sakai Site: https://dev1.sakaicloud.com/plus/sakai/sakai.site

To launch a single Sakai tool, you can send an LTI Resource Link
request to a target URI of:

    Sakai Site: https://dev1.sakaicloud.com/plus/sakai/sakai.resources

Where `sakai.resources` is any registered Sakai tool that is in the
`Allowed Tools` field in the Sakai Plus tenant:

    Allowed Tools: sakai.site:sakai.samigo:sakai.lessonbuildertool:sakai.conversations:sakai.postem

Of course if you use Deep Linking - Sakai Plus will construct the
`target_link_uri` values for you.   And if you use Dynamic Provisioning,
Sakai Plus will set up Deep Linking in your LMS with no cutting and pasting.

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





