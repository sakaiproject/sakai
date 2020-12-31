
Testing Sakai with the Tsugi LMSTest Tool
=========================================

Tsugi is a great way to exercise Sakai.   Tsugi has a tool called "LMSTest" that is great
at exercising any implementation the LMS side of LTI Advantage.  In these examples, I will 
use https://sakai.tsugicloud.org as for the samples.

Make sure LMSTest is installed
------------------------------

You can check to see if the LMSTest tool is installed by navigating to:

https://sakai.tsugicloud.org/tsugi/store/

And checking if the LMSTest tool is installed.

You need to have the administrator password for your Tsugi instance.  To access the Adminstrator 
interface in Tsugi go to

https://sakai.tsugicloud.org/tsugi/admin/

Note that someitmes the Tsugi UI does not include links to the Admin UI until you have successfully
logged into the Admin UI once.

Once you are logged in, if the LMSTest tool is not installed, go to Manage Installed Modules ->
Advanced and enter the github URL for the LMSTest repo:

https://github.com/tsugicontrib/lmstest

And press `Clone Repository` - This works best in FireFox.  Once the repository clone is complete,
revisit https://sakai.tsugicloud.org/tsugi/admin/ to verify it is installed.

Setup Post SAK-44055
====================

The Tsugi provisioning / setup has changed dramatically for the better 
as a result of https://jira.sakaiproject.org/browse/SAK-44055 - while the
previous manual approach to setup still will work post SAK-44055, it is
simpler, quicker and preferred to use the auto-provisioning feature instead
of the manual approach:

* [Auto Provisioning Support in Sakai](PROVISION.md)

This feature will be in Sakai 21.1 and later and Sakai 20.3 and later - if all goes
well it might be in earlier versions of Sakai by the time you are reading this.

You can tell if your Sakai has SAK-44055 installed by going to Administration Workspace ->
External Tools and you see thw "LTI Advantage Auto Provision" option underneath "Install LTI 1.x Tool".
(<a href="IMG_PROVISION/01-Auto-Insert.png" target="_blank">Image</a>)

If you see that link you are in luck and can ignore the rest of this document unless you
are using it to get clues as to how configure a tool (not Tsugi ) that does not support
auto provisioning.

Setup Pre SAK-44055
===================

Setting up the Issuer in Tsugi
------------------------------

Go back to the Admin page in Tsugi and select `Manage Access Keys` and then `LTI 1.3 Issuers`

If you don't already have an issuer set up, do `Add Issuer` - if you have an Issuer already,
you can view the issuer.  Or you could delete and re-add the issuer just to make sure.
(<a href="IMG_TSUGI/01-Tsugi-Add-Issuer.png" target="_blank">Image</a>)

In the Add/Edit page, Tsugi will convienently show you the URLs and information you need to
copy to Sakai.  The names of the variables are pretty similar between Sakai and Tsugi.

    LTI 1.3 OpenID Connect Endpoint:
    http://localhost:8888/py4e/tsugi/lti/oidc_login/04696848-C819-A74C-E694-6166C14A248E 

    LTI 1.3 Tool Redirect Endpoint:
    http://localhost:8888/py4e/tsugi/lti/oidc_launch 

    LTI 1.3 Tool Keyset URL:
    http://localhost:8888/py4e/tsugi/lti/keyset/04696848-C819-A74C-E694-6166C14A248E 

    LTI Content Item / Deep Link Endpoint:
    http://localhost:8888/py4e/tsugi/lti/store/ 

The values with the GUIDs can be pasted into Sakai but the URLs won't work until you
actually save the entry in Tsugi - but you need to save the item in Sakai first to get
values that you need to paste into Tsugi before saving this item.

So once you see these values at the top of the screen, leave all the fill-in fields
blank, and switch to Sakai in a new tab.

Setting up the security contract in Sakai
-----------------------------------------

Go in Sakai, go to Admin -> Admin Workspace -> External Tools -> Install LTI 1.x Tool
with the following values (watch for extra spaces while pasting and slashes matter - 
you can use the Copy features in Tsugi to simplify things):

    Choose a title like "sakai.tsugicloud.org Advantage"
    Check: Allow tool title to be changed
    Use "LTI Content Item / Deep Link Endpoint" from Tsugi as Launch URL in Sakai
    Check: Allow launch URL to be changed
    Set the key to something *other than* 12345 - just pick a number
    Set the secret to be 'secret'
    Check: Send Names, Send Email, All services
    Un-Check: Allow the tool to be launched as a link
    Check: The tool can receive a Content-Item or Deep-Link launch
    Check: Allow the tool to selected from Lessons
    Check: Allow the tool to be one of the assessment types
    Check: Allow the tool to be used from the rich text editor
    Don't check "common cartridge" or "provide a file"
    Check: Always launch in debug mode
    Check: Tool supports LTI 1.3
    Paste in the LTI 1.3 Tool Keyset URL from Tsugi
    Paste in the LTI 1.3 OpenID Connect Endpoint from Tsugi
    Paste in the LTI 1.3 Tool Redirect Endpoint from Tsugi
    Launch type - Inherit System-Wide Default

