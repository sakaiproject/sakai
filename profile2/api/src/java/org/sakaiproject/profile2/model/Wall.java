package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Wall implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private List<WallItem> wallItems = new ArrayList<WallItem>();
	
	public void addWallItem(WallItem wallItem) {
		wallItems.add(wallItem);
	}
	
	public void removeWallItem(WallItem wallItem) {
		wallItems.remove(wallItem);
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public List<WallItem> getWallItems() {
		return wallItems;
	}

	public void setWallItems(List<WallItem> wallItems) {
		this.wallItems = wallItems;
	}
	
}
