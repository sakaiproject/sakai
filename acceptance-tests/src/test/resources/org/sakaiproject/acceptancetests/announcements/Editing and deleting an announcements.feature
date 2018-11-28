Feature: Editing and deleting an announcements

Scenario: Editing an announcement

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement
        And I should see announcement on announcement page
        And I should navigate to edit underneath an announcement that was posted without email
        Then I should have 'Message: "Update the form, then choose the appropriate button at the bottom.Required items marked with a *" ','Form fields : Title and body','Access : Only members of this site can see this announcement (selected by default),This announcement is publicly viewable,   Display this announcement to selected groups only ','Availability: Show - (Post and display this announcement immediately) (selected by default), Hide - (Draft mode - Do not display this announcement at this time),Specify Dates - (Choose when this announcement will be displayed)','Attachments: List of attachments with links (if any were added), Add Attachments button','Email Notification : None - No notification (selected by default),High - All participants,Low - Not received by those who have opted out','See revision history link (clicking it will show when it was last revised, whether email notification went out, and availability restrictions)','Save Changes, Preview, and Cancel buttons' 
        And I should change the text of the announcement title
        And I should click save changes
        Then I should return to announcemnt list with a new title
        And I should click edit for the announcement 
        And I should click see revision history link
        Then I should see two dates of revision , with 'No notification' as the Notification setting for the most recent edit


Scenario: Edit an announcement that didn't have email notification at last edit and send email notification as instructor.


        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement
        And I should navigate to edit underneath an announcement that was just edited
        And I should select High- All participants for the email notification settings
        And I click save changes 
        Then I should return to the announcement list
        And I should click on edit for the announcement
        And I should click the see revision history link
        Then I should see two dates of revision , with 'No notification' as the Notification setting for the most recent edit
        And I check the email
        Then I should have received email containing the announcement body and title 


Scenario: Edit an announcement that didn't have email notification at last edit and send email notification as Student.

 
        Given I am logged in as "student" 
        When I navigate to "Site1" site
        And I am an student
        And I should check the email 
        Then I should have received email containing the announcement body and title 

Scenario: Remove an announcement

        Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement 
        Then I should make sure that their are enough announcements in the site
        And I should select the check box in the remove? column
        And I should click update
        Then I should see the a confirmation screen with the message: "Are you sure you want to delete the following announcements?" and your selected announcement listed
        And I should click remove
        Then I should return to announcement page with the deleted announcement no longer there.
        And I select two announcements by check boxing the remove? column
        And I click on update
        Then I should see the a confirmation screen with the message: "Are you sure you want to delete the following announcements?" and your selected announcement listed
        And I click remove 
        Then I should return to announcement page with the deleted announcement no longer there and other announcement should be available