The EventTrackingSerive.newEvent() got a slick new method signature in 2.6 which adds a contextId.

This is very useful to admins.  But it makes it so that the same code cannot compile 
in 2.5/2.4 versus 2.6/2.7.

I have both bits of code in with one commented out in these files:

impl/src/java/org/sakaiproject/basiclti/impl/BasicLTISecurityServiceImpl.java
portlet/src/java/org/sakaiproject/portlets/IMSBLTIPortlet.java

In both cases look for "EventTrackingService.newEvent" and uncomment the right one.

If you have the wrong one in 2.5 - you will get a compile error.
If you have the wrong one in 2.6 - you will not see the context ID.

So I will leave it in SVN as the 2.6 and later way - and the 2.5 folks will need
two little patches.

/Chuck
Sat Jul 25 18:17:41 EDT 2009
