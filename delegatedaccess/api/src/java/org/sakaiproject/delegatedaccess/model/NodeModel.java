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
	private String realm = "";
	private String role = "";
	private String realmOrig = "";
	private String roleOrig = "";
	private NodeModel parentNode;
	private List<ListOptionSerialized> restrictedTools;
	private List<ListOptionSerialized> restrictedToolsOrig;
	private List<ListOptionSerialized> terms;
	private List<ListOptionSerialized> termsOrig;
	private Date shoppingPeriodStartDate = new Date();
	private Date shoppingPeriodStartDateOrig = new Date();
	private Date shoppingPeriodEndDate = new Date();
	private Date shoppingPeriodEndDateOrig = new Date();
//	private String shoppingPeriodAuth;
	private String shoppingPeriodAuthOrig;
	private boolean addedDirectChildrenFlag = false;	
	private boolean shoppingPeriodAdmin = false;
	private boolean shoppingPeriodAdminOrig = false;
	private Date updatedDate = new Date();
	private Date processedDate = new Date();
	private String siteTerm;
	private String siteInstructors;
	private SelectOption shoppingPeriodAuthOption;
	private SelectOption roleOption;

	public NodeModel(String nodeId, HierarchyNodeSerialized node,
			boolean directAccess, String realm, String role, NodeModel parentNode,
			List<ListOptionSerialized> restrictedTools, Date shoppingPeriodStartDate,
			Date shoppingPeriodEndDate,
			String shoppingPeriodAuth, boolean addedDirectChildrenFlag, boolean shoppingPeriodAdmin,
			Date updatedDate, Date processedDate, List<ListOptionSerialized> terms){

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
		this.restrictedToolsOrig = copyListOptions(restrictedTools);
		setShoppingPeriodAuth(shoppingPeriodAuth);
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
		this.terms = terms;
		this.termsOrig = copyListOptions(terms);
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
		//only worry about modifications to a direct access node
		if(directAccess){
			return isModified(getShoppingPeriodAuth(), shoppingPeriodAuthOrig, shoppingPeriodStartDate, shoppingPeriodStartDateOrig, shoppingPeriodEndDate, shoppingPeriodEndDateOrig,
					realm, realmOrig, role, roleOrig, convertListToArray(getSelectedRestrictedTools()), convertListToArray(getSelectedRestrictedToolsOrig()), 
					convertListToArray(getSelectedTerms()), convertListToArray(getSelectedTermsOrig()));
		}

		return false;
	}

	public boolean isModified(String shoppingAuthOld, String shoppingAuthNew, Date shoppingStartDateOld, Date shoppingStartDateNew,
			Date shoppingEndDateOld, Date shoppingEndDateNew, String realmOld, String realmNew, String roleOld, String roleNew,
			String[] toolsOld, String[] toolsNew, String[] termsOld, String[] termsNew){
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

		if(shoppingAuthOld != null && shoppingAuthNew != null){
			if(!shoppingAuthOld.equals(shoppingAuthNew))
				return true;
		}else if((shoppingAuthOld == null || shoppingAuthNew == null) && !(shoppingAuthOld == null && shoppingAuthNew == null)){
			return true;
		}
		if(toolsOld != null && toolsNew != null){
			if(toolsOld.length != toolsNew.length){
				return true;
			}else{
				for(int i = 0; i < toolsOld.length; i++){
					boolean found = false;
					for(int j = 0; j < toolsNew.length; j++){
						if(toolsOld[i].equals(toolsNew[j])){
							found = true;
							break;
						}
					}
					if(!found){
						return true;
					}
				}
			}
		}else if((toolsOld == null || toolsNew == null) && !(toolsOld == null && toolsNew == null)){
			return true;
		}
		
		if(termsOld != null && termsNew != null){
			if(termsOld.length != termsNew.length){
				return true;
			}else{
				for(int i = 0; i < termsOld.length; i++){
					boolean found = false;
					for(int j = 0; j < termsNew.length; j++){
						if(termsOld[i].equals(termsNew[j])){
							found = true;
							break;
						}
					}
					if(!found){
						return true;
					}
				}
			}
		}else if((termsOld == null || termsNew == null) && !(termsOld == null && termsNew == null)){
			return true;
		}
		
		return false;
	}

	private boolean isRestrictedToolsModified(){
		for(ListOptionSerialized origTool : restrictedToolsOrig){
			for(ListOptionSerialized tool : restrictedTools){
				if(tool.getId().equals(origTool.getId())){
					if(tool.isSelected() != origTool.isSelected()){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isTermsModified(){
		for(ListOptionSerialized origTerm : termsOrig){
			for(ListOptionSerialized term : terms){
				if(term.getId().equals(origTerm.getId())){
					if(term.isSelected() != origTerm.isSelected()){
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

	public String getNodeShoppingPeriodAuth(){
		if(isDirectAccess()){
			return getShoppingPeriodAuth();
		}else{
			return getInheritedShoppingPeriodAuth();
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

	public List<ListOptionSerialized> getRestrictedTools() {
		return restrictedTools;
	}

	public void setRestrictedTools(List<ListOptionSerialized> restrictedTools) {
		this.restrictedTools = restrictedTools;
	}

	public String[] getNodeRestrictedTools(){
		List<ListOptionSerialized> myRestrictedTools = getSelectedRestrictedTools();
		if(!isDirectAccess()){
			myRestrictedTools = getInheritedRestrictedTools();
		}

		if(myRestrictedTools == null || myRestrictedTools.size() == 0){
			return new String[0];
		}else{
			return convertListToArray(myRestrictedTools);
		}
	}
	
	private String[] convertListToArray(List<ListOptionSerialized> list){
		String[] restrictedToolsArray = new String[list.size()];
		int i = 0;
		for(ListOptionSerialized tool : list){
			restrictedToolsArray[i] = tool.getId();
			i++;
		}
		return restrictedToolsArray;
	}

	public List<ListOptionSerialized> getInheritedRestrictedTools(){
		return getInheritedRestrictedToolsHelper(parentNode);
	}

	private List<ListOptionSerialized> getInheritedRestrictedToolsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess()){
			return parent.getSelectedRestrictedTools();
		}else{
			return getInheritedRestrictedToolsHelper(parent.getParentNode());
		}
	}

	public List<ListOptionSerialized> getSelectedRestrictedTools(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedTools){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}
	
	public List<ListOptionSerialized> getSelectedRestrictedToolsOrig(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : restrictedToolsOrig){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}

	public boolean hasAnyRestrictedToolsSelected(){
		for(ListOptionSerialized tool : restrictedTools){
			if(tool.isSelected())
				return true;
		}
		return false;
	}

	public void setToolRestricted(String toolId, boolean restricted){
		for(ListOptionSerialized tool : restrictedTools){
			if(tool.getId().equals(toolId)){
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
		String shoppingPeriodAuth = null;
		if(shoppingPeriodAuthOption != null){
			shoppingPeriodAuth = shoppingPeriodAuthOption.getValue();
		}
		return shoppingPeriodAuth;
	}

	public void setShoppingPeriodAuth(String shoppingPeriodAuth){
		if(shoppingPeriodAuthOption == null){
			shoppingPeriodAuthOption = new SelectOption("", shoppingPeriodAuth);
		}else{
			shoppingPeriodAuthOption.setValue(shoppingPeriodAuth);
			shoppingPeriodAuthOption.setLabel("");
		}
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}

	public Date getNodeUpdatedDate(){
		if(isDirectAccess()){
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

	public String getSiteTerm() {
		return siteTerm;
	}

	public void setSiteTerm(String siteTerm) {
		this.siteTerm = siteTerm;
	}

	public String getSiteInstructors() {
		return siteInstructors;
	}

	public void setSiteInstructors(String siteInstructors) {
		this.siteInstructors = siteInstructors;
	}

	public void setShoppingPeriodAuthOption(SelectOption shoppingPeriodAuthOption) {
		this.shoppingPeriodAuthOption = shoppingPeriodAuthOption;
	}

	public SelectOption getShoppingPeriodAuthOption() {
		return shoppingPeriodAuthOption;
	}

	public List<ListOptionSerialized> getTerms() {
		return terms;
	}

	public void setTerms(List<ListOptionSerialized> terms) {
		this.terms = terms;
	}

	public List<ListOptionSerialized> getTermsOrig() {
		return termsOrig;
	}

	public void setTermsOrig(List<ListOptionSerialized> termsOrig) {
		this.termsOrig = termsOrig;
	}
	
	public String[] getNodeTerms(){
		List<ListOptionSerialized> myTerms = getSelectedTerms();
		if(!isDirectAccess()){
			myTerms = getInheritedTerms();
		}

		if(myTerms == null || myTerms.size() == 0){
			return new String[0];
		}else{
			String[] termsArray = new String[myTerms.size()];
			int i = 0;
			for(ListOptionSerialized tool : myTerms){
				termsArray[i] = tool.getId();
				i++;
			}
			return termsArray;
		}
	}

	public List<ListOptionSerialized> getInheritedTerms(){
		return getInheritedTermsHelper(parentNode);
	}

	private List<ListOptionSerialized> getInheritedTermsHelper(NodeModel parent){
		if(parent == null){
			return Collections.emptyList();
		}else if(parent.isDirectAccess()){
			return parent.getSelectedTerms();
		}else{
			return getInheritedTermsHelper(parent.getParentNode());
		}
	}

	public List<ListOptionSerialized> getSelectedTerms(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : terms){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}
	
	public List<ListOptionSerialized> getSelectedTermsOrig(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized tool : termsOrig){
			if(tool.isSelected())
				returnList.add(tool);
		}
		return returnList;
	}

	public boolean hasAnyTermsSelected(){
		for(ListOptionSerialized tool : terms){
			if(tool.isSelected())
				return true;
		}
		return false;
	}

	public void setTerm(String id, boolean restricted){
		for(ListOptionSerialized tool : terms){
			if(tool.getId().equals(id)){
				tool.setSelected(restricted);
				break;
			}
		}
	}

	public SelectOption getRoleOption() {
		return roleOption;
	}

	public void setRoleOption(SelectOption roleOption) {
		this.roleOption = roleOption;
	}
}
