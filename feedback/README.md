Sakai Feeback Tool
==================

Overview
--------

The Feedback tool was developed as a way to try to direct user queries to the appropriate team or a relevant web site. This tool's default name within the interface is "Contact Us".

The central admin team were getting fed up with receiving emails from students complaining about site content,
eg, broken links to websites, spelling mistakes in PDFs, ambiguous questions in a test and so on.

These questions need directing to the site owner or teacher of the course
(or to anybody with site update permission unless they have hidden themselves via Preferences in My Workspace).

The Feedback tool has 4 default panels each covering a different aspect and two custimizable panels:

1/ Content panel, "Problem with content?"

2/ Help panel, "How do I do this?"

3/ Technical panel, "I've hit a bug / technical issue"

4/ Suggestions panel, "Feature suggestion"

5/ Supplemental A Panel (default disabled)

6/ Supplemental B Panel (default disabled)

Each panel can be disabled in the properties file, so if you don't want users to see a paenl, you can remove that panel.

Panel 1 takes its contacts from the membership of the current site.

All panels, except for #1, may either be an email form or a link. This is controlled via property settings, for example:
    feedback.link.help.panel = true.

This 5 minute video may explain more: http://screencast.com/t/ctJKW7iFI

The feedback tool sends an email containing all known relevant info automatically included
(eg, site URL, username, browser and version, plugins installed) see: http://blogs.it.ox.ac.uk/adamweblearn/2016/04/the-contact-us-tool/

###Icons

The Feedback tool's icons are all Font Awesome icons and are controlled by the following bundles:
* content_icon_no_translate
* ask_icon_no_translate
* technical_icon_no_translate
* suggest_icon_no_translate
* supplemental_a_icon_no_translate
* supplemental_b_icon_no_translate

Deployment
----------------

The Feedback tool is intended to be added to every site in much the same way as the Help tool
- it is placed just above the help tool. It is intended to replicate the "Contact Us" facility you may see on Google or other websites.


Installation and Configuration
------------------------------

The code builds as a webapp which you can drop straight into your tomcat. To
configure the tool, set these properties in your sakai.properties or
local.properties.

    feedback.featureSuggestionUrl
    feedback.helpPagesUrl
    feedback.supplementaryInfo
    feedback.helpAddress

###The address to which technical feedback and queries will be sent if mail.support is not appropriate.

    feedback.technicalAddress
	
Examples:

    feedback.featureSuggestionUrl=http://sakaifeature.myinstitution.ac.uk
    feedback.helpPagesUrl=http://sakaihelp.myinstitution.ac.uk
    feedback.supplementaryInfo=This is a chunk of really helpful \
    supplementary information. Enjoy!
    feedback.technicalAddress=feedback@sakai.myinstitution.ac.uk

If you have configured Recaptcha in your Sakai, Feedback will use it to
validate unauthenticated technical feedback reports, ie, when non-logged in users use the Feedback tool.

Please see the [default.sakai.properties](https://github.com/sakaiproject/sakai/blob/master/config/configuration/bundles/src/bundle/org/sakaiproject/config/bundle/default.sakai.properties#L4429) for further informaiton on the properties.


Forcing the Tool onto Existing Sites
---------------------------

To backfill existing sites to have the Feedback Tool. you need to run a quartz
job to add the tool to all the sites. There is a Job called "Backfill tool into sites"
which when run can add a specific tool to all sites that match a particular type.


Forcing the Tool to Stay on Sites
---------------------------

You can force all your sites to have a copy of this tool with the following lines
in your sakai.properties

    # Make sure the feedback tool can't be removed from sites.
    poh.uneditables=sakai.feedback
    poh.unhideables=sakai.feedback


Turning Off Automatically Including the Tool in New Sites
---------------------------------------------------------

The way the Feedback tool works by default is that, when you are creating a new
 site, when you get to the 'Site Tools' page where you can choose the tools
 to include on the new site, the Feedback tool is automatically ticked and greyed out
  (like the Site Info tool).  To turn off this feature so that the tool is not ticked
  or greyed out by default, you need to remove the words "required=true"
  in 2 places from the 'toolOrder.xml' file in the kernel module.


Developers
----------

The Feedback tool is written using a mixture of Java, Javascript and Handlebars
templates. A servlet (FeedbackServlet.java) and JSP page (bootstrap.jsp) are
used to initialise the page shell with Javascript variables. Javascript then
takes over and renders templates into the shell when a link is clicked. Forms
are submitted to an EntityProvider (FeedbackEntityProvider.java) and that
provider does the emailing. Some protection against DDoS attacks is provided by
an optional Recaptcha integration. There's a template for every page type although
they are all compiled from WEB-INF/templates into templates/all.handlebars. One
download with all the templates compiled to JS.

To compile the templates use handlebars.js

    npm install -g handlebars

The precompile step is available through the plugin

    mvn3 install -P templates

