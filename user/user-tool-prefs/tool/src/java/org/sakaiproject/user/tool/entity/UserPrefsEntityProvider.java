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

import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
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
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.tool.UserPrefsTool;

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
		return true;
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

	}

	public Object getEntity(EntityReference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub

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
		getPreferencesService().commit(prefs);
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
			edit = getPreferencesService().edit(getUserId());
		}
		catch (IdUnusedException e)
		{
			try
			{
				edit = getPreferencesService().add(getUserId());
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


}
