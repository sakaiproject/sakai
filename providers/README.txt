This is the direcrtory for the providers which configure Sakai for 
Users, Courses, and Realms.

The directory layout is as follows:

component    Contains the component which is deployed to the 
   servlet container and selects the providers.  As shipped,
   Sample providers are selected.  Sample configuration necessary
   to use the jldap provider is included in the configuration files:
   
sample       Contains the sample providers.

jldap        Contains the jldap UserDirectoryProvider - see the README
   in that directory for proper configuration and use.

kerberos     Contains a basic Kerberos provider.

allhands 	Contains a provider that makes a "sakai.allhands" provider and
   enrolls all users in that provider.
