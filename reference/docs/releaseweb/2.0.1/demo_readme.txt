DEMO INSTALLATION GUIDE


Demo Installation Introduction:

The Sakai "Demo" includes a fully built and configured Sakai in a ready-to-run package that also includes Tomcat and the compatibility patch.  The only prerequisite is Java 1.4.  The Demo installation is the quickest way to get started with Sakai for demonstration and evaluation.



TABLE OF CONTENTS

   1. Installation Steps 
	1.1  Verify Java installation (and install if necessary)
   	1.2  Set environment variables
   	1.3  Download Sakai Demo
   	1.4  Unpack Demo archive
   	1.5  Start Tomcat
   	1.6  Test Sakai

   2. Demo Notes 
	2.1  Data Storage
   	2.2  Things Not Included in the Demo
   	2.3  Common Problems



1.  INSTALLATION STEPS


1.1  Verify Java Installation

      Check to see if you have Java installed on your system by running the following commands:

      Sample Commands for Windows: 	
C:\> java -version
Java(TM) 2 Runtime Environment, Standard Edition (build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

      Sample Commands for Mac/Linux/Solaris: 	
$ java -version
java version "1.4.2_04"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.4.2_04-141.3)
Java HotSpot(TM) Client VM (build 1.4.2-38, mixed mode)

      If you do not have the correct version of Java installed, install the Java Run-Time Environment (JRE) or the Java Software Development Kit (SDK) from http://java.sun.com/j2se/1.4.2/download.html

      * Note: Java is already installed on Mac OSX computers.


1.2 Set Environment variables

      In UNIX operating systems you typically modify a startup file like ~/.bash_login to set and export environment variables. In Windows XP, you go to

      Start -> Control Panel -> System -> Advanced -> Environment Variables

      in order to create or modify the named variables.

      Set the JAVA_HOME environment variable to point to the base directory of your Java installation. This will enable Tomcat to find the right Java installation automatically. This may already be set up for you by your Java SDK installation.
      
	Sample commands for Windows: 	
Set the environment variable JAVA_HOME to "C:\j2sdk1.4.2_04" (do not include the quotes)

      	Sample commands for Mac: 	
export JAVA_HOME=/Library/Java/Home

      	Sample commands for Linux: 	
export JAVA_HOME=/usr/local/java-current

      Then extend the PATH variable so as to include the Java commands.

      	Sample Commands for Windows: 	
Append the string ";C:\j2sdk1.4.2_04\bin" (include the semicolon but not the quotes) to the end of the System variable named Path.

      	Mac: not necessary

      	Sample for *nix: 	
export PATH=$PATH:$JAVA_HOME/bin/

      You should test that these variables are set correctly.  In both Windows XP and *nix operating systems you can simply start a new shell and type the 'set' command to see your environment variables.

      Once the variables are set properly, run the java -version command once more as a final check.


1.3  Download the Sakai Demo

      Get the demo from

      http://cvs.sakaiproject.org/releases/2.0.1/sakai-demo_2-0-1.zip

      OR

      http://cvs.sakaiproject.org/releases/2.0.1/sakai-demo_2-0-1.tar.gz


1.4 Unpack the Demo Archive

      When you unpack the demo archive appropriate for your operating system, you will have a folder called sakai-demo.  This folder will contain its own Tomcat directory.


1.5  Start Tomcat

      From the top Tomcat directory you can run the following startup commands:
      
      Windows: 	
bin\catalina start

      Mac/*nix: 	
bin/catalina.sh start


1.6  Test Sakai

      Once your Tomcat is started, open a browser and enter the following URL (don't be too alarmed about delays - it can take Tomcat half a minute or more to load the entire Sakai application):

http://localhost:8080/portal

      This will bring you to the Sakai gateway site.  One user account is included in the demo, the administrator's account.  Login using user id "admin" and password "admin".

      New user accounts can be created from the admin's User tool, or using the "new account" link on the Sakai gateway site.

      To stop the demo you can stop Tomcat with the following commands:

      Windows: 	
bin\catalina stop

      Mac/*nix: 	
bin/catalina.sh stop



2. DEMO NOTES


2.1  Data Storage

      The data for a Demo installation is stored in a HypersonicSql database which is stored in memory and then written out to files when Tomcat is shut down. These files are located at:

sakai-demo/sakai/db/sakai.db.*

      Objects created and modified in runs of the Sakai demo will persist through server restarts.

      HSQL is not appropriate for running Sakai in any sort of production environment. For these, we recommend MySql or Oracle (see the online Source Installation Guide on the 2.0.1 release page).


2.2  Things Not in the Demo

      * email detection in Sakai (i.e. mail sent to the Sakai server) is not enabled.
      * email from Sakai (i.e. mail sent from Sakai) is not configured. This would require providing Sakai with an SMTP server to use. If you want to do this, you can edit the sakai.properties found in sakai-demo/sakai and add:

smtp@org.sakaiproject.service.framework.email.EmailService=<SMTP>

      where <SMTP> is replaced with the name or ip address of an SMPT server that will accept mail from your Sakai app server.


2.3  Common Problems

     * Problems Getting Started: Sakai's Tomcat will run on port 8080, and also make use of ports 8005 and 8009.   If another process is running on any of these ports, Sakai will not start up.
     * Sakai's Tomcat will find itself relative to the startup directory.  Always start it from the sakai-demo folder.   If you have an environment variable CATALINA_HOME set to another Tomcat, this will interfere with Sakai's startup.

     * Problems Running: Memory or sluggish performance: 
	The Java virtual machine's configuration is very important to tune for best performance. Unfortunately this is something of a black art. We recommend that you take the time to experiment with different memory and garbage collection settings and see what works best in your environment.  The following details are offered only as samples or suggestions: before making any such changes to a production system please consult a systems administrator or local Java guru, if you're not such a person.

The standard way to control the JVM options when Tomcat starts up is to have an environment variable JAVA_OPTS defined with JVM startup options.  One sample value might be:

JAVA_OPTS=-server -Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

This is a fairly good starting point: it selects server mode, turns on garbage collection details, and sets the memory. We have found the best results when you set the min and max memory to the same values. 512 megs is not too much memory for Sakai; 1 gig is even better.
