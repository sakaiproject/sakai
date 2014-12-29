********************
Recent Changes
********************
10/25/2005 - Reporting to you live from the Sakai project trunk. You may con-
sider the contrib version of su-tool deprecated, but that's still where you
will find the 2.0.1 version of the tool. Now to finish integrating with the
new framework.

10/24/2005 - I am integrating su-tool with Sakai 2.1. As of now, the trunk of su 
is not suitable for deploying to 2.0.1; To get a release for 2.0.1, go to 
https://source.sakaiproject.org/contrib/migration/tags/su_2-0-1

10/6/2005 - Zach Thomas (me) moved all messages into a localized message bundle, 
and applied a couple of styles for attractiveness.

10/6/2005 - Aaron Zeckoski supplied fixes for the non-existant user and 
error-handling in general, and added a User Info feature.

9/14/2005 - Scott Fischbein at UC Davis added the code that will refresh the 
user's realms when logging in as them.

********************
What is Is
********************
su is a tool for Sakai 2.1.x for administrators to use to log in as another 
user. It is code developed at Texas State University for their local brand of 
Sakai called TRACS: Teaching Research and Collaboration System. It is meant to 
be used within the Administrative Workspace. It features a simple form in which 
you type the user id of the user you wish to login as or "become" in the system.

The name stands for Super User, and comes from a command-line tool in Unix that 
serves the same purpose. It is often pronounced "Sue."

*********************
Building / Deploying
*********************
Due to the way Sakai 2.1 builds are configured, the su directory now _must_ be 
copied into the Sakai source directory before you run maven on it. Sakai now 
uses a master project descriptor file, and su expects to find this at 
../master/project.xml.

Alternately, you can find a pre-compiled su-tool.war file in the "bin" 
directory. Simply copy this file into your Tomcat "webapps" directory, and the 
su tool will deploy automatically. This should work even if Tomcat is already 
running.

********************
Using the Tool
********************
After the su tool is deployed, it will appear in the list of tools in the 
Administrative Sites tool. It will NOT appear in the list of tools for the 
Worksite Setup tool, since su is intended for administrators only, and not for 
general use.

In order to use the tool, use the Sites tool in the Administrative Workspace to 
edit the !admin site, add a page or edit an existing page, and place the tool 
on that page. The su tool will appear in the list with the title "Become User" 
and the id "sakai.admin.su." When you've placed the tool, remember to click the 
Save button.

The tool itself is very simple. There is a text field to type a user id, and 
there is a Submit button. Your session will continue as though you had logged 
in as the specified user. This will work even if that user is already logged 
in at another location.

To change back to "yourself," you must logout and log back in.

*******************
Security
*******************
The su tool is hard-coded only to work for users with administrative 
privileges. Naturally you should take care whom you give these privileges to. 
The ability to have more fine-grained control of permissions on the tool may be 
developed for a future version.

*******************
Known Issues
*******************
It's a small thing, but if you click the "View user info" button, the button 
should then become disabled unless the id field should change.

*******************
Contact
*******************
TRACS su is written by Zach Thomas at Texas State University. You may contact 
him at zach.thomas@txstate.edu

*******************
Contributions
*******************
You are encouraged to make improvements and submit patches to Zach at the 
address given above. He will incorporate your changes into the public release.
