Feature: Reorder Announcement

Scenario: Reorder announcements using automatic sorting as an Instructor

		Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement 
        Then I should make sure that their are enough announcements in the site with two different creators with different beginning and end dates
        And I click on reorder 
        And I click sort by subject 
        Then I should see announcements which are sorted alphabetically by subject in descending order 
        And I click on update
        Then I should see the announcements are listed alphabetically by subject in descending order
        And I click on announcements and navigate to reorder page
        And I click on sort by subject twice
        Then I should see announcement sorted alphabetically by subject in ascending order
        And I click on sort by author 
        Then I should see the announcements sorted alphabetically by author in descending order
        And I click on Sort by author
        Then I should see the announcements sorted alphabetically by author in Ascending order
        And I click on Sort by Beginning date
        Then I should see the announcements sorted By beginning date with the earliest one on top
        And I click on sort by ending date 
        Then I should see the announcements sorted By beginning date with the latest one on top
        And I click on Sort by ending date
        Then I should see the announcements sorted By ending date with the earliest one on top
        And I click on sort by ending date 
        Then I should see the announcements sorted By ending date with the latest one on top
        And I click on Sort by Modified date
        Then I should see the announcements sorted By Modified date in descending order
        And I click on sort by Modified date 
        Then I should see the announcements sorted By Modified date in ascending order

Scenario: Reorder announcements using drag and drop as an Instructor

		Given I am logged in as "Instructor" 
        When I navigate to "Site1" site
        And I am an instructor
        And I click on Announcement 
        And I click on reorder 
        And I click on an announcement and drag and drop it to a new position in the list that takes it out of order of modified date 
        And I click on update
        Then I should return to announcement list with the sort order 