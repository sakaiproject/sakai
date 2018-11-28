Feature: Date Test3
Scenario: Student opens assignment after due date has passed 
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I create an assignment with 5 minutes due from now
        And I post it
Scenario: Verifying due date as a student
        Given I logged in as an Student
        When I navigate to Site1 site
        And I am a student
        And I click on Assignments
        Then I should see the correct date/time
        And I click on assignment 
        Then I should see that the due date/time inside the assignment is correct
        And I repeat the above 2 steps for several more students, all in different time zones.