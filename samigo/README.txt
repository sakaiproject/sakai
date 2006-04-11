Welcome to Samigo, the Sakai Assessment Manager.  This is a full-featured
assessment module, that works as the "Test & Quizes" module in the Sakai 2.1 management system.


***** To build and run Samigo as an integrated module in Sakai2.0: *****
1. Install tomcat 5.5.9 and jdk 1.4.2
   Download a copy of tomcat 5.5.9 or higher and the 1.4 compatible patch, and make sure
   your JAVA_HOME points to a java version of 1.4.2 or higher

   http://archive.apache.org/dist/jakarta/tomcat-5/v5.5.9/bin/jakarta-tomcat-5.5.9.tar.gz
   http://archive.apache.org/dist/jakarta/tomcat-5/v5.5.9/bin/jakarta-tomcat-5.5.9-compat.tar.gz

2. Install Samigo2.0 from the latest source
   Note that the detailed instructions for installing sakai can be found at
   http://cvs.sakaiproject.org/release/

   Install Sakai 2.0.0.  Either download the source from the release website,
   or checkout the CVS modules.  CVS access requires a private key.
   See http://cvs.sakaiproject.org/anoncvs/ to obtain and configure the
   anonymous private key.

   Two most common mistakes that you can make when trying to set up the anonymous check out are:
   i. you forgot to set CVS_RSH=ssh as your environment variable
   ii. the read permission for your sakai_anoncvs_dsa is incorrect

       -r--------   1 daisyf   staff        668 May 31 16:07 sakai_anoncvs_dsa

   The current CVS tag is "sakai_2-0-0-rc3".
   Samigo resides as a module under the sakai2 repository. If you have already checked out 
   sakai2 using the following command, you should notice that samigo has alraedy been checked
   out and is located at sakai2/sam/.
   cvs -q -z9 -d :ext:sakaicvs:/cvs co -P -r sakai_2-0-0-rc3 sakai2

   Otherwise, check out Samigo 2.0 using this command:
   cvs -q -z9 -d :ext:sakaicvs:/cvs co -P -r sakai_2-0-0-rc3 sakai2/sam

3. Assuming that you already have "Maven" (version 1.0.2 or later)  installed and 
   it is executable  from anywhere. 

   If you run "maven sakai" under sakai2, all the modules (including Samigo) inside sakai2 will be
   compiled and copied to the tomcat/webapps directory. This process can take over 10min the first
   time, as a lot of libraries will be downloaded from maven remote repository to your 
   ~home/.maven/repository. You will see a lot of download failure warning message, don't worry 
   about it. All you need to watch out for is the "BUILD SUCCESSFUL" message at the end of 
   the process.

   If you only want to recompile Samigo, goto sakai2/sam and run "maven sakai". The process will
   take about 3min the first time and 2min any consecutive times.

4. To deploy samigo.war (and all the rest of sakai2), go to tomcat/bin, and do startup.sh
   (startup.bat for Windows)

5. Test Samigo as a tool inside of the Sakai portal
   Login as admin at http://localhost:8080/portal.  Create a new worksite by clicking
   on "Worksite Setup" on the left navigation bar, then "New", select "project website"
   and hit "Continue". A project Information form will be displayed, complete the form
   and hit "Continue". A list of tools available for your project will be displayed and
   you should notice that Samigo is among them. Select "Gradebook" and "Test & Quizes"
   and hit "Continue", and follow the instructions to finish setting up the new worksite.
   You should notice that the new sie that you just created will appear as a tab near the
   top of the screen, select it and now you shall notice that "Gradebook" and "Test & Quizes"
   appears on the left navigation bar.

6. Trying out Samigo 2.0 and Gradebook
   To begin, we probably should create an assessment. Select "Test & Quizes" to get the
   "Assessment Authoring page", type in an assessment name and hit the button "Create".
   Add some questions to your assessment, when you are  ready to publish the assessment,
   use the link "Settings" located near the top of the "Edit Assessment" page. Since, we
   have included Gradebook in our site, by default any assessments published by Samigo will
   be added to Gradebook. You should select Gradebook and check that out. Note that, in this
   case all Gradebook does is providing a view to the students score in samigo. Gradebook
   does not provide way for editing a score. To edit a score, you must return to Samigo to
   do so.

7. Taking the assessment
   return to http://localhost:8080/portal, create a new account and join the new site that 
   you just created in step 5. Once this new user become a member of a site, the site name
   will appear as a tab near the top of the screen. Select the site, then "Test & Quizes",
   now you are seeing the "Assessment Taking page". You should see that the assesment that
   you created in step 6 showed up in the list.

8. Where is all the data being stored?
   Like the otehr Sakai2's modules, Samigo 2.0 came out of the box with support for HSQLDB,
   the assessment that you created in step 5 is stored in memory managed by HSQLDB. You will
   need to set up appropriate database instance if you were to persist these records. Refer
   to sakai2 installation instructions for setting up different databases.

   a. it is recommended that in production, all the tables required should be created manually.
      In the unix platform, you can simply run 
      find . -name "*.sql" -print
      to locate all the sql files.
   b. Samigo's sql files are located at sakai2/sam/src/ and orgainized by the name of the 3 
      databases that we support: oracle, hsqldb & mysql.
      
      For example: 01_schema_oracle.sql which contains SQL statement for creating all the
      required tables and sequence in oracle . Be sure that you commit the changes if your
      autocommit is not on. sakai-samigo.sql contains all the insert statement with default
      value that is required to run Samigo. 

9. A note about File Upload questions supported by Samigo 2.0, if you are creating all the 
   Samigo 2.0. tables using the sql files, you are set and ready to support this question type.
   If you were going to take advantage of the auto.ddl functionality that Hibernate provides.
   In other words, Hibernate inspect the Samigo's OR map, create a set of ddl statements with
   the best guess it could and create the tables when the application is deployed in tomcat.
   In this case, you may need to inspect the data type for the column "MEDIA" in the table 
   SAM_MEDIA_T. Depending on what Hibernate's best guess is, you may need to alter this column
   accordingly. 

   In oracle, Hibernate interpreted the binary column "MEDIA" with a "raw" datatype which is not
   enough if you intend to store big files. In this case, drop the couln and add a new column with
   the same name but select "blob" as its data type instead.

Get Help: If you have any questions about the installation, please feel free to
email navigo-dev@lists.stanford.edu. We will be glad to help.

Additional documentation will be found in the docs subdirectory.

