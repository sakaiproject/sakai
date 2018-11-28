Feature: Date Test1 
Scenario: Instructor wants to hide the due date for an assignment
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I create a new assignment 
        And I check the box for "Hide due date from students"
        And I post the assignment
Scenario: Verifying due date as a student
        Given I logged in as an Student
        When I navigate to Site1 site
        And I am a Student
        And I click on assignment
        Then I should not see due date
        And I click on assignment to open
        Then I should not see the due date in the assignment