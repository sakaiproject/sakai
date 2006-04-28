BINARY INSTALLATION GUIDE

Binary Installation Overview:

The Binary Installation of Sakai provides a shortcut for those that already have Tomcat in place and configured as needed, and it does so by providing a pre-built Sakai that can simply be dropped into Tomcat as an overlay.

All you need to do is unzip the Binary archive from within the top Tomcat directory (e.g. $CATALINA_HOME). The appropriate wars, jars, etc., will be deposited in the correct locations, as the structure of the archive maps to the file structure of Tomcat.


Configuration:

If you wish to configure your Sakai installation you'll still need to manually create a 'sakai' directory in $CATALINA_HOME to hold your *.properties files (i.e. the binary installation doesn't include this).  On a Unix system, for example,

mkdir $CATALINA_HOME/sakai

For more configuration information see the online Source Install Guide available on the 2.0.1 release page. 
