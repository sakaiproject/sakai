package org.sakaiproject.poll.model;

import java.util.Date;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.event.api.UsageSession;

public class VoteImpl implements Vote {

	private Long id;
	private String userId;
	private String ip;
	private Long pollId;
	private Date voteDate;
	private Long optionid;
	private String submissionId;	
	
	public VoteImpl(){
		//needed by hibernate
	}
	public VoteImpl(Poll poll, Option option, String subId) {
		
		this.pollId = poll.getPollId();
		this.optionid = option.getId();
		this.submissionId = subId;
		
		// the date can default to now
		voteDate = new Date();
		//user is current user
	    User currentuser = UserDirectoryService.getCurrentUser();
	    userId = currentuser.getId();
	    //set the Ip to the current sessions IP
	    UsageSession usageSession = UsageSessionService.getSession();
	    ip = usageSession.getIpAddress();
	}
	
	public void setId(Long value) {
		id =value;
		

	}

	public Long getId() {

		return id;
	}

	public void setUserId(String uid) {
		userId = uid;

	}

	public String getUserId() {

		return userId;
	}

	public void setIp(String value) {
		ip = value;

	}

	public String getIp() {
		
		return ip;
	}

	public void setVoteDate(Date date) {
		this.voteDate = date;

	}

	public Date getVoteDate() {
		
		return this.voteDate;
	}

	public void setPollOption(Long voption) {
		
		optionid = voption;
	}

	public Long getPollOption() {
		
		return optionid;
	}

	public void setPollId(Long value) {
		this.pollId = value;

	}

	public Long getPollId() {
		
		return pollId;
	}


	public void setSubmissionId(String sid) {
		this.submissionId = sid;

	}

	public String getSubmissionId() {
		
		return this.submissionId;
	}
	
	public String toString() {
		
		return this.pollId + ":" + this.userId + ":" + this.ip + ":" + this.optionid;
		
	}

}
