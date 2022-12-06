
Testing SakaiPlus
=================

Start up a Sakai

Add an LTI 1.1 and and LTI Advantage tool that can return grades to your Sakai.  This could
be Trophy or LMSTest, or really any LTI tool.   We will want to test both LTI 1.1 and LTI Advantage
from the "Plus Site" and track grade flow back to the "Main Site".

Follow the instructions in [Installing in Sakai](INSTALL-SAKAI.md) to set up a Sakai talking to Sakai.

Create an instructor account and student account.  Make sure both accounts have email addresses.
This allows Sakai to keep you logged into both sites at the same time.  Don't use SakaiPlus Launch
as 'admin' unless you add an email address to the admin account.  SakaiPlus works best when incoming
accounts have email addresses so each user gets only one "user record" in SakaiPlus.

Create a Sakai Site as the instructor we will call this the "Main Site" - this site will serve as
a proxy for the launch, roster, grade book, and plus launch as if it were in Canvas/D2L/Blackboard, etc.
Whilst creating the site, add the Grade book, Lessons, and Plus Launch tools to you under-construction
the Main Site.  If you have installed the Plus Launch to correctly it should be available at the bottom
of the tool list under "External Tools".

After the site has been created, add the student account to the site.  Don't log into the student yet - they
should just be in the roster in the main site.

As Instructor, Launch the Plus Launch link from the Main Site. You should now have two
tabs - one on the Main Site and one on the Plus Site.  The instructor can change the Site Description in
and site title.  It is nice to change the site title of the Plus Site to help you keep the two sites
straight whilst testing.

The default configuration is to construct the Plus Site from a `!plus.site` template.  This template
site has a number of Sakai tools and the "Sakai Plus" tool that lets a teacher monitor how SakaiPlus
is functioning.  The Sakai Plus tool in the Plus Site is quite different from the Sakai Launch tool
in the Main Site.

From the Plus site, look at Site Info and verify that the roster has both the instructor and
student (i.e. before the student has even launched SakaiPlus).

In the Plus Site, add Lessons, Grade book, Samigo, Forums, and Assignments if they are not there.
The "sakai.plus" tool in the Plus site is quite different from the "Plus Launch" tool in the
main site.  Do not add the Plus Launch tool in the Plus Site - or you will end up with three
sites and inception.  It should technically work - but will give you a headache :).

Go into the Sakai Plus tool.  You should be able to see some information and a debug log of recent
activity.  At this point you should see one launch and one roster retrieval.  The log of the
retrieval should show that both the instructor and student records were retrieved.

Go into Grade book and add a column.  Give it a name, score, and due date.  Save it and verify it
exists in the Plus Site.  Go into the Plus tool and you should see a debug entry for creating
the score via the AGS service.  Switch to the Main Site and check the Grade book and verify
that the score and due date match between the sites.

The go back to the Plus Site and edit the column details, change the score and due date and save.
Then check the Plus tool and verify that an update web service request was sent.  Then go into the
Main site and verify that whatever values you changed are correct.

Now close the tab with the Plus Site and go back to the Main Site and re-launch the Plus
Site.  Then use the Plus tool to look at the debug.  If it has been more than 5 minutes
the roster should be re-retrieved. If less that five minutes between launches, the roster
will not be re-retrieved.

Now close the tab with the Plus Site again and go back to the Main Site and re-launch the Plus
Site.  Then use the Plus tool to look at the debug.  Now you will see that since there
were two launches within five minutes - there won't be a new roster retrieval for the latest
launch.

Go into the grade book in the Plus Site, add a score for your student.  Check the Plus
tool and make sure a web service call happened and was successful.  Then go to the
Main Site and check the grade made it.

Go back to Plus site and add a comment in the grade book - check that it makes it
to the Main grade book.

Go back to Plus site and edit the comment and score in the grade book - check
that both makes it to the Main grade book.

Now log in as the student, go to the Main Site - check the Grade book - the student
should see the latest values.  Launch into the Plus Site and check the grade book -
again all values should be correct.

Launch the Plus tool as the student - it should not show any logging data. After this
test, you can hide the tool from students - it is not intended (so far) to give students
any UI - in the future we might add a student view of this tool.

One feature of SakaiPlus is site title synchronization.  One each launch, the site title
is passed from the main site to the plus site.   You can test by changing the main site
title then launching the plus site and watching the title change.  If you change the plus
site title and re-launch from the main site the plus site title should be overwritten.
It can be a little weird for there to be two titles in the top bar - but you can keep track
of which is which by the tools in each site or the content in the site - perhaps something
on the overview page.  This loop-back pattern is not really intended for production
use or deployment - it is really just for testing.

Now we need to generate grades in the Plus site and keep checking to see they make it to
the Main site.  Here are some scenarios:

* Lessons launching an an LTI 1.1 tool
* Lessons launching an an LTI Advantage tool
* Assignments - Local - Associate with existing grade book item
* Assignments - Local - Create new grade book item (verify points make it to Main)
* Assignment with an LTI 1.1 tool
* Assignment with an LTI Advantage tool
* Assignments - Peer graded
* Forums
* Samigo

At some point add more students to the course.   The next launch from a newly added account
or an existing account should pull down the roster.  Instructor launches delay pulling down
a new roster until five minutes pass.  Student launches delay pulling down a new roster
until 30 minutes pass.

One thing to test after you add a few more students is the Grade book feature to
set all the blank columns to some score.  When this is done in Sakai - all the new
scores should be sent to the Main Site.

As a note, SakaiPlus handles numeric scores.  Letter scores and pass/fail scores in the Plus site
will not be transported to the Main site.


TODO
----

Document a test plan for the Deep Link / Content Item use cases with Plus

Test with zero, one and two allowedTools - it treats these use cases quite differently.
If there is > 1 tool, you see a set of cards (like Tsugi) to allow you to choose which
Sakai tool to install.  With 1 tool, it just auto-chooses that tool and installs it with
no list of cards.  If there are zero tools (not a very useful use case) it falls back to
installing `sakai.site`.

You need to launch tools in an iframe and then in a new window.  It is easy to do this in
Lessons.   Neither should show any of the site list or tool list navigation (see screenshots
in JIRA)



