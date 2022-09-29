
TODO LIST
=========

Plus tool button to retrieve membership

Plus tool to resend scores

Build conversion scripts for 23.x

Put contextLog expiry into the event expire batch jobs - Matt will help :)

Make sure columns are created and grades are flowing:
    - Assignments - LTI - Graded in LTI (i.e. StickyGrader)
    - Assignments - Local - Associate with existing gradebook item
    - Assignments - Peer graded
    - Samigo
    - Forums

Understand how to adjust for the weird Canvas "many deployment id" strategy.

Make sakai.plus template site that includes the sakai.plus tool.

Handle paged rosters in the NRPS API - both reading and producing.

Make Plus tool useful for students

Look for or make standard donut chart webcomponent

Put lineitem creation and score sending into database for redo on failure and add batch job

o.s.u.ResourceLoader.getString bundle 'Messages'  missing key: 'Missing Sakai Session'

Make smarter use of cookies in the iframe Content Security Policy - Matt
Understand the ramifications of SameSite None - Perhaps add a CSP header to lock things down

Put context memberships into a batch job - Sam / Matt?

DONE
====

When auto-provisioning set the "Tool Supports LTI 1.3"

Teach admin tool about the setting - add message if not enabled.

Add isDraft() indicator to Tenant List in Admin UI

Make sure columns are created and grades are flowing:
    - Gradebook UI
    - LTI 1.1
    - LTI 1.3
    - Assignments - Local - New Gradebook Item
    - Assignments - LTI - Graded in Sakai

Finish delete comment PR with Adrian and then re-merge to plus
https://sakaiproject.atlassian.net/browse/SAK-47279

Make verbose nicer for Earle (i.e. no System.out.println)

Make sure we don't retrieve the context memberships too often.

Teach Sakai to open new windows using JavaScript SAK-47769
    onclick="window.open(this.href,'_blank');return false;"

Don't put each new syncSiteMemberships processing member=xyzzy@umich.edu in its own debug entry

Make sure we research what it means when the grade is set to "nothing"

Roster delay - Instructor 5 minutes Learner 30 minutes

Expire old ContextLog entries

Make Plus tool useful for instructors

Write Test Plan

Document properties

Rationalise verbose and log.debug

Add `deployment_id` to Context and use it if available - Thanks to Peter Fr.
Implement wildcard or comma separated values for `deployment_id` as option,

Escape from iframe on launch.

Move waterfall-lite to library and use standard spinner

