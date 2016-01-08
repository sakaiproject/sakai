package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.sakaiproject.user.api.User;

import lombok.Getter;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String userUuid;

	/**
	 * If displaying an eid, this is the one to display
	 */
	@Getter
	private final String displayId;

	@Getter
	private final String displayName;

	public GbUser(final User u) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
	}

}
