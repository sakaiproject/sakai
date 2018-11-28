Feature:  Add an announcement with no Email Notification

      Tests that you can add an announcement


Scenario: Add an announcement as an Instructor
       
        Given I am logged in as "Instructor"
        When I navigate to "Site1" site
        And I am an Instructor
        And I click the Announcement tool
        Then I should see the tabs called 'Add','Merge','Reorder','Options','Permissions','Help Button','Link Button','View Drop Down'
        When I click on Add
        Then I should see Post Announcement page with 'Title','body' as required field
        When I enter Title,body and click on post announcement
        Then I should see the announcement under main announcement page
        When I click on Home link 
        Then I should see Announcement under the recent announcements section .


Scenario: Add an announcement as a Student

        Given I am logged in as "Student"
        When I navigate to "Site1" site
        And I am an Student
        And I click the Announcement tool
        And I click on Announcement Title
        Then I should see the body of announcement
        And I should go to site where announcement was created
        And I click on announcement title 
        Then I should see the body of announcement
        When I click on announcements 
        Then I should see newly created announcement in the list
        When I click on Announcement title
        Then I should see the body of announcement

Scenario: Add an announcement with Email as an Instructor.

        Given I am logged in as "Instructor"
        When I navigate to "Site1" site
        And I am an Instructor
        And I click the Announcement tool
        When I click on Add
        And I should Enter an announcement 'title', 'body', Choose High-all participants from email notification dropdown.
        And I click on Post Announcement.
        Then I should receive an email for the new announcement. 
        When I click on Home link 
        Then I should see Announcement under the recent announcements section.

Scenario: Add an announcement with Email as an Student
        Given I am logged in as "Student"
        When I navigate to "Site1" site
        And I am an Student
        When I check Email
        Then I should see email received
        When I click on Announcement link
        And I should go to site where announcement was created
        When I click on announcement title 
        Then I should see the body of announcement
        When I click on announcements 
        Then I should see newly created announcement in the list
        When I click on Announcement title
        Then I should see the body of announcement


Scenario: Add an announcement with Specific Dates as Instructor

        Given I am logged in as "Instructor"
        When I navigate to "Site1" site
        And I am an Instructor
        And I click the Announcement tool
        And I click on Add
        When I enter 'Title','Body','Specify Dates','check box beside beginning and set time to 15 minutes from now','Select High-All Participants from email notification dropdown'
        When I click Post announcement
        Then I should see the new announcement which is greyed out under main announcement page
        When I click on home link
        Then I should not see the annount under recent announcements

Scenario: Add an announcement with Specific Dates as Student

        Given I am logged in as "Student"
        When I navigate to "Site1" site
        And I am an Student
        When I click on Announcements link
        Then I should not see the newly created announcement 
        When I go to site where announcement is created
        Then I should not see the newly created announcement of home page
        When I click the announcement
        Then I should not see the newly created announcement under the Announcement page

Scenario: Checking announcement after begin date has passed as Instructor

        Given I am logged in as "Instructor"
        When I navigate to "Site1" site
        And I am an Instructor
        And I should check email after begin date has passed
        When I click on announcement
        Then I should see the announcement in the announcement page

Scenario: Checking announcement after begin date has passed as Student

        Given I am logged in as "Student"
        When I navigate to "Site1" site
        And I am an Student
        And I should check email after begin date has passed
        When I click on announcement
        Then I should see the announcement in the announcement page

Scenario: Creating a group announcement as Instructor

        Given I am logged in as "Instructor"
        When I navigate to "Site1" site
        And I am an Instructor
        And I navigate to site info > manage group >create new group 
        And I click on create new group 
        And I click on Announcement and enter 'title','body',select option: Display this announcement to selected groups only','check in the box to the group created','Choose high-all group members from email notification drop down'
        When I click on post announcement
        Then I should return to main announcement page where new announcement is visible with the group title
        And I click on the home link
        Then Announcement should appear under the recent announcement
        And I check email
        Then I should see the email


Scenario: Checking an announcement as group member


        Given I am logged in as "Student1" 
        When I navigate to "Site1" site
        And I am an Student of group
        When I go to site I should see new announcement on the home page
        And I click on announcement
        Then I should see announcement on the announcement page

Scenario: Checking an announcement as non group member


        Given I am logged in as "Student2" 
        When I navigate to "Site1" site
        And I am an Student of non group
        When I go to site I should not see new announcement on the home page
        And I click on announcement
        Then I should not see announcement on the announcement page


Scenario: Creating a public announcement with RSS feed check


        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body','Select option: This announcement is publicly viewable'
        And I click on post
        Then I should see new announcement with 'public' under for column
        And I click on options on announcement page
        Then options should display
        When I copy RSS url and paste into another browser where you are not logged into Sakai
        Then I should see announcement
        And I click on cancel 
        Then I should return To main announcement page
        And I click on home 
        Then I should see announcement in the home page


