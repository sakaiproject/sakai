package org.sakaiproject.poll.model;

import java.util.List;



import org.sakaiproject.id.cover.IdManager;

public class VoteCollectionImpl implements VoteCollection {

	private String id;
	private List votes;
	private Long pollId;
	public String[] optionsSelected;
	public String option;
	private String submittionStatus;
	
	public VoteCollectionImpl(){
		//need a new id here
		String tid = IdManager.createUuid();
		id = tid;
	}
	
	public void setId(String value) {
		id = value;

	}

	public String getId() {
				return id;
	}

	public void setVotes(List rvotes) {
		votes = rvotes;

	}

	public List getVotes() {
		
		return votes;
	}

	public void setPollId (Long pid) {
		this.pollId=pid;
	}
	public Long getPollId(){
		return this.pollId;
	}
	
	public void setOption(String s){
		this.option = s;
	}
	public String getOption(){
		return option;
	}
	
	public void setOptionsSelected(String[] s) {
		this.optionsSelected = s;
	}
	public String[] getOptionsSelected(){
		return this.optionsSelected;
	}
	
	public void setSubmissionStatus(String s){
		this.submittionStatus=s;
	}
	
	public String getSubmissionStatus(){
		return this.submittionStatus;
	}
}

