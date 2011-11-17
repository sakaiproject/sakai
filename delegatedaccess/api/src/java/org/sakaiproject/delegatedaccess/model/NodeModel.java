package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
	private Date shoppingPeriodStartDate = new Date();
	private Date shoppingPeriodStartDateOrig = new Date();
	private Date shoppingPeriodEndDate = new Date();
	private Date shoppingPeriodEndDateOrig = new Date();
	private String shoppingPeriodAuth;
	private String shoppingPeriodAuthOrig;
	private List<PermissionSerialized> shoppingPeriodPerms;
	private List<PermissionSerialized> shoppingPeriodPermsOrig;
	private boolean addedDirectChildrenFlag = false;

	public NodeModel(String nodeId, HierarchyNodeSerialized node,
			boolean directAccess, final Map<String, List<String>> realmMap,
			String realm, String role, NodeModel parentNode,
			List<ToolSerialized> restrictedTools, Date shoppingPeriodStartDate,
			Date shoppingPeriodEndDate,
			List<PermissionSerialized> shoppingPeriodPerms,
			String shoppingPeriodAuth, boolean addedDirectChildrenFlag){
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
		this.shoppingPeriodAuth = shoppingPeriodAuth;
		this.shoppingPeriodAuthOrig = shoppingPeriodAuth;
		this.shoppingPeriodEndDate = shoppingPeriodEndDate;
		this.shoppingPeriodEndDateOrig = shoppingPeriodEndDate;
		this.shoppingPeriodStartDate = shoppingPeriodStartDate;
		this.shoppingPeriodStartDateOrig = shoppingPeriodStartDate;
		this.shoppingPeriodPerms = shoppingPeriodPerms;
		this.shoppingPeriodPermsOrig = copyPermsList(shoppingPeriodPerms);
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
	}
	
	private List<PermissionSerialized> copyPermsList(List<PermissionSerialized> permsList){
		List<PermissionSerialized> returnList = new ArrayList<PermissionSerialized>();
		for(PermissionSerialized permS : permsList){
			returnList.add(new PermissionSerialized(permS.getPermissionId(), permS.isSelected()));
		}
		
		return returnList;
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
		if(realm != null && realmOrig != null){
			if(!realm.equals(realmOrig))
				return true;
		}else if(realm == null || realmOrig == null){
			return true;
		}
		if(shoppingPeriodStartDate != null && shoppingPeriodStartDateOrig != null){
			if(!shoppingPeriodStartDate.equals(shoppingPeriodStartDateOrig))
				return true;
		}else if(shoppingPeriodStartDate == null || shoppingPeriodStartDateOrig == null){
			return true;
		}
		if(shoppingPeriodEndDate != null && shoppingPeriodEndDateOrig != null){
			if(!shoppingPeriodEndDate.equals(shoppingPeriodEndDateOrig))
				return true;
		}else if(shoppingPeriodEndDate == null || shoppingPeriodEndDateOrig == null){
			return true;
		}
		
		
		if(role != null && roleOrig != null){
			if(!role.equals(roleOrig))
				return true;
		}else if(role == null || roleOrig == null){
			return true;
		}
		
		if(shoppingPeriodAuth != null && shoppingPeriodAuthOrig != null){
			if(!shoppingPeriodAuth.equals(shoppingPeriodAuthOrig))
				return true;
		}else if(shoppingPeriodAuth == null || shoppingPeriodAuthOrig == null){
			return true;
		}
		return directAccessOrig != directAccess || isRestrictedToolsModified() || isSelectedPermsModified();
	}
	private boolean isSelectedPermsModified(){
		for(PermissionSerialized origPerm : shoppingPeriodPermsOrig){
			for(PermissionSerialized perm : shoppingPeriodPerms){
				if(perm.getPermissionId().equals(origPerm.getPermissionId())){
					if(perm.isSelected() != origPerm.isSelected()){
						return true;
					}
				}
			}
		}
		return false;
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
	
	public Date getInheritedShoppingPeriodEndDate(){
		return 	getInheritedShoppingPeriodEndDateHelper(parentNode);
	}
	
	private Date getInheritedShoppingPeriodEndDateHelper(NodeModel parent){
		if(parent == null){
			return null;
		}else if(parent.isDirectAccess()){
			return parent.getShoppingPeriodEndDate();
		}else{
			return getInheritedShoppingPeriodEndDateHelper(parent.getParentNode());
		}
	}
	
	public Date getInheritedShoppingPeriodStartDate(){
		return getInheritedShoppingPeriodStartDateHelper(parentNode);
	}

	private Date getInheritedShoppingPeriodStartDateHelper(NodeModel parent){
		if(parent == null){
			return null;
		}else if(parent.isDirectAccess()){
			return parent.getShoppingPeriodStartDate();
		}else{
			return getInheritedShoppingPeriodStartDateHelper(parent.getParentNode());
		}
	}
	
	public String getInheritedShoppingPeriodAuth(){
		return getInheritedShoppingPeriodAuthHelper(parentNode);
	}
	
	private String getInheritedShoppingPeriodAuthHelper(NodeModel parent){
		if(parent == null){
			return "";
		}else if(parent.isDirectAccess()){
			return parent.getShoppingPeriodAuth();
		}else{
			return getInheritedShoppingPeriodAuthHelper(parent.getParentNode());
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

	public void setToolRestricted(String toolId, boolean restricted){
		for(ToolSerialized tool : restrictedTools){
			if(tool.getToolId().equals(toolId)){
				tool.setSelected(restricted);
				break;
			}
		}
	}

	public Date getShoppingPeriodStartDate() {
		return shoppingPeriodStartDate;
	}

	public void setShoppingPeriodStartDate(Date shoppingPeriodStartDate) {
		this.shoppingPeriodStartDate = shoppingPeriodStartDate;
	}

	public Date getShoppingPeriodEndDate() {
		return shoppingPeriodEndDate;
	}

	public void setShoppingPeriodEndDate(Date shoppingPeriodEndDate) {
		this.shoppingPeriodEndDate = shoppingPeriodEndDate;
	}

	public String getShoppingPeriodAuth() {
		return shoppingPeriodAuth;
	}

	public void setShoppingPeriodAuth(String shoppingPeriodAuth) {
		this.shoppingPeriodAuth = shoppingPeriodAuth;
	}

	public List<PermissionSerialized> getShoppingPeriodPerms() {
		return shoppingPeriodPerms;
	}

	public void setShoppingPeriodPerms(List<PermissionSerialized> shoppingPeriodPerms) {
		this.shoppingPeriodPerms = shoppingPeriodPerms;
	}
	
	public void setPermissionSelected(String permId, boolean restricted){
		for(PermissionSerialized perm : shoppingPeriodPerms){
			if(perm.getPermissionId().equals(permId)){
				perm.setSelected(restricted);
				break;
			}
		}
	}
	
	public boolean hasAnySelectedPermissions(){
		if(shoppingPeriodPerms != null){
			for(PermissionSerialized perm : shoppingPeriodPerms){
				if(perm.isSelected())
					return true;
			}
		}
		return false;
	}

	public String[] getNodePermissionsSelected(){
		List<PermissionSerialized> mySelectedPermissions = getSelectedPermissions();
		if(mySelectedPermissions == null || mySelectedPermissions.size() == 0){
			mySelectedPermissions = getInheritedSelectedPermissions();
		}

		if(mySelectedPermissions == null || mySelectedPermissions.size() == 0){
			return new String[0];
		}else{
			String[] selectedPermissionsArray = new String[mySelectedPermissions.size()];
			int i = 0;
			for(PermissionSerialized perm : mySelectedPermissions){
				selectedPermissionsArray[i] = perm.getPermissionId();
				i++;
			}
			return selectedPermissionsArray;
		}
	}


	public List<PermissionSerialized> getInheritedSelectedPermissions(){
		return getInheritedSelectedPermissionsHelper(parentNode);
	}

	private List<PermissionSerialized> getInheritedSelectedPermissionsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess() && parent.hasAnySelectedPermissions()){
			return parent.getSelectedPermissions();
		}else{
			return getInheritedSelectedPermissionsHelper(parent.getParentNode());
		}
	}

	public List<PermissionSerialized> getSelectedPermissions(){
		List<PermissionSerialized> returnList = new ArrayList<PermissionSerialized>();
		for(PermissionSerialized perm : shoppingPeriodPerms){
			if(perm.isSelected())
				returnList.add(perm);
		}
		return returnList;
	}

	public boolean isAddedDirectChildrenFlag() {
		return addedDirectChildrenFlag;
	}

	public void setAddedDirectChildrenFlag(boolean addedDirectChildrenFlag) {
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
	}
}
