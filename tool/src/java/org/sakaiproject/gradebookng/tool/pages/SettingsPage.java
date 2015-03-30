package org.sakaiproject.gradebookng.tool.pages;


/**
 * Settings page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SettingsPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public SettingsPage() {
		disableLink(this.settingsPageLink);
		
	}
}
