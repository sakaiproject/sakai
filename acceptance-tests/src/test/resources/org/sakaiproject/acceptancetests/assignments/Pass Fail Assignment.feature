Feature: Pass / Fail Assignment
Scenario:Pass / Fail Assignment as an Instructor
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I click on Assignment 
        And I should have couple of students submit the assignment
        And I click on In/New counts or grade
        And I click on student's name to open the grading page
        And I scroll down to assignment submission ,enter a comment inside curly braces 
        And I select pass or fail for the student
        And I click save and release to student, top of page displays a checked checkbox with the message: "Comments and/or grade have been saved."
Scenario:Pass / Fail Assignment as an Student
        Given I logged in as a Student
        When I navigate to Site1 site
        And I am a Student
        And I click on Assignments
        Then I should see the status for the assignment shows "Returned"
        And I click open the assignment
        Then I should see grade, instructors comments in red text below your submission text