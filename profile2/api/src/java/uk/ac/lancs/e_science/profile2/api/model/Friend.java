package uk.ac.lancs.e_science.profile2.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Friend.java
 * 
 * This is a model for storing information about a friend of a user. 
 * It has a limited number of fields and is only ever populated when a List of friends for a given user is required
 * with each friend having one of these objects for fast access to the info required.
 */


public class Friend implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private String displayName;
	private String statusMessage;
	private Date statusDate;
	private boolean confirmed;
	private Date requestedDate;

	
	public String getUserUuid() {
		return userUuid;
	}
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public Date getStatusDate() {
		return statusDate;
	}
	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}
	public boolean isConfirmed() {
		return confirmed;
	}
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	
	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
	}
	public Date getRequestedDate() {
		return requestedDate;
	}

}
