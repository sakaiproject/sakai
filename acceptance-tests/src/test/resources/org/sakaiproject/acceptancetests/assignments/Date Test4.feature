Feature: Date Test4
Scenario: Student opens assignment after due date has passed 
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I create an assignment 
        And I check the box to "Add due date to calender"
        And I post the assignment
        And I make sure to have the calendar tool added to the site
Scenario: Verifying due date as a student
        Given I logged in as an Student
        When I navigate to Site1 site
        And I am a student
        And I should see an indicator on the calendar for the assignment due date 
        And I click on Calender in the navigation menu 
        Then I should see that the assignment due date