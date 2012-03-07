package org.sakaiproject.delegatedaccess.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
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

	public boolean entityExists(String id) {
		List<String> nodeIds = projectLogic.getNodesBySiteRef("/site/" + id, DelegatedAccessConstants.HIERARCHY_ID); 
		return nodeIds != null && nodeIds.size() == 1;
	}


	public String getPropertyValue(String reference, String name) {
		return getProperties(reference).get(name);
	}

	public Map<String, String> getProperties(String reference) {
		List<String> nodeIds = projectLogic.getNodesBySiteRef("/site/" + reference.substring(reference.lastIndexOf("/") + 1), DelegatedAccessConstants.HIERARCHY_ID);		
		if(nodeIds == null || nodeIds.size() != 1){
			throw new IllegalArgumentException("NodeId: " + reference + " doesn't exist");
		}
		String nodeId = nodeIds.get(0);
		NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		if(node == null){
			throw new IllegalArgumentException("NodeId: " + nodeId + " doesn't exist");
		}
		Map<String,String> valuesMap = new HashMap<String, String>();
		valuesMap.put("shoppingAuth", node.getNodeShoppingPeriodAuth());
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
		//TODO: verify user's credentials:
		List<String> nodeIds = projectLogic.getNodesBySiteRef("/site/" + ref.getId(), DelegatedAccessConstants.HIERARCHY_ID);
		if(nodeIds == null || nodeIds.size() != 1){
			throw new IllegalArgumentException("Node doesn't exist or has multiple instances: " + ref.getId());
		}
		String nodeId = nodeIds.get(0);
		String shoppingAuth = (String) params.get("shoppingAuth");
		String shoppingStartDateStr = (String) params.get("shoppingStartDate");
		String shoppingEndDateStr = (String) params.get("shoppingEndDate");
		String role = (String) params.get("shoppingRole");
		String realm = (String) params.get("shoppingRealm");
		Object toolList = params.get("shoppingShowTools");
		boolean directAccess = true;
		if(params.get("directAccess") != null){
			directAccess = Boolean.valueOf("" + params.get("directAccess"));
		}
		String[] tools = null;
		if(toolList != null){
			if(toolList instanceof String[]){
				tools = (String[]) params.get("shoppingShowTools");
			}else if(toolList instanceof String && !"".equals(toolList)){
				tools = new String[]{(String) toolList};
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
		
		if(!directAccess){
			//no need to continue, just set direct access to false and save (will clear out the rest and cause
			//the node to inherrit it's settings
			node.setDirectAccess(false);
			projectLogic.updateNodePermissionsForUser(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
			return;
		}
		
		//Get Original Settings before we modify it
		String authOrig = node.getNodeShoppingPeriodAuth();
		Date startDateOrig = node.getNodeShoppingPeriodStartDate();
		Date endDateOrig = node.getNodeShoppingPeriodEndDate();
		String realmOrig = node.getNodeAccessRealmRole()[0];
		String roleOrig = node.getNodeAccessRealmRole()[1];
		String[] toolsOrig = node.getNodeRestrictedTools();
		String[] termsOrig = node.getNodeTerms();
		//modify the setting to the new settings
		node.setShoppingPeriodAuth(shoppingAuth);
		node.setShoppingPeriodStartDate(shoppingStartDate);
		node.setShoppingPeriodEndDate(shoppingEndDate);
		node.setRealm(realm);
		node.setRole(role);
		node.setRestrictedTools(projectLogic.getEntireToolsList());
		if(tools != null){
			for(String toolId : tools){
				node.setToolRestricted(toolId, true);
			}
		}
		//user could have checked overrideDirectAccess and changed nothing else
		node.setDirectAccess(directAccess);
		//Get new modified settings
		String authNew = node.getNodeShoppingPeriodAuth();
		Date startDateNew = node.getNodeShoppingPeriodStartDate();
		Date endDateNew = node.getNodeShoppingPeriodEndDate();
		String realmNew = node.getNodeAccessRealmRole()[0];
		String roleNew = node.getNodeAccessRealmRole()[1];
		String[] toolsNew = node.getNodeRestrictedTools();
		String[] termsNew = node.getNodeTerms();
		//only update if there were true modifications
		if(node.isModified(authOrig, authNew, startDateOrig, startDateNew, endDateOrig, endDateNew,
				realmOrig, realmNew, roleOrig, roleNew, toolsOrig, toolsNew, termsOrig, termsNew)){
			projectLogic.updateNodePermissionsForUser(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		}
	}
	
	public Object getEntity(EntityReference ref) {
		List<String> nodeIds = projectLogic.getNodesBySiteRef("/site/" + ref.getId(), DelegatedAccessConstants.HIERARCHY_ID);
		if(nodeIds == null || nodeIds.size() != 1){
			throw new IllegalArgumentException("NodeId for Site: " + ref + " doesn't exist");
		}
		String nodeId = nodeIds.get(0);
		NodeModel node = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		if(node == null){
			throw new IllegalArgumentException("NodeId: " + nodeId + " doesn't exist");
		}

		Map valuesMap = new HashMap<String, String>();
		valuesMap.put("shoppingAuth", node.getNodeShoppingPeriodAuth());
		valuesMap.put("shoppingStartDate", node.getNodeShoppingPeriodStartDate());
		valuesMap.put("shoppingEndDate", node.getNodeShoppingPeriodEndDate());
		valuesMap.put("shoppingRealm", node.getNodeAccessRealmRole()[0]);
		valuesMap.put("shoppingRole", node.getNodeAccessRealmRole()[1]);
		valuesMap.put("directAccess", node.isDirectAccess());
//		List<String> selectedRestrictedTools = new ArrayList<String>();
//		for(ListOptionSerialized tool : node.getNodeRestrictedTools()){
//			selectedRestrictedTools.add(tool.getId());
//		}
		valuesMap.put("shoppingShowTools", node.getNodeRestrictedTools());

		return valuesMap;
	}

	/**
	 * shoppingOptions/roles
	 * shoppingOptions/tools
	 * shoppingOptions/authorization
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
        }else if("authorization".equals(option)){
        	return convertListToSerializedList(projectLogic.getAuthorizationOptions());
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
        		return projectLogic.grantAccessToSite("/site/" + siteId);
        	}else{
        		throw new IllegalArgumentException("Expected url path is /initialize/site/{id}");
        	}
        }else{
        	throw new IllegalArgumentException("Expected url path is /initialize/site/{id}");
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
