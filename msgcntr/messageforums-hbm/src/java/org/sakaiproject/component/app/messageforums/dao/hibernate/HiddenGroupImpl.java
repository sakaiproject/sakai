package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.HiddenGroup;
import org.sakaiproject.api.app.messageforums.PrivateForum;

public class HiddenGroupImpl implements HiddenGroup {

	private Long id;
	private String groupId;
	protected Integer version;
	private Area area;
	
	public HiddenGroupImpl(){
		
	}
	
	public HiddenGroupImpl(String groupId){
		this.groupId = groupId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public Integer getVersion(){
		return version;
	}
	
	public void setVersion(Integer version)
	{
		this.version = version;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

}
