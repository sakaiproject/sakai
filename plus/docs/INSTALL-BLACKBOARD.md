Blackboard
----------

Blackboard is planning on supporting LTI Dynamic Registration, but until they do,
you need to do a bit of cutting and pasting of URLs between the systems.

To use this process, create a Tenant in Administration Workspace -> Plus Admin, with
a title and the following information:

    Issuer: https://blackboard.com
    OIDC Auth: https://developer.blackboard.com/api/v1/gateway/oidcauth
    OIDC Token: https://developer.blackboard.com/api/v1/gateway/oauth2/jwttoken

Then go into the Sakai Plus Registration for the tenant and grab the "Manual Configuration"
URLs so you can create an LTI 1.3 clientID in the Blackboard Developer Portal.  Here
are some sample Sakai Plus URLs you will need for the Blackboard Developer portal:

    OIDC Login: https://dev1.sakaicloud.com/plus/sakai/oidc_login/654321
    OIDC Redirect: https://dev1.sakaicloud.com/plus/sakai/oidc_launch
    OIDC KeySet: https://dev1.sakaicloud.com/imsblis/lti13/keyset

Note that the `OIDC Login` value for Sakai Plus includes the Tenant ID for your
newly created Sakai Plus Tenant so it is unique for each Sakai Plus Tenant.  The
Redirect and Keyset values are the same for all tenants.

Use these Sakai Plus values in the Blackboard Developer portal to create an
LTI 1.3 integration.  The developer portal will give you a Client Id and
per-client KeySet URL similar to the following:

    OIDC KeySet: https://developer.blackboard.com/api/vl/management/applications/fe3ebd13-39a4-42c4-8b83-194f08e77f8a/jwks.json
    Client Id: fe3ebd13-39a4-42c4-8b83-194f08e77f8a

The value in the KeySet is the same as the Client Id.  You will need to update these values
in your Sakai Plus Tenant.

Once you place Sakai Plus into a Blackboard instance you will be given
a Deployment Id for that integration.

    Deployment Id: ea4e4459-2363-348e-bd38-048993689aa0

Once you have updated your Sakai Plus tenant with the `Client ID`,
`Keyset URL`, and `Deployment ID` your security arrangement should be
set up.

Once the Tenant has all the necessary security set up, there a number
of `target_link_uri` values that you can use.  You can send a Deep Link

