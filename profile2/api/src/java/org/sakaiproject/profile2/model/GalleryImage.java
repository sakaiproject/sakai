package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * Gallery image container and hibernate model.
 */
public class GalleryImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String userUuid;
	private String mainResource;
	private String thumbnailResource;
	private String displayName;
	
	/**
	 * Empy constructor
	 */
	public GalleryImage() {
		
	}

	/**
	 * Creates a new instance of GalleryImage.
	 */
	public GalleryImage(String userUuid, String mainResource,
			String thumbnailResource, String displayName) {
		
		this.userUuid = userUuid;
		this.mainResource = mainResource;
		this.thumbnailResource = thumbnailResource;
		this.displayName = displayName;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}
