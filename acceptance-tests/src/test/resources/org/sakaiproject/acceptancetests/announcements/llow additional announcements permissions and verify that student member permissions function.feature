Feature: Allow additional announcements permissions and verify that student/member permissions function

Scenario: Give students/members permission to add and edit their own announcements as an Instructor
		

		Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement > Permissions
        Then I should select the check boxes under the student column: 'Create announcements,Delete own announcements,Edit own announcements'
        And I click on save
        Then I should return to announcement list

Scenario: Post an announcements as a Student/member

		Given I am logged in as "Student/Member" 
        When I navigate to "Site1" site
        And I am an student
        And I click on announcements
        Then I should see an add tab available at the top, but no edit links for any existing announcements and no remove column in the table listing announcements
        And I click on add
        And I should enter 'text','body'
        And I click on post announcement
        Then I should be taken back to the announcements list with the new announcement displayed at the top,I should have an edit link underneath it, The remove? column should also have been added to the table with an active check box in the remove? column for newly added announcement to allow you to remove it, but Inactive check boxes for all other announcements
        And I click on home
        Then I should see the announcement added in the recent announcements

Scenario: Edit an announcements as a Student/member

		Given I am logged in as "Student/Member" 
        When I navigate to "Site1" site
        And I am an student
        And I click on announcements 
        And I click on edit under announcement 
        Then I should edit the announcement title ,body 
        And I click save changes
        Then I should see the edited announcement listed under return announcement
        And I click add
        And I should enter text,body
        And I click on post announcement
        Then I should be taken back to the announcements list with the new announcement displayed at the top, I should have an edit link underneath it, The remove? column should also have been added to the table with an active check box in the remove? column for 2 announcement to allow you to remove them, but Inactive check boxes for all other announcements

Scenario: Remove an announcements as a Student/member

		Given I am logged in as "Student/Member" 
        When I navigate to "Site1" site
        And I am an student
        And I click on announcements 
        And I check the remove? box for one of the announcement
        And I click on update
        Then I should be taken to a confirmation page: "Are you sure you want to delete the following announcements?"
        And I click on remove 
        Then I should return to announcement list with the deleted item gone

Scenario: create an announcement WITH email notification as a different student/member.


		Given I am logged in as "Student2" 
        When I navigate to "Site1" site
        And I am an student2
        And I click on announcements
        And I click on add
        And I enter text in title,body
        And I select email notification as high - All Participants
        And i click post
        Then I should be taken back to the announcements list with the new announcement displayed at the top, I should have an edit link underneath it, The remove? column should also have been added to the table with an active check box in the remove? column for newly added announcement to allow you to remove them, but Inactive check boxes for all other announcements
        And I click on add
        And I enter text in title,body
        And Under availability select specify dates : select both beginning and end date
        And I enter beginning date of tomorrow
        Then I should be taken back to the announcements list with the new announcement displayed at the top,The text listing the announcement's Subject, Saved By, Modified Date, etc. information should all be in gray, to indicate that it's not available to other students/members yet. I should have an edit link underneath it, The remove? column should also have been added to the table with an active check box in the remove? column for newly added announcement 
        And I click on home
        Then I should see the anouncement added when returned to announcement 

Scenario: Verify email notification and visibility of other student's announcement.

		Given I am logged in as "Student2" 
        When I navigate to "Site1" site
        And I am an student2
        And I click on announcements
        And I check the email
        Then I should see the email received from student2's open announcement
        And I click on home
        Then I should see student2's open announcement listed under recent announcem,ent but not student2's announcement that starts tomorrow.
        And I click on announcements
        Then I should see student2's open announcement but Not student2's announcement that starts tomorrow.There should not be an edit link for student2 announcement,remove check box for the announcement should be disabled

Scenario: View,edit,remove students announcements as an Instructor

		Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an Instructor
        And I click on home
        Then I should see the announcements listed under the recent announcement(not the one that strats tomorrow)
        And I click Announcement
        Then I should see the students announcements listed with the one that opens tomorrow in gray,there should be edit links and active remove? check boxes for all announcements
        And I clcik edit under the announcement that opens tomorrow
        And I should uncheck the box for the beginning date as I navigate to under availability > Specify dates
        And I click save changes
        Then I should Returned to Announcements list with the announcement posted immediately, it should have no Beginning date, but it should have an Ending date.
        And I check the remove? box for different atudent announcement
        And I click update
        And I should click on remove
        Then I should return to announcement list with the deleted item gone

Scenario: Verify changes as student

		Given I am logged in as "Student2" 
        When I navigate to "Site1" site
        And I am a student2 who created the draft announcement that the instructor just edited to post it
        And I click on home
        Then I should see the announcements listed under the recent announcement
        And I click on announcements
        Then I should NOT have an Edit link anymore for the announcement that your instructor edited,this is because it now belongs to the instructor.