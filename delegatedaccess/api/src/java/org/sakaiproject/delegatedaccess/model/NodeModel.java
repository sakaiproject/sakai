package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 
 * This is a Model object for each tree node.  This helps store tree state information as well as get information for the node
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class NodeModel implements Serializable {
	private String nodeId;
	private HierarchyNodeSerialized node;
	private boolean directAccessOrig = false;
	private boolean directAccess = false;
	private String realm = "";
	private String role = "";
	private String realmOrig = "";
	private String roleOrig = "";
	private NodeModel parentNode;
	private List<ToolSerialized> restrictedTools;
	private List<ToolSerialized> restrictedToolsOrig;

	public NodeModel(String nodeId, HierarchyNodeSerialized node, boolean directAccess, final Map<String, List<String>> realmMap, String realm, String role, NodeModel parentNode, List<ToolSerialized> restrictedTools){
		this.nodeId = nodeId;
		this.node = node;
		this.directAccessOrig = directAccess;
		this.directAccess = directAccess;
		this.realm = realm;
		this.role = role;
		this.realmOrig = realm;
		this.roleOrig = role;
		this.parentNode = parentNode;
		this.restrictedTools = restrictedTools;
		this.restrictedToolsOrig = restrictedTools;
	}

	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public HierarchyNodeSerialized getNode() {
		return node;
	}
	public void setNode(HierarchyNodeSerialized node) {
		this.node = node;
	}
	public boolean isDirectAccessOrig() {
		return directAccessOrig;
	}
	public void setDirectAccessOrig(boolean directAccess) {
		this.directAccessOrig = directAccess;
	}

	@Override
	public String toString() {
		return node.title;
	}

	public boolean isDirectAccess() {
		return directAccess;
	}

	public void setDirectAccess(boolean directAccess) {
		this.directAccess = directAccess;
	}

	public boolean isModified(){
		boolean realmChanged = false;
		if(realm != null && realmOrig != null){
			realmChanged = !realm.equals(realmOrig);
		}else if(realm == null || realmOrig == null){
			realmChanged = true;
		}
		boolean roleChanged = false;
		if(role != null && roleOrig != null){
			roleChanged = !role.equals(roleOrig);
		}else if(role == null || roleOrig == null){
			roleChanged = true;
		}
		return directAccessOrig != directAccess || realmChanged || roleChanged || isRestrictedToolsModified();
	}
	
	private boolean isRestrictedToolsModified(){
		for(ToolSerialized origTool : restrictedToolsOrig){
			for(ToolSerialized tool : restrictedTools){
				if(tool.getToolId().equals(origTool.getToolId())){
					if(tool.isSelected() != origTool.isSelected()){
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Will return the inherited role from it's parents and "" if not found
	 * @return
	 */
	public String[] getNodeAccessRealmRole(){
		String[] myAccessRealmRole = new String[]{getRealm(), getRole()};
		if(myAccessRealmRole == null || "".equals(myAccessRealmRole[0]) || "".equals(myAccessRealmRole[1])){
			myAccessRealmRole = getInheritedAccessRealmRole();
		}
		if(myAccessRealmRole == null || "".equals(myAccessRealmRole[0]) || "".equals(myAccessRealmRole[1])){
			return new String[]{"",""};
		}else{
			return myAccessRealmRole;
		}
	}
	
	public String[] getInheritedAccessRealmRole(){
		return getInheritedAccessRealmRoleHelper(parentNode);
	}

	private String[] getInheritedAccessRealmRoleHelper(NodeModel parent){
		if(parent == null){
			return new String[]{"",""};
		}else if(parent.isDirectAccess() && !"".equals(parent.getRealm()) && !"".equals(parent.getRole())){
			return new String[]{parent.getRealm(), parent.getRole()};
		}else{
			return getInheritedAccessRealmRoleHelper(parent.getParentNode());
		}
	}

	public NodeModel getParentNode() {
		return parentNode;
	}

	public void setParentNode(NodeModel parentNode) {
		this.parentNode = parentNode;
	}

	public List<ToolSerialized> getRestrictedTools() {
		return restrictedTools;
	}

	public void setRestrictedTools(List<ToolSerialized> restrictedTools) {
		this.restrictedTools = restrictedTools;
	}
	
	public String[] getNodeRestrictedTools(){
		List<ToolSerialized> myRestrictedTools = getSelectedRestrictedTools();
		if(myRestrictedTools == null || myRestrictedTools.size() == 0){
			myRestrictedTools = getInheritedRestrictedTools();
		}
		
		if(myRestrictedTools == null || myRestrictedTools.size() == 0){
			return new String[0];
		}else{
			String[] restrictedToolsArray = new String[myRestrictedTools.size()];
			int i = 0;
			for(ToolSerialized tool : myRestrictedTools){
				restrictedToolsArray[i] = tool.getToolId();
				i++;
			}
			return restrictedToolsArray;
		}
	}
	
	
	public List<ToolSerialized> getInheritedRestrictedTools(){
		return getInheritedRestrictedToolsHelper(parentNode);
	}
	
	private List<ToolSerialized> getInheritedRestrictedToolsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess() && parent.hasAnyRestrictedToolsSelected()){
			return parent.getSelectedRestrictedTools();
		}else{
			return getInheritedRestrictedToolsHelper(parent.getParentNode());
		}
	}
	
	public List<ToolSerialized> getSelectedRestrictedTools(){
		List<ToolSerialized> returnList = new ArrayList<ToolSerialized>();
		for(ToolSerialized tool : restrictedTools){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}
	
	public boolean hasAnyRestrictedToolsSelected(){
		for(ToolSerialized tool : restrictedTools){
			if(tool.isSelected())
				return true;
		}
		return false;
	}
	
	public void addRestrictedToolId(ToolSerialized tool){
		restrictedTools.add(tool);
	}
	
	public void setToolRestricted(String toolId, boolean restricted){
		for(ToolSerialized tool : restrictedTools){
			if(tool.getToolId().equals(toolId)){
				tool.setSelected(restricted);
				break;
			}
		}
	}

}
