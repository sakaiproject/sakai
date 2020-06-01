/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.charon.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;


/**
 * Handler for manipulating the list of favorite sites for the current user.
 * Handles AJAX requests from the "More Sites" drawer.
 *
 */
@Slf4j
public class FavoritesHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "favorites";
	private static final String SEPARATOR = ";";
	private static final String FAVORITES_PROPERTY = "order";
	private static final String AUTO_FAVORITE_ENABLED_PROPERTY = "autoFavoriteEnabled";
	private static final String SEEN_SITES_PROPERTY = "autoFavoritesSeenSites";
	private static final String FIRST_TIME_PROPERTY = "firstTime";
	private PreferencesService preferencesService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;

	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
		preferencesService = (PreferencesService) ComponentManager.get(PreferencesService.class);
		serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		siteService = (SiteService) ComponentManager.get(SiteService.class);
		userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
	}


	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				UserFavorites favorites = userFavorites(session.getUserId());
				res.setContentType(ContentType.APPLICATION_JSON.toString());
				res.getWriter().write(favorites.toJSON());
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException {
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				UserFavorites favorites = UserFavorites.fromJSON(req.getParameter("userFavorites"));

				synchronized (session) {
					saveUserFavorites(session.getUserId(), favorites);
				}

				res.setContentType(ContentType.APPLICATION_JSON.toString());
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

	public UserFavorites userFavorites(String userId)
		throws PermissionException, PortalHandlerException, InUseException, IdUnusedException {
		UserFavorites result = new UserFavorites();

		if (userId == null) {
			// User isn't logged in
			return result;
		}

		Preferences prefs = preferencesService.getPreferences(userId);
		ResourceProperties props = prefs.getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

		// Find any sites that this user was added to since we last looked
		boolean autoFavorite = serverConfigurationService.getBoolean("portal.autofavorite", true);

		List<String> autofavoritableUserTypes
			= serverConfigurationService.getStringList("portal.autofavoritableUserTypes", Collections.<String>emptyList());

		try {
			if (autofavoritableUserTypes.size() > 0) {
				autoFavorite = autofavoritableUserTypes.contains(userDirectoryService.getUser(userId).getType());

				// This needs setting to false, otherwise existing sites won't be favorited on first login.
				if (autoFavorite) props.addProperty(FIRST_TIME_PROPERTY, String.valueOf(false));
			}
		} catch (UserNotDefinedException e) {
			log.error("Failed to find user for " + userId, e);
		}

		try {
			autoFavorite = props.getBooleanProperty(AUTO_FAVORITE_ENABLED_PROPERTY);
		} catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
			// Take the default
		}

		result.autoFavoritesEnabled = autoFavorite;

		Set<String> favoriteSiteIds = Collections.<String>emptySet();
		List<String> listFavoriteSiteIds = (List<String>)props.getPropertyList(FAVORITES_PROPERTY);
		if (listFavoriteSiteIds != null) {
			favoriteSiteIds = new LinkedHashSet<String>(listFavoriteSiteIds);
		}

		if (autoFavorite) {
			// If the user wants new sites to be automatically added as favorites, slot them in now.
			result.favoriteSiteIds = applyAutoFavorites(userId, props, favoriteSiteIds);
		} else {
			result.favoriteSiteIds = favoriteSiteIds;
		}

		return result;
	}

	private Set<String> applyAutoFavorites(String userId, ResourceProperties existingProps, Set<String> existingFavorites)
		throws PermissionException, PortalHandlerException, InUseException, IdUnusedException {

		// The site list as when we last checked
		Set<String> oldSiteSet = Collections.<String>emptySet();
		List<String> oldSiteList = (List<String>)existingProps.getPropertyList(SEEN_SITES_PROPERTY);
		if (oldSiteList != null) {
			oldSiteSet = new HashSet<String>(oldSiteList);
		}

		//The limit for the number of sites to be added for a first time user
		int firstTimeLimit = serverConfigurationService.getInt(Portal.CONFIG_DEFAULT_TABS, 15);
		
		boolean firstTimeFavs = true;
		try {
			firstTimeFavs = existingProps.getBooleanProperty(FIRST_TIME_PROPERTY);
		} catch (EntityPropertyNotDefinedException e) {
			// Take the default
		} catch (EntityPropertyTypeException e) {
			// Take the default
		}

		// This should not call getUserSites(boolean, boolean) because the property is variable, while the call is cacheable otherwise
		List<String> userSites = siteService.getSiteIds(SelectionType.MEMBER, null, null, null, SortType.CREATED_ON_DESC, null);
		Set<String> newFavorites = new LinkedHashSet<String>();

		for (String userSite : userSites) {
			// If this is the first time running favorites and below the first time limit of sites
			// or if there are some sites that haven't been set as favorite before, add it
			if (firstTimeFavs && newFavorites.size() >= firstTimeLimit) {
				log.debug("First time favorites size limit exceeded {} for {}", firstTimeLimit, userId);
				break;
			}
			if (firstTimeFavs || (!oldSiteSet.contains(userSite) && !existingFavorites.contains(userSite))) {
				log.debug("Adding {} as a favorite for {}", userSite, userId);
				newFavorites.add(userSite);
			}
		}
		newFavorites.addAll(existingFavorites);

		if( !newFavorites.equals(existingFavorites) || firstTimeFavs ) {
			// There are new favourites and need to update database. 
			// We will not lock database if it's not neccessary
			PreferencesEdit edit = null;
			try {
				edit = preferencesService.edit(userId);
				ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
				if (firstTimeFavs) {
					props.removeProperty(FIRST_TIME_PROPERTY);
					props.addProperty(FIRST_TIME_PROPERTY, String.valueOf(false));
				}
				props.removeProperty(SEEN_SITES_PROPERTY);
				for (String userSite : userSites) {
					props.addPropertyToList(SEEN_SITES_PROPERTY, userSite);
				}
				props.removeProperty(FAVORITES_PROPERTY);
				for (String siteId : newFavorites) {
					props.addPropertyToList(FAVORITES_PROPERTY, siteId);
				}

				preferencesService.commit(edit);
			}
			catch (PermissionException | InUseException | IdUnusedException e) {
				log.info("Exception editing user preferences", e);
				preferencesService.cancel(edit);
			}
		}

		return newFavorites;
	}

	private void saveUserFavorites(String userId, UserFavorites favorites) throws PortalHandlerException {
		if (userId == null) {
			return;
		}

		PreferencesEdit edit = null;
		try {
			edit = preferencesService.edit(userId);
			ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

			// Replace all existing values
			props.removeProperty(FAVORITES_PROPERTY);

			for (String siteId : favorites.favoriteSiteIds) {
				props.addPropertyToList(FAVORITES_PROPERTY, siteId);
			}

			props.removeProperty(AUTO_FAVORITE_ENABLED_PROPERTY);
			props.addProperty(AUTO_FAVORITE_ENABLED_PROPERTY, String.valueOf(favorites.autoFavoritesEnabled));

			preferencesService.commit(edit);
		}
		catch (PermissionException | InUseException | IdUnusedException e) {
			log.info("Exception editing user preferences", e);
			preferencesService.cancel(edit);
		}
	}

	public static class UserFavorites {
		public Set<String> favoriteSiteIds;
		public boolean autoFavoritesEnabled;

		public UserFavorites() {
			favoriteSiteIds = Collections.<String>emptySet();
			autoFavoritesEnabled = false;
		}

		public String toJSON() {
			JSONObject obj = new JSONObject();

			obj.put("autoFavoritesEnabled", autoFavoritesEnabled);
			obj.put("favoriteSiteIds", new ArrayList<String>(favoriteSiteIds));

			return obj.toString();
		}

		public static UserFavorites fromJSON(String json) throws ParseException {
			JSONParser parser = new JSONParser();

			JSONObject obj = (JSONObject)parser.parse(json);

			UserFavorites result = new UserFavorites();
			result.favoriteSiteIds = new LinkedHashSet<String>();

			if (obj.get("favoriteSiteIds") != null) {
				// Site IDs might be numeric, so coerce everything to strings.
				for (Object siteId : (List<String>)obj.get("favoriteSiteIds")) {
					if (siteId != null) {
						result.favoriteSiteIds.add(siteId.toString());
					}
				}
			}

			result.autoFavoritesEnabled = (Boolean)obj.get("autoFavoritesEnabled");

			return result;
		}
	}
}
