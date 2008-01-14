package org.sakaiproject.poll.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class PollViewParameters extends SimpleViewParameters {
	
	public String id;
	
	
	public PollViewParameters(String viewId, String id) {
		this.id= id;
		this.viewID = viewId;
	}


	public PollViewParameters(String id) {
		super();
		this.id = id;
	}


	public PollViewParameters() {
		super();
	}

	
	
}
