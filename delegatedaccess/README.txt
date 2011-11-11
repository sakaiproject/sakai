Delegated Access Tool

The delegated access tool allows administrators to search for users and delegate site and role access without enrolling the user in the site.  It also allows you to select specific tools the user should not have access to.

Getting it to work:

Step 1: Apply kernel and portal patches:

delegatedaccess_kernel.patch
delegatedaccess_portal.patch

Then build and deploy.

Step 2: Build Delegated Access

Build and deploy the delegated access tool

Step 3:  Create a hierarchy

The default hierarchy is based on a site's property values (in order):

School
Department
Subject

you can overwrite the hierarchy structure in sakai.properties with: delegatedaccess.hierarchy.site.properties 
	ex:
	delegatedaccess.hierarchy.site.properties.count=3
	delegatedaccess.hierarchy.site.properties.1=Top
	delegatedaccess.hierarchy.site.properties.2=Middle
	delegatedaccess.hierarchy.site.properties.3=Bottom


Once you have set up your hierarchy properties, you will need to add these properties to your sites.  This can be done during your site integration job.

Step 4: Quartz Job to Populate Your Hierarchy Structure

DelegatedAccessSiteHierarchyJob

This is the default quartz job to populate/update(add/remove) the Delegated Access site hierarchy.  It searches through all sites in Sakai and looks for structure properties tied to the site.  You can run it as many times as you want.   The best bet would be to set up a quartz trigger to go off after every time your site integration runs.

Step 5: Add tool to Administrative Workspace

Only an admin will be able to assign delegated access to users.

Step 6: Delegate Access as Admin

The easiest way to think of how the tool works is liking it to the Role Swap feature in Sakai.  Instead of just swapping the role, you can specify the realm and role the user will receive for that particular site or node in the hierarchy.

Go to the tool and click "Search Users" and find a user you want to delegate access for.  Click their name.  Now you will be able to select which nodes they will have access to and which role they will emulate.  You can also restrict tools by clicking the "Restrict Tools" link.  Every child of the selected node will inherit these settings but will be able to override them by selecting the node and choosing its own values.  When done, click save.

Step 7: Get Access as a User

By default the tool can be added to a user's My Workspace.  Since only administrators can delegate access, a regular user won't be able to modify anything.  If the user doesn't have any access delegated to them, they will see a message saying so.  Otherwise, you will see a node structure in which you can navigate and click on the sites you've been granted access to.  Since this tool populates the delegated access information during login, a user could also use direct links to a delegated site.

