
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
    sakai.assignment.grades:blogger:sakai.dropbox:sakai.mailbox:sakai.forums:
    sakai.gradebook.tool:sakai.podcasts:sakai.poll:sakai.resources:
    sakai.schedule:sakai.samigo:sakai.rwiki

    casa.provider=true

The casa.provider value defaults to the same as basiclti.provider.enabled.  If
you turn on basiclti.provider.enabled and want to turn off CASA simply use:

    casa.provider=false


JIRA References
---------------

https://jira.sakaiproject.org/browse/SAK-30372
