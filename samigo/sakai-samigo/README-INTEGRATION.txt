This CVS module is for the Sakai-Samigo integration effort. 

The directory structure of "sakai-samigo" CVS module is organized as a patch
to the standalone Samigo, which is in the "sam" CVS module.  The Sakai and
Samigo developers should keep the files in the "sakai-samigo" module up-to-date
with the files in the standalone "sam" module.

***Installation Instructions***

1. Install Sakai from the latest source
   Note that the detailed instructions for installing sakai can be found at
   http://cvs.sakaiproject.org/release/
   
   Install Sakai 1.5.0.  Either download the source from the release website,
   or checkout the CVS modules.  CVS access requires a private key. 
   See http://cvs.sakaiproject.org/anoncvs/ to obtain and configure the 
   anonymous private key.
   
   The current CVS tag is "sakai_1-5-rc11".  
   You can checkout the necessary modules using the following CVS commands:
   cvs -q -z9 -d :ext:sakaicvs:/cvs co -P -r sakai_1-5-rc11 sakai
   cvs -q -z9 -d :ext:sakaicvs:/cvs co -P -r sakai_1-5-rc11 sakai-samigo
   cvs -q -z9 -d :ext:sakaicvs:/cvs co -P -r sakai_1-5-rc11 sam

2. Deploy and test Sakai standalone
   Test Sakai by logging in as "admin", "admin" and try some of the tools.
   http://localhost:8080/portal

3a. Install Samigo v1.5
    Expand sam.tar by running

    tar xvf sam.tar (in Unix), 

    You can use WinZip or StuffIt Expander to expand sam.tar in Windows.
    This will create a directory called sam/ in your server. If you would like to get the
    latest Samigo version 1.5 source, then checkout the CVS module "sam" on the branch
    "samigo1_5_rc1" with tag "sakai_1-5-rc11". DO NOT check out from "sam" 
    main trunk as this is the developing Samigo version 2.0.
    Edit build.properties and change appserver.deployment.dir to your Tomcat 
    webapps directory. For example:

    appserver.deployment.dir=/usr/local/tomcat for Unix (for example)
    appserver.deployment.dir=c:\tomcat\webapps for Windows (for example)

    Then build sam using the command "ant deploy". This will create a build/ in your
    sam/ directory and a samigo.war which will be moved to your tomcat/webapps/ ready for
    deployment.

3b. Set up an user in your Oracle database. Then create the tables by running 
   the following ddl files in your SQLPlus in this order
   i.  01_schema_oracle.sql which contains SQL statement for creating all the 
       required tables and sequence, please be sure to commit.
   ii. 02_defaultSetUp_oracle.sql which contains SQL statements for populating 
       basic information (such as Type and  a Default Template) required for 
       running Samigo
   Both files are located inside sam/ddl/samigo-ddl/. You may want to capture the output to 
   check that all the tables and sequence are created accordingly.
   If you have previously installed an earlier version of Samigo and wish to upgrade
   to the current version, you should first finish the installation process in this section and
   then follow the instructions in "Migrating data from an earlier Samigo version".

3c. Configuring Samigo's security & settings
    i.   copy the security and settings directories opt/sa_forms/ and opt/j2ee/ 
         respectively to / in Unix and c:\ in Windows. Both directories can be 
         found in sam/conf/.

    ii.  update /opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml 
         so it reflects your Oracle database settings. 

    iii. If you prefer to put the security and settings directories in other locations
         instead of /opt, complete all the following steps FIRST. Then following the instructions
         in the section "Changing the location of the security & settings directory".

3d. Download Oracle JDBC Driver and install it in sam/lib/ directory
    Goto http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.html,
    select 10g(10.1.0.2.0) drivers, complete the licensing agreement to download this
    driver: 
    ojdbc14.jar (1,352,918 bytes) - classes for use with JDK 1.4

4. Deploy and test Samigo standalone
   Test Samigo standalone by going to the welcome page:   

   http://localhost:8080/samigo/jsf/index/index.faces

   You will see three "Student Entry Point" and one "Instructor Entry Point".
   If you get this far, you have installed Samigo correctly.

   If you want to experiment with Samigo, the first thing to do is probably to
   create an assessment using the "Instructor Entry Point".  Type in an assessment name
   and hit the button "Create". Add some questions to your assessment. When you are 
   ready to publish the assessment, use the link "Settings" located near the top of the
   "Edit Assessment" page, and then click "Publish".  You will be returned to the welcome
   page, and you can use the Student Entry point to access the assessment that you just published.

