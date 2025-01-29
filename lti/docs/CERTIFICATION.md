
Certification Notes
-------------------

The certification suite used two different redirect urls - one for regular launch
and one for deep link launches.  

    https://ltiadvantagevalidator.imsglobal.org/ltiplatform/deeplinkredirecturl.html
    https://ltiadvantagevalidator.imsglobal.org/ltiplatform/oidcredirecturl.html

When setting up a Sakai, you need to include both of these in the "LTI 1.3 Tool Redirect 
Endpoint(s) (comma separated and provided by the tool)" field separated by commas so that
both the Deep Link and Resource Link launches work.  The IMS OIDC login step chooses the
right redirect url.

   https://ltiadvantagevalidator.imsglobal.org/ltiplatform/deeplinkredirecturl.html, https://ltiadvantagevalidator.imsglobal.org/ltiplatform/oidcredirecturl.html

Create two sites, with the same instructor.  Use DeepLink to install a link in both sites
before you do the Deep Link certification - it will use the second one - but you will need
two links in two contexts with two users to pass the AGS part of the course or you will
get a duplicate lineitem error.

If you restart the test delete all of the gradebook columns before restarting or you 
will get duplicate columns in the AGS phase.

It seems as though you can run the whole test with PII turned off.

Also delete the LTI links installed by the DeepLink process if you restart.

Please Submit a 2nd, Different Context Student Payload


