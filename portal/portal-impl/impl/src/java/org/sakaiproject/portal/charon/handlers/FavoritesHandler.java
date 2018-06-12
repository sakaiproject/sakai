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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.Preferences;
import java.util.Collections;
import org.sakaiproject.tool.cover.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.sakaiproject.entity.api.ResourceProperties;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Handler for manipulating the list of favorite sites for the current user.
 * Handles AJAX requests from the "More Sites" drawer.
 *
 */
public class FavoritesHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "favorites";
	private static final String SEPARATOR = ";";
	private static final String FAVORITES_PROPERTY = "order";
	private static final String AUTO_FAVORITE_ENABLED_PROPERTY = "autoFavoriteEnabled";
	private static final String SEEN_SITES_PROPERTY = "autoFavoritesSeenSites";
	private static final String FIRST_TIME_PROPERTY = "firstTime";
	private ServerConfigurationService serverConfigurationService;

	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
		serverConfigurationService = (ServerConfigurationService) 
				ComponentManager.get(ServerConfigurationService.class);
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

		Preferences prefs = PreferencesService.getPreferences(userId);
		ResourceProperties props = prefs.getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

		// Find any sites that this user was added to since we last looked
		boolean autoFavorite = serverConfigurationService.getBoolean("portal.autofavorite", true);

		try {
			autoFavorite = props.getBooleanProperty(AUTO_FAVORITE_ENABLED_PROPERTY);
		} catch (EntityPropertyNotDefinedException e) {
			// Take the default
		}  catch (EntityPropertyTypeException e) {
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

	private static Set<String> applyAutoFavorites(String userId, ResourceProperties existingProps, Set<String> existingFavorites)
		throws PermissionException, PortalHandlerException, InUseException, IdUnusedException {

		// The site list as when we last checked
		Set<String> oldSiteSet = Collections.<String>emptySet();
		List<String> oldSiteList = (List<String>)existingProps.getPropertyList(SEEN_SITES_PROPERTY);
		if (oldSiteList != null) {
			oldSiteSet = new HashSet<String>(oldSiteList);
		}

		boolean firstTimeFavs = true;
		try {
			firstTimeFavs = existingProps.getBooleanProperty(FIRST_TIME_PROPERTY);
		} catch (EntityPropertyNotDefinedException e) {
			// Take the default
		} catch (EntityPropertyTypeException e) {
			// Take the default
		}

		// Update our list of seen sites
		PreferencesEdit edit = PreferencesService.edit(userId);
		ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

		// This should not call getUserSites(boolean, boolean) because the property is variable, while the call is cacheable otherwise
		List<Site> userSites = SiteService.getSites(SelectionType.MEMBER, null, null, null, SortType.TITLE_ASC, null, false);
		Set<String> newFavorites = new LinkedHashSet<String>();

		props.removeProperty(SEEN_SITES_PROPERTY);
		for (Site userSite : userSites) {
			if (!oldSiteSet.contains(userSite.getId()) && !existingFavorites.contains(userSite.getId()) && !firstTimeFavs) {
				newFavorites.add(userSite.getId());
			}
			props.addPropertyToList(SEEN_SITES_PROPERTY, userSite.getId());
		}
		newFavorites.addAll(existingFavorites);

		if (firstTimeFavs) {
			props.removeProperty(FIRST_TIME_PROPERTY);
			props.addProperty(FIRST_TIME_PROPERTY, String.valueOf(false));
		}

		// Add our new properties and any existing favorite sites after them
		props.removeProperty(FAVORITES_PROPERTY);
		for (String siteId : newFavorites) {
			props.addPropertyToList(FAVORITES_PROPERTY, siteId);
		}

		PreferencesService.commit(edit);

		return newFavorites;
	}

	private void saveUserFavorites(String userId, UserFavorites favorites) throws PermissionException, InUseException, IdUnusedException, PortalHandlerException {
		if (userId == null) {
			return;
		}

		PreferencesEdit edit = PreferencesService.edit(userId);
		ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

		// Replace all existing values
		props.removeProperty(FAVORITES_PROPERTY);

		for (String siteId : favorites.favoriteSiteIds) {
			props.addPropertyToList(FAVORITES_PROPERTY, siteId);
		}

		props.removeProperty(AUTO_FAVORITE_ENABLED_PROPERTY);
		props.addProperty(AUTO_FAVORITE_ENABLED_PROPERTY, String.valueOf(favorites.autoFavoritesEnabled));

		PreferencesService.commit(edit);
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
			result.favoriteSiteIds = new LinkedHashSet<String>((List<String>)obj.get("favoriteSiteIds"));
			result.autoFavoritesEnabled = (Boolean)obj.get("autoFavoritesEnabled");

			return result;
		}
	}
}
