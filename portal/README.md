Sakai Portal
============

This is the code that controls the outer bits of the Sakai user interface.

The top level folders in this directory are as follows:

* portal-api (shared jar) Defines the Portal APIs - contains APIs for things that can be
plugged into Portal like SiteNeighbourhoodService.java, StyleAbleProvider.java
and other things that can be wired into more than one implementation.  

* portal-impl (jar) Contains SkinnableCharonPortal.java and all of the Handlers.

* portal-charon (war) Builds and deploys (portal.war) - primary dependency is
portal-impl

* portal-service-impl (component) contains implementations for some of the things that are 
plugged in to portal like SiteNeighbourhoodServiceImpl.java

* portal-render-engine-impl (war) portal-render.war - contains morpheus templates, etc. 
There is no Java code here - only templates.

* portal-render-api Contains RenderResult.java APIs (4 APIs total)

* portal-render-impl (component) Contains things like IFrameToolRenderService.java, 
PortletToolRenderService.java, and portlet support code like PortletStateEncoder.java

* portal-shared-deploy (shared) - Pretty empty - is this needed?

* portal-util A set of utility classes to be used in tools and in the portal

* portal-chat Makes portal-chat.war - The Rest APIs to support portal chat

* portal-tool

* editor-tool

## Special portal URL's/Handlers
The portal tools has a number of special handlers these are defined/added in the init of [SkinnableCharonPortal](https://github.com/sakaiproject/sakai/blob/master/portal/portal-impl/impl/src/java/org/sakaiproject/portal/charon/SkinnableCharonPortal.java)

Some special urls include
* Generate Bug Report, just tests the bug report, must be logged on as admin.
  * `/portal/generatebugreport`
* Direct Tool, goes directly to a tool id given a site id
  * `/portal/directtool/sakai.samigo?sakai.site=e4a2b1db-f452-4ef0-8aee-b4bdde701c8c`
  * `/portal/directtool/sakai.rsf.evaluation?sakai.site=~`

## Information about Morpheus
- [ Morpheus Documentation ](../reference/README.md)

