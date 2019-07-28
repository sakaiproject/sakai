
Using the IMS Reference Implementation with Sakai
=================================================

IMS Provides a very useful reference implementation of LTI advantage that can be useful
for developer or QA testing.  It is important to test with as many implementations as
you can.

The setup order is a little weird because both Sakai and the Reference Implementation
need to actually start the endpoint creation process before certain URLs are available.
We solve this by starting the process onthe reference implementation and half-creating
the tool end point, copying some URLs and then creating the Sakai end of the
security contract.

It is best to use this with an IMS account to unlock the advanced features - but a lot of
the test can be done without logging in.  If you have an IMS log in, then login in
your browser before you start the process.

This first version of the document only describes the "not logged into the RI" scenarios.
Once you get thisgs going with the RI, and once you log in - just keep going and test more
stuff.   Remember that instructor / admin cannot recevie grades in Sakai so you need
to make a Student / access account to test any grade passback feature.

Start By Making (or Editing) a Tool in The RI
---------------------------------------------

Go to

https://lti-ri.imsglobal.org/lti/tools

If you have done this before, you can reuse an existing tool.   These tools don't "belong"
to anyone so you can edit any tool - but be cool and only edit your tools.  Folks tend to
put their name of the name of the software they are testing as the title of the tool.

If you need to add a tool just set it up with the minimum fields:
(<a href="IMG_IMS_RI/01-TI-Tool-Partial.png" target="_blank">Image</a>)

    Name: Chuck dev1.sakaicloud.org
    Client: 12345  (will change later)
    Deployment: 1

Once you save the tool go back to https://lti-ri.imsglobal.org/lti/tools and find your
tool and copy these values (examples shown):
(<a href="IMG_IMS_RI/02-RI-Tool-View.png" target="_blank">Image</a>)

    Tool end-point to receive launches:
    https://lti-ri.imsglobal.org/lti/tools/362/launches

    Tool end-point to receive Deep Link launches:
    https://lti-ri.imsglobal.org/lti/tools/362/deep_link_launches

    OIDC Login Initiation URL:
    https://lti-ri.imsglobal.org/lti/tools/362/login_initiations

Testing a Normal LTI 1.3 / JWT Launch
-------------------------------------

Back in Sakai, go to Admin -> Admin Workspace -> External Tools -> Install LTI 1.x Tool
With the following values (watch for extra spaces while pasting):

    Choose a title like "IMS RI Normal Launch"
    Use "Tool end-point to receive launches" as Launch URL in Sakai
    Leave key / secret blank
    Check: Send Names, Send Email, All services
    Check Only: Allow the tool to be launched as a link
    Don't check any other Placements.
    Check : Always launch in debug mode
    Check: Supports LTI 1.3
    Launch type - Inherit

Copy these fields from the IMS RI to Sakai (don't add spaces at the end):

    LTI 1.3 Tool OpenID Connect/Initialization Endpoint <- OIDC Login Initiation URL
    LTI 1.3 Tool Redirect Endpoint(s) <- Tool Launch Url

The fact that the Tool Launch URL is the same as the Redirect URL is a oversimplification
in the RI.  These should be different for most tools.  And if they are the same, the RI
should at least document it as they are separate parts of the contract.

Press `Save` to create the Sakai end of the Tool security contract.  Then in Sakai
view the entry you just created.

Back in the IMS RI edit the your tool and transfer these fields from Sakai to
the RI:

    Client ID
    Keyset url <- LTI 1.3 Platform OAuth2 Well-Known/KeySet URL
    Oauth2 url <- LTI 1.3 Platform OAuth2 Bearer Token Retrieval URL
    Platform oidc auth url <- LTI 1.3 Platform OIDC Authentication URL
    Private key <- Tool Private Key (Must toggle at the bottom to show)

Update the tool.
(<a href="IMG_IMS_RI/03-RI-Data-Copied.png" target="_blank">Image</a>)

Go back to https://lti-ri.imsglobal.org/lti/tools and find your entry again - I like to
keep this open in a separate tab.

Testing Normal Launch in Sakai
------------------------------

Back in Sakai - find/make a site and find/add Lessons.

Lessons -> Add Content -> Add External Tool (way at the bottom of the pop up menu)
(<a href="IMG_IMS_RI/11-Lessons-Add-External.png" target="_blank">Image</a>)

Find and install the tool you installed above - it should plug into Sakai and you should
be able to launch the tool.  
(<a href="IMG_IMS_RI/12-Lessons-Installed.png" target="_blank">Image</a>)

You should see a page in the RI that says something like "Make Authentication Request
back to Platform" - you may need to scroll down - press
the "Proceed with LTI 1.3 Launch" button to "Continue".
(<a href="IMG_IMS_RI/13-Lessons-OIDC.png" target="_blank">Image</a>)

You actually need to press this button pretty quickly - this is part of
the OIDC Connect Flow and Sakai has a pretty short window of time that
it allows the OIDC Connect flow to come back.

After you press the button to continue through the Sakai debug screen, you should 
add "Lauch Status - Valid".
(<a href="IMG_IMS_RI/14-Lessons-Launched.png" target="_blank">Image</a>)

*When you are done, remove launch link from Lessons since we are going to switch our
tool entry to Deep Linking which wil break normal launches.*

Switching to Content Item
-------------------------

Now we are going to adjust the entry in Sakai so we can test `Deep Linking` - the
LTI Advantage version of `Content Item`.

Go to Admin -> Admin Workspace -> External Tools -> Install LTI 1.x Tool
Edit the tool you installed and make the following changes:

    Change the title and button test to something like "IMS RI Deep Link"
    Change Launch URL to the "Tool end-point to receive Deep Link launches" value
    You don't have to change the redirect endpoint
    Leave Checked: Send Names, Send Email, All four services,
    Un-Check: Allow the tool to be launched as a link
    Check: Allow tool to configure and Allow use in rich text editor
    Don't check any other placements

Save the Tool.
(<a href="IMG_IMS_RI/21-Sakai-Tool-Checkboxes.png" target="_blank">Image</a>)

Testing Deep Link Launch
------------------------

Back in Lessons, use Add Content -> Add Learning App

You should see the Reference Implementation tool you just edited in Sakai - choose it.
It should launch in a modal.  If there are debug buttons - just continue.

You should see a page in the RI that says something like "Make Authentication Request
back to Platform" - you may need to scroll down - press
the "Proceed with LTI 1.3 Launch" button to "Continue".

You actually need to press this button pretty quickly - this is part of
the OIDC Connect Flow and Sakai has a pretty short window of time that
it allows the OIDC Connect flow to come back.

The next screen you should see is also from the RI - it should say "Successful launch".

At this point if you have not logged in you have no more steps unless you log in.
If you are logged in with your IMS account - keep going :)

