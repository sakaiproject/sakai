
Support for Emerging "lti" postmessages
---------------------------------------

Sakai has support for an informal standard currently supported
by Canvas, Lumen Learning, and others that allows a tool nestled
in an iframe to inform its parent that it needs to so a resize
even if it the parent and frame are served from different origins.

The trick is to use web messaging:

https://www.w3.org/TR/2012/WD-webmessaging-20120313/

The messages and parameters are defined by convention and
hopefully will evolve to being a standard.  For example,
to commuicate that a tool has been resized it would 
run the following JavaScript:

    parent.postMessage(JSON.stringify({
        subject: "lti.frameResize",
        height: num
    }), "*");


Support in Sakai
----------------

It is necessary to add a message listener at each of the points in Sakai 
where iframe markup is generated that might include an LTI URL.  So far
the following tools have been changed:

* Lessons
* LTI Portlet (External Tool)
* Iframe tool

There may be more iframes that we identify as we go forward.

Use in Sakai Tool Markup
------------------------

Code has been added to headscripts.js to send this message if the 
Sakai tool is using setMainFrameHeight() to communicate height changes
to the parent document.  The code detects whether or not we are
same or different origins and uses the old resize trick or the 
postMessage as appropriate.

So this means that auto-resizing Sakai tools will resize when
placed in an iframe Canvas or a Sakai hosted on a different domain.

Test Harness
------------

To test this in Sakai or in Canvas, you can use this:

https://online.dr-chuck.com/sakai-api-test/resize.htm

Use any key/secret. You press a button and this tool resizes itself
randomly each time the button is presed.  Watch the console
to see what is happening.



