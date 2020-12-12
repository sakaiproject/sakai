
IMS LTI Advantage Auto Provisioning
===================================

In Sakai 21.1, there is early support for an emerging feature that will make the
installation and configuration of LTI Advantage tools even simpler than LTI 1.1
tools.

The idea is that much of the security arrangements for LTI Advantage are done using
public-private keypairs and the publis keys need no particular protection and the 
private keys never should leave the server in which they are "created".

So the configuration data for both tools and platforms is easily shared and moved between
systems - for example some systems export their OpenID configuration at what is called
a "well-known-url" that is publically readable.

The IMS (in draft as of December 2020) expands on this mostly-public exchange of security
arrangements.

Steps in the Provisioning Flow
------------------------------

First you work with a tool provider and arrange for permission to use a service.  In the
LTI 1.1 days, they gave you a launch url, key and secret that you typed into an LMS along
with some other settings.  Canvas allowed those "other settings" to come across in an XML
format - so you needed a configuration URL, key, and secret.

If the tool supports auto-provisioning, your tool vendor simply gives you the provisioning
URL and some tool-vendor-specific credential to use when you access the URL.  They may give you
both the LTI 1.1 and auto-provisioning information or augment an existing LTI 1.1 credential
with an LTI Advantage auto-provisioning URL if they have a good LTI 1.1 -> LTI Advantage
transistion strategy.

Then you go into your LMS and enter the auto-provisioning URL where appropriate.  Then a number
of steps take place:

* The LMS launches that tool auto-provisioning URL (usually in a modal dialog) and passes
in a LMS configuration url and a one-time use token for a web service call back as
parameters.

* (optionally) The tool may have you log in or present some other information to prove that
you "own" the key.

* The tool retrieves the LMS configuration data which includes things like key set URLs
and token endpoints.  This also includes the tool registration endpoint.

* The tool looks at the configuration and if it is OK for the tool, the tool sends its 
configuration with things like its key set url, OIDC initiation URL, and redirect urls.
The tool uses the one-time registration token to secure the web service.

* The LMS receives the tool configuration and returns the `client_id` that will be 
used for the security relationship.

* Id the tool is happy with the returned information, it updates its local configuration
and directs the user back to the LMS.

* The LMS usually gives the user a chance to review the returned information before
activating the tool.

Once activated the tool is ready to use.

Using Automatic Provisioning with Tsugi and Sakai
-------------------------------------------------

Get a key in Tsugi.  You can either use the administrator account to make a key
or use and instructor account to request / make a key.  Once the key is created - you can 
go in and view the key - you will see the LTI 1.1 information and at the bottom you will
see:

    LTI Advantage Auto Configuration URL:
    https://www.tsugicloud.org/tsugi/settings/key/auto?tsugi_key=4

It is a good idea to stay logged into Tsugi in another tab so Tsugi knows which keys you own.

Go into Sakai as the `admin` account and go into External Tools in the Adinistration
Workspace.  There is a new option at the main page titled "LTI Advantage Auto
Provision" - click it, enter a title and press "Auto Provision".
(<a href="IMG_PROVISION/01-Auto-Insert.png" target="_blank">Image</a>)


Sakai makes a partially complete LTI tool and puts you into the Edit page so
you can run the auto provisioning process.  Press 'Use LTI Advantage Auto Configuration',
enter the provisioning URL in the modal, and press "Begin Configuration".
(<a href="IMG_PROVISION/02-Auto-Update.png" target="_blank">Image</a>)

At this point, unless you need to log in - the protocol just runs.  (a) Tsugi retrieves
the OpenId configuration from Sakai, (b) Tsugi sends its registration to Sakai,
and (c) Tsugi updates its local key. 

When that is all done, you see a button titled, 'Continue Registration in the LMS'.

When you press that button, Sakai swings back in to action, closes the modal and reads
the tool registration information and updates the tool with the new information.  

You should scan the information to make sure it makes sense and then Save the tool.

The tool should be ready to use.

If something goes wrong, you can 'Cancel' out of the the tool edit screen and delete the draft tool
to start over.   Tsugi's Auto Configuration URLs are not one-time so you can run the process
over and over again and Tsugi won't care.

