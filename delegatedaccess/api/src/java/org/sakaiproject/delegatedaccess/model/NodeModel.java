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
	private boolean addedDirectChildrenFlag = false;	
	private boolean shoppingPeriodAdmin = false;
	private boolean shoppingPeriodAdminOrig = false;
	private Date updatedDate = new Date();
	private Date processedDate = new Date();


	public NodeModel(String nodeId, HierarchyNodeSerialized node,
			boolean directAccess, String realm, String role, NodeModel parentNode,
			List<ToolSerialized> restrictedTools, Date shoppingPeriodStartDate,
			Date shoppingPeriodEndDate,
			String shoppingPeriodAuth, boolean addedDirectChildrenFlag, boolean shoppingPeriodAdmin,
			Date updatedDate, Date processedDate){

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
		this.restrictedToolsOrig = copyRestrictedTools(restrictedTools);
		this.shoppingPeriodAuth = shoppingPeriodAuth;
		this.shoppingPeriodAuthOrig = shoppingPeriodAuth;
		this.shoppingPeriodEndDate = shoppingPeriodEndDate;
		this.shoppingPeriodEndDateOrig = shoppingPeriodEndDate;
		this.shoppingPeriodStartDate = shoppingPeriodStartDate;
		this.shoppingPeriodStartDateOrig = shoppingPeriodStartDate;
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
		this.shoppingPeriodAdmin = shoppingPeriodAdmin;
		this.shoppingPeriodAdminOrig = shoppingPeriodAdmin;
		this.updatedDate = updatedDate;
		this.processedDate = processedDate;
	}
	
	private List<ToolSerialized> copyRestrictedTools(List<ToolSerialized> tools){
		List<ToolSerialized> returnList = new ArrayList<ToolSerialized>();
		for(ToolSerialized tool : tools){
			returnList.add(new ToolSerialized(tool.getToolId(), tool.getToolName(), tool.isSelected()));
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
		if(directAccessOrig != directAccess){
			return true;
		}
		
		if(shoppingPeriodAdmin != shoppingPeriodAdminOrig){
			return true;
		}
		//only worry about modifications to a direct access node
		if(directAccess){
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



			

			if(isRestrictedToolsModified()){
				return true;
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
	
	public String getNodeShoppingPeriodAuth(){
		if(getShoppingPeriodAuth() != null && !"".equals(getShoppingPeriodAuth()) && !"none".equals(getShoppingPeriodAuth())){
			return getShoppingPeriodAuth();
		}else{
			return getInheritedShoppingPeriodAuth();
		}
	}

	public Date getNodeShoppingPeriodStartDate(){
		if(getShoppingPeriodStartDate() != null){
			return getShoppingPeriodStartDate();
		}else{
			return getInheritedShoppingPeriodStartDate();
		}
	}
	
	public Date getNodeShoppingPeriodEndDate(){
		if(getShoppingPeriodEndDate() != null){
			return getShoppingPeriodEndDate();
		}else{
			return getInheritedShoppingPeriodEndDate();
		}
	}
	
	public String[] getInheritedAccessRealmRole(){
		return getInheritedAccessRealmRoleHelper(parentNode);
	}

	private String[] getInheritedAccessRealmRoleHelper(NodeModel parent){
		if(parent == null){
			return new String[]{"",""};
		} else if (parent.isDirectAccess() && !"null".equals(parent.getRealm())
				&& !"".equals(parent.getRealm())
				&& !"".equals(parent.getRole())
				&& !"null".equals(parent.getRole())) {
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

	public Date getUpdatedDate() {
		return updatedDate;
	}
	
	public Date getNodeUpdatedDate(){
		if(updatedDate != null){
			return updatedDate;
		}else{
			return getInheritedUpdatedDate();
		}
	}
	
	public Date getInheritedUpdatedDate(){
		return 	getInheritedUpdatedDateHelper(parentNode);
	}
	
	private Date getInheritedUpdatedDateHelper(NodeModel parent){
		if(parent == null){
			return null;
		}else if(parent.isDirectAccess()){
			return parent.getUpdatedDate();
		}else{
			return getInheritedUpdatedDateHelper(parent.getParentNode());
		}
	}

	public Date getProcessedDate() {
		return processedDate;
	}

	public boolean isAddedDirectChildrenFlag() {
		return addedDirectChildrenFlag;
	}

	public void setAddedDirectChildrenFlag(boolean addedDirectChildrenFlag) {
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
	}

	public boolean isShoppingPeriodAdmin() {
		return shoppingPeriodAdmin;
	}

	public void setShoppingPeriodAdmin(boolean shoppingPeriodAdmin) {
		this.shoppingPeriodAdmin = shoppingPeriodAdmin;
	}
	
	public boolean getNodeShoppingPeriodAdmin(){
		if(isShoppingPeriodAdmin()){
			return true;
		}else{
			return getInheritedShoppingPeriodAdmin();
		}
	}
	
	public boolean getInheritedShoppingPeriodAdmin(){
		return getInheritedShoppingPeriodAdminHelper(parentNode);
	}
	
	private boolean getInheritedShoppingPeriodAdminHelper(NodeModel parent){
		if(parent == null){
			return false;
		}else if(parent.isShoppingPeriodAdmin()){
			return true;
		}else{
			return getInheritedShoppingPeriodAdminHelper(parent.getParentNode());
		}
	}
}
