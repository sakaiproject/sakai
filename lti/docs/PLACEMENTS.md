
Placements and LTI Message Types in Sakai
=========================================


Over the years, when we were installing new LTI tools using the “External Tool” UI
in the Admin Workspace, we have been evolving a set of check boxes that capture the
message types a tool can accept and the placements in the Sakai UI that we want the
tool to appear.

Launch message types and placements are very different concepts and in the pre Sakai-25
UI there was a bit of mixing based on historical accretion of features and evolution
of the LTI standards over time.

One effort in Sakai-25 was to normalize the user interface and reflect how things
turned out after over a decade of feature accretion.

https://sakaiproject.atlassian.net/browse/SAK-49540

Placements in Sakai
-------------------

As we were designing user interfaces to add an LTI Tool or Link some part of Sakai, initially
it was simple to just show all of the tools as selectable options any time the user
said, "I want an LTI tool here".  But not all tools work well in all placements.

Slowly Sakai and other LMS's started adding checkboxes to indicate (i.e. filter)
which tools were available from which "Add an LTI Tool" buttons.  In Sakai-25
this becomes the Placements checkboxes in the LTI Tool insert/edit screen:

    Indicate where in the Sakai User interface that these tools should appear

    [ ] Allow the tool to be selected from Lessons
    [ ] Allow the tool to be used from the rich text editor
    [ ] Allow the tool to be one of the assignment types
    [ ] Allow the tool to be placed in site-level navigation (requires Resource Link launch)
    [ ] Allow the tool to provide a common cartridge response (requires Deep Link launch)

These are mostly self explanatory.  The Administrator can simply tick a check box
and the tool will or won't appear in the appropriate LTI UI selectors.  This does
not affect the launch-ability of already placed LTI tools or tools imported with a
Common Cartridge or through a site copy.  This only indicates if a tool should
be included in the list when the user is about to add a new LTI tool.

The lessons, editor, and assignment placements can place single tools that take
a Resource Link launch or select a tool from a multi-tool "store" using
a Deep Link launch.  These placements check which message type(s) the tool can
handle and choose accordingly.  The Sakai picker code sees a tool that
accepts both message types, it chooses Deep-Link and lets the tool solve it 
through user interaction.

The site level (left) navigation placement only will send a Resource Link
launch and will only work with tools that support that launch message.

The "provide common cartridge" placement is a special version of the Deep Link / 
Content Item launch where the tool picker is instructed to only send back a ZIP
file in the form of an IMS Common Cartridge (IMS CC).  This is then retrieved
and imported into Sakai.  When a tool with this placement checked that supports Deep Linking
launches is present, Lessons will add a link under Lessons -> More Tools called "Import content
from External Tool" that will trigger the Common Cartridge specific flow, retrieve
the cartridge and import it into Lessons.  

Launch Message Types
--------------------

Paired with the placement checkboxes, we also must indicate the types of LTI launches the tool
supports:

    The launch URL for this tool must support at least one launch message
    type. Most tools support one or the other, but some tools do support
    both messages at one URL.

    [ ] The tool URL supports a single LTI tool (Resource Link launch)

    [ ] The tool URL supports a "resource picker" experience with one or more tools (Deep Link launch)

The key here is to check the boxes appropriately based on what launch messages the tool is *capable
of handling* at the specified launch URL that is being entered for the tool.  If you check a box
and Sakai sends the message you indicated was supported - the tool will just fail.

As the message says, some tools *do* accept both message types at the exact same URL.  That
is relatively rare.  What is more common (i.e. Tsugi does this), is that you have a "store"
URL that you put into the tool like:

https://www.tsugicloud.org/tsugi/lti/store/

This URL only accepts Deep Link or Content Item launches and will fail with an error
if you send Tsugi a Resource
Link launch at that URL.  But once you use the picker and select a tool, Tsugi returns a different
launch URL for the *selected* tool like:

https://www.tsugicloud.org/mod/breakout/

This is the URL that is placed in the newly created link within Sakai and Sakai sends a Resource
Link launch message to this new link.  So technically there is not "one url" that is receiving both
types of launch messages - but the www.tsugicloud.org "site" has multiple URLs and different URLs are launched
with different launch messages.  Sakai, Tsugi, and the Deep Link protocol have all that worked out.


A Matrix View of Placements and Launches
----------------------------------------

In this section, we map between the placement types and launch messages:

                                            Deep Link or
Placement         |  Resource Link     |    Content Item
------------------|--------------------|------------------
Lessons           |      Yes           |      Yes(+)
Editor            |      Yes           |      Yes(+)
Assignments       |      Yes           |      Yes(+)
Left Navigation   |    Required        |       n/a
CC Import         |      n/a           |     Required

