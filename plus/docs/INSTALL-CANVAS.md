Canvas
------

Canvas does not support LTI Dynamic Registration but has their own JSON-based
automatic Registration process that is supported by Sakai Plus.

    https://canvas.instructure.com/doc/api/file.lti_dev_key_config.html

To use this process, create a Tenant in Sakai Plus with a title and the following
information:

    Issuer: https://canvas.instructure.com
    OIDC Auth: https://canvas.instructure.com/api/lti/authorize_redirect
    OIDC KeySet: https://canvas.instructure.com/api/lti/security/jwks
    OIDC Token: https://canvas.instructure.com/login/oauth2/token

Make sure to check "Trust Email" - this needs to be set in the SakaiPlus Tenant
from the beginning.

This is a partially complete tenant, to get the remaining data, go into
the Tenant detail page and find the Canvas URL that looks like:

     https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=1234567

Use this URL in the Canvas Admin -> Developer Keys -> + Developer Key -> + LTI Key.
Set Key Name, Title, and your email address.  Then Choose "Enter URL" from the drop-down
and paste the URL for your Tenant in Sakai.  Make sure not to have any spaces in
the URL.  Then press "Save".  The go back in to edit the key and make sure the
key is marked as "Public" in "Additional Settings", changing and saving if necessary.

to create an integration.  This integration
creates a Client Id similar to the following:

    Client Id: 85730000000000147

Then to install Sakai Plus into a course or set of courses, you must use the
Client Id to add the tool and it then gives you a Deployment ID.
For a single course, go to Settings -> View App Configurations -> + App.  Then
choose "By Client ID" from the drop down and enter the ClientID from the previous
step and press "Submit".

    Deployment Id: 327:a17deed8f179b120bdd14743e67ca7916eaea622

Come back to Sakai Plus and update the Tenant to include both values and
your integration should start working.

For Canvas, sometimes it generates *lots* of Deployment Id values, so you
can make authorization of SakaiPlus based only on Client Id by leaving
the Deployment Id blank/empty in the Tenant.  SakaiPlus will track
Deployment Id on a per-context basis for AccessToken calls to the the LMSs.
