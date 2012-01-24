package org.sakaiproject.delegatedaccess.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
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
		//String role = (String) params.get("shoppingRole");
		//String realm = (String) params.get("shoppingRealm");
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
		node.setShoppingPeriodAuth(shoppingAuth);
		node.setShoppingPeriodStartDate(shoppingStartDate);
		node.setShoppingPeriodEndDate(shoppingEndDate);

		//to enable these settings, you must set the direct access to true, disabled = false
		if((shoppingAuth == null || "".equals(shoppingAuth)) && shoppingStartDate == null && shoppingEndDate == null){
			//user wants to remove information, so make the direct access == false:
			node.setDirectAccess(false);
		}else{
			//user is adding/updating information, so make sure direct access is true
			node.setDirectAccess(true);
		}

		projectLogic.updateNodePermissionsForUser(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
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

		return valuesMap;
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
}
