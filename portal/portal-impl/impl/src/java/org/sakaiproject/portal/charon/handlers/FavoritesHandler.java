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
import java.util.List;
import org.sakaiproject.entity.api.ResourceProperties;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.IdUnusedException;

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


	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
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
		boolean autoFavorite = true;

		try {
			autoFavorite = props.getBooleanProperty(AUTO_FAVORITE_ENABLED_PROPERTY);
		} catch (EntityPropertyNotDefinedException e) {
			// Take the default
		}  catch (EntityPropertyTypeException e) {
			// Take the default
		}

		result.autoFavoritesEnabled = autoFavorite;

		List<String> favoriteSiteIds = (List<String>)props.getPropertyList(FAVORITES_PROPERTY);

		if (favoriteSiteIds == null) {
			favoriteSiteIds = Collections.<String>emptyList();
		}

		if (autoFavorite) {
			// If the user wants new sites to be automatically added as favorites, slot them in now.
			result.favoriteSiteIds = applyAutoFavorites(userId, props, favoriteSiteIds);
		} else {
			result.favoriteSiteIds = favoriteSiteIds;
		}

		return result;
	}

	private static List<String> applyAutoFavorites(String userId, ResourceProperties existingProps, List<String> existingFavorites)
		throws PermissionException, PortalHandlerException, InUseException, IdUnusedException {

		// The site list as when we last checked
		List<String> oldSiteList = (List<String>)existingProps.getPropertyList(SEEN_SITES_PROPERTY);

		if (oldSiteList == null) {
			oldSiteList = Collections.<String>emptyList();
		}

		List<Site> userSites = SiteService.getUserSites(false);
		List<String> newFavorites = new ArrayList<String>();

		for (Site userSite : userSites) {
			if (!oldSiteList.contains(userSite.getId()) && !existingFavorites.contains(userSite.getId())) {
				newFavorites.add(userSite.getId());
			}
		}

		if (newFavorites.isEmpty()) {
			// No change!  Don't bother writing back to the DB
			return existingFavorites;
 		}

		newFavorites.addAll(existingFavorites);

		// Add our new sites to the list of user favorites and update our list of seen sites
		PreferencesEdit edit = PreferencesService.edit(userId);
		ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);

		// Add our new properties and any existing favorite sites after them
		props.removeProperty(FAVORITES_PROPERTY);
		for (String siteId : newFavorites) {
			props.addPropertyToList(FAVORITES_PROPERTY, siteId);
		}

		// Store our new seen sites
		props.removeProperty(SEEN_SITES_PROPERTY);
		for (Site site : userSites) {
			props.addPropertyToList(SEEN_SITES_PROPERTY, site.getId());
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
		public List<String> favoriteSiteIds;
		public boolean autoFavoritesEnabled;

		public UserFavorites() {
			favoriteSiteIds = Collections.<String>emptyList();
			autoFavoritesEnabled = false;
		}

		public String toJSON() {
			JSONObject obj = new JSONObject();

			obj.put("autoFavoritesEnabled", autoFavoritesEnabled);
			obj.put("favoriteSiteIds", favoriteSiteIds);

			return obj.toString();
		}

		public static UserFavorites fromJSON(String json) throws ParseException {
			JSONParser parser = new JSONParser();

			JSONObject obj = (JSONObject)parser.parse(json);

			UserFavorites result = new UserFavorites();
			result.favoriteSiteIds = (List<String>)obj.get("favoriteSiteIds");
			result.autoFavoritesEnabled = (Boolean)obj.get("autoFavoritesEnabled");

			return result;
		}
	}
}
