@Manual
Feature: Basic GUI Testing Announcement 
	This tests the announcement tool
Scenario: Before adding announcement 
#this is 
	Given I am logged in as "Instructor" 
	When I navigate to "Sit1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	Then I should see 8 tabs called 'Add','Merge','Reorder','Options','Permissions','View Drop Down','Help Button','Link Button' 
Scenario: After adding announcement 
	Given I am logged in as "Instructor" 
	When I navigate to "Sit1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	Then I should see Table listing announcements in the site, with the 'Subject','Saved By','Modified Date','For','Beginning Date','Ending Date','Remove?' Columns 
	And I should see Edit link under each listed announcement 
Scenario: Checking Permissions for Instructor 
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	And I click the Permissions link 
	Then I should see Permissions for all students, with 'Read', And Instructor should have 'Read','Create','Delete all','Delete own announcements','Edit all announcements','Edit own announcements','Access all group announcements','Read all draft' And Teaching assistant should have 'Read' Permissions 
Scenario: Checking Options 
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	And I click on Options link 
	Then I should see the message 'You are currently setting options for announcements.",Display Options: Sortable table view,Sortable table view with announcement body,List view with announcement body, Characters in body (All by default),RSS Feed Options public announcements only: 'RSS Alias','RSS URL',Display Limits: 'Number of days in the past','Number of announcements' 
	And I should see Update  and Cancel Buttons 
Scenario: Checking Reorder link 
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	And I click on Reorder link 
	Then I should see 'Back to Announcements tab',Reorder Announcements 
	And Message: 'To reorder, drag and drop list items and then click Update.' 
Scenario: Checking Reorder link after announcement has been added 
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	And I click on Reorder link 
	Then I should see Text: 'Undo last and Undo all (Not links unless you have dragged and dropped at least one announcement to reorder- when you drag and drop an announcement, Undo last and Undo all should become links)',Series of links:  'Sort by subject | Sort by author | Sort by beginning date | Sort by ending date | Sort by modified date','List of announcements you can drag and drop to reorder' 
	And I should see Update and Cancel buttons 
Scenario: Checking Merge link 
	Given I am logged in as "Instructor" 
	When I navigate to "Site1" site 
	And I am an Instructor 
	And I click the Announcement tool 
	And I click on Merge link 
	Then I should see Show Announcements from another site 
	And Message: "Select what announcements you want to merge into this site.",A table listing your other sites and a Show Announcements column with a checkbox under Show Announcements for each site 
	And I should see the Save and Cancel buttons 
	