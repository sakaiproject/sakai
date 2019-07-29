
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

Setting up the Issuer in Tsugi
------------------------------

Go back to the Admin page in Tsugi and select `Manage Access Keys` and then `LTI 1.3 Issuers`

If you don't already have an issuer set up, do `Add Issuer` - if you have an Issuer already,
you can view the issuer.  Or you could delete and re-add the issuer just to make sure.
(<a href="IMG_TSUGI/01-Tsugi-Add-Issuer.png" target="_blank">Image</a>)

In the Add/Edit page, Tsugi will convienently show you the URLs and information you need to
copy to Sakai.  The names of the variables are pretty similar between Sakai and Tsugi.

    LTI Content Item / Deep Link Endpoint: https://sakai.tsugicloud.org/tsugi/lti/store/
    LTI 1.3 OpenID Connect Endpoint: https://sakai.tsugicloud.org/tsugi/lti/oidc_login
    LTI 1.3 Tool Redirect Endpoint: https://sakai.tsugicloud.org/tsugi/lti/oidc_launch
    LTI 1.3 Tool Keyset URL (optional):
    https://sakai.tsugicloud.org/tsugi/lti/keyset     (contains all keys)
    https://sakai.tsugicloud.org/tsugi/lti/keyset?issuer=<issuer>

Do not save this item *yet* - will nedd some data from Sakai before we save this.

Setting up the security contract in Sakai
-----------------------------------------

Go in Sakai, go to Admin -> Admin Workspace -> External Tools -> Install LTI 1.x Tool
with the following values (watch for extra spaces while pasting and slashes matter):

    Choose a title like "sakai.tsugicloud.org Advantage"
    Check: Allow tool title to be changed
    Use "LTI Content Item / Deep Link Endpoint" from Tsugi as Launch URL in Sakai
    Check: Allow launch URL to be changed
    Set the key to something other than 12345
    Set the secret to be 'secret'
    Check: Send Names, Send Email, All services
    Un-Check: Allow the tool to be launched as a link
    Check: Allow tool to configure and Allow use in rich text editor
    Don't check any other placements
    Check: Always launch in debug mode
    Check: Supports LTI 1.3
    Paste in the LTI 1.3 OpenID Connect Endpoint from Tsugi
    Paste in the LTI 1.3 Tool Redirect Endpoint from Tsugi
    Launch type - Inherit

Press `Save` to create the Sakai end of the Tool security contract.  Then in Sakai
view the entry you just created and keep it in a tab in your browser.
(<a href="IMG_TSUGI/02-Sakai-View-Tool.png" target="_blank">Image</a>)

Completing the security contract in Tsugi
-----------------------------------------

Go back to the "Add Issuer" screen in Tsugi (hopefully still there in a tab)
and copy values from the Sakai tool view back into Tsugi:

    LTI 1.3 Issuer for this Platform
    LTI 1.3 Client ID
    Client ID
    LTI 1.3 Platform OAuth2 Well-Known/KeySet URL
    LTI 1.3 Platform OAuth2 Bearer Token Retrieval URL
    Leave LTI 1.3 Platform OAuth2 Bearer Token Audience Value blank
    LTI 1.3 Platform OIDC Authentication URL
    Copy the Tool Public Key from Sakai to Tsugi
    Copy the Tool Private Key from Sakai to Tsugi

Add the Tool in Tsugi.
(<a href="IMG_TSUGI/03-Tsugi-Issuer-Added.png" target="_blank">Image</a>)

Then we need to add a Tenant in Tsugi.  In the `Manage Keys` area of Tsugi
admin, select `Tenant Keys` and `Insert New Key`.  Fill in the data:

    LTI 1.1: OAuth Consumer Key (should not be 12345)
    LTI 1.1: OAuth Consumer Secret  (should be 'secret')
    LTI 1.3: Deployment ID - set to 1
    Pick your issuer from the drop down.
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

If you go into "Names and Roles" / "Debug Log" it should complain about missing bits.

