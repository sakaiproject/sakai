Sakai Plus
==========

"Sakai Plus" is an LTI 1.3 / LTI Advantage "tool provider" built in to Sakai 23.

SakaiPlus allows a school to use Sakai as a "secondary LMS" without requiring you to
switch the entire campus to Sakai.  Once installed, the instructors and students have
a link in their Enterprise LMS to launch SakaiPlus in a new tab.  In that new tab,
they simply teach using any and all Sakai features. In the background, SakaiPlus keeps
the rosters and grade books synchronized.

The teachers and students continue to use the Enterprise LMS to see grades, get notifications,
view their calendar and due dates.  This means that essential student records are stored
in the Enterprise LMS as the system of record. Teachers can use as much or as little of
SakaiPlus as they like in their teaching.  Perhaps they want to use a few of Sakai's
market-leading tools like Lessons, Tests and Quizzes, or Conversations.  Or perhaps they
are teaching a course where there are enhanced concerns around the control and possession
of private student activity data - so they teach the entire course in SakaiPlus on a
server owned by the school.  Or perhaps a few faculty members want to use Sakai and
make suggestions on how to improve the product and be part of an open source community
that will listen to, care about, and implement their suggestions in a future version.

SakaiPlus has many hosting options.   SakaiPlus is just a feature in every instance
of Sakai 23 waiting to be enabled, configured and used.  SakaiPlus is multi-tenant
and efficient so it is possible to provide a free or low cost cloud service that
would allow a school to test SakaiPlus or run a small pilot.   You can get a small
single-tenant vendor supported cloud instance of SakaiPlus from a Sakai Commercial
Affiliate at about the cost of an LTI tool.   Sakai can be self-hosted by a school
using an Amazon EC2 Instance, an Aurora Serverless Database, and Amazon's Elastic
File System (EFS).   If a school is a little uncertain about their in-house cloud
hosting skills, Sakai Commercial Affiliates can assist a school in setting up,
monitoring and upgrading their SakaiPlus instance.

Installation Documentation
--------------------------

We have documentation on how to install Sakai Plus into a number of LMS systems:

* [Canvas](docs/INSTALL-CANVAS.md)
* [Brightspace](docs/INSTALL-BRIGHTSPACE.md)
* [Blackboard](docs/INSTALL-BLACKBOARD.md)
* [Moodle](docs/INSTALL-MOODLE.md)
* [Sakai](docs/INSTALL-SAKAI.md)

Installing Sakai Plus in to a Sakai installation is most often used for "loop back" QA testing.

Test Servers
------------

You can test a SakaiPlus install using one of our nightly servers as long as it is
running at least Sakai 23.

    https://trunk-mysql.nightly.sakaiproject.org

Of course this server is reset every 24 hours so anything that you set up will vanish - and if
you messJ anything up - your mistakes will convienently disappear.  If you want access to a server
for testing that will not be reset each evening, send an email to `plus at sakailms.org`.

Enabling SakaiPlus in sakai.properties
--------------------------------------

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
depending on the capability of the LMS that will launch SakaiPlus.

* A simple left navigation launch from the enterprise LMS to "SakaiPlus" to make a Sakai site, enroll all
the students in the site, and launch students into the site. This is either a `LtiResourceLinkRequest` to
the endpoint base or by adding `sakai.site` to the endpoint.  If you send a `LtiContextLaunchRequest` (emerging spec)
to the base endpoint regardless of tool id, it is treated as a `sakai.site` request.

        https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/
        https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/sakai.site

* A `LtiDeepLinkingRequest` sent to the the base endpoint or the `sakai.deeplink` endpoint.   This will
check the `plus.allowedtools` list and go through a deep linking flow to choose and install an
individual Sakai tool.  You can install a link to `sakai.site` through deep linking if `sakai.site`
is included in the `plus.allowedtools` property

        https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/
        https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/sakai.deeplink

* You can launch to a single Sakai tool without any portal mark-up by hard-coding the `target_uri` to
include the Sakai tool id like `sakai.resources` and sending that end point an `LtiResourceLinkRequest`.

        https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/sakai.resources

If you have exactly one tool enabled in the allowed tools list (i.e. like sakai.conversations) and do not
even have sakai.site enabled, a `LtiResourceLinkRequest` sent to the base URL will be sent to that tool.
This feature would allow you to put up a server like conversations.sakaicloud.org and serve one and
only one tool.

* You can also send a `DataPrivacyLaunchRequest`, SakaiPlus checks the following properties (in order)
and redirects the user to the correct URL:

        plus.server.policy.uri
        plus.server.tos.uri

At a high level some effort has been made to make it so that the `target_link_uri` is the base launch point

   https://trunk-mysql.nightly.sakaiproject.org/plus/sakai/

And the launch `message_type` determines that happens - except of course for launches directly to a single
tool.  This reflects the (good) general trend in LTI Advantage to use message type rather than URL patterns
for different kinds of launches.

It might be necessary to install SakaiPlus more than once so that it shows up in all the right placements
in a particular LMS.

