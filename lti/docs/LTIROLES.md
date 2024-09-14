
How LTI Roles work in Sakai
===========================

The documentation for LTI Roles is at:

[LTI Role Vocabularies](https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies)

Sakai has a set of roles that are very flexible and user-defined.  These roles
are generally unaware that LTI exists and so we need to define a mapping
between the Sakai and LTI roles.

The typical roles for Project sites are `maintain` and `access`.  For course sites
the roles are `Instructor`, `Student`, and `Teaching Assistant`.  But through the
Sakai site and realm templating feature a school can even throw these away
and start over.

There is also a realm called `!site.template.lti` that has a number of additional roles
beyond a Sakai Course site that match the LTI roles.  This template is used to create
sites in the LTI providers in Sakai.  Sinces these sites are coming from "LTI" sources
it is good to capture these incoming LTI roles in a realm so a school can customize the
security functions.

The only thing that is universal across all realms is that one (or more) roles
is the "most powerful role" (a.k.a. the "Maintain role").  The maintain role
can edit things, change site configuration, add and remove members, etc.

Keeping it Simple
-----------------

The fall back mapping in Sakai is to mark any role that has "site.upd" powers
in Sakai as the "Instructor" role in an LTI launch and all other roles are launched
with the "Learner" role.

Using the fall back mapping has worked well enough for a large range of outbound
LTI launches as long as the tools have a simple Instructor / Learner role split.

Administrators
--------------

Some tools want to know if a "super user" / "Sakai administrator" is doing the launch.
These users are more powerful than instructors so Sakai sends both the Instructor
and Administrator roles.  This way if a tool does not want to differentiate admins
they function like instructors.  In LTI 1.1, Sakai sends both the 'Instructor' and
'Administrator roles so tools that want to identify an admin - but if a tool
does not check, admins are treated as Instructors.

Advanced Role Mapping
-------------------------------

Sakai has flexible and rich role mapping for both inbound and outbound LTI Requests.
For the rest of this document "outbound" means LTI launches to external LTI tools
and the roles that Sakai provides via its Names and Roles Provisioning Service.
When we reference "inbound" roles, we are referring to roles that are provided to
Sakai when it is acting as an LTI provider.

Outbound LTI Role Mapping
-------------------------

In order to reduce the effort to map internal Sakai roles to LTI Roles during Launch
and the Names and Roles Service, Sakai 23 introduces default role mapping that should
cover most common cases.

The full default mappings are documented in
[SakaiBLTIUtil.java](https://github.com/sakaiproject/sakai/blob/master/basiclti/basiclti-common/src/java/org/sakaiproject/basiclti/util/SakaiBLTIUtil.java)
But here is a copy (may be out of date) so that we can talk about them.

    admin:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,
            http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,
            Administrator,
            http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator,
            http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator;
    access:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;
    maintain:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;
    Instructor:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;
    Student:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;
    Teaching Assistant:TeachingAssistant,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant;
    Learner:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;
    Mentor:Mentor,http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor;
    ContentDeveloper:ContentDeveloper,http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper;

Newlines added to the `admin` entry for readibility - they are all one line, comma separated.

These roles come from all three out-of-the-box realm templates (project/site, course, lti) so
this outbound mapping can be used regardless of which site type the launch is coming
from.

Note that `Teaching Assistant` is a little weird because it maps to sub-type of `Instructor`.
Also in Sakai, `Teaching Assistant` has a blank and in LTI `TeachingAssistant` does
not have a blank.  The mappings take great care to make sure this works correctly.  Sakai's
use of `Teaching Assistant` predates LTI by about a decade so we just leave it alone in Sakai
and map it back and forth :)

You can add Sakai-side default mappings or override any or all of the mappings above through
(you guessed it) a Sakai property:

    lti.outbound.role.map=Sakaiger:Mascot,http://purl.imsglobal.org/vocab/lis/v2/membership#Mascot;Friend...

These mappings in `sakai.properties` are one *very long* line with semicolon separator.

The precedence for applying outbound tool mappings from highest to lowest is: (a) The per-tool
outbound mapping, (b) the role mapping from the `lti.outbound.role.map` and (c) the default
role mapping in `SakaiBLTIUtil.java`.

In general, it should be pretty rare when you need to override the default mapping.  If new use
cases or new roles arise in LTI and see common use, we can add them to the default mapping
over time.   The overrides allow for a quick response when it might take a little while before
we can fix Sakai and you get an upgraded version.

