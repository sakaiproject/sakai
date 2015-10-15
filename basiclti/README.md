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
    lti2.support_email=
    lti2.service_owner.id=
    lti2.service_owner.owner_name=
    lti2.service_owner.description=
    lti2.service_owner.support_email=
    lti2.service_provider.id=
    lti2.service_provider.provider_name=
    lti2.service_provider.description=
    lti2.service_provider.support_email=
    lti2.product_family.product_code=
    lti2.product_family.vendor_code=
    lti2.product_family.vendor_name=
    lti2.product_family.vendor_description=
    lti2.product_family.vendor_website=
    lti2.product_family.vendor_contact=
    lti2.product_info.product_name=
    lti2.product_info.product_version=
    lti2.product_info.product_description=

