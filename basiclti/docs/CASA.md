
Sakai Support for IMS CASA
==========================

Note: As of the writing of this documentation IMS has not finalized the
CASA specification and so this code should be considered experimental
until it has passed certification.

CASA is an app store protocol.   Sakai's LTI Provider can export its tools
to a CASA app store.  CASA Registration URLs are simple, public URLs that
produce a JSON list of the apps available on a server.

    https://trunk-mysql.nightly.sakaiproject.org/imsblti/provider/casa.json

Configuration in sakai.properties
---------------------------------

This only will be turned on if the provider is enabled and there are tools to provide
(lines broken below for formatting)

    basiclti.provider.enabled=true

    basiclti.provider.allowedtools=sakai.announcements:sakai.singleuser:
    sakai.assignment.grades:sakai.dropbox:sakai.forums:
    sakai.gradebook.tool:sakai.podcasts:sakai.poll:sakai.resources:
    sakai.schedule:sakai.samigo:sakai.rwiki

    casa.provider.enabled=true

The casa.provider.enabled value defaults to the same as basiclti.provider.enabled.  If
you turn on basiclti.provider.enabled and want to turn off CASA simply use:

    casa.provider.enabled=false

The contact information in the CASA JSON file is taken from the LTI2 configuration variables:

* [Configuring LTI 2.0](LTI2.md)

So it is a good thing to populate these variables in your server.

JIRA References
---------------

https://jira.sakaiproject.org/browse/SAK-30372
