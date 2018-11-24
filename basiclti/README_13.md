
How to add an LTI 1.3 tool to Sakai-19
======================================

Log in as admin / admin - go to Administration Workspace - Scroll down and select External Tools - Install LTI 1.x tool

Consumer key and secret are  not needed if you are going to only use LTI 1.3.  If you want
to test switching back and forth between LTI 1.1 and LTI 1.3.  The tool will specify the
Consumer Key and Secret.  If this is Tsugi, the consumer key is based on the issuer so
you you may have to add the tool in Sakai, Add it in Tsugi, and them come back and set
the consumer key and secret in the tool entry.

Put in the launch URL of the tool or Deep Link endpoint.

Check lots of check boxes for serivices and data sharing.

For a normal tool select "Allow the tool to be launched as a link"

For a deep link tool select "Allow external tool to configure itself"

Select LTI 1.3 near the bottom and provide both the Tools's OIDC endpoints.  Sakai only
supports one of each endpoint, (i.e. you cannot enter a comma separated list of values).

After you save the tool immediately view it.  Then you will see public keys, private keys, issuer,
keyset endpoint, token endpoint, etc that you can copy into your tool.   

If you want to use a different public / private key pair for communications from the tool to Sakai,
you can edit the tool entry, delete the tool private key, and paste in your own tool public key.  Sakai
does not yet support a keyset url for communications coming from the tool.

