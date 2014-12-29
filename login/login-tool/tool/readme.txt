SAK-23187 makes it possible to externalize CAS configuration from the login tool.

This is done by placing a file called xlogin-context.xml into your sakai.home folder, and then either recycling
the login-tool app or the tomcat itself.

This mechanism takes advantage of the typical way CAS can be configuring via Spring.

See https://wiki.jasig.org/display/CASC/Configuring+the+JA-SIG+CAS+Client+for+Java+using+Spring for more detail
on configuring CAS with Spring.

A sample xlogin-context.xml is included for reference.

