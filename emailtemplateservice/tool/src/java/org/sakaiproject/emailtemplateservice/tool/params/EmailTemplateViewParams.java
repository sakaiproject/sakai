package org.sakaiproject.emailtemplateservice.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class EmailTemplateViewParams extends SimpleViewParameters {

	public String id;
	
	public EmailTemplateViewParams(String viewId, String id) {
		this.id = id;
		this.viewID = viewId;
	}
	
	
	public EmailTemplateViewParams(String id) {
		super();
		this.id = id;
		
	}
	
	public EmailTemplateViewParams() {
		super();
		
		
	}
}
