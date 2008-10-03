package org.sakaiproject.poll.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class VoteCollectionViewParameters extends SimpleViewParameters {
	
	public String id;
	
	public VoteCollectionViewParameters() {
		
	}
	
	public VoteCollectionViewParameters(String viewId) {
		this.viewID = viewId;
	}
	
	public VoteCollectionViewParameters(String viewId, String id) {
		this.viewID = viewId;
		this.id = id;
	}

}
