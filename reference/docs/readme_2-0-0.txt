Sakai 2.0.0 readme.txt

This readme contains installation, customization and configuration information for Sakai version 2.0.0.

Additional documentation on Sakai can be seen on Sakai Collab:

    http://collab.sakaiproject.org

in the Sakai Development site.

1 Getting Sakai
  1.1 Packages
   1.1.1 Demo
   1.1.2 Source
   1.1.3 Binary
  1.2 CVS
2 Configuring Sakai
  2.1 Configuration Files
  2.1.1 placeholder properties
  2.1.2 sakai.properties
  2.2 Configuration Cookbook
   2.2.1 HSQLDB Configurations
   2.2.2 MySql Configurations
   2.2.3 Oracle Configurations
   2.2.4 Database DDL and Seed Data Configuration
   2.2.5 Email Setup
   2.2.6 Personalizing your Sakai
   2.2.7 File Based Content Hosting
   2.2.8 How to CASify Sakai
  2.3 Tomcat configuration
    2.3.1 Character Encoding
    2.3.2 Other Tomcat Configuration
  2.4 Java Configuration
3 Sakai In Action
  3.1 Log4J configuration
  3.2 Other log files
4 Integrating Sakai
  4.1 Provider Registration
  4.2 Changing The Provider Module's Components
  4.3 Replacing the Provider Module
  4.4 Providers and Single-Signon
5 Sakai and SSL / HTTPS
6 Apache - Tomcat
  6.1 Apache configuration
  6.2 Tomcat configuration
7 Clustering Sakai
  7.1 Configuration your Cluster
  7.2 Starting and Stopping your Cluster
  7.3 Monitoring your Cluster
  7.4 Changing the Cluster

TODO:
* db maintenance - locks, ghosts, events



1. Getting Sakai

Sakai is distributed in two forms; in pre-packaged archives, and from anonymous CVS access.

1.1 Packages

The packaged distributions of Sakai include:

    - Sakai demo
    - Sakai source
    - Sakai binary

The packaged archive distributions of Sakai come in two formats; a .zip file for use on Microsoft Windows operating systems, and a .tar.gz file for use on unix, linux, other *nix, and Macintosh OSX operating systems.  The file permissions and text file line ends are tuned for these different systems in the different distributions.

1.1.1 Demo

Sakai demo includes a fully built and configured Sakai, along with Jakarta Tomcat 5.5.9 also configured for Sakai, in a ready-to-run package.  This is the quickest way to get started with Sakai for demonstration and evaluation.

Get the demo from

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-demo_2-0-0.zip

or

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-demo_2-0-0.tar.gz

Demo requires a Java version 1.4 runtime environment, and includes java installation instructions in the file java_readme.txt.

When you unpack the demo archive appropriate for your operating system, you will have a folder called sakai-demo.  See the demo_readme.txt file for startup and shutdown instructions, and other information about the demo.

Demo uses the Hypersonic Sql (HSQL) package to store data, in files in

    sakai-demo/sakai/db/sakai.db.*

Objects created and modified in runs of the Sakai demo will persist through server restarts.

HSQL is not appropriate for running Sakai in any sort of production or even medium to large scale demonstration and evaluation environments.  For these, we recommend MySQL or Oracle.

1.1.2 Source

Sakai source is available from

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-src_2-0-0.zip

or

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-src_2-0-0.tar.gz

When you unpack the source archive, you will get a folder called sakai-src.  All the source code for the release is included.

To work with the Sakai source, you need a java development environment, and the Maven project management system.  We recommend using the Eclipse Java IDE, and include Eclipse project files for Sakai in the source distribution.

For details about how to work with the Sakai source code and Maven, refer to the document on Sakai Collab:

    http://collab.sakaiproject.org/access/content/group/1097928811887-22636/Architecture%20Docs/Sakai%202/sakai_maven.pdf

1.1.3 Binary

The binary distribution of Sakai includes the pre-built Sakai source, just like in the demo, but does not include the Tomcat files or Sakai configuration files.  The binary is organized as an overlay to the standard Tomcat distribution - the files are just the additional files we need to add to Tomcat in the appropriate folders.

