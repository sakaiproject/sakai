package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * Extension of BasicPerson to include connection related information.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasicConnection extends BasicPerson implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int onlineStatus;

	/**
	 * No-arg constructor
	 */
	public BasicConnection() {
	}
	
	
	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public int getOnlineStatus() {
		return onlineStatus;
	}

}
