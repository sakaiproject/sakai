package org.sakaiproject.site.tool.helper.managegroup.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author
 *
 */
public class GroupEditViewParameters extends SimpleViewParameters {

    public String id;

    public GroupEditViewParameters(String id) {
		super();
		this.id = id;
	}
    
    public GroupEditViewParameters() {
		super();
	}
    
	public GroupEditViewParameters(String viewId, String id) {
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
