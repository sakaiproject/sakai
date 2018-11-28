Feature: Points Assignment - No gradebook
      
Scenario: Letter Grade Assignment as instructor
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I click on Assignment 
        And I should have couple of students submit the assignment
        And I click on In/New counts or grade
        And I click on student's name to open the grading page
        And I scroll down to Assignment submission, enter a comment inside curly braces, give the student a grade 
        And I click on "save and release to student"
        Then I should see the message "comments and/or grade have been saved"
Scenario: Letter Grade Assignment as Student
        Given I logged in as a Student
        When I navigate to Site1 site
        And I am a Student
        And I click on Assignments
        Then I should see the status for the assignment shows "Returned"
        And I click open the assignment
        Then I should see grade, instructors comments in red text below the submission text 