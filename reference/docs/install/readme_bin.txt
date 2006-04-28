BINARY INSTALL

A successful binary installation requires the support of the correct Java and Tomcat installations.  The binary install assumes that these are already in place.  If you have questions for how to do this, see the source installation instructions (readme_source.txt in this directory, or in docs/install/readme_source.txt on subversion).

The Binary Installation of Sakai provides a shortcut for those that already have Tomcat in place and configured as needed (see the "Set up Build Environment" and "Post-Installation Configuration" sections of the source installation readme), and it does so by providing a pre-built Sakai that can simply be dropped into Tomcat as an overlay. All you need to do is unpack the Binary archive from within the root Tomcat directory (e.g. $CATALINA_HOME). The appropriate wars, jars, etc., will be deposited in the correct locations, and then you can start up Tomcat.

If you wish to configure a binary installation you'll need to manually create a 'sakai' directory in $CATALINA_HOME to hold your *.properties files, as described in the "Post-Installation Configuration" section of the source installation instructions.