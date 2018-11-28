@Manual

Feature: Instructor submission 2

# Atleast 1 student is needed


Scenario:Student submission 
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I create assignmnet allowing inline & attachment submission
   And I choose a grading option
   And I have a couple of student submit the assignmnet with inline text only
   And I have a couple of student submit the assignmnet with an attachment
   
Scenario:Verify student submission
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I click grade below the assignment to display the submission page
   Then I should verify there is a paperclip icon beside the student which submitted an attachment
   Then I should verify both students have a submission date
   Then I should verify both students have a status 
   Then I should verify both students have grade of "ungraded"
   
Scenario:Grade student and dont release
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   And I click on the grade under the assignment title 
   Then I should see the submissions page
   And I click on the student name to grade the student
   Then I should see the grading page 
   And I enter the grade point for the student 
   And I should click " Save and Don't Release to student"   
   Then I should verify there is no checkmark in the release column on the submission page
   
Scenario:Grade student and release
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   And I click on the grade under the assignment title 
   Then I should see the submissions page
   And I click on the student name to grade the student
   Then I should see the grading page 
   And I enter the grade point for the student 
   And I should click " Save and Release to student"   
   Then I should verify there is checkmark in the release column on the submission page
