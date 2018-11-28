@Manual

Feature: Instructor submission 1

# Atleast 1 student is needed


Scenario:Verify links for student submission
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I create an assignment with 1 student submit
   And I click grades under the assignment
   Then I will verify the submission page contains "title of assignment" ,submissions
   Then I should see the Download All ,Upload All , Release Grades links
   Then I shoukd see textbox to assign a grade to participnats without a grade
   Then I should view 1-x of x items
   Then I should see x items dropdown
   
Scenario:Setting for feedback 
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I click on grade under the assignment
   Then  I should see submission page 
   And I click on setting for feedback by expanding this section 
   Then I should see a textarea for writing feedback'
   And I click on checkbox for overwrite current feedback 
   And I click on return feedback to selected student now
   And I click update
   Then I should see the submission page again 
   
Scenario:Selecting feedback for student 
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I click on grade under the assignment
   Then  I should see submission page 
   When I select users by checking the checkbox for the users
   And I check checkbox for allow resubmission
   And I expand the section
   Then I should see a dropdown for number of resubmission allowed
   And I check checkbox with calendar
   And I click update button
   Then I should see submission page

Scenario:Verify student information 
   Given I am logged in as "Instructor"
   When I navigate to "Site1" site
   And I am an Instructor
   And I click the Assignment tool
   Then I should see the Assignment List page
   When I click on grade under the assignment
   Then  I should see submission page  
   Then I should see student information columns "checkbox , paperclip , student , submitted , status , Grade , Release "
   Then I should see the assignmnet details section
   And  I expand the section
   Then I should verify assignmnet details are correct
 
