package org.sakaiproject.delegatedaccess.model;


import java.io.Serializable;

/**
 * This is a serialized representation of a Sakai Tool
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class ListOptionSerialized implements Serializable {
	private String id;
	private String name;
	private boolean selected = false;

	public ListOptionSerialized(String id, String name, boolean selected){
		this.id = id;
		this.name = name;
		this.selected = selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}