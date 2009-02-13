package uk.ac.lancs.e_science.profile2.api.entity.model;


/**
 * Model for allowing RESTful access to a list of users's friends
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class FriendsEntity {
	
	private String[] userIds;

	/**
	 * Default constructor
	 */
	public FriendsEntity() {
	}
	
	/**
	 * One call constructor
	 * 
	 * @param usersPresent
	 */
	public FriendsEntity(String[] userIds) {
		this.setUserIds(userIds);
	}

	public void setUserIds(String[] userIds) {
		this.userIds = userIds;
	}

	public String[] getUserIds() {
		return userIds;
	}
	
	

}
