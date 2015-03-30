package org.sakaiproject.gradebookng.tool.pages;


/**
 * Permissions page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class PermissionsPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public PermissionsPage() {
		disableLink(this.permissionsPageLink);
		
	}
}
