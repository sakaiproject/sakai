Wed Feb 13 07:36:21 EST 2013

This tool (web-tool) is now deprecated for use with the following 
tool registrations:

  sakai.iframe 
  sakai.iframe.myworkspace 
  sakai.iframe.service 
  sakai.iframe.site 

These tool registrations are now taken over by the web-portlet
tool.  The code for the above tool registrations has not been 
removed, it is simply no longer active.

This code (web-tool) *is* being used to serve the 

  sakai.iframe.annotatedurl

tool identifier.   This tool id could also be moved to the web-portlet
but the code for annotatedurl needs to be completed and tested.

From an internationalization perspective the two sets of bundles
(web-tool) and (web-portlet) should remain clones of one another 
to keep things as simple as possible - there is no reason for them
to diverge in any way.

