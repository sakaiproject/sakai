package org.sakaiproject.tool.gradebook.ui.helpers.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import org.sakaiproject.tool.gradebook.Assignment;

public class FinishedHelperViewParams extends SimpleViewParameters{
	
	public Long id;
	public String name;
	
	public FinishedHelperViewParams(){}
	
	public FinishedHelperViewParams(String viewId, Long id, String name){
		super(viewId);
		this.id = id;
		this.name = name;
	}
}