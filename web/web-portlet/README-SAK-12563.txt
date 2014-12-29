Wed Feb 13 07:36:21 EST 2013

This tool (web-portlet) now serves the following tool registrations:

  sakai.iframe 
  sakai.iframe.myworkspace 
  sakai.iframe.service 
  sakai.iframe.site 

This web-tool *is* being used to serve the 

  sakai.iframe.annotatedurl

tool identifier.   

This tool id could also be moved to the web-portlet
but the code for annotatedurl in this portlet is currently only
partially implemented and to be used, it needs to be completed 
and tested.  

The annotatedurl feature is rately used and its use cases are 
pretty narrow and subtle.

From an internationalization perspective the two sets of bundles
(web-tool) and (web-portlet) should remain clones of one another 
to keep things as simple as possible - there is no reason for them
to diverge in any way.

