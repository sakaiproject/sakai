The EventTrackingSerive.newEvent() got a slick new method signature in 2.6 which adds a contextId.

This is very useful to admins.  But it makes it so that the same code cannot compile 
in 2.5/2.4 versus 2.6/2.7.

EDIT:

Because of LocalEventTrackingService and the profiles this is no longer an issue. 

build-helper-maven-plugin compiles only the code that will work with each specific version.

/jonespm
Mon Aug 31 16:51:11 EDT 2009