+ If support for both launch messages is indicated for a tool in
these placements, the placement ignores Resource Link support and
treats the tool as if it only supports Deep Link.

Submission Review Launch
------------------------

There is another LTI Message Type and launch URL that is supported by Sakai that does not appear
anywhere in the Sakai Admin UI.  But it is important to Sakai Admins and 
LTI Tool Developers to know about the message type when integrating into Assignments.

The `SubmissionReview` launch allows a grade book like UI to launch deep into a tool directly to
a grading screen for a particular student - usually in an iframe within a grading UI.  In Sakai
when an instructor starts grading an LTI tool in Assignments, they are presented with a list of student submissions.
They can jump to any student's submission or even use arrows to page through each student's submission.
It is a nice UI.

The mechanics of this launch are defined in:

https://www.imsglobal.org/spec/lti-sr/v1p0#availability-and-placement-of-submission-review-request-message

The availability of the Submission Review launch and the URL to launch to is part of the Deep Linking
protocol.   When the tool is installing a Resource link, it can provide the LMS a URL that can accept
the Submission Review message.  When the LMS sees this indicator in its grading UI code, it sends
a Submission Review launch with a `for_user` claim to indicate the (a) we want to see a grading screen and (b) which
user the LMS is currently grading.

https://www.imsglobal.org/spec/lti-sr/v1p0#ltisubmissionreviewrequest-claims

Submission review and the Sakai Assignments UI is enhanced using the `gradingProgress` feature of the
Assignments and Grades Service.

https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service

Properly done, this leads to a very nice UI flow.  Tsugi has several tools that implement this flow
very nicely and if you are interested a demo is simple to do.  We test these flows as a part of
our regular QA of Assignments + LTI.

Since this is triggered by signals in the return data from the Deep-Linking flow, there is no reason to
add a check box for the Submission Review launch message to the tool editing screens.

Privacy Launch Message Type (experimental)
------------------------------------------

If you look very closely at the privacy section the tool insert / edit page, you can see
a checkbox about the "Privacy Launch".

    Privacy Settings:
    
    [ ] Send User Names to External Tool
    [ ] Send Email Addresses to External Tool
    [ ] Tool supports the privacy launch message (experimental)


If you look closely at the Internet, you will not find any mention of a specification that describes
the "LTI Privacy Launch" protocol.  It is because (as of the writing of this documentation) the
Privacy Launch specification is essentially done but not approved and released yet.  Often
in these situations, Sakai takes a nearly complete specification and implements it
early and then also implements it in Tsugi as a nice "proof of concept".  These early implementations
help other LMS and tool vendors see working code so they are assured that the specification can
be implemented and works well.

That is where this feature is in Sakai - working perfectly and waiting for the official
specification to be released.  It is a nice feature and easy to build - if you are an LMS or Tool
developer and 1EdTech member and want to experiment with solid working code or just get
a demo - contact Dr. Chuck.

If you use Tsugi and Sakai and want to use this feature - try it out.  Just check the box
on a Tsugi tool, then click the "Privacy" link in the External Tool Admin UI - and play around.
It is cool.


History of Content Item and Deep Linking
----------------------------------------

In the beginning of LTI there was the original "LTI launch" which we now call a
"Resource Link" launch.  When you place an LTI tool at various places in a course,
each link has a unique "resource link ID" - which can correspond to a unique column
in the grades.

Canvas added an innovation to their LTI 1.1 where they could launch to a "tool picker" or "tool store"
in an iframe.  The user could then scan through a list of tools and/or resources, then
select one and send the launch URL (or other type of resource) back to the LMS for
insertion into a place like Lessons or Modules.

The community adopted the Canvas idea and added a lot of features and released an IMS standard
that added "Content Item Launch" to LTI 1.1 to support the resource picking use case across
LMS's in a standard way.  Since Content Item was a richer protocol than what Canvas had called
"Content Item" - there was a little mis-match between Canvas and the rest of the market for a
while.

This was resolved with the complete re-engineering of the "resource picker" use case in
LTI 1.3 / LTI Advantage.  The new protocol accomplished much the same thing as "Content Item"
but using LTI 1.3 security patterns.  The latest (and preferred) protocol is called
"Deep Linking 2.0".

https://www.imsglobal.org/spec/lti-dl/v2p0

Many LMS's (Sakai included) found it quite easy to support Content Item and Deep Linking
in parallel, because while the syntax of the messages being exchanged is
quite different - the flow, features, and approach of the two protocols were almost identical

It has been almost 10 years since Deep Linking was released so as of Sakai 25 we
only mention Deep Linking but note here that Content Item also continues to
be supported for LTI 1.1 tools that want to provide a "resource picker" experience to
their users in Sakai.

