This is the Admin Site Role Permissions updater, it is an admin tool for Sakai
Author: Aaron Zeckoski (azeckoski@vt.edu) (azeckoski@gmail.com)

Description: This will add or remove the selected permissions in all sites of the types selected and all roles selected, 
you must select at least one permission, one site type, and one role to do an update, 
this can be somewhat slow when updating a very large number of sites
This is a super user/admin tool only

To use this project, you should build it and deploy it into a Sakai installation
which is at least version 2.9. If you do not know what that is you may
want to check this website out:
    http://bugs.sakaiproject.org/confluence/display/BOOT

Run the following command from within the root directory of your source code project to download all the 
dependencies using maven 2.x or better and compile the code:
	mvn install

Deployment:
    A) mvn sakai:deploy
    OR
    B) Copy the war file from the target dir to your tomcat webapps dir

You can import the project into eclipse (using the current location)
or continue using it in eclipse if you created it in your sakai source tree.
