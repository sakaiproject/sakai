package uk.ac.lancs.e_science.profile2.hbm;

import org.apache.log4j.Logger;

public class ProfileImage {

	private transient Logger log = Logger.getLogger(ProfileImage.class);

	private long id;
	private String userUuid;
	private int type;
	private String resourceId;
	
	public ProfileImage(long id, String userUuid, int type, String resourceId) {
		super();
		this.id = id;
		this.resourceId = resourceId;
		this.type = type;
		this.userUuid = userUuid;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
}
