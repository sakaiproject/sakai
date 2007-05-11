package org.sakaiproject.sequencing.api;

import org.adl.sequencer.SeqActivityTree;

public interface Sequencer {

	//public LaunchContent navigationRequest(int iRequest);
	
	public void setActivityTree(SeqActivityTree iTree);
	
}
