package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author
 *
 */
public class GroupAutoCreateViewParameters extends SimpleViewParameters {

    public String id;

    public GroupAutoCreateViewParameters(String id) {
		super();
		this.id = id;
	}
    
    public GroupAutoCreateViewParameters() {
		super();
	}
    
	public GroupAutoCreateViewParameters(String viewId, String id) {
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
