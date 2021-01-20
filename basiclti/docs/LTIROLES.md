
How LTI Roles work in Sakai
===========================

Sakai has a set of roles that are very flexible and user-defined.  These roles
are generally unaware that IMS exists and so we need to define a mapping
between the Sakai and IMS roles.

The typical roles for Project sites are `maintain` and `access`.  For course sites
the roles are `Instructor`, `Student`, and `Teaching Assistant`.  But through the
Sakai site and realm templating feature a school can even throw these away
and start over.

The only thing that is universal across all realms is that one (or more) roles
is the "most powerful role" (a.k.a. the "Maintain role").  The maintain role
an edit things change site configuration, add and remove members etc.

Keeping it Simple
-----------------

The most basic mapping in Sakai is to mark any role that has "maintain" powers
in Sakai as the "Instructor" role in an LTI launch and all other roles are launched
with the "Learner" role.

This has worked well enough and makes it so that schools can have complete flexibility
with how they define roles.  Also LTI works on course and project sites just fine.

Administrators
--------------

Some tools want to know if a "super user" / "Sakai administrator" is doing the launch.
These users are more powerful than instructors so Sakai sends both the Instructor
and Administrator roles.  This way if a tool does not want to differentiate admins
they function like instructors.  In LTI 1.1, Sakai sends these roles:

Instructor,Administrator,urn:lti:sysrole:ims/lis/Administrator,urn:lti:instrole:ims/lis/Administrator

for superusers.

Role Mapping
------------

When you are installing an LTI tool into Sakai, you have an option for mapping particular
Sakai role to a particular IMS role using a set of mapping strings that specify a Sakai
role and the corresponding IMS role:

Teaching Assistant:Instructor,Librarian:Learner

These examples are using the LTI 1.1 "short form" for the IMS roles.

Along comes LTI Advantage
-------------------------

The biggest change for LTI Advantage was that the short form roles are strongly deprecated.
We are supposed to use the long-form URI based roles like:

    http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator
    http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper
    http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor
    http://purl.imsglobal.org/vocab/lis/v2/membership#Learner
    http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor
    http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator
    http://purl.imsglobal.org/vocab/lis/v2/institution/person#Faculty

The normal roles wer handled and Sakai sent the long roles based on the 'Maintain role'
in Sakai.

    http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor
    http://purl.imsglobal.org/vocab/lis/v2/membership#Learner

But Sakai's role mapping for LTI Advantage left something to be desired until it was
fixed in SAK-44866.

LTI Advantage Role Mapping Sakai 20.3 and Later
-----------------------------------------------

The 'Learner' and 'Instructor' short forms are expanded in launches and Names and Role
Provisioning Service (NRPS).  You can have a mapping like this on your tool:

Teaching Assistant:Instructor,Librarian:Learner

For LTI 1.1 launches this will map the role and sent the short versions of the roles on launch.
For LTI 1.3 launches the mapping will happen and then the role will be properly expanded.

But you can also specify the full length roles as well, and you can specify more than one IMS
role be the result of the role mapping.  To do this use a semicolon as the mapping separator
instead of comma (blanks and linebreaks added for readabliltiy):

    Instructor:Instructor,http://purl.imsglobal.org/vocab/lis/v2/institution/person#Faculty;
        Teaching Assistant:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper

You can mix the short forms and long form role strings.  The comma becomes part of the list
of roles in LTI 1.1 and is expanded into an array of roles in LTI Advantage.

You probably want to use the short-form roles like `Instructor` and `Learner` in your IMS role
list as shown above so your mapping works as you launch for LTI 1.1 or LTI 1.3.

