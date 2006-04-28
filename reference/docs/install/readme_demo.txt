DEMO REQUIREMENTS

A successful demo installation requires the support of the correct Java installation.  Steps for ensuring this are detailed here.


1. CHECK JAVA VERSION

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


2. SET ENVIRONMENT VARIABLES

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

Finally, you need to set a variable to tune your Java environment.  The default JVM is not sufficient to run Sakai out of the box, and some of its settings will need to be adjusted by setting a new environment variable called JAVA_OPTS, just as you did above. The recommended value of this variable is:

JAVA_OPTS="-server -Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m"

See the "JVM Tuning" section of the online installation guide (http://cvs.sakaiproject.org/release/2.1.2/postconfig.html#jvm) for a fuller discussion of the details and ways to improve performance.

You should test that these variables are set correctly.  In both Windows XP and *nix operating systems you can simply start a new shell and type the 'set' command to see your environment variables. You may run the java -version command once more (see above) as a final check.

You can also look for the Sun Java Installation Instructions page at the Java web site for further details.


3. DOWNLOAD AND UNPACK THE DEMO ARCHIVE

* Windows version: 
http://cvs.sakaiproject.org/release/2.1.2/sakai_2-1-2/sakai-demo_2-1-2.zip
* Mac/*nix version:
http://cvs.sakaiproject.org/release/2.1.2/sakai_2-1-2/sakai-demo_2-1-2.tar.gz


4. START TOMCAT (from the root Tomcat directory):

Windows: 	start-sakai.bat
Mac/*nix: 	start-sakai.sh

Once Tomcat is started up, you can point your browser to

http://localhost:8080/portal

This will bring you to the Sakai gateway site where you can log in using the default administrator's account (user id "admin" and password "admin"). New user accounts can be created from the admin "User" tool, or by using the "New Account" link on the Sakai gateway site. New sites can be created using the admin "Worksite Setup" tool.

To stop the demo:
Windows: 	stop-sakai.bat
Mac/*nix :	stop-sakai.sh


NOTES

* Data Storage
The data for a Demo installation is stored in a HypersonicSql database (HSQLDB) which is stored in memory and then written out to files when Tomcat is shut down. These files are located at:

sakai-demo/sakai/db/sakai.db.*

Objects created and modified during runs of the Sakai demo will persist through server restarts. [Warning: HSQLDB is not appropriate for running Sakai in any sort of production environment. For such cases we recommend MySql or Oracle (see Database Configuration)]
	
* Email not configured in the Demo
    * email detection in Sakai (i.e. mail sent to the Sakai server) is not enabled.
    * email from Sakai (i.e. mail sent from Sakai) is not configured. This would require providing Sakai with an SMTP server to use. See Post-Installation Configuration for more details.