Scenario: Checking public announcement as student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should see Announcement on announcement page

Scenario: Checking public announcement hidden with RSS feed check as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body','Select option: This announcement is publicly viewable',select hide
        And I click on post
        Then I should see new announcement with draft is greyed out 'public' under for column
        And I click on options on announcement page
        Then options should display
        When I copy RSS url and paste into another browser where you are not logged into Sakai
        Then I should not see announcement
        And I click on cancel 
        Then I should return To main announcement page
        And I click on home 
        Then I should not see announcement in the home page

Scenario: Checking public announcement hidden with RSS feed check as Student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should not see Announcement on the announcement page


Scenario: Creating public announcement selected dates with RSS feed check as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body','Select option: This announcement is publicly viewable',select 'Specific dates','click the boxes beside beginning and ending','Enter beginning date 5 minutes from time','Enter an ending date 15 minutes from time'
        And I click on post
        Then I should see new announcement with draft is greyed out 'public' under for column
        And I click on options on announcement page
        Then options should display
        When I copy RSS url and paste into another browser where you are not logged into Sakai
        And I click on home 
        Then I should not see announcement in the home page


Scenario: Checking public announcement selected dates with RSS feed check as Student


        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should see Announcement on the announcement page
        And I copy and paste RSS Url where I have not logged into Sakai after the beginning time has come and before ending time has passed 
        Then I should see the announcement on the announcement page


Scenario: Checking public announcement After beginning time and before ending time  with RSS feed check as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        Then I should see announcement on the announcement page
        And I copy and pasted the RSS url into another browser after beginning time has come and ending time has passed
        Then I should see the announcement on the announcement page


Scenario: Checking public announcement After beginning time and before ending time with RSS feed check as Student
        
        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should not see Announcement on the announcement page
        And I copy and paste RSS Url where I have not logged into Sakai after ending time has passed
        Then I should not see the announcement on the announcement page

Scenario: Checking public announcement After ending time has passed with RSS feed check as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        Then I should not see announcement on the announcement page 

Scenario: Checking public announcement After ending time has passed with RSS feed check as Student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should not see announcement on the announcement page after the end time has passed
        


Scenario: Creating site announcement hidden as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I navigate to Announcement > Add announcements 
        Then I enter 'Title','Body','Hide'
        And I click on Post announcement
        And I click on home link
        Then I should not see announcement under the recent announcements section of home page

Scenario: Creating site announcement hidden as Student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should not see announcement on the announcement


Scenario: Creating announcement on selected dates as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body','Specific dates','click the boxes beside beginning and ending','Enter beginning date 15 minutes from time','Enter an ending date 15 minutes from time'
        And I click on post
        Then I should see new announcement greyed out on announcement page 
        And I click on home 
        Then I should not see announcement in the home page


Scenario: Checking announcement selected dates as Student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should not see an announcement on the announcement


Scenario: Checking announcement on After Beginning time and before ending time as Instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        Then I should see an announcement 

Scenario: checking announcement on After Beginning time and before ending time as Student

        Given I am logged in as "Student" 
        When I navigate to "Site1" site
        And I am an Student
        And I click on Announcement
        Then I should see an announcement 


Scenario: checking announcement on After ending time as instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        Then I should see an announcement as greyed out

Scenario: checking announcement on After ending time as student

        Given I am logged in as "student" 
        When I navigate to "Site1" site
        And I am an student
        And I click on Announcement
        Then I should not see an announcement 

Scenario: creating announcement with an attachment as instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body'
        And I click on add attachment button
        And I click on Browse to attach a file
        And I click continue
        And I click on post 
        Then I should see announcement with attachment icon 
        And I click on home
        Then I should see an announcement with an attachment icon

Scenario: checking announcement with an attachment as student
  

        Given I am logged in as "student" 
        When I navigate to "Site1" site
        And I am an student
        And I click on Announcement
        Then I should see an announcement with attachment icon

Scenario: creating an announcement with low priority as instructor

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I make sure one of the student site whose email has set their my workspace > preferences > announcements to do not send me low priority announcement
        And I make sure that different student whose email has set their my my workspace > preferences >announcements to send me each notification aeparately 
        And I click on Announcement
        And I click on add 
        Then I enter 'Title','Body','email notification - low'
        And I click on post announcement
        Then I should see announcement on main announcemnt page
        And I click on home
        Then I should see an announcement on home page
        And I check email
        Then I should not see email receipt notification

Scenario: creating an announcement with low priority as student1

        Given I am logged in as "student1" 
        When I navigate to "Site1" site
        And I am an student1
        And I click on Announcement
        And I should see announcement on announcement page

Scenario: creating an announcement with low priority as student1
 
        Given I am logged in as "student2" 
        When I navigate to "Site1" site
        And I am an student2
        And I click on Announcement
        And I should see announcement on announcement page