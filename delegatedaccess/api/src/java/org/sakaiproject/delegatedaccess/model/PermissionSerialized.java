package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;

public class PermissionSerialized implements Serializable{
	private boolean selected = false;
	private String permissionId;
	
	public PermissionSerialized(String permissionId, boolean selected){
		this.setPermissionId(permissionId);
		this.setSelected(selected);
	}

	public void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}

	public String getPermissionId() {
		return permissionId;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
}