Press `Save` to create the Sakai end of the Tool security contract. Sakai routes you
to a special screen 
(<a href="IMG_TSUGI/02-Post-Add.png" target="_blank">Image</a>)
that provides you a simplified view of the values you need to copy
back into Tsugi.

    LTI 1.3 Platform Issuer (provide to tool)
    LTI 1.3 Client Id (provide to tool)
    LTI 1.3 Deployment Id (provide to tool)
    LTI 1.3 Platform OAuth2 Well-Known/KeySet URL
    LTI 1.3 Platform OAuth2 Bearer Token Retrieval URL (provide to tool)
    LTI 1.3 Platform OIDC Authentication URL (provide to tool)

If you miss these values on this special "post add" screen, you can see
all the values by viewing the entry in Sakai
(<a href="IMG_TSUGI/03-Sakai-View-Tool.png" target="_blank">Image</a>).

Completing the security contract in Tsugi
-----------------------------------------

Go back to the "Add Issuer" screen in Tsugi in a new tab.  If you lots the tab,
the Tsugi urls will change so you need to actually start over.  Go to the tab
and copy values from the Sakai tool view back into Tsugi:

    LTI 1.3 Issuer for this Platform
    LTI 1.3 Client ID
    Client ID
    LTI 1.3 Platform OAuth2 Well-Known/KeySet URL
    LTI 1.3 Platform OAuth2 Bearer Token Retrieval URL
    LTI 1.3 Platform OIDC Authentication URL
    Leave LTI 1.3 Platform OAuth2 Bearer Token Audience Value blank

Note that the `deployment_id` is not used on this screen - save it for the next step.

Add the Tool in Tsugi.

Creating a Tenant / Deployment in Tsugi
---------------------------------------

Then we need to add a Tenant in Tsugi.  In the `Manage Keys` area of Tsugi
admin, select `Tenant Keys` and `Insert New Key`.  Fill in the data:

    LTI 1.1: OAuth Consumer Key (i.e. not 12345 - pick a random number - these need to be unique within Tsugi)
    LTI 1.1: OAuth Consumer Secret  (should be 'secret')
    LTI 1.3: Deployment ID - set to 1 (the value from Sakai)
    Pick your issuer/client-id from the drop down.
    Leave the Caliper fields blank
    Set the User Id to 1

And save the Tenant in Tsugi.
(<a href="IMG_TSUGI/04-Tsugi-Add-Tenant.png" target="_blank">Image</a>)

At this point you should have both sides of the security contract.

Testing Sakai Launching Tsugi
-----------------------------

Back in Sakai - find/make a site and find/add Lessons.

Back in Lessons, use Add Content -> Add Learning App and choose the tool you added
like "sakai.tsugicloud.org Advantage".  Click through any debug screens.

Click "+Install" in LMS Test, and fill out any screens and continue through any debug screens.
After some blinking, you should see LMS Test in Lessons.

Click on LMSTest, and click through the debug screen.  You should see the LMSTest UI with things like
"Home" "Grade Sent" ... "Interact With Line Items".
(<a href="IMG_TSUGI/05-LMSTest-Launch.png" target="_blank">Image</a>)

Click across the top items, and for each top item, click all the secondary items 
and see if you get any kind of error.

Under the `Home` button in LMSTest, look at the "Tsugi User Object" and not the id of the 
User for the next step.

Testing LTI 1.1 to LTI Advantage Migration
------------------------------------------

Go into Administration Workspace -> External Tools, edit the tool, check "Tool does not support LTI 1.3"
and save the tool.

Go pack to Lessons and launch LMSTest again.  It should say the LTI 1.1 debug message like
"Press to continue to the external tool".  You can look at the launch data is it will be old school 
LTI 1.1 data.  When you continue, it should launch again into LMSTest.
(<a href="IMG_TSUGI/06-LMSTest-Migration.png" target="_blank">Image</a>)

Look under "Home" / "Tsugi User Object" and the User's id should be the same as in the LTI Advantage launch.

If you go into "Names and Roles" / "Debug Log" it should complain about missing bits because that is LTI
Advantage only.