Per-Tool Outbound Role Mapping
------------------------------

There are special situations where a particular tool needs some very particular role
mapping or your Sakai sites have additional roles beyond the out-of-the-box roles.
When you are installing an LTI tool into Sakai, you have an option for mapping a particular
Sakai role to a particular LTI role using a set of mapping strings, like this:

Teaching Assistant:Instructor;Librarian:Learner

These examples are using the LTI 1.1 "short form" for the LTI roles.
But you can also specify the full length roles as well, and you can specify more than one LTI
role be the result of the role mapping.  (blanks and linebreaks added for readablility):

    Instructor:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Faculty;
        Teaching Assistant:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper

Inbound LTI Role Mapping
------------------------

When an inbound launch comes to an LTI Provider, it must create a new site and associate a
realm with the site and then based on the incoming LTI role, choose a role in the new site
for the incoming user.  Since there are a number of quite different realm templates `site.template`,
`!site.template.course`, and `!site.template.lti` - each external LTI role is mapped
to a priority ordered list of Sakai roles from the site where the user is being placed.

These roles are checked in order and the first matching role is chosen.  The priority order
is from least likely (site.template.lti) and most specific to most likely and least
specfic (site.template).

    http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator=Instructor,maintain
    http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper=ContentDeveloper,Instructor,maintain
    http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant=Teaching Assistant,Instructor,maintain
    http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor=Instructor,maintain
    http://purl.imsglobal.org/vocab/lis/v2/membership#Learner=Learner,Student,access
    http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor=Mentor,Teaching Assistant,Learner,Student,access

    http://purl.imsglobal.org/vocab/lis/v2/membership#Manager=Learner,Student,access
    http://purl.imsglobal.org/vocab/lis/v2/membership#Member=Learner,Student,access
    http://purl.imsglobal.org/vocab/lis/v2/membership#Officer=Learner,Student,access

Note that `TeachingAssistant` is a little weird because it is a sub-type of `Instructor` and the
use of a blank within Sakai and no blank in LTI.

The actual default list is stored in
[SakaiBLTIUtil.java](https://github.com/sakaiproject/sakai/blob/master/basiclti/basiclti-common/src/java/org/sakaiproject/basiclti/util/SakaiBLTIUtil.java)

You can add new default mappings or override any or all of the mappings above through
Sakai property:

    lti.inbound.role.map=http://purl.imsglobal.org/vocab/lis/v2/membership#Mascot=Learner,Student,access;http://...

These mappings in `sakai.properties` are one *very long* line with semicolon separator.

Legacy Role Mapping
-------------------

As the LTI versions evolved over more than a decade, the role strings evolved as well.  Some
old role strings ended up in our code and others ended up Sakai production databases in the definition
of LTI Tools.  In order to modernize these older role strings Sakai provides a legacy role map.

    Learner=http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;
    learner=http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;
    Instructor=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;
    instructor=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;
    TeachingAssistant=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant;
    Mentor=http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor;
    ContentDeveloper=http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper;
    Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;
    urn:lti:sysrole:ims/lis/Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;
    urn:lti:instrole:ims/lis/Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;

This mapping is applied to outbound roles in the LTI tool definitions (pretty rare).  This
mapping is also applied to incoming LTI roles from the various LMS's.

You can add new default mappings or override any or all of the mappings above through
Sakai property:

    lti.legacy.role.map=https://otherlms.com/role/Mascot=http://purl.imsglobal.org/vocab/lis/v2/membership#Sakaiger;https://...

These mappings in `sakai.properties` are one *very long* line with semicolon separator.

Evolution Over Time
-------------------

With a rich set of defaults and the ability to quickly override the defaults without needing to change code,
we hopefully have made it so as new use cases arise, Sakai schools can quickly react to new situations.
If there are use cases or roles that will be generally encountered, we are happy to adjust Sakai to address
these use cases out-of-the-box.

Since Sakai is 100% open source, you could even improve Sakai yourself! :)  Simply go to:

Source code: [SakaiBLTIUtil.java](https://github.com/sakaiproject/sakai/blob/master/basiclti/basiclti-common/src/java/org/sakaiproject/basiclti/util/SakaiBLTIUtil.java)

If you have a github account and edit the file, you can send a "Pull Request" and become a Sakai
contributor.



