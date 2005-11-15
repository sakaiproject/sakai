SAMIGO STANDALONE BUILD INSTRUCTIONS VERSION 2.1
================================================================================
[provisional]

Samigo can be built as a standalone webapp, but has a few dependencies on
other Sakai jars.  As of version 2.1, it is also section-aware, so you will need
to install the section tool in standalone mode as well.

How to build and run:
================================================================================

A. SETUP FOR BUILD
1.  Install Maven and Subversion.

2. If you have not ever built a copy of Sakai, you may need to build it once, so
that maven has downloaded and/or built all required libraries.  You can skip
this step, if you think you have them.  Otherwise--

In your $HOME you can edit your build.properties file, so you direct the
build to a scratch directory, so that your server does not get Sakai installed.
 maven.repo.remote = http://www.ibiblio.org/maven/,http://cvs.sakaiproject.org/maven/
 # SHOULD NOT be your tomcat directory: i.e. scratch/webapps, shared/lib
 maven.tomcat.home=/usr/local/home/myusername/scratch
 # this is the new line, change to your own directory
 hibernate.properties.dir=/opt/sa_forms/java/dev/org/sakaiproject/security/sam/

We don't want a Sakai installation, just making sure library dependencies get
resolved....

          svn checkout https://source.sakaiproject.org/svn/trunk/sakai
          cd sakai
          maven sakai

3. Check out a fresh copy of the Section Tool from Subversion,

          svn checkout https://source.sakaiproject.org/svn/trunk/sakai/sections

4. Check out a fresh copy of Samigo from Subversion,

          svn checkout https://source.sakaiproject.org/svn/trunk/sakai/sam

5. Download and install tomcat with pre-1.5 compatibility, for example,

          jar xvfM apache-tomcat-5.5.12.zip
          jar xvfM apache-tomcat-5.5.12-compat.zip

          (for UNIX/Linux)
          cd apache-tomcat-5.5.12/bin
          chmod 755 *.sh


B. BUILD AND DEPLOY THE SECTION TOOL
1. Edit sections-app/project.properties file.

 # The directory containing your hibernate.properties file
 # uncomment this line to bypass the one in $HOME/build.properties
 #hibernate.properties.dir=${basedir}/src/hibernate/

 # The temp directory to use for the webapp
 maven.war.src=${maven.build.dir}/webapp_dir/sakai-sections-tool
 maven.war.webapp.dir=${maven.build.dir}/webapp_dir/sakai-sections-tool

 # JUnit formatting (plain gives more details than brief)
 maven.junit.format=plain

 # Display junit output rather than redirecting it to a file
 maven.junit.usefile=false

 # Tomcat webapps directory for standalone (non-sakai) deployment
 #-- change this one to your own standalone tomcat, e.g.:
 #standalone.deploy.dir=C:/xyz/jakarta-tomcat-5.5.9/webapps/
 #standalone.deploy.dir=/usr/local/home/myusername/tomcatstandalone/webapps/

2. In your $HOME edit your build.properties file, and add one more line.
 maven.repo.remote = http://www.ibiblio.org/maven/,http://cvs.sakaiproject.org/maven/
 # this will be your tomcat directory
 maven.tomcat.home=/usr/local/home/myusername/standalone/apache-tomcat-5.5.12/
 # this is the new line, change to your own directory
 hibernate.properties.dir=/opt/sa_forms/java/dev/org/sakaiproject/security/sam/

3. Add a hibernate.properties file in your {hibernate.properties.dir}. For
example:

 hibernate.connection.driver_class=oracle.jdbc.driver.OracleDriver
 hibernate.connection.url=jdbc:oracle:thin:@acompdb2.stanford.edu:1521:acompdb2
 hibernate.connection.username=username
 hibernate.connection.password=password
 hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
 hibernate.show_sql=false
 hibernate.cache.provider_class=net.sf.hibernate.cache.EhCacheProvider


4. Make sure you have your database jar in your .maven/repository/oracle/jars.
Oracle users would have ojdbc14.jar in .maven/repository/oracle/jars. Other
database users will use a similar setup.

5. Change to the sections directory and build standalone.

    maven -Dmode=standalone  cln bld

Make sure all tests are successful.

6. Create the sections database schema

If you are not using Hypersonic, make sure you uncomment your database driver (e.g. Oracle, mysql) in the
sections-app/project.xml directory and that maven can find your database driver


    maven -Dmode=standalone -Dmem=false schema loadData

Make sure all tests are successful.

7. Start tomcat, for example

          cd apache-tomcat-5.5.12/bin
          ./startup.sh (UNIX/Linux, startup.bat for Windows)

8. Verify that you can access the Section Tool:

    http://[host}:{port}/sakai-sections-tool/

9. Shut down tomcat.

          ./shutdown.sh (UNIX/Linux, shutdown.bat for Windows)

C. BUILD AND DEPLOY SAMIGO (TEST AND QUIZZES TOOL)

1. By default the standalone configuration database configuration file is
specified in the bean with the id of "propertyConfigurer" in
sam/component/src/java/org/sakaiproject/spring applicationContextStandalone.xml
as "/opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.properties".

2. Either

a. Edit your samigo.properties file, an example for Oracle follows,

  db.driverClassName=oracle.jdbc.driver.OracleDriver
  db.url=jdbc:oracle:thin:@xxx.myuniversity.edu:1521:yyy
  db.username=mydbusername
  db.password=mydbuserpw
  hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
  hibernate.show_sql=false
  hibernate.jdbc.batch_size=0
  hibernate.connection.pool_size=100
  hibernate.cache.provider_class=net.sf.hibernate.cache.EhCacheProvider

 the location is specified in the propertyConfigurer bean in
  sam/component/src/java/org/sakaiproject/spring/applicationContextStandalone.xml

 Or,
 b. (quick and dirty) edit your applicationContextStandalone.xml file
  i. replace all the ${}'s with actual values
   (${db.driverClassName}, ...${hibernate.cache.provider_class}
  ii. comment out the propertyConfigurer bean.



3. Copy any database drivers required to your tomcat's shared/lib.

4. Change to the sam directory and build standalone,

           maven standalone

5. Restart tomcat

          cd apache-tomcat-5.5.12/bin
          ./startup.sh (UNIX/Linux, startup.bat for Windows)

6.  Verify that you can access the Samigo test index page:

     http://[host}:{port}/samigo/jsf/index/index.faces

================================================================================

