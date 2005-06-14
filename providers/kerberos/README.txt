This directory contains a sample Kerberos UserDirectoryProvider.

This has not yet been tested so you should consider this code sample code 
and test accordingly.

By default, the provider provides authentication ONLY for existing 
accounts in the local Sakai user database (where Sakai user ids and 
Kerberos principals are the same). If you wish to allow anyone with a 
valid Kerberos principal to use Sakai, follow the instructions below 
under ADVANCED OPTIONS.

In addition, to protect the integrity of your Kerberos password, the use 
of a secure Web front-end is HIGHLY recommended.

**GENERAL SETUP**

To use this provider:

1) Configure Java for Kerberos using JAAS:

- Find the file java.security in the your Java installation, usually in 
$JAVA_HOME/lib/security or $JAVA_HOME/jre/lib/security.

- Add this block (or edit an existing block):

# Default login configuration file
#
#login.config.url.1=file:${user.home}/.java.login.config
login.config.url.1=file:${java.home}/lib/security/jaas.conf

The name of the login configuration file can be anything.

- In the same directory as java.security, create a file called jaas.conf 
with the following content:

/*
 * Login Configuration for the JAAS Applications 
 */

KerberosAuthentication {
   com.sun.security.auth.module.Krb5LoginModule required;
};

The UserDirectoryProvider uses the KerberosAuthentication context by 
default; it can be configured by changing the loginContext parameter in 
the components.xml file described in Step 3.

- Copy or create a symlink to your existing krb5.conf in the same 
directory as java.security.

2) Test your JAAS installation. A test program (JaasTest.java) is 
available to confirm Kerberos authentication. To run it:

$ javac JaasTest.java
$ java JaasTest KerberosAuthentication

LoginContext for testing: KerberosAuthentication
Enter a username and password to test this LoginContext.

Kerberos username [user1]: user1
Kerberos password for user1: foo
Authentication succeeded.

If authentication fails, contact your local administrator for help.

3) Uncommment the configuration information in

    ../component/project.xml

Then, change the domain parameter from "mydomain.edu" to your domain in

    ../component/src/webapp/WEB-INF/components.xml

After deployment, you can also edit this file:

$TOMCAT_HOME/components/sakai-legacy-providers/WEB-INF/components.xml 

4) After deployment and startup, create a new Sakai account with your 
Kerberos principal as the user id. You should then be able to login 
using your Kerberos password.

**ADVANCED OPTIONS**

The Kerberos UserDirectoryProvider takes 4 configuration properties:

domain -- the 2nd-level domain name of the user's e-mail address
loginContext -- the name of the JAAS LoginContext to use
requireLocalAccount -- require a local account to login (true/false)
knownUserMsg -- error message used to determine valid Kerberos principal

If you wish to allow anyone with a valid Kerberos principal to login 
into Sakai without a local account, you must configure the last two.

To do so:

1) Determine the error message to be used by passing a valid Kerberos 
principal and an INCORRECT password to the JaasTest application:

$ java JaasTest KerberosAuthentication

LoginContext for testing: KerberosAuthentication
Enter a username and password to test this LoginContext.

Kerberos username [user1]: user1
Kerberos password for user1: foo
Authentication failed.

Error message
 --> Integrity check on decrypted field failed (31) - PREAUTH_FAILED

In this case, we'll use "Integrity check on decrypted field failed" as 
the string to compare (the provider compares the first part of the 
string).

2) In the components.xml file, add these two lines:

<property name="requireLocalAccount"><value>false</value></property> 
<property name="knownUserMsg"><value>STRING</value></property>

where STRING is replaced by "Integrity check on decrypted field failed"
Note: This string is the default, culled from RFC 1510.


