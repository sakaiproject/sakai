SOURCE INSTALLATION GUIDE


Source Installation Introduction:

The Sakai "Source" installation includes all the source code required for fully customizing, configuring, and building Sakai.  This is the recommended installation type for any production installation.  The prerequisites include Java, Tomcat, Maven, and most likely another database (like the demo, the source install uses HSQLDB out of the box, but this is not recommended for production.  You may configure Sakai for either Oracle or MySQL - see below for details).



TABLE OF CONTENTS

   1. Set Up Build Environment 
	1.1  Verify Java installation (and install if necessary)
   	1.2  Set environment variables
   	1.3  Download and install Tomcat 5.5
   	1.4  Tomcat configuration
   	1.5  Download and install Maven
   	1.6  Configure Maven
   	1.7  Test Maven

   2. Deploy Sakai 
	2.1  Source Download source
   	2.2  Unpack source
   	2.3  Deploy Sakai

   3. Post-Installation Configuration 
	3.1  Create sakai folder for properties files
   	3.2  The sakai.properties file
   	3.3  Personalizing Sakai
   	3.4  Email configuration
   	3.5  Test Sakai
   	3.6  JVM tuning (optional)

   4. Database Configuration 
	4.1  Deploy drivers
   	4.2  Create Sakai database and user
   	4.3  The placeholder.properties file
   	4.4  sakai.properties database properties
   	4.5  Special database configuration (including upgrade issues)

*NOTE: For configuration issues that are not part of a basic, initial installation, but likely concerns for any institution working toward production, see the online Production Configuration Guide on the 2.0.1 release site.



1.  SET UP BUILD ENVIRONMENT


1.1  Verify Java Installation (and install if needed)

      Check to see if you have Java 1.4 installed on your system by running the following commands:
      
      Windows: 	
