D2L BrightSpace
---------------

BrightSpace supports LTI Dynamic Registration.  Create a Tenant with a title,
issuer, and registration unlock code.   Then go to the SakaiPlus Tenant detail page and find the LTI
Dynamic Registration URL and use that in the auto-provisioning screen of BrightSpace.

The issuer for a D2L system is the base URL of the system without a trailing slash:

    https://school.brightspacedemo.com

While Dynamic Registration is the easiest approach, you can create a draft Tenant
in Sakai Plus, then paste all the Sakai Plus URLs into Brightspace manually, save the tool
in Brightspace, then get copy the Brightspace URLs and edit your Sakai Plus
Tenant.  Here are what typical values look like for Brightspace:

    Client ID:           04a7d304-477d-401a-b701-5a58f54772d6
    Deployment ID:       7862b2ce-79a0-77da-b2dd-7c77c4bb6e39
    LMS Authorization:   https://school.brightspacedemo.com/d2l/lti/authenticate
    LMS KeySet:          https://school.brightspacedemo.com/d2l/.well-known/jwks
    LMS Token:           https://auth.brightspace.com/core/connect/token
    LMS Token Audience:  https://api.brightspace.com/auth/token

Some of the values are local to the Brightspace school's URL and others
are global for all schools.

The basic outline in Brightspace is to

* Install an LTI Advantage Tool
* Create a Deployment for the tool
* Create a Link for the tool (this is what most LMS's call "Placement")

Make sure to enable the security settings for `Org Unit Information`,
`User Information`,`Link Information`.  If you do not send `Org Unit Information`
Sakai Pus will not know anything about the course it is being launched from.
And sending email is important because otherwise all the SakaiPlus accounts will
use the "subject" as the logical key for user accounts.   SakaiPlus can function
without email - but it makes it a lot harder to re-connect user accounts later.

For Dynamic Registration to work, Sakai Plus demands that the issuer in Sakai Plus
match the issuer provided by the LMS during the LTI Dynamic Registration process.
The registration lock is single use and must be reset in Sakai Plus to re-run the Dynamic
Registration process.

Here are some helpful URLs:

    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/intro_to_LTI.htm
    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/LTI_register_external_learning_tool.htm
    https://success.vitalsource.com/hc/en-gb/articles/360052454313-Brightspace-D2L-LTI-1-3-Tool-Setup
    https://documentation.brightspace.com/EN/integrations/ipsis/LTI%20Advantage/deploy_external_learning_tool_for_LTI_A.htm
