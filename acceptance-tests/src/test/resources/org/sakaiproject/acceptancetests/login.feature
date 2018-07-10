@Automated 
Feature: Can Login 

	Tests that a user can login

Scenario: Can login as admin 

	Given I am logged out
	And I am on the "https://trunk-oracle.nightly.sakaiproject.org/portal/" page 
	When I enter "admin" for username 
	And I enter "admin" for password 
	And I click the "submit" button 
	Then I am logged in 
	Then I should see "Administration Workspace" site