The binary distribution is useful if you want the stock Sakai, but want to use your own Tomcat and sakai configuration files.

Sakai binary is available from

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-binary_2-0-0.zip

or

    http://cvs.sakaiproject.org/releases/2.0.0/sakai-binary_2-0-0.tar.gz

To use the binary distribution, setup your Tomcat, unpack the Sakai binary archive while in the root tomcat folder, and then add a sakai folder with your desired Sakai configuration.

1.2 CVS

Sakai source is also available from our CVS server, and supports anonymous access.  You can get the source from this release by checking out the tag named

    sakai_2-0-0

Other tags are available, as well as the latest checkins in the cvs head.

You need the CVS system properly installed, as well as SSH, and CVS configured to use SSH.  You also need to install and configure the anonymous CVS SSH key.  More information about this process, and access to the key, are found on the cvs.sakaiproject.org site:

    http://cvs.sakaiproject.org/anoncvs/readme.txt
    http://cvs.sakaiproject.org/anoncvs/sakai_anoncvs_dsa

You will also need Maven, Java, and appropriate development tools work with with the Sakai source.  See the Sakai Collab document for more information about working with Sakai source and Maven:

    http://collab.sakaiproject.org/access/content/group/1097928811887-22636/Architecture%20Docs/Sakai%202/sakai_maven.pdf

2. Configuring Sakai

When you want to go beyond using the demo configuration of Sakai, you need to create your own custom Sakai configuration.  The configuration of Sakai lets you change:

    - the database you use
    - the server id for each of your application servers
    - the URLs of your Sakai service
    - the skin and appearance setup
    - control of optional Sakai features
    - email configuration
    - Sakai service identity information

Any configuration of all of the Sakai components can be overridden by your Sakai configuration.

You may also want to configure Tomcat and the Java virtual machine for optimal Sakai operation and performance.

For more information about the Sakai configuration process, see the document on Sakai Collab:

    http://collab.sakaiproject.org/access/content/group/1097928811887-22636/Architecture%20Docs/Sakai%202/sakai_config.pdf

2.1 Configuration Files

Sakai configuration is controlled by files in the sakai.home directory, which defaults to the sakai folder in the root of your Tomcat.  Configuration is usually done with two files:

    - placeholder.properties
    - sakai.properties

placeholder.properties defined the placeholder values to use throughout the Sakai system configuration.  These are the values which are referred to by the pattern ${name}.  There are a few placeholder values used in the Sakai distribution - you can add others in your Sakai configuration files.

If you need to change the default values of the placeholders used in Sakai, or if you need to add new placeholder names, you need a placeholder.properties file.  This file must contain the complete set of placeholder values, those from Sakai and those you added.  If you don't provide a placeholder.properties, defaults for the Sakai placeholders will be used:

    hibernate.dialect=net.sf.hibernate.dialect.HSQLDialect
    auto.ddl=true
    jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_UNCOMMITTED

sakai.properties contains the rest of the configuration values that do not belong in the placeholder.properties.  If there is no sakai.properties, default values for Sakai will be used.

The placeholder value:

    ${sakai.home}

is automatically pre-defined and can be used in the sakai.properties files to refer to the sakai.home location.

2.1.1 placeholder properties

The placeholder properties are all related to the database technology and behavior.  These must be set properly for the database you are using.  There are settings in sakai.properties that also define the database technology and connection information.

See the demo file:

    sakai-demo/sakai/sakai.properties

which contains comments about the placeholder.properties values (there is no need for the placeholder.properties in the demo), or the source code file:

    sakai-src/kernel/component/src/config/org/sakaiproject/config/placeholder.properties

for details about the settings and values you can control in the placeholder.properties file.

2.1.2 sakai.properties

sakai.properties defines a set of values that override configuration defaults of sakai components, and values that are made available to the running code from the Sakai configuration service.  Override values have the form:

    name@component-name=value

and configuration service values have the form:

    name=value

These can be mixed in the sakai.properties file.

For examples of typical settings, see the example file:

    sakai-src/docs/sakai.properties

included in the source release.

2.2 Configuration Cookbook

There are some configurations that will be common to Sakai installers.  These are described in this section.  Make sure you get the settings in the right files, between placeholder.properties and sakai.properties.

