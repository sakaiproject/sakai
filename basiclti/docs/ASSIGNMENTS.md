
Sakai LTI Integration in the Assignments Tool
=============================================

As of Sakai-21, the Sakai Assignments tool has a new feature to support an
"External Tool" / LTI assignment type.  This assignment type compliments the existing
placement types that =have been in Sakai for a long time:

* Lessons Placement - Learning App and External Tool
* Rich Text Editor Placement
* Left navigation bar placement - Site Manage / Manage External Tools

The Assignments placement is a nice feature because none of these other placements really support
open and close times, due dates, etc.  LTI Advantage Deep Linking has very nice support that
allows external tools to specifs open and close datea, and maximum points.  Maximum points are supported
in many of the placements but only assignments supports any kind of date workflow.

Another change you will see in the Sakai-20 (after 20.3) and Sakai-21 is a refactoring of the two
separate concepts of "What kind of launch the endpoint wants to receive?" (protocol) and
"Where should this appear in the Sakai UI?" (placement).

Creating an LTI Assignment in Sakai
-----------------------------------

It is pretty simple, install or edit an LTI tool and indicate that it supports the Assignment
placement.  Two great tools to use are the Tsugi Trophy tool and the Tsugi LMSTest tool because you
can exercise a lot of features.

Go into Assignments and Add a new Assignment.  Add a new assignment and then scroll
down to the Submission Type and change it to "External Tool (LTI)".  Then you will see
a "Select External Tool (LTI)" button.  Pressing the button will launch a resource picker
similar to the "Install Learning App" in Lessons.  Choose a tool, and go through the selection
process.  If you are using something like Tsugi's Trophy just before you will be given an option
to "Configure the LineItem" (<a href="images/assignments/01-Tsugi-LineItem.png" target="_blank">image</a>).
LineItem is part of the DeepLink response:

See http://www.imsglobal.org/spec/lti-dl/v2p0 - search for "lineitem", "available", and "submission".

This allows the tool to request a different scoreMaximum and available and submission date ranges.
The Sakai Assignment tool looks for these values when you are placing an assignment.

Once you pick these values (or your tool picks these values), they are sent back to Sakai.  When
you come back to the Sakai assignments flow, Sakai parses these values and puts them into the
Assignments's UI in the appropriate locations.  The instructor can scroll around and change all these
value before saving the assignment.  Sakai simply sees this as "defaults" or "suggestions" and lets
the instructor make the final decision within Sakai.

A tool can find out the actual values set or updated by the instruction
using the Assignments and Grades Service or by using the following custom
variable substitution values:

    ResourceLink.available.startDateTime
    ResourceLink.available.endDateTime
    ResourceLink.submission.startDateTime
    ResourceLink.submission.endDateTime

Once you are happy with the assignment, save it.

Once you save the assignment, you should be able to go into the gradebook and see the
new column with the correct maximum score value.

Sakai LTI Assignments and Gradebook Integration
-----------------------------------------------

LTI Assignments are integrated into the Sakai gradebook differently than
other assignment types.

For non-LTI assignment submission types, when you create an Assignment and
select "Send Grades to Gradebook" - the Assignments tool creates an externally
managed Gradebook column that the LTI services cannot use.  The column exists somewhere
deep inside Assignment but is not available via the grade book API that LTI Assignments
and Grades Service uses.

When the Assignments tool is creating a grade book column associated with an LTI
launched assignment - the grade is created in a way that it can be used by LTI
Services.  It also means that these LTI assignment grades can be overridden
from within the gradebook (like most other grade book columns).

I debated as to whether to try to implement LTI grades as externally maintained
in the Assignments tables but it would make complying with all
of the rules of LTI Advantage very difficult.

For example, if you have a tool that is used with two placements with one in
assignments and in lessons, and the tool asks "show me my grade columns" using
the LTI Advantage Assignments and Grades Services - it is supposed to see both
the assignments columns and the lessons columns.   A pretty cool feature that
tools will find quite useful.  This also avoids adding yet another externally
maintained class of grades that we have to move back into the grade book at some
point in the future.

This is a fun thing to test by placing the Trophy tool in Assignments, and the
LMSTest tool in Lessons and retrieving the AGS lineitems - you will see both
and be able to access either line item from either placement - which is cool.
For this to work they my be two placements of the same 
tool (i.e. sakai.tsugicloud.org).  AGS only shows lineitems across placements
of the same server.

Another side effect of storing LTI Assignment grades directly in the gradebook
is that when an assignment is deleted - the grades and grade column are *not*
deleted.   This give instructors flexibility - it allows a lot of use cases - but also
it can lead to some "oops" moments - but at least the grades should not be lost
unless the column is explicitly deleted in the grade book.



