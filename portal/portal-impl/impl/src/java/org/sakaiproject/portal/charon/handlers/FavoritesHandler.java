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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
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
	private PreferencesService preferencesService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;

	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
		preferencesService = ComponentManager.get(PreferencesService.class);
		portalService = ComponentManager.get(PortalService.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		siteService = ComponentManager.get(SiteService.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	}


	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				String userId = session.getUserId();
				updateUserFavorites(userId);
				res.setContentType(ContentType.APPLICATION_JSON.toString());
				res.getWriter().write(getUserFavorites(userId).toJSON());
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
				FavoriteSites favorites = FavoriteSites.fromJSON(req.getParameter("userFavorites"));
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

	public FavoriteSites getUserFavorites(String userId) {
		FavoriteSites favoriteSites = new FavoriteSites();
		favoriteSites.favoriteSiteIds = portalService.getPinnedSites(userId);
		return favoriteSites;
	}

	public void updateUserFavorites(final String userId) {
		if (StringUtils.isBlank(userId)) return;

		Preferences prefs = preferencesService.getPreferences(userId);
		ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
		List<String> excludedSites = Optional.ofNullable(props.getPropertyList(PreferencesService.SITENAV_PREFS_EXCLUDE_KEY)).orElseGet(Collections::emptyList);

		List<String> pinnedSites = portalService.getPinnedSites(userId);
		List<String> unPinnedSites =  portalService.getUnpinnedSites(userId);
		List<String> combinedSiteIds = Stream.concat(pinnedSites.stream(), unPinnedSites.stream()).collect(Collectors.toList());

		// when the user has no sites it is most likely their first login since pinning was introduced
		if (combinedSiteIds.isEmpty()) {
			log.debug("User has no pinned site data performing favorites migration for user [{}]", userId);
			// look to the users favorites stored in preferences
			List<String> favoriteSiteIds = props.getPropertyList(FAVORITES_PROPERTY);
			if (favoriteSiteIds != null && !favoriteSiteIds.isEmpty()) {
				favoriteSiteIds.stream()
						.map(id -> {
                            try {
                                return siteService.getSiteVisit(id);
                            } catch (IdUnusedException | PermissionException e) {
                                return null;
                            }
                        })
						.filter(Objects::nonNull)
						.filter(site -> site.getMember(userId).isActive())
						.map(Site::getId)
						.forEach(id -> {
							log.debug("Adding site [{}] from favorites to pinned sites for user [{}]", id, userId);
							portalService.addPinnedSite(userId, id, true);
							combinedSiteIds.add(id);
						});
			}

			List<String> seenSiteIds = props.getPropertyList(SEEN_SITES_PROPERTY);
			if (seenSiteIds != null && !seenSiteIds.isEmpty()) {
				seenSiteIds.stream()
						.map(id -> {
							try {
								return siteService.getSiteVisit(id);
							} catch (IdUnusedException | PermissionException e) {
								return null;
							}
						})
						.filter(Objects::nonNull)
						.filter(site -> site.getMember(userId).isActive())
						.map(Site::getId)
						.forEach(id -> {
							log.debug("Adding site [{}] from unseen to unpinned sites for user [{}]", id, userId);
							portalService.addPinnedSite(userId, id, false);
							combinedSiteIds.add(id);
						});
			}
			if (favoriteSiteIds != null || seenSiteIds != null) {
				removeFavoritesData(userId);
			}
		}

		// Remove newly hidden sites from pinned and unpinned sites
		combinedSiteIds.stream().filter(excludedSites::contains).forEach(siteId -> portalService.removePinnedSite(userId, siteId));
		combinedSiteIds.addAll(excludedSites);

		// This should not call getUserSites(boolean, boolean) because the property is variable, while the call is cacheable otherwise
		List<String> userSiteIds = siteService.getSiteIds(SelectionType.MEMBER, null, null, null, SortType.CREATED_ON_DESC, null);

		userSiteIds.stream()
				.filter(Predicate.not(combinedSiteIds::contains))
				.map(id -> {
					try {
						return siteService.getSiteVisit(id);
					} catch (IdUnusedException | PermissionException e) {
						log.warn("Could not access site with id [{}], {}", id, e.toString());
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(site -> site.getMember(userId).isActive())
				.map(Site::getId)
				.peek(id -> log.debug("Adding pinned site [{}] for user [{}]", id, userId))
				.forEach(id -> portalService.addPinnedSite(userId, id, true));
	}

	private void removeFavoritesData(String userId) {
		PreferencesEdit edit = null;
		try {
			edit = preferencesService.edit(userId);
		} catch (Exception e) {
			log.warn("Could not get the preferences for user [{}], {}", userId, e.toString());
		}

		if (edit != null) {
			try {
				ResourcePropertiesEdit props = edit.getPropertiesEdit(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
				log.debug("Clearing favorites data from preferences for user [{}]", userId);
				props.removeProperty(FIRST_TIME_PROPERTY);
				props.removeProperty(SEEN_SITES_PROPERTY);
				props.removeProperty(FAVORITES_PROPERTY);
			} catch (Exception e) {
				log.warn("Could not remove favorites data for user [{}], {}", userId, e.toString());
				preferencesService.cancel(edit);
				edit = null; // set to null since it was cancelled, prevents commit in finally
			} finally {
				if (edit != null) preferencesService.commit(edit);
			}
		}
	}

	private void saveUserFavorites(String userId, FavoriteSites favorites, boolean reorder) throws PortalHandlerException {

		if (userId == null) {
			return;
		}

        if (reorder) {
            portalService.reorderPinnedSites(favorites.favoriteSiteIds);
        } else {
		    portalService.savePinnedSites(favorites.favoriteSiteIds);
        }
	}

	public static class FavoriteSites {

		public List<String> favoriteSiteIds;
		public boolean autoFavoritesEnabled;

		public FavoriteSites() {
			favoriteSiteIds = Collections.emptyList();
			autoFavoritesEnabled = false;
		}

		public String toJSON() {
			JSONObject obj = new JSONObject();

			obj.put("autoFavoritesEnabled", autoFavoritesEnabled);
			obj.put("favoriteSiteIds", favoriteSiteIds);

			return obj.toString();
		}

		public static FavoriteSites fromJSON(String json) throws ParseException {
			JSONParser parser = new JSONParser();

			JSONObject obj = (JSONObject)parser.parse(json);

			FavoriteSites result = new FavoriteSites();
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
