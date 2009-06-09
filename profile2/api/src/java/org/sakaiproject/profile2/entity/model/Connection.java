package org.sakaiproject.profile2.api.entity.model;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;

/**
 * This is the model for a connection, used by the ProfileEntityProvider
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class Connection {

	@EntityId
	private String userUuid;
	
	@EntityTitle @EntityOwner
	private String displayName;
	

	/**
	 * Basic constructor
	 */
	public Connection() {
	}
	
	/**
	 * Full constructor
	 * @param userUuid
	 * @param displayName
	 */
	public Connection(String userUuid, String displayName) {
		super();
		this.userUuid = userUuid;
		this.displayName = displayName;
	}



	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	
}
