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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;

import org.apache.commons.lang3.StringUtils;

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

	private PortalService portalService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;

	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
		portalService = (PortalService) ComponentManager.get(PortalService.class);
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
                boolean reorder = StringUtils.equals("true", req.getParameter("reorder"));

				synchronized (session) {
					saveUserFavorites(session.getUserId(), favorites, reorder);
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

		result.favoriteSiteIds = portalService.getPinnedSites();

		return result;
	}

	private void saveUserFavorites(String userId, UserFavorites favorites, boolean reorder) throws PortalHandlerException {

		if (userId == null) {
			return;
		}

        if (reorder) {
            portalService.reorderPinnedSites(favorites.favoriteSiteIds);
        } else {
		    portalService.savePinnedSites(favorites.favoriteSiteIds);
        }
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
			result.favoriteSiteIds = new ArrayList<String>();

			if (obj.get("favoriteSiteIds") != null) {
				// Site IDs might be numeric, so coerce everything to strings.
				for (Object siteId : (List<String>)obj.get("favoriteSiteIds")) {
					if (siteId != null) {
						result.favoriteSiteIds.add(siteId.toString());
					}
				}
			}

			return result;
		}
	}
}