C:\> java -version
Java(TM) 2 Runtime Environment, Standard Edition (build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

      Macintosh/Linux/Solaris: 	
$ java -version
java version "1.4.2_04"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

      *NOTE: It is possible, but not recommended, to use Java 1.5

      If you do not have the correct version of Java installed, install the Java Software Development Kit (SDK) from http://java.sun.com/j2se/1.4.2/download.html

      * Note: Java is already installed on Mac OS/X computers.


1.2 Set Environment variables

      In the UNIX operating systems, you typically modify a startup file like ~/.bash_login to set and export these shell variables. In Windows XP, you go to

      Start -> Control Panel -> System -> Advanced -> Environment Variables -> New

      in order to create or modify the named variables.

      Set the JAVA_HOME environment variable to point to the base directory of your Java installation. This will enable Tomcat to find the right Java installation automatically. This may already be set up for you by your Java SDK installation.

      Sample Commands for Windows: 	
Set the environment variable JAVA_HOME to "C:\j2sdk1.4.2_04" (do not include the quotes)

      Mac: 	
export JAVA_HOME=/Library/Java/Home

      Linux: 	
export JAVA_HOME=/usr/local/java-current

      Extend the PATH variable so as to include the Java commands.

      Sample Commands for Windows: 	
Append the string ";C:\j2sdk1.4.2_04\bin" (include the semicolon but not the quotes) to the end of the System variable named Path.

      Mac: not necessary

      *nix: 	
export PATH=$PATH:$JAVA_HOME/bin/

      You should test that these variables are set correctly.  In both Windows XP and *nix operating systems you can simply start a new shell and type the 'set' command to see your environment variables.

      Once the variables are set properly, run the java -version command once more as a final check.

      You can also look for the Sun Java Installation Instructions page at the Java web site for further details.


1.3  Download and Install Tomcat 5.5 binary (and compatibility patch)

      The latest stable version of Tomcat 5.5 (currently 5.5.9) can be downloaded as a binary from:

      http://jakarta.apache.org/site/downloads/downloads_tomcat-5.cgi

      If you're using Java 1.4 (which is recommended) be sure to also download the 5.5.9_Compat archive.

      Choose a location to install Tomcat, and unpack both the Tomcat binary and the compatibility patch there in the same location. The compatibility patch will simply overlay your Tomcat directories with the appropriate files. From this point forward these instructions will refer to the top-level Tomcat directory (e.g. jakarta-tomcat-5.5.9) as $CATALINA_HOME. You may set this as an environment variable for convenience sake, but this is not required.  

	Make sure that you have write permissions to the Tomcat files and directories.


1.4  Tomcat Configuration

      Sakai supports UTF-8 allowing for non-Roman characters. In order for all of the Sakai tools to properly handle UTF-8 non-Roman characters, Tomcat must be configured to accept UTF-8 URLs. UTF-8 is the offical standard for URLs, however Tomcat ships with ISO-8859-1 as the default URL encoding. To change this setting, edit $CATALINA_HOME/conf/server.xml. Add the attribute URIEncoding="UTF-8" to the element. For example:

<Connector
port="8080" maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
enableLookups="false" redirectPort="8443" acceptCount="100"
debug="0" connectionTimeout="20000" disableUploadTimeout="true"
URIEncoding="UTF-8"/>

      If you want to run Tomcat on different ports than the defaults, this would also be a good time to make those changes in the server.xml file. See Tomcat documentation for more details.

      If you're going to run Tomcat in isolation (i.e. if you're not going to configure it to run with Apache) then you'll want to make a further minor Tomcat change that may spare some confusion later. In order to make sure that entering the URL to your Sakai server will redirect to the Sakai application appropriately, you'll need to copy an index.html file to webapps/ROOT. If you don't, then you'll have to append '/portal' to the end of your URL in the browser when you wish to reach the Sakai application (this is because the ROOT webapp is still Tomcat's default webapp). This index.html file should look like this:

<html>
<head>
<meta http-equiv="refresh" content="0;url=/portal">
</head>
<body>
redirecting to /portal ...
</body>
</html>

      If you are going to connect Tomcat with Apache, you can handle this as a matter of Apache configuration.


1.5  Download Maven

      Maven is the build tool used by Sakai, and the latest stable release (currently 1.0.2) can be downloaded from:

      http://maven.apache.org/start/download.html

      Choose a location to install Maven, and unpack the archive there. You will have a top-level directory named maven-<VERSION> (e.g. maven-1.0.2).


1.6  Configure Maven

      To use Maven you'll need to set two environment variables and create a local repository using a script provided by Maven.

      First, define the MAVEN_HOME environment variable which is the directory where you just unpacked the Maven install archive. You will also need to add MAVEN_HOME/bin to your path so that you can run the scripts provided with Maven.

      Sample Commands for Windows: 	
Create a new MAVEN_HOME environment variable to "C:\maven-1.0.2"
Append to the PATH variable ";C:\maven-1.0.2\bin"

      Mac/*nix: 	
export MAVEN_HOME=/usr/local/maven
export PATH=$PATH:$MAVEN_HOME/bin

      Next, you should create your local repository by running the following command:

      Windows: 	
%MAVEN_HOME%\bin\install_repo.bat %HOMEDRIVE%%HOMEPATH%\.maven\repository

      Mac/*nix: 	
$MAVEN_HOME/bin/install_repo.sh $HOME/.maven/repository

      Finally, you'll need to create a properties file in your home directory which will configure Maven for your Sakai build. Simply create a new text file with the filename build.properties in your home directory, and paste in the following contents:

maven.repo.remote =http://www.ibiblio.org/maven/,http://cvs.sakaiproject.org/maven/
maven.tomcat.home=/usr/local/tomcat/

      *WARNING: Do not omit the trailing slashes, and be sure to change the value of maven.tomcat.home to match the path to your Tomcat installation

      If you are running on Windows, special care is needed in identifying your tomcat home. Maven wants Unix-style forward slashes, "/", and is confused by Windows-style backslashes "\". If you have your tomcat located in "c:\tomcat", you need to identify it like this:

maven.tomcat.home = c:/tomcat/


1.7.  Test Maven

      To confirm that you can start Maven, run the command:

	maven -v

      This should start maven, and cause it to report its version.

*NOTE: See the maven.pdf resource posted to the Resources area of the sakai-dev site on http://collab.sakaiproject.org for a more thorough examination of how maven is used with Sakai.



2.  DEPLOY SAKAI SOURCE


2.1  Download Source

      Download the Sakai Source archive from:

      http://cvs.sakaiproject.org/release/2.0.1/


2.2  Unpack Source Archive

      Choose a location to unpack the Sakai source (we recommend somewhere in your home directory or one of its subdirectories), and when you unpack it you will see a directory named sakai-src.


2.3  Run Maven

      From within sakai-src, run the command:

	maven sakai

      This will run for a few minutes with very verbose output. It will download any necessary jars into the local repository (do not be too concerned with reported jar download failures for your first build.  This is normal for a new install, and maven will later build many of the required jars.  Subsequent builds should not report these failures), compile the Sakai code, and deploy Sakai to Tomcat in the form of .war files in the $CATALINA_HOME/webapps directory. If it completes with the message BUILD SUCCESSFUL, you should be able to move on to the next step. If you are greeted with the report BUILD FAILED read the accompanying error message carefully to troubleshoot (see the Troubleshooting Guide).

      You will probably be able to start up Tomcat and run Sakai with the default configuration at this point, but it might be better to take care of some basic configuration first.



3.  POST-INSTALLATION CONFIGURATION

 	This set of instructions covers all the basic configuration details apart from database configuration, which deserves its own category (below).  For more optional configurations that may be significant for institutions working toward production (including enterprise integration) see the online Production Configuration Guide on the 2.0.1 release site.

      
3.1  Create sakai folder for properties files

      Sakai runs with a default set of properties for its various components.  To override them you'll want to specify them in a sakai.properties file which should be located in $CATALINA_HOME/sakai by default.  This directory is not created by maven, so you'll have to do so manually.  Once this directory is created, there are several *.properties files you can place inside which can override default properties. 


3.2  The sakai.properties file

      The main configuration file for Sakai is called sakai.properties, and you can either create it from scratch or copy in a known working copy. A sample sakai.properties file which self-documents many of the standard properties in its comments can be found in:

sakai-src/docs/sakai.properties

sakai.properties can define two different types of values.  The first type is those values that are made available to the running code from the Sakai configuration service.  A line of sakai.properties sets this sort of value if it has the form: 

name=value

The second type of sakai.properties value overrides the configuration defaults of individual sakai components.  These defaults are set in the components.xml file of any particular component, and so configuration can be achieved by also editing a large number of components.xml files, but it's a best practice - for reasons that should be obvious - to keep all configuration changes in sakai.properties.  Override values have the form:

name@component-name=value

New values can be freely added to the sakai.properties file, since any component property can in principle be overridden here, and so any sample sakai.properties will show only a small fraction of all the possible values.


3.3  Personalizing Sakai

      Sakai has a number of places where your institution and service names, and the host name of your service are used. These can be configured in sakai.properties:

# identify your application server with a short name, unique among the servers in your cluster.
# choose a server id even if you are running a single app server
serverId=localhost

# the URL to the server, including transport, DNS name, and port, if any
serverUrl=http://localhost:8080

# the DNS name of the server
serverName=localhost

# the URL to send folks to after they logout
loggedOutUrl=http://localhost:8080/portal

# some fill-ins for the css/vm ui (Worksite Setup, Digest Service, Email notification, Worksite Setup, Contact Support, Portal)
ui.institution = Your Institution
ui.service = SakaiOrWhatever

#copyright text to appear in the bottom area of each web page.
bottom.copyrighttext=(c) 2003, 2004, 2005 sakaiproject.org. All rights reserved.

# links placed on the bottom nav - set the .count to the number of items, then add each item
bottomnav.count = 2
bottomnav.1 = <a href="https://localhost/portal/site/!gateway">Gateway</a>
bottomnav.2 = <a href="http://sakaiproject.org/cms" target="_blank">The Sakai Project</a>

      You can add more or other links to the bottom navigation bar by setting the proper bottomnav.count value and adding bottomnav.N values (1 through the number of links).


3.4  Email Configuration

      Sakai needs to be set up for two email functions; receiving email sent to Sakai sites, and sending out email notifications.

      For sending mail Sakai needs the address (name or IP) of an SMTP server that will accept mail from Sakai. This needs to be set in your sakai.properties file:

smtp@org.sakaiproject.service.framework.email.EmailService=some.smtp.org

      To enable Sakai to receive mail there are a few settings needed in the sakai.properties file:

# dns addresses used for incoming email
smtp.dns.1 = 255.255.255.1
smtp.dns.2 = 255.255.255.2

# SMTP port on which our SMTP server runs. Default is 25. Recommend running on 8025, and using a
standard mailer on 25 to forward mail to Sakai.
smtp.port = 25

# flag to enable or disable our SMTP server for incoming email (true | false)
smtp.enabled = true

      To disable the SMTP server for incoming email, use this in sakai.properties:

smtp.enabled = false

      Sakai's SMTP server is 'James,' and to run with the above configuration which runs James on the standard SMTP port 25 you must be running with admin privileges. Most production folks don't want to let Tomcat run with that, and would rather run a standard mailer like postfix on port 25 and configure it to forward requests to Sakai. You might also already have a mailer service running on port 25 (Linux usually has it running by default), and so you'd want to change the James port simply to avoid a conflict. You'll typically want to run James on another, non-restricted port, then.  For example:

smtp.port = 8025

      If you do wish to run James on port 25 for any reason, you should be aware that James will try to handle outgoing mail if it runs on port 25, even though it shouldn't. There is a way around this. If you pass Tomcat the system property

-Dmail.smtp.host=<host ip>

      This will allow James to run on port 25 while still delegating outgoing mail to be handled by an external SMTP host. You can also accomplish this by setting it in the CATALINA_OPTS environment variable.


3.5  Test Sakai

      Sakai has not at this point been fully configured, but you should now be able to bring it up as a working web application by starting Tomcat, and it can be helpful at this point to know whether Sakai is working before you try to connect it to an external database, for example. Tomcat will take half a minute or so to start up, depending on the speed of your machine, and it's a good idea to watch the Tomcat log as it comes up to catch any errors (see the Troubleshooting Guide).

      From $CATALINA_HOME you can run the following startup commands:
      
      Windows: 	
bin\catalina start

      Mac/*nix: 	
bin/catalina.sh start

      Once Tomcat has loaded the Sakai application (again, this can take a minute or so) point your browser to http://localhost:8080/portal. If the gateway page does not come up, check the Tomcat logs (see the Troubleshooting Guide).  If the gateway page does come up, log in with the default admin account:  username = admin, password = admin.  If you can log in without errors you should be able to stop Tomcat and proceed with further configuring your Sakai installation, if you need to.

      Windows: 	
bin\catalina stop

      Mac/*nix: 	
bin/catalina.sh stop


3.6  JVM Tuning (optional)

      The Java virtual machine's configuration is very important to tune for best performance in any production environment. Unfortunately this is something of a black art, which is why it is here only labeled as optional and it comes with a caution. We recommend that you take the time to experiment with different memory and garbage collection settings and see what works best in your environment.  The following details are offered only as samples or suggestions: before making any such changes to a production system please consult a systems administrator or local Java guru, if you're not such a person.

      The standard way to control the JVM options when Tomcat starts up is to have an environment variable JAVA_OPTS defined with JVM startup options.  One sample value might be:

JAVA_OPTS=-server -Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

      This is a fairly good starting point: it selects server mode, turns on garbage collection details, and sets the memory. We have found the best results when you set the min and max memory to the same values. 512 megs is not too much memory for Sakai; 1 gig is even better.

      Tomcat will see this environment variable and use it. Instead of putting this in an environment variable, you might modify the $CATALINA_HOME/bin/catalina.bat or $CATALINA_HOME/bin/catalina.sh file to manually set these values.



4.  DATABASE CONFIGURATION


4.1  Deploy Drivers

      The supported production-grade databases include MySQL 4.1+ and Oracle 9i+. The version of the JDBC driver (or connector) is also important. For MySQL a 3.1.10 (or higher) connector should be used, while for Oracle the 10g driver should be used, even if the database is version 9i. The 9i driver will not be sufficient.

      You need to have appropriate JDBC drivers for your database installed in your $CATALINA_HOME/common/lib directory. For Oracle and MySQL drivers are available here:

          * Oracle: http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/htdocs/jdbc101020.html

          * MySQL: http://dev.mysql.com/downloads/connector/j/3.1.html


4.2  Create Sakai Database and User

      A Sakai database and privileged user must be prepared for Sakai's use. Consult your database documentation for details, but here are sample commands for MySQL:

C:\sakai\reference\sql\legacy\mysql\>
mysql -u root -p
Enter password: ******
Welcome to the MySQL monitor. Commands end with ; or \g.

Your MySQL connection id is 51 to server version: 4.1.5-gamma-nt

Type 'help;' or '\h' for help. Type '\c' to clear the buffer.

mysql> create database sakai default character set utf8;
Query OK, 1 row affected (0.00 sec)

mysql> grant all on sakai.* to sakaiuser@'localhost' identified
by 'sakaipassword';
Query OK, 0 rows affected (0.00 sec)

mysql> grant all on sakai.* to sakaiuser@'127.0.0.1' identified
by 'sakaipassword';
Query OK, 0 rows affected (0.00 sec)

mysql> quit 


4.3  The placeholder.properties File

      The placeholder properties are all related to database technology and behavior, and as with the sakai.properties file, placeholder.properties belongs in the $CATALINA_HOME/sakai directory. These must be set properly for the database you are using. See the sample placeholder.properties file in the source code:

sakai-src/kernel/component/src/config/org/sakaiproject/config/placeholder.properties

      To select MySQL as your database technology the placeholder.properties must include:

hibernate.dialect=net.sf.hibernate.dialect.MySQLDialect
auto.ddl=true
jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_COMMITTED

      To select Oracle as your database technology the placeholder.properties must include:

hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
auto.ddl=true
jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_COMMITTED


4.4  sakai.properties Database Properties

      There are settings in sakai.properties that also define the database technology and connection information. Appropriate sakai.properties settings for Oracle and MySQL, respectively, are listed below (although some pieces will of course need to be altered appropriately for your installation):

          * Oracle:
vendor@org.sakaiproject.service.framework.sql.SqlService=oracle
driverClassName@javax.sql.BaseDataSource=oracle.jdbc.driver.OracleDriver
url@javax.sql.BaseDataSource=jdbc:oracle:thin:@your.oracle.dns:1521:SID
username@javax.sql.BaseDataSource=[database user name]
password@javax.sql.BaseDataSource=[password]
validationQuery@javax.sql.BaseDataSource=select 1 from DUAL 

          * MySQL:
vendor@org.sakaiproject.service.framework.sql.SqlService=mysql
driverClassName@javax.sql.BaseDataSource=com.mysql.jdbc.Driver
url@javax.sql.BaseDataSource=jdbc:mysql://127.0.0.1:3306/sakai?useUnicode=true&characterEncoding=UTF-8
username@javax.sql.BaseDataSource=[database user name]
password@javax.sql.BaseDataSource=[password]
validationQuery@javax.sql.BaseDataSource=select 1 from DUAL


4.5  Special Database Configuration (including upgrade issues)

      * Database DDL and Seed Data Configuration:

      Each database you use for a Sakai installation needs to have certain tables and seed data. Sakai will automatically create these tables if the auto.ddl feature is enabled. This is fine for most situations, and is used when you run Sakai from source or SVN, and in the demo.

      To enable auto.ddl (it is also enabled by default) have this setting in the placeholder.properties file:

auto.ddl=true

      In certain situations, you may want to manually create and populate the tables (or your DBA will want to do this). In these cases, turn off the auto.ddl feature. Disable auto.ddl by placing this in the placeholder.properties file:

auto.ddl=false

      The sql files to use to create tables are found distributed throughout the source code. Each Sakai component can have one. The convention used in the code is to place them under src/sql/folder. There will be a separate file for Oracle, MySQL, and hsqldb, each located under a separate folder. For example, the sql to create and populate the content hosting tables for MySQL is found here:

sakai-src/legacy/component/src/sql/mysql/sakai_content.sql

      auto.ddl can be safely left turned on even if you earlier switched it off to manually create tables, for it will not do anything if the needed tables already exist.


      * Samigo Schema Alteration:

      If you're running Oracle or are upgrading from a previous Sakai version you should check the datatype of the "MEDIA" column in the SAM_MEDIA_T table.

      The correct types for each database are:
          * HSQLDB --> varbinary
          * Oracle --> blob
          * MySQL --> longblob

      If you need to change this type for your database, this will also involve finding the primary key constraint, dropping it and then recreating it. Contact your local DBA for further information on making this change.

      Below is some sample Oracle SQLplus output to better illustrate (SYS_C0064435 is the example constraint; replace it with yours):

SQL> alter table SAM_MEDIA_T modify MEDIA BLOB;

Table altered.

SQL> select constraint_name from user_constraints where table_name='SAM_MEDIA_T' and CONSTRAINT_TYPE='P';

CONSTRAINT_NAME
------------------------------
SYS_C0064435

SQL> alter table sam_media_t drop constraint SYS_C0064435;

Table altered.

SQL> alter table SAM_MEDIA_T add constraint SYS_C0064435 primary key (MEDIAID);

Table altered.

SQL> desc SAM_MEDIA_T;

[table with BLOB type]

SQL> select constraint_name from user_constraints where table_name='SAM_MEDIA_T' and CONSTRAINT_TYPE='P';

CONSTRAINT_NAME
------------------------------
SYS_C0064435

SQL> commit;

Commit complete.

	* Samigo upgrade patch for MySQL:

If you have an existing MySQL instance that you installed for sakai 2.0.0, please run the following script to update your existing tables.

> mysql -u username -p schemaname < $HOME/sakai-src/sam/src/mysql/samigo-2.0.0.patch1.sql --force

This will change the column type of date columns to 'datetime'.

	* 1.5 to 2.0 DB conversion script

A script included with the source release will need to be run on a Sakai 1.5 DB to perform the schema alterations necessary for Sakai 2.0. You can find this script in sakai-src/docs/sakai_1_5-2_0_conversion.sql. Note that the Oracle parts of this script have been commented out because only the MySQL statements have been tested.