2.2.1 HSQLDB Configurations

Hypersonic SQL is a java based in-memory SQL database.  It is very convenient for development and simple demos.  It should not be used in production or with any sort of wider use demo or evaluation; use MySql or Oracle for that.

    http://hsqldb.sourceforge.net/

HSQLDB supports two modes of operation that we use; an in-memory only mode, that does not save state between server runs, and a disk based mode that does.  Sakai source from CVS or the source distribution is set to use the in-memory mode; the demo is set to use the disk based.

To select HSQLDB as your database technology, the placeholder.properties must include:

    hibernate.dialect=net.sf.hibernate.dialect.HSQLDialect
    auto.ddl=true
    jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_UNCOMMITTED

and your sakai.properties must include:

    vendor@org.sakaiproject.service.framework.sql.SqlService=hsqldb
    driverClassName@javax.sql.BaseDataSource=org.hsqldb.jdbcDriver
    username@javax.sql.BaseDataSource=sa
    password@javax.sql.BaseDataSource=
    validationQuery@javax.sql.BaseDataSource=select 1 from SYSTEM_USERS

Also in the sakai.properties file, for the in memory option, add this line:

    url@javax.sql.BaseDataSource=jdbc:hsqldb:.

Or for the disk option, storing the database in sakai.home, use this line:

    url@javax.sql.BaseDataSource=jdbc:hsqldb:${sakai.home}/db/sakai.db

2.2.2 MySql Configurations

MySQL can handle a demo, evaluation, or production instance of Sakai.

    http://www.mysql.com/

We support MySQL 4.1.  Make sure you have the appropriate MySQL driver available to Sakai - we usually place it in tomcat/common/lib.

You must install it, create a user for Sakai, and grant it appropriate privileges for table creation and data manipulation.

To select MySQL as your database technology, the placeholder.properties must include:

    hibernate.dialect=net.sf.hibernate.dialect.MySQLDialect
    auto.ddl=true
    jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_COMMITTED

and your sakai.properties must include:

    vendor@org.sakaiproject.service.framework.sql.SqlService=mysql
    driverClassName@javax.sql.BaseDataSource=com.mysql.jdbc.Driver
    url@javax.sql.BaseDataSource=jdbc:mysql://127.0.0.1:3306/sakai?useUnicode=true&amp;characterEncoding=UTF-8
    username@javax.sql.BaseDataSource=<database user name>
    password@javax.sql.BaseDataSource=<password>
    validationQuery@javax.sql.BaseDataSource=select 1 from DUAL

Adjust these to include the proper values for your setup.

2.2.3 Oracle Configurations

Oracle can handle a demo, evaluation, or production instance of Sakai.

http://www.oracle.com

You must install it, create a user for Sakai, and grant it appropriate privileges for table creation and data manipulation.

We support Oracle 9i and 10g.  Make sure you have the appropriate oracle driver available to Sakai - we usually place it in tomcat/common/lib.

To select Oracle as your database technology, the placeholder.properties must include:

    hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
    auto.ddl=true
    jdbc.defaultTransactionIsolation=java.sql.Connection.TRANSACTION_READ_COMMITTED

and your sakai.properties must include:

    vendor@org.sakaiproject.service.framework.sql.SqlService=oracle
    driverClassName@javax.sql.BaseDataSource=oracle.jdbc.driver.OracleDriver
    url@javax.sql.BaseDataSource=jdbc:oracle:thin:@your.oracle.dns:1521:SID
    username@javax.sql.BaseDataSource=<database user name>
    password@javax.sql.BaseDataSource=<password>
    validationQuery@javax.sql.BaseDataSource=select 1 from DUAL

Adjust these to include the proper values for your setup.

2.2.4 Database DDL and Seed Data Configuration

Each database you use for a Sakai installation needs to have certain tables and seed data.  Sakai will automatically create these tables if the auto.ddl feature is enabled.  This is fine for most situations, and is used when you run Sakai from source or CVS, and in the demo.

Enable auto.ddl by creating a complete placeholder.properties file, and have this for auto.ddl:

    auto.ddl=true

