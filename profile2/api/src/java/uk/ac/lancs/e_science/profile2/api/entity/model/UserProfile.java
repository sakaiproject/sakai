package uk.ac.lancs.e_science.profile2.api.entity.model;

import java.util.Date;

/**
 * This is the model for a user's profile, used by the ProfileEntityProvider
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class UserProfile {

	private String userUuid;
	private String nickname;
	private Date dateOfBirth;
	private String displayName;

	/**
	 * Basic constructor
	 */
	public UserProfile() {
	}
	
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
}