5. Install the patch "sakai-samigo.tar" by copying it over (overwriting) the Samigo source
   To install the patch, just unzip sakai-samigo.tar, and copy the files over top of 
   the latest Samigo source.  Do not make sakai-samigo a subdirectory of sam.
   Rather, copy sakai-samigo over the top of sam (overwriting a few existing files).
   If you are getting the patch "sakai-samigo" from CVS, 
   be careful not to copy the "CVS" directory over to sam/.  
   On Unix you could install the patch with these commands:

   cd sakai-samigo
   gtar -cvf /tmp/sakai-samigo.tar --exclude=CVS .   <-- note the . at the end
   (or tar -cvf /tmp/sakai-samigo.tar --X=CVS .) 
   cd ../sam
   gtar -xvf /tmp/sakai-samigo.tar
   (or tar -xvf /tmp/sakai-samigo.tar)

   This will:
   * Replace the previous integration jar (SakaiBaseFramework-1.0.a1.jar) with a 
     different version for integrated Sakai-Samigo.
   * Install new Samigo facades that perform authorization through Sakai's authorization API.
   * Modify the Samigo welcome page so that it looks different for students versus instructors.
   * Install a new Samigo publishing target that targets Sakai sites.

6. Register the Samigo tool by copying sakai-samigo/reg/*.xml to /usr/local/sakai/reg
   This makes Samigo available in the Sakai user interface.
   After this step and the next step, Samigo will be available 
   to the user as a tool in Sakai. Stop and Restart tomcat for this will take effect.


7. Before deploying Samigo with the patch, delete sam/build, tomcat/work, 
   tomcat/webapps/samigo, and tomcat/webapps/samigo.war. Run "ant deploy-in-sakai" to create
   samigo.war. This ant process will also move samigo.war to tomcat/webapps ready to be deployed.
   Note that this ant target is different from step 3a. This target exclude jar files when
   creating the samigo.war file. The excluded jar files has already been installed in 
   tomcat/shared/lib through the installation of sakai in step 2. 
   
   To test, startup Sakai and login as admin.  
   In another window in the same browser, go to:
   http://localhost:8080/xtunnel/samigo/jsf/index/mainIndex.faces
   You should be greeted with "Welcome, Sakai Administrator".
   Click on "Select Assessments", etc.


8. Test Samigo as a tool inside of the Sakai portal
   Login as admin at http://localhost:8080/portal.  Create a new worksite by clicking 
   on "Worksite Setup" on the left navigation bar, then "New", select "project website"
   and hit "Continue". A project Information form will be displayed, complete the form
   and hit "Continue". A list of tools available for your project will be displayed and
   you should notice that Samigo is among them. Select Samigo and hit "Continue", and follow
   the instructions to finish setting up the new worksite.

-------------------------------------------------------------------------------------------
***** Changing the location of the security and settings directory *****

1. If you would like to put the security directory in a different location other than /opt,
   you may do so by updating the following setting in
   sam/src/org/sakaiproject/spring/applicationContext.xml:

   <value>file:/opt/sa_forms/java/dev/org/sakaiproject/security/sam/samigo.xml</value>

   Note:
   a. step 5 - when installing the integration patch over samigo, the gtar command
      will overwite samigo.xml. You need to correct this value after step 5.

   AND modify the following parameter in sam/webapp/WEB_INF/web.xml:
   
   <context-param>
     <param-name>PathToSecurity</param-name>
     <param-value>/opt/sa_forms/java/dev</param-value>
   </context-param>

   Note:
   a. the directory structure after /opt/sa_forms/java/dev/ must remain as
      org/sakaiproject/security/sam.

2. You may also store the setting directories in a different location by updating
   the following parameter in sam/webapp/WEB-INF/web.xml:

   <context-param>
     <param-name>PathToSettings</param-name>
     <param-value>/opt/j2ee/dev</param-value>
   </context-param>

   However, make sure that the directory structure after /opt/j2ee/dev/ remains as
   org/sakaiproject/settings/sam/.


-------------------------------------------------------------------------------------------
***** Migrating data from an earlier Samigo version *****
1. You should complete the installation setup above and check that your Samigo
   instance is working before attempting this.
   To migrate your data from old tables over to the new tables in Samigo 1.5, run
   the following ddl files in your SQLPlus in this order:
   i.  01_schema_oracle.sql which contains SQL statement for creating all the
       required tables and sequence. Be sure that you commit the changes if your
       autocommit is not on.
   ii. 02_migrateData_v1_to_v1_5_oracle.sql which contains SQL statements that copy
       your existing data over to the new tables. Note that all the sequence will be
       reset accordingly.
   Both files are located inside sam/ddl/samigo-ddl/. You may want to capture the output to
   check that all the tables and sequence were created correctly.
