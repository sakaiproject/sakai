/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/PermissionLevelManagerImpl.java $
 * $Id: PermissionLevelManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PermissionLevelManagerImpl extends HibernateDaoSupport implements PermissionLevelManager {

	private static final Log LOG = LogFactory.getLog(PermissionLevelManagerImpl.class);
	private EventTrackingService eventTrackingService;
	private SessionManager sessionManager;
	private IdManager idManager;
	private MessageForumsTypeManager typeManager;
	private AreaManager areaManager;
	
	private Map<String, PermissionLevel> defaultPermissionsMap;
	
	private static final String QUERY_BY_TYPE_UUID = "findPermissionLevelByTypeUuid";
	private static final String QUERY_ORDERED_LEVEL_NAMES = "findOrderedPermissionLevelNames";
	private static final String QUERY_BY_AREA_ALL_FORUMS_MEMBERSHIP = "findAllMembershipItemsForForumsForSite";
	private static final String QUERY_GET_ALL_TOPICS = "findAllTopicsForSite";
	private static final String QUERY_BY_TOPIC_IDS_ALL_TOPIC_MEMBERSHIP = "findAllMembershipItemsForTopicsForSite";
	private static final String QUERY_BY_AREA_ID_ALL_MEMBERSHIP =	"findAllMembershipItemsForSite";
	
	private Boolean autoDdl;
			
	public void init(){
		LOG.info("init()");
		try {

			// add the default permission level and type data, if necessary
			if (autoDdl != null && autoDdl) {
				loadDefaultTypeAndPermissionLevelData();
			}

			// for performance, load the default permission level information now
			// to make it reusable
			initializePermissionLevelData();
		} catch ( Exception ex ) {
			LOG.error("PermissionsLevelManager - a problem occurred loading default permission level data ",ex);
		}

	}
	
	public PermissionLevel getPermissionLevelByName(String name){
		if (LOG.isDebugEnabled()){
			LOG.debug("getPermissionLevelByName executing(" + name + ")");
		}
		
		if (PERMISSION_LEVEL_NAME_OWNER.equals(name)){
			return getDefaultOwnerPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_AUTHOR.equals(name)){
			return getDefaultAuthorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR.equals(name)){
			return getDefaultNoneditingAuthorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_CONTRIBUTOR.equals(name)){
			return getDefaultContributorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_REVIEWER.equals(name)){
			return getDefaultReviewerPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_NONE.equals(name)){
			return getDefaultNonePermissionLevel();
		}		
		else{
			return null;
		}
	}
	
	public  List getOrderedPermissionLevelNames(){

		if (LOG.isDebugEnabled()){
			LOG.debug("getOrderedPermissionLevelNames executing");
		}

		List<String> levelNames = new ArrayList<String>();

		List<PermissionLevel> levels = getDefaultPermissionLevels();
		if (levels != null && !levels.isEmpty()) {
			for (PermissionLevel level : levels) {
				levelNames.add(level.getName());
			}
			
			Collections.sort(levelNames);
		}

		return levelNames;
	}	
	
	public String getPermissionLevelType(PermissionLevel level){
		
		if (LOG.isDebugEnabled()){
			LOG.debug("getPermissionLevelType executing(" + level + ")");
		}
		
		if (level == null) {      
      throw new IllegalArgumentException("Null Argument");
		}
		
		PermissionLevel ownerLevel = getDefaultOwnerPermissionLevel();		
		if (level.equals(ownerLevel)){
			return ownerLevel.getTypeUuid();
		}
				
		PermissionLevel authorLevel = getDefaultAuthorPermissionLevel();		
		if (level.equals(authorLevel)){
			return authorLevel.getTypeUuid();
		}
		
		PermissionLevel noneditingAuthorLevel = getDefaultNoneditingAuthorPermissionLevel();		
		if (level.equals(noneditingAuthorLevel)){
			return noneditingAuthorLevel.getTypeUuid();
		}
				
	  PermissionLevel reviewerLevel = getDefaultReviewerPermissionLevel();
	  if (level.equals(reviewerLevel)){
			return reviewerLevel.getTypeUuid();
		}
	  	  
		PermissionLevel contributorLevel = getDefaultContributorPermissionLevel();
		if (level.equals(contributorLevel)){
			return contributorLevel.getTypeUuid();
		}
				
		PermissionLevel noneLevel = getDefaultNonePermissionLevel();
		if (level.equals(noneLevel)){
			return noneLevel.getTypeUuid();
		}
		
		return null;
	}
	
	/**
	 * Populates the permission level data for the case when the default permission levels
	 * are being created, not the custom levels
	 * @param name
	 * @param typeUuid
	 * @param mask
	 * @param uuid
	 * @return
	 */
	private PermissionLevel createDefaultPermissionLevel(String name, String typeUuid, PermissionsMask mask)
	{
		if (LOG.isDebugEnabled()){
			LOG.debug("createDefaultPermissionLevel executing(" + name + "," + typeUuid + "," + mask + ")");
		}
		
		if (name == null || typeUuid == null || mask == null) {      
			throw new IllegalArgumentException("Null Argument");
		}
								
		PermissionLevel newPermissionLevel = new PermissionLevelImpl();
		Date now = new Date();
		newPermissionLevel.setName(name);
		newPermissionLevel.setUuid(idManager.createUuid());
		newPermissionLevel.setCreated(now);
		newPermissionLevel.setCreatedBy("admin");
		newPermissionLevel.setModified(now);
		newPermissionLevel.setModifiedBy("admin");
		newPermissionLevel.setTypeUuid(typeUuid);
			
		// set permission properties using reflection
		for (Iterator i = mask.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			Boolean value = (Boolean) mask.get(key);
			try{
			  PropertyUtils.setSimpleProperty(newPermissionLevel, key, value);
			}
			catch (Exception e){
				throw new Error(e);
			}
		}										
				
		return newPermissionLevel;		
	}
	
	public PermissionLevel createPermissionLevel(String name, String typeUuid, PermissionsMask mask){
		
		if (LOG.isDebugEnabled()){
			LOG.debug("createPermissionLevel executing(" + name + "," + typeUuid + "," + mask + ")");
		}
		
		if (name == null || typeUuid == null || mask == null) {      
      throw new IllegalArgumentException("Null Argument");
		}
								
		PermissionLevel newPermissionLevel = new PermissionLevelImpl();
		Date now = new Date();
		String currentUser = getCurrentUser();
		newPermissionLevel.setName(name);
		newPermissionLevel.setUuid(idManager.createUuid());
		newPermissionLevel.setCreated(now);
		newPermissionLevel.setCreatedBy(currentUser);
		newPermissionLevel.setModified(now);
		newPermissionLevel.setModifiedBy(currentUser);
		newPermissionLevel.setTypeUuid(typeUuid);
			
		// set permission properties using reflection
		for (Iterator i = mask.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			Boolean value = (Boolean) mask.get(key);
			try{
			  PropertyUtils.setSimpleProperty(newPermissionLevel, key, value);
			}
			catch (Exception e){
				throw new Error(e);
			}
		}										
				
		return newPermissionLevel;		
	}
	
  public DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type){
		
		if (LOG.isDebugEnabled()){
			LOG.debug("createDBMembershipItem executing(" + name + "," + type + ")");
		}
		
		if (name == null || type == null) {      
      throw new IllegalArgumentException("Null Argument");
		}
								
		DBMembershipItem newDBMembershipItem = new DBMembershipItemImpl();
		Date now = new Date();
		String currentUser = getCurrentUser();
		newDBMembershipItem.setName(name);
		newDBMembershipItem.setPermissionLevelName(permissionLevelName);
		newDBMembershipItem.setUuid(idManager.createUuid());
		newDBMembershipItem.setCreated(now);
		newDBMembershipItem.setCreatedBy(currentUser);
		newDBMembershipItem.setModified(now);
		newDBMembershipItem.setModifiedBy(currentUser);
		newDBMembershipItem.setType(type);
															
		return newDBMembershipItem;		
	}
  
  public void saveDBMembershipItem(DBMembershipItem item){
  	getHibernateTemplate().saveOrUpdate(item);
  }
  
  public void savePermissionLevel(PermissionLevel level) {
	  getHibernateTemplate().saveOrUpdate(level);
  }
	
  public PermissionLevel getDefaultOwnerPermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultOwnerPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getOwnerLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {    

		  LOG.warn("No permission level data exists for the Owner level in the MFR_PERMISSION_LEVEL_T table. " +
				  "If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		          "to insert the missing permission level data. Default owner permissions will be used.");

		  // return the default owner permission
		  PermissionsMask mask = getDefaultOwnerPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, typeUuid, mask);
	  }
		  
	  return level;
  }

  public PermissionLevel getDefaultAuthorPermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultAuthorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getAuthorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  LOG.warn("No permission level data exists for the Author level in the MFR_PERMISSION_LEVEL_T table. " +
				  "If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		          "to insert the missing permission level data. Default Author permission settings will be used.");

		  // return the default author permission
		  PermissionsMask mask = getDefaultAuthorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, typeUuid, mask);
	  }

	  return level;
  }

  public PermissionLevel getDefaultNoneditingAuthorPermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultNoneditingAuthorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getNoneditingAuthorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  LOG.warn("No permission level data exists for the NoneditingAuthor level in the MFR_PERMISSION_LEVEL_T table. " +
		  		"If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		  		"to insert the missing default permission level data. Default NoneditingAuthor permission settings will be used.");
		  
		  // return the default nonediting author permission
		  PermissionsMask mask = getDefaultNoneditingAuthorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, typeUuid, mask);

	  }
	  
	  return level;
  }

  public PermissionLevel getDefaultReviewerPermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultReviewerPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getReviewerLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  LOG.warn("No permission level data exists for the Reviewer level in the MFR_PERMISSION_LEVEL_T table. " +
		  		"If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		  		"to insert the missing permission level data. Default Reviewer permissions will be used.");
		  
		  // return the default reviewer permission
		  PermissionsMask mask = getDefaultReviewerPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, typeUuid, mask);

	  }
	  
	  return level;
  }

  public PermissionLevel getDefaultContributorPermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultContributorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getContributorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  LOG.warn("No permission level data exists for the Contributor level in the MFR_PERMISSION_LEVEL_T table. " +
		  		"If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		  		"to insert the missing permission level data. Default Contributor permissions will be used.");
		  
		  // return the default contributor permission
		  PermissionsMask mask = getDefaultContributorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, typeUuid, mask);

	  }

	  return level;	
  }

  public PermissionLevel getDefaultNonePermissionLevel(){

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultNonePermissionLevel executing");
	  }

	  String typeUuid = typeManager.getNoneLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {    
		  LOG.warn("No permission level data exists for the None level in the MFR_PERMISSION_LEVEL_T table. " +
		  		"If you have autoDdl=false, look at mfr_m2-m3_mysq_conversion.sql or mfr_m2-m3_oracle_conversion.sql" +
		  		"to insert the missing permission level data. Default None permissions will be used.");
		  
		// return the default None permission
		  PermissionsMask mask = getDefaultNonePermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, typeUuid, mask);

	  }
	
	  return level;
  }
  	
  /**
   * 
   * @param typeUuid
   * @return the PermissionLevel for the given typeUuid. Returns null if no
   * PermissionLevel found.
   */
  private PermissionLevel getDefaultPermissionLevel(final String typeUuid){

	  if (typeUuid == null) {      
		  throw new IllegalArgumentException("Null Argument");
	  }

	  if (LOG.isDebugEnabled()){
		  LOG.debug("getDefaultPermissionLevel executing with typeUuid: " + typeUuid);
	  }

	  PermissionLevel level = null;

	  if(defaultPermissionsMap != null && defaultPermissionsMap.containsKey(typeUuid)) {
		  // check to see if it is already in the map that was created at startup
		  level =  ((PermissionLevel)defaultPermissionsMap.get(typeUuid)).clone();
		  if (LOG.isDebugEnabled()) LOG.debug("got Default PermissionLevel from defaultPermissionsMap as " + level);
	  
	  } else {
		  // retrieve it from the table
		  HibernateCallback hcb = new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException, SQLException {
				  Query q = session.getNamedQuery(QUERY_BY_TYPE_UUID);
				  q.setParameter("typeUuid", typeUuid);            

				  return q.uniqueResult();
			  }
		  };

		  level = (PermissionLevel) getHibernateTemplate().execute(hcb);
		  if (LOG.isDebugEnabled()) LOG.debug("Returned Permission Level from query was "+level);
	  }

	  return level;

  }	
	
	public Boolean getCustomPermissionByName(String customPermName, PermissionLevel permissionLevel) {
    	if (customPermName == null) 
    		throw new IllegalArgumentException("Null permissionLevelName passed");
    	if (permissionLevel == null)
    		throw new IllegalArgumentException("Null permissionLevel passed");
    		  
    	if (customPermName.equals(PermissionLevel.NEW_FORUM))
    		return permissionLevel.getNewForum();
    	else if (customPermName.equals(PermissionLevel.NEW_RESPONSE))
    		return permissionLevel.getNewResponse();
    	else if (customPermName.equals(PermissionLevel.NEW_RESPONSE_TO_RESPONSE))
    		return permissionLevel.getNewResponseToResponse();
    	else if (customPermName.equals(PermissionLevel.NEW_TOPIC))
    		return permissionLevel.getNewTopic();
    	else if (customPermName.equals(PermissionLevel.POST_TO_GRADEBOOK))
    		return permissionLevel.getPostToGradebook();
    	else if (customPermName.equals(PermissionLevel.DELETE_ANY))
    		return permissionLevel.getDeleteAny();
    	else if (customPermName.equals(PermissionLevel.DELETE_OWN))
    		return permissionLevel.getDeleteOwn();
    	else if (customPermName.equals(PermissionLevel.MARK_AS_READ))
    		return permissionLevel.getMarkAsRead();
    	else if (customPermName.equals(PermissionLevel.MODERATE_POSTINGS))
    		return permissionLevel.getModeratePostings();
    	else if (customPermName.equals(PermissionLevel.MOVE_POSTING))
    		return permissionLevel.getMovePosting();
    	else if (customPermName.equals(PermissionLevel.READ))
    		return permissionLevel.getRead();
    	else if (customPermName.equals(PermissionLevel.REVISE_ANY))
    		return permissionLevel.getReviseAny();
    	else if (customPermName.equals(PermissionLevel.REVISE_OWN))
    		return permissionLevel.getReviseOwn();
    	else if (customPermName.equals(PermissionLevel.CHANGE_SETTINGS))
    		return permissionLevel.getChangeSettings();
    	else 
    		return null;
    }
	
	public List getCustomPermissions() {
		List customPerms = new ArrayList();
		customPerms.add(PermissionLevel.NEW_FORUM);
		customPerms.add(PermissionLevel.NEW_RESPONSE);
		customPerms.add(PermissionLevel.NEW_RESPONSE_TO_RESPONSE);
		customPerms.add(PermissionLevel.NEW_TOPIC);
		customPerms.add(PermissionLevel.DELETE_ANY);
		customPerms.add(PermissionLevel.DELETE_OWN);
		customPerms.add(PermissionLevel.MARK_AS_READ);
		customPerms.add(PermissionLevel.MODERATE_POSTINGS);
		customPerms.add(PermissionLevel.MOVE_POSTING);
		customPerms.add(PermissionLevel.POST_TO_GRADEBOOK);
		customPerms.add(PermissionLevel.READ);
		customPerms.add(PermissionLevel.REVISE_ANY);
		customPerms.add(PermissionLevel.REVISE_OWN);
		customPerms.add(PermissionLevel.CHANGE_SETTINGS);
		
		return customPerms;
	}
    
    
	
	private String getCurrentUser() {    
		String user = sessionManager.getCurrentSessionUserId();
		return (user == null) ? "test-user" : user;    
  }
	
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}

	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setAreaManager(AreaManager areaManager) {
		this.areaManager = areaManager;
	}
	
	public List getAllMembershipItemsForForumsForSite(final Long areaId)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getAllMembershipItemsForForumsForSite executing");
		}
		
		HibernateCallback hcb = new HibernateCallback() 
		{
      public Object doInHibernate(Session session) throws HibernateException, SQLException 
      {
        Query q = session.getNamedQuery(QUERY_BY_AREA_ALL_FORUMS_MEMBERSHIP);
        q.setParameter("areaId", areaId, Hibernate.LONG);
        return q.list();
      }
    };
					
    return (List) getHibernateTemplate().execute(hcb);
	}

	private List getAllTopicsForSite(final Long areaId)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getAllTopicsForSite executing");
		}
		
		HibernateCallback hcb = new HibernateCallback() 
		{
      public Object doInHibernate(Session session) throws HibernateException, SQLException 
      {
        Query q = session.getNamedQuery(QUERY_GET_ALL_TOPICS);
        q.setParameter("areaId", areaId, Hibernate.LONG);
        return q.list();
      }
    };
    List topicList = (List) getHibernateTemplate().execute(hcb);
    List ids = new ArrayList();
    
    try
    {
    	if(topicList != null)
    	{
    		for(int i=0; i<topicList.size(); i++)
    		{
    			Object[] thisObject =(Object[]) topicList.get(i);
    			if(thisObject != null)
    			{
    				for(int j=0; j<thisObject.length; j++)
    				{
    					Object thisTopic = (Object) thisObject[j];
    					if(thisTopic instanceof Topic)
    					{
    						ids.add(((Topic)thisTopic).getId());
    						break;
    					}
    				}
    			}
    		}
    	}
    }
    catch(Exception e)
    {
    	LOG.error("PermissionLevelManagerImpl.getAllTopicsForSite--" + e);
    }
    
    return ids;
	}
    
	public List getAllMembershipItemsForTopicsForSite(final Long areaId)
	{
		final List topicIds = this.getAllTopicsForSite(areaId);
		
		if(topicIds != null && topicIds.size() >0)
		{
			HibernateCallback hcb1 = new HibernateCallback() 
			{
				public Object doInHibernate(Session session) throws HibernateException, SQLException 
				{
					Query q = session.getNamedQuery(QUERY_BY_TOPIC_IDS_ALL_TOPIC_MEMBERSHIP);
					q.setParameterList("topicIdList", topicIds);
					return q.list();
				}
			};
			return (List) getHibernateTemplate().execute(hcb1);
		}
		else
			return new ArrayList();
	}
	
	private void initializePermissionLevelData()
	{
		if (LOG.isDebugEnabled()){
			LOG.debug("loadInitialDefaultPermissionLevel executing");
		}
		
		defaultPermissionsMap = new HashMap();
		
		defaultPermissionsMap.put(typeManager.getOwnerLevelType(), getDefaultOwnerPermissionLevel());
		defaultPermissionsMap.put(typeManager.getAuthorLevelType(), getDefaultAuthorPermissionLevel());
		defaultPermissionsMap.put(typeManager.getNoneditingAuthorLevelType(), getDefaultNoneditingAuthorPermissionLevel());
		defaultPermissionsMap.put(typeManager.getContributorLevelType(), getDefaultContributorPermissionLevel());
		defaultPermissionsMap.put(typeManager.getReviewerLevelType(), getDefaultReviewerPermissionLevel());
		defaultPermissionsMap.put(typeManager.getNoneLevelType(), getDefaultNonePermissionLevel());	
	}
	
	private void loadDefaultTypeAndPermissionLevelData() {
		try {
			// first, call the methods that will load type data if it is missing
			String ownerType = typeManager.getOwnerLevelType();
			String authorType = typeManager.getAuthorLevelType();
			String contributorType = typeManager.getContributorLevelType();
			String reviewerType = typeManager.getReviewerLevelType();
			String noneditingAuthorType = typeManager.getNoneditingAuthorLevelType();
			String noneType = typeManager.getNoneLevelType();

			// now let's check to see if we need to add the default permission level
			// data
			if (getDefaultPermissionLevel(ownerType) == null) {
				PermissionsMask mask = getDefaultOwnerPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, ownerType, mask);

				savePermissionLevel(permLevel);
			}

			if (getDefaultPermissionLevel(authorType) == null) {
				PermissionsMask mask = getDefaultAuthorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, authorType, mask);

				savePermissionLevel(permLevel);
			}

			if (getDefaultPermissionLevel(contributorType) == null) {
				PermissionsMask mask = getDefaultContributorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, contributorType, mask);

				savePermissionLevel(permLevel);
			}

			if (getDefaultPermissionLevel(reviewerType) == null) {
				PermissionsMask mask = getDefaultReviewerPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, reviewerType, mask);

				savePermissionLevel(permLevel);
			}

			if (getDefaultPermissionLevel(noneditingAuthorType) == null) {
				PermissionsMask mask = getDefaultNoneditingAuthorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, noneditingAuthorType, mask);

				savePermissionLevel(permLevel);
			}

			if (getDefaultPermissionLevel(noneType) == null) {
				PermissionsMask mask = getDefaultNonePermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, noneType, mask);

				savePermissionLevel(permLevel);
			}
		} catch (Exception e) {
			LOG.warn("Error loading initial default types and/or permissions", e);
		}
	}

	public void deleteMembershipItems(Set membershipSet)
	{
		if(membershipSet != null)
		{
			Iterator iter = membershipSet.iterator();
			Set permissionLevels = new HashSet();
			while(iter.hasNext())
			{
				DBMembershipItem item = (DBMembershipItem) iter.next();
				if(item != null && item.getPermissionLevel() != null && PermissionLevelManager.PERMISSION_LEVEL_NAME_CUSTOM.equals(item.getPermissionLevelName()))
				{
					permissionLevels.add((PermissionLevel)item.getPermissionLevel());
				}
			}
			getHibernateTemplate().deleteAll(membershipSet);
			getHibernateTemplate().deleteAll(permissionLevels);
		}
	}
	
	private PermissionsMask getDefaultOwnerPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(true)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(true));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(true));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(true));
		  mask.put(PermissionLevel.READ, new Boolean(true));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(true));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(true));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(true));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(true));
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultAuthorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(true)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(true));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(true));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(true));
		  mask.put(PermissionLevel.READ, new Boolean(true));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(true));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(true));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(true));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(false));
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultContributorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(false)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(false));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(false));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(false));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(false));
		  mask.put(PermissionLevel.READ, new Boolean(true));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(true));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(false));
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultNoneditingAuthorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(true)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(true));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(false));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(true));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(true));
		  mask.put(PermissionLevel.READ, new Boolean(true));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(true));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(true));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(false));
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultNonePermissionsMask() {
		  PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(false)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(false));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(false));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(false));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(false));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(false));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(false));
		  mask.put(PermissionLevel.READ, new Boolean(false));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(false));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(false));
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultReviewerPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, new Boolean(false)); 
		  mask.put(PermissionLevel.NEW_TOPIC, new Boolean(false));
		  mask.put(PermissionLevel.NEW_RESPONSE, new Boolean(false));
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, new Boolean(false));
		  mask.put(PermissionLevel.MOVE_POSTING, new Boolean(false));
		  mask.put(PermissionLevel.CHANGE_SETTINGS,new Boolean(false));
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, new Boolean(false));
		  mask.put(PermissionLevel.READ, new Boolean(true));
		  mask.put(PermissionLevel.MARK_AS_READ,new Boolean(true));
		  mask.put(PermissionLevel.MODERATE_POSTINGS, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.DELETE_ANY, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_OWN, new Boolean(false));
		  mask.put(PermissionLevel.REVISE_ANY, new Boolean(false));
		  
		  return mask;
	}
	
	public void setAutoDdl(Boolean autoDdl) {
		this.autoDdl = autoDdl;
	}
	
	public List<PermissionLevel> getDefaultPermissionLevels() {
		// first, check for the levels in the map. if map is null,
		// return the default permission level data
		List<PermissionLevel> defaultLevels = new ArrayList<PermissionLevel>();
		if (defaultPermissionsMap != null && !defaultPermissionsMap.isEmpty()) {
			defaultLevels.addAll(defaultPermissionsMap.values());
		} else {
			if (LOG.isDebugEnabled()) LOG.debug("Default permissions map was null!! Loading defaults to return from getDefaultPermissionLevels");
			defaultLevels.add(getDefaultOwnerPermissionLevel());
			defaultLevels.add(getDefaultAuthorPermissionLevel());
			defaultLevels.add(getDefaultContributorPermissionLevel());
			defaultLevels.add(getDefaultNoneditingAuthorPermissionLevel());
			defaultLevels.add(getDefaultNonePermissionLevel());
			defaultLevels.add(getDefaultReviewerPermissionLevel());
		}
		
		return defaultLevels;
	}
 }
