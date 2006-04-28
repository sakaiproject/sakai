WHAT IT IS

su is a tool for Sakai 2.1.x for administrators to use to log in as another user. It is code developed at Texas State University for their local brand of Sakai called TRACS: Teaching Research and Collaboration System. It is meant to be used within the Administrative Workspace. It features a simple form in which you type the user id of the user you wish to login as or "become" in the system.

The name stands for Super User, and comes from a command-line tool in Unix that serves the same purpose. It is often pronounced "Sue."


BUILDING/DEPLOYING

The su tool is part of the standard Sakai release. The tool is only available to adminstrators and not part of the Adminstrator Workspace by default. If you would like to completely remove the su tool, it is at the path /admin-tools/su in the Sakai source tree.


USING THE TOOL

After the su tool is deployed, it will appear in the list of tools in the Administrative Sites tool. It will NOT appear in the list of tools for the Worksite Setup tool, since su is intended for administrators only, and not for general use.

In order to use the tool, use the Sites tool in the Administrative Workspace to edit the !admin site, add a page or edit an existing page, and place the tool on that page. The su tool will appear in the list with the title "Become User" and the id "sakai.su." When you've placed the tool, remember to click the Save button.

The tool itself is very simple. There is a text field to type a user id, and there is a Submit button. Your session will continue as though you had logged in as the specified user. This will work even if that user is already logged in at another location.

To change back to "yourself," you must logout and log back in.


SECURITY

The su tool is hard-coded only to work for users with administrative privileges. Naturally you should take care whom you give these privileges to. The ability to have more fine-grained control of permissions on the tool may be developed for a future version.


KNOWN ISSUES

It's a small thing, but if you click the "View user info" button, the button should then become disabled unless the id field should change.


CONTACT

Sakai su is written by Zach Thomas at Texas State University. You may contact him at zach.thomas@txstate.edu


CONTRIBUTIONS

You are encouraged to make improvements and submit patches to Zach at the address given above. He will incorporate your changes into the public release.