In certain situations, you may want to manually create and populate the tables (or your DBA will want to do this).  In these cases, turn off the auto.ddl feature.  Disable auto.ddl by creating a complete placeholder.properties file, and have this for auto.ddl:

    auto.ddl=false

The sql files to use to create tables are found distributed throughout the source code.  Each Sakai component can have one.  The convention used in the code is to place them under src/sql/ folder.  There will be a separate file for Oracle, MySQL, and hsqldb, each located under a separate folder.  For example, the sql to create and populate the content hosting tables for MySQL is found here:

    sakai-src/legacy/component/src/sql/mysql/sakai_content.sql

auto.ddl can be left turned on, and will not do anything if the tables it needs to create already exist.

2.2.5 Email Setup

Sakai needs setup for two email functions; receiving email sent to Sakai sites, and sending out email notifications.

For sending mail, Sakai needs the address (name or IP) of an SMTP server that will accept mail from Sakai.  This needs to set in your sakai.properties file:

    smtp@org.sakaiproject.service.framework.email.EmailService=some.smtp.org

To enable Sakai to receive mail, there are a few settings needed in the sakai.properties file:

    # dns addresses used for incoming email
    smtp.dns.1 = 255.255.255.1
    smtp.dns.2 = 255.255.255.2

    # SMTP port on which our SMTP server runs.  Default is 25.  Recommend running on 8025, and using a standard mailer on 25 to forward mail to Sakai.
    smtp.port = 25

    # flag to enable or disable our SMTP server for incoming email (true | false)
    smtp.enabled = true

To disable the SMTP server for incoming email, use this in sakai.properties:

    smtp.enabled = false

To run with the above configuration, which uses the standard SMTP port 25, you must be running with admin privileges.  Most production folks don't want to let Tomcat run with that, and would rather run a standard mailer like postfix on port 25, and configure it to forward requests to Sakai.  Sakai can then run SMTP on port 8025, which is not a restricted port.  You can setup Sakai to work this way with this in the sakai.properties:

    smtp.port = 8025

2.2.6 Personalizing your Sakai

Sakai has a number of places where your institution and service names, and the host name of your service are used.  These are configured in sakai.properties:

    # identify your application server with a short name, unique among the servers in your cluster.
    # choose a server id even if you are running a single app server
    serverId=localhost

    # the URL to the server, including transport, DNS name, and port, if any
    serverUrl=http://localhost:8080

    # the DNS name of the server
    serverName=localhost

    # the URL to send folks to after they logout
    loggedOutUrl=http://portal

    # some fill-ins for the css/vm ui (Worksite Setup, Digest Service, Email notification, Worksite Setup, Contact Support, Portal)
    ui.institution = Your Institution
    ui.service = SakaiOrWhatever

    #copyright text to appear in the bottom area of each web page.
    bottom.copyrighttext=(c) 2003, 2004, 2005 sakaiproject.org. All rights reserved.

    # links placed on the bottom nav - set the .count to the number of items, then add each item
    bottomnav.count = 2
    bottomnav.1 = <a href="https://localhost/portal/site/!gateway">Gateway</a>
    bottomnav.2 = <a href="http://sakaiproject.org/cms" target="_blank">The Sakai Project</a>

You can add more or other links by setting the proper bottomnav.count value, and adding bottomnav.N values, 1 through the number of links.

2.2.7 File Based Content Hosting

Anytime you want to use the file system feature of content hosting, to store the content hosting resource body binary bytes in files in a file system instead of records in the database, you must

    1) configure your Sakai to do this, and

    2) run a conversion to bring your files out of your database and into the file system.

This conversion is needed even for a brand new database, since there are some resources shipped with the started Sakai db.

2.2.7.1 Configuration

The best place for configuring this is the sakai.properties file.

    # the file system root for content hosting's external stored files (default is null, i.e. store them in the db)
    bodyPath@org.sakaiproject.service.legacy.content.ContentHostingService =${sakai.home}content

Enable the above line, and point at the root folder for the files to be stored.

    # when storing content hosting's body bits in files, an optional set of folders just within the content.filesystem.root
    # to act as volumes to distribute the files among - a comma separate list of folders.  If left out, no volumes will be used.
    bodyVolumes@org.sakaiproject.service.legacy.content.ContentHostingService = v1,v2,v3

