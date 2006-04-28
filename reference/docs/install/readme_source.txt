SOURCE INSTALLATION

Installing Sakai from source provides the greatest flexibility for configuring Sakai.


TABLE OF CONTENTS

   1. Installation Overview
   	* Migrating from a previous release
   2. Set Up Build Environment
         2.1 Verify Java installation
         2.2 Set environment variables
         2.3 Install Tomcat
         2.4 Configure Tomcat
         2.5 Install Maven
         2.6 Configure Maven
         2.7 Test Maven
   3. Build and Deploy Sakai
         3.1 Download source
         3.2 Unpack source
         3.3 Run maven
   4. Post-Installation Configuration
         4.1 Create sakai folder
            for properties files
         4.2 The sakai.properties file
         4.3 Personalizing Sakai
         4.4 Email configuration
         4.5 JVM tuning
         4.6 Test Sakai
   5. Database Configuration
   	 5.1 Migrating from a previous version (redux)
         5.2 Deploy drivers
         5.3 Create Sakai database and user
         5.4 Database Properties
         5.5 Upgrade Scripts
   6. Troubleshooting
         6.1 Common Issues
         6.2 Working with Maven
         6.3 Tomcat logs
         

1. INSTALLATION OVERVIEW

The Demo and Binary installs described above offer much-abbreviated processes, but they assume that no special configuration is called for. The remainder of this installation guide is devoted to the more rigorous and configurable Source installation, the steps of which may be organized into the following phases:

   1. Set up the build environment, including Java, Tomcat, and Maven.
   2. Build and deploy Sakai by running Maven on the source.
   3. Perform appropriate Post-Installation Configuration of the application.
   4. Carry out the steps for Database Configuration.
   
* Migrating from a previous release:

2.1.2 includes changes to CSS (which may affect some skins), some APIs, and the database schema.  Be sure to examine the release notes (release_2.1.2.txt) carefully for the details.


2. SET UP BUILD ENVIRONMENT


2.1 Verify Java installation

Check to see if you have Java 1.4.2 (1.4 is required; Java 1.5 will not work) installed on your system by running the following command:

Windows: 	
C:\> java -version
Java(TM) 2 Runtime Environment, Standard Edition 
(build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

*nix variants: 	
$ java -version
java version "1.4.2_04"
Java(TM) 2 Runtime Environment, Standard Edition 
(build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

If you do not have the correct version of Java installed, install the Java Software Development Kit (SDK) from http://java.sun.com/j2se/1.4.2/download.html 


2.2 Set Environment Variables

Set the JAVA_HOME environment variable to point to the base directory of your Java installation. This will enable Tomcat to find the right Java installation automatically. This may already be set up for you by your Java SDK installation, but it should be double-checked.

In the UNIX operating systems, you typically modify a startup file like ~/.bash_login to set and export these shell variables. In Windows XP, you go to
Start -> Control Panel -> System -> Advanced -> Environment Variables
And then set them as described below:

Windows: 	Set the environment variable JAVA_HOME to "C:\j2sdk1.4.2_04" (do not include the quotes)

Mac: 	export JAVA_HOME=/Library/Java/Home

Linux: 	export JAVA_HOME=/usr/local/java-current

Next you'll want to extend the PATH variable so as to include the Java commands.

Windows: 	Append the string ";C:\j2sdk1.4.2_04\bin" (include the semicolon but not the quotes) to the end of the system variable named Path.
Mac: 	not necessary
*nix: 	export PATH=$PATH:$JAVA_HOME/bin/

You should test that these variables are set correctly.  In both Windows XP and *nix operating systems you can simply start a new shell and type the 'set' command to see your environment variables. You may run the java -version command once more (see above) as a final check.

You can also look for the Sun Java Installation Instructions page at the Java web site for further details.


2.3 Install Tomcat

The latest stable version of Tomcat 5.5 (currently 5.5.15) can be downloaded as a binary from:
http://tomcat.apache.org/download-55.cgi
The distribution you want is the one labeled Core, along with the JDK 1.4 Compatability Package.

**Windows Note:**
Windows users have the option of either downloading an .exe Tomcat installer or a .zip binary. Although either may serve, they are however not entirely equivalent when it comes to the startup scripts they include, and for brevity's sake subsequent instructions will simply assume that you grabbed the .zip file.

Choose a location to install Tomcat, and unpack both the Tomcat binary and the compatibility package there in the same location. The compatibility package will simply overlay your Tomcat directories with the appropriate files. From this point forward these instructions will refer to the top-level Tomcat directory (e.g. apache-tomcat-5.5.12) as $CATALINA_HOME. You may set this as an environment variable for convenience's sake, but this is not required.  Make sure that you have write permissions to the Tomcat files and directories before proceeding, or you may later run into errors during the build phase.


2.4 Configure Tomcat

Sakai supports UTF-8, allowing for non-Roman characters, but this requires that Tomcat be configured to accept UTF-8 URLs, since it ships with ISO-8859-1 as the default URL encoding. To change this setting, edit $CATALINA_HOME/conf/server.xml. Add the attribute URIEncoding="UTF-8" to the <connector> element. For example:

<Connector
port="8080" maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
enableLookups="false" redirectPort="8443" acceptCount="100"
debug="0" connectionTimeout="20000" disableUploadTimeout="true"
URIEncoding="UTF-8"/>

If you want to run Tomcat on different ports than the defaults, this would also be a good time to make those changes in the server.xml file. See Tomcat configuration documentation for more details.

If you're going to run Tomcat in isolation (i.e. if you're not going to connect it to Apache) then you'll want to make a further minor Tomcat change that may spare some confusion later. In order to make sure that entering the URL to your server will redirect to the Sakai application seamlessly, you'll need to copy an index.html file to webapps/ROOT. The ROOT webapp is the one served up when a request is made to your web server's root URL. The index.html file to add to webapps/ROOT simply redirects browsers to the full URL of the gateway page, and it should look something like:

<html>
<head>
  <meta http-equiv="refresh" content="0;url=/portal">
</head>
<body>
  redirecting to /portal ...
</body>
</html>

If you don't make this change you'll need to append '/portal' to the URL you enter to access Sakai each time. If you are going to connect Tomcat with Apache, you can handle this as a matter of Apache configuration, which is however outside the scope of this document.


2.5 Install Maven

Maven is the build tool used by Sakai, and the latest stable release (currently 1.0.2) can be downloaded from:
http://maven.apache.org/start/download.html

**Newer versions of Maven**
It's important that Maven 1.0.2 be used. There are betas for Maven 1.1 and Maven 2.0 currently available, but neither will work for Sakai 2.1. Many plugins have not yet been adapted for the new Maven architecture.

Choose a location to install Maven, and unpack the archive there. You will have a top-level directory named maven-<VERSION> (e.g. maven-1.0.2).

2.6 Configure Maven

To use Maven you'll need to set two environment variables and then create a local repository using a script provided by Maven.

First, define the MAVEN_HOME environment variable which is the directory where you just unpacked the Maven install archive. You will also need to add MAVEN_HOME/bin to your path so that you can run the scripts provided with Maven.

Windows: 	Create a new MAVEN_HOME environment variable to "C:\maven-1.0.2"
Append to the PATH variable ";C:\maven-1.0.2\bin"

Mac/*nix: export MAVEN_HOME=/usr/local/maven
export PATH=$PATH:$MAVEN_HOME/bin

Next, you should create your local repository by running the following command:

Windows: install_repo.bat %HOMEDRIVE%%HOMEPATH%\.maven\repository
Mac/*nix: install_repo.sh $HOME/.maven/repository

Finally, you'll need to create a build properties file in your home directory which will configure Maven for your Sakai build. Simply create a new text file with the filename build.properties in your home directory, and paste in the following contents:

maven.repo.remote=http://cvs.sakaiproject.org/maven/,http://source.sakaiproject.org/mirrors/ibiblio/maven/
maven.tomcat.home=/usr/local/tomcat/

**build.properties syntax**
Maven is very sensitive to the syntax of the build.properties file. Be sure not to omit the trailing slashes as shown above, and be sure to change the value of maven.tomcat.home to match the path to your Tomcat installation.

Further, if you are running on Windows special care is needed in identifying your tomcat home. Maven wants Unix-style forward slashes, "/", and is confused by Windows-style backslashes "\". If you have your tomcat located in "c:\tomcat", for example, you need to identify it like this:

maven.tomcat.home = c:/tomcat/


2.7 Test Maven

To confirm that you can start Maven, run the command:

maven -v

This should start maven and cause it to report its version.

At this point your environment is prepared to build and deploy the Sakai source code.


3. BUILD AND DEPLOY SAKAI

** Where to Find More Information **

The steps below are limited to what you need to get started, but you may feel the need for more in-depth explanations of certain topics.

How Sakai Uses Maven: to find the most detailed documentation on how Sakai uses Maven, look for sakai_maven.doc in docs/architecture of the source archive. This includes directions for installing the Sakai plugin into your maven environment, so that you can run maven commands from the modules and projects within Sakai, instead of always building the entire code base. A direct link to the documentation file in subversion is below:
https://source.sakaiproject.org/svn/tags/sakai_2-1-2/docs/architecture/sakai_maven.doc


3.1 Download Source

Download the Sakai Source archive from: http://www.sakaiproject.org/release

**Getting the Code from Subversion**
Alternatively, you may check out the source code from subversion with the command:

svn co https://source/sakaiproject.org/svn/tags/sakai_2-1-2

In which case you could skip the unpacking step below, and your root directory would be sakai_2-1-2 instead of sakai-src.


3.2 Unpack Source

Choose a location to unpack the Sakai source (your home directory is fine), and when you unpack it you will see a directory named sakai-src.


3.3 Run Maven

From within sakai-src, run the command:

maven sakai

This will run for quite some time with fairly verbose output, particularly if it's your first build. Do not be alarmed by reports of sakai jar download failures during an initial clean phase: most of the sakai jars will be compiled during the build phase, yet maven will still try to look for them ahead of time. A full clean, build, and deploy cycle may take quite some time, depending on the responsiveness of remote repositories, but a normal maven build of all the Sakai source should take roughly 15-20 minutes. Maven will download any dependencies into the local repository, compile the Sakai code, and deploy Sakai to Tomcat in the form of .war files in the $CATALINA_HOME/webapps directory.

**.jar Download Failures **

A first build of Maven on a fresh installation will warn of numerous jar download failures for sakai jars during the clean phase. This is not generally a problem. Even when doing a mere "clean", maven tries to download dependencies. Typically these jars are named according to the build release e.g. for Sakai 2.1 you will see:

WARNING: Failed to download sakai-authentication-sakai_2-1.jar

Once these jars are built the errors will not reappear for subsequent builds.


If Maven completes with the message BUILD SUCCESSFUL, you should be able to move on to the next step. If you are greeted with the report BUILD FAILED read the accompanying error message carefully to troubleshoot (see the Troubleshooting section).

You will probably be able to start up Tomcat and run Sakai with the default configuration at this point, but it might be better to take care of some basic configuration first.


4. POST-INSTALLATION CONFIGURATION

This section of the Installation Guide describes basic configuration details. For a more exhaustive treatment of Sakai configuration, refer to the "Full Configuration" area of the "Documentation" space on Confluence:
http://bugs.sakaiproject.org/confluence/display/DOC/Full+Configuration


4.1 Create sakai folder for properties files

Sakai runs with a default set of properties for its various components.  To override them you'll want to specify them in a sakai.properties file which should be located in $CATALINA_HOME/sakai by default.  This directory is not created by maven, so you'll have to do so manually.  Once this directory is created, there are several *.properties files you can place inside which can override default properties. 

**Choosing a different location for the properties files**
You may find it advantageous to store sakai's configuration files outside of the Tomcat file structure. In a development environment, for example, you may find yourself frequently blowing Tomcat away, and you may wish to avoid recreating the sakai folder and its contained properties files each time.

To accomplish this, you need only modify the java startup command (or the JAVA_OPTS environment variable) to set the system property sakai.home, e.g.:

-Dsakai.home=/path/to/desired/sakai/home/

You'll need to make sure that this location is readable and writable to Tomcat.


4.2 The sakai.properties file

The main configuration file for Sakai is called sakai.properties, and you can either create it from scratch or copy in a known working copy. A sample sakai.properties file which self-documents many of the standard properties in its comments can be found in:

sakai-src/docs/sakai.properties

sakai.properties can define two different types of settings.  The first type sets those values that are made available to the running code from the Sakai configuration service.  A line of sakai.properties sets this sort of value if it has the form: 

name=value

The second type of sakai.properties setting overrides the configuration defaults of individual sakai components.  These defaults are set in the components.xml file of any particular component, and so this configuration can in principle be achieved by also editing a large number of components.xml files (one for each component), but it's a best practice to keep all configuration changes in sakai.properties.  Override settings have the form:

name@component-name=value

New value settings can be freely added to the sakai.properties file, since any component property can in principle be overridden here, and so any sample sakai.properties will show only a small fraction of all the possible settings.

**Where to Find More Information**

sakai.properties documentation: to find the most detailed documentation on the full variety of possible sakai.properties settings, look for sakai_properties.doc in docs/architecture of the source archive. A direct link to the documentation file in the 2-1-2 tag of subversion is below:
https://source.sakaiproject.org/svn/tags/sakai_2-1-2/docs/architecture/sakai_properties.doc


4.3 Personalizing Sakai

Sakai has a number of places where your institution names, service names, and the host name of your service are used. These can be configured in sakai.properties:

# identify your application server with a short name,
unique among the servers in your cluster.

# choose a server id even if you are running a single app server
serverId=localhost
# the URL to the server, including transport, DNS name, and port, if any
serverUrl=http://localhost:8080

# the DNS name of the server
serverName=localhost

# the URL to send folks to after they logout
loggedOutUrl=http://localhost:8080/portal

# some fill-ins for the css/vm ui
(Worksite Setup, Digest Service, Email notification, Worksite Setup, Contact Support, Portal)
ui.institution = Your Institution
ui.service = SakaiOrWhatever

#copyright text to appear in the bottom area of each web page.
bottom.copyrighttext=(c) 2003, 2004, 2005 sakaiproject.org. All rights reserved.

# links placed on the bottom nav - set the .count to the number of items,
then add each item
bottomnav.count = 2
bottomnav.1 = <a href="https://localhost/portal/site/!gateway">Gateway</a>
bottomnav.2 = <a href="http://sakaiproject.org/cms" target="_blank">The Sakai Project</a>

You can add more or other links to the bottom navigation bar by setting the proper bottomnav.count value and adding bottomnav.N values (1 through the number of links).


4.4 Email Configuration

Sakai needs to be set up for two email functions: receiving email sent to Sakai sites, and sending out email notifications.

For sending mail Sakai needs the address (name or IP) of an SMTP server that will accept mail from Sakai. This needs to be set in your sakai.properties file:

smtp@org.sakaiproject.service.framework.email.EmailService=some.smtp.org

To enable Sakai to receive mail there are a few settings needed in the sakai.properties file:

# dns addresses used for incoming email	
smtp.dns.1 = 255.255.255.1
smtp.dns.2 = 255.255.255.2
	 
# SMTP port on which our SMTP server runs. Default is 25.
#Recommend running on 8025, and using a standard mailer on 25 to forward mail to Sakai.
smtp.port = 25

# flag to enable or disable our SMTP server for incoming email (true | false)
smtp.enabled = true


To disable the SMTP server for incoming email, use this in sakai.properties:

smtp.enabled = false

Sakai's SMTP server is 'James,' and to run with the above configuration which runs James on the standard SMTP port 25 you must be running with admin privileges. Most production folks don't want to let Tomcat run with that, and would rather run a standard mailer like postfix on port 25 and configure it to forward requests to Sakai. You might also already have a mailer service running on port 25 (Linux usually has it running by default), and so you'd want to change the James port simply to avoid a conflict. You'll typically want to run James on another, non-restricted port, then.  For example:

smtp.port = 8025


4.5 JVM Tuning

The Java virtual machine's configuration is very important to tune for best performance in any production environment, and some of Sakai's tools may not operate with the memory alloted by the default JVM.

**Disclaimer**
JVM tuning is, as a general rule, something of a black art, and we recommend that you take the time to experiment with different memory and garbage collection settings and see what works best in your environment. We can make some minimal recommendations that should serve as a foundation for further experimentation and testing, but the following details are however offered only as samples or suggestions, and we recommend that you consult a systems administrator or local Java guru before making any such changes to a production system. See Sun's "Tuning Garbage Collection" documentation for more details.

The standard way to control the JVM options when Tomcat starts up is to have an environment variable JAVA_OPTS defined with JVM startup options. Since potential Sakai installs can range from developers just wanting to kick the tires all the way up to large-scale deployments, it's hard to recommend a single set of such options as the preferred ones for every case. But we can start with a bare minimum, which provides for sufficient heap size and the permanent generation to at least avoid "Out of Memory" errors:
      
JAVA_OPTS="-server -Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m"
      
This is an adequate - if minimal - starting point: it selects server mode, sets an adequate heap size [We have found the best results when the min and max memory settings (Xms and Xmx, respectively) are set to be the same values], and sizes the permanent generation to accommodate more longer-persisting objects. These settings will allow things to be functional for testing, but will hardly be adequate for serving multiple concurrent users. A more suitable production environment on a 32-bit machine might use a set of options like:
      
JAVA_OPTS="-server -Xms1500m -Xmx1500m -XX:NewSize=400m -XX:MaxNewSize=400m -XX:PermSize=128m -XX:MaxPermSize=128m -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"
      
This is better: a larger heap size is set for smoother performance, and garbage collection messages are turned on. Another important setting is the NewSize - actually, the ratio of it to the heap size. We want as large a NewSize as we can fit in the total heap, while keeping the total heap significantly bigger than NewSize in order for Java to properly garbage collect.
      
As you can see, there's a lot to think about here, and in practice each implementing institution uses a different JAVA_OPTS that they've settled on for their deployment and hardware, many of which use considerably more option arguments than we've shown here. This discussion is meant only as a head start, and there is no replacement for doing your own testing.
      
In any event, once you set JAVA_OPTS Tomcat will see this environment variable upon startup and use it. Instead of putting this in an environment variable, you might modify the $CATALINA_HOME/bin/catalina.bat or $CATALINA_HOME/bin/catalina.sh file to automatically set these values during startup.

**JVM Tuning in Windows XP**
Windows users may set Java options in a "Tomcat Properties" dialog. But you may find that the permanent generation settings mentioned above will not work appropriately if set in the Java Options: field of the dialog window. That's because these settings have their own fields in the properties GUI, under the headings Initial Memory Pool and Maximum memory pool, respectively.  Windows apparently ignores attempts to alter these values through the Java Options field.


4.6 Test Sakai

At this stage your installation of Sakai has not yet been configured to use your preferred database (it will use its own HSQLDB by default), but you should now be able to bring it up as a working web application by simply starting Tomcat, and it can be helpful at this point to know if any problems exist before you try to connect it to another database. Tomcat will take a minute or so to start up, depending on the speed of your machine, and it's a good idea to watch the Tomcat log as it comes up to catch any errors (see Troubleshooting).

From $CATALINA_HOME you can start up Tomcat with the command:

Windows: start-sakai.bat
Mac/*nix: start-sakai.sh

Once Tomcat has loaded the Sakai application (again, this can take a minute or so) point your browser to

http://localhost:8080/portal

If the gateway page does not come up, check the Tomcat logs (see Troubleshooting ).  If the gateway page does come up, log in with the default admin account:username = admin, password = admin. 

If you can log in without errors you should be able to stop Tomcat and proceed with Database configuration, if needed.

Windows: stop-sakai.bat
Mac/*nix: stop-sakai.sh


5. DATABASE CONFIGURATION

5.1 Migrating from a Previous Version (redux)

There are database schema changes between 2.1.1 and 2.1.2, and the update scripts to be applied - in distinct versions for mysql and Oracle, respectively - are found in the docs/updating folder of the release or on subversion:

MySQL: https://source.sakaiproject.org/svn/tags/sakai_2-1-2/docs/updating/sakai_2_1_0-2_1_1_mysql_conversion.sql
Oracle: https://source.sakaiproject.org/svn/tags/sakai_2-1-2/docs/updating/sakai_2_1_0-2_1_1_oracle_conversion.sql

In the same directory you'll also find conversion scripts for earlier Sakai versions. Migration from an earlier version will require the successive application of all intermediate update scripts. You cannot, for example, move from 2.0.1 to 2.1.1 by applying a single DB script. You will need to move first from 2.0.1 to 2.1.0, and then to 2.1.1.

** Examine Before Using **
As a general rule, be sure to read through these conversion scripts before applying them. The 2.0-2.1 script, in particular, includes notes about roles that may spare you a potential headache if you've been running 2.0 in production.


5.2 Deploy Drivers

The supported production-grade databases include MySQL 4.1+ and Oracle 9i+. The version of the JDBC driver (or connector) is also important. For MySQL the 3.1.12 connector should be used, while for Oracle the 10g driver should be used, even if the database is version 9i. The 9i driver will not be sufficient.

**Driver Versions**
Database driver versions are a common source of problems. It's worth emphasizing again that the Oracle 10g driver *must* be used for Sakai installations running against Oracle, even when Oracle 9i is the database version.

Problems have been reported for both the 3.1.10 and 3.1.11 MySQL drivers. 3.1.12 is the recommended version.

You need to have appropriate JDBC drivers for your database installed in your $CATALINA_HOME/common/lib directory, and they are available from the official sites for both Oracle and MySQL.


5.2 Create Sakai Database and User

A Sakai database and privileged user must be prepared for Sakai's use. Consult your database documentation for details, but here are sample commands for MySQL:

C:\sakai\reference\sql\legacy\mysql\>
mysql -u root -p
Enter password: ******
Welcome to the MySQL monitor. Commands end with ; or \g.

Your MySQL connection id is 51 to server version: 4.1.5-gamma-nt

Type 'help;' or '\h' for help. Type '\c' to clear the buffer.

mysql> create database sakai default character set utf8;
Query OK, 1 row affected (0.00 sec)

mysql> grant all on sakai.* to sakaiuser@'localhost' identified by 'password';
Query OK, 0 rows affected (0.00 sec

mysql> grant all on sakai.* to sakaiuser@'127.0.0.1' identified by 'password';
Query OK, 0 rows affected (0.00 sec)

mysql> quit 


5.3 Database Properties

**Significant Change to DB configuration**
The process of setting database properties has changed significantly from previous versions of Sakai. Specifically, the placeholder.properties file in its entirety has been deprecated, and all configuration settings are now made in sakai.properties.

There are settings in sakai.properties that also define the database technology and connection information. Appropriate sakai.properties settings for Oracle and MySQL, respectively, are listed below (although some pieces will of course need to be altered appropriately for your installation):

    * Oracle:

vendor@org.sakaiproject.service.framework.sql.SqlService=oracle
driverClassName@javax.sql.BaseDataSource=oracle.jdbc.driver.OracleDriver
url@javax.sql.BaseDataSource=jdbc:oracle:thin:@your.oracle.dns:1521:SID
username@javax.sql.BaseDataSource=[database user name]
password@javax.sql.BaseDataSource=[password]
hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
auto.ddl=true
validationQuery@javax.sql.BaseDataSource=select 1 from DUAL
defaultTransactionIsolationString@javax.sql.BaseDataSource=TRANSACTION_READ_COMMITTED

**Oracle performance**
Oracle may have performance problems with some of the SQL settings that work for HSQL and perhaps even for MySQL. By default, Oracle should be set to the proper settings automatically; setting it with each use may affect performace. In addition, validating the connection on each transaction caused problems in at least one large production environment (University of Michigan). Sakai installations using Oracle should strongly consider the following settings in sakai.properties to avoid these problems:

# For improved Oracle performance (from the University of Michigan)
validationQuery@javax.sql.BaseDataSource=
defaultTransactionIsolationString@javax.sql.BaseDataSource=
testOnBorrow@javax.sql.BaseDataSource=false

This unsets the first two values and overrides the settings that are in the base (kernel) sakai.properties file.

    * MySQL:

vendor@org.sakaiproject.service.framework.sql.SqlService=mysql
driverClassName@javax.sql.BaseDataSource=com.mysql.jdbc.Driver
url@javax.sql.BaseDataSource=jdbc:mysql://127.0.0.1:3306/sakai?useUnicode=true&characterEncoding=UTF-8
username@javax.sql.BaseDataSource=[database user name]
password@javax.sql.BaseDataSource=[password]
hibernate.dialect=net.sf.hibernate.dialect.MySQLDialect
auto.ddl=true
validationQuery@javax.sql.BaseDataSource=select 1 from DUAL
defaultTransactionIsolationString@javax.sql.BaseDataSource=TRANSACTION_READ_COMMITTED

**Oracle Change for Tests&Quizzes**
If you're running Oracle and using auto.ddl to create tables, you should check the data type of the "MEDIA" column in the SAM_MEDIA_T table. Hibernate tries to choose the right data type for this field, but has a habit of choosing the wrong one for Oracle.

    * The correct types for each database are: HSQLDB --> varbinary
    * Oracle --> blob
    * MySQL --> longblob

If you need to change this type for your database, this will also involve finding the primary key constraint, dropping it and then recreating it. Contact your local DBA for further information on making this change.

Below is some sample Oracle SQLplus output to better illustrate (SYS_C0064435 is the example constraint; replace it with yours):
______________________________

SQL> alter table SAM_MEDIA_T modify MEDIA BLOB;

Table altered.

SQL> select constraint_name from user_constraints where table_name='SAM_MEDIA_T'
and CONSTRAINT_TYPE='P';

CONSTRAINT_NAME
------------------------------
SYS_C0064435

SQL> alter table sam_media_t drop constraint SYS_C0064435;

Table altered.

SQL> alter table SAM_MEDIA_T add constraint SYS_C0064435 primary key (MEDIAID);

Table altered.

SQL> desc SAM_MEDIA_T;

[table with BLOB type]

SQL> select constraint_name from user_constraints where table_name='SAM_MEDIA_T'
and CONSTRAINT_TYPE='P';

CONSTRAINT_NAME
------------------------------
SYS_C0064435

SQL> commit;

Commit complete.
_______________________________


6. TROUBLESHOOTING


6.1 Common Issues

* Out of Memory Errors:
	If the screen of a tool fails to load and catalina.out reports an "Out of Memory" error, then you'll likely need to alter your JVM settings. Many modern app-server technologies make use of objects that persist for longer periods of time, and therefore many Sakai deployments have found it beneficial to adjust the JVM with flags to specify enough permanent space to accommodate these objects. Programs that dynamically generate and load many classes (e.g. Hibernate, Struts, JSF, Spring etc.) usually need a larger permanent generation than the default 32MB maximum. The permanent generation is sized independently from the other generations because it's where the JVM allocates classes, methods, and other "reflection" objects. Specify flag -XX:PermSize=16m to allow the JVM to start with enough memory so that it doesn't have to pause apps to allocate more memory. Specify -XX:MaxPermSize for the size of the permanent generation to be greater than the default 32MB maximum. When running on servers with multiple cpus, you'd multiply the memory by the number of CPU's. For example, to run a quad-processor machine you'd set the flag to -XX:MaxPermSize=128m. 

See also the "JVM Tuning" discussion in the Post-Installation Configuration section (section 4.5).


6.2 Working with Maven

**Maven in Sakai**
See the maven.pdf posted to the "Architecture Docs" folder of the sakai-dev collab site's resources for a more thorough examination of how maven is used with Sakai. You can also find the "source" of this document as an MSWord file in docs/architecture (or on subversion):


* Newer versions of Maven:
      Sakai will fail to build with the latest beta versions of maven (currently 1-1-beta, and 2.0). Maven 1.0.2 has been working reliably with Sakai for over a year, and the Sakai plugins have not been altered to conform to the architecture of Maven 2, so that even if Maven 2 comes out of beta in the near future 1.0.2 should continue to be used.
* Updating the plugin:
      You can install the Sakai plugin into your maven environment, which is useful for letting you run maven commands from the modules and projects within Sakai instead of always building the entire code base. Installing the plugin makes it available whenever you use maven, not just for those projects that declare a dependency on the plugin. You will need to do this once each time you upgrade a minor point version of sakai.
      Maven plugin install
      maven plugin:download -DgroupId=sakaiproject -DartifactId=sakai -Dversion=2.1
      Note that the plugin-version is not necessarily the same as the Sakai version. The correct version to use will be available as the value of the sakai.plugin.version property in the sakai/master/project.properties file.
* jar download failures:
      A first build of Maven on a fresh installation will warn of numerous jar download failures during the clean phase. This is not a problem. Even when doing a clean, maven tries to download dependencies. When the repository is initially empty maven can freak out during the clean phase while looking for jars that the impending build phase has yet to build. Once they are built, maven clean is perfectly happy next "clean build" cycle.
* Tomcat write permissions:
      The most common maven build failures stem from the user running maven not having write access to the Tomcat directories being deployed to. Maven's error messages should make this fairly plain when it happens, and will even specify which copy or delete operation failed.
* Maven -x:
      Running maven with the x argument runs it in debug mode, and produces even more verbose output, which can be very helpful if the standard output is not providing enough information about errors.


6.3 Tomcat Logs

* Log overview
      Once you have Sakai installed, configured and started, you can monitor Sakai by watching the logs. The log level for the standard Sakai source code and the demo is set to show info and warnings only. If you need to see more information, you can change the logging level to enable debug messages as well for all of Sakai or merely specific components. Sakai uses log4j for logging: see the log4j documentation for more information about how to configure it. Watch for the WARN: messages. There are going to be some "normal" ones at startup, and some will likely slip by at runtime, but any warning is potentially something you might want to check out.
* Finding the Logs
      On Mac and *nix systems the most important log is catalina.out in $CATALINA_HOME/logs. On Windows systems the situation is a little more complicated. The logs are also stored in $CATALINA_HOME\logs, but there are more of them:
          o jakarta_service_%date%.log
          o admin.%date%.log
          o catalina.%date%.log
          o host-manager.%date%.log
          o localhost.%date%.log
          o manager.%date%.log
      The Windows equivalent of catalina.out is the catalina.%date%.log, so focus on that one.
* Watching logs at startup
      Tomcat can take a long time to load the Sakai application, and it's often a good idea to watch the log in real-time as Tomcat comes up. You can watch for errors, and it will also allow you to be sure when Tomcat is done starting up. To do so on Mac or *nix systems you can run the command:

      tail -f $CATALINA_HOME/logs/catalina.out

      You can achieve something similar in Windows with Cygwin or other utilities.
* Log Configuration
      To change the logging for Sakai, you need to change Sakai source code and re-deploy sakai. The file you need to change is:

      sakai-src/kernel/log-configure/src/conf/log4j.properties

      The section that pertains to Sakai as distributed is:

      log4j.logger.org.sakaiproject=INFO

      To turn on debug logging for all of Sakai, change that to:

      log4j.logger.org.sakaiproject=DEBUG

      To turn on debugging only for one part of Sakai, add a line like the one below:

      log4j.logger.org.sakaiproject=INFO
      log4j.logger.org.sakaiproject.component.framework.sql.BasicSqlService=DEBUG

      This, for example, will leave most of Sakai at INFO, but the legacy SQL service will generate DEBUG level messages.
* Other logs
      The SMTP server logs from Sakai will be written to the $CATALINA_HOME/sakai/logs directory.





