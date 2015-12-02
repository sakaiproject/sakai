package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;

import org.sakaiproject.user.api.User;

/**
 * DTO for a user. Enhance as required.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private String userUuid;
	
	/**
	 * If displaying an eid, this is the one to display
	 */
	@Getter
	private String displayId;
	
	@Getter
	private String displayName;
	
	public GbUser(User u){
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
	}
	
}
