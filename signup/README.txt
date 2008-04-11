This project was created by Yale University.

The current Sign-up tool is a beta version. 

Installation:
The Sign-up Tool is integrated into Sakai and it is not a standalone application. The source code of the Sign-up Tool can be placed in your Sakai source tree and built using Maven. If unfamiliar with installation of Sakai, please check the following links for installation documentation of Sakai application:

	Sakai 2.3.1	
		http://source.sakaiproject.org/release/2.3.1/install-overview.html

	Sakai 2.4	
		http://source.sakaiproject.org/release/2.4.0/

	Sakai 2.5	
		http://confluence.sakaiproject.org/confluence/display/DOC/Install+Guide+(2.5)

Compatibility:
	
The Sign-up tool is currently compatible with Sakai versions from 2.3 to 2.5.  Appropriate build files are provided for both Maven 1 and Maven 2.
Requirements to run Sign-up tool:

	Sakai 2.3 or higher.
	Java JDK 1.5 version or higher.
	Tomcat 5.5.20 version or higher.

For the initial creation of the Sign-up tool database tables, an Oracle SQL script is available at the folder of Sign-up tool’s source code:
		sign-up\resources

If a MySQL Database is used, turn on the auto.ddl property value from false to true in the sakai.properties. 

Customize the UI and Email Contents:
You could customize the UI labels and email message contents by modifying the following two property files:
	emailMessage.properties
	messages.properties
