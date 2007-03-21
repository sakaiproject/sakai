package org.sakaiproject.poll.tool.params;

import java.io.Serializable;


import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.VoteCollection;

public class VoteBean implements Serializable {

	public VoteCollection voteCollection;
	public Poll poll;
	
	public VoteCollection getVoteCollection(){
		return voteCollection;
	}
	
	public Poll getPoll() {
		return poll;
	}
	public void setPoll(Poll p){
		this.poll = p;
	}
}
