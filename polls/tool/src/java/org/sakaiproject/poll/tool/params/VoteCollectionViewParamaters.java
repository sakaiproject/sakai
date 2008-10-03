package org.sakaiproject.poll.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class VoteCollectionViewParamaters extends SimpleViewParameters {
	
	public String id;
	
	public VoteCollectionViewParamaters() {
		
	}
	
	public VoteCollectionViewParamaters(String viewId) {
		this.viewID = viewId;
	}
	
	public VoteCollectionViewParamaters(String viewId, String id) {
		this.viewID = viewId;
		this.id = id;
	}

}
