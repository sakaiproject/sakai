@automated
Feature: Can Logout

	Background:
		Given I am logged in

  Scenario: Can logout
    When I click the "loginLink1" button
    Then I should see "Gateway" site