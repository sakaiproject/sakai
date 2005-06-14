Welcome to Samigo, the Sakai Assessment Manager.  This is a full-featured
assessment module, that works as a standalone system or as part of a course
management system.


***** To build and run standalone Samigo and Sakai2-Samigo integrated v2.0: *****
1. Expand sam.tar into c:\samigo in Windows using program like winunzip
   or stuffIt. In Unix, you can run this incommand prompt:

   shell> tar -xvf sam.tar
   
   If you would like to get Samigo from the sakai cvs repository, the module name for
   Samigo is "sam" and the branch for version 1.5 is "samigo1_5_rc1". DO NOT check
   out from "sam" main trunk as this is the developing Samigo version 2.0

2. Download a copy of tomcat 5.5.9 or higher and the 1.4 compatible patch, and make sure
   your JAVA_HOME points to a java version of 1.4.2 or higher

   http://archive.apache.org/dist/jakarta/tomcat-5/v5.5.9/bin/jakarta-tomcat-5.5.9.tar.gz
   http://archive.apache.org/dist/jakarta/tomcat-5/v5.5.9/bin/jakarta-tomcat-5.5.9-compat.tar.gz

3. Set up an Oracle user and create the tables by running
   the following ddl files in your SQLPlus in this order
   i.  01_schema_oracle.sql which contains SQL statement for createing all the
       required tables and sequence. Be sure that you commit the changes if your
       autocommit is not on.
   ii. 02_defaultSetUp_oracle.sql which contains SQL statements for populating
       basic information (such as Type and  a Default Template) required for
       running Samigo
   Both files are located inside sam/ddl/samigo-ddl/. You may want to spool the output to
   check that all the tables and sequence are created accordingly. 
   If you have previously installed an earlier version of Samigo and wish to migrate the
   data over. Please first finish the installation process in this section before following
   the instructions in "Migrating data from an earlier Samigo version".

4. Configuring Samigo's security & settings
    i.   copy the security and settings directories opt/sa_forms/ and opt/j2ee/
         respectively to /opt in Unix and c:\ in Windows. Both directories can be
         found in sam/conf/.

    ii.  update /opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml
         so it reflects your Oracle database settings. You can skip to step 3c if you
         do not mind the location of the security & settings directories.

   iii.  If you prefer to put samigo.xml in a different location, you may do so by 
         updating the following setting in sam/src/org/sakaiproject/spring/applicationContext.xml

         <value>file:/opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml</value>

         AND 

         the following parameter in sam/webapp/WEB_INF/web.xml

         <context-param>
           <param-name>PathToSecurity</param-name>
           <param-value>/opt/sa_forms/java/dev</param-value>
         </context-param>

         Note that the directory structure after /opt/sa_forms/java/dev/ must remain as
         org/sakaiproject/security/sam.

     iv. you may also store the setting directories in a different location by updating
         the following parameter in sam/webapp/WEB-INF/web.xml

         <context-param>
           <param-name>PathToSettings</param-name>
           <param-value>/opt/j2ee/dev</param-value>
         </context-param>

         However, make sure that the directory structure after /opt/j2ee/dev/ remains as
         org/sakaiproject/settings/sam/.

 c. Download Oracle JDBC Driver and install it in tomcat/shared/lib/ directory
    Goto http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html,
    select 10g(10.1.0.2.0) drivers, complete the licensing agreement to download this
    driver:
    ojdbc14.jar (1,352,918 bytes) - classes for use with JDK 1.4

 d. For Hypersonic database support, please read the section "Support for Hypersonic HSQLDB".

5. Assuming that you already have "Maven" (version 1.0.2 or later)  installed and 
   it is executable  from anywhere. 

   a. Samigo Standalone compilation: go to the sam top level directory, and do
   "maven" in the command prompt. samigo.war will be created and
   copied to the tomcat/webapps directory

   b. Sakai2-Samigo Integrated compilation: you must check out "sakai2" code and run "maven sakai" 
   before attempting to install Samigo. When you are ready, go to the sam top level directory, and do
   "maven sakai" in the command prompt. samigo.war will be created and
   copied to the tomcat/webapps directory

6. To deploy samigo.war, go to tomcat/bin, and do startup.sh
   (startup.bat for Windows)

