@Manual
Feature: Managing announcements 

	Tests that you can add an announcement

Scenario: Add an announcement
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	Then I should the tabs called 'Add','Merge','Reorder','options','Permissions','Help Button','Link Button','View Drop Down' 
	When I click on Add 
	Then I should see Post Announcement page with 'Title', 'body' as required field
	
Scenario: Delete an announcement
	Given This test needs to be written