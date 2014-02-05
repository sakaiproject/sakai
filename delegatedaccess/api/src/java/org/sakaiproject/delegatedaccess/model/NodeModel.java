/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


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
	private boolean accessAdmin = false;
	private boolean accessAdminOrig = false;
	private String realm = "";
	private String role = "";
	private String realmOrig = "";
	private String roleOrig = "";
	private NodeModel parentNode;
	private List<ListOptionSerialized> restrictedAuthTools;
	private List<ListOptionSerialized> restrictedAuthToolsOrig;
	private List<ListOptionSerialized> restrictedPublicTools;
	private List<ListOptionSerialized> restrictedPublicToolsOrig;
	private Date shoppingPeriodStartDate = new Date();
	private Date shoppingPeriodStartDateOrig = new Date();
	private Date shoppingPeriodEndDate = new Date();
	private Date shoppingPeriodEndDateOrig = new Date();
	private boolean addedDirectChildrenFlag = false;	
	private boolean shoppingPeriodAdmin = false;
	private boolean shoppingPeriodAdminOrig = false;
	private String siteInstructors;
	private SelectOption roleOption;
	private Date shoppingAdminModified = null;
	private String shoppingAdminModifiedBy = null;
	private Date modified = null;
	private String modifiedBy = null;
	//this flag is used to track accessAdmin access
	private boolean editable = true;
	private boolean shoppingPeriodRevokeInstructorEditable = false;
	private boolean shoppingPeriodRevokeInstructorEditableOrig = false;
	private boolean shoppingPeriodRevokeInstructorPublicOpt = false;
	private boolean shoppingPeriodRevokeInstructorPublicOptOrig = false;
	private String[] subAdminSiteAccess = null;
	private boolean isActive = true;
	private boolean allowBecomeUser = false;
	private boolean allowBecomeUserOrig = false;
	//flag to keep track of nodes that were edited by the instructor
	private boolean instructorEdited = false;
	private boolean instructorEditedOrig = false;
	/**
	 * this function should be called after a save in order to reset the original values to their current value.
	 * By doing this, you allow the next save the check against the new values
	 */
	public void setOriginals(){
		directAccessOrig = directAccess;
		accessAdminOrig = accessAdmin;
		shoppingPeriodAdminOrig = shoppingPeriodAdmin;
		realmOrig = realm;
		roleOrig = role;
		restrictedAuthToolsOrig = copyListOptions(restrictedAuthTools);
		restrictedPublicToolsOrig = copyListOptions(restrictedPublicTools);
		shoppingPeriodStartDateOrig = shoppingPeriodStartDate;
		shoppingPeriodEndDateOrig = shoppingPeriodEndDate;
		shoppingPeriodRevokeInstructorEditableOrig = shoppingPeriodRevokeInstructorEditable;
		shoppingPeriodRevokeInstructorPublicOptOrig = shoppingPeriodRevokeInstructorPublicOpt;
		allowBecomeUserOrig = allowBecomeUser;
		instructorEditedOrig = instructorEdited;
	}
	
	public NodeModel(String nodeId, HierarchyNodeSerialized node,
			boolean directAccess, String realm, String role, NodeModel parentNode,
			List<ListOptionSerialized> restrictedAuthTools, List<ListOptionSerialized> restrictedPublicTools, Date shoppingPeriodStartDate,
			Date shoppingPeriodEndDate, boolean addedDirectChildrenFlag, boolean shoppingPeriodAdmin,
			String modifiedBy, Date modified,
			Date shoppingAdminModified, String shoppingAdminModifiedBy, boolean accessAdmin, boolean shoppingPeriodRevokeInstructorEditable,
			boolean shoppingPeriodRevokeInstructorPublicOpt, boolean allowBecomeUser, boolean instructorEdited){

		this.nodeId = nodeId;
		this.node = node;
		this.directAccessOrig = directAccess;
		this.directAccess = directAccess;
		this.realm = realm;
		this.role = role;
		this.realmOrig = realm;
		this.roleOrig = role;
		this.parentNode = parentNode;
		this.restrictedAuthTools = restrictedAuthTools;
		this.restrictedAuthToolsOrig = copyListOptions(restrictedAuthTools);
		this.restrictedPublicTools = restrictedPublicTools;
		this.restrictedPublicToolsOrig = copyListOptions(restrictedPublicTools);
		this.shoppingPeriodEndDate = shoppingPeriodEndDate;
		this.shoppingPeriodEndDateOrig = shoppingPeriodEndDate;
		this.shoppingPeriodStartDate = shoppingPeriodStartDate;
		this.shoppingPeriodStartDateOrig = shoppingPeriodStartDate;
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
		this.shoppingPeriodAdmin = shoppingPeriodAdmin;
		this.shoppingPeriodAdminOrig = shoppingPeriodAdmin;
		this.modifiedBy = modifiedBy;
		this.modified = modified;
		this.shoppingAdminModified = shoppingAdminModified;
		this.shoppingAdminModifiedBy = shoppingAdminModifiedBy;
		this.accessAdmin = accessAdmin;
		this.accessAdminOrig = accessAdmin;
		this.shoppingPeriodRevokeInstructorEditable = shoppingPeriodRevokeInstructorEditable;
		this.shoppingPeriodRevokeInstructorEditableOrig = shoppingPeriodRevokeInstructorEditable;
		this.shoppingPeriodRevokeInstructorPublicOpt = shoppingPeriodRevokeInstructorPublicOpt;
		this.shoppingPeriodRevokeInstructorPublicOptOrig = shoppingPeriodRevokeInstructorPublicOpt;
		this.allowBecomeUser = allowBecomeUser;
		this.allowBecomeUserOrig = allowBecomeUser;
		this.instructorEdited = instructorEdited;
	}

	private List<ListOptionSerialized> copyListOptions(List<ListOptionSerialized> tools){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : tools){
			returnList.add(new ListOptionSerialized(tool.getId(), tool.getName(), tool.isSelected()));
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
		//this is where the display of the node title is set
		return node.description;
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
		
		if(accessAdmin != accessAdminOrig){
			return true;
		}
		
		//only worry about modifications to a direct access node
		if(directAccess){
			return isModified(shoppingPeriodStartDate, shoppingPeriodStartDateOrig, shoppingPeriodEndDate, shoppingPeriodEndDateOrig,
					realm, realmOrig, role, roleOrig, convertListToArray(getSelectedRestrictedAuthTools()), convertListToArray(getSelectedRestrictedAuthToolsOrig()), 
					convertListToArray(getSelectedRestrictedPublicTools()), convertListToArray(getSelectedRestrictedPublicToolsOrig()), shoppingPeriodRevokeInstructorEditable, shoppingPeriodRevokeInstructorEditableOrig,
					shoppingPeriodRevokeInstructorPublicOpt, shoppingPeriodRevokeInstructorPublicOptOrig, allowBecomeUser, allowBecomeUserOrig, instructorEdited, instructorEditedOrig);
		}

		return false;
	}

	public boolean isModified(Date shoppingStartDateOld, Date shoppingStartDateNew,
			Date shoppingEndDateOld, Date shoppingEndDateNew, String realmOld, String realmNew, String roleOld, String roleNew,
			String[] authToolsOld, String[] authToolsNew, String[] publicToolsOld, String[] publicToolsNew, boolean shoppingPeriodRevokeInstructorEditable, boolean shoppingPeriodRevokeInstructorEditableOrig,
			boolean shoppingPeriodRevokeInstructorPublicOpt, boolean shoppingPeriodRevokeInstructorPublicOptOrig, boolean allowBeomeUser, boolean allowBecomeUserOrig,
			boolean instructorEdited, boolean instructorEditedOrig){
		if(realmOld != null && realmNew != null){
			if(!realmOld.equals(realmNew))
				return true;
		}else if((realmOld == null || realmNew == null) && !(realmOld == null && realmNew == null)){
			return true;
		}
		if(shoppingStartDateOld != null && shoppingStartDateNew != null){
			if(!shoppingStartDateOld.equals(shoppingStartDateNew))
				return true;
		}else if((shoppingStartDateOld == null || shoppingStartDateNew == null) && !(shoppingStartDateOld == null && shoppingStartDateNew == null)){
			return true;
		}
		if(shoppingEndDateOld != null && shoppingEndDateNew != null){
			if(!shoppingEndDateOld.equals(shoppingEndDateNew))
				return true;
		}else if((shoppingEndDateOld == null || shoppingEndDateNew == null) && !(shoppingEndDateOld == null && shoppingEndDateNew == null)){
			return true;
		}


		if(roleOld != null && roleNew != null){
			if(!roleOld.equals(roleNew))
				return true;
		}else if((roleOld == null || roleNew == null) && !(roleOld == null && roleNew == null)){
			return true;
		}

		if(authToolsOld != null && authToolsNew != null){
			if(authToolsOld.length != authToolsNew.length){
				return true;
			}else{
				for(int i = 0; i < authToolsOld.length; i++){
					boolean found = false;
					for(int j = 0; j < authToolsNew.length; j++){
						if(authToolsOld[i].equals(authToolsNew[j])){
							found = true;
							break;
						}
					}
					if(!found){
						return true;
					}
				}
			}
		}else if((authToolsOld == null || authToolsNew == null) && !(authToolsOld == null && authToolsNew == null)){
			return true;
		}
		
		if(publicToolsOld != null && publicToolsNew != null){
			if(publicToolsOld.length != publicToolsNew.length){
				return true;
			}else{
				for(int i = 0; i < publicToolsOld.length; i++){
					boolean found = false;
					for(int j = 0; j < publicToolsNew.length; j++){
						if(publicToolsOld[i].equals(publicToolsNew[j])){
							found = true;
							break;
						}
					}
					if(!found){
						return true;
					}
				}
			}
		}else if((publicToolsOld == null || publicToolsNew == null) && !(publicToolsOld == null && publicToolsNew == null)){
			return true;
		}
		
		if(shoppingPeriodRevokeInstructorEditable != shoppingPeriodRevokeInstructorEditableOrig ||
				shoppingPeriodRevokeInstructorPublicOpt != shoppingPeriodRevokeInstructorPublicOptOrig){
			return true;
		}
		
		if(allowBeomeUser != allowBecomeUserOrig){
			return true;
		}
		
		if(instructorEdited != instructorEditedOrig){
			return true;
		}
		
		return false;
	}

	private boolean isRestrictedAuthToolsModified(){
		for(ListOptionSerialized origTool : restrictedAuthToolsOrig){
			for(ListOptionSerialized tool : restrictedAuthTools){
				if(tool.getId().equals(origTool.getId())){
					if(tool.isSelected() != origTool.isSelected()){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isRestrictedPublicToolsModified(){
		for(ListOptionSerialized origTool : restrictedPublicToolsOrig){
			for(ListOptionSerialized tool : restrictedPublicTools){
				if(tool.getId().equals(origTool.getId())){
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
		if(!isDirectAccess()){
			myAccessRealmRole = getInheritedAccessRealmRole();
		}
		if(myAccessRealmRole == null || "".equals(myAccessRealmRole[0]) || "".equals(myAccessRealmRole[1])){
			return new String[]{"",""};
		}else{
			return myAccessRealmRole;
		}
	}

	public Date getNodeShoppingPeriodStartDate(){
		if(isDirectAccess()){
			return getShoppingPeriodStartDate();
		}else{
			return getInheritedShoppingPeriodStartDate();
		}
	}

	public Date getNodeShoppingPeriodEndDate(){
		if(isDirectAccess()){
			return getShoppingPeriodEndDate();
		}else{
			return getInheritedShoppingPeriodEndDate();
		}
	}
	
	public boolean getNodeAccess(){
		if(isDirectAccess()){
			return true;
		}else{
			return getInheritedNodeAccess();
		}
	}

	public boolean getInheritedNodeAccess(){
		return getInheritedNodeAccessHelper(parentNode);
	}
	
	public boolean getInheritedNodeAccessHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isDirectAccess()) {
			return true;
		}else{
			return getInheritedNodeAccessHelper(parent.getParentNode());
		}
	}
	
	public String[] getInheritedAccessRealmRole(){
		return getInheritedAccessRealmRoleHelper(parentNode);
	}

	private String[] getInheritedAccessRealmRoleHelper(NodeModel parent){
		if(parent == null){
			return new String[]{"",""};
		} else if (parent.isDirectAccess()) {
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

	public NodeModel getParentNode() {
		return parentNode;
	}

	public void setParentNode(NodeModel parentNode) {
		this.parentNode = parentNode;
	}

	public List<ListOptionSerialized> getRestrictedAuthTools() {
		return restrictedAuthTools;
	}

	public void setRestrictedAuthTools(List<ListOptionSerialized> restrictedAuthTools) {
		this.restrictedAuthTools = restrictedAuthTools;
	}

	public String[] getNodeRestrictedAuthTools(){
		List<ListOptionSerialized> myRestrictedTools = getSelectedRestrictedAuthTools();
		if(!isDirectAccess()){
			myRestrictedTools = getInheritedRestrictedAuthTools();
		}

		if(myRestrictedTools == null || myRestrictedTools.size() == 0){
			return new String[0];
		}else{
			return convertListToArray(myRestrictedTools);
		}
	}
	
	public String[] convertListToArray(List<ListOptionSerialized> list){
		String[] restrictedToolsArray = new String[list.size()];
		int i = 0;
		for(ListOptionSerialized tool : list){
			restrictedToolsArray[i] = tool.getId();
			i++;
		}
		return restrictedToolsArray;
	}

	public List<ListOptionSerialized> getInheritedRestrictedAuthTools(){
		return getInheritedRestrictedAuthToolsHelper(parentNode);
	}

	private List<ListOptionSerialized> getInheritedRestrictedAuthToolsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess()){
			return parent.getSelectedRestrictedAuthTools();
		}else{
			return getInheritedRestrictedAuthToolsHelper(parent.getParentNode());
		}
	}

	public List<ListOptionSerialized> getSelectedRestrictedAuthTools(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedAuthTools){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}
	
	public List<ListOptionSerialized> getSelectedRestrictedAuthToolsOrig(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedAuthToolsOrig){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}

	public boolean hasAnyRestrictedAuthToolsSelected(){
		for(ListOptionSerialized tool : restrictedAuthTools){
			if(tool.isSelected())
				return true;
		}
		return false;
	}

	public void setAuthToolRestricted(String toolId, boolean restricted){
		for(ListOptionSerialized tool : restrictedAuthTools){
			if(tool.getId().equals(toolId)){
				tool.setSelected(restricted);
				break;
			}
		}
	}
	
	public boolean isAuthToolRestricted(String toolId){
		for(ListOptionSerialized tool : restrictedAuthTools){
			if(tool.getId().equals(toolId)){
				return tool.isSelected();
			}
		}
		return false;
	}
	
	//public tools:
	public List<ListOptionSerialized> getRestrictedPublicTools() {
		return restrictedPublicTools;
	}

	public void setRestrictedPublicTools(List<ListOptionSerialized> restrictedPublicTools) {
		this.restrictedPublicTools = restrictedPublicTools;
	}

	public String[] getNodeRestrictedPublicTools(){
		List<ListOptionSerialized> myRestrictedTools = getSelectedRestrictedPublicTools();
		if(!isDirectAccess()){
			myRestrictedTools = getInheritedRestrictedPublicTools();
		}

		if(myRestrictedTools == null || myRestrictedTools.size() == 0){
			return new String[0];
		}else{
			return convertListToArray(myRestrictedTools);
		}
	}

	public List<ListOptionSerialized> getInheritedRestrictedPublicTools(){
		return getInheritedRestrictedPublicToolsHelper(parentNode);
	}

	private List<ListOptionSerialized> getInheritedRestrictedPublicToolsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess()){
			return parent.getSelectedRestrictedPublicTools();
		}else{
			return getInheritedRestrictedPublicToolsHelper(parent.getParentNode());
		}
	}

	public List<ListOptionSerialized> getSelectedRestrictedPublicTools(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedPublicTools){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}
	
	public List<ListOptionSerialized> getSelectedRestrictedPublicToolsOrig(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedPublicToolsOrig){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}

	public boolean hasAnyRestrictedPublicToolsSelected(){
		for(ListOptionSerialized tool : restrictedPublicTools){
			if(tool.isSelected())
				return true;
		}
		return false;
	}

	public void setPublicToolRestricted(String toolId, boolean restricted){
		for(ListOptionSerialized tool : restrictedPublicTools){
			if(tool.getId().equals(toolId)){
				tool.setSelected(restricted);
				break;
			}
		}
	}

	public boolean isPublicToolRestricted(String toolId){
		for(ListOptionSerialized tool : restrictedPublicTools){
			if(tool.getId().equals(toolId)){
				return tool.isSelected();
			}
		}
		return false;
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

	public boolean isAddedDirectChildrenFlag() {
		return addedDirectChildrenFlag;
	}

	public void setAddedDirectChildrenFlag(boolean addedDirectChildrenFlag) {
		this.addedDirectChildrenFlag = addedDirectChildrenFlag;
	}

	public boolean isShoppingPeriodAdmin() {
		return shoppingPeriodAdmin;
	}
	
	public boolean isShoppingPeriodAdminOrig(){
		return shoppingPeriodAdminOrig;
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

	public String getSiteInstructors() {
		return siteInstructors;
	}

	public void setSiteInstructors(String siteInstructors) {
		this.siteInstructors = siteInstructors;
	}
	
	public SelectOption getRoleOption() {
		return roleOption;
	}

	public void setRoleOption(SelectOption roleOption) {
		this.roleOption = roleOption;
	}

	public Date getShoppingAdminModified() {
		return shoppingAdminModified;
	}

	public void setShoppingAdminModified(Date shoppingAdminModified) {
		this.shoppingAdminModified = shoppingAdminModified;
	}

	public String getShoppingAdminModifiedBy() {
		return shoppingAdminModifiedBy;
	}

	public void setShoppingAdminModifiedBy(String shoppingAdminModifiedBy) {
		this.shoppingAdminModifiedBy = shoppingAdminModifiedBy;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isAccessAdmin() {
		return accessAdmin;
	}

	public void setAccessAdmin(boolean accessAdmin) {
		this.accessAdmin = accessAdmin;
	}

	public boolean isAccessAdminOrig() {
		return accessAdminOrig;
	}

	public void setAccessAdminOrig(boolean accessAdminOrig) {
		this.accessAdminOrig = accessAdminOrig;
	}
	
	public boolean getNodeAccessAdmin(){
		if(isAccessAdmin()){
			return true;
		}else{
			return getInheritedAccessAdmin();
		}
	}

	public boolean getInheritedAccessAdmin(){
		return getInheritedAccessAdminHelper(parentNode);
	}
	
	public boolean getInheritedAccessAdminHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isAccessAdmin()) {
			return true;
		}else{
			return getInheritedAccessAdminHelper(parent.getParentNode());
		}
	}
	
	public boolean isEditable(){
		return editable;
	}
	
	public void setEditable(boolean editable){
		this.editable = editable;
	}
	
	public boolean isNodeEditable(){
		if(isEditable()){
			return true;
		}else{
			return getInheritedEditable();
		}
	}
	
	private boolean getInheritedEditable(){
		return getInheritedEditableHelper(parentNode);
	}
	
	private boolean getInheritedEditableHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isEditable()) {
			return true;
		}else{
			return getInheritedEditableHelper(parent.getParentNode());
		}
	}

	public boolean isShoppingPeriodRevokeInstructorEditable() {
		return shoppingPeriodRevokeInstructorEditable;
	}

	public void setShoppingPeriodRevokeInstructorEditable(
			boolean shoppingPeriodRevokeInstructorEditable) {
		this.shoppingPeriodRevokeInstructorEditable = shoppingPeriodRevokeInstructorEditable;
	}

	public boolean isShoppingPeriodRevokeInstructorEditableOrig() {
		return shoppingPeriodRevokeInstructorEditableOrig;
	}

	public void setShoppingPeriodRevokeInstructorEditableOrig(
			boolean shoppingPeriodRevokeInstructorEditableOrig) {
		this.shoppingPeriodRevokeInstructorEditableOrig = shoppingPeriodRevokeInstructorEditableOrig;
	}
	
	public boolean getNodeShoppingPeriodRevokeInstructorEditable(){
		if(isDirectAccess()){
			return isShoppingPeriodRevokeInstructorEditable();
		}else{
			return getInheritedShoppingPeriodRevokeInstructorEditable();
		}
	}

	public boolean getInheritedShoppingPeriodRevokeInstructorEditable(){
		return getInheritedShoppingPeriodRevokeInstructorEditableHelper(parentNode);
	}
	
	public boolean getInheritedShoppingPeriodRevokeInstructorEditableHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isDirectAccess()) {
			return parent.isShoppingPeriodRevokeInstructorEditable();
		}else{
			return getInheritedShoppingPeriodRevokeInstructorEditableHelper(parent.getParentNode());
		}
	}

	public boolean isShoppingPeriodRevokeInstructorPublicOpt() {
		return shoppingPeriodRevokeInstructorPublicOpt;
	}

	public void setShoppingPeriodRevokeInstructorPublicOpt(
			boolean shoppingPeriodRevokeInstructorPublicOpt) {
		this.shoppingPeriodRevokeInstructorPublicOpt = shoppingPeriodRevokeInstructorPublicOpt;
	}

	public boolean isShoppingPeriodRevokeInstructorPublicOptOrig() {
		return shoppingPeriodRevokeInstructorPublicOptOrig;
	}

	public void setShoppingPeriodRevokeInstructorPublicOptOrig(
			boolean shoppingPeriodRevokeInstructorPublicOptOrig) {
		this.shoppingPeriodRevokeInstructorPublicOptOrig = shoppingPeriodRevokeInstructorPublicOptOrig;
	}
	
	public boolean getNodeShoppingPeriodRevokeInstructorPublicOpt(){
		if(isDirectAccess()){
			return isShoppingPeriodRevokeInstructorPublicOpt();
		}else{
			return getInheritedShoppingPeriodRevokeInstructorPublicOpt();
		}
	}

	public boolean getInheritedShoppingPeriodRevokeInstructorPublicOpt(){
		return getInheritedShoppingPeriodRevokeInstructorPublicOptHelper(parentNode);
	}
	
	public boolean getInheritedShoppingPeriodRevokeInstructorPublicOptHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isDirectAccess()) {
			return parent.isShoppingPeriodRevokeInstructorPublicOpt();
		}else{
			return getInheritedShoppingPeriodRevokeInstructorPublicOptHelper(parent.getParentNode());
		}
	}

	public String[] getSubAdminSiteAccess() {
		return subAdminSiteAccess;
	}

	public void setSubAdminSiteAccess(String[] subAdminSiteAccess) {
		this.subAdminSiteAccess = subAdminSiteAccess;
	}

	public String[] getInheritedSubAdminSiteAccess(){
		return getInheritedSubAdminSiteAccessHelper(parentNode);
	}
	
	public String[] getInheritedSubAdminSiteAccessHelper(NodeModel parent){
		if(parent == null){
			return null;
		} else if (parent.getSubAdminSiteAccess() != null) {
			return parent.subAdminSiteAccess;
		}else{
			return getInheritedSubAdminSiteAccessHelper(parent.getParentNode());
		}
	}
	
	public String[] getNodeSubAdminSiteAccess(){
		if(getSubAdminSiteAccess() != null){
			return getSubAdminSiteAccess();
		}else{
			return getInheritedSubAdminSiteAccess();
		}
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isActive() {
		return isActive;
	}
	
	public boolean isSiteNode(){
		return node != null && node.title != null && node.title.startsWith("/site/");
	}

	public boolean isAllowBecomeUser() {
		return allowBecomeUser;
	}

	public void setAllowBecomeUser(boolean allowBecomeUser) {
		this.allowBecomeUser = allowBecomeUser;
	}
	
	public boolean getNodeAllowBecomeUser(){
		if(isDirectAccess()){
			return isAllowBecomeUser();
		}else{
			return getInheritedAllowBecomeUser();
		}
	}

	public boolean getInheritedAllowBecomeUser(){
		return getInheritedAllowBecomeUserHelper(parentNode);
	}
	
	public boolean getInheritedAllowBecomeUserHelper(NodeModel parent){
		if(parent == null){
			return false;
		} else if (parent.isDirectAccess()) {
			return parent.isAllowBecomeUser();
		}else{
			return getInheritedAllowBecomeUserHelper(parent.getParentNode());
		}
	}

	public boolean isInstructorEdited() {
		return instructorEdited;
	}

	public void setInstructorEdited(boolean instructorEdited) {
		this.instructorEdited = instructorEdited;
	}
}
