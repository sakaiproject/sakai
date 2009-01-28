package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;

public class ProfileImage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String userUuid;
	private String mainResource;
	private String thumbnailResource;
	private boolean current;
	
	/* 
	 * Empty constructor
	 */
	public ProfileImage() {
	}

	/* 
	 * Constructor to create a ProfileImage record in one go
	 */
	public ProfileImage(String userUuid, String mainResource, String thumbnailResource, boolean current) {
		this.userUuid = userUuid;
		this.mainResource = mainResource;
		this.thumbnailResource = thumbnailResource;
		this.current = current;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getMainResource() {
		return mainResource;
	}

	public void setMainResource(String mainResource) {
		this.mainResource = mainResource;
	}

	public String getThumbnailResource() {
		return thumbnailResource;
	}

	public void setThumbnailResource(String thumbnailResource) {
		this.thumbnailResource = thumbnailResource;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}
	
	
}
