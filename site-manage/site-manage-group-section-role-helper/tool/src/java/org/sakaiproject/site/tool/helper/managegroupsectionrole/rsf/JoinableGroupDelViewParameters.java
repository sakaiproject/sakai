package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class JoinableGroupDelViewParameters extends SimpleViewParameters{
	public String id;

    public JoinableGroupDelViewParameters(String id) {
		super();
		this.id = id;
	}

    public JoinableGroupDelViewParameters() {
		super();
	}

	public JoinableGroupDelViewParameters(String viewId, String id) {
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
