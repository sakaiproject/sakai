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

Of course nightly servers are reset every 24 hours so anything that you set up will vanish - and if
you mess anything up - your mistakes will convienently disappear.  If you want access to a server
for testing that will not be reset each evening, send an email to `plus at sakailms.org`.

Enabling SakaiPlus in sakai.properties
--------------------------------------

    # Needed for launches to work inside iframes
    sakai.cookieSameSite=none

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

* You can also send a `LtiDataPrivacyLaunchRequest`, SakaiPlus checks the following properties (in order)
and redirects the user to the correct URL:

        plus.server.policy.uri
        plus.server.tos.uri

At a high level some effort has been made to make it so that the `target_link_uri` is the base launch point

   https://trunk-mysql.nightly.sakaiproject.org/plus/sakai

And the launch `message_type` determines that happens - except of course for launches directly to a single
tool.  This reflects the (good) general trend in LTI Advantage to use message type rather than URL patterns
for different kinds of launches.

It might be necessary to install SakaiPlus more than once so that it shows up in all the right placements
in a particular LMS.

SakaiPlus Tenants
-----------------

A SakaiPlus server can support many "tenants".  Each Learning System that you are plugging SakaiPlus into
should have its own tenant.  In SakaiPlus, all data within a tenant is isolated (each tenant is a 'silo').
This way you can have a multi-tenant SakaiPlus server to serve many different learning systems.  However
it is also a quite typical use case to have one Enterprise LMS - say Canvas and one SakaiPlus server
for the same school and to have a single Tenant entry in SakaiPlus for the Canvas system.

You can create a "draft" tenant with a Title and Issuer and optionally a Registration Lock.  Once you have created
a draft tenant, you can view the tenant to either start the LTI Dynamic Registration process or provide
tool configuration to your calling learning system.

You can view the documentation for LTI Dynamic Registration at:

* [Dynamic Registration](https://www.imsglobal.org/spec/lti-dr/v1p0)

Each tenant in SakaiPlus has a set of data:

*Issuer*

Issuer is different for each LMS, but it is usually a URL like "https://plus.sakailms.org" - with no trailing slash.
Sometimes this will be the domain where the LMS is hosted. For some cloud-hosted providers, they use one
issuer across all customers. This field is required.

*Client ID*

These are provided by the launching Learning system as part of tool registration.  If the Learning system
supports LTI Dynamic Registration it will automatically populate this field.

*Deployment ID*

`Deployment ID` can be tricky.  For some systems it is the same for an entire system and is provided
as part of Dynamic Registration.  For other systems a new `Deployment ID` is generated by each course.
You can set the `Deployment ID` to `*` if you can accept any `Deployment ID` for a particular
`Client ID`.  See the per-LMS installation instructions above for details.

*Allowed Tools*

This field is a colon-separated list of Sakai tool ids like "sakai.resources". There is a special
"sakai.site" tool id which controls the availability of the "entire site" launch". A simple default
for this is "sakai.site" or "sakai.site:sakai.resources: ...". This field is required.

*New Window Tools*

This field is a colon-separated list of Sakai tool ids which will be forced to always open in a
 new window. The "sakai.site" is always launched in a new window. This is typically left blank unless
it is known that a particular tool just does not work well in an iframe.  Or perhaps you are setting
up a single tool server and want it to always be in a new window.

*Trust Email*

If the Learning system that is calling SakaiPlus for this tenant sends email, you *should* trust the
email address to avoid creating multiple user records for each user in each site. If you mark this tenant
as 'trust email', and the calling system provides the email address of the user, multiple launches from
multiple contexts will all be linked to the same user within this Tenant in SakaiPlus.

*Site Template*

This specifies an existing site like `!plussite` which will be copied to make a new site when SakaiPlus
receives an incoming site. This template site determines the default tools that are added to the new
SakaiPlus site.  The default is '!plussite' unless it is changed using the `plus.new.site.template` Sakai property.


*Realm Template*

This specifies an existing realm like `!site.template.lti` which will be copied to set the roles and
permissions used when creating a new site when SakaiPlus receives an incoming site. The default is
`!site.template.lti` unless it is changed using the `plus.new.site.realm` Sakai property.

*Inbound Role Map*

This field allows for overriding the default mapping from incoming LTI roles to Sakai roles.  See
this documentation for detail on how role mapping works and the format for role mapping entries.

* [Sakai to LTI Role Mapping](../basiclti/docs/LTIROLES.md)

*Registration Lock*

You set this field to "unlock" LTI Dynamic Registration for this tenant. It should only be set while
performing dynamic registration and should be cleared after dynamic registration is complete.
If the launching system does not support dynamic registration you will set these manually.

The `LMS Keyset Url`, `LMS Authorization Url`, `LMS Token Url`, and `LMS Token Audience` fields are
set up as part of tool registration with the calling learning system. If the system supports
LTI Dynamic Registration these values should be set automatically.  The `LMS Token Audience`
is left blank for most systems except for Desire2Learn.


