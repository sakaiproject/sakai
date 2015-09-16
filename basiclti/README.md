Sakai's Support for IMS LTI
---------------------------

This folder holds our code for IMS Learning Tools Interoperability.   This area has many different contributors.  

There are several internal documents that you might find useful ranging from test plans to developer/configuration
documentation.  These documents are stored here in github:

* [Sakai LTI Documentation](https://github.com/sakaiproject/sakai/tree/master/basiclti/basiclti-docs/resources/docs)

Sakai has a unit test that we keep up-to-date with the latest LTi specifications.  It functions as both Consumer and Provider and exercises both standard services and Sakai's particular extensions.  I make this tool available online at

* https://online.dr-chuck.com/sakai-api-test

IMS LTI 2.0 Variables
---------------------

If you are going to use IMS LTI 2.0 in Sakai 10.x and later, you will need to define a number
of variables in your *sakai.properties* as defined in the LTI 2.0 documentation:

    http://www.imsglobal.org/lti/ltiv2p0/ltiIMGv2p0.html

lti2.guid=
lti2.support\_email=
lti2.service\_owner.id=
lti2.service\_owner.owner\_name=
lti2.service\_owner.description=
lti2.service\_owner.support\_email=
lti2.service\_provider.id=
lti2.service\_provider.provider\_name=
lti2.service\_provider.description=
lti2.service\_provider.support\_email=
lti2.product\_family.product\_code=
lti2.product\_family.vendor\_code=
lti2.product\_family.vendor\_name=
lti2.product\_family.vendor\_description=
lti2.product\_family.vendor\_website=
lti2.product\_family.vendor\_contact=
lti2.product\_info.product\_name=
lti2.product\_info.product\_version=
lti2.product\_info.product\_description=