Enable the above line, and set the list of "volumes" for storage.  You can specify one or more volume names, comma separated on this line.  These are folders under the file system root.  Files will be distributed among these volumes.

If you are going to use multiple volume devices, you need to map them to these volume names that live "under" the root.  We have done this with our AFS file storage system at the University of Michigan.  If you are not using separate devices, then you can use any folder names for the volumes.  Provide at least one.

Files will be stored under each volume in a way so that there are not too many in any one folder.  The folder structure we use is:

    YYYY/DDD/HH/id, where YYYY=year, DDD=day of year, HH=hour of day, and the 1111...=an id-based file name

for example,

    2005/070/03/3223479379834-2343

or, using the above root and volumes, it might be:

    /usr/local/tomcat/sakai/content/v2/2005/070/03/3223479379834-2343

Note that the resource name and type is not at all encoded here.  The date/time used to form the file name is the date/time of file creation.

2.2.7.2 Conversion

Once you have your database setup, and your file values configured, and the file system read/write accessible to your application server, you need to configure your Sakai to run and convert.  Do this once, shutdown after conversion is complete, and re-configure Sakai to no longer do the conversion.  At that point you could remove the table CONTENT_RESOURCE_BODY_BINARY.

To convert, add this to your sakai.properties:

    convertToFile@org.sakaiproject.service.legacy.content.ContentHostingService = true

As soon as Sakai starts up, it will create a file system file for any content hosting resource that has a null "FILE_PATH" field.  This could take a long time, if you have many resources.  If the process dies mid way, you can restart it, and it will pick up about where it left off (since it does not update the "FILE_PATH" until it has written out the file).

2.2.7.3 Clustering

If you are running multiple application servers, it is vital that you choose a file system technology that is equally available to all the servers at once.  We use our campus AFS system for this.  Any network file system should work.  Make sure that the file system is mounted in the same place on all the application servers.

The file path is stored in the content hosting resource record.  This includes the path from the volume name all the way to the file name.  It does NOT include the root.  You can change the root location over time, or have it different on each app server, as long as the app server is properly configured.  But you can never change the volume names under the root (you could add, but you cannot remove).

2.2.8 How to CASify Sakai 2.0 (also works for other SSOs) (thanks to Seth Theriault slt@columbia.edu)

1) Install mod_cas (or its equivalent) under Apache.

2) Edit Apache's httpd.conf and add this:

    <Location /sakai-login/container>
        AuthType CAS
        Require valid-user
    </Location>

3) Edit Tomcat's server.xml

Disable Tomcat's container authentication by adding the following parameter to the JK2 connector configuration:

    tomcatAuthentication="false"

When you're done, the connector declaration should look something like this:

    <Connector port="8009" 
        enableLookups="false" redirectPort="8443" debug="0"
        tomcatAuthentication="false" URIEncoding="UTF-8"
        protocol="AJP/1.3" />

4) edit sakai.properties

    top.login = false
    container.auth = true

If you want to install a servlet filter, it needs to go in the sakai-login webapp and protect the /container path.

2.3 Tomcat configuration

Tomcat works mostly as distributed, but there is one required and some optional changes you might want to make.

2.3.1 Character Encoding

The required change enables internal characters.  The Tomcat shipped as part of the Sakai demo includes this change already - make this change to your Tomcat's 
conf/server.xml file if you are using a stock Tomcat:

    <!-- Define a non-SSL HTTP/1.1 Connector on port 8080 -->
    <Connector port="8080" maxHttpHeaderSize="8192" URIEncoding="UTF-8"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" redirectPort="8443" acceptCount="100"
               connectionTimeout="20000" disableUploadTimeout="true" />

The difference here is the addition of this:

    URIEncoding="UTF-8"

Any "Connector" element you use must have this for proper character processing in Sakai.

2.3.2 Other Tomcat Configuration

The other things in Tomcat you might want to change are:

    - the port number on which http requests are accepted
    - the logging options

2.4 Java Configuration

The Java virtual machine's configuration is very important to tune for best performance.  Unfortunately, this is a black art.  We recommend that you take the time to experiment with different memory and garbage collection settings and see what works best in your environment.

