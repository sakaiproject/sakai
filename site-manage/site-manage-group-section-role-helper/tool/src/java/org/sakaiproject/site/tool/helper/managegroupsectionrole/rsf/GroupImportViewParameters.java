package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
* 
* Params for the group import process
*
*/
public class GroupImportViewParameters extends SimpleViewParameters {
    
	public String status;
		
		
	public GroupImportViewParameters(String viewID, String status) {
		this.status= status;
		this.viewID = viewID;
	}
	
	
	public GroupImportViewParameters(String status) {
		super();
		this.status = status;
	}
	
	
	public GroupImportViewParameters() {
		super();
	}   
}
