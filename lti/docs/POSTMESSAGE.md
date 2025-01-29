LTI and cross-window post messages
==================================

Back in the day, Canvas, Lumen Learning, Sakai and others started
supporting the ability for a tool nestled in an iframe to inform
its parent about something even if it the parent and frame are
served from different origins.

The trick is to use web messaging:

https://www.w3.org/TR/2012/WD-webmessaging-20120313/

The messages and parameters started out through informal
and ad hoc cooperation and are increasingly becoming official
and mainstream in this 1EdTech document for example:

https://www.imsglobal.org/spec/lti-cs-pm/v0p1

This is a list of some of the post messages supported by Sakai.

Asking Sakai for which messages it supports
-------------------------------------------

A tool in an iframe can request which messages Sakai supports using
the `lti.capabilities` message as described in:

https://www.imsglobal.org/spec/lti-cs-pm/v0p1#example-capabilities-messages

Sakai also accepts the "legacy" variant of this message
as `org.imsglobal.lti.capabilities`.

Storing and Retrieving Key / Value data in Sakai's parent frame
---------------------------------------------------------------

As part of the LTI 1.3 approach to addressing the difficulty of setting cookies
in an iframe, Sakai supports the `lti.put_data` and `lti.get_data` messages as per:

https://www.imsglobal.org/spec/lti-pm-s/v0p1

Sakai also accepts the "legacy" variants of this message as `org.imsglobal.lti.get_data`
and `org.imsglobal.lti.put_data`.

Request to have the iframe closed
---------------------------------

The LTI Dynamic Registration specification introduced the `lti.close` message:

https://www.imsglobal.org/spec/lti-dr/v1p0

Sakai also accepts the "pre-marketing-rebrand" variant of this message
as `org.imsglobal.lti.close`.

Resize iframe
-------------

To communicate that a tool has been resized it would run the following JavaScript:

    parent.postMessage(JSON.stringify({
        subject: "lti.frameResize",
        height: num
    }), "*");

This was part of the early Lumen / Sakai / Canvas work and is supported in
Canvas, Sakai, D2L, Moodle and potentially other systems.  It is in effect
a de-facto standard that tools probably should be using so that they look nice
in these systems.

To test this in Sakai or in Canvas, you can use this as an LTI 1.1 endpoint:

https://www.tsugi.org/lti-test/resize.htm

Use any key/secret. You press a button and this tool resizes itself
randomly each time the button is pressed.  Watch the console
to see what is happening.

Refresh enclosing page
----------------------

This is an extension that was proposed back in some of the very early Lumen work
and Sakai supports the `lti.pageRefresh` message:

    parent.postMessage(JSON.stringify({
        subject: "lti.pageRefresh"
    }), "*");

This refreshes the outer page containing the frame and most likely will re-launch the LTI
tool in the iframe.  It has seen little or no use.

Notify that LTI has Changed a Grade in an iframe
------------------------------------------------

When the SakaiGrader is being used to grade a student submission in an iframe with next/back options,
the tool can notify the enclosing frame that it has made an LTI grade change through
the following postMessage:

    parent.postMessage(JSON.stringify({
        subject: "lti.gradeChangeNotify"
    }), "*");

It can have parameters but Sakai already knows which grades are visible on the screen and
handles the message apccordingly.  If you want to test or experiment with this feature,
Tsugi sends the message on the next screen view after sending an LTI grade message.



