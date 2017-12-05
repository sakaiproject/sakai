/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/access/trunk/access-impl/impl/src/java/org/sakaiproject/access/tool/AccessServlet.java $
 * $Id: AccessServlet.java 17063 2006-10-11 19:48:42Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.privacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Query;

import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupAdvisor;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hbm.privacy.PrivacyRecord;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class PrivacyManagerImpl extends HibernateDaoSupport implements PrivacyManager, AuthzGroupAdvisor
{
	private static final String QUERY_BY_USERID_CONTEXTID_TYPEID = "findPrivacyByUserIdContextIdType";
	private static final String QUERY_BY_DISABLED_USERID_CONTEXTID = "findDisabledPrivacyUserIdContextIdType";
	private static final String QUERY_BY_CONTEXT_VIEWABLE_TYPE = "finalPrivacyByContextViewableType";
	private static final String QUERY_BY_CONTEXT__TYPE = "finalPrivacyByContextType";
	private static final String QUERY_BY_CONTEXT__TYPE_IDLIST = "finalPrivacyByContextTypeAndUserIds";
	private static final String QUERY_BY_CONTEXT_VIEWABLE_TYPE_IDLIST = "finalPrivacyByContextViewableTypeUserList";
	private static final String CONTEXT_ID = "contextId";
	private static final String USER_ID = "userId";
	private static final String RECORD_TYPE = "recordType";
	private static final String VIEWABLE = "viewable";

	private PreferencesService preferencesService;
	private AuthzGroupService authzGroupService;
	private PlatformTransactionManager transactionManager;
	private TransactionTemplate transactionTemplate;
	
	protected boolean defaultViewable = true;
	protected Boolean overrideViewable = null;
	protected boolean userRecordHasPrecedence = true;
	protected int maxResultSetNumber = 1000;
	
	public void init() {
		Assert.notNull(transactionManager, "The 'transactionManager' argument must not be null.");
		transactionTemplate = new TransactionTemplate(transactionManager);
		authzGroupService.addAuthzGroupAdvisor(this);
	}

	public void destroy() {
		authzGroupService.removeAuthzGroupAdvisor(this);
	}

	@Transactional(readOnly = true)
	public Set findViewable(String contextId, Set userIds)
	{
		if (contextId == null || userIds == null)
		{
			throw new IllegalArgumentException("Null Argument in findViewable");
		}

		if(overrideViewable!=null)
		{
			if(overrideViewable.booleanValue())
				return userIds;
			else
				return new HashSet();
		}
		
		Iterator iter = userIds.iterator();
		List userIdList = new ArrayList();
		while(iter.hasNext())
		{
			String userId = (String) iter.next();
			if(userId != null)
				userIdList.add(userId);
		}

		Map sysMap = new HashMap();
		Map userMap = new HashMap();
		List pieceList = new ArrayList();
		List resultPieceList = new ArrayList();
		for(int i=0; i <= (int)(userIdList.size() / maxResultSetNumber); i++)
		{
			pieceList.clear();
			if(i == (int)(userIdList.size() / maxResultSetNumber))
			{
				for(int j=0; j<(userIdList.size() % maxResultSetNumber); j++)
				{
					pieceList.add(userIdList.get(j + ((int)i*maxResultSetNumber)));
				}
			}
			else
			{
				for(int j=0; j<maxResultSetNumber; j++)
				{
					pieceList.add(userIdList.get(j + ((int)i*maxResultSetNumber)));
				}
			}

			if(pieceList.size() > 0)
			{
				resultPieceList = getPrivacyByContextAndTypeAndUserIds(contextId, PrivacyManager.SYSTEM_RECORD_TYPE, pieceList);
				for(int j=0; j<resultPieceList.size(); j++)
					sysMap.put(((PrivacyRecord)resultPieceList.get(j)).getUserId(), (PrivacyRecord)resultPieceList.get(j));

				resultPieceList = getPrivacyByContextAndTypeAndUserIds(contextId, PrivacyManager.USER_RECORD_TYPE, pieceList);
				for(int j=0; j<resultPieceList.size(); j++)
					userMap.put(((PrivacyRecord)resultPieceList.get(j)).getUserId(), (PrivacyRecord)resultPieceList.get(j));
			}
		}
		
		Set returnSet = new HashSet();
		for(int i=0; i<userIdList.size(); i++)
		{
			String id = (String) userIdList.get(i);
			if(!getDisabled((PrivacyRecord)sysMap.get(id), (PrivacyRecord)userMap.get(id)))
				returnSet.add(id);
		}
		
		return returnSet;
	}

	@Transactional(readOnly = true)
	public Set findHidden(String contextId, Set userIds)
	{
		if (contextId == null || userIds == null)
		{
			throw new IllegalArgumentException("Null Argument in findViewable");
		}

		if(overrideViewable!=null)
		{
			if(overrideViewable.booleanValue())
				return new HashSet();
			else
				return userIds;
		}
		
		Iterator iter = userIds.iterator();
		List userIdList = new ArrayList();
		while(iter.hasNext())
		{
			String userId = (String) iter.next();
			if(userId != null)
				userIdList.add(userId);
		}

		Map sysMap = new HashMap();
		Map userMap = new HashMap();
		List pieceList = new ArrayList();
		List resultPieceList = new ArrayList();
		for(int i=0; i <= (int)(userIdList.size() / maxResultSetNumber); i++)
		{
			pieceList.clear();
			if(i == (int)(userIdList.size() / maxResultSetNumber))
			{
				for(int j=0; j<(userIdList.size() % maxResultSetNumber); j++)
				{
					pieceList.add(userIdList.get(j + ((int)i*maxResultSetNumber)));
				}
			}
			else
			{
				for(int j=0; j<maxResultSetNumber; j++)
				{
					pieceList.add(userIdList.get(j + ((int)i*maxResultSetNumber)));
				}
			}

			if(pieceList.size() > 0)
			{
				resultPieceList = getPrivacyByContextAndTypeAndUserIds(contextId, PrivacyManager.SYSTEM_RECORD_TYPE, pieceList);
				for(int j=0; j<resultPieceList.size(); j++)
					sysMap.put(((PrivacyRecord)resultPieceList.get(j)).getUserId(), (PrivacyRecord)resultPieceList.get(j));

				resultPieceList = getPrivacyByContextAndTypeAndUserIds(contextId, PrivacyManager.USER_RECORD_TYPE, pieceList);
				for(int j=0; j<resultPieceList.size(); j++)
					userMap.put(((PrivacyRecord)resultPieceList.get(j)).getUserId(), (PrivacyRecord)resultPieceList.get(j));
			}
		}
		
		Set returnSet = new HashSet();
		for(int i=0; i<userIdList.size(); i++)
		{
			String id = (String) userIdList.get(i);
			if(getDisabled((PrivacyRecord)sysMap.get(id), (PrivacyRecord)userMap.get(id)))
				returnSet.add(id);
		}
		
		return returnSet;
	}

	@Transactional(readOnly = true)
	public Set getViewableState (String contextId, Boolean value, String recordType)
	{
  	if(contextId == null || value == null || recordType == null)
  	{
      throw new IllegalArgumentException("Null Argument in getViewableState");
  	}
  	
  	try
  	{
  		AuthzGroup realm = authzGroupService.getAuthzGroup(contextId);
  		List users = new ArrayList();
  		users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
  		List siteUserIds = new ArrayList();
  		for(int i=0; i < users.size(); i++)
  			siteUserIds.add(((User)users.get(i)).getId());

  		//List returnedList = getViewableStateList(contextId, value, recordType);
  		List returnedList = new ArrayList();
  		List pieceList = new ArrayList();
  		List resultPieceList = new ArrayList();
  		for(int i=0; i <= (int)(siteUserIds.size() / maxResultSetNumber); i++)
  		{
  			pieceList.clear();
  			if(i == (int)(siteUserIds.size() / maxResultSetNumber))
  			{
  				for(int j=0; j<(siteUserIds.size() % maxResultSetNumber); j++)
  				{
  					pieceList.add(siteUserIds.get(j + ((int)i*maxResultSetNumber)));
  				}
  			}
  			else
  			{
  				for(int j=0; j<maxResultSetNumber; j++)
  				{
  					pieceList.add(siteUserIds.get(j + ((int)i*maxResultSetNumber)));
  				}
  			}
  			
  			if(pieceList.size() > 0)
  			{
  				resultPieceList = getViewableStateList(contextId, value, recordType, pieceList);
  				for(int j=0; j<resultPieceList.size(); j++)
  					returnedList.add(resultPieceList.get(j));
  			}
  		}
  		
  		if(returnedList != null)
  		{
  			Set returnSet = new HashSet();
  			for(int i=0; i<returnedList.size(); i++)
  			{
  				returnSet.add(((PrivacyRecord)returnedList.get(i)).getUserId());
  			}
  			return returnSet;
  		}
  		else
  			return null;
  	}
  	catch(org.sakaiproject.authz.api.GroupNotDefinedException gnde)
  	{
  		return null;
  	}
	}

	@Transactional(readOnly = true)
	public Map getViewableState(String contextId, String recordType)
	{
  	if(contextId == null || recordType == null)
  	{
      throw new IllegalArgumentException("Null Argument in getViewableState");
  	}

  	try
  	{
  		AuthzGroup realm = authzGroupService.getAuthzGroup(contextId);
  		List users = new ArrayList();
  		users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
  		List siteUserIds = new ArrayList();
  		for(int i=0; i < users.size(); i++)
  			siteUserIds.add(((User)users.get(i)).getId());

  		//List returnedList = getPrivacyByContextAndType(contextId, recordType);
  		List returnedList = new ArrayList();
  		List pieceList = new ArrayList();
  		List resultPieceList = new ArrayList();
  		for(int i=0; i <= (int)(siteUserIds.size() / maxResultSetNumber); i++)
  		{
  			pieceList.clear();
  			if(i == (int)(siteUserIds.size() / maxResultSetNumber))
  			{
  				for(int j=0; j<(siteUserIds.size() % maxResultSetNumber); j++)
  				{
  					pieceList.add(siteUserIds.get(j + ((int)i*maxResultSetNumber)));
  				}
  			}
  			else
  			{
  				for(int j=0; j<maxResultSetNumber; j++)
  				{
  					pieceList.add(siteUserIds.get(j + ((int)i*maxResultSetNumber)));
  				}
  			}

  			if(pieceList.size() > 0)
  			{
  				resultPieceList = getPrivacyByContextAndTypeAndUserIds(contextId, recordType, pieceList);
  				for(int j=0; j<resultPieceList.size(); j++)
  					returnedList.add(resultPieceList.get(j));
  			}
  		}

  		if(returnedList != null)
  		{
  			HashMap returnMap = new HashMap(); 
  			PrivacyRecord pr;
  			for(int i=0; i<returnedList.size(); i++)
  			{
  				pr = (PrivacyRecord)returnedList.get(i);
  				returnMap.put(pr.getUserId(), Boolean.valueOf(pr.isViewable()));
  			}
  			return returnMap;
  		}
  		return null;
  	}
  	catch(org.sakaiproject.authz.api.GroupNotDefinedException gnde)
  	{
		  return null;
  	}
	}

	@Transactional(readOnly = true)
	public boolean isViewable(String contextId, String userId)
	{
		if(contextId == null || userId == null)
		{
			throw new IllegalArgumentException("Null Argument in isViewable");
		}
		
		if(overrideViewable != null)
		{
			return overrideViewable.booleanValue();
		}
		else
		{
			PrivacyRecord sysRecord = getPrivacy(contextId, userId, PrivacyManager.SYSTEM_RECORD_TYPE);
			PrivacyRecord userRecord = getPrivacy(contextId, userId, PrivacyManager.USER_RECORD_TYPE);
			
			return checkPrivacyRecord(sysRecord, userRecord);
		}
	}

	@Transactional(readOnly = true)
	public boolean userMadeSelection(String contextId, String userId){
		if(contextId == null || userId == null)
		{
			throw new IllegalArgumentException("Null Argument in isViewable");
		}
		PrivacyRecord userRecord = getPrivacy(contextId, userId, PrivacyManager.USER_RECORD_TYPE);
			
		return (userRecord == null ? false : true);
	}

	@Transactional
	public void setViewableState(String contextId, String userId, Boolean value, String recordType)
	{
		if (contextId == null || userId == null || value == null || recordType == null)
		{
			throw new IllegalArgumentException("Null Argument in setViewableState");
		}
		
		PrivacyRecord pr = getPrivacy(contextId, userId, recordType);
		if(pr != null)
		{
			pr.setViewable(value.booleanValue());
			savePrivacyRecord(pr);
		}
		else
		{
			pr = createPrivacyRecord(userId, contextId, recordType, value.booleanValue());
		}
	}

	@Transactional
	public void setViewableState(String contextId, Map userViewableState, String recordType)
	{
		if (contextId == null || userViewableState == null || recordType == null)
		{
			throw new IllegalArgumentException("Null Argument in setViewableState");
		}
		
		Set keySet = userViewableState.keySet();
		for(Iterator<Entry<String, Boolean>> mapIter = keySet.iterator(); mapIter.hasNext();)
		{
			Entry<String, Boolean> entry = mapIter.next();
			Boolean viewable = (Boolean) entry.getValue();
			setViewableState(contextId, entry.getKey(), viewable, recordType);
		}
	}

	@Override
	public void update(AuthzGroup group) {
		// the authz group is changing
	
		if (group == null) {
			return;
		}

		// /site/7e7c810d-fbd5-4017-a0bf-76be2d50a79d
		String[] gIdParts = group.getId().split("/");
		
		// only updating site level authz groups
		if (gIdParts.length == 3 && "site".equals(gIdParts[1])) {

			// Perform the updates in a transaction
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

					String context = "/site/" + gIdParts[2];
					List<PrivacyRecord> prList = getPrivacyByContextAndType(context, PrivacyManager.USER_RECORD_TYPE);
					Set<String> grpMembers = new HashSet<>();

					grpMembers.addAll(group.getUsers());

					// ignore members who already have a privacy record for this site
					for (PrivacyRecord record : prList) {
						if(!grpMembers.remove(record.getUserId())) {
							// user is no longer a member of this authz group remove their record
							removePrivacyObject(record);
						}
					}

					// the remaining members will need to lookup their default preference
					for (String member : grpMembers) {
						// the default is visible so we only need to update those that are set to hidden
						String privacy = getDefaultPrivacyState(member);
						if (PrivacyManager.VISIBLE.equals(privacy)) {
							setViewableState(context, member, true, PrivacyManager.USER_RECORD_TYPE);
						} else if (PrivacyManager.HIDDEN.equals(privacy)) {
							setViewableState(context, member, false, PrivacyManager.USER_RECORD_TYPE);
						}
					}
				}
			});
		}
	}
	
	@Override
	public void groupUpdate(AuthzGroup group, String userId, String roleId) {
		// nothing to do for groupUpdate
		return;
	}

	@Override
	public void remove(AuthzGroup group) {
		// when authz groups are removed we cleanup the privacy records
		
		if (group == null) {
			return;
		}

		// /site/7e7c810d-fbd5-4017-a0bf-76be2d50a79d
		String[] gIdParts = group.getId().split("/");
		
		// only removing site level authz groups
		if (gIdParts.length == 3 && "site".equals(gIdParts[1])) {

			// Perform the updates in a transaction
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					String context = "/site/" + gIdParts[2];
					List<PrivacyRecord> prList = getPrivacyByContextAndType(context, PrivacyManager.USER_RECORD_TYPE);

					for (PrivacyRecord record : prList) {
						removePrivacyObject(record);
					}
				}
			});
		}
	}


	@Override
	public void setDefaultPrivacyState(String userId, String visibility) {
		if (userId == null) {
			log.warn("Cannot set priavacy status for a null userId");
			return;
		}
		
		if (visibility == null) {
			visibility = PrivacyManager.VISIBLE;
		}
		
		PreferencesEdit editPref;
		try {
			editPref = preferencesService.edit(userId);
			
			ResourcePropertiesEdit props = editPref.getPropertiesEdit(PRIVACY_PREFS);
			props.addProperty(PrivacyManager.DEFAULT_PRIVACY_KEY, visibility);

			preferencesService.commit(editPref);
		} catch (PermissionException e) {
			log.warn("You do not have the appropriate permissions to edit preferences for user: " + userId + ". " + e.getMessage());
		} catch (InUseException e) {
			log.warn("Preferences for user: " + userId + " are currently being edited. " + e.getMessage());
		} catch (IdUnusedException e) {
			try {
				editPref = preferencesService.add(userId);
				
				ResourcePropertiesEdit props = editPref.getPropertiesEdit(PRIVACY_PREFS);
				props.addProperty(PrivacyManager.DEFAULT_PRIVACY_KEY, visibility);

				preferencesService.commit(editPref);
			} catch (PermissionException e1) {
				// TODO Auto-generated catch block
				log.warn("You do not have the appropriate permissions to edit preferences for user: " + userId + ". " + e1.getMessage());
			} catch (IdUsedException e1) {
				log.warn("No preferences for user: " + userId + " found intially, attempted to add new preferences. " + e1.getMessage());
			}
		}
	}

	@Override
	public String getDefaultPrivacyState(String userId) {
		String privacy = null;
		
		if (userId != null) {
			Preferences prefs = preferencesService.getPreferences(userId);
			ResourceProperties props = prefs.getProperties(PRIVACY_PREFS);
			privacy = props.getProperty(PrivacyManager.DEFAULT_PRIVACY_KEY);
		}
		
		if (privacy == null) {
			// default privacy is visible
			privacy = PrivacyManagerImpl.VISIBLE;
		}
		
		return privacy;
	}

	private PrivacyRecord getPrivacy(final String contextId, final String userId, final String recordType)
	{
		if (contextId == null || userId == null || recordType == null)
		{
			throw new IllegalArgumentException("Null Argument in getPrivacy");
		}

		HibernateCallback<PrivacyRecord> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_USERID_CONTEXTID_TYPEID);
            q.setString(CONTEXT_ID, contextId);
            q.setString(USER_ID, userId);
            q.setString(RECORD_TYPE, recordType);
            return (PrivacyRecord) q.uniqueResult();
        };

		return getHibernateTemplate().execute(hcb);

	}

	
	private String getDisabledPrivacy(final String contextId, final String userId)
	{
		if (contextId == null || userId == null)
		{
			throw new IllegalArgumentException("Null Argument in getDisabledPrivacy");
		}
		PrivacyRecord sysRecord = getPrivacy(contextId, userId, PrivacyManager.SYSTEM_RECORD_TYPE);
		PrivacyRecord userRecord = getPrivacy(contextId, userId, PrivacyManager.USER_RECORD_TYPE);
		if(!checkPrivacyRecord(sysRecord, userRecord))
			return userId;
		else
			return null;
	}
	
	private boolean getDisabled(PrivacyRecord sysRecord, PrivacyRecord userRecord)
	{
		if(!checkPrivacyRecord(sysRecord, userRecord))
			return true;
		else
			return false;
	}

	private PrivacyRecord createPrivacyRecord(final String userId, final String contextId, final String recordType, final boolean viewable) {
		if (userId == null || contextId == null || recordType == null) {
			throw new IllegalArgumentException("Null Argument in createPrivacyRecord");
		} else {
			PrivacyRecord privacy = new PrivacyRecord(userId, contextId, recordType, viewable);
			savePrivacyRecord(privacy);
			return privacy;
		}
	}

  private List<PrivacyRecord> getViewableStateList(final String contextId, final Boolean viewable, final String recordType)
  {
  	if(contextId == null || viewable == null || recordType == null)
  	{
      throw new IllegalArgumentException("Null Argument in getViewableStateList");
  	}
  	
    HibernateCallback<List<PrivacyRecord>> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_BY_CONTEXT_VIEWABLE_TYPE);
      q.setString(CONTEXT_ID, contextId);
      q.setBoolean(VIEWABLE, viewable);
      q.setString(RECORD_TYPE, recordType);
      return q.list();
    };

    return getHibernateTemplate().execute(hcb);
  }

  private List<PrivacyRecord> getViewableStateList(final String contextId, final Boolean viewable, final String recordType, final List userIds)
  {
  	if(contextId == null || viewable == null || recordType == null || userIds == null)
  	{
      throw new IllegalArgumentException("Null Argument in getViewableStateList");
  	}
  	
    HibernateCallback<List<PrivacyRecord>> hcb = session -> {
      Query q = session.getNamedQuery(QUERY_BY_CONTEXT_VIEWABLE_TYPE_IDLIST);
      q.setString(CONTEXT_ID, contextId);
      q.setBoolean(VIEWABLE, viewable);
      q.setString(RECORD_TYPE, recordType);
      q.setParameterList("userIds", userIds);
      return q.list();
    };

    return getHibernateTemplate().execute(hcb);
  }

  private List<PrivacyRecord> getPrivacyByContextAndType(final String contextId, final String recordType)
  {
  	if(contextId == null || recordType == null)
  	{
  		throw new IllegalArgumentException("Null Argument in getPrivacyByContextAndType");
  	}

	  HibernateCallback<List<PrivacyRecord>> hcb = session -> {
		  Query q = session.getNamedQuery(QUERY_BY_CONTEXT__TYPE);
		  q.setString(CONTEXT_ID, contextId);
		  q.setString(RECORD_TYPE, recordType);
		  return q.list();
	  };
  	
  	return getHibernateTemplate().execute(hcb);
  }

  private List<PrivacyRecord> getPrivacyByContextAndTypeAndUserIds(final String contextId, final String recordType, final List userIds)
  {
  	if(contextId == null || recordType == null || userIds == null)
  	{
  		throw new IllegalArgumentException("Null Argument in getPrivacyByContextAndTypeAndUserIds");
  	}

	  HibernateCallback<List<PrivacyRecord>> hcb = session -> {
		  Query q = session.getNamedQuery(QUERY_BY_CONTEXT__TYPE_IDLIST);
		  q.setString(CONTEXT_ID, contextId);
		  q.setString(RECORD_TYPE, recordType);
		  q.setParameterList("userIds", userIds);
		  return q.list();
	  };
  	
  	return getHibernateTemplate().execute(hcb);
  }

  private void savePrivacyRecord(PrivacyRecord privacy)
  {
  	getHibernateTemplate().saveOrUpdate(privacy);
  }

  private void removePrivacyObject(PrivacyRecord o)
  {
    getHibernateTemplate().delete(o);
  }
  
  private boolean checkPrivacyRecord(PrivacyRecord sysRecord, PrivacyRecord userRecord)
  {
		if(sysRecord != null && userRecord != null)
		{
			if(userRecordHasPrecedence)
			{
				return userRecord.isViewable();
			}
			else
				return sysRecord.isViewable();
		}
		else if(sysRecord == null && userRecord == null)
		{
			if(defaultViewable)
				return true;
			else
				return false;
		}
		else if(sysRecord != null)
		{
			return sysRecord.isViewable();
		}
		else
		{
			return userRecord.isViewable();
		}
  }
  
	/**
	 * A 'true' value will set privacy enabled for a user whose privacy settings
	 * are not known. A 'false' value will set privacy disabled for a user whose
	 * privacy settings are not known (i.e. no data found).
	 * 
	 * The default behavior will be to show users or make them viewable.
	 * 
	 * @param defaultViewable
	 *          the defaultViewable to set
	 */
	public void setDefaultViewable(boolean defaultViewable)
	{
		this.defaultViewable = defaultViewable;
	}

	/**
	 * A 'true' value will make all users viewable in the system. A 'false' value
	 * will make all users hidden in the system.
	 * 
	 * Do not set this value for normal operation (non overridden behavior; i.e. null).
	 * 
	 * @param overrideViewable
	 *          the overrideViewable to set
	 */
	public void setOverrideViewable(Boolean overrideViewable)
	{
		this.overrideViewable = overrideViewable;
	}

	/**
	 * A 'true' value indicates that a user record has precedence over a system
	 * record. A 'false' value indicates that a system record has precedence over
	 * a user record
	 * 
	 * @param userRecordHasPrecedence
	 *          the userRecordHasPrecedence to set
	 */
	public void setUserRecordHasPrecedence(boolean userRecordHasPrecedence)
	{
		this.userRecordHasPrecedence = userRecordHasPrecedence;
	}
	
	/**
	 * Set maximum result set number for database query, defulat is 1000.
	 * 
	 * @param maxResultSetNumber
	 *          the maxResultSetNumber to set
	 */
	public void setMaxResultSetNumber(int maxResultSetNumber)
	{
		this.maxResultSetNumber = maxResultSetNumber;
	}

	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
