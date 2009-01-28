package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

/* FriendAction.java
 * 
 * This is a model to store the actions like adding friends/removing friends etc.
 * Given to the modal windows, they then set the attributes and the calling page knows what to do based on these attributes.
 */


public class FriendAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean requested;
	private boolean confirmed;
	private boolean remove;
	
	public boolean isRequested() {
		return requested;
	}
	public void setRequested(boolean requested) {
		this.requested = requested;
	}
	public boolean isConfirmed() {
		return confirmed;
	}
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	public boolean isRemove() {
		return remove;
	}
	public void setRemove(boolean remove) {
		this.remove = remove;
	}
	
	/*
	 * Default constructor
	 */
	public FriendAction() {
	
	}
	
	
	
}	