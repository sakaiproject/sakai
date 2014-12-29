package org.sakaiproject.site.tool.helper.participant.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author
 *
 */
public class AddViewParameters extends SimpleViewParameters {

    public String id;

    public AddViewParameters(String id) {
		super();
		this.id = id;
	}
    
    public AddViewParameters() {
		super();
	}
    
	public AddViewParameters(String viewId, String id) {
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
