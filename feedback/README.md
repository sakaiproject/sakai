Sakai Feeback Tool
==================

Overview
--------

This tool allows a Sakai user to report problems with a site's content or
functionality. Reports are sent as emails to the problem site's contact email,
or, if that hasn't been specified, to a user selected site maintainer. In
addition to the two reports, there is a link to an area where you can suggest
new features. This link has to be configured from sakai.properties.

Installation and Configuration
------------------------------

The code builds as a webapp which you can drop straight into your tomcat. To
configure the tool, set these properties in your sakai.properties or
local.properties.

    feedback.featureSuggestionUrl=http://sakaifeature.myinstitution.ac.uk
    feedback.helpPagesUrl=http://sakaihelp.myinstitution.ac.uk
    feedback.supplementaryInfo=This is a chunk of really helpful \
    supplementary information. Enjoy!

# The address to which technical feedback and queries will be sent.

    feedback.technicalAddress=feedback@sakai.myinstitution.ac.uk

If you have configured Recaptcha in your Sakai, Feedback will use it to
validate unauthenticated technical feedback reports.

Developers
----------

The feedback tool is written using a mixture of Java, Javascript and Handlebars
templates. A servlet (FeedbackServlet.java) and JSP page (bootstrap.jsp) are
used to initialise the page shell with Javascript variables. Javascript then
takes over and renders templates into the shell when a link is clicked. Forms
are submitted to an EntityProvider (FeedbackEntityProvider.java) and that
provider does the emailing. Some protection against DDoS attacks is provided by
an optional Recaptcha integration. There's a template for every page although
they are all compiled from WEB-INF/templates into templates/all.handlebars. One
download with all the templates compiled to JS.

To compile the templates use handlebars.js

    npm install -g handlebars

The precompile step is available through the plugin

    mvn3 install -P templates

