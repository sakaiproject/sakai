package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class CreateJoinableGroupViewParameters  extends SimpleViewParameters{
	public String id;

    public CreateJoinableGroupViewParameters(String id) {
		super();
		this.id = id;
	}
    
    public CreateJoinableGroupViewParameters() {
		super();
	}
    
	public CreateJoinableGroupViewParameters(String viewId, String id) {
		this.id= id;
		this.viewID = viewId;
	}
    
    public String getId()
    {
    	return id;
    }

	public void setId(String id) {
		this.id = id;
	}
}
