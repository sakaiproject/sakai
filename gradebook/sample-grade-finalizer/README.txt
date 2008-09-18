WHAT'S HERE

This directory contains source code which can be used to finalize course grades
in existing Sakai sites, either on a site-by-site basis or for all gradebooks
associated with a given academic session or term.

For background, see: http://jira.sakaiproject.org/jira/browse/SAK-13652


FINALIZING GRADES THROUGH THE JOB SCHEDULER

1. Build and deploy the "sakai-gradebooksample-grade-finalizer" component from
this directory:

mvn clean install sakai:deploy

2. Log into Sakai as an administrator and go to the "Administration Workspace".

3. Select the "Job Scheduler" tool and go to the "Jobs" and then the "New Job"
page.

4. From the "Type" menu, select "Finalize course grades; specify Job Name as
'siteUid=xxxx-xxx-xxx' or 'academicSessionEid=xxxx'".

5. Use the "Job Name" field to specify which gradebooks to finalize.

To finalize grades for a gradebook in a single course site, enter the ID of the
site (as found after the "/site/" portion of the URL). For example, if the
course site is found at the URL:

http://localhost:8080/portal/site/9cbef755-d625-4d75-a24a-0448827d7a07

Then specify it in the "Job Name":

siteUid=9cbef755-d625-4d75-a24a-0448827d7a07

To finalize all gradebooks associated with a given academic session or term,
first find the "academic session EID" identifying that term. (This will depend
on your local institution's Course Management implementation. One way to find
the correct code is to go to the administrator-only "Sites" tool, click on a
course site, and look in its "Properties" section for the "term_eid" field.)
Then specify it in the "Job Name":

academicSessionEid=2008-B


FINALIZING GRADES FROM THE COMMAND LINE

This approach is more experimental and might require editing the "pom.xml" file
in this directory. It requires running Maven on a system with a deployed version
of Sakai and a "sakai.properties" file that's properly set up for your
installation.

1. Edit "pom.xml" if necessary. In the "exec-maven-plugin" section at the bottom
of the file, you'll find a set of arguments to pass through to the job. The
default version of this file is designed to work with the demo version of Sakai
(which loads sample course, section, and user data). For running against a non-
demo installation, you'll need to change the line:

<argument>-Dsakai.demo=true</argument>

to

<!-- <argument>-Dsakai.demo=true</argument> -->

2. From this directory, issue a Maven command with "tomcat.home" set to the
location of your Sakai deployment, "sakai.home" set to the location of your
"sakai.properties" file, and "added.exec.arg" specifying what site or academic
session should have its grades finalized.

For example, here's how I finalized the gradebook for the course site whose ID
was "59bfa92f-b872-42b3-97ec-84adb81261fe":

mvn -Dtomcat.home=$CATALINA_HOME \
  -Dsakai.home=C:/java/sakaisettings/mysql-sakai/ \
  -Dadded.exec.arg="-DsiteUid=59bfa92f-b872-42b3-97ec-84adb81261fe" \
  clean install exec:exec

And here's how I finalized gradebooks for all course sites taking place in the
term with the code "Spring 2008":

mvn -Dtomcat.home=$CATALINA_HOME \
  -Dsakai.home=C:/java/sakaisettings/mysql-sakai/ \
  -Dadded.exec.arg="-DacademicSessionEid=Spring\ 2008" \
  clean install exec:exec
