package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;


/**
 * Hibernate model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileFriend implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String userUuid;
	private String friendUuid;
	private int relationship;
	private Date requestedDate;
	private boolean confirmed;
	private Date confirmedDate;

	/**
	 * Empty constructor
	 */
	public ProfileFriend(){
	}
	
	/**
	 * Constructor to create a ProfileFriend in one go
	 */
	public ProfileFriend(String userUuid, String friendUuid, int relationship, Date requestedDate, boolean confirmed, Date confirmedDate){
		this.userUuid = userUuid;
		this.friendUuid = friendUuid;
		this.relationship = relationship;
		this.requestedDate = requestedDate;
		this.confirmed = confirmed;
		this.confirmedDate = confirmedDate;
	}
	
	/**
	 * Constrctuor that should be used when requesting a friend as it has preinitialised values
	 */
	public ProfileFriend(String userUuid, String friendUuid, int relationship){
		this.userUuid = userUuid;
		this.friendUuid = friendUuid;
		this.relationship = relationship;
		this.requestedDate = new Date();
		this.confirmed = false;
		this.confirmedDate = null;
	}
	
		
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUserUuid() {
		return userUuid;
	}
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	public String getFriendUuid() {
		return friendUuid;
	}
	public void setFriendUuid(String friendUuid) {
		this.friendUuid = friendUuid;
	}
	public int getRelationship() {
		return relationship;
	}
	public void setRelationship(int relationship) {
		this.relationship = relationship;
	}
	public Date getRequestedDate() {
		return requestedDate;
	}
	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
	}
	public boolean isConfirmed() {
		return confirmed;
	}
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	public Date getConfirmedDate() {
		return confirmedDate;
	}
	public void setConfirmedDate(Date confirmedDate) {
		this.confirmedDate = confirmedDate;
	}

	
}
