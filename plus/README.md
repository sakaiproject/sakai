Sakai Plus
==========

"Sakai Plus" is adding an LTI 1.3 / LTI Advantage "tool provider" to Sakai 23.  Sakai
already has an LTI 1.1 provider (i.e. you can launch a Sakai tool from another
LMS like Canvas, Moodle, Blackboard, etc.).

The LTI 1.1 provider will be kept separate from the LTI Advantage provider to allow
each to evolve to best meet their respective use cases.

Some Properties To Make It Work
-------------------------------

    # Needed for launches to work inside iframes
    sakai.cookieSameSite=none

    lti.role.mapping.Instructor=maintain
    lti.role.mapping.Student=access

    # Not enabled by default
    plus.provider.enabled=true
    plus.tools.allowed=sakai.resources:sakai.site


Installation Documentation
--------------------------

We have documentation on how to install Sakai Plus into a number of LMS systems:

* [Canvas](docs/INSTALL-CANVAS.md)
* [Brightspace](docs/INSTALL-BRIGHTSPACE.md)
* [Blackboard](docs/INSTALL-BLACKBOARD.md)
* [Moodle](docs/INSTALL-MOODLE.md)
* [Sakai](docs/INSTALL-SAKAI.md)

Installing Sakai Plus in to a Sakai installation is most often used for "loop back" QA testing.

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
