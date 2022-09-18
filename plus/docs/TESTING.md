
Start up a Sakai

Add the "sakai.plus" tool to the Administration Workspace if it is not there.

Add an LTI 1.1 and and LTI Advantage tool that can return grades to your Sakai.  This could
be Trophy or LMSTest, or really any LTI tool.   We will want to test both LTI 1.1 and LTI Advantage
from the "Plus Site".

Follow the instructions in [../README.md] to set up a Sakai talking to Sakai.

Create an instructor account and student account.

Create a Sakai Site as the instructor and add the student account to the site - we will call
this the "Main Site" - this site will serve as a proxy for the launch, roster, and gradebook
in Canvas/D2L/Blackboard, etc.

Don't log into the student yet - they should just be in the roster in the main site.

As instructor, add the Gradebook and Lessons to the Main Site. Place the "SakaiPlus Launch" in Lessons
in the Main Site and tell it to open in a new window.

Launch the Sakai Plus Link from the Main Site. You should now have two tabs - one on the
Main Site and one on the Plus Site - they will have the same name and you can't change that.
SakaiPlus keeps the name synchronized at each launch.

From the Plus site, look at Site Info and verify that the roster has both the instructor and
student (i.e. before the student has even launched SakaiPlus).

In the Plus Site, add Lessons, Gradebook, Samigo, Forums, and Assignments.  There is a "sakai.plus"
tool that yon need to add to the site using the Admin because it is not in the Site Info list
of tools.  In time - I might make a plus template that has this tool pre-installed to bypass
this step.

Note that the Plus tool can be added to any site - but it will only show information for
a Plus site (i.e. if you add it to the Main Site you will never see any data).

Go into the Sakai Plus tool.  You should be able to see some information and a debug log of recent
activity.  At this point you should see one launch and one roster retrieval.  The log of the
retrieval should show that both the instructor and student records were retrieved.

Go into Gradebook and add a column.  Give it a name, score, and due date.  Save it and verify it
exists in the Plus Site.  Go into the Plus tool and you should see a debug entry for creating
the score via the AGS service.  Switch to the Main Site and check the Gradebook and verify
that the score and due date match between the sites.

The go back to the Plus Site and edit the colum details, change the score and due date and save.
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

Go into the gradebook in the Plus Site, add a score for your student.  Check the Plus
tool and make sure a web service call happenned and was successful.  Then go to the
Main Site and check the grade made it.  

Go back to Plus site and add a comment in the gradebook - check that it makes it 
to the Main gradebook.

Go back to Plus site and edit the comment and score in the gradebook - check
that both makes it to the Main gradebook.

Now log in as the student, go to the Main Site - check the Gradebook - the student
should see the latest values.  Launch into the Plus Site and check the gradebook - 
again all values should be correct.

Launch the Plus tool as the student - it should not show any logging data. After this 
test, you can hide the tool from students - it is not intended (so far) to give students
any UI - in the future we might add a student view of this tool.

Now we need to generate grades in the Plus site and keep checking to see they make it to
the Main site.  Here are some scenarios:

* Lessons launching an an LTI 1.1 tool
* Lessons launching an an LTI Advantage tool
* Assignments - Local - Associate with existing gradebook item
* Assignments - Local - Create new gradebook item (verify points make it to Main)
* Assignment with an LTI 1.1 tool
* Assignment with an LTI Advantage tool
* Assignments - Peer graded
* Forums
* Samigo

At some point add more students to the course.   The next launch from a newly added account
or an existing account should pull down the roster.  Instructor launches delay pulling down
a new roster until five minutes pass.  Student launches delay pulling down a new roster
until 30 minutes pass.

One thing to test after you add a few more students is the Gradebook feature to
set all the blank columns to some score.  WHen this is done in Sakai - all the new 
scores should be sent to the Main Site.

As a note, SakaiPlus handles numeric scores.  Letter scores and pass/fail scores in the Plus site
will not be transported to the Main site.


TODO
----

Document a test plan for the Deep Link / Content Item use cases with Plus

