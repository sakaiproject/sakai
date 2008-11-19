package uk.ac.lancs.e_science.profile2.hbm;

import java.util.Date;

import org.apache.log4j.Logger;



public class ProfileFriends {

	private transient Logger log = Logger.getLogger(ProfileFriends.class);

	private long id;
	private String userUuid;
	private String friendUuid;
	private int relationship;
	private boolean confirmed;
	private Date createdDate;
	
	public ProfileFriends(){
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

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	
}