7. There are two points of entry for Samigo; the student view
   and the instructor view.
   In samigo Standalone, go to our welcome page:
   http://your.server.name:8080/samigo/jsf/index/index.faces
   You will see three "Student Entry Point" and one "Instructor Entry Point".

   For Sakai2-Samigo integrated environment, you should create a working sie and select "Test & Quizzes"
   as one of the site tool. When it is set, click at the site tab and select Test & Quizzes in the left 
   navigation bar to start.

   To begin, we probably should create an assessment using the "Instructor Entry Point",
   type in an assessment name and hit the button "Create". Add some questions to your 
   assessment, when you are  ready to publish the assessment, use the link "Settings"
   located near the top of the "Edit Assessment" page. Then you shall return to the 
   welcome page and use one of the Student Entry point to access the assessment that you
   just published.


***** Migrating data from Samigo version 1.2 and below *****
1. You should complete the installation set up above and check that your Samigo
   instance is working before attempting this.
   To migrate your data from old tables over to the new tables in Samigo 1.5, run
   the following ddl files in your SQLPlus in this order
   i.  01_schema_oracle.sql which contains SQL statement for creating all the
       required tables and sequence. Be sure that you commit the changes if your
       autocommit is not on.
   ii. 02_migrateData_v1_to_v1_5_oracle.sql which contains SQL statements that copy
       your existing data over to the new tables. Note that all the sequence will be
       reset accordingly.
   Both files are located inside sam/ddl/samigo-ddl/. You may want to spool the output to
   check that all the tables and sequence are created accordingly. 

Get Help: If you have any questions about the installation, please feel free to
email navigo-dev@lists.stanford.edu. We will be glad to help.


***** Support for Hypersonic HSQLDB ******
1. Download Hypersonic 1.7.x from http://hsqldb.sourceforge.net/ and follow the installation instruction

2. Add the following statement to sqltool.rc. If you have followed the HSQLDB installation instruction,
   this file should be placed at the HSQLDB Owner's home directory.

   urlid samigo
   url jdbc:hsqldb:hsql://your.server.edu:9001/samigo
   username sa
   password

   Note that we are using the user "sa" with password "" that came with the initial set up.

3. Create ~/hsqldb/samigo/server.properties and add the following statement.

   server.database.0=~/hsqldb/samigo
   server.dbname.0=samigo

4. Start the server with database "samigo" using these commands

   shell> cd hsqldb/lib
   shell> java -cp hsqldb.jar org.hsqldb.Server -database samigo &


5. Create tables and popluate default settings using the SQL tool

   shell> cd hsqldb/lib
   shell> java -jar hsqldb.jar --rcfile ~/sqltool.rc samigo 

   cut and paste SQL stmt from ~/sam/ddl/samigo-ddl/01_schema_hsqldb.sql and
   ~/sam/ddl/samigo-ddl/02_defaultSetUp_hsqldb.sql to the "sql>" priompt

   Afterward, execute "select * from sam_type_t;". If it returns a list of rows back,
   then you have succssfully set up the database.

6. Now, update the database settings in /opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml
   There is no need to specify the database name "samigo" as it is the only one that is up - step4.

   db.driverClassName=org.hsqldb.jdbcDriver
   db.url=jdbc:hsqldb:hsql://your.server.edu:9001
   db.username=sa
   db.password=

***** Support for MySQL ******
1. Download MySQL 4.x from http://dev.mysql.com/downloads/ and follow the installation instruction. 

2. Start the database server using these commands

   shell> cd mysql_installation_directory
   shell> bin/mysqld_safe &

3. Login to the database, create a user with a password (using GRANT), and create a database called "samigo"

   shell> mysql -u root -p
   mysql> grant all on * to 'yourname'@'%' identified by 'yourpassword';
   mysql> create database samigo

4. Create tables and popluate default settings

   shell> mysql -u root -p samigo < ~/sam/ddl/samigo-ddl/01_schema_mysql.sql --force
   shell> mysql -u root -p samigo < ~/sam/ddl/samigo-ddl/02_ defaultSetUp_mysql.sql  --force

5. Now, update the database settings in /opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml

   db.driverClassName=com.mysql.jdbc.Driver
   db.url=jdbc:mysql://your.server.edu:3306/samigo?user=youname&password=yourpassword&autoReconnect=true
   db.username=yourname
   db.password=yourpassword

