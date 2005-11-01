SAMIGO STANDALONE BUILD INSTRUCTIONS VERSION 2.1
[provisional, still incomplete]

Samigo can be built as a standalone webapp, but has a few dependencies on
other Sakai jars.

1. Check out a fresh copy of Samigo from Subversion,

          svn checkout https://source.sakaiproject.org/svn/trunk/sakai/sam

2. Download and install tomcat with pre-1.5 compatibility, for example,

          jar xvfM apache-tomcat-5.5.12.zip
          jar xvfM apache-tomcat-5.5.12-compat.zip

          (for UNIX/Linux)
          cd apache-tomcat-5.5.12/bin
          chmod 755 *.sh

3. Edit your build.properties file in your user directory, for example,

  maven.repo.remote = http://www.ibiblio.org/maven/,http://cvs.sakaiproject.org/maven/,http://horde.planetmirror.com/pub/maven/
  maven.tomcat.home=/usr/local/home/myusername/standalone/apache-tomcat-5.5.12/

4. By default the standalone configuration database configuration file is
specified in the bean with the id of "propertyConfigurer" in
sam/component/src/java/org/sakaiproject/spring applicationContextStandalone.xml
as "/opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.properties".

Edit your samigo.properties file, an example for Oracle follows,

db.driverClassName=oracle.jdbc.driver.OracleDriver
db.url=jdbc:oracle:thin:@xxx.myuniversity.edu:1521:yyy
db.username=mydbusername
db.password=mydbuserpw
hibernate.dialect=net.sf.hibernate.dialect.Oracle9Dialect
hibernate.show_sql=false
hibernate.jdbc.batch_size=0
hibernate.connection.pool_size=100
hibernate.cache.provider_class=net.sf.hibernate.cache.EhCacheProvider

Copy any database drivers required to your tomcat's shared/lib.

5. Change to the sam directory and build standalone,

           maven standalone

5. Start tomcat

          cd apache-tomcat-5.5.12/bin
          ./startup.sh (UNIX/Linux, startup.bat for Windows)





