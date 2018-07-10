
package org.sakaiproject.acceptancetests.stepdefs;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class Announcements {

	@Given("^I am logged in as \"([^\"]*)\"$")
	public void i_am_logged_in_as(final String arg1) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I navigate to \"([^\"]*)\" site$")
	public void i_navigate_to_site(final String arg1) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I am an Instructor$")
	public void i_am_an_Instructor() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click the Announcement tool$")
	public void i_click_the_Announcement_tool() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should the tabs called 'Add','Merge','Reorder','options','Permissions','Help Button','Link Button','View Drop Down'$")
	public void i_should_the_tabs_called_Add_Merge_Reorder_options_Permissions_Help_Button_Link_Button_View_Drop_Down() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click on Add$")
	public void i_click_on_Add() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Post Announcement page with 'Title', 'body' as required field$")
	public void i_should_see_Post_Announcement_page_with_Title_body_as_required_field() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Given("^This test needs to be written$")
	public void this_test_needs_to_be_written() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see (\\d+) tabs called 'Add','Merge','Reorder','Options','Permissions','View Drop Down','Help Button','Link Button'$")
	public void i_should_see_tabs_called_Add_Merge_Reorder_Options_Permissions_View_Drop_Down_Help_Button_Link_Button(final int arg1)
			throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Table listing announcements in the site, with the 'Subject','Saved By','Modified Date','For','Beginning Date','Ending Date','Remove\\?' Columns$")
	public void i_should_see_Table_listing_announcements_in_the_site_with_the_Subject_Saved_By_Modified_Date_For_Beginning_Date_Ending_Date_Remove_Columns()
			throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Edit link under each listed announcement$")
	public void i_should_see_Edit_link_under_each_listed_announcement() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click the Permissions link$")
	public void i_click_the_Permissions_link() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Permissions for all students, with 'Read', And Instructor should have 'Read','Create','Delete all','Delete own announcements','Edit all announcements','Edit own announcements','Access all group announcements','Read all draft' And Teaching assistant should have 'Read' Permissions$")
	public void i_should_see_Permissions_for_all_students_with_Read_And_Instructor_should_have_Read_Create_Delete_all_Delete_own_announcements_Edit_all_announcements_Edit_own_announcements_Access_all_group_announcements_Read_all_draft_And_Teaching_assistant_should_have_Read_Permissions()
			throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click on Options link$")
	public void i_click_on_Options_link() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see the message 'You are currently setting options for announcements\\.\",Display Options: Sortable table view,Sortable table view with announcement body,List view with announcement body, Characters in body \\(All by default\\),RSS Feed Options public announcements only: 'RSS Alias','RSS URL',Display Limits: 'Number of days in the past','Number of announcements'$")
	public void i_should_see_the_message_You_are_currently_setting_options_for_announcements_Display_Options_Sortable_table_view_Sortable_table_view_with_announcement_body_List_view_with_announcement_body_Characters_in_body_All_by_default_RSS_Feed_Options_public_announcements_only_RSS_Alias_RSS_URL_Display_Limits_Number_of_days_in_the_past_Number_of_announcements()
			throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Update and Cancel Buttons$")
	public void i_should_see_Update_and_Cancel_Buttons() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click on Reorder link$")
	public void i_click_on_Reorder_link() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see 'Back to Announcements tab',Reorder Announcements$")
	public void i_should_see_Back_to_Announcements_tab_Reorder_Announcements() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^Message: 'To reorder, drag and drop list items and then click Update\\.'$")
	public void message_To_reorder_drag_and_drop_list_items_and_then_click_Update() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Text: 'Undo last and Undo all \\(Not links unless you have dragged and dropped at least one announcement to reorder- when you drag and drop an announcement, Undo last and Undo all should become links\\)',Series of links:  'Sort by subject | Sort by author | Sort by beginning date | Sort by ending date | Sort by modified date','List of announcements you can drag and drop to reorder'$")
	public void i_should_see_Text_Undo_last_and_Undo_all_Not_links_unless_you_have_dragged_and_dropped_at_least_one_announcement_to_reorder_when_you_drag_and_drop_an_announcement_Undo_last_and_Undo_all_should_become_links_Series_of_links_Sort_by_subject_Sort_by_author_Sort_by_beginning_date_Sort_by_ending_date_Sort_by_modified_date_List_of_announcements_you_can_drag_and_drop_to_reorder()
			throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Update and Cancel buttons$")
	public void i_should_see_Update_and_Cancel_buttons() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^I click on Merge link$")
	public void i_click_on_Merge_link() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see Show Announcements from another site$")
	public void i_should_see_Show_Announcements_from_another_site() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^Message: \"([^\"]*)\",A table listing your other sites and a Show Announcements column with a checkbox under Show Announcements for each site$")
	public void message_A_table_listing_your_other_sites_and_a_Show_Announcements_column_with_a_checkbox_under_Show_Announcements_for_each_site(
			final String arg1) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@Then("^I should see the Save and Cancel buttons$")
	public void i_should_see_the_Save_and_Cancel_buttons() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

}
