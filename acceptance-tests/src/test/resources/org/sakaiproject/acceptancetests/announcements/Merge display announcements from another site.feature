Feature: Merge (display) announcements from another site

Scenario: Make sure you have atleast 2 sites, both with the announcements tool added, and one with Resources added(Site creation scenario needs to be executed before this scenario and make sure that site is created)

		Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I make sure that I have atleast 3 announcements in each site
        And I go to Site1 announcement > add
        And I enter announcement title
        And I click in the editor for the announcements body 
        And I click the editor's image icon
        And I click browse server
        And I click the upload file icon
        And I uploaded the file to resources file by browsing and selecting an image 
        And I click the file name of the image you just uploaded to select it
        And I click ok
        And I enter alternative text in the image properties dialog box
        And I click ok
        Then I should see the image added to announcement body
        And I click Post Announcement
        Then I should see the anouncement added when returned to announcement 
        And I click on the announcement title to make sure that image is available


Scenario: Add two students/members to site


		Given I am logged in as "Instructor" 
        When I navigate to "Site2" site
        And I am an Instructor
        And I make sure that I have atleast 3 students, all the studentsare also members of site1,Add students to the site if necessary
        And I click on merge button
        Then I should see the Show Announcements from Another Site page with a list of sites
        And I click in the box to the right of site 1
        And I click save
        Then I should see all the anouncements from the site1 added when returned to announcements, the edit link should not appear under the anouncement title and the site 1 is entered under the site column for these announcement
        And I click home link 
        Then I should see newly merged announcements that meet Announcement Options criteria under the Recent Announcements section of Home page 

Scenario: Member of both site 1 and site 2

		Given I am logged in as "Student1" 
        When I navigate to "Site2" site
        And I am an Student1
        And I click on announcements link
        Then I should see all the announcements,image in the announcement that has the image.
        Then I should see newly merged announcements that meet Announcement Options criteria under the Recent Announcements section of Home page 
        And I click announcement 
        Then I should see all the announcements, able to see the image in the announcement that has the image.


Scenario: Member of both site2 only
		Given I am logged in as "Student3"
        When I navigate to "Site2" site
        And I am an Student3
        And I click on announcements link
        Then I should not see all the announcements,image in the announcement that has the image.
        Then I should see newly merged announcements that meet Announcement Options criteria under the Recent Announcements section of Home page 
        And I click announcement 
        Then I should see all the announcements but not able to access image in the announcement from Site 1 with image embedded,The embedded image displays its alt text instead