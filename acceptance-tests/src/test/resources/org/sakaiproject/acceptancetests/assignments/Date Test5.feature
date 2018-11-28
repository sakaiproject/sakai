    
Feature: Date Test5
Scenario: Announcement about the open date 
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I create an assignment 
        And I check the box to "Add an announcement about the open date to Announcements"
        And I select the email option from dropdown
        And I post the assignment        
Scenario: Verifying due date as a student
        Given I logged in as an Student
        When I navigate to Site1 site
        And I am a student
        And I should see an announcement for the assignment on the overview page for the course 
        And I click on Announcement in the navigation menu 
        Then I should see that the assignment announcement