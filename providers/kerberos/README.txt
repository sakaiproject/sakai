This directory contains a sample Kerberos UserDirectoryProvider.

It (and its derivatives) are in use at a few institutions, but you
should review the code and test it accordingly.

Instructions for installing and configuring the provider can be found
in the "docs" directory. The INSTALL.txt file is the place to start.

There are some unit tests but they are only enabled with the test
profile is run, and you need to copy the sample configuration files
(in src/test-bundle) to drop the sample- prefix.

This provider has its roots in the University of Michigan's
UnivOfMichUserDirectoryProvider.java that was included with Sakai 1.5.
A stand-alone Kerberos provider first appeared in Sakai 2.0.0.
