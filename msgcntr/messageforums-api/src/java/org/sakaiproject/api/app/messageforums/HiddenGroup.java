package org.sakaiproject.api.app.messageforums;

public interface HiddenGroup {
	public Long getId();
	public void setId(Long id);
	public Integer getVersion();
	public void setVersion(Integer version);
	public Area getArea();
	public void setArea(Area area);
	
	public String getGroupId();
	public void setGroupId(String groupId);
}
