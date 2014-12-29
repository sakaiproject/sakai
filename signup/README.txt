This project was created by Yale University.

The current Sign-up tool is a beta version.
 
Section A: Installation
-----------------------
 
 The Sign-up Tool is integrated into Sakai and it is not a standalone application. 
 The source code of the Sign-up Tool can be placed in your Sakai source tree and built 
 using Maven. If unfamiliar with installation of Sakai, please check the following links 
 for installation documentation of Sakai application:

	Sakai 2.4	
		http://source.sakaiproject.org/release/2.4.0/

	Sakai 2.5	
		http://confluence.sakaiproject.org/confluence/display/DOC/Install+Guide+(2.5)
		
	sakai 2.6
		http://confluence.sakaiproject.org/display/DOC/Install+Guide+(2.6)

 Compatibility:
	
   The Sign-up tool is currently compatible with Sakai versions from 2.4 to 2.6.  Appropriate build 
   files are provided for both Maven 1 and Maven 2.
 
 Requirements to run Sign-up tool:

	Sakai 2.4 or higher.
	Java JDK 1.5 version or higher.
	Tomcat 5.5.20 version or higher.

 For the initial creation of the Sign-up tool database tables, an Oracle/mySQL SQL scripts is available 
 at the folder of Sign-up tool’s source code:
		sign-up\resources

 If a MySQL Database is used, turn on the auto.ddl property value from false to true in the 
 sakai.properties (Production level is not recommended and should use db-scripts). 

 Customize the UI and Email Contents:
   You could customize the UI labels and email message contents by modifying the following two 
   property files:
	 emailMessage.properties
	 messages.properties

	
Section B: Permission Setup
---------------------------

Permission levels
 The following 13 permission levels have been defined to satisfy the various requirements:

 *Create 
   a)signup.create.site - create events/meetings open to all site participants
   b)signup.create.group - create events/meetings for the own group(s)
   c)signup.create.group.all - create events/meetings for any/all groups in the site

 *Delete
   a)signup.delete.site - delete any site-wide event/meeting
   b)signup.delete.group - delete any event/meeting of the own group(s)
   c)signup.delete.group.all - delete any group event/meeting in the site


 *Update
   a)signup.update.site - update or edit any site-wide event/meeting
   b)Signup.update.group - update or edit events/meetings of the own group(s)
   c)signup.update.group.all - update or edit events/meetings for any group in the site

 *Attend
   a)signup.attend - attend (sign up) group level event/meeting if they are member of the group 
     and site level event/meeting if they are member of the site
   b)signup.attend.all - attend (sign up) any event/meeting in the site and its groups

 *View
   a)signup.view - view group level events/meetings if they are member of the group or 
     site level events/meetings if they are member of the site
   b)signup.view.all - view any event/meeting in the site and its groups

 Example of Permission Setup
   Roles can be assigned a combination of the above permissions.  Here are suggested settings:

	*Instructor
		-signup.create.site
		-signup.delete.site
      		-signup.update.site
      		-signup.view
      		-signup.view.all

	*Teaching Assistant(TF)
		-signup.create.group or signup.create.group.all
		-signup.delete.group or signup.delete.group.all
		-signup.update.group or signup.update.group.all
		-signup.attend.all
		-signup.view
		-signup.view.all

	*Student
		-signup.attend
		-signup.view

	*Guest
		-signup.view or signup.view.all
	
	*Auditor
		-signup.attend
		-signup.view
		
	*Study-Group Organizer Role (student)
		-signup.create.group 
		-signup.delete.group 
		-signup.update.group 
		-signup.view
		-signup.attend
		
	*Maintain
		-signup.create.site
		-signup.delete.site
		-signup.update.site
		-signup.view
		-signup.view.all

	*Access
		-signup.attend
		-signup.view

*Important: The signup.view permission is required to use the tool.


Here is the summary of the possible permission settings for the corresponding roles:
---------------------------------------------------------------------------------------------------------------
Permissions				Instructor		TF		Student		Auditor		Guest	Maintain     Access
---------------------------------------------------------------------------------------------------------------
signup.create.group						X							
signup.create.group.all					(or X)							
signup.create.site			X			                                            X								
signup.delete.group						X							
signup.delete.group.all					(or X)							
signup.delete.site			X			 											X								
signup.update.group						X							
signup.update.group.all					(or X)							
signup.update.site			X			                                            X								
											
signup.attend										X			X			                   X	
signup.attend.all						X							
signup.view											X			X			X		 	       X		
signup.view.all				X			X									(X)      X		 		
----------------------------------------------------------------------------------------------------------------

*Note:	It is a good idea to set these permissions for the !site.template and !group.template.course 
 		so they will be inherited in any new created sites or groups.
 