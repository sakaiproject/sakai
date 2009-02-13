package uk.ac.lancs.e_science.profile2.api.entity.model;

/**
 * 
 * Model for profile info entities
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileEntity {
	
	private String userId;
	private String nickname;
	
	/**
	 * Default constructor
	 */
	public ProfileEntity() {
	}


	/**
	 * Getters and Setters
	 */
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
}
