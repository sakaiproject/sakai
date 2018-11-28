Feature: Checkmark Assignment
Scenario: Checkmark Assignment as instructor
        Given I logged in as an Instructor
        When I navigate to Site1 site
        And I am an Instructor
        And I click on Assignment
        And I create an assignment with checkmark grading
        And I should make sure that I have couple of students submitted the assignment
        And I clcik on In/New counts or grade
        And I click on student's name to open the grading page
        And I scroll down to assignment submission, enter a comment inside curlr braces 
        And I click save and release to students
        Then I should see checked checkbox with the message "comments and/or grade have been saved" 
        
Scenario: Checkmark Assignment as student
        Given I logged in as a Student
        When I navigate to Site1 site
        And I am a Student
        And I click on Assignments
        Then I should see assignment status as returned
        And I click on assignment to open
        Then I should see the grade and the instructors comments in red text below your submission text