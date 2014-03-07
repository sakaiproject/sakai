/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

package org.sakaiproject.user.tool.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.tool.UserPrefsTool;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;


public class UserPrefsEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, RequestStorable {

	private static Log log = LogFactory.getLog(UserPrefsEntityProvider.class);
	public static String PREFIX = "userPrefs";
	private PreferencesService preferencesService;
	private SessionManager sessionManager;
	private RequestStorage requestStorage;

	public void init() {
		log.info("init()");
	}
		
	public String getEntityPrefix() {
		return PREFIX;
	}
	public boolean entityExists(String id) {
		log.debug(this + " entityExists() " + id);
		boolean rv = false;
		Preferences p = preferencesService.getPreferences(id);
		if (p != null)
		{
			rv = true;
		}
		return rv;
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
		// TODO Auto-generated method stub
		String queryString = (String) params.get("queryString");
		if (queryString != null)
		{
			String[] parts = queryString.split("=");
			if (parts != null && parts.length==2)
			{
				String key = parts[0];
				String val = parts[1];
				String userId = getUserId();
				PreferencesEdit m_edit = getPreferencesEdit(userId);
				
				ResourcePropertiesEdit props = m_edit.getPropertiesEdit("resourcesColumn");
				props.addProperty(key, val); // Save the permission to see if it changes the next time they sign in
				
				preferencesService.commit(m_edit);
			}
			
		}

	}
	
    /**
	 * Set editing mode on for user and add user if not existing
	 */
	private PreferencesEdit getPreferencesEdit(String userId) {

		PreferencesEdit m_edit = null;
		try {
			m_edit = preferencesService.edit(userId);
		} catch (IdUnusedException e) {
			try {
				m_edit = preferencesService.add(userId);
			} catch (Exception ee) {
				log.error("getPreferencesEdit: " + ee.getMessage());
				return null;
			}
		} catch (InUseException e) {
			log.error("getPreferencesEdit: " + e.getMessage());
			return null;
		} catch (PermissionException e) {
			log.error("getPreferencesEdit: " + e.getMessage());
			return null;
		}
		
		return m_edit;
	}
	

	public Object getEntity(EntityReference ref) {
		
		Entity rv = null;
		
		if (ref != null)
		{
			log.debug(this + ".getEntity() " + ref.getReference());
			
			if (PREFIX.equals(ref.getPrefix()))
			{
				String userId = ref.getId();
				rv = preferencesService.getPreferences(userId);
				if (rv == null)
				{
					try {
						rv = preferencesService.add(userId);
					} catch (Exception ee) {
						log.error(" getEntity: " + ee.getMessage());
					}
				}
				
			}
		}
			
		return rv;
	}
	
	/**
	 * delete user preference entity
	 */
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		log.debug(this + ".deleteEntity of user  " + ref);
		try
		{
			PreferencesEdit edit = preferencesService.edit(ref.getId());
			
			// now remove the preference 
			preferencesService.remove(edit);
		}
		catch (IdUnusedException e)
		{
			log.warn(this + ".deleteEntity of user  " + ref + " " + e.getMessage());
		}
		catch (PermissionException e)
		{
			log.warn(this + ".deleteEntity of user  " + ref + " " + e.getMessage());
		}
		catch (InUseException e)
		{
			log.warn(this + ".deleteEntity of user  " + ref + " " + e.getMessage());
		}

	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledOutputFormats() {
		 return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
	}

	public String[] getHandledInputFormats() {
		 return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
	}

	public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }
    
    public PreferencesService getPreferencesService() {
    	return this.preferencesService;
    }
    
    public void setPreferencesService(PreferencesService preferencesService) {
    	this.preferencesService = preferencesService;
    }
    
    public SessionManager getSessionManager() {
    	return this.sessionManager;
    }
    
    public void setSessionManager(SessionManager sessionManager) {
    	this.sessionManager = sessionManager;
    }

    @EntityCustomAction(action="saveDivState", viewKey=EntityView.VIEW_EDIT)
    public void doSaveDivState(EntityView view) {
    	
    	String key = requestStorage.getStoredValueAsType(String.class, "key");
    	String state = requestStorage.getStoredValueAsType(String.class, "state");
    	log.debug("key: " + key);
    	log.debug("state: " + state);
    	
    	PreferencesEdit prefs = getPrefsEdit();
    	ResourcePropertiesEdit expandProps = prefs.getPropertiesEdit(UserPrefsTool.PREFS_EXPAND);
		if (expandProps != null) {
			expandProps.addProperty(key, state);
		}
		preferencesService.commit(prefs);
    }
    
    /**
	 * @return Returns the userId.
	 */
	private String getUserId()
	{
		return getSessionManager().getCurrentSessionUserId();
	}
	
	/**
	 * Set editing mode on for user and add user if not existing
	 */
	protected PreferencesEdit getPrefsEdit()
	{
		PreferencesEdit edit = null;
		log.debug("getPrefsEdit()");

		try
		{
			edit = preferencesService.edit(getUserId());
		}
		catch (IdUnusedException e)
		{
			try
			{
				edit = preferencesService.add(getUserId());
			}
			catch (Exception ee)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(ee.toString()));
			}
		}
		catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.toString()));
		}
		return edit;
	}
	
	/**
	 * 
	 * Get a list of resources in a site
	 * 
	 * site/siteId
	 */
	@EntityCustomAction(action = "key", viewKey = EntityView.VIEW_LIST)
	public Map<String, Object> getKeyProperties(EntityView view) {
		
		Map<String, Object> rv = new HashMap<String, Object>();
		
		// get userId
		String userId = view.getPathSegment(2);
		String key = view.getPathSegment(3);

		if(log.isDebugEnabled()) {
			log.debug(this + " getKeyProperties for userId=" + userId + " key=" + key);
		}
		
		Preferences pref = preferencesService.getPreferences(userId);
		if (pref == null)
		{
			try {
				pref = preferencesService.add(userId);
			} catch (Exception ee) {
				log.error(this + " getKeyProperties: " + ee.getMessage());
			}
		}
		
		if (pref != null)
		{
			ResourceProperties p = pref.getProperties(key);
			
			for (Iterator<String> iNames = p.getPropertyNames(); iNames.hasNext();)
			{
				String name = iNames.next();
				String value = p.getProperty(name);
				rv.put(name, value);
			}
		}
		return rv;
	}
}
