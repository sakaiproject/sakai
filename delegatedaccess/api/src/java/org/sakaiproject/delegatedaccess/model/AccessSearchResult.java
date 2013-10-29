package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessSearchResult implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id;
	private String eid;
	private String displayName;
	private String sortName;
	private int level;
	private int type;
	private List<String> restrictedTools = new ArrayList<String>();
	private List<String> hierarchyNodes;
	private String nodeId;
	private boolean canEdit = false;
	private String[] access = null;
	
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}
	public String getSortName() {
		return sortName;
	}
	public void setSortName(String sortName) {
		this.sortName = sortName;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public List<String> getHierarchyNodes() {
		return hierarchyNodes;
	}
	public void setHierarchyNodes(List<String> hierarchyNodes) {
		this.hierarchyNodes = hierarchyNodes;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public boolean isCanEdit() {
		return canEdit;
	}
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
	public String[] getAccess() {
		return access;
	}
	public void setAccess(String[] access) {
		this.access = access;
	}
	public List<String> getRestrictedTools() {
		Collections.sort(restrictedTools);
		return restrictedTools;
	}
	public void setRestrictedTools(List<String> restrictedTools) {
		this.restrictedTools = restrictedTools;
	}
}
