
Sakai Support for IMS ContentItem
=================================

Note: As of the writing of this documentation IMS has not finalized the
ContentItem specification and so this code should be considered experimental
until it has passed certification.

Configuration in sakai.properties
---------------------------------

This only will be turned on if the provider is enabled and there are tools to provide
(lines broken below for formatting)

    basiclti.provider.enabled=true

    basiclti.provider.allowedtools=sakai.announcements:sakai.singleuser:
    sakai.assignment.grades:sakai.dropbox:sakai.forums:
    sakai.gradebook.tool:sakai.podcasts:sakai.poll:sakai.resources:
    sakai.schedule:sakai.samigo:sakai.rwiki

    basiclti.provider.12345.secret=secret

Launching / Configuration - Sakai
---------------------------------

First you must install the ContentItem Picker in Sakai so you
can use it to pick Sakai tools.

Some how you must get the LMS to understand that you are installing a link that
can handle a ContentItem request.  In Sakai this is done as follows.

Go into Lessons, add an external tool, press "Manage External Tools", and
add an LTI 1.x tool.  Set the title, and allow the title to be changed.

Set the launch URL, key and secret.  This test endpoint should work:

    https://trunk-mysql.nightly.sakaiproject.org/imsblti/provider/content.item
    key: 12345
    secret: secret

Indicate that the launch url can be changed.

Make sure to pass name and email addresses and make sure to select the box
labelled "Allow external tool to configure itself (the tool must support
the IMS Content-Item message)" - this indicated that the tool is ready to accept
the special Content Item messages.

After you save the tool and go back to the "Add External Tool" in Lessons,
you should be able to launch the picker using the title you selected and off you
go.

Testing with one server (Inception Mode)
----------------------------------------

When the provider servlet receives a launch, it sets up a new user and session.
If you use the above process to set up a launch to:

    http://localhost:8080/imsblti/provider/content.item

You will find yourself logged out after the ContentItem selection is done
because you got a new session with the new user.   

Fortunately Sakai has a nice setting to indicate there is great trust between
the Consumer and Provider by setting this parameter for the consumer key:

    basiclti.provider.highly.trusted.consumers=12345

This tells the Provider to assume the same user and in particular for
ContentItem, it just assumes there is already a session and uses that
session instead of logging you out and back in.

If you turn on Inception Mode, ContentItem will fail when launches
are coming from external servers.  If you want both loop-back
ContentItem to work and external consumers to work as well just use
two separate keys.

Launching / Configuration - Canvas
----------------------------------

Also in Canvas, you must take a special step to convince Canvas
to do the ContentItem launch.

Go into Settings, View App Configurations, Add App.  Select
"By URL" give your entry a name and fill these in:

    config url: https://trunk-mysql.nightly.sakaiproject.org/imsblti/provider/canvas-config.xml
    key: 12345
    secret: secret

Then press "Submit" to add the tool to Canvas.

Then switch to modules and use the "+" to add a new tool.  Choose
"External Tool" from the top drop-down.  Then scroll down until
you see "Sakai Tools" - there should be a magnifying glass icon
by the Sakai entry indicating it was installed as a "picker" rather
than a tool.

Launch the picker, choose a tool, and then press "Install" in the
picker.  Then when back in Canvas press "Add Item" and the new
tool should appear in Modules and be ready to launch.

Do **not** set the "highly.trusted.consumers" value for the consumer
key when using Canvas.

You have several configuration options that can control the generation of the
canvas-config.xml file

    canvas.config.enabled - Defaults to true if basiclti.provider.enabled is true
    canvas.config.title - Sets the blti:title field in the XML
    canvas.config.description - Sets the blti:description text in the XML
    canvas.config.icon - Sets the path to the icon

These are less likely to need to be altered:

    canvas.config.domain - Override the domain property
    canvas.config.launch - Override the base launch URL


Debugging Settings
------------------

This is a useful set of debug classes for sakai.properties:

    log.config.count=5
    log.config.1 = DEBUG.org.sakaiproject.basiclti.util.SakaiBLTIUtil
    log.config.2 = DEBUG.org.sakaiproject.blti.tool.LTIAdminTool
    log.config.3 = DEBUG.org.sakaiproject.lessonbuildertool.tool.producer.BltiPickerProducer
    log.config.4 = DEBUG.org.sakaiproject.lessonbuildertool.tool.producers.LtiFileItemProducer
    log.config.5 = DEBUG.org.sakaiproject.blti.ProviderServlet

JIRA References
---------------

Here are some related JIRAs

https://jira.sakaiproject.org/browse/SAK-30418

https://jira.sakaiproject.org/browse/SAK-30424
