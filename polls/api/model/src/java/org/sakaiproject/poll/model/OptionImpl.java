package org.sakaiproject.poll.model;



public class OptionImpl implements Option {

	
	private Long pollId;
	private Long id;
	private String text;
	private String status;
	
	public OptionImpl(){
		
	}	
	public OptionImpl(Long oId){
		this.id = oId;
	}
	
	public void setId(Long value) {
		// TODO Auto-generated method stub
		id = value;
	}

	public Long getId() {
		
		return id;
	}

	public void setOptionText(String option) {
		
		text = option;

	}

	public String getOptionText() {
		
		return text;
	}

	public Long getPollId(){
		return pollId;
	}
	
	public void setPollId(Long pollid) {
		this.pollId = pollid;
	}
	
	public void setStatus(String s) {
		this.status = s;
	}
	
	public String getStatus() {
		return this.status;
	}
}