The standard way to control the JVM options when Tomcat starts up is to have an environment variable defined with JVM statup options:

    JAVA_OPTS=-server -Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

This is a good starting point, selecting server mode, turning on garbage collection details, and setting the memory.  We have found the best results when you set the min and max memory values to the same.  512 megs is not too much memory for Sakai - 1 gig is even better.

Tomcat will see this environment variable and use it.  Instead of putting this in an environment variable, you can modify the tomcat/bin/catalina.bat or tomcat/bin/catalina.sh file to manually set these values.

3. Sakai In Action

Once you have Sakai installed, configured and started, you can monitor Sakai by watching the logs.  The most interesting log is the tomcat catalina.out.  In some situations, this file will instead be the output from the tomcat process, showing up in the terminal window where you started Sakai.

Sakai code log messages end up in catalina.out. The log level for the standard Sakai source code and the demo is set to show info and warnings only.  If you need to, you can change the logging level to enable debug messages as well, for all of Sakai or specific components.  Sakai uses log4j for logging, see the log4j documentation for more information about how to configure it.

Look for the WARN: messages.  There are going to be some "normal" ones at startup, and some will likely slip by at runtime, but any warning is something you might want to check.

3.1 Log4J configuration

To change the logging for Sakai, you need to change Sakai source code and re-deploy sakai.  The file you need to change is:

    sakai-src/kernel/log-configure/src/conf/log4j.properties

The section that pertains to Sakai as distributed is:

    log4j.logger.org.sakaiproject=INFO

To turn on debug logging for all of Sakai, change that to:

    log4j.logger.org.sakaiproject=DEBUG

To turn on debugging only for one part of Sakai, add a line so you have:

    log4j.logger.org.sakaiproject=INFO
    log4j.logger.org.sakaiproject.component.framework.sql.BasicSqlService=DEBUG

This will leave most of Sakai at INFO, but the legacy SQL service will generate DEBUG.

3.2 Other log files

The SMTP server logs from Sakai will be written to the sakai.home/logs directory.  Tomcat's other logs in tomcat's logs folder might also have things of interest.

4. Integrating Sakai

Sakai can be integrated into your institution's information systems, so that it can share end user definitions, user role information, and some course management information.  These are all done by using custom Providers.  Sakai has three:

    - UserDirectoryProvider
    - RealmProvider
    - CourseManagementProvider

The default configurations of Sakai enable the "Sample" providers, located in the sakai-src/provider/sample project.  Other sample and working providers are found in the other projects in the provider module.

To select a provider for your instance of Sakai, you can either change the declaration in the provider module's component project, or replace this module with one you prepare for your needs.

Look for a document on Sakai Collab in the Sakai Development site resources for details about how to configure and create new providers for Sakai.

4.1 Provider Registration

Providers are found by the Sakai components that use them by looking for provider components registered under the names

    org.sakaiproject.service.legacy.user.UserDirectoryProvider (the user directory provider)
    org.sakaiproject.service.legacy.realm.RealmProvider (the realm provider)
    org.sakaiproject.service.legacy.coursemanagement.CourseManagementProvider (the course management provider)

The trick is to make sure that one component gets registered in your Sakai setup with each of these names, no more.

If you do not want a provider, set things up so that there is no component registered with the provider name.  The provider client code will properly handle not having one defined.

4.2 Changing The Provider Module's Components

One way to change the provider is to modify the sakai source code file that controls the provider registration in the Provider module.  The file, a Spring bean definition file, is found here:

    sakai-src/providers/component/src/webapp/WEB-INF/components.xml

See this file for more details - examples of an LDAP and Kerberos providers are included in comments.

Edit this file to pick which Provider module provider you want to use, and re-deploy Sakai.

4.3 Replacing the Provider Module

The other way to change the provider configuration is to completely remove the Provider module from your local sakai-src folder.  Then you need to create a new module / project with the providers you write to satisfy the three provider APIs.  You register your new components with the proper provider names, and include your module in the Sakai build and deploy.

At runtime, the Sakai provider module will be missing, your new module will be present, and when the provider client components get started up they will be wired to your new provider components.

4.4 Providers and Single-Signon

