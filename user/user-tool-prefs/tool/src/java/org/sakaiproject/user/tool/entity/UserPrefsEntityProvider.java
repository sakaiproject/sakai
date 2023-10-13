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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.exception.EntityException;
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
import org.sakaiproject.user.tool.UserPrefsTool;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

@Slf4j
@Setter
public class UserPrefsEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful, RequestStorable {

	public static String PREFIX = "userPrefs";
	private PreferencesService preferencesService;
	private SessionManager sessionManager;
	private RequestStorage requestStorage;

	public String getEntityPrefix() {
		return PREFIX;
	}

	public boolean entityExists(String id) {

		boolean rv = false;
		Preferences p = preferencesService.getPreferences(id);
		if (p != null) {
			rv = true;
		}
		log.debug("id = {}, rv = {}", id, rv);
		return rv;
	}

	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return null;
	}

	public Object getSampleEntity() {
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	}

	private Preferences getPreferences(String userId) {

		try {
			return preferencesService.getPreferences(userId);
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}
	
    /**
	 * Set editing mode on for user and add user if not existing
	 */
	private PreferencesEdit getPreferencesEdit(String userId) {

		PreferencesEdit preference = null;
		try {
			try {
				preference = preferencesService.edit(userId);
			} catch (IdUnusedException iue) {
				preference = preferencesService.add(userId);
			}
		} catch (Exception e) {
			log.warn("Could not get the preferences for user [{}], {}", userId, e.toString());
		}

		return preference;
	}
	
	public Object getEntity(EntityReference ref) {

		String sessionUserID = getUserId();
		if (sessionUserID == null) {
			throw new SecurityException("You must be logged in to use this action.");
		}

		Entity rv = null;
		if (ref != null)
		{
			log.debug("reference = {}", ref.getReference());
			
			if (PREFIX.equals(ref.getPrefix()))
			{
				String requestedUserID = ref.getId();
				if (!requestedUserID.equals(sessionUserID) && !developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
					throw new SecurityException("You can't use this action to view other users' preferences.");
				}

				rv = preferencesService.getPreferences(requestedUserID);
				if (rv == null)
				{
					try {
						rv = preferencesService.add(requestedUserID);
					} catch (Exception ee) {
						log.error(ee.getMessage());
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
		log.debug("user = {}", ref);
		String sessionUserID = getUserId();
		if (sessionUserID == null) {
			throw new SecurityException("You must be logged in to use this action.");
		}

		String requestedUserID = ref.getId();
		if (!requestedUserID.equals(sessionUserID) && !developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			throw new SecurityException("You can't use this action to delete other users' preferences.");
		}

		try
		{
			PreferencesEdit edit = preferencesService.edit(requestedUserID);
			
			// now remove the preference 
			preferencesService.remove(edit);
		}
		catch (IdUnusedException e)
		{
			log.warn("user = {}", ref, e.getMessage());
			throw new EntityException("UserPrefsEntityProvider get UserPreference not found for ", requestedUserID, 404);
		}
		catch (PermissionException e)
		{
			log.warn("user = {}", ref, e.getMessage());
			throw new EntityException("UserPrefsEntityProvider get UserPreference not permitted for ", requestedUserID, 403);
		}
		catch (InUseException e)
		{
			log.warn("user = {}", ref, e.getMessage());
			throw new EntityException("UserPrefsEntityProvider get UserPreference not found for", requestedUserID, 404);
		}

	}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}

	public String[] getHandledOutputFormats() {
		 return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
	}

	public String[] getHandledInputFormats() {
		 return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
	}

    /**
     * Save key-value pair as current user preferences. 
     * Here is the request url pattern: /direct/userPrefs/saveDivState/{key_name}/{state_value}
     * @param view
     */
    @EntityCustomAction(action="saveDivState", viewKey=EntityView.VIEW_EDIT)
    public void doSaveDivState(EntityView view) {
    	
    	String key = requestStorage.getStoredValueAsType(String.class, "key");
    	String state = requestStorage.getStoredValueAsType(String.class, "state");
        log.debug("key: {}, state: {}", key, state);

        String userId = getUserId();
        PreferencesEdit preferences = getPreferencesEdit(userId);
        if (preferences != null) {
            try {
                ResourcePropertiesEdit expandProps = preferences.getPropertiesEdit(UserPrefsTool.PREFS_EXPAND);
                if (expandProps != null) {
                    expandProps.addProperty(key, state);
                }
            } catch (Exception e) {
                log.warn("Could not set the preference prefs.expand for user [{}], {}", userId, e.toString());
                preferencesService.cancel(preferences);
                preferences = null;
            } finally {
                if (preferences != null) preferencesService.commit(preferences);
            }
        }
    }
    
    /**
	 * @return Returns the userId.
	 */
	private String getUserId()
	{
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * 
	 * Get a list of resources in a site
	 * 
	 * site/siteId
	 */
	@EntityCustomAction(action = "key", viewKey = EntityView.VIEW_LIST)
	public Map<String, Object> getKeyProperties(EntityView view) {

		String sessionUserID = getUserId();
		if (sessionUserID == null) {
			throw new SecurityException("You must be logged in to use this action.");
		}

		Map<String, Object> rv = new HashMap<>();

		// get userId
		String requestedUserID = view.getPathSegment(2);
		String key = view.getPathSegment(3);
		if (!requestedUserID.equals(sessionUserID) && !developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			throw new SecurityException("You can't use this action to view other users' preferences.");
		}

		if(log.isDebugEnabled()) {
			log.debug("userId={}, key={}", requestedUserID, key);
		}
		
		Preferences pref = preferencesService.getPreferences(requestedUserID);
		if (pref == null)
		{
			try {
				pref = preferencesService.add(requestedUserID);
			} catch (Exception ee) {
				log.error(ee.getMessage());
			}
		}
		
		if (pref != null && key != null)
		{
			ResourceProperties p = pref.getProperties(key);
			
			for (Iterator<String> iNames = p.getPropertyNames(); iNames.hasNext();)
			{
				String name = iNames.next();
				List<String> values = p.getPropertyList(name);

				if (values.size() == 1) {
					rv.put(name, values.get(0));
				} else if (values.size() > 1) {
					rv.put(name, values);
				} else {
					rv.put(name, null);
					log.info("No value for property '%s'. Setting null ...", name);
				}
			}
		}
		return rv;
	}

	/**
	 * update the key-ed property values 
	 * 
	 * use the following format to invoke this function:
	 *"/direct/userPrefs/updateKey/[user_id]/[key_name]/[name=val&name1=val1...]
	 * @param view
	 */
	@EntityCustomAction(action = "updateKey", viewKey = EntityView.VIEW_EDIT)
	public void updateKeyProperties(EntityView view) {

		String sessionUserID = getUserId();
		if (sessionUserID == null) {
			throw new SecurityException("You must be logged in to use this action.");
		}

		// get all params
		final String requestedUserID = view.getPathSegment(2);
		if (!requestedUserID.equals(sessionUserID) && !developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			throw new SecurityException("You can't use this action to update other users' preferences.");
		}
		final String key = view.getPathSegment(3);
		final Map<String, Object> params = requestStorage.getStorageMapCopy();

		log.debug("updateKeyProperties for userId={}, key={}", requestedUserID, key);
		
		String queryString = (String) params.get("queryString");
		log.debug("queryString = {}", queryString);
		if (queryString != null) {
			// queryString is of type name1=val1&name2=val2&name3=val3...
			String[] pairs = queryString.split("&");

			if (pairs != null && pairs.length > 0) {
				// Build a map of the supplies properties
				Map<String, String> suppliedProps = new HashMap<>();
				for (String pair : pairs) {
					String[] parts = pair.split("=");
					if (parts != null && parts.length == 2) {
						suppliedProps.put(parts[0], parts[1]);
					}
				}
				// get the edit object
				Preferences existingPrefs = getPreferences(requestedUserID);
				Map<String, String> propsToSet;
				if (existingPrefs != null) {
					ResourceProperties existingProps = existingPrefs.getProperties(key);
					propsToSet = suppliedProps.entrySet().stream().filter(e -> {

						String existingVal = existingProps.getProperty(e.getKey());
						return existingVal == null || !existingVal.equals(e.getValue());
					}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				} else {
					propsToSet = suppliedProps;
				}

				if (!propsToSet.isEmpty()) {
					log.debug("We have some props to set. Getting edit lock ...");
					PreferencesEdit editPrefs = getPreferencesEdit(requestedUserID);

					if (editPrefs != null) {
						try {
							ResourcePropertiesEdit editProps = editPrefs.getPropertiesEdit(key);
							propsToSet.forEach(editProps::addProperty);
							log.debug("Props set! Committing preferences edit ...");
						} catch (Exception e) {
							log.warn("Could not update keys for user [{}], {}", requestedUserID, e.toString());
							preferencesService.cancel(editPrefs);
							editPrefs = null;
						} finally {
							if (editPrefs != null) preferencesService.commit(editPrefs);
						}
					} else {
						log.warn("Could not get a lock on prefs to update for user: {}", requestedUserID);
					}
				} else {
					log.debug("No new props to set");
				}
			}
		}
	}
}
