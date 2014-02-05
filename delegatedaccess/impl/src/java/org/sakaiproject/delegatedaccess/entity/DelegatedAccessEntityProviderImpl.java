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

package org.sakaiproject.delegatedaccess.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.model.AccessNode;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessMutableTreeNode;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

/**
 * This is the RESTful service for the Shopping Period Admin.  This allows an instructor to
 * update their own shopping period information through site-manage
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class DelegatedAccessEntityProviderImpl implements DelegatedAccessEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, PropertyProvideable, RequestStorable, RESTful, RequestAware {

	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter
	private SakaiProxy sakaiProxy;

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	private String getFirstNodeId(String siteRef, String hierarchyId){
		Map<String, List<String>> nodeIds = projectLogic.getNodesBySiteRef(new String[]{siteRef}, hierarchyId);
		if(nodeIds != null && nodeIds.containsKey(siteRef) && nodeIds.get(siteRef) != null && nodeIds.get(siteRef).size() == 1){
			return nodeIds.get(siteRef).get(0);
		}else{
			return null;
		}
	}

	public boolean entityExists(String id) {
		String firstNodeId = getFirstNodeId("/site/" + id, DelegatedAccessConstants.HIERARCHY_ID);
		return firstNodeId != null;
	}


	public String getPropertyValue(String reference, String name) {
		return getProperties(reference).get(name);
	}

	public Map<String, String> getProperties(String reference) {
		String siteRef = "/site/" + reference.substring(reference.lastIndexOf("/") + 1);
		String nodeId = getFirstNodeId(siteRef, DelegatedAccessConstants.HIERARCHY_ID);
		if(nodeId == null){
			throw new IllegalArgumentException("NodeId: " + reference + " doesn't exist");
		}
		NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		if(node == null){
			throw new IllegalArgumentException("NodeId: " + nodeId + " doesn't exist");
		}
		Map<String,String> valuesMap = new HashMap<String, String>();
		valuesMap.put("shoppingStartDate", Long.toString(node.getNodeShoppingPeriodStartDate().getTime()));
		valuesMap.put("shoppingEndDate", Long.toString(node.getNodeShoppingPeriodEndDate().getTime()));
		valuesMap.put("shoppingRealm", node.getNodeAccessRealmRole()[0]);
		valuesMap.put("shoppingRole", node.getNodeAccessRealmRole()[1]);
		valuesMap.put("directAccess", "" + node.isDirectAccess());

		return valuesMap;
	}

	public void setPropertyValue(String reference, String name, String value) {
		// TODO Auto-generated method stub

	}

	public List<String> findEntityRefs(String[] prefixes, String[] name,
			String[] searchValue, boolean exactMatch) {
		// TODO Auto-generated method stub
		return null;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSampleEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		boolean isSuperUser = sakaiProxy.isSuperUser();
		boolean isInstructor = sakaiProxy.isUserInstructor(sakaiProxy.getCurrentUserId(), ref.getId());
		if(!(isSuperUser || (isInstructor && sakaiProxy.isShoppingPeriodInstructorEditable()))){
			//we only want to allow user's who are either an admin or is an actual member of the site and has "instructor" permsission (site.upd)
			//otherwise, they can just use the Delegated Access interface to make modifications
			throw new IllegalArgumentException("User: " + sakaiProxy.getCurrentUserId() + " is not a member of the site with site.upd permission or an admin");
		}
		String siteRef = "/site/" + ref.getId();
		String nodeId = getFirstNodeId(siteRef, DelegatedAccessConstants.HIERARCHY_ID);
		if(nodeId == null){
			throw new IllegalArgumentException("Node doesn't exist or has multiple instances: " + ref.getId());
		}
		String shoppingStartDateStr = (String) params.get("shoppingStartDate");
		String shoppingEndDateStr = (String) params.get("shoppingEndDate");
		String role = (String) params.get("shoppingRole");
		String realm = (String) params.get("shoppingRealm");
		Object authToolsList = params.get("shoppingShowAuthTools");
		Object publicToolsList = params.get("shoppingShowPublicTools");
		boolean directAccess = true;
		if(params.get("directAccess") != null){
			directAccess = Boolean.valueOf("" + params.get("directAccess"));
		}
		String[] authTools = null;
		if(authToolsList != null){
			if(authToolsList instanceof String[]){
				authTools = (String[]) params.get("shoppingShowAuthTools");
			}else if(authToolsList instanceof String && !"".equals(authToolsList)){
				authTools = new String[]{(String) authToolsList};
			}
		};
		
		String[] publicTools = null;
		if(publicToolsList != null){
			if(publicToolsList instanceof String[]){
				publicTools = (String[]) params.get("shoppingShowPublicTools");
			}else if(publicToolsList instanceof String && !"".equals(publicToolsList)){
				publicTools = new String[]{(String) publicToolsList};
			}
		};
		
		Date shoppingStartDate = null;
		Date shoppingEndDate = null;
		if(shoppingStartDateStr != null && !"".equals(shoppingStartDateStr)){
			try{
				shoppingStartDate = new Date(Long.parseLong(shoppingStartDateStr));
			}catch (Exception e) {
				throw new IllegalArgumentException("shoppingStartDate: " + shoppingStartDateStr + " is not a valid date time.");
			}
		}
		if(shoppingEndDateStr != null && !"".equals(shoppingEndDateStr)){
			try{
				shoppingEndDate = new Date(Long.parseLong(shoppingEndDateStr));
			}catch (Exception e) {
				throw new IllegalArgumentException("shoppingEndDate: " + shoppingEndDateStr + " is not a valid date time.");
			}
		}

		//get the node to store the information:
		NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		//lets verify the instructor is able to make these modifications:
		//is this disabled:
		if(node.getNodeShoppingPeriodRevokeInstructorEditable()){
			throw new IllegalArgumentException("This node has the ability for an instructor to make edits to the shopping period settings revoked.  Node: " + node.getNodeId() + ", ref: " + ref.getId());
		}
		
		if(!directAccess){
			//no need to continue, just set direct access to false and save (will clear out the rest and cause
			//the node to inherrit it's settings
			node.setDirectAccess(false);
			DefaultMutableTreeNode treeNode = new DelegatedAccessMutableTreeNode();
			treeNode.setUserObject(node);
			projectLogic.updateNodePermissionsForUser(treeNode, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
			return;
		}
		
		//Get Original Settings before we modify it
		boolean directAccessOrig = node.isDirectAccess();
		Date startDateOrig = node.getShoppingPeriodStartDate();
		Date endDateOrig = node.getShoppingPeriodEndDate();
		String realmOrig = node.getRealm();
		String roleOrig = node.getRole();
		String[] authToolsOrig = node.convertListToArray(node.getSelectedRestrictedAuthTools());
		String[] publicToolsOrig = node.convertListToArray(node.getSelectedRestrictedPublicTools());
		boolean instructorEditedOrig = node.isInstructorEdited();
		//modify the setting to the new settings
		node.setShoppingPeriodStartDate(shoppingStartDate);
		node.setShoppingPeriodEndDate(shoppingEndDate);
		node.setRealm(realm);
		node.setRole(role);
		node.setInstructorEdited(isInstructor);
		node.setRestrictedAuthTools(projectLogic.getEntireToolsList());
		if(authTools != null){
			for(String toolId : authTools){
				node.setAuthToolRestricted(toolId, true);
			}
		}
		node.setRestrictedPublicTools(projectLogic.getEntireToolsList());
		if(node.getNodeShoppingPeriodRevokeInstructorPublicOpt()){
			//since the instructor isn't allowed to edit public options, make sure that the inherritted
			//options are stored in this node since they could have chosen "override":
			if(directAccessOrig != directAccess){
				//if direct access is changing from not accessed to access, then just grab the inheritted tools, otherwise, keep what is already saved
				publicTools = node.convertListToArray(node.getInheritedRestrictedPublicTools());
			}else{
				//direct access isn't change, so just keep the existing restricted tools for this node
				publicTools =  publicToolsOrig;
			}
		}
		if(publicTools != null){
			for(String toolId : publicTools){
				node.setPublicToolRestricted(toolId, true);
			}
		}
		//user could have checked overrideDirectAccess and changed nothing else
		node.setDirectAccess(directAccess);
		//Get new modified settings
		Date startDateNew = node.getShoppingPeriodStartDate();
		Date endDateNew = node.getShoppingPeriodEndDate();
		String realmNew = node.getRealm();
		String roleNew = node.getRole();
		String[] authToolsNew = node.convertListToArray(node.getSelectedRestrictedAuthTools());
		String[] publicToolsNew = node.convertListToArray(node.getSelectedRestrictedPublicTools());
		
		//Set advanced options to what it inherits since instructors can't edit this if this is the first time this node is being selected,
		//otherwise, keep whatever was assigned to it
		if(directAccessOrig != directAccess){
			node.setShoppingPeriodRevokeInstructorEditable(node.getInheritedShoppingPeriodRevokeInstructorEditable());
			node.setShoppingPeriodRevokeInstructorPublicOpt(node.getInheritedShoppingPeriodRevokeInstructorPublicOpt());
		}else{
			node.setShoppingPeriodRevokeInstructorEditable(node.isShoppingPeriodRevokeInstructorEditable());
			node.setShoppingPeriodRevokeInstructorPublicOpt(node.isShoppingPeriodRevokeInstructorPublicOpt());
		}
		
		//only update if there were true modifications
		if(directAccessOrig != directAccess || node.isModified(startDateOrig, startDateNew, endDateOrig, endDateNew,
				realmOrig, realmNew, roleOrig, roleNew, authToolsOrig, authToolsNew, publicToolsOrig, publicToolsNew, false, false, false, false, node.isAllowBecomeUser(), node.isAllowBecomeUser(),
				node.isInstructorEdited(), instructorEditedOrig)){
			DefaultMutableTreeNode treeNode = new DelegatedAccessMutableTreeNode();
			treeNode.setUserObject(node);
			projectLogic.updateNodePermissionsForUser(treeNode, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		}
	}
	
	public Object getEntity(EntityReference ref) {
		String siteRef = "/site/" + ref.getId();
		String nodeId = getFirstNodeId(siteRef, DelegatedAccessConstants.HIERARCHY_ID);
		if(nodeId == null){
			throw new IllegalArgumentException("NodeId for Site: " + ref + " doesn't exist");
		}
		NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		if(node == null){
			throw new IllegalArgumentException("NodeId: " + nodeId + " doesn't exist");
		}

		Map valuesMap = new HashMap<String, String>();
		valuesMap.put("shoppingStartDate", node.getNodeShoppingPeriodStartDate());
		valuesMap.put("shoppingEndDate", node.getNodeShoppingPeriodEndDate());
		valuesMap.put("shoppingRealm", node.getNodeAccessRealmRole()[0]);
		valuesMap.put("shoppingRole", node.getNodeAccessRealmRole()[1]);
		valuesMap.put("directAccess", node.isDirectAccess());
		valuesMap.put("revokeInstructorEditable", node.getNodeShoppingPeriodRevokeInstructorEditable());
		valuesMap.put("revokeInstructorPublicOpt", node.getNodeShoppingPeriodRevokeInstructorPublicOpt());
		valuesMap.put("shoppingShowAuthTools", node.getNodeRestrictedAuthTools());
		valuesMap.put("shoppingShowPublicTools", node.getNodeRestrictedPublicTools());

		return valuesMap;
	}

	@EntityCustomAction(action="canEditShopping",viewKey=EntityView.VIEW_LIST)
    public List<?> canEditShopping(EntityView view, Map<String, Object> params) {
		Boolean canEdit = Boolean.FALSE;
		String option = view.getPathSegment(2);
        if(option == null || "".equals(option)){
        	throw new IllegalArgumentException("Expected url path is /canEditShopping/site/{id}");
        }
        if("site".equals(option)){
        	String siteId = view.getPathSegment(3);
        	if(siteId != null && !"".equals(siteId)){
        		if(sakaiProxy.isSuperUser() || (sakaiProxy.isUserInstructor(sakaiProxy.getCurrentUserId(), siteId)) && sakaiProxy.isShoppingPeriodInstructorEditable()){
        			String siteRef = "/site/" + siteId;
        			String nodeId = getFirstNodeId(siteRef, DelegatedAccessConstants.HIERARCHY_ID);
        			if(nodeId == null){
        				throw new IllegalArgumentException("Node doesn't exist or has multiple instances: " + siteRef);
        			}else{
        				//get the node to find the settings
        				NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
        				//lets verify the instructor is able to make these modifications:
        				//is this disabled:
        				if(!node.getNodeShoppingPeriodRevokeInstructorEditable()){
        					canEdit = Boolean.TRUE;
        				}
        			}
        		}
        	}
        }
        return Arrays.asList(canEdit);
	}
        
	/**
	 * shoppingOptions/roles
	 * shoppingOptions/tools
	 * 
	 * @param view
	 * @param params
	 * @return
	 */
	@EntityCustomAction(action="shoppingOptions",viewKey=EntityView.VIEW_LIST)
    public List<?> getShoppingOptions(EntityView view, Map<String, Object> params) {
        String option = view.getPathSegment(2);
        if(option == null || "".equals(option)){
        	throw new IllegalArgumentException("An option is required:  shoppingOptions/roles, shoppingOptions/tools, shoppingOptions/authorization");
        }
        if("roles".equals(option)){
        	return convertMapToSerializedList(projectLogic.getRealmRoleDisplay(true));
        }else if("tools".equals(option)){
        	return convertListToSerializedList(projectLogic.getEntireToolsList());
        }else{
        	throw new IllegalArgumentException("A valid option is required:  shoppingOptions/roles, shoppingOptions/tools, shoppingOptions/authorization");
        }
	}
	
	private List<GenericOutputSerialized> convertListToSerializedList(List<ListOptionSerialized> list){
		List<GenericOutputSerialized> returnList = new ArrayList<GenericOutputSerialized>();
		for(ListOptionSerialized l : list){
    		returnList.add(new GenericOutputSerialized(l.getId(), l.getName()));
    	}
		sortGenericOutputList(returnList);
    	return returnList;
	}
	
	private List<GenericOutputSerialized> convertMapToSerializedList(Map<String, String> map){
		List<GenericOutputSerialized> returnList = new ArrayList<GenericOutputSerialized>();
		for(Entry<String, String> entry : map.entrySet()){
    		returnList.add(new GenericOutputSerialized(entry.getKey(), entry.getValue()));
    	}
		sortGenericOutputList(returnList);
    	return returnList;
	}
	
	private void sortGenericOutputList(List<GenericOutputSerialized> l){
		Collections.sort(l, new Comparator<GenericOutputSerialized>() {
    		public int compare(GenericOutputSerialized arg0,
    				GenericOutputSerialized arg1) {
    			return arg0.getValue().compareToIgnoreCase(arg1.getValue());
    		}
		});
	}
	
	@EntityCustomAction(action="initialize",viewKey=EntityView.VIEW_LIST)
	public List initializeAccessForSite(EntityView view, Map<String, Object> params) {
		String option = view.getPathSegment(2);
        if(option == null || "".equals(option)){
        	throw new IllegalArgumentException("Expected url path is /initialize/site/{id}");
        }
        if("site".equals(option)){
        	String siteId = view.getPathSegment(3);
        	if(siteId != null && !"".equals(siteId)){
        		return Arrays.asList(projectLogic.getCurrentUsersAccessToSite("/site/" + siteId));
        	}else{
        		throw new IllegalArgumentException("Expected url path is /initialize/site/{id}");
        	}
        }else{
        	throw new IllegalArgumentException("Expected url path is /initialize/site/{id}");
        }
	}
	
	@EntityCustomAction(action="access",viewKey=EntityView.VIEW_LIST)
	public List getUsersWithAccessToSite(EntityView view, Map<String, Object> params) {
		String option = view.getPathSegment(2);
        if(option == null || "".equals(option)){
        	throw new IllegalArgumentException("Expected url path is /access/site/{id}");
        }
        if("site".equals(option)){
        	String siteId = view.getPathSegment(3);
        	if(siteId != null && !"".equals(siteId)){
        		//check the user is actually an instructor or super admin:
        		if(!sakaiProxy.isSuperUser() && !sakaiProxy.isUserInstructor(sakaiProxy.getCurrentUserId(), siteId)){
        			throw new IllegalArgumentException("You must be an instructor or admin user to view this information.");
        		}
        		
        		List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
        		for(AccessNode node : projectLogic.getUserAccessForSite("/site/" + siteId).values()){
        			Map<String,Object> accessMap = new HashMap<String, Object>();
        			accessMap.put("realm", "");
        			accessMap.put("role", "");
        			if(node.getAccess() != null && node.getAccess().length == 2){
        				accessMap.put("realm", node.getAccess()[0]);
        				accessMap.put("role", node.getAccess()[1]);
        			}
        			accessMap.put("deniedTools", node.getDeniedAuthTools());
        			accessMap.put("userId", node.getUserId());
        			User user = sakaiProxy.getUser(node.getUserId());
        			accessMap.put("userEid", "");
        			accessMap.put("userDisplayName", "");
        			if(user != null){
        				accessMap.put("userEid", user.getEid());
        				accessMap.put("userDisplayName", user.getDisplayName());	
        			}
        			String deniedToolsNames = "";
        			if(node.getDeniedAuthTools() != null){
        				for(String toolId : node.getDeniedAuthTools()){
        					Tool tool = sakaiProxy.getTool(toolId);
        					if(tool != null){
        						if(!deniedToolsNames.equals("")){
        							deniedToolsNames += ", ";
        						}
        						deniedToolsNames += tool.getTitle();
        					}
        				}
        			}
        			accessMap.put("deniedToolsNames", deniedToolsNames);
        			
        			returnList.add(accessMap);
        		}
        		//filter hidden roles:
        		String[] hiddenRoles = sakaiProxy.getHideRolesForInstructorViewAccess();
        		if(hiddenRoles != null){
        			for (Iterator iterator = returnList.iterator(); iterator.hasNext();) {
        				Map<String, Object> map = (Map<String, Object>) iterator.next();
        				for(String role : hiddenRoles){
        					if(map.containsKey("role") && role.equals(map.get("role"))){
        						iterator.remove();
        						break;
        					}
        				}
        			}
        		}
        		
        		return returnList;
        	}else{
        		throw new IllegalArgumentException("Expected url path is /access/site/{id}");
        	}
        }else{
        	throw new IllegalArgumentException("Expected url path is /access/site/{id}");
        }
	}
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRequestGetter(RequestGetter requestGetter) {
		// TODO Auto-generated method stub

	}

	public void setRequestStorage(RequestStorage requestStorage) {
		// TODO Auto-generated method stub

	}
	
	public class GenericOutputSerialized implements Serializable{
		public String key;
		public String value;
		public GenericOutputSerialized(String key, String value){
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