If you integrate Sakai in some sort of single-signon environment, you will need to also make your provider for users work with the same environment.  Most requests come in from browsers and will trigger the single-singon for authentication.  But our WebDAV support bypasses this normal route and relies on the internal authentication system in Sakai.  This works with the UserDirectoryProvider to check authentication against external sources.  Sakai also has a direct login path (/portal/xlogin) to bypass the single signon and invoke internal authentication directly.

5. Sakai and SSL / HTTPS

Most Sakai installations will want to run under HTTP for the SSL secure connections between the browser and the server.  This is good to protect the user passwords as well as the data that goes in and out of Sakai.

We do not recommend using Tomcat SSL support.  This SSL is implemented in java, and is much slower than a native SSL like that found in an Apache module for ssl.

Even better, put a hardware SSL handler / load balancer in front of your app servers to take care of the SSL processing.

See the Apache documentation for how to install an SSL plugin to Apache.

6. Apache - Tomcat

For many reasons, production systems will likely have an Apache web server on the front end to handle HTTP and HTTPS request to Sakai.  Apache is generally more trusted to run on the protected ports (80, 443) than Tomcat. It also is more efficient in processing the SSL part of the requests.

When Apache is handling the requests, it must send them to Tomcat for Sakai processing.  This is done by using an Apache - Tomcat connector.  This has to be properly configured on the Apache side and the Tomcat side.

6.1 Apache configuration

See the Apache and Apache - Tomcat connector documentation from Apache for details about how to configure this to send requests to Tomcat.

6.2 Tomcat configuration

The Tomcat side of the configuration involves turning off the http connector and turning on the ajp connector.  Simply comment out the parts of the configuration file (tomcat/conf/server.xml) that you don't want, comment in those that you do.  Make sure the port number set in Tomcat and Apache match.

    <!-- Define an AJP 1.3 Connector on port 8009 -->
    <Connector port="8009" 
               enableLookups="false" redirectPort="8443" protocol="AJP/1.3" URIEncoding="UTF-8" />

Make sure to add the URIEncoding option for proper character handling in Sakai.

7. Clustering Sakai

You may need to run multiple Sakai application servers in a cluster to support your user load.  At this time, we don't have a good feel for how many users each application server can support, but it's likely that an application server class machine should be able to handle a load of 100 concurrent users or more. You must  experiment with loads and your environment to see what you will need.

(Note: load testing of Sakai is schedule to occur around the release time - look for information about load testing and results on Sakai Collab in the Sakai Development site).

7.1 Configuration your Cluster

Sakai clusters by running a number of application servers with the same version of Sakai on each.  The only difference between them is the configuration value "serverId", usually set in a file in sakai.home called "local.properties".  This is an extension to sakai.properties that is optional.  If present, it will be used.  The advantage of using this file is that it is the only file that needs to be different between the clustered app servers - sakai.properties and the rest of Sakai are the same.

You need a front end "sprayer" or load balancer to take requests and distribute them among the machines in the cluster.  This must preserve "Session Stickiness".  This means that once a user establishes a Sakai session, they remain on the same app server until they logout.  Sakai does not support session sharing or serialization.

The machines in the cluster must also share the same back end database.  MySQL or Oracle are acceptable for this.

7.2 Starting and Stopping your Cluster

Simply start and stop each app server in the normal way.  It registers with the cluster on startup, and unregisters on shutdown.  If an app server ends without proper shutdown, the other app servers will notice this and do some cleanup of sessions left open by the missing server.

7.3 Monitoring your Cluster

The Admin's OnLine tool shows who's on and what app server they are connected to.  This is one way to see that your cluster is working.  Note that if an app server is running but has no active sessions, it will not show up in the list.

7.4 Changing the Cluster

You can add machines to the cluster, and remove machines from the cluster, without bringing your entire service down.  This is useful if you have a load increase and want to bring more machines on-line temporarily to handle it.  It can also be used to rotate machines out for maintenance, and back in, without service interruption.

When removing a machine from the cluster, first configure your front end load balancer to stop sending new requests to that app server.  Then watch for users to drain off of the app server.  The Admin's OnLine tool, or the access logs can be monitored to see that everyone's off the machine.  At that point it's safe to shut it down